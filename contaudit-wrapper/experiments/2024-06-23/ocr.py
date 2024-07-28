import cv2
import pytesseract
from PIL import Image

def extract_texts_from_video(video_path, output_path, start_sec=0, end_sec=None):
    cap = cv2.VideoCapture(video_path)
    fps = cap.get(cv2.CAP_PROP_FPS)
    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    duration = frame_count / fps

    if end_sec is None or end_sec > duration:
        end_sec = int(duration)

    texts_per_second = []

    # Loop through each second within the specified range
    for sec in range(start_sec, end_sec):
        cap.set(cv2.CAP_PROP_POS_MSEC, sec * 1000)
        ret, frame = cap.read()
        if not ret:
            break
        
        frame_image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        text = pytesseract.image_to_string(frame_image)
        texts_per_second.append((sec, text))
        print(f"Processed second {sec}/{end_sec}")

    cap.release()

    with open(output_path, 'w') as f:
        for sec, text in texts_per_second:
            f.write(f"Second {sec}:\n{text}\n\n")

# Path to the video file
filename = 'experiment_50\\docker'
video_path = 'D:\\\\Fontes\\ufrgs-poc\\contaudit-wrapper\\experiments\\2024-06-23\\' + filename + '.mp4'

# Path to the output text file
output_path = 'D:\\\\Fontes\\ufrgs-poc\\contaudit-wrapper\\experiments\\2024-06-23\\' + filename + '.txt'

# Extract texts from the video
extract_texts_from_video(video_path, output_path)

print(f"Texts have been extracted and saved to {output_path}")
