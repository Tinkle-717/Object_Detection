
import cv2
import numpy as np
import os

png_path="G:\\summer\\JAVA\\images\\pic"
out_path="G:\\summer\\JAVA\\images\\pic\\cut"

def cut(pngname,outpath,filename):
 
    pic_path = pngname
    pic_target = outpath 
    if not os.path.exists(pic_target):
        os.makedirs(pic_target)
        
    cut_width = 256
    cut_length = 256

    picture = cv2.imread(pic_path)
    (width, length, depth) = picture.shape
    pic = np.zeros((cut_width, cut_length, depth))
    num=0
    num_width = int(width / cut_width)
    num_length = int(length / cut_length)
    for i in range(0, num_width):
        for j in range(0, num_length):
            num+=1
            pic = picture[i*cut_width : (i+1)*cut_width, j*cut_length : (j+1)*cut_length, :]      
            result_path =  '{} ({}).png'.format(filename, num)

            out=os.path.join(pic_target,result_path)
            cv2.imwrite(out, pic)


def handle(png_path,outpath):
    files=os.listdir(png_path)
    for i in files:
        pngpath=os.path.join(png_path,i)
        filename=i.split('.')[0]
        folder=os.path.join(outpath,filename)
        cut(pngpath,folder,filename)
        
        
handle(png_path,out_path)