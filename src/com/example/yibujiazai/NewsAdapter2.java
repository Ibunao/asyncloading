package com.example.yibujiazai;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
//ʵ�ֹ���ʱ�������ڹ���ʱ������UI,����������ʵ�ּ����ӿ���Ҫʵ��AbsListView.OnScrollListener�ӿ�
public class NewsAdapter2 extends BaseAdapter implements  AbsListView.OnScrollListener{
	private List<NewBean> mListBean;
	private LayoutInflater inflater;
	private ImageLoader2 mImageLoader2;
	public static String[] URLS;
	private int mStart,mEnd;
	private boolean first;
	public NewsAdapter2(Context context,List<NewBean> listBean,ListView listView) {
		mListBean=listBean;
		inflater=LayoutInflater.from(context);
		mImageLoader2=new ImageLoader2(listView);
		//�ǵó�ʼ������    ��Ȼ�������
		URLS=new String[listBean.size()];
		for (int i = 0; i <listBean.size(); i++) {
			URLS[i]=listBean.get(i).newsIcon;
		}
		//��Ҫ������ע���¼�
		listView.setOnScrollListener(this);
		first=true;
	}
	@Override
	public int getCount() {
		// TODO �Զ����ɵķ������
		return mListBean.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO �Զ����ɵķ������
		return mListBean.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO �Զ����ɵķ������
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO �Զ����ɵķ������
		ViewHolder viewHolder;
		if (convertView==null) {
			viewHolder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_source, null);
			viewHolder.tvContent=(TextView) convertView.findViewById(R.id.content);
			viewHolder.ivIcon=(ImageView) convertView.findViewById(R.id.imageview);
			viewHolder.tvTitle=(TextView) convertView.findViewById(R.id.title);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		Log.i("position", position+"");
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		String url=mListBean.get(position).newsIcon;
		viewHolder.ivIcon.setTag(url);
		
		mImageLoader2.showImageByAsyncTask(viewHolder.ivIcon, url);
		
		viewHolder.tvContent.setText(mListBean.get(position).newsContent);
		viewHolder.tvTitle.setText(mListBean.get(position).newsTitle);
		return convertView;
	}
	class ViewHolder{
		public TextView tvTitle,tvContent;
		public ImageView ivIcon;
	}
	//ʵ�֣�����ʱ������UI
	//����״̬�����ı�ʱ�����ķ���     ����϶��������ǹ���
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO �Զ����ɵķ������
		if (scrollState==SCROLL_STATE_IDLE) {
			//ֹͣ״̬���м���
			mImageLoader2.loadImages(mStart,mEnd);
		} else {
			//��ֹ����
			mImageLoader2.cancelAllTasks();
		}
		
	}
	//����״̬��һֱ�����ķ���
    //��Ϊ��ʼʱ�������onScrollStateChanged���������ǻ����onScroll����
    // ���Ե�һ�μ��ص�ʱ���ֶ����ص�һ��������  ��onScroll������
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO �Զ����ɵķ������
		mStart=firstVisibleItem;
		mEnd=firstVisibleItem+visibleItemCount;
		if (first==true && visibleItemCount>0) {
			mImageLoader2.loadImages(mStart, mEnd);
			first=false;
		}
	}

}
