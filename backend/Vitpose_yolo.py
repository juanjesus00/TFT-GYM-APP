import torch
import cv2
import os
import numpy as np
from PIL import Image
from transformers import AutoProcessor, RTDetrForObjectDetection, VitPoseForPoseEstimation
from collections import deque  # Para el suavizado temporal
from ultralytics import YOLO
import json
import datetime

class AngleTracker:
    def __init__(self, buffer_size=5):
        self.buffer = deque(maxlen=buffer_size)
    
    def update(self, angle):
        if angle is not None:
            self.buffer.append(angle)
            return np.mean(self.buffer)
        return None
        
class repetition:
    def __init__(self, original_fps):
        self.stateCounting = False
        self.coutingEnable = False
        self.start_frame = None
        self.repDuration = 0
        self.repsDurations = []
        self.reps = 0
        self.original_fps = original_fps  # Almacenamos los FPS originales
        self.rep_frames = 0
    def calculate_repetition(self, elbowsAngels, extensionAngle, contractionAngle, current_frame):
        if self.stateCounting == False:
            if np.mean(elbowsAngels) > extensionAngle:
                self.stateCounting = True
                if self.coutingEnable:
                    self.coutingEnable = False
                    # Calcular duración basada en los frames del video original
                    self.rep_frames = current_frame - self.start_frame + 1
                    self.repDuration = self.rep_frames / self.original_fps
                    self.repsDurations.append(self.repDuration)
                    self.start_frame = None
                    self.reps += 1
                    
        if self.stateCounting:
            if np.mean(elbowsAngels) < contractionAngle:
                self.stateCounting = False
                self.coutingEnable = True
                self.start_frame = current_frame  # Registrar el frame inicial de la repetición
        return self.reps
    def get_last_rep_data(self):
        return {
            'frames': self.rep_frames,
            'seconds': self.repDuration,
            'speed': 1 / self.repDuration if self.repDuration > 0 else 0  # Reps/segundo
        }
        
    def repsMeanDuration(self):
        return np.mean(self.repsDurations)
    
    
# Configuración de dispositivo
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"Usando dispositivo: {device}")

# Parámetros ajustables
MIN_AREA = 5000 #2000          # Área mínima en píxeles para considerar como persona válida
BUFFER_SIZE = 5          # Tamaño del buffer para suavizado temporal
CENTER_PRIORITY = True   # Priorizar personas cerca del centro del frame

COCO_KEYPOINTS = [
    "nose", "left_eye", "right_eye", "left_ear", "right_ear",
    "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
    "left_wrist", "right_wrist", "left_hip", "right_hip",
    "left_knee", "right_knee", "left_ankle", "right_ankle"
]
ANGLE_COLORS = {
    'left_knee': (0, 255, 255),
    'right_knee': (0, 165, 255),
    'left_elbow': (255, 255, 0),
    'right_elbow': (255, 0, 255)
}

# Cargar modelos
yolo_model = YOLO("yolov8x.pt")
pose_processor = AutoProcessor.from_pretrained("usyd-community/vitpose-plus-large")
pose_model = VitPoseForPoseEstimation.from_pretrained("usyd-community/vitpose-plus-large").to(device)

#pose_processor = AutoProcessor.from_pretrained("vitpose_finetuned2/vitpose_finetuned")
#pose_model = VitPoseForPoseEstimation.from_pretrained("vitpose_finetuned2/vitpose_finetuned/best_model").to(device)

#pose_processor = AutoProcessor.from_pretrained("usyd-community/vitpose-plus-huge")
#pose_model = VitPoseForPoseEstimation.from_pretrained("usyd-community/vitpose-plus-huge").to(device)

# Configuración de visualización
keypoint_edges = pose_model.config.edges
palette = np.array([[255,128,0],[255,153,51],[255,178,102],[230,230,0],[255,153,255],
                   [153,204,255],[255,102,255],[255,51,255],[102,178,255],[51,153,255],
                   [255,153,153],[255,102,102],[255,51,51],[153,255,153],[102,255,102],
                   [51,255,51],[0,255,0],[0,0,255],[255,0,0],[255,255,255]])
link_colors = palette[[0,0,0,0,7,7,7,9,9,9,9,9,16,16,16,16,16,16,16]]
keypoint_colors = palette[[16,16,16,16,16,9,9,9,9,9,9,0,0,0,0,0,0]]

