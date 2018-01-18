package cn.wdcloud.download.downloadUtils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cn.wdcloud.download.MyApp;
import cn.wdcloud.download.downloadUtils.db.DBInterface;
import cn.wdcloud.download.downloadUtils.db.DownloadBean;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by fmm on 2017/12/18.
 */

public class DownloadManager {
    //下载状态
    public static final int STATUS_SUCCESS = 1;//成功
    public static final int STATUS_FAILURE = 0;//失败
    public static final int STATUS_RUNNING = 2;//下载中
    public static final int STATUS_WAITING = 3;//等待
    public static final int STATUS_PAUSE = 4;//暂停下载
    public static final int STATUS_EXCAPTION = -1;//异常
    //获取文件长度失败
    public static final long TOTAL_ERROR = -1;//获取进度失败

    private static final int DEFULT_DOWNLOAD_NUM = 2;//默认的下载最大个数
    private static int MAX_DOWNLOAD_NUM = DEFULT_DOWNLOAD_NUM;
    private static DownloadManager instance;
    private final BaseHttpClient mClient;
    //回调
    private HashMap<String, Call> downCalls = new HashMap<>(); //1、取消下载 2、获得当前下载任务个数时 使用
    private HashMap<String, DownloadSubscribe> mSubscribeMap = new HashMap<>();//修改下载状态使用
    private HashMap<String,Consumer<DownloadBean>> mConsumerHashMap = new HashMap<>();//等待下载回调
    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    public DownloadManager() {
        mClient = new BaseHttpClient();
        //初始化数据库
        DBInterface.getInstance().initDBHelp();
    }

    public void addDownload(String url, Consumer<DownloadBean> consumer) {
        //1、校验url
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            Log.e("addDownload", "下载url错误!");
            return;
        }
        //TODO 正在下载和等待下载  无法再次添加下载
        DownloadBean bean = DBInterface.getInstance().qureByUrl(url);
        if (bean != null && bean.getStatus() != STATUS_WAITING && bean.getStatus() == STATUS_RUNNING) {
            return;
        }

        //添加 Consumer
        if(mConsumerHashMap.containsKey(url)){
            consumer = mConsumerHashMap.get(url);
        }else{
            mConsumerHashMap.put(url,consumer);
        }

