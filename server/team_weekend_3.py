from flask import Flask, jsonify, request
import base64
import skimage.draw
from imutils.face_utils import FaceAligner
from imutils.face_utils import rect_to_bb
import imutils
import dlib
import cv2
import numpy as np
from scipy.spatial.qhull import ConvexHull
import videooverlay as video
import ffmpeg
import os
import glob
import wave, warnings




detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor("/var/www/html/shape_predictor_68_face_landmarks.dat")

predictor_model = "/var/www/html/shape_predictor_68_face_landmarks.dat"
fa = FaceAligner(predictor, desiredFaceWidth=546)

app = Flask(__name__)


@app.route('/')
def index():
    return 'aaaaa'


@app.route('/voiceupload', methods=['POST', 'GET'])
def abc():
    if request.method == 'POST':
        file = request.files['file0']
        tmpname = file.filename
        tmpname1 = []
        tmpname2 = []
        tmpname3 = []
        tmpname1 = tmpname.split('.')
        tmpname2 = tmpname1[0]
        tmpname3 = tmpname2.split('_')
        user = tmpname3[0]
        num = tmpname3[1]
        voice = tmpname3[2]
        time = tmpname3[3]
        print(user, num, voice, time)

        print('음성 전송 시작')
        file.save('/var/www/html/receive/' + user + time + '.mp4')
        print('음성 파일 저장, 비디오 합성 시작')
        video.choice(user, num)
        print('이미지 비디오 합치기 시작')

        makeVideo(user, time)
        print('비디오 만들기 완성')
        print('비디오 오디오 합치기')
        os.system("ffmpeg -i /var/www/html/receive/" + user + time + ".mp4 -ac 2 -f wav /var/www/html/receive/" + user + time + ".wav")
        os.system("rm -r /var/www/html/receive/" + user + time + ".mp4")

        
        if voice=='1':
            # 음성변조 시작
            warnings.filterwarnings("ignore", category=DeprecationWarning)
            print('         음성변조 시작')
            
            wr = wave.open("/var/www/html/receive/" + user + time + ".wav", 'r')
            
            # Set the parameters for the output file.
            par = list(wr.getparams())
            par[3] = 0  # The number of samples will be set by writeframes.
            par = tuple(par)
            # ww = wave.open('out.wav', 'w')
            ww=wave.open("/var/www/html/receive/" + user + time + "out.wav", 'w')
            ww.setparams(par)

            fr = 1
            sz = wr.getframerate() // fr  # Read and process 1/fr second at a time.
            # A larger number for fr means less reverb.
            c = int(wr.getnframes() / sz)  # count of the whole file
            # shift = 1000//fr  # shifting 100 Hz
            shift = 300  # shifting 100 Hz
            for num in range(c):
                da = np.fromstring(wr.readframes(sz), dtype=np.int16)
                left, right = da[0::2], da[1::2]  # left and right channel
                lf, rf = np.fft.rfft(left), np.fft.rfft(right)
                lf, rf = np.roll(lf, shift), np.roll(rf, shift)
                lf[0:shift], rf[0:shift] = 0, 0
                nl, nr = np.fft.irfft(lf), np.fft.irfft(rf)
                ns = np.column_stack((nl, nr)).ravel().astype(np.int16)
                ww.writeframes(ns.tostring())

            wr.close()
            ww.close()
            # 음성변조 끝

            os.system("ffmpeg -i /var/www/html/receive/" + user + time + "out.wav -ac 2 -f mp4 /var/www/html/receive/" + user + time + ".mp4")
            os.system("ffmpeg -i /var/www/html/receive/" + user + time + ".mp4 -i " + "/var/www/html/tmp/" + user + time + ".mp4 -c copy /var/www/html/final/" + user + time + ".mp4")

            print('최종 완료')

            return 'success'

        
        
        elif voice == '0':
            os.system("ffmpeg -i /var/www/html/receive/" + user + time + ".wav -ac 2 -f mp4 /var/www/html/receive/" + user + time + ".mp4")
            os.system("ffmpeg -i /var/www/html/receive/" + user + time + ".mp4 -i " + "/var/www/html/tmp/" + user + time + ".mp4 -c copy /var/www/html/final/" + user + time + ".mp4")
    
            print('최종 완료')
    
            return 'success'


@app.route('/upload_file', methods=['POST', 'GET'])
def upload_file():
    if request.method == 'POST':
        user = request.form['id']
        file = request.files['file']
        file.save('/var/www/html/team/' + user + '.jpg')

        file_name = '/var/www/html/team/' + user + '.jpg'
        image = cv2.imread(file_name)
        image = imutils.resize(image, width=1010)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        rects = detector(gray, 1)

        for rect in rects:
            (x, y, w, h) = rect_to_bb(rect)
            sp = predictor(image, rect)
            landmarks = np.array([[p.x, p.y] for p in sp.parts()])

            vertices = ConvexHull(landmarks).vertices
            Y, X = skimage.draw.polygon(landmarks[vertices, 1], landmarks[vertices, 0])
            cropped_img = np.zeros(image.shape, dtype=np.uint8)
            cropped_img[Y, X] = image[Y, X]

            image = imutils.resize(cropped_img, width=1000, height=1000)
            faceAligned = fa.align(image, gray, rect)

            cv2.imwrite("/var/www/html/team1/{}.jpg".format(user), faceAligned)

            src = cv2.imread("/var/www/html/team1/{}.jpg".format(user), 1)

            tmp = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
            _, alpha = cv2.threshold(tmp, 0, 255, cv2.THRESH_BINARY)
            b, g, r = cv2.split(src)
            rgba = [b, g, r, alpha]
            dst = cv2.merge(rgba, 4)
            cv2.imwrite("/var/www/html/team1/{}.jpg".format(user), dst)
            cv2.waitKey(0)

    return "success"


@app.route('/detect', methods=["POST"])
def detect():
    imgBytes = request.data
    imgdata = base64.b64decode(imgBytes)
    with open("test.mp4", 'wb') as f:
        f.write(imgdata)
    return 'success'


def makeVideo(user, time):
    count = 1
    img_array = []
    for tmp_image in glob.glob('./tmp_image/' + user + '/*.jpg'):
        img = cv2.imread('./tmp_image/' + user + '/' + str(count) + '.jpg')
        height, width, layers = img.shape
        size = (width, height)
        img_array.append(img)
        count += 1

    out = cv2.VideoWriter('./tmp/' + user + time + '.mp4', cv2.VideoWriter_fourcc(*'mp4v'), 30, size)

    for i in range(len(img_array)):
        out.write(img_array[i])

    out.release()


if __name__ == "__main__":
    app.run('0.0.0.0', port=11000)
