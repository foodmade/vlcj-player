package com.vlcjPlayer.mode;

import com.vlcjPlayer.common.StringUtil;

import java.util.List;
import java.util.Arrays;

public class BitTorrentInfo {
    public static List<String> keyList;

    static {
        String[] keys = {"announce", "announce-list", "creation date", "comment", "created by",
                "info", "length", "md5sum", "name", "piece length", "pieces", "files", "path"};
        keyList = Arrays.asList(keys);
    }

    private String announce;
    private List<String> announceList;
    private long creationDate;
    private String comment;
    private String createBy;
    private Info info;

    public BitTorrentInfo() {
    }

    public BitTorrentInfo(String announce, List<String> announceList, long creationDate, String comment,
                          String createBy, Info info) {
        super();
        this.announce = announce;
        this.announceList = announceList;
        this.creationDate = creationDate;
        this.comment = comment;
        this.createBy = createBy;
        this.info = info;
    }

    public static List<String> getKeyList() {
        return keyList;
    }

    public static void setKeyList(List<String> keyList) {
        BitTorrentInfo.keyList = keyList;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public List<String> getAnnounceList() {
        return announceList;
    }

    public void setAnnounceList(List<String> announceList) {
        this.announceList = announceList;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public void setValue(String key, Object value) throws Exception {
        if (!keyList.contains(key)) {
            throw new Exception("not contains this key: " + key);
        } else {
            switch (key) {
                case "announce":
                    this.setAnnounce(value.toString());
                    break;
                case "announce-list":
                    this.getAnnounceList().add(value.toString());
                    break;
                case "creation date":
                    if (StringUtil.isNumeric(value.toString())) {
                        this.setCreationDate(Long.parseLong(value.toString()));
                    } else {
                        this.setCreationDate(0);
                    }
                    break;
                case "comment":
                    this.setComment(value.toString());
                    break;
                case "created by":
                    this.setCreateBy(value.toString());
                    break;
                case "length":
                    List<Files> filesList1 = this.getInfo().getFiles();
                    if (filesList1 != null) {
                        Files files = this.getInfo().getFiles().get(filesList1.size() - 1);
                        files.setLength(Long.parseLong(value.toString()));
                    } else {
                        this.getInfo().setLength(Long.parseLong(value.toString()));
                    }
                    break;
                case "md5sum":
                    List<Files> filesList2 = this.getInfo().getFiles();
                    if (filesList2 != null) {
                        Files files = this.getInfo().getFiles().get(filesList2.size() - 1);
                        files.setMd5sum(value.toString());
                    } else {
                        this.getInfo().setMd5sum(value.toString());
                    }
                    break;
                case "name":
                    this.getInfo().setName(value.toString());
                    break;
                case "piece length":
                    this.getInfo().setPiecesLength(Long.parseLong(value.toString()));
                    break;
                case "pieces":
                    if (StringUtil.isNumeric(value.toString())) {
                        this.getInfo().setPieces(null);
                    } else {
                        this.getInfo().setPieces((byte[]) value);
                    }
                    break;
                case "path":
                    List<Files> filesList3 = this.getInfo().getFiles();
                    Files files3 = filesList3.get(filesList3.size() - 1);
                    files3.getPath().add(value.toString());
                    break;
            }
        }
    }
}
