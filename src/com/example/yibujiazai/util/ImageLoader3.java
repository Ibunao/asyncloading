package com.example.yibujiazai.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.yibujiazai.NewsAdapter2;
import com.example.yibujiazai.NewsAdapter3;
import com.example.yibujiazai.R;
import com.example.yibujiazai.ImageLoader2.NewsAsyncTask;


/**
 * ͼƬ�ĸ�Ч�����ࡣ
 * ��Ҫͨ��LruCache�ڴ滺���DiskLruCache���̻�����������
 * �����ʵ��˼�룺
  ͨ��loadBitmap�������ֱ�ӻ��桢���̺������м���ͼƬ(������һ��һ�����жϵģ�
 * ��������������ͼƬ����䲻������ִ��)��
 * ���裺
 * 1��ImageLoader���췽������ʼ��LruCache��DiskLruCache�ࡣ
 * 2���ֱ�ʵ��LruCache��DiskLruCache�����ӡ�ɾ�������ҷ�����
 */
public class ImageLoader3 {
    private static final String TAG = "ImageLoader";
    private LruCache<String,Bitmap> mMemoryCache;
    //��Ҫ�ֶ��������DiskLruCache��
    private DiskLruCache mDiskLruCache;
    private Context mContext;
    private boolean mIsDiskLruCacheCreated = false;
    private static final int TAG_KEY_URI = R.id.imageview;
    private static final int IO_BUFFER_SIZE = 8*1024;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int MESSAGE_POST_result = 1;
    private static ListView mListView;

