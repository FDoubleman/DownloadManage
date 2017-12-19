package cn.wdcloud.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import cn.wdcloud.download.downloadUtils.DownLoadObserver;
import cn.wdcloud.download.downloadUtils.DownloadManager;
import cn.wdcloud.download.downloadUtils.db.DownloadBean;
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
                DownloadManager.getInstance().addDownload(url1, new DownLoadObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onNext(DownloadBean downloadbean) {
                        super.onNext(downloadbean);
                        Log.e("onNext1:","TotalSize:---"+downloadbean.getTotalSize()
                                +"----CurrentSize----"+downloadbean.getCurrentSize());
                        mBp1.setMax((int) downloadbean.getTotalSize().intValue());
                        mBp1.setProgress((int) downloadbean.getCurrentSize().intValue());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("onError:","下载失败：");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();

                        Log.e("onComplete:","下载成功："+downloadbean.getCurrentSize());
                    }
                });
                break;
            case R.id.btn_start_2:
                DownloadManager.getInstance().addDownload(url2, new DownLoadObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onNext(DownloadBean downloadbean) {
                        super.onNext(downloadbean);
                        Log.e("onNext2:","TotalSize:---"+downloadbean.getTotalSize()
                                +"----CurrentSize----"+downloadbean.getCurrentSize());
                        mBp2.setMax( downloadbean.getTotalSize().intValue());
                        mBp2.setProgress(downloadbean.getCurrentSize().intValue());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("onError:","下载失败：");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        Log.e("onComplete:","下载成功："+downloadbean.getCurrentSize());
                    }
                });
                break;
            case R.id.btn_start_3:
                DownloadManager.getInstance().addDownload(url3, new DownLoadObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onNext(DownloadBean downloadbean) {
                        super.onNext(downloadbean);
                        Log.e("onNext3:","TotalSize:---"+downloadbean.getTotalSize()
                                +"----CurrentSize----"+downloadbean.getCurrentSize());
                        mBp3.setMax( downloadbean.getTotalSize().intValue());
                        mBp3.setProgress( downloadbean.getCurrentSize().intValue());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("onError:","下载失败：");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if(downloadbean!=null){
                            Log.e("onComplete:","下载成功："+downloadbean.getCurrentSize());
                        }

                    }
                });
                break;

            case R.id.btn_cancle_1:
                DownloadManager.getInstance().pause(url1);
                break;
            case R.id.btn_cancle_2:
                DownloadManager.getInstance().pause(url2);
                break;
            case R.id.btn_cancle_3:
                DownloadManager.getInstance().pause(url3);
                break;
            default:
                break;
        }
    }
}
