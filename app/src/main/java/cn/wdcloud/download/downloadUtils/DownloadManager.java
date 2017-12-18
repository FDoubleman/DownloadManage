package cn.wdcloud.download.downloadUtils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import cn.wdcloud.download.MyApp;
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
    }

    public void download(String url, DownLoadObserver loadObserver) {
        Observable.just(url)
                .filter(new Predicate<String>() {//call的map已经有了,就证明正在下载,则这次不下载
                    @Override
                    public boolean test(String s) throws Exception {
                        return !downCalls.containsKey(s);
                    }
                })
                .flatMap(new Function<String, ObservableSource<DownloadInfo>>() {
                    @Override
                    public ObservableSource<DownloadInfo> apply(String s) throws Exception {
                        return Observable.just(createDownInfo(s));
                    }
                })
                .map(new Function<DownloadInfo, DownloadInfo>() {//检测本地文件夹,生成新的文件名
                    @Override
                    public DownloadInfo apply(DownloadInfo downloadInfo) throws Exception {
                        return getRealFileName(downloadInfo);
                    }
                })
                .flatMap(new Function<DownloadInfo, ObservableSource<DownloadInfo>>() {//下载
                    @Override
                    public ObservableSource<DownloadInfo> apply(DownloadInfo downloadInfo) throws Exception {
                        DownloadSubscribe subscribe = new DownloadSubscribe(downloadInfo);
                        //保存SubscribeMap
                        mSubscribeMap.put(downloadInfo.getUrl(),subscribe);

                        return Observable.create(subscribe);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io())//在子线程执行
                .subscribe(loadObserver);//添加观察者
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url) {
        DownloadInfo downloadInfo = new DownloadInfo();

        long contentLength = getContentLength(url);
        String fileName = getFileName(url);

        downloadInfo.setUrl(url);
        downloadInfo.setTotalSize(contentLength);
        downloadInfo.setFileName(fileName);
        downloadInfo.setStatus(STATUS_WAITING);

        return downloadInfo;
    }

    /**
     * 获得文件真正的下载地址
     *
     * @param downloadInfo
     * @return
     */
    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0, contentLength = downloadInfo.getTotalSize();

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
        downloadInfo.setCurrentSize(downloadLength);
        downloadInfo.setFileName(file.getName());
        downloadInfo.setFilePath(file.getAbsolutePath());
        Log.e("getRealFileName---->",file.getAbsolutePath());
        return downloadInfo;
    }


    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;

        public void setPause(){
            downloadInfo.setStatus(STATUS_PAUSE);
        }
        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getCurrentSize();//已经下载好的长度
            long contentLength = downloadInfo.getTotalSize();//文件的总长度
            //初始进度信息
            e.onNext(downloadInfo);

            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" )
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            File file = new File(downloadInfo.getFilePath());
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
                    if(downloadInfo.getStatus() ==STATUS_PAUSE){//暂停
                        //跳出循环
                        break;
                    }
                    downloadInfo.setCurrentSize(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();

                //暂停下载
                if (downloadInfo.getStatus() ==STATUS_PAUSE) {
                    call.cancel();//取消
                }
                downCalls.remove(url);
            } finally {
                //关闭IO流
                IOUtil.closeAll(is, fileOutputStream);

            }
            e.onComplete();//完成
        }
    }

    /**
     * 取消下载
     * @param url
     */
    public void pause(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    /**
     * 取消下载
     * @param url
     */
    public void cancel(final String url) {
        //取消读流
        DownloadSubscribe subscribe = mSubscribeMap.get(url);
        if(subscribe!=null){
            subscribe.setPause();
        }
        mSubscribeMap.remove(url);


//        Call call = downCalls.get(url);
//        if (call != null) {
//            call.cancel();//取消
//        }
//        downCalls.remove(url);

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
        return fileName;
    }






    //测试区-------------------------------------------------------------
    //    下载块大小
    private static final int chunk_size = 512 * 1024;
    private class DownloadSubscribe2 implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;

        public DownloadSubscribe2(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(final ObservableEmitter<DownloadInfo> e) throws Exception {
            final String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getCurrentSize();//已经下载好的长度
            long contentLength = downloadInfo.getTotalSize();//文件的总长度
            //初始进度信息
            e.onNext(downloadInfo);

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
                    File file = new File(downloadInfo.getFilePath());

                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();

                    BufferedSink sink = null;
                    if (downloadInfo.getCurrentSize() > 0) {//已经下载
                        sink = Okio.buffer(Okio.appendingSink(file));
                    } else {
                        sink = Okio.buffer(Okio.sink(file));
                    }
                    long bytesRead = downloadInfo.getCurrentSize(), chunk = 0;
                    //9、read buff
                    while ((chunk = source.read(sink.buffer(), chunk_size)) != -1) {
                        sink.emit();
                        sink.flush();
                        bytesRead += chunk;

                        Log.d("wdedu", "下载进度：bytesRead-->"+bytesRead);
                    }
                    sink.flush();
                    sink.close();
                    source.close();
                    e.onComplete();//完成
                }
            });

        }
    }
}
