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

//异步加载
public class ImageLoader {
	
/*
 * 这种方法没有办法解决错位的问题，
 * */
/*	int count=0;
	//方法一
	private ImageView imageview;
	private String mUrl;
	//Thread异步加载
	public void showImageByThread(ImageView inImageView,final String url){
		imageview=inImageView;
		mUrl=url;
		new Thread(){
			public void run() {
				super.run();
				Bitmap bitmap=getBitmapFromUrl(url);
				//将数据返回到主线程  创建的handle主线程
				Message message=Message.obtain();
				message.obj=bitmap;
				handler.sendMessage(message);
			};
		}.start();
	}
	//创建Handler用来接收
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			super.handleMessage(msg);
			
			count++;
			Log.i("bunao",imageview.getTag()+"  :  "+mUrl+ count);
			if (imageview.getTag().equals(mUrl)) {
				imageview.setImageBitmap((Bitmap) msg.obj);
			}

		}
	};*/
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
	
	//方法二使用AsyncTask
	public void showImageByAsyncTask(ImageView inImageView,final String url){
		new NewsAsyncTask(inImageView,url).execute(url);
	}
	//使用异步加载AsyncTask,泛型的三个参数，第一个传给 doInBackground的参数类型，通过在开启异步加载时
	//的方法.execute(url)进行传入  ，第二个参数进度参数 onProgressUpdate接收的，在doInBackground
	//方法中通过publishProgress(i)方法传出的      第三个返回结果参数，doInBackground方法return出的值的类型
	//也是onPostExecute方法接收的值
	public class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{
		private ImageView mImageView;
        private String mUrl;
        public NewsAsyncTask(ImageView imageView,String url){
            mImageView=imageView;
            mUrl=url;
        }
		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO 自动生成的方法存根
			return getBitmapFromUrl(params[0]);
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO 自动生成的方法存根
			super.onPostExecute(result);
//			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap(result);
//			}
		}
		
	}
}
