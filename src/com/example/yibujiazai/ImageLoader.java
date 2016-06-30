package com.example.yibujiazai;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

//�첽����
public class ImageLoader {
	
/*
 * ���ַ���û�а취�����λ�����⣬
 * */
/*	int count=0;
	//����һ
	private ImageView imageview;
	private String mUrl;
	//Thread�첽����
	public void showImageByThread(ImageView inImageView,final String url){
		imageview=inImageView;
		mUrl=url;
		new Thread(){
			public void run() {
				super.run();
				Bitmap bitmap=getBitmapFromUrl(url);
				//�����ݷ��ص����߳�  ������handle���߳�
				Message message=Message.obtain();
				message.obj=bitmap;
				handler.sendMessage(message);
			};
		}.start();
	}
	//����Handler��������
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO �Զ����ɵķ������
			super.handleMessage(msg);
			
			count++;
			Log.i("bunao",imageview.getTag()+"  :  "+mUrl+ count);
			if (imageview.getTag().equals(mUrl)) {
				imageview.setImageBitmap((Bitmap) msg.obj);
			}

		}
	};*/
	//��ȡBitmap
	
	public Bitmap getBitmapFromUrl(String url){
		Bitmap bitmap;
		InputStream is;
		try {
			//��ȡͼƬ�õ����ֽ���
			URL imUrl=new URL(url);
			HttpURLConnection connection=(HttpURLConnection) imUrl.openConnection();
			is=new BufferedInputStream(connection.getInputStream());
			bitmap=BitmapFactory.decodeStream(is);
			return bitmap;
		} catch (MalformedURLException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		return null;
	}
	
	//������ʹ��AsyncTask
	public void showImageByAsyncTask(ImageView inImageView,final String url){
		new NewsAsyncTask(inImageView,url).execute(url);
	}
	//ʹ���첽����AsyncTask,���͵�������������һ������ doInBackground�Ĳ������ͣ�ͨ���ڿ����첽����ʱ
	//�ķ���.execute(url)���д���  ���ڶ����������Ȳ��� onProgressUpdate���յģ���doInBackground
	//������ͨ��publishProgress(i)����������      ���������ؽ��������doInBackground����return����ֵ������
	//Ҳ��onPostExecute�������յ�ֵ
	public class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{
		private ImageView mImageView;
        private String mUrl;
        public NewsAsyncTask(ImageView imageView,String url){
            mImageView=imageView;
            mUrl=url;
        }
		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO �Զ����ɵķ������
			return getBitmapFromUrl(params[0]);
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO �Զ����ɵķ������
			super.onPostExecute(result);
//			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap(result);
//			}
		}
		
	}
}
