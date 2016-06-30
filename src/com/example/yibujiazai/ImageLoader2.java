package com.example.yibujiazai;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;
/*
 * ��listview������ֱ��ʹ��listview������Ӧ��imageview
 */
public class ImageLoader2 {
	//��������     ��map���ϵ��÷���࣬�����е�һ���Ǽ�ֵ���ڶ����ǻ�������
	private LruCache<String,Bitmap> mCache;
	
	private Set<NewsAsyncTask> mTask;
	private ListView mlistView;
	
	public ImageLoader2(ListView listView) {
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
		mlistView=listView;
		mTask=new HashSet<ImageLoader2.NewsAsyncTask>();

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
	//����ֹͣʱ����ͼƬ     ֻ�����еĶ�����м���
	public void loadImages(int start,int end){
		for (int i = start; i < end; i++) {
			String url=NewsAdapter2.URLS[i];
			Log.i("xyz", i+"");
			//�ȴӻ�����ȡ��
			Bitmap bitmap=getBitmapFromCache(url);
			Log.i("xyz", "wancheng");
			Log.i("xyz", bitmap+"");
			if (bitmap==null){
				Log.i("xyz", "jinlaile0");
				NewsAsyncTask task=new NewsAsyncTask(url);
				task.execute(url);
				mTask.add(task);
				Log.i("xyz", "jinlaile1");
			} else {
				//ͨ��findViewWithTag�ҵ���Ӧ�Ŀؼ�����Ϊ����������Ϊÿ��ImageView����tag
				ImageView imageView=(ImageView) mlistView.findViewWithTag(url);
				imageView.setImageBitmap(bitmap);
			}
		}
	}
	//����ʱֹͣ����
	public void cancelAllTasks(){
		if (mTask!=null) {
			for (NewsAsyncTask newsAsyncTask : mTask) {
				//ȡ���̣߳�Ϊtrueʱֻ�Ǳ��Ϊȡ��״̬������ͨ��������߳̽���״̬�жϽ���ȡ��
				newsAsyncTask.cancel(false);
			}
		}
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
				inImageView.setImageResource(R.drawable.ic_launcher);
			}
			
		}
		//ʹ���첽����AsyncTask,���͵�������������һ������ doInBackground�Ĳ������ͣ�ͨ���ڿ����첽����ʱ
		//�ķ���.execute(url)���д���  ���ڶ����������Ȳ��� onProgressUpdate���յģ���doInBackground
		//������ͨ��publishProgress(i)����������      ���������ؽ��������doInBackground����return����ֵ������
		//Ҳ��onPostExecute�������յ�ֵ
		public class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{
	        private String mUrl;
	        public NewsAsyncTask(String url){
	            mUrl=url;
	        }
			@Override
			protected Bitmap doInBackground(String... params) {
				// TODO �Զ����ɵķ������
				String url=params[0];
				Bitmap bitmap=getBitmapFromUrl(url);
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
				ImageView imageView=(ImageView) mlistView.findViewWithTag(mUrl);
				if (imageView!=null && result!=null) {
					imageView.setImageBitmap(result);
				}
				mTask.remove(this);
			}
			
		}
}
