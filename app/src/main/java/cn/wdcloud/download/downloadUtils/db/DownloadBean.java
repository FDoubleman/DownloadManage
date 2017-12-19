package cn.wdcloud.download.downloadUtils.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by fmm on 2017/12/19.
 */
@Entity
public class DownloadBean {
    @Id(autoincrement = true)
    private Long id;
    private String url;//下载地址
    private Long totalSize =0L;//下载大小
    private Long currentSize=0L;//已下载大小
    private int progress=0;//进度
    private int status ;//下载状态
    private String filePath;//本地文件路径
    private String fileName;//文件名称
    private String startTime;//开始时间
    @Generated(hash = 13571938)
    public DownloadBean(Long id, String url, Long totalSize, Long currentSize,
            int progress, int status, String filePath, String fileName,
            String startTime) {
        this.id = id;
        this.url = url;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.progress = progress;
        this.status = status;
        this.filePath = filePath;
        this.fileName = fileName;
        this.startTime = startTime;
    }
    @Generated(hash = 2040406903)
    public DownloadBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Long getTotalSize() {
        return this.totalSize;
    }
    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }
    public Long getCurrentSize() {
        return this.currentSize;
    }
    public void setCurrentSize(Long currentSize) {
        this.currentSize = currentSize;
    }
    public int getProgress() {
        return this.progress;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getFilePath() {
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getStartTime() {
        return this.startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
}