# Buffer para suavizado temporal
position_buffer = deque(maxlen=BUFFER_SIZE)


def get_keypoint_coords(keypoints, scores, part_name, confidence_threshold=0.3):
    """Obtiene coordenadas de una parte específica del cuerpo"""
    part_index = COCO_KEYPOINTS.index(part_name)
    if scores[part_index] > confidence_threshold:
        return (int(keypoints[part_index][0]), int(keypoints[part_index][1]))
    return None

def calculate_angle(a, b, c):
    """Calcula el ángulo entre tres puntos en grados"""
    if None in [a, b, c]:
        return None
    
    # Convertir a arrays numpy para cálculos vectoriales
    a = np.array(a)
    b = np.array(b)
    c = np.array(c)
    
    ba = a - b
    bc = c - b
    
    dot_product = np.dot(ba, bc)
    norm_ba = np.linalg.norm(ba)
    norm_bc = np.linalg.norm(bc)
    
    # Evitar división por cero
    if norm_ba == 0 or norm_bc == 0:
        return None
    
    cosine_angle = dot_product / (norm_ba * norm_bc)
    angle = np.degrees(np.arccos(np.clip(cosine_angle, -1.0, 1.0)))
    return angle
    
# Inicializar trackers para cada articulación
left_knee_tracker = AngleTracker()
right_knee_tracker = AngleTracker()
left_elbow_tracker = AngleTracker()
right_elbow_tracker = AngleTracker()



def select_closest_person(person_boxes, frame_size):
    """Selecciona la persona más cercana basada en tamaño y posición"""
    if person_boxes.size == 0:
        return np.empty((0, 4))
    
    # Calcular áreas y filtrar por tamaño mínimo
    widths = person_boxes[:, 2] - person_boxes[:, 0]
    heights = person_boxes[:, 3] - person_boxes[:, 1]
    areas = widths * heights
    
    valid_indices = np.where(areas > MIN_AREA)[0]
    if valid_indices.size == 0:
        return np.empty((0, 4))
    
    person_boxes = person_boxes[valid_indices]
    areas = areas[valid_indices]
    
    if CENTER_PRIORITY:
        # Priorizar personas cerca del centro
        height, width = frame_size
        frame_center = np.array([width/2, height/2])
        box_centers = np.array([[(x1 + x2)/2, (y1 + y2)/2] 
                              for x1, y1, x2, y2 in person_boxes])
        distances = np.linalg.norm(box_centers - frame_center, axis=1)
        closest_idx = np.argmin(distances)
    else:
        # Seleccionar por área máxima
        closest_idx = np.argmax(areas)
    
    selected_box = person_boxes[closest_idx:closest_idx+1]
    
    # Suavizado temporal
    position_buffer.append(selected_box[0])
    smoothed_box = np.mean(position_buffer, axis=0)
    
    return np.array([smoothed_box])
 
 
def pose_results_to_coco(image, pose_results, image_id=1, annotation_id=1):
    coco_data = {
        "info": {
            "description": "Pose Estimation Results",
            "version": "1.0",
            "year": datetime.datetime.now().year,
            "date_created": datetime.datetime.now().isoformat()
        },
        "licenses": [],
        "categories": [{
            "id": 1,
            "name": "person",
            "keypoints": COCO_KEYPOINTS,
            "skeleton": [list(map(int, edge)) for edge in pose_model.config.edges]
        }],
        "images": [],
        "annotations": []
    }
    
    # Añadir imagen
    height, width = image.shape[:2]
    coco_data["images"].append({
        "id": int(image_id),
        "width": int(width),
        "height": int(height),
        "file_name": "input_image.jpg",
        "license": int(1),
        "date_captured": datetime.datetime.now().isoformat()
    })
    
    # Procesar cada persona
    for person in pose_results[0]:
        keypoints = person["keypoints"].cpu().numpy()
        scores = person["scores"].cpu().numpy()
        
        # Keypoints en formato COCO
        coco_kps = []
        for kp, score in zip(keypoints, scores):
            coco_kps.extend([
                float(kp[0]),
                float(kp[1]),
                float(score)
            ])
        
        # Calcular bbox
        valid_mask = scores > 0.3
        if not np.any(valid_mask):
            continue
            
        valid_kps = keypoints[valid_mask]
        x_min, y_min = np.min(valid_kps[:, :2], axis=0)
        x_max, y_max = np.max(valid_kps[:, :2], axis=0)
        
        # Crear anotación
        annotation = {
            "id": int(annotation_id),
            "image_id": int(image_id),
            "category_id": int(1),
            "keypoints": coco_kps,
            "num_keypoints": int(np.sum(valid_mask)),
            "bbox": [
                float(x_min),
                float(y_min),
                float(x_max - x_min),
                float(y_max - y_min)
            ],
            "area": float((x_max - x_min) * (y_max - y_min)),
            "iscrowd": int(0),
            "scores": [float(s) for s in scores.tolist()]
        }
        
        coco_data["annotations"].append(annotation)
        annotation_id += 1
    
    return coco_data
 
            
