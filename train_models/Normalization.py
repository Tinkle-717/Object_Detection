import os


def readtxt(file_name):
    data =[]
    file = open(file_name,'r')
    file_data  =file.readlines()
    for row in file_data:
        row = row.strip()
        if len(row)!=0: 
            tmp_list = row.split(',')
            tmp_list[-1] = tmp_list[-1].replace('\n','')
            data.append(tmp_list)  
           
    return data

def convert(xmin,ymin,xmax,ymax):
    img_width = 256
    img_height = 256

    xcenter = xmin + (xmax - xmin) / 2
    ycenter = ymin + (ymax - ymin) / 2
    w = xmax - xmin
    h = ymax - ymin
    
    xcenter = round(xcenter / img_width, 6)
    ycenter = round(ycenter / img_height, 6)
    w = round(w / img_width, 6)
    h = round(h / img_height, 6)
    
    yolo=[]
    yolo.append(xcenter)
    yolo.append(ycenter)
    yolo.append(w)
    yolo.append(h)
    return yolo
    
def yolo_txt(list,path):
    yolo=list
    class_name = 0
    msg= str(class_name)+" "+str(yolo[0])+" "+str(yolo[1])+" "+str(yolo[2])+" "+str(yolo[3])+"\n"
    # print(msg)
    file = open(path,'a')
    file.write(msg)



inpath = "G:\\summer\\JAVA\\images\\txt"

def handle(inpath):
    ori_folder ='txt'
    dir_folder = "yolo_txt"
    folders = os.listdir(inpath)
    for i in folders:
        root_path = os.path.join(inpath,i)
        txt_path = os.path.join(root_path,ori_folder)
        yolo_txt_dir = os.path.join(root_path,dir_folder)
        if not os.path.exists(yolo_txt_dir):
            os.makedirs(yolo_txt_dir)
        ori_dirs = os.listdir(txt_path)
        for j in ori_dirs:
            ori_txt_path = os.path.join(txt_path,j)
            yolo_txt_path = os.path.join(yolo_txt_dir,j)
            data=readtxt(ori_txt_path)
            for m in range(len(data)):
                xmin = int(data[m][0])
                ymin = int(data[m][1])
                xmax = int(data[m][2])
                ymax = int(data[m][3])
                yolo=convert(xmin,ymin,xmax,ymax)
                yolo_txt(yolo,yolo_txt_path)
        
    
handle(inpath)