    //cpu������
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    //cpu�����߳���
    private static final int CORE_POOL_SIZE = CPU_COUNT +1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT *2+1;
    //�̳߳�ʱʱ��
    private static final long KEEP_ALIVE = 10L;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        //AtomicInteger��һ���ṩԭ�Ӳ�����Integer���ࡣ��Java�����У�++i��i++�����������̰߳�ȫ�ģ���ʹ�õ�ʱ��
        // ���ɱ���Ļ��õ�synchronized�ؼ��֡���AtomicInteger��ͨ��һ���̰߳�ȫ�ļӼ������ӿڡ�
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            //�����µ��̲߳������߳�����
            //mCount.getAndIncrement()��ȡ��ǰ��ֵ��������
            return new Thread(r,"imageLoader#"+mCount.getAndIncrement());
        }
    };
    //�����̳߳أ�����������߳̽��д��������ٶ���ɵĿ���
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),sThreadFactory);
    //Ҫˢ��UI��handlerҪ�õ����̵߳�looper����ô�����߳� Handler handler = new Handler();��
    // ����������̣߳�ҲҪ����������ܵĻ���ҪHandler handler = new Handler(Looper.getMainLooper());
    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView iv = result.imageView;
            Bitmap bitmap = result.bitmap;
            String getUrl = (String) iv.getTag();
            if(getUrl.equals(result.url)){
                iv.setImageBitmap(bitmap);
            }else{
//                Log.i(TAG,"set image bitmap,but url has changed");
            }
        }
    };
    //�޲ι��췽��������ʼ��LruCache|DiskLruCache
    public ImageLoader3(Context context){
        mContext = context.getApplicationContext();
        initLruCache();
        initDiskLruCache();
    }
    public ImageLoader3(Context context,ListView mListView){
    	this.mListView=mListView;
        mContext = context.getApplicationContext();
        initLruCache();
        initDiskLruCache();
    }
    /**
     * ��ʼ��LruCache
     */
    private void initLruCache(){
        //���㻺��Ĵ�С
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //���û����СΪ��ǰ���̿����ڴ��1/4
        int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }

    /**
     * ��ʼ��DiskLruCache
     */
    private void initDiskLruCache(){
        //���Ӧ�ó������ڴ���λ��+/bitmap
        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if(!diskCacheDir.exists()){ //��������� ����Ŀ¼
            diskCacheDir.mkdirs();
        }
        //������̿��ÿռ������Ҫ�����Ļ���ռ��򴴽�
        if(getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void showImages(ImageView inImageView,final String url,int mPosition){
    	Bitmap bitmap=loadBitmapFromMemCache(url);
    	if (mPosition==1) {
			Log.i("ding", url);
		}
		if (bitmap!=null) {
			inImageView.setImageBitmap(bitmap);
		} else {
			Log.i("bunao", mPosition+"");
			inImageView.setImageResource(R.drawable.ic_launcher);
		}
    }
    public void bindBitmap(String url,ImageView imageView){
        bindBitmap(url,imageView,0,0);
    }
    public void bindBitmap(int mStart,int mEnd,final int reqWidth,
            final int reqHeight){
    	for (int i = mStart; i < mEnd; i++) {
			String url=NewsAdapter3.URLS[i];
			ImageView imageView=(ImageView) mListView.findViewWithTag(url);
			bindBitmap(url,imageView,reqWidth,reqWidth);
		}
	}
    	
 
    /**
     * ͨ���첽��ʽ����bitmap��
     * ���裬�ȴӻ����м���bitmap��������ھ�ֱ�ӷ��أ�������ͼƬ��
     * �����ڣ��ͻ����̳߳��е���loadBitmap������������UI
     * @param url
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */
  
    public void bindBitmap(final String url, final ImageView imageView,final int reqWidth,
                           final int reqHeight){
        //���ñ��
//        imageView.setTag(TAG_KEY_URI,url);
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
            //����
            return;
        }
        //��ȡͼƬ
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                //�̳߳��м���bitmap������loadBitmap()����
                Bitmap bitmap = loadBitmap(url,reqWidth,reqHeight);
                if(bitmap!=null){
                    LoaderResult result = new LoaderResult(imageView,url,bitmap);
                    
                    mMainHandler.obtainMessage(MESSAGE_POST_result,result).sendToTarget();
                }
            }
        };
        //�̳߳���ִ��loadBitmapTask
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }
    /**
     * ��ȡ�����п��õĿռ�
     * @param path
     * @return
     */
    private long getUsableSpace(File path) {
        //Build.VERSION.��ǰʹ�ð汾Build.VERSION_CODES.�ɰ汾GINGERBREAD��2.3
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return stats.getBlockSize()*stats.getAvailableBlocks();
    }

    /**
     * ͬ����������ͼƬ
     * ����ͼƬ��˳��
     * �����м���ͼƬ--->�����м���ͼƬ--->�����м���ͼƬ
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap loadBitmap(String url,int reqWidth,int reqHeight){
        //�ӻ����м���ͼƬ
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if(bitmap!=null){
            return bitmap;
        }
        try {
            //�Ӵ����м���bitmap
            bitmap = loadBitmapFromDiskCache(url,reqWidth,reqHeight);
            if(bitmap !=null){
                return bitmap;
            }
            //�������м���bitmap
            bitmap = loadBitmapFromHttp(url, reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bitmap == null && !mIsDiskLruCacheCreated){
        //���bitmapΪ�գ����ң�������û�л���bitmap����ôͨ��url·������ȡbitmap
            bitmap = downloadBitmapFromUrl(url);
        }
        return bitmap;
    }

    /**
     * ͨ��url·����������ͼƬ��
     * ��ȡbitmap��
     * @param urlString
     * @return
     */
    private Bitmap downloadBitmapFromUrl(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            bis = new BufferedInputStream(conn.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(conn!=null)
                conn.disconnect();
            if(bis!=null){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * �ӻ����м���bitmap
     * @param url
     * @return
     */
    private Bitmap loadBitmapFromMemCache(String url) {
        String key = hashKeyFromUrl(url);
//        Log.i(TAG,"�ӻ����м�������");
        return getBitmapFromMemoryCache(key);
    }

    /**
     * LruCache����ӷ���
     * �洢�������С�
     * @param key
     * @param bitmap
     */

    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemoryCache(key)==null)
            mMemoryCache.put(key,bitmap);
    }

    /**
     * LruCache�Ĳ��ҷ���
     * �ӻ����в�ѯ
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

    /**
     * ��ȡ���̵Ĵ洢Ŀ¼��������Ŀ¼
     * @param mContext
     * @param bitmap
     * @return
     */
    private File getDiskCacheDir(Context mContext, String bitmap) {
        //�ж��Ƿ�����ⲿ�ڴ�
        boolean externalStorageAvailable = Environment.getExternalStorageState().
                equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if(externalStorageAvailable){
            //��ȡӦ�ó�����ⲿ�ڴ滺��λ��
            cachePath = mContext.getExternalCacheDir().getPath();
        }else{
            //�ڲ�����λ��
            cachePath = mContext.getCacheDir().getPath();
        }
        //File.separator�ָ����ʹ�����Ͳ��ù���ʲôϵͳ��
        return new File(cachePath + File.separator+ bitmap);
    }

    /**
     * DiskLruCache��ӷ���(�洢)
     * ��ͼƬ�洢�����ش��̻���
     * @param urlString
     * @param outputStream
     * @return
     */
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream){
        HttpURLConnection conn = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(conn.getInputStream(),IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);
            int b;
            while((b = in.read())!=-1){
                out.write(b);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(conn !=null)
                conn.disconnect();
            if(in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * �������ϼ���ͼƬ
     * ֱ�����ص����̻���
     * @param urlString
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadBitmapFromHttp(String urlString,int reqWidth,int reqHeight) throws IOException {
//        Log.i(TAG,"�������м�������");

        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("���ܴ����߳��з�������ͼƬ");
        }
        if(mDiskLruCache == null){
            return null;
        }
        //�浽����
        
        //��urlת����key
        String key = hashKeyFromUrl(urlString);

        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor!=null){
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if(downloadUrlToStream(urlString,outputStream)){ //�洢�������ļ�����
                editor.commit();
            }else{
                //δ�������
                editor.abort();
            }
        }
        //ˢ��д��
        mDiskLruCache.flush();
        return loadBitmapFromDiskCache(urlString,reqWidth,reqHeight);
    }

    /**
     *
     * ��urlת����String���͵�keyֵ
     * ��Ϊurl�к��п����������ַ�
     * �������url��md5ֵ��Ϊkeyֵ
     * @param url
     * @return
     */
    private String hashKeyFromUrl(String url){
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
           cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    /**
     * ���彫urlת����MD5ֵ�ķ�ʽ��
     * @param digest
     * @return
     */
    private String bytesToHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<digest.length;i++){
            String hex = Integer.toHexString(0xFF & digest[i]);
            if(hex.length() == 1){
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * DiskLruCache�ǲ�ѯ����
     * �Ӵ����л�ȡBitmap
     * @param urlString
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadBitmapFromDiskCache(String urlString, int reqWidth, int reqHeight) throws IOException {
//        Log.i(TAG,"�Ӵ����м�������");
        //�жϵ�ǰ��looper�Ƿ������̵߳�looper��Ҳ�����ж��Ƿ����������߳�
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("���ܴ����߳��м���ͼƬ");
        }
        if(mDiskLruCache == null){
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(urlString);
        //ͨ��keyֵ��ȡ�����е�������
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot !=null){
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor descriptor = fileInputStream.getFD();
            //ͨ��д��һ��ImageResizer��Ч����ͼƬ
            bitmap = ImageResizer.decodeSampleBitmapFromFile(descriptor,reqWidth,reqHeight);
            if(bitmap !=null){
                //�򻺴��м���ͼƬ
                addBitmapToMemoryCache(key,bitmap);
            }
        }
        return bitmap;
    }

    /**
     * ��̬��������ImageLoader
     * @param context
     * @return
     */
    public static ImageLoader3 bindBitmap(Context context){
        return new ImageLoader3(context);
    }

    private class LoaderResult {
        public ImageView imageView;
        public String url;
        public Bitmap bitmap;
        public LoaderResult(ImageView imageView, String url, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.imageView = imageView;
            this.url = url;
        }
    }
}
