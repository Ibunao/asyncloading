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
 * 将listview传进来直接使用listview来找相应的imageview
 */
public class ImageLoader2 {
	//创建缓存     和map集合的用法差不多，泛型中第一个是键值，第二个是缓存类型
	private LruCache<String,Bitmap> mCache;
	
	private Set<NewsAsyncTask> mTask;
	private ListView mlistView;
	
	public ImageLoader2(ListView listView) {
		// TODO 自动生成的构造函数存根
		//获取最大的缓存空间
		int maxMemory=(int) Runtime.getRuntime().maxMemory();
		//设置程序可用的缓存
		int cacheSize=maxMemory/4;
		//创建缓存
		mCache=new LruCache<String, Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO 自动生成的方法存根
//				return super.sizeOf(key, value);
				//告诉系统存入图片的大小
				return value.getByteCount();
			}
			
		};
		mlistView=listView;
		mTask=new HashSet<ImageLoader2.NewsAsyncTask>();

	}
	//添加到缓存的方法
	public void addBitmapToCache(String url,Bitmap bitmap){
		if(getBitmapFromCache(url)==null){
			mCache.put(url, bitmap);
		}
	}
	//获取缓存内容的方法
	public Bitmap getBitmapFromCache(String url){
		return mCache.get(url);
		
	}
	//滚动停止时加载图片     只对屏中的对象进行加载
	public void loadImages(int start,int end){
		for (int i = start; i < end; i++) {
			String url=NewsAdapter2.URLS[i];
			Log.i("xyz", i+"");
			//先从缓存中取出
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
				//通过findViewWithTag找到相应的控件，因为在适配器中为每个ImageView加了tag
				ImageView imageView=(ImageView) mlistView.findViewWithTag(url);
				imageView.setImageBitmap(bitmap);
			}
		}
	}
	//滚动时停止加载
	public void cancelAllTasks(){
		if (mTask!=null) {
			for (NewsAsyncTask newsAsyncTask : mTask) {
				//取消线程，为true时只是标记为取消状态，可以通过自身的线程进行状态判断进行取消
				newsAsyncTask.cancel(false);
			}
		}
	}
	
	//获取Bitmap
		public Bitmap getBitmapFromUrl(String url){
			Bitmap bitmap;
			InputStream is;
			try {
				//读取图片用的是字节流
				URL imUrl=new URL(url);
				HttpURLConnection connection=(HttpURLConnection) imUrl.openConnection();
				is=new BufferedInputStream(connection.getInputStream());
				bitmap=BitmapFactory.decodeStream(is);
				return bitmap;
			} catch (MalformedURLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			return null;
		}
		
		//使用AsyncTask
		public void showImageByAsyncTask(ImageView inImageView,final String url){
			Bitmap bitmap=getBitmapFromCache(url);
			if (bitmap!=null) {
				inImageView.setImageBitmap(bitmap);
			} else {
				inImageView.setImageResource(R.drawable.ic_launcher);
			}
			
		}
		//使用异步加载AsyncTask,泛型的三个参数，第一个传给 doInBackground的参数类型，通过在开启异步加载时
		//的方法.execute(url)进行传入  ，第二个参数进度参数 onProgressUpdate接收的，在doInBackground
		//方法中通过publishProgress(i)方法传出的      第三个返回结果参数，doInBackground方法return出的值的类型
		//也是onPostExecute方法接收的值
		public class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{
	        private String mUrl;
	        public NewsAsyncTask(String url){
	            mUrl=url;
	        }
			@Override
			protected Bitmap doInBackground(String... params) {
				// TODO 自动生成的方法存根
				String url=params[0];
				Bitmap bitmap=getBitmapFromUrl(url);
				//如果得到的图片存在就添加进去
				if(bitmap!=null){
					addBitmapToCache(url, bitmap);
				}
				return bitmap;
			}
			@Override
			protected void onPostExecute(Bitmap result) {
				// TODO 自动生成的方法存根
				super.onPostExecute(result);
				ImageView imageView=(ImageView) mlistView.findViewWithTag(mUrl);
				if (imageView!=null && result!=null) {
					imageView.setImageBitmap(result);
				}
				mTask.remove(this);
			}
			
		}
}
