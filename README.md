# DownloadManage
RxJava和OkHttp 封装的下载控制管理
一、功能
    1、多任务下载、取消、暂停
    2、设置下载最大数量
    3、等待下载，主动下载
    4、引入数据库
二、实现过程所遇问题：

    1、下载功能实现了，如何暂停下载？和取消下载一样吗？
        call.cancal();
        这个方法会调用 ---> DownLoadObserver onError();
        如何处理 是暂停下载 还是 取消下载？

    2、如何动态修改 DownLoadObserver 中的 DownloadInfo

    3、数据库 在下载功能使用位置？
        添加下载---->插入
        开始下载---->更新状态
        下载中 ----->更新进度
        暂停、重新下载--->更新状态

    4、马丹 数据库写好了  使用
     DBInterface.getInstance().insertOrUpdate(downloadbean);
     不管调用几次只能 插入一条。
     想了半天  原来是数据库 使用问题：
      @Id(autoincrement = true)
         private Long id;  ------->是大写 不是小写的   private Long id;

三、