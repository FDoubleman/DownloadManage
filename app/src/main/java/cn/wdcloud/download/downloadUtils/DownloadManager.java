package cn.wdcloud.download.downloadUtils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import cn.wdcloud.download.MyApp;
import cn.wdcloud.download.downloadUtils.db.DBInterface;
import cn.wdcloud.download.downloadUtils.db.DownloadBean;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private static final int STATUS_SUCCESS = 1;//成功
    private static final int STATUS_FAILURE = 0;//失败
    private static final int STATUS_RUNNING = 2;//下载中
    private static final int STATUS_WAITING = 3;//等待
    private static final int STATUS_PAUSE = 4;//暂停下载
    private static final int STATUS_EXCAPTION = -1;//异常
    //获取文件长度失败
    public static final long TOTAL_ERROR = -1;//获取进度失败

    private static DownloadManager instance;
    private final BaseHttpClient mClient;
    //回调
    private HashMap<String, Call> downCalls = new HashMap<>();
    private HashMap<String, DownloadSubscribe> mSubscribeMap = new HashMap<>();

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

    public void addDownload(String url, DownLoadObserver loadObserver) {
        //1、校验url
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            Log.e("addDownload", "下载url错误!");
            return;
        }


        download(url, loadObserver);
    }


    public void download(String url, DownLoadObserver loadObserver) {

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
                .map(new Function<DownloadBean, DownloadBean>() {//检测本地文件夹,生成新的文件名
                    @Override
                    public DownloadBean apply(DownloadBean downloadbean) throws Exception {
                        return getFileName(downloadbean);
                    }
                })
                .flatMap(new Function<DownloadBean, ObservableSource<DownloadBean>>() {//下载
                    @Override
                    public ObservableSource<DownloadBean> apply(DownloadBean downloadbean) throws Exception {
                        DownloadSubscribe subscribe = new DownloadSubscribe(downloadbean);
                        //保存SubscribeMap
                        mSubscribeMap.put(downloadbean.getUrl(), subscribe);
                        //添加下载至数据库
                        DBInterface.getInstance().insertOrUpdate(downloadbean);
                        return Observable.create(subscribe);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io())//在子线程执行
                .subscribe(loadObserver);//添加观察者
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
        public void subscribe(ObservableEmitter<DownloadBean> e) throws Exception {
            String url = downloadbean.getUrl();
            long downloadLength = downloadbean.getCurrentSize();//已经下载好的长度
            long contentLength = downloadbean.getTotalSize();//文件的总长度
            //初始进度信息
            e.onNext(downloadbean);

            updataDownloadbean(downloadbean, STATUS_RUNNING);

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
                    if (downloadbean.getStatus() == STATUS_PAUSE) {//暂停
                        //跳出循环
                        break;
                    }
                    e.onNext(downloadbean);
                }
                fileOutputStream.flush();

                //暂停下载
                if (downloadbean.getStatus() == STATUS_PAUSE) {
                    call.cancel();//取消
                }
                downCalls.remove(url);
            } finally {
                //关闭IO流
                IOUtil.closeAll(is, fileOutputStream);

                updataDownloadbean(downloadbean, STATUS_EXCAPTION);
                DBInterface.getInstance().insertOrUpdate(downloadbean);

            }
            //此时还是处于下载进行状态
            if (downloadbean.getStatus() == STATUS_RUNNING) {
                updataDownloadbean(downloadbean, STATUS_SUCCESS);
                DBInterface.getInstance().insertOrUpdate(downloadbean);
            }
            e.onComplete();
        }
    }

    /**
     * 暂停下载
     *
     * @param url
     */
    public void pause(String url) {
        stopDownload(url);
        //TODO do else db
        DownloadBean bean = DBInterface.getInstance().qureByUrl(url);
        updataDownloadbean(bean, STATUS_PAUSE);

    }

    /**
     * 取消下载
     *
     * @param url
     */
    public void cancel(final String url) {
        stopDownload(url);
        //TODO do else db
        DBInterface.getInstance().deleteByUrl(url);

    }

    //停止下载
    private void stopDownload(String url) {
        //取消读写流
        DownloadSubscribe subscribe = mSubscribeMap.get(url);
        if (subscribe != null) {
            subscribe.setStatus(STATUS_PAUSE);
        }
        mSubscribeMap.remove(url);
    }

    /**
     * 更新数据库
     *
     * @param bean
     * @param status
     */
    public void updataDownloadbean(DownloadBean bean, int status) {
        if (bean == null) {
            return;
        }
        bean.setStatus(status);
        DBInterface.getInstance().insertOrUpdate(bean);
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
            downloadbean.setUrl(url);
            downloadbean.setTotalSize(contentLength);
            downloadbean.setFileName(fileName);
            downloadbean.setStatus(STATUS_WAITING);

            return downloadbean;
        }
    }

    private DownloadBean getFileName(DownloadBean downloadbean){
        long downloadLength = 0;
        String fileName = downloadbean.getFileName();
        String packagePath = MyApp.sContext
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file = new File(packagePath, fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        downloadbean.setFilePath(file.getAbsolutePath());
        //downloadbean.setCurrentSize(downloadLength);
        return downloadbean;
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
        String fileName = url.substring(url.lastIndexOf("/"));
        if(fileName.contains("?")){
            fileName = fileName.replaceAll("[?]","p");
            //fileName.replaceAll("/","p");
        }
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
