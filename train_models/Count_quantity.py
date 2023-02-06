import os

path="G:\\summer\\JAVA\\123"

def count(filename):
    file=open(filename)
    lines = file.readlines()
    count=len(lines)
    return count
    
def get_total(path):
    total=0
    files=os.listdir(path)
    for i in files:
        filename= os.path.join(path,i)
        j=count(filename)
        total+=j
    print(total)
    
    
if __name__ =='__main__':    
    get_total(path)