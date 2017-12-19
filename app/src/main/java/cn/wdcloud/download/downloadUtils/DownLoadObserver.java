package cn.wdcloud.download.downloadUtils;

import cn.wdcloud.download.downloadUtils.db.DownloadBean;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fmm on 2017/12/18.
 */

public abstract class DownLoadObserver implements Observer<DownloadBean> {

    protected Disposable d;//可以用于取消注册的监听者
    protected DownloadBean downloadbean;
    @Override
    public void onSubscribe(Disposable d) {
         this.d =d;
    }

    @Override
    public void onNext(DownloadBean downloadbean) {
        this.downloadbean =downloadbean;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {

    }
}
