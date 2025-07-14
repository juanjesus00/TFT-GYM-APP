from flask import Flask, request, jsonify, send_from_directory, abort
import os
import uuid
import json
import jwt
import firebase_admin
from firebase_admin import credentials, auth
import datetime
from functools import wraps
from werkzeug.utils import secure_filename
from pathlib import Path

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['PROCESSED_FOLDER'] = 'processed'
app.config['RESULTS_FOLDER'] = 'results'
app.config['PROGRESS_FOLDER'] = 'progress'
app.config['TASK_QUEUE'] = 'task_queue.json'
app.config['ALLOWED_EXTENSIONS'] = {'mp4', 'mov', 'avi'}
cred = credentials.Certificate("firebase_key.json")
firebase_admin.initialize_app(cred)
TOKENS_FILE = "worker_tokens.json"
# ======================== UTILS ============================

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in app.config['ALLOWED_EXTENSIONS']

def save_task_to_queue(task):
    queue_file = app.config['TASK_QUEUE']
    if os.path.exists(queue_file):
        with open(queue_file, 'r') as f:
            tasks = json.load(f)
    else:
        tasks = []

    tasks.append(task)

    with open(queue_file, 'w') as f:
        json.dump(tasks, f, indent=2)

def load_pending_tasks():
    queue_file = app.config['TASK_QUEUE']
    if not os.path.exists(queue_file):
        return []

    with open(queue_file, 'r') as f:
        tasks = json.load(f)

    # Filtra los que aún no tienen resultado
    pending = []
    for task in tasks:
        analysis_id = task['analysis_id']
        result_file = os.path.join(app.config['RESULTS_FOLDER'], f"{analysis_id}.json")
        if not os.path.exists(result_file):
            pending.append(task)

    return pending

def load_worker_tokens():
    if not os.path.exists(TOKENS_FILE):
        return []
    
    try:
        with open(TOKENS_FILE, 'r') as f:
            return json.load(f)
    except:
        return []
def require_firebase_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get("Authorization")
        if not token or not token.startswith("Bearer "):
            return jsonify({"error": "Missing or malformed token"}), 401

        token = token[7:]
        try:
            decoded_token = auth.verify_id_token(token)
            request.user_id = decoded_token["uid"]
        except Exception as e:
            print(f"[AUTH ERROR] {e}")
            return jsonify({"error": "Invalid or expired token"}), 403

        return f(*args, **kwargs)
    return decorated

def require_worker_token(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get("x-worker-token")
        valid_tokens = load_worker_tokens()
        
        if not token or token not in valid_tokens:
            print(f"Intento de acceso no autorizado con token: {token}")
            return jsonify({"error": "Unauthorized worker"}), 401
        
        return f(*args, **kwargs)
    return decorated

# ======================== ENDPOINTS ============================



@app.route('/analyze', methods=['POST'])
@require_firebase_auth
def analyze_video():
    if 'video' not in request.files:
        return jsonify({"error": "No video uploaded"}), 400

    file = request.files['video']
    if file.filename == '':
        return jsonify({"error": "Empty filename"}), 400

    if not allowed_file(file.filename):
        return jsonify({"error": "Invalid file type"}), 415

    exercise = request.form.get("exercise")
    
    if not exercise:
        return jsonify({"error" : "Exercise not provided"}), 400
    
    analysis_id = str(uuid.uuid4())
    filename = secure_filename(f"{analysis_id}_{file.filename}")
    save_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(save_path)

    task = {
        "analysis_id": analysis_id,
        "filename": filename,
        "exercise": exercise
    }

    save_task_to_queue(task)

    return jsonify({
        "analysis_id": analysis_id,
        "exercise": exercise,
        "status": "queued",
        "message": "Video recibido. En espera de análisis."
    }), 202

@app.route('/pending_tasks', methods=['GET'])
@require_worker_token
def get_pending_tasks():
    tasks = load_pending_tasks()
    return jsonify(tasks), 200

@app.route('/upload_result', methods=['POST'])
@require_worker_token
def upload_result():
    analysis_id = request.form.get('analysis_id')
    result_json = request.form.get('result')
    file = request.files.get('video')

    if not analysis_id or not result_json or not file:
        return jsonify({"error": "Datos incompletos"}), 400

    # Guardar vídeo procesado
    processed_path = os.path.join(app.config['PROCESSED_FOLDER'], f"processed_{analysis_id}.mp4")
    file.save(processed_path)

    # Guardar resultado JSON
    result_path = os.path.join(app.config['RESULTS_FOLDER'], f"{analysis_id}.json")
    with open(result_path, 'w') as f:
        f.write(result_json)

    return jsonify({"status": "received"}), 200

@app.route('/results/<analysis_id>', methods=['GET'])
@require_firebase_auth
def get_results(analysis_id):
    result_file = os.path.join(app.config['RESULTS_FOLDER'], f"{analysis_id}.json")
    if not os.path.exists(result_file):
        return jsonify({"status": "processing"}), 202

    with open(result_file, 'r') as f:
        results = json.load(f)

    return jsonify({
        "status": "completed",
        "results": results
    }), 200

@app.route('/processed/<analysis_id>', methods=['GET'])
@require_firebase_auth
def serve_processed_video(analysis_id):
    filename = f"processed_{analysis_id}.mp4"
    if not os.path.exists(os.path.join(app.config['PROCESSED_FOLDER'], filename)):
        return jsonify({"error": "Video no encontrado"}), 404

    return send_from_directory(
        app.config['PROCESSED_FOLDER'],
        filename,
        mimetype='video/mp4',
        as_attachment=True
    )
    
@app.route('/uploads/<filename>', methods=['GET'])
@require_worker_token
def serve_upload(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'],
                               filename,
                               as_attachment=True)

@app.route('/progress/<analysis_id>', methods=['GET'])
@require_firebase_auth
def get_progress(analysis_id):
    """Devuelve el porcentaje actual (0-100) del análisis."""
    prog_file = os.path.join(app.config['PROGRESS_FOLDER'],
                             f"{analysis_id}.txt")

    # Si ya existe resultado → 100 %
    if os.path.exists(os.path.join(app.config['RESULTS_FOLDER'],
                                   f"{analysis_id}.json")):
        return jsonify({"progress": 100}), 200

    if not os.path.exists(prog_file):
        return jsonify({"progress": 0}), 200

    with open(prog_file) as f:
        pct = int(f.read().strip() or 0)
    return jsonify({"progress": pct}), 200


@app.route('/progress/<analysis_id>', methods=['POST'])
@require_worker_token
def update_progress(analysis_id):
    """Recibe {progress: int} desde el worker y lo guarda."""
    data = request.get_json(silent=True) or {}
    pct  = int(data.get("progress", 0))
    pct  = max(0, min(100, pct))           # clamp 0-100

    prog_file = os.path.join(app.config['PROGRESS_FOLDER'],
                             f"{analysis_id}.txt")
    with open(prog_file, 'w') as f:
        f.write(str(pct))

    return jsonify({"stored": pct}), 200

# ======================== MAIN ============================

if __name__ == '__main__':
    os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
    os.makedirs(app.config['PROCESSED_FOLDER'], exist_ok=True)
    os.makedirs(app.config['RESULTS_FOLDER'], exist_ok=True)
    os.makedirs(app.config['PROGRESS_FOLDER'], exist_ok=True)
    if not os.path.exists(app.config['TASK_QUEUE']):
        with open(app.config['TASK_QUEUE'], 'w') as f:
            json.dump([], f)

    app.run(host='0.0.0.0', port=5000, threaded=True)
