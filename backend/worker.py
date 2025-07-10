#!/usr/bin/env python3
"""
Worker que:
1. Pregunta al servidor Linux por tareas pendientes
2. Descarga el vídeo
3. Ejecuta VitPose (GPU)
4. Devuelve vídeo procesado + JSON de resultados
"""

import os, time, json, requests, tempfile, shutil, threading
import cv2
from VitposePlusR50 import process_video

SERVER      = "https://gymapi.org"      
POLL_SECS   = 5                            # intervalo de sondeo
UPLOAD_PROGRESS_EVERY = 1
TMP_DIR     = tempfile.gettempdir()        # dir temporal (ej. /tmp)
TOKEN_FILE = "worker_token.txt"

def load_worker_token():
    try:
        with open(TOKEN_FILE, 'r') as f:
            return f.read().strip()
    except:
        return ""
    
    
token = load_worker_token()
headers = {"x-worker-token": token} if token else {}

def download(url, dst_path):
    with requests.get(url, headers=headers, stream=True) as r:
        r.raise_for_status()
        with open(dst_path, "wb") as f:
            shutil.copyfileobj(r.raw, f)



def send_progress(server, analysis_id, pct):
    try:
        requests.post(f"{server}/progress/{analysis_id}",
                      json={"progress": pct}, headers=headers, timeout=5)
    except Exception as e:
        print(f"[WORKER] No pude mandar progreso ({pct}%):", e)

while True:
    try:
        tasks = requests.get(f"{SERVER}/pending_tasks", headers=headers, timeout=10).json()
        if not tasks:
            time.sleep(POLL_SECS)
            continue

        task = tasks[0]                        # procesa una por vuelta
        aid  = task["analysis_id"]
        fname= task["filename"]

        print(f"[WORKER] Procesando {aid}")

        # descarga vídeo original
        in_path = os.path.join(TMP_DIR, fname)
        download(f"{SERVER}/uploads/{fname}", in_path)
        
        time.sleep(0.5)
        # Comprobación: ¿es un video legible?
        cap = cv2.VideoCapture(in_path)
        if not cap.isOpened():
            print(f"[ERROR] No se pudo abrir el video {in_path}, reintentando luego.")
            continue
        cap.release()
        
        # procesa con GPU en un hilo
        out_path     = os.path.join(TMP_DIR, f"processed_{aid}.mp4")
        progress_txt = os.path.join(TMP_DIR, f"{aid}.progress")
        result_holder = {}

        def run_vitpose():
            result_holder['data'] = process_video(
                in_path, out_path, 30, 100, progress_txt
            )

        t = threading.Thread(target=run_vitpose, daemon=True)
        t.start()

        last_sent = -1
        while t.is_alive():
            if os.path.exists(progress_txt):
                try:
                    with open(progress_txt) as fp:
                        pct = int(fp.read().strip() or 0)
                    if pct != last_sent:
                        send_progress(SERVER, aid, pct)
                        last_sent = pct
                except ValueError:
                    pass
            time.sleep(UPLOAD_PROGRESS_EVERY)

        t.join()                 # asegurarse de que terminó
        send_progress(SERVER, aid, 100)   # forzamos 100 %

        result = result_holder.get('data', {})
        
        # sube resultado
        with open(out_path, "rb") as video_file:
            files = {"video": video_file}
            data  = {"analysis_id": aid, "result": json.dumps(result)}
            resp  = requests.post(f"{SERVER}/upload_result",
                                  data=data, files=files, headers=headers,timeout=30)
            print(f"[WORKER] Servidor respondió {resp.status_code}")

        # limpieza
        for path in (in_path, out_path, progress_txt):
            if os.path.exists(path):
                os.remove(path)

    except Exception as e:
        print("[WORKER] Error:", e)

    time.sleep(POLL_SECS)
