package cn.wdcloud.download.downloadUtils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.Query;

import java.util.List;

import cn.wdcloud.download.MyApp;

/**
 * Created by fmm on 2017/12/19.
 */

public class DBInterface {
    private Context mContext;
    private static DBInterface instance;
    private String DEFULT_DB_NAME ="mydefult.db";
    private DaoMaster.DevOpenHelper openHelper;

    private static final int STATUS_SUCCESS = 1;//成功
    private static final int STATUS_FAILURE = 0;//失败
    private static final int STATUS_RUNNING = 2;//下载中
    private static final int STATUS_WAITING = 3;//等待
    private static final int STATUS_PAUSE = 4;//暂停下载
    private static final int STATUS_EXCAPTION = -1;//异常

    public static DBInterface getInstance(){
        if(instance ==null){
            synchronized (DBInterface.class){
                if(instance ==null){
                    instance =new DBInterface();
                }
            }
        }

        return instance;
    }

    public DBInterface(){
        mContext = MyApp.sContext;
    }

    //初始化数据库
    public void initDBHelp() {
        reset();
        Log.d("initDBHelp", "打开数据库");
        openHelper = new DaoMaster.DevOpenHelper(mContext, DEFULT_DB_NAME, null);
    }

    private void reset() {
        if (openHelper != null) {
            Log.d("reset", "关闭数据库");
            openHelper.close();
            openHelper = null;
        }
    }

    private void isInitOk() {
        if (openHelper == null) {
            Log.e("isInitOk", "openHelper未初始化");
            openHelper = new DaoMaster.DevOpenHelper(mContext, DEFULT_DB_NAME, null);
            Log.e("isInitOk", "openHelper重新初始化");
        }
    }

    private DaoSession openWritableDb() {
        isInitOk();
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        return master.newSession();
    }

    private DaoSession openReadAbleDb() {
        isInitOk();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        DaoMaster master = new DaoMaster(db);
        return master.newSession();
    }

    /**
     * 增 修改
     */
    public long insertOrUpdate(DownloadBean downloadBean) {
        DownloadBeanDao dao = openWritableDb().getDownloadBeanDao();
        return dao.insertOrReplace(downloadBean);
    }

    public long insert(DownloadBean downloadBean) {
        DownloadBeanDao dao = openWritableDb().getDownloadBeanDao();
        return dao.insert(downloadBean);
    }

    /**
     * 删全部
     */
    public void deleteAll() {
        DownloadBeanDao dao = openWritableDb().getDownloadBeanDao();
        dao.deleteAll();
    }

    /**
     * 根据Url 删除数据
     */
    public void deleteByUrl(String url){
        DownloadBeanDao dao = openWritableDb().getDownloadBeanDao();
        DeleteQuery<DownloadBean> db =dao.queryBuilder()
                .where(DownloadBeanDao.Properties.Url.eq(url))
                .buildDelete();
        db.executeDeleteWithoutDetachingEntities();
    }

    /**
     * 根据Url 查询对象
     * @param url
     * @return
     */
    public DownloadBean qureByUrl(String url){
        DownloadBeanDao dao= openReadAbleDb().getDownloadBeanDao();
        DownloadBean bean = dao.queryBuilder()
                .where(DownloadBeanDao.Properties.Url.eq(url))
                .unique();
        return bean;
    }

    /**
     * 查询全部
     */
    public List<DownloadBean> qureAll(){
        DownloadBeanDao dao = openReadAbleDb().getDownloadBeanDao();

        return dao.loadAll();
    }

    /**
     * 查询全部 已下载
     */
    public List<DownloadBean> qureAllDownloaded(){
        DownloadBeanDao dao = openReadAbleDb().getDownloadBeanDao();
        Query<DownloadBean> query =dao.queryBuilder()
                .where(DownloadBeanDao.Properties.Status.eq(STATUS_SUCCESS))
                .orderDesc(DownloadBeanDao.Properties.StartTime)
                .build();
        return query.list();
    }

    /**
     * 查询全部 正在下载
     */
    public List<DownloadBean> qureAllDownloading(){
        DownloadBeanDao dao = openReadAbleDb().getDownloadBeanDao();
        Query<DownloadBean> query =dao.queryBuilder()
                .where(DownloadBeanDao.Properties.Status.notEq(STATUS_SUCCESS))
                .orderDesc(DownloadBeanDao.Properties.StartTime)
                .build();
        return query.list();
    }


    public DownloadBean qureFistWaiting(){
        DownloadBeanDao dao = openReadAbleDb().getDownloadBeanDao();
        DownloadBean bean =dao.queryBuilder()
                .where(DownloadBeanDao.Properties.Status.eq(STATUS_WAITING))
                .orderDesc(DownloadBeanDao.Properties.StartTime).unique();
        return bean;
    }
}
