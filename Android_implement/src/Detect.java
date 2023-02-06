package com.example.yolo_picture_01;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.widget.ImageView;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Result{
    int classIndex;
    Float score;
    Rect rect;

    public Result(int index,Float output,Rect rect){
        this.classIndex = index;
        this.score = output;
        this.rect = rect;
    }
}
//1.保持宽高比resize图片
//2.letterbox边缘填充

//3.切割bitmap分批送入网络

public class Detect  {

//1
public static Bitmap[][] bitmapCrop(Bitmap mBitmap){                                            //切片推理
    // 不缩放直接填充为正方形
    int step = 512;                                                                             //滑窗步长
    float imgWidth = (float) mBitmap.getWidth();                                                // eg 4120
    float imgHeight = (float) mBitmap.getHeight();                                              //eg 3100

    int step_amount_x = (int) Math.ceil(imgWidth / step);                                       //X轴滑窗个数
    int step_amount_y = (int) Math.ceil(imgHeight / step);                                      //y轴滑窗个数

    int Square_size = (imgWidth > imgHeight ? step * step_amount_x :step * step_amount_y);

    Bitmap uncut_Bitmap = Bitmap.createBitmap(Square_size,Square_size,Bitmap.Config.ARGB_4444); //创建带灰色边界正方形
    Canvas canvas = new Canvas(uncut_Bitmap);
    canvas.drawColor(Color.GRAY);
    canvas.drawBitmap(mBitmap,0,0,null);                                         //将原图画到正方形上

//        Bitmap.createBitmap(Bitmap source, int x, int y, int width, int height)

    Bitmap[][] bitmaps = new Bitmap[step_amount_x][step_amount_y];                              //创建二维数组存储切片

    for(int x=0;x < step_amount_x-1 ; x++){
        for(int y = 0; y< step_amount_y-1 ; y++){
            Bitmap cut =Bitmap.createBitmap(uncut_Bitmap,x*step,y*step,640,640);
            bitmaps[x][y]=cut;
        }
    }
    return  bitmaps;
}
    //2
    public static ArrayList<Result>  handleResult(Bitmap mBitmap, Module mModule) {
        Bitmap[][] bitmaps = bitmapCrop(mBitmap);
        ArrayList<Result> All_Results = new ArrayList<>();

        for (int i = 0; i < bitmaps.length - 1; i++) {
            for (int j = 0; j < bitmaps[0].length - 1; j++) {
                float[] chips_output = outputs(bitmaps[i][j],mModule);
                ArrayList<Result> chip_results = SingleChips(chips_output,bitmaps[i][j],i,j);       //存放每个小切片结果
                All_Results.addAll(chip_results);
            }
        }
        return nonMaxSuppression(All_Results,mNmsLimit,iou_thres);
    }

