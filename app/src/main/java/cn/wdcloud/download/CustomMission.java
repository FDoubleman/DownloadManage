package cn.wdcloud.download;


public class CustomMission {
    private String img = "";
    private String introduce = "";
    private String url = "";

    public CustomMission( String url, String introduce, String img) {

        this.introduce = introduce;
        this.img = img;
        this.url =url;
    }

    public CustomMission( String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
