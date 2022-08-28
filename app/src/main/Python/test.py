# from __future__ import print_function
from imutils.object_detection import non_max_suppression
import numpy as np
import imutils
import cv2

def test1():
    return "testPyYYYYYYYYYYYYYYYY"

def distance_to_camera(knownWidth, focalLength, perWidth):
    return (knownWidth * focalLength) / perWidth

# def toimg(path):
#     img1 = cv2.imread(path)
#     return img1

def test2(arrayt):
    a = np.array(arrayt)
    frame = a.reshape(500,500,3)
    return depEva(np.uint8(frame))


def depEva(frame):
    # frame=cv2.imread(path)

    hog = cv2.HOGDescriptor()
    hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())
    KNOW_PERSON_HEIGHT = 170;
    focalLength = 100;
    pix_person_height = 5;
    # frame = imutils.resize(frame, width=min(400, frame.shape[1]))
    # detect people in the image
    (rects, weights) = hog.detectMultiScale(frame, winStride=(4, 4),
                                            padding=(8, 8), scale=1.05)

    rects = np.array([[x, y, x + w, y + h] for (x, y, w, h) in rects])
    # 　非极大抑制 消除多余的框 找到最佳人体
    pick = non_max_suppression(rects, probs=None, overlapThresh=0.65)
    for (xA, yA, xB, yB) in pick:
        cv2.rectangle(frame, (xA, yA), (xB, yB), (0, 255, 0), 2)
        ya_max = yA
        yb_max = yB

        pix_person_height = yb_max - ya_max
    inches = distance_to_camera(KNOW_PERSON_HEIGHT, focalLength, pix_person_height)

    return (inches * 30.48 / 12)