    public static float[] outputs(Bitmap mBitmap,Module mModule){
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(mBitmap,NO_MEAN_RGB,NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        return outputs;
    }
    static ArrayList<Result> SingleChips(float[] outputs,Bitmap bitmap,int row,int column){                             //合并切片
        int OutputNum =25200;                                                                       //输入640*640分辨率图片锚框数量
        float imgScaleX =bitmap.getWidth()/640;
        float imgScaleY = bitmap.getHeight()/640;

        ArrayList<Result> result = new ArrayList<>();

        for(int i= 0;i<OutputNum;i++) {
            if (outputs[i * mOutputColumn + 4] > conf_thres) {
                float x = outputs[i * mOutputColumn];
                float y = outputs[i * mOutputColumn + 1];
                float w = outputs[i * mOutputColumn + 2];
                float h = outputs[i * mOutputColumn + 3];

                float left = imgScaleX * (x - w / 2);
                float top = imgScaleY * (y - h / 2);
                float right = imgScaleX * (x + w / 2);
                float bottom = imgScaleY * (y + h / 2);
                float max = outputs[i * mOutputColumn + 5];
                int cls = 0;
                for (int j = 0; j < mOutputColumn - 5; j++) {
                    if (outputs[i * mOutputColumn + 5 + j] > max) {
                        max = outputs[i * mOutputColumn + 5 + j];
                        cls = j;
                    }
                }
                Rect rect = new Rect((int)(column*640+left),(int)(row*640+top),(int)(column*640+right),(int)(row*640+bottom));
                Result chip = new Result(cls,outputs[i*mOutputColumn+4],rect);
                result.add(chip);
            }
        }
        return result;
    }


    static ArrayList<Result> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold) {
        Collections.sort(boxes,
                new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return o1.score.compareTo(o2.score);
                    }
                });

        ArrayList<Result> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        boolean done = false;
        for (int i=0; i<boxes.size() && !done; i++) {
            if (active[i]) {
                Result boxA = boxes.get(i);
                selected.add(boxA);
                if (selected.size() >= limit) break;
                for (int j=i+1; j<boxes.size(); j++) {
                    if (active[j]) {
                        Result boxB = boxes.get(j);
                        if (IOU(boxA.rect, boxB.rect) > threshold) {
                            active[j] = false;
                            numActive -= 1;
                            if (numActive <= 0) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }
    static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;
        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) * Math.max(intersectionMaxX - intersectionMinX, 0);
        float part1 = intersectionArea / (areaA + areaB - intersectionArea);

        int Wa = (a.right-a.left);
        int Ha = (a.bottom-a.top);
        int Wb = (b.right-b.left);
        int Hb = (b.bottom-b.top);

        float xa=Wa/2+a.left;
        float ya=Ha/2+a.top;
        float xb=Wb/2+b.left;
        float yb=Hb/2+b.top;
        float Quad_l1 = (xa-xb)*(xa-xb)+(ya-yb)*(ya-yb);

        float x_left=Math.min(a.left,b.left);
        float x_right=Math.max(a.right,b.right);
        float y_top=Math.min(a.top,b.top);
        float y_bottom=Math.max(a.bottom,b.bottom);
        float Quad_l2 = (x_right-x_left)*(x_right-x_left)+(y_bottom-y_top)*(y_bottom-y_top);
        float part2 = Quad_l1/Quad_l2;

//        double v1 = Math.pow(Math.atan(Wa/Ha) - Math.atan(Wb/Hb),2) ;
//        float v = (float) (4/(Math.pow(Math.PI,2))* v1);
//        float alpha = v / ((1-part1)+v);
//        float cious=part1-part2-alpha*v;
        return part1-part2;
    }



    static float[] NO_MEAN_RGB = new float[] {0.0f, 0.0f, 0.0f};
    static float[] NO_STD_RGB = new float[] {1.0f, 1.0f, 1.0f};

//    static int inputWidth = 2016;                    //输入网络分辨率
//    static int inputHeight = 2016;


    static int mInputWidth = 2016;
    static int mInputHeight = 2016;
    private static int mOutputRow = 250047;
//    static int mInputWidth = 1600;                         //模型要求大小
//    static int mInputHeight = 1600;
//    private static int mOutputRow = 157500;

    private static int mOutputColumn = 6;
    private static float iou_thres = 0.30f;                            //重合框参数
    private static float conf_thres = 0.50f;                      //置信度参数
    private static int mNmsLimit = 4000;

    static String[] mClasses;






//1
    public static Bitmap resizedBitmap(Bitmap mBitmap){
          int s = 32;
          float imgWidth = (float) mBitmap.getWidth();                                              //图片宽度
          float imgHeight = (float)mBitmap.getHeight();
          float WidthScale = imgWidth / mInputWidth;                                                //图片宽度与模型宽度比例
          float HeightScale = imgHeight / mInputHeight;
          float ratio=0;
          float new_Width,new_Height;
          //两种情况等比缩放
          if(imgWidth>imgHeight){           //1)  eg 2200*1700 
              ratio = WidthScale;
              new_Width = mInputWidth;
              new_Height = (float) (Math.ceil(imgHeight / ratio / s) * s);
          }
          else {                            // eg 1700*2200
              ratio = HeightScale;
              new_Height = mInputHeight;
              new_Width = (float) (Math.ceil(imgWidth / ratio / s) * s);
          }
          Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap,(int)new_Width,(int)new_Height,true);

          return resizedBitmap;
    }
// 2
    public static Bitmap letterbox(Bitmap mBitmap){                                                 //缩放后由矩形填充至正方形

        Bitmap Rec_Bitmap = resizedBitmap(mBitmap);

        Bitmap grayMap = Bitmap.createBitmap(mInputWidth,mInputHeight , Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(grayMap);
        canvas.drawColor(Color.GRAY);
        canvas.drawBitmap(Rec_Bitmap,0,0,null);

        return grayMap;
    }

    public static  ArrayList<Result> res (Bitmap mBitmap, Module mModule ,float[] arr){

        Bitmap resizedBitmap = letterbox(mBitmap);                                               //等比缩放


        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap,NO_MEAN_RGB,NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        ArrayList<Result> results = outputsToNMSPredictions(outputs, arr[0],arr[1],arr[2],arr[3],arr[4],arr[5]);

        return  results;
    }
//    输出层处理
    public static float[] array(Bitmap mBitmap, ImageView image){
        float[] arr = new float[6];
        float mImgScaleX = (float)mBitmap.getWidth() / mInputWidth;                                //图片宽度与模型宽度比例
        float mImgScaleY = (float)mBitmap.getHeight() / mInputHeight;
        float mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)image.getWidth() / mBitmap.getWidth() : (float)image.getHeight() / mBitmap.getHeight());
        float mIvScaleY = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)image.getHeight() / mBitmap.getHeight() : (float)image.getWidth() / mBitmap.getWidth());
        float mStartX = (image.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
        float mStartY = (image.getHeight() - mIvScaleY * mBitmap.getHeight())/2;

        arr[0]=mImgScaleX;
        arr[1]=mImgScaleY;
        arr[2]=mIvScaleX;
        arr[3]=mIvScaleY;
        arr[4]=mStartX;
        arr[5]=mStartY;

        return arr;
    }

    static ArrayList<Result> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY, float ivScaleX, float ivScaleY, float startX, float startY) {
        ArrayList<Result> results = new ArrayList<>();
        for (int i = 0; i< mOutputRow; i++) {
            if (outputs[i* mOutputColumn +4] > conf_thres) {
                float x = outputs[i* mOutputColumn];
                float y = outputs[i* mOutputColumn +1];
                float w = outputs[i* mOutputColumn +2];
                float h = outputs[i* mOutputColumn +3];

                float left = imgScaleX * (x - w/2);
                float top = imgScaleY * (y - h/2);
                float right = imgScaleX * (x + w/2);
                float bottom = imgScaleY * (y + h/2);

                float max = outputs[i* mOutputColumn +5];
                int cls = 0;
                for (int j = 0; j < mOutputColumn -5; j++) {
                    if (outputs[i* mOutputColumn +5+j] > max) {
                        max = outputs[i* mOutputColumn +5+j];
                        cls = j;
                    }
                }
                Rect rect = new Rect((int)(startX+ivScaleX*left), (int)(startY+top*ivScaleY), (int)(startX+ivScaleX*right), (int)(startY+ivScaleY*bottom));   //扩展到原图
                Result result = new Result(cls, outputs[i*mOutputColumn+4], rect);
                results.add(result);
            }
        }
        return nonMaxSuppression(results, mNmsLimit, iou_thres);
    }





}
