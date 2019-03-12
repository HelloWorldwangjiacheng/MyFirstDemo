package com.example.myfirstdemo.Util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class RQCodeUtil {
    /**
     * 创建二维码位图
     * @param content 字符串的内容
     * @param width 位图的宽度(px)
     * @param height 位图的高度(px)
     */
    public static Bitmap createQRCodeBitmap(String content,int width,int height){
        return createQRCodeBitmap(content,width,height,"UTF-8","H","2",Color.BLACK,Color.WHITE);
    }

    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     *
     * @param content 字符串内容
     * @param width 位图宽度,要求>=0(单位:px)
     * @param height 位图高度,要求>=0(单位:px)
     * @param character_set 字符集/字符转码格式 ()。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param error_correction 容错级别 ()。传null时,zxing源码默认使用 "L"
     * @param margin 空白边距 (可修改,要求:整型且>=0), 传null时,zxing源R码默认使用"4"。
     * @param color_black 黑色色块的自定义颜色值
     * @param color_white 白色色块的自定义颜色值
     * @return
     */
    public static Bitmap createQRCodeBitmap(String content,int width,int height,
                                            @Nullable String character_set, @Nullable String error_correction,
                                            @Nullable String margin,int color_black,int color_white){
        /**
         * 合法性的判断
         */
        if (TextUtils.isEmpty(content)) return null;    //如果字符串内容为空,则返回null
        if (width<0 || height<0) return null;   //位图的宽和高都要大于0

        /**
         * 设置二维码的相关配置
         */
        try{
            //生成BitMatrix(位矩阵)对象
            Hashtable<EncodeHintType,String> hints = new Hashtable<>();
            //字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) hints.put(EncodeHintType.CHARACTER_SET,character_set);
            //容错级别设置
            if (!TextUtils.isEmpty(error_correction)) hints.put(EncodeHintType.ERROR_CORRECTION,error_correction);
            //空白边距设置
            if (!TextUtils.isEmpty(margin)) hints.put(EncodeHintType.MARGIN,margin);
            //创建BitMatrix(位矩阵)对象
            BitMatrix bitMatrix = new QRCodeWriter().encode(content,BarcodeFormat.QR_CODE,width,height,hints);

            /**
             * 创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素颜色值
             */
            int[] pixels = new int[width*height];
            for (int y=0;y<height;y++){
                for (int x=0;x<width;x++){
                    if (bitMatrix.get(x,y)){
                        pixels[y*width+x] = color_black;    //黑色色块像素设置
                    }else {
                        pixels[y+width+x] = color_white;    //白色色块像素设置
                    }
                }
            }

            /**
             * 创建Bitmap对象,根据像素数组设置Bitmap每一个像素点的颜色值,
             * 之后返回Bitmap对象
             */
            Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels,0,width,0,0,width,height);
            return bitmap;
        }catch (WriterException e){
            e.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
