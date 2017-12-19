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

三、