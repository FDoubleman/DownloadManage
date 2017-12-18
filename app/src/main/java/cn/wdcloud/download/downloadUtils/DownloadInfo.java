package cn.wdcloud.download.downloadUtils;

import java.io.Serializable;

/**
 * Created by fmm on 2017/12/18.
 */

public class DownloadInfo implements Serializable {

    private String url;//下载地址
    private long totalSize;//下载大小
    private long currentSize;//已下载大小
    private int progress;//进度
    private int status;//下载状态
    private String filePath;//文件路径
    private String fileName;//文件名称

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
