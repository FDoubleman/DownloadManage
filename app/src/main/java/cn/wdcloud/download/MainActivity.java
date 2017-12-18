package cn.wdcloud.download;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import cn.wdcloud.download.downloadUtils.DownLoadObserver;
import cn.wdcloud.download.downloadUtils.DownloadInfo;
import cn.wdcloud.download.downloadUtils.DownloadManager;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar mBp1;
    private ProgressBar mBp2;
    private ProgressBar mBp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBp1 = (ProgressBar) findViewById(R.id.pb_1);
        mBp2 = (ProgressBar) findViewById(R.id.pb_2);
        mBp3 = (ProgressBar) findViewById(R.id.pb_3);

        Button btnStart1 = (Button) findViewById(R.id.btn_start_1);
        Button btnStart2 = (Button) findViewById(R.id.btn_start_2);
        Button btnStart3 = (Button) findViewById(R.id.btn_start_3);

        Button btnCancle1 = (Button) findViewById(R.id.btn_cancle_1);
        Button btnCancle2 = (Button) findViewById(R.id.btn_cancle_2);
        Button btnCancle3 = (Button) findViewById(R.id.btn_cancle_3);

        btnStart1.setOnClickListener(this);
        btnStart2.setOnClickListener(this);
        btnStart3.setOnClickListener(this);

        btnCancle1.setOnClickListener(this);
        btnCancle2.setOnClickListener(this);
        btnCancle3.setOnClickListener(this);
    }

    private String url1 ="http://192.168.6.100:8082/group5/M00/13/06/wKgG0VoqBxqET16uAAAAAAAAAAA904.mov";
    private String url2 ="https://dldir1.qq.com/qqfile/qq/QQ9.0.0/22692/QQ9.0.0Trial.exe";
    private String url3 ="http://dl.wdcloud.cc/group6/M01/3C/DC/pIYBAFoJaKOADvMvAf9TJHnjQ4c362.apk?filename=云上国学-生产-1.0.1-2017.11.13-14.36.31.apk";
    @Override
    public void onClick(View view) {
        int vID = view.getId();
        switch (vID) {
            case R.id.btn_start_1:
                DownloadManager.getInstance().download(url1, new DownLoadObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        super.onNext(downloadInfo);
                        Log.e("onNext1:","TotalSize:---"+downloadInfo.getTotalSize()
                                +"----CurrentSize----"+downloadInfo.getCurrentSize());
                        mBp1.setMax((int) downloadInfo.getTotalSize());
                        mBp1.setProgress((int) downloadInfo.getCurrentSize());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("onError:","下载失败：");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();

                        Log.e("onComplete:","下载成功："+downloadInfo.getCurrentSize());
                    }
                });
                break;
            case R.id.btn_start_2:
                DownloadManager.getInstance().download(url2, new DownLoadObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        super.onNext(downloadInfo);
                        Log.e("onNext2:","TotalSize:---"+downloadInfo.getTotalSize()
                                +"----CurrentSize----"+downloadInfo.getCurrentSize());
                        mBp2.setMax((int) downloadInfo.getTotalSize());
                        mBp2.setProgress((int) downloadInfo.getCurrentSize());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("onError:","下载失败：");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();

                        Log.e("onComplete:","下载成功："+downloadInfo.getCurrentSize());
                    }
                });
                break;
            case R.id.btn_start_3:
                DownloadManager.getInstance().download(url3, new DownLoadObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        super.onNext(downloadInfo);
                        Log.e("onNext3:","TotalSize:---"+downloadInfo.getTotalSize()
                                +"----CurrentSize----"+downloadInfo.getCurrentSize());
                        mBp3.setMax((int) downloadInfo.getTotalSize());
                        mBp3.setProgress((int) downloadInfo.getCurrentSize());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("onError:","下载失败：");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        Log.e("onComplete:","下载成功："+downloadInfo.getCurrentSize());
                    }
                });
                break;

            case R.id.btn_cancle_1:
                DownloadManager.getInstance().cancel(url1);
                break;
            case R.id.btn_cancle_2:
                DownloadManager.getInstance().cancel(url2);
                break;
            case R.id.btn_cancle_3:
                DownloadManager.getInstance().cancel(url3);
                break;
            default:
                break;
        }
    }
}
