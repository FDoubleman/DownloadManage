package cn.wdcloud.download;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import cn.wdcloud.download.downloadUtils.DownloadManager;
import cn.wdcloud.download.downloadUtils.db.DownloadBean;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar mBp1;
    private ProgressBar mBp2;
    private ProgressBar mBp3;
    private Button btnApplist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBp1 = (ProgressBar) findViewById(R.id.pb_1);
        mBp2 = (ProgressBar) findViewById(R.id.pb_2);
        mBp3 = (ProgressBar) findViewById(R.id.pb_3);
        btnApplist = (Button) findViewById(R.id.btn_applist);

        Button btnStart1 = (Button) findViewById(R.id.btn_start_1);
        Button btnStart2 = (Button) findViewById(R.id.btn_start_2);
        Button btnStart3 = (Button) findViewById(R.id.btn_start_3);

        Button btnCancle1 = (Button) findViewById(R.id.btn_cancle_1);
        Button btnCancle2 = (Button) findViewById(R.id.btn_cancle_2);
        Button btnCancle3 = (Button) findViewById(R.id.btn_cancle_3);

        Button btnDelete1 = (Button) findViewById(R.id.btn_delete_1);
        Button btnDelete2 = (Button) findViewById(R.id.btn_delete_2);
        Button btnDelete3 = (Button) findViewById(R.id.btn_delete_3);

        btnStart1.setOnClickListener(this);
        btnStart2.setOnClickListener(this);
        btnStart3.setOnClickListener(this);

        btnCancle1.setOnClickListener(this);
        btnCancle2.setOnClickListener(this);
        btnCancle3.setOnClickListener(this);

        btnDelete1.setOnClickListener(this);
        btnDelete2.setOnClickListener(this);
        btnDelete3.setOnClickListener(this);
        btnApplist.setOnClickListener(this);
    }

    private String url1 = "http://192.168.6.100:8082/group5/M00/13/06/wKgG0VoqBxqET16uAAAAAAAAAAA904.mov";
    private String url2 = "https://dldir1.qq.com/qqfile/qq/QQ9.0.0/22692/QQ9.0.0Trial.exe";
    private String url3 = "http://dl.wdcloud.cc/group6/M01/3C/DC/pIYBAFoJaKOADvMvAf9TJHnjQ4c362.apk?filename=云上国学-生产-1.0.1-2017.11.13-14.36.31.apk";

    @Override
    public void onClick(View view) {
        int vID = view.getId();
        switch (vID) {
            case R.id.btn_start_1:
                DownloadManager.getInstance().addDownload(url1, new DownloadLinstanerImp());
                break;
            case R.id.btn_start_2:
                DownloadManager.getInstance().addDownload(url2, new DownloadLinstanerImp());
                break;
            case R.id.btn_start_3:
                DownloadManager.getInstance().addDownload(url3, new DownloadLinstanerImp());
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
            case R.id.btn_delete_1:
                DownloadManager.getInstance().delete(url1);
                mBp1.setProgress(0);
                break;
            case R.id.btn_delete_2:
                DownloadManager.getInstance().delete(url2);
                mBp2.setProgress(0);
                break;
            case R.id.btn_delete_3:
                DownloadManager.getInstance().delete(url3);
                mBp3.setProgress(0);
//                textFilter();
                break;
            case R.id.btn_applist:

                startActivity(new Intent(this,AppListActivity.class));
                break;
            default:
                break;
        }
    }

    public class DownloadLinstanerImp implements DownloadManager.DownloadListener {
        @Override
        public void start(String url) {

        }

        @Override
        public void stop(DownloadBean downloadbean) {

        }

        @Override
        public void cancle(String url) {

        }

        @Override
        public void success(DownloadBean downloadbean) {

        }

        @Override
        public void error(DownloadBean downloadbean) {

        }

        @Override
        public void downloading(DownloadBean downloadbean) {
            Log.e("onNext1:", "TotalSize:---" + downloadbean.getTotalSize()
                    + "----CurrentSize----" + downloadbean.getCurrentSize());

            if (downloadbean.getUrl().equals(url1)) {
                mBp1.setMax((int) downloadbean.getTotalSize().intValue());
                mBp1.setProgress((int) downloadbean.getCurrentSize().intValue());
            } else if (downloadbean.getUrl().equals(url2)) {
                mBp2.setMax((int) downloadbean.getTotalSize().intValue());
                mBp2.setProgress((int) downloadbean.getCurrentSize().intValue());
            } else if (downloadbean.getUrl().equals(url3)) {
                mBp3.setMax((int) downloadbean.getTotalSize().intValue());
                mBp3.setProgress((int) downloadbean.getCurrentSize().intValue());
            }


        }
    }


    public void textFilter() {
        Observable.just(1, 2, 3, 4)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer.intValue() < 0;
                    }
                }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                Log.e("onSubscribe", "----onSubscribe");
            }

            @Override
            public void onNext(Integer integer) {
                Log.e("onNext", "----" + integer.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("onError", "----就是666");
            }

            @Override
            public void onComplete() {
                Log.e("onComplete", "----就是666");
            }
        });
    }
}
