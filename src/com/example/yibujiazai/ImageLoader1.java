package com.example.yibujiazai;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.yibujiazai.ImageLoader.NewsAsyncTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
//�첽����   ������
public class ImageLoader1 {
	//��������     ��map���ϵ��÷���࣬�����е�һ���Ǽ�ֵ���ڶ����ǻ�������
	private LruCache<String,Bitmap> mCache;
	public ImageLoader1() {
		// TODO �Զ����ɵĹ��캯�����
		//��ȡ���Ļ���ռ�
		int maxMemory=(int) Runtime.getRuntime().maxMemory();
		//���ó�����õĻ���
		int cacheSize=maxMemory/4;
		//��������
		mCache=new LruCache<String, Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO �Զ����ɵķ������
//				return super.sizeOf(key, value);
				//����ϵͳ����ͼƬ�Ĵ�С
				return value.getByteCount();
			}
			
		};
	}
	//��ӵ�����ķ���
	public void addBitmapToCache(String url,Bitmap bitmap){
		if(getBitmapFromCache(url)==null){
			mCache.put(url, bitmap);
		}
	}
	//��ȡ�������ݵķ���
	public Bitmap getBitmapFromCache(String url){
		return mCache.get(url);
		
	}
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
		
		//ʹ��AsyncTask
		public void showImageByAsyncTask(ImageView inImageView,final String url){
			Bitmap bitmap=getBitmapFromCache(url);
			if (bitmap!=null) {
				inImageView.setImageBitmap(bitmap);
			} else {
				new NewsAsyncTask(inImageView,url).execute(url);
			}
			
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
				String url=params[0];
				Bitmap bitmap=getBitmapFromUrl(params[0]);
				//����õ���ͼƬ���ھ���ӽ�ȥ
				if(bitmap!=null){
					addBitmapToCache(url, bitmap);
				}
				return bitmap;
			}
			@Override
			protected void onPostExecute(Bitmap result) {
				// TODO �Զ����ɵķ������
				super.onPostExecute(result);
//				if (mImageView.getTag().equals(mUrl)) {
					mImageView.setImageBitmap(result);
//				}
			}
			
		}
		
		
}