def process_video(input_path, output_path=None, target_fps=30, display_scale=100):
    
    # Configurar entrada de video
    cap = cv2.VideoCapture(input_path)
    original_fps = cap.get(cv2.CAP_PROP_FPS)
    frame_skip = max(1, int(original_fps / target_fps))
    
    # Inicializar contador de repeticiones
    repsCounter = repetition(original_fps)
    
    # Configurar salida de video
    if output_path:
        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        out = cv2.VideoWriter(output_path, fourcc, target_fps, (width, height))
    
    frame_count = 0
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
            
        frame_count += 1
        if frame_count % frame_skip != 0:
            continue
        
        current_frame = frame_count - 1 # obtencion de frames actuales  
        # Convertir frame a PIL Image
        pil_image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        frame_size = pil_image.size[::-1]  # (height, width)
        
        # ---------------------------------------------------------
        # Etapa 1: Detección de personas
        # ---------------------------------------------------------
        
        # Convertir PIL a numpy si es necesario
        numpy_image = np.array(pil_image)

        # Ejecutar detección con YOLO
        yolo_results = yolo_model.predict(
            source=numpy_image,
            conf=0.25,         # baja confianza mínima (default es 0.25, prueba 0.1 si es muy estricto)
            iou=0.7,           # mayor solapamiento permitido entre detecciones (default 0.7)
            classes=[0],       # solo detectar personas (clase 0 en COCO)
            device=0 if torch.cuda.is_available() else 'cpu',
            verbose=False
        )[0]


        # Extraer bounding boxes de personas (clase 0 en COCO = 'person')
        person_boxes = []
        for box, cls in zip(yolo_results.boxes.xyxy.cpu().numpy(), yolo_results.boxes.cls.cpu().numpy()):
            if int(cls) == 0:
                person_boxes.append(box)

        person_boxes = np.array(person_boxes)
        
        # Seleccionar persona más cercana
        person_boxes = select_closest_person(person_boxes, frame_size)
        if person_boxes.size == 0:
            continue
            
        # Convertir formato de cajas (VOC a COCO)
        person_boxes[:, 2] -= person_boxes[:, 0]
        person_boxes[:, 3] -= person_boxes[:, 1]
        
        # ---------------------------------------------------------
        # Etapa 2: Estimación de postura
        # ---------------------------------------------------------
        inputs = pose_processor(
            images=pil_image, 
            boxes=[person_boxes], 
            return_tensors="pt"
        ).to(device)
        
        # Añade el dataset_index manualmente (ejemplo con índice 0 para COCO)
        inputs["dataset_index"] = torch.tensor([0], device=inputs["pixel_values"].device)  # Ajusta el índice según tu dataset
        
        with torch.no_grad():
            outputs = pose_model(**inputs)
            
        pose_results = pose_processor.post_process_pose_estimation(outputs, boxes=[person_boxes])
        
        # ---------------------------------------------------------
        # Visualización
        # ---------------------------------------------------------
        numpy_image = np.array(pil_image)
        for pose_result in pose_results[0]:
            scores = pose_result["scores"].cpu().numpy()
            keypoints = pose_result["keypoints"].cpu().numpy()
            
            # Piernas
            left_hip = get_keypoint_coords(keypoints, scores, 'left_hip')
            left_knee = get_keypoint_coords(keypoints, scores, 'left_knee')
            left_ankle = get_keypoint_coords(keypoints, scores, 'left_ankle')
            
            right_hip = get_keypoint_coords(keypoints, scores, 'right_hip')
            right_knee = get_keypoint_coords(keypoints, scores, 'right_knee')
            right_ankle = get_keypoint_coords(keypoints, scores, 'right_ankle')
            
            # Brazos
            left_shoulder = get_keypoint_coords(keypoints, scores, 'left_shoulder')
            left_elbow = get_keypoint_coords(keypoints, scores, 'left_elbow')
            left_wrist = get_keypoint_coords(keypoints, scores, 'left_wrist')
            
            right_shoulder = get_keypoint_coords(keypoints, scores, 'right_shoulder')
            right_elbow = get_keypoint_coords(keypoints, scores, 'right_elbow')
            right_wrist = get_keypoint_coords(keypoints, scores, 'right_wrist')
            
            # Ángulos de rodillas
            left_knee_angle = calculate_angle(left_hip, left_knee, left_ankle)
            right_knee_angle = calculate_angle(right_hip, right_knee, right_ankle)
            
            # Ángulos de codos
            left_elbow_angle = calculate_angle(left_shoulder, left_elbow, left_wrist)
            right_elbow_angle = calculate_angle(right_shoulder, right_elbow, right_wrist)
            
            #angulos suavizados
            smoothed_left_knee = left_knee_tracker.update(left_knee_angle)
            smoothed_right_knee = right_knee_tracker.update(right_knee_angle)
            smoothed_left_elbow = left_elbow_tracker.update(left_elbow_angle)
            smoothed_right_elbow = right_elbow_tracker.update(right_elbow_angle)
            
            # calculate reps
            repsCounter.calculate_repetition(np.array([smoothed_left_elbow, smoothed_right_elbow]), 135, 50, current_frame)
            
            
            # Dibujar texto con los ángulos
            font = cv2.FONT_HERSHEY_SIMPLEX
            font_scale = 0.6
            thickness = 2
            
            # Rodilla izquierda
            if left_knee and smoothed_left_knee:
                cv2.putText(numpy_image, f"{smoothed_left_knee:.1f}", 
                           (left_knee[0]+10, left_knee[1]), 
                           font, font_scale, ANGLE_COLORS['left_knee'], thickness)
            
            # Rodilla derecha
            if right_knee and smoothed_right_knee:
                cv2.putText(numpy_image, f"{smoothed_right_knee:.1f}", 
                           (right_knee[0]+10, right_knee[1]), 
                           font, font_scale, ANGLE_COLORS['right_knee'], thickness)
            
            # Codo izquierdo
            if left_elbow and smoothed_left_elbow:
                cv2.putText(numpy_image, f"{smoothed_left_elbow:.1f}", 
                           (left_elbow[0]+10, left_elbow[1]), 
                           font, font_scale, ANGLE_COLORS['left_elbow'], thickness)
            
            # Codo derecho
            if right_elbow and smoothed_right_elbow:
                cv2.putText(numpy_image, f"{smoothed_right_elbow:.1f}", 
                           (right_elbow[0]+10, right_elbow[1]), 
                           font, font_scale, ANGLE_COLORS['right_elbow'], thickness)
            
            # velocidad de cada repetición y conteo de repeticiones
            rep_data = repsCounter.get_last_rep_data()
            texto = [
                f"Rep: {repsCounter.reps} | Frames: {rep_data['frames']} |",
                f"Tiempo: {rep_data['seconds']:.2f}s | Velocidad: {rep_data['speed']:.2f} reps/s"
                ]
            y = 20
            for linea in texto:
                
                cv2.putText(numpy_image, linea, (20,y), font, font_scale, (0,0,0), thickness)
                y += 30
            
            # Dibujar puntos y conexiones
            for kpt, score in zip(keypoints, scores):
                if score > 0.3:
                    x, y = int(kpt[0]), int(kpt[1])
                    cv2.circle(numpy_image, (x, y), 5, (0,255,0), -1)
                    
            for (start, end), color in zip(keypoint_edges, link_colors):
                if scores[start] > 0.3 and scores[end] > 0.3:
                    x1, y1 = int(keypoints[start][0]), int(keypoints[start][1])
                    x2, y2 = int(keypoints[end][0]), int(keypoints[end][1])
                    cv2.line(numpy_image, (x1,y1), (x2,y2), color.tolist(), 2)
        
        # Mostrar resultado
        result_frame = cv2.cvtColor(numpy_image, cv2.COLOR_RGB2BGR)
        if display_scale != 100:
            h, w = result_frame.shape[:2]
            new_size = (int(w*display_scale/100), int(h*display_scale/100))
            result_frame = cv2.resize(result_frame, new_size)
            
        cv2.imshow('Video Analysis', result_frame)
        if output_path:
            out.write(result_frame)
            
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
            
    cap.release()
    if output_path:
        out.release()
    cv2.destroyAllWindows()
    
    
