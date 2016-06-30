package com.example.yibujiazai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
//记得加访问权限
public class MainActivity extends Activity {
	private ListView listView;
	private String url="http://www.imooc.com/api/teacher?type=4&num=30";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView=(ListView) findViewById(R.id.listview);
		new NewsAsyncTask().execute(url);
	}
	
	//AsyncTask异步加载，在doInBackground外线程处理，将结果返回到onPostExecute(主线程)
	class NewsAsyncTask extends AsyncTask<String, Void, List<NewBean>>{

		@Override
		protected List<NewBean> doInBackground(String... params) {
			// TODO 自动生成的方法存根
			//获取网络资源
			return getJsonData(params[0]);
		}
		@Override
		protected void onPostExecute(List<NewBean> result) {
			// TODO 自动生成的方法存根
			super.onPostExecute(result);
//			NewsAdapter adapter=new NewsAdapter(MainActivity.this,result);
			NewsAdapter2 adapter=new NewsAdapter2(MainActivity.this, result, listView);
			listView.setAdapter(adapter);
		}
		
	}
	//获取网络资源
	private List<NewBean> getJsonData(String url){
		List<NewBean> newBeanList=new ArrayList<NewBean>();
		NewBean newBean;
		InputStream input = null;
		try {
			input=new URL(url).openStream();
			//和new URL(url).openConnection().getInputStream()等同
			String jsonString=readStream(input);
			//声明json对象
			JSONObject jsonObject;
			try {
				//创建成json对象
				jsonObject=new JSONObject(jsonString);
				//创建json数组
				JSONArray jsonArray=jsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					//json数组中每一个值都是一个json对象
					jsonObject=jsonArray.getJSONObject(i);
					newBean=new NewBean();
					newBean.newsContent=jsonObject.getString("description");
					newBean.newsTitle=jsonObject.getString("name");
					newBean.newsIcon=jsonObject.getString("picSmall");
					newBeanList.add(newBean);
					
				}
			} catch (JSONException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}finally {
			try {
				input.close();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return newBeanList;
	}
	private String readStream(InputStream is){
		
		InputStreamReader isR = null;
		String result="";
		String line="";

		try {
			//变成字符流
			isR = new InputStreamReader(is,"utf-8");
			BufferedReader br=new BufferedReader(isR);
			//这里不是-1是因为是一行一行的读的
			while ((line=br.readLine())!=null) {
				result+=line;
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}finally {
			try {
				isR.close();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return result;
	}

}
