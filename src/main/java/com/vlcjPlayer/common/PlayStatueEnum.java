package com.vlcjPlayer.common;


public enum  PlayStatueEnum {
    LOADING("libvlc_Opening","加载中..."),
    PLAYING("libvlc_Playing","播放中..."),
    PAUSEIING("libvlc_Paused","暂停中..."),
    STOPING("libvlc_Stopped","停止中..."),
    UNKNOWN("","");

    private String status;

    private String describe;

    PlayStatueEnum(String status, String describe) {
        this.status = status;
        this.describe = describe;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public static PlayStatueEnum getEnumStatus(String value){
        for(PlayStatueEnum a : PlayStatueEnum.values()){
            if(value.equals(a.getStatus()) ){
                return a;
            }
        }
        return UNKNOWN;
    }
}
