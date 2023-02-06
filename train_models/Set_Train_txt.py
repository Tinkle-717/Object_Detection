import os
import random

def ImageSet():
    trainval_percent = 0.1
    train_percent = 0.9
    txtfilepath = 'data/labels'
    txtsavepath = 'data/ImageSets'
    total_txt = os.listdir(txtfilepath)
    num = len(total_txt)
    list = range(num)
    tv = int(num * trainval_percent)
    tr = int(tv * train_percent)
    trainval = random.sample(list, tv)
    train = random.sample(trainval, tr)
    ftrainval = open('data/ImageSets/trainval.txt', 'w')
    ftest = open('data/ImageSets/test.txt', 'w')
    ftrain = open('data/ImageSets/train.txt', 'w')
    fval = open('data/ImageSets/val.txt', 'w')
    for i in list:
        name = total_txt[i][:-4] + '\n'
        if i in trainval:
            ftrainval.write(name)
            if i in train:
                ftest.write(name)
            else:
                fval.write(name)
        else:
            ftrain.write(name)
    ftrainval.close()
    ftrain.close()
    fval.close()
    ftest.close()


Set_test ="G:\\summer\\yolo5\\yolov5\\data\\ImageSets\\test.txt"
Set_train = "G:\\summer\\yolo5\\yolov5\\data\\ImageSets\\train.txt"
Set_val =  "G:\\summer\\yolo5\\yolov5\\data\\ImageSets\\val.txt"

Data_test="G:\\summer\\yolo5\\yolov5\\data\\test.txt"
Data_train="G:\\summer\\yolo5\\yolov5\\data\\train.txt"
Data_val= "G:\\summer\\yolo5\\yolov5\\data\\val.txt"
path = "G:/summer/yolo5/yolov5/data/images/"

def get_Set_Name(file_name):
    data=[]
    file = open(file_name,"r")
    file_data = file.readlines()
    for row in file_data:
        row = row.strip()
        if len(row)!=0:
            tmp_list = row.split(',')
            tmp_list[-1] = tmp_list[-1].replace('\n','')
            data.append(tmp_list)
            
    return data
        
def write_data(file_name,list):
    result=[]
    for i in list:
        result.append(' '.join(i))
    file = open(file_name,'a')
    for j in result:      
        msg=path+str(j)+".png\n"
        file.write(msg)    
    
def handle():
    ImageSet()
    
    write_data(Data_test,get_Set_Name(Set_test))    
    write_data(Data_train,get_Set_Name(Set_train))    
    write_data(Data_val,get_Set_Name(Set_val))    

 
handle()