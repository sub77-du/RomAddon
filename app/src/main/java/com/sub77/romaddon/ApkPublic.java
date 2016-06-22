package com.sub77.romaddon;

public class ApkPublic {

    private String apk;
    private int url;
    private long id;
    private boolean checked;

    public ApkPublic(String apk, int url, long id, boolean checked) {
        this.apk = apk;
        this.url = url;
        this.id = id;
        this.checked = checked;
    }

    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }


    public int getUrl() {
        return url;
    }

    public void setUrl(int url) {
        this.url = url;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked (boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        String output = url + " x " + apk;

        return output;
    }
}