        download(url, consumer);
    }


    public void download(String url, Consumer<DownloadBean> consumer) {
        if (consumer == null) {
            return;
        }

        Observable.just(url)
                .filter(new Predicate<String>() {//call的map已经有了,就证明正在下载,则这次不下载
                    @Override
                    public boolean test(String s) throws Exception {
                        return !downCalls.containsKey(s);
                    }
                })
                .flatMap(new Function<String, ObservableSource<DownloadBean>>() {
                    @Override
                    public ObservableSource<DownloadBean> apply(String s) throws Exception {
                        return Observable.just(createDownInfo(s));
                    }
                })
                .map(new Function<DownloadBean, DownloadBean>() {//保存数据库
                    @Override
                    public DownloadBean apply(DownloadBean downloadbean) throws Exception {
                        return saveDB(downloadbean);
                    }
                })
                .filter(new Predicate<DownloadBean>() {//正在下载的数量是否大于最大下载量
                    @Override
                    public boolean test(DownloadBean bean) throws Exception {
                        //返回true则表示数据满足条件，返回false则表示数据需要被过滤
                        return downCalls.size() < MAX_DOWNLOAD_NUM;
                    }
                })
                .flatMap(new Function<DownloadBean, ObservableSource<DownloadBean>>() {//下载
                    @Override
                    public ObservableSource<DownloadBean> apply(DownloadBean downloadbean) throws Exception {
                        DownloadSubscribe subscribe = new DownloadSubscribe(downloadbean);
                        //保存SubscribeMap
                        mSubscribeMap.put(downloadbean.getUrl(), subscribe);
                        //添加下载至数据库
                        //DBInterface.getInstance().insertOrUpdate(downloadbean);
                        return Observable.create(subscribe);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io())//在子线程执行
                .subscribe(consumer);//添加观察者
    }


    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadBean> {
        private DownloadBean downloadbean;

        public void setStatus(int status) {
            downloadbean.setStatus(status);
        }

        public DownloadSubscribe(DownloadBean downloadbean) {
            this.downloadbean = downloadbean;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadBean> emitter) throws Exception {
            String url = downloadbean.getUrl();
            long downloadLength = downloadbean.getCurrentSize();//已经下载好的长度
            long contentLength = downloadbean.getTotalSize();//文件的总长度


            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-")
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();


            File file = new File(downloadbean.getFilePath());
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                try {
                    //初始进度信息  初始化下载开始下载
                    updataDownloadbean(downloadbean, STATUS_RUNNING);
                    emitter.onNext(downloadbean);

                    is = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(file, true);
                    byte[] buffer = new byte[2048];//缓冲数组2kB
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        downloadLength += len;
                        downloadbean.setCurrentSize(downloadLength);
                        //更新数据库
                        DBInterface.getInstance().insertOrUpdate(downloadbean);
                        emitter.onNext(downloadbean);
                        if (downloadbean.getStatus() == STATUS_PAUSE) {//修改暂停状态
                            //跳出循环
                            break;
                        }
                    }
                    fileOutputStream.flush();
                } finally {
                    //关闭IO流
                    IOUtil.closeAll(is, fileOutputStream, response);
                }
                //此时还是处于下载进行状态
                if (downloadbean.getStatus() == STATUS_RUNNING) {
                    updataDownloadbean(downloadbean, STATUS_SUCCESS);
                    DBInterface.getInstance().insertOrUpdate(downloadbean);
                    //发送下载成功状态
                    emitter.onNext(downloadbean);
                    downCalls.remove(url);
                    mConsumerHashMap.remove(url);
                }

                emitter.onComplete();
                //检查等待下载
                checkWaitingStaus();
            } catch (IOException e) {
                //下载异常状态
                updataDownloadbean(downloadbean, STATUS_EXCAPTION);
                DBInterface.getInstance().insertOrUpdate(downloadbean);
                emitter.onError(e);
            }
        }
    }

    /**
     * 暂停下载
     *
     * @param url
     */
    public void pause(String url) {
        stopDownload(url);

    }

    /**
     * 取消下载
     *
     * @param url
     */
    public void delete(final String url) {
        final String url1 = url;
        //1、停止下载
        stopDownload(url1);
        //TODO 待优化---
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        //2、删除数据库
                        DBInterface.getInstance().deleteByUrl(url1);
                        //3、删除本地文件
                        File file = new File(creatFilePath(url1));
                        if (file.exists()) {
                            boolean isSuccess = file.delete();
                            Log.e("删除下载", "" + isSuccess);
                        }
                    }
                });
    }

    /**
     * 设置最大下载个数
     *
     * @param num
     */
    public void dowloadMaxNum(int num) {
        MAX_DOWNLOAD_NUM = num;
    }


    //停止下载
    private void stopDownload(String url) {
        //取消读写流
        DownloadSubscribe subscribe = mSubscribeMap.get(url);
        if (subscribe != null) {
            subscribe.setStatus(STATUS_PAUSE);
        }
        mSubscribeMap.remove(url);

        //移除call
        Call call = downCalls.remove(url);
        if (call != null) {
            call.cancel();
        }

    }

    /**
     * 更新数据库
     *
     * @param bean
     * @param status
     */
    private void updataDownloadbean(DownloadBean bean, int status) {
        if (bean == null) {
            return;
        }
        bean.setStatus(status);
        DBInterface.getInstance().insertOrUpdate(bean);
    }

    //检查是否有等待状态 如果有开始下载
    private void checkWaitingStaus() {
        DownloadBean bean = DBInterface.getInstance().qureFistWaiting();
        if (bean == null) {
            return;
        }
        addDownload(bean.getUrl(),null);
    }


    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadBean createDownInfo(String url) {
        //1、从数据库获得 判断是否下载过
        DownloadBean bean = DBInterface.getInstance().qureByUrl(url);
        if (bean != null) {//已下载
            return bean;
        } else {//未下载
            DownloadBean downloadbean = new DownloadBean();
            long contentLength = getContentLength(url);
            String fileName = getFileName(url);
            String filePath = creatFilePath(url);
            String startTime = System.currentTimeMillis() + "";

            downloadbean.setUrl(url);
            downloadbean.setTotalSize(contentLength);
            downloadbean.setFileName(fileName);
            downloadbean.setStatus(STATUS_WAITING);
            downloadbean.setFilePath(filePath);
            downloadbean.setStartTime(startTime);
            return downloadbean;
        }
    }

    /**
     * 插入或者更新一个
     * @param downloadbean
     * @return
     */
    private DownloadBean saveDB(DownloadBean downloadbean) {
        //添加下载至数据库
        updataDownloadbean(downloadbean,STATUS_WAITING);

        return downloadbean;
    }

    /**
     * 根据下载地址 返回文件路径
     *
     * @param url
     * @return
     */
    private String creatFilePath(String url) {
        //获得文件名
        String fileName = getFileName(url);
        //获得路径
        String packagePath = MyApp.sContext
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        //创建文件
        File file = new File(packagePath, fileName);
        return file.getAbsolutePath();
    }

    /**
     * 获得文件真正的下载地址
     *
     * @param downloadbean
     * @return
     */
    private DownloadBean getRealFileName(DownloadBean downloadbean) {
        String fileName = downloadbean.getFileName();
        long downloadLength = 0, contentLength = downloadbean.getTotalSize();

        String packagePath = MyApp.sContext
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        File file = new File(packagePath, fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }

        //之前下载过,需要重新来一个文件
        int i = 1;
        while (downloadLength >= contentLength) {
            int dotIndex = fileName.lastIndexOf(".");
            String fileNameOther;
            if (dotIndex == -1) {
                fileNameOther = fileName + "(" + i + ")";
            } else {
                fileNameOther = fileName.substring(0, dotIndex)
                        + "(" + i + ")" + fileName.substring(dotIndex);
            }
            File newFile = new File(MyApp.sContext.getFilesDir(), fileNameOther);
            file = newFile;
            downloadLength = newFile.length();
            i++;
        }
        //设置改变过的文件名/大小
        downloadbean.setCurrentSize(downloadLength);
        downloadbean.setFileName(file.getName());
        downloadbean.setFilePath(file.getAbsolutePath());

        Log.e("getRealFileName---->", file.getAbsolutePath());
        return downloadbean;
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TOTAL_ERROR;
    }

    /**
     * 获得文件名称
     *
     * @return
     */
    private String getFileName(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
        return fileName;
    }


    //测试区-------------------------------------------------------------
    //    下载块大小
    private static final int chunk_size = 512 * 1024;

    private class DownloadSubscribe2 implements ObservableOnSubscribe<DownloadBean> {
        private DownloadBean downloadbean;

        public DownloadSubscribe2(DownloadBean downloadbean) {
            this.downloadbean = downloadbean;
        }

        @Override
        public void subscribe(final ObservableEmitter<DownloadBean> e) throws Exception {
            final String url = downloadbean.getUrl();
            long downloadLength = downloadbean.getCurrentSize();//已经下载好的长度
            long contentLength = downloadbean.getTotalSize();//文件的总长度
            //初始进度信息
            e.onNext(downloadbean);

//            Request request = new Request.Builder()
//                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
//                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
//                    .url(url)
//                    .build();
            Request request = new Request.Builder()
                    .tag(url.toString())
                    .url(url)
                    .header("Range", "bytes=" + downloadLength + "-")
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            //Response response = call.execute();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("wdedu", "onFailure：bytesRead-->");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    File file = new File(downloadbean.getFilePath());

                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();

                    BufferedSink sink = null;
                    if (downloadbean.getCurrentSize() > 0) {//已经下载
                        sink = Okio.buffer(Okio.appendingSink(file));
                    } else {
                        sink = Okio.buffer(Okio.sink(file));
                    }
                    long bytesRead = downloadbean.getCurrentSize(), chunk = 0;
                    //9、read buff
                    while ((chunk = source.read(sink.buffer(), chunk_size)) != -1) {
                        sink.emit();
                        sink.flush();
                        bytesRead += chunk;

                        Log.d("wdedu", "下载进度：bytesRead-->" + bytesRead);
                    }
                    sink.flush();
                    sink.close();
                    source.close();
                    e.onComplete();//完成
                }
            });

        }
    }

    //获得当前运行线程
    public void getCurrentThread() {
        long tID = Thread.currentThread().getId();
        String tName = Thread.currentThread().getName();
        Log.e("getCurrentThread", "tID:" + tID + "--tName:" + tName);
    }
}
