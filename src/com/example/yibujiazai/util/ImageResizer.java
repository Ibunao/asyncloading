package com.example.yibujiazai.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

/**
 * ͼƬ�ĸ�Ч���ء��ֱ��Ǵ���Դ�ļ��м��غͱ����ļ��м���
 */
public class ImageResizer {
    /**
     * ����Դ�ļ��м���ͼƬ��
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampleBitmapFromResource(Resources res,int resId,int reqWidth,int reqHeight){

        BitmapFactory.Options options = new BitmapFactory.Options();
        //��һ��options����Ϊtrue��BitmapFactoryֻ�����ͼƬ�Ŀ����Ϣ������������
        //�ļ���ͼƬ������Ҫ������Ϊfalse��Ȼ����м���
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);
        //���������
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resId,options);
    }

    /**
     * ���ڴ濨�м���ͼƬ
     * @param fd
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampleBitmapFromFile(FileDescriptor fd,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }
    /**
     *����ͼƬ�Ĳ�����
     * ԭ��������õ�ͼƬ����С��ԭͼ�Ŀ��ߡ���inSampleSize��2��ָ����С
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    private static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        if(reqHeight==0 || reqWidth ==0){
            return 1;
        }
        //Ĭ�ϵĲ�����
        int inSampleSize = 1;
        //��ȡԭͼ�Ŀ��
        int width = options.outWidth;
        int height = options.outHeight;

        if(width > reqWidth || height > reqHeight){
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            while((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
