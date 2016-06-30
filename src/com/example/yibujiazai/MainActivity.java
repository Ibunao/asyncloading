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
//�ǵüӷ���Ȩ��
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
	
	//AsyncTask�첽���أ���doInBackground���̴߳�����������ص�onPostExecute(���߳�)
	class NewsAsyncTask extends AsyncTask<String, Void, List<NewBean>>{

		@Override
		protected List<NewBean> doInBackground(String... params) {
			// TODO �Զ����ɵķ������
			//��ȡ������Դ
			return getJsonData(params[0]);
		}
		@Override
		protected void onPostExecute(List<NewBean> result) {
			// TODO �Զ����ɵķ������
			super.onPostExecute(result);
//			NewsAdapter adapter=new NewsAdapter(MainActivity.this,result);
			NewsAdapter2 adapter=new NewsAdapter2(MainActivity.this, result, listView);
			listView.setAdapter(adapter);
		}
		
	}
	//��ȡ������Դ
	private List<NewBean> getJsonData(String url){
		List<NewBean> newBeanList=new ArrayList<NewBean>();
		NewBean newBean;
		InputStream input = null;
		try {
			input=new URL(url).openStream();
			//��new URL(url).openConnection().getInputStream()��ͬ
			String jsonString=readStream(input);
			//����json����
			JSONObject jsonObject;
			try {
				//������json����
				jsonObject=new JSONObject(jsonString);
				//����json����
				JSONArray jsonArray=jsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					//json������ÿһ��ֵ����һ��json����
					jsonObject=jsonArray.getJSONObject(i);
					newBean=new NewBean();
					newBean.newsContent=jsonObject.getString("description");
					newBean.newsTitle=jsonObject.getString("name");
					newBean.newsIcon=jsonObject.getString("picSmall");
					newBeanList.add(newBean);
					
				}
			} catch (JSONException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}finally {
			try {
				input.close();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
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
			//����ַ���
			isR = new InputStreamReader(is,"utf-8");
			BufferedReader br=new BufferedReader(isR);
			//���ﲻ��-1����Ϊ��һ��һ�еĶ���
			while ((line=br.readLine())!=null) {
				result+=line;
			}
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}finally {
			try {
				isR.close();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return result;
	}

}
