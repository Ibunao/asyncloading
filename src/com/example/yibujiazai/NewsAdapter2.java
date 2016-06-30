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
//实现滚动时监听，在滚动时不加载UI,在适配器中实现监听接口需要实现AbsListView.OnScrollListener接口
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
		//记得初始化数组    不然程序崩溃
		URLS=new String[listBean.size()];
		for (int i = 0; i <listBean.size(); i++) {
			URLS[i]=listBean.get(i).newsIcon;
		}
		//不要忘记了注册事件
		listView.setOnScrollListener(this);
		first=true;
	}
	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return mListBean.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO 自动生成的方法存根
		return mListBean.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO 自动生成的方法存根
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO 自动生成的方法存根
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
	//实现，滚动时不更新UI
	//滚动状态发生改变时触发的方法     鼠标拖动，而不是滚动
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO 自动生成的方法存根
		if (scrollState==SCROLL_STATE_IDLE) {
			//停止状态进行加载
			mImageLoader2.loadImages(mStart,mEnd);
		} else {
			//禁止加载
			mImageLoader2.cancelAllTasks();
		}
		
	}
	//滚动状态下一直触发的方法
    //因为初始时不会调用onScrollStateChanged方法，但是会调用onScroll方法
    // 所以第一次加载的时候手动加载第一屏的数据  放onScroll方法内
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO 自动生成的方法存根
		mStart=firstVisibleItem;
		mEnd=firstVisibleItem+visibleItemCount;
		if (first==true && visibleItemCount>0) {
			mImageLoader2.loadImages(mStart, mEnd);
			first=false;
		}
	}

}
