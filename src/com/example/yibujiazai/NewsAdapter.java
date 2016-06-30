package com.example.yibujiazai;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 错位问题就是并不是慕课老师讲的添加tag标签来解决的，他的产生是因为在一个对象中创建了多个线程(handler方式的)，而每个线程出来的结果匹配的是
 * 对象的实例变量，而这些事例变量时每次调用开新线程的时候传进的值，后面的会将前面的覆盖。这就导致了线程输出结果绑定的是刚赋值的实例变量，而不是自己
 * 想要的
 * 而AsyncTask异步是每次都创建一个对象，传入的参数也存在这个对象中，所以每次子线程的结果出来找到的就是自己所在对象存储的实例变量，就是自己想要的
 * 没那么麻烦，不需要添加tag判断
 * */
public class NewsAdapter extends BaseAdapter {
	private Context mContext;
	private List<NewBean> mListBean;
	private LayoutInflater inflater;
	private ImageLoader mImageLoader;
	private ImageLoader1 mImageLoader1;
	public NewsAdapter(Context context,List<NewBean> listBean) {
		// TODO 自动生成的构造函数存根
		mContext=context;
		mListBean=listBean;
		inflater=LayoutInflater.from(context);
		mImageLoader=new ImageLoader();
		mImageLoader1=new ImageLoader1();
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
		
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		String url=mListBean.get(position).newsIcon;
//		viewHolder.ivIcon.setTag(url);
		//每次创建一个对象，创建一个新的线程，线程池增加一个线程
		//showImageByThread方法之所以用一个固定的对象引用会出现错乱是因为输出条件并没有跟随放进放进线程中
		//笃定的对象，里面生成多个线程线程，但是因为输出条件的变量是在外面的，线程完成任务输出是恒成立的，而像
		//showImageByAsyncTask将参数跟随进去的，会判断输出
		
		//并不能实现
//		mImageLoader.showImageByThread(viewHolder.ivIcon,url);
		//实现了
//		mImageLoader.showImageByAsyncTask(viewHolder.ivIcon,url);
		//因为使用了缓存，就要用固定的对象了
		mImageLoader1.showImageByAsyncTask(viewHolder.ivIcon, url);
		
		viewHolder.tvContent.setText(mListBean.get(position).newsContent);
		viewHolder.tvTitle.setText(mListBean.get(position).newsTitle);
		return convertView;
	}
	class ViewHolder{
		public TextView tvTitle,tvContent;
		public ImageView ivIcon;
	}

}