def process_image(image_path, output_path=None, json_output=None, display_scale=100):
    # Leer imagen
    frame = cv2.imread(image_path)
    if frame is None:
        print(f"Error: No se pudo cargar la imagen {image_path}")
        return
    
    # Convertir frame a PIL Image
    pil_image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
    frame_size = pil_image.size[::-1]  # (height, width)
    
    # ---------------------------------------------------------
    # Etapa 1: Detección de personas
    # ---------------------------------------------------------
    """inputs = person_processor(images=pil_image, return_tensors="pt").to(device)
    with torch.no_grad():
        outputs = person_model(**inputs)
        
    results = person_processor.post_process_object_detection(
        outputs, target_sizes=torch.tensor([frame_size]), threshold=0.5
    )
    person_boxes = results[0]["boxes"][results[0]["labels"] == 0].cpu().numpy()"""
    
    # Convertir PIL a numpy si es necesario
    numpy_image = np.array(pil_image)

    # Ejecutar detección con YOLO
    yolo_results = yolo_model.predict(
        source=numpy_image,
        conf=0.1,         # baja confianza mínima (default es 0.25, prueba 0.1 si es muy estricto)
        iou=0.7,           # mayor solapamiento permitido entre detecciones (default 0.7)
        classes=[0],       # solo detectar personas (clase 0 en COCO)
        device=0 if torch.cuda.is_available() else 'cpu',
        verbose=False
    )[0]


    # Extraer bounding boxes de personas (clase 0 en COCO = 'person')
    person_boxes = []
    for box, cls in zip(yolo_results.boxes.xyxy.cpu().numpy(), yolo_results.boxes.cls.cpu().numpy()):
        if int(cls) == 0:
            person_boxes.append(box)

    person_boxes = np.array(person_boxes)

    
    # Seleccionar persona más cercana
    person_boxes = select_closest_person(person_boxes, frame_size)
    if person_boxes.size == 0:
        print("No se detectaron personas en la imagen")
        return
    
    # Convertir formato de cajas (VOC a COCO)
    person_boxes[:, 2] -= person_boxes[:, 0]
    person_boxes[:, 3] -= person_boxes[:, 1]
    
    # ---------------------------------------------------------
    # Etapa 2: Estimación de postura
    # ---------------------------------------------------------
    inputs = pose_processor(
        images=pil_image, 
        boxes=[person_boxes], 
        return_tensors="pt"
    ).to(device)
    
    inputs["dataset_index"] = torch.tensor([0], device=inputs["pixel_values"].device) #para imagenes comentar para procesar video.
    
    with torch.no_grad():
        outputs = pose_model(**inputs)
        
    pose_results = pose_processor.post_process_pose_estimation(outputs, boxes=[person_boxes])
    
    print(pose_results)
    # ---------------------------------------------------------
    # Visualización & json-extraction
    # ---------------------------------------------------------
    numpy_image = np.array(pil_image)
    
    if json_output:
        coco_data = pose_results_to_coco(numpy_image, pose_results)
        with open(json_output, 'w') as f:
            json.dump(coco_data, f, indent=2, default=lambda x: x.tolist() if isinstance(x, np.ndarray) else x)
        print(f"Datos de pose guardados en {json_output}")
        
        
    for pose_result in pose_results[0]:
        scores = pose_result["scores"].cpu().numpy()
        keypoints = pose_result["keypoints"].cpu().numpy()
        
        # (Mantener todo el código de cálculo de ángulos y dibujo igual)
        # ... [código de cálculo de ángulos] ...
        # Piernas
        left_hip = get_keypoint_coords(keypoints, scores, 'left_hip')
        left_knee = get_keypoint_coords(keypoints, scores, 'left_knee')
        left_ankle = get_keypoint_coords(keypoints, scores, 'left_ankle')
        
        right_hip = get_keypoint_coords(keypoints, scores, 'right_hip')
        right_knee = get_keypoint_coords(keypoints, scores, 'right_knee')
        right_ankle = get_keypoint_coords(keypoints, scores, 'right_ankle')
        
        # Brazos
        left_shoulder = get_keypoint_coords(keypoints, scores, 'left_shoulder')
        left_elbow = get_keypoint_coords(keypoints, scores, 'left_elbow')
        left_wrist = get_keypoint_coords(keypoints, scores, 'left_wrist')
        
        right_shoulder = get_keypoint_coords(keypoints, scores, 'right_shoulder')
        right_elbow = get_keypoint_coords(keypoints, scores, 'right_elbow')
        right_wrist = get_keypoint_coords(keypoints, scores, 'right_wrist')
        
        # Ángulos de rodillas
        left_knee_angle = calculate_angle(left_hip, left_knee, left_ankle)
        right_knee_angle = calculate_angle(right_hip, right_knee, right_ankle)
        
        # Ángulos de codos
        left_elbow_angle = calculate_angle(left_shoulder, left_elbow, left_wrist)
        right_elbow_angle = calculate_angle(right_shoulder, right_elbow, right_wrist)
        
        #angulos suavizados
        smoothed_left_knee = left_knee_tracker.update(left_knee_angle)
        smoothed_right_knee = right_knee_tracker.update(right_knee_angle)
        smoothed_left_elbow = left_elbow_tracker.update(left_elbow_angle)
        smoothed_right_elbow = right_elbow_tracker.update(right_elbow_angle)
        
        # Dibujar texto con los ángulos
        font = cv2.FONT_HERSHEY_SIMPLEX
        font_scale = 0.6
        thickness = 2
        
        
        # Rodilla izquierda
        if left_knee and smoothed_left_knee:
            cv2.putText(numpy_image, f"{smoothed_left_knee:.1f}", 
                        (left_knee[0]+10, left_knee[1]), 
                        font, font_scale, ANGLE_COLORS['left_knee'], thickness)
        
        # Rodilla derecha
        if right_knee and smoothed_right_knee:
            cv2.putText(numpy_image, f"{smoothed_right_knee:.1f}", 
                        (right_knee[0]+10, right_knee[1]), 
                        font, font_scale, ANGLE_COLORS['right_knee'], thickness)
        
        # Codo izquierdo
        if left_elbow and smoothed_left_elbow:
            cv2.putText(numpy_image, f"{smoothed_left_elbow:.1f}", 
                        (left_elbow[0]+10, left_elbow[1]), 
                        font, font_scale, ANGLE_COLORS['left_elbow'], thickness)
        
        # Codo derecho
        if right_elbow and smoothed_right_elbow:
            cv2.putText(numpy_image, f"{smoothed_right_elbow:.1f}", 
                        (right_elbow[0]+10, right_elbow[1]), 
                        font, font_scale, ANGLE_COLORS['right_elbow'], thickness)
        # Dibujar puntos y conexiones
        for kpt, score in zip(keypoints, scores):
            if score > 0.3:
                x, y = int(kpt[0]), int(kpt[1])
                cv2.circle(numpy_image, (x, y), 5, (0,255,0), -1)
                
        for (start, end), color in zip(keypoint_edges, link_colors):
            if scores[start] > 0.3 and scores[end] > 0.3:
                x1, y1 = int(keypoints[start][0]), int(keypoints[start][1])
                x2, y2 = int(keypoints[end][0]), int(keypoints[end][1])
                cv2.line(numpy_image, (x1,y1), (x2,y2), color.tolist(), 2)
    
    # Procesamiento final de salida
    result_frame = cv2.cvtColor(numpy_image, cv2.COLOR_RGB2BGR)
    
    if display_scale != 100:
        h, w = result_frame.shape[:2]
        new_size = (int(w*display_scale/100), int(h*display_scale/100))
        result_frame = cv2.resize(result_frame, new_size)
    
    # Guardar o mostrar resultado
    if output_path:
        cv2.imwrite(output_path, result_frame)
        print(f"Imagen procesada guardada en: {output_path}")
    else:
        cv2.imshow('Image Analysis', result_frame)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        
        

if __name__ == "__main__":
    video_path = "press_banca4_2.mp4"
    output_path = "press_banca_4_2.mp4"
    # video_path = "frames\\groundTruth\\PressBanca\\frame_00000.jpg"
    # output_path = "testVideoPersonDetector\\testFineTunedVitpose_txt_00000.jpg"
    # json_output = "testVideoPersonDetector\\testFineTunedVitpose_frame_00000.json"
    process_video(video_path, output_path, 30, 100)
    
    #process_image(video_path, output_path, json_output, 100)