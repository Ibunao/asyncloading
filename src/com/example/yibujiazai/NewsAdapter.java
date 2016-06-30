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
 * ��λ������ǲ�����Ľ����ʦ�������tag��ǩ������ģ����Ĳ�������Ϊ��һ�������д����˶���߳�(handler��ʽ��)����ÿ���̳߳����Ľ��ƥ�����
 * �����ʵ������������Щ��������ʱÿ�ε��ÿ����̵߳�ʱ�򴫽���ֵ������ĻὫǰ��ĸ��ǡ���͵������߳��������󶨵��Ǹո�ֵ��ʵ���������������Լ�
 * ��Ҫ��
 * ��AsyncTask�첽��ÿ�ζ�����һ�����󣬴���Ĳ���Ҳ������������У�����ÿ�����̵߳Ľ�������ҵ��ľ����Լ����ڶ���洢��ʵ�������������Լ���Ҫ��
 * û��ô�鷳������Ҫ���tag�ж�
 * */
public class NewsAdapter extends BaseAdapter {
	private Context mContext;
	private List<NewBean> mListBean;
	private LayoutInflater inflater;
	private ImageLoader mImageLoader;
	private ImageLoader1 mImageLoader1;
	public NewsAdapter(Context context,List<NewBean> listBean) {
		// TODO �Զ����ɵĹ��캯�����
		mContext=context;
		mListBean=listBean;
		inflater=LayoutInflater.from(context);
		mImageLoader=new ImageLoader();
		mImageLoader1=new ImageLoader1();
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
		
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		String url=mListBean.get(position).newsIcon;
//		viewHolder.ivIcon.setTag(url);
		//ÿ�δ���һ�����󣬴���һ���µ��̣߳��̳߳�����һ���߳�
		//showImageByThread����֮������һ���̶��Ķ������û���ִ�������Ϊ���������û�и���Ž��Ž��߳���
		//�ƶ��Ķ����������ɶ���߳��̣߳�������Ϊ��������ı�����������ģ��߳������������Ǻ�����ģ�����
		//showImageByAsyncTask�����������ȥ�ģ����ж����
		
		//������ʵ��
//		mImageLoader.showImageByThread(viewHolder.ivIcon,url);
		//ʵ����
//		mImageLoader.showImageByAsyncTask(viewHolder.ivIcon,url);
		//��Ϊʹ���˻��棬��Ҫ�ù̶��Ķ�����
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
