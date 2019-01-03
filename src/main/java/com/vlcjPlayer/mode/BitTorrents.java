package com.vlcjPlayer.mode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class BitTorrents {
    public static BitTorrentInfo parse(File btFile) throws Exception {
        return new BitTorrents().analyze(new FileInputStream(btFile));
    }

    public static BitTorrentInfo parse(String btFilePath) throws Exception {
        return new BitTorrents().analyze(new FileInputStream(btFilePath));
    }

    private BitTorrentInfo analyze(InputStream is) throws Exception {
        BitTorrentInfo btInfo = new BitTorrentInfo();
        String key = null;
        StringBuilder strLengthBuilder = new StringBuilder();
        int tempByte;
        while ((tempByte = is.read()) != -1) {
            char temp = (char) tempByte;
            switch (temp) {
                case 'i':
                    StringBuilder itempBuilder = new StringBuilder();
                    char iTemp;
                    while ((iTemp = (char) is.read()) != 'e') {
                        itempBuilder.append(iTemp);
                    }
                    btInfo.setValue(key, itempBuilder.toString());
                    break;
                case '0': case '1': case  '2': case '3': case '4': case  '5': case '6': case '7': case '8': case '9':
                    strLengthBuilder.append(temp);
                    break;
                case ':':
                    int strLen = Integer.parseInt(strLengthBuilder.toString());
                    strLengthBuilder = new StringBuilder();
                    byte[] tempBytes = new byte[strLen];
                    is.read(tempBytes);
                    if (key != null && key.equals("pieces")) {
                        btInfo.setValue(key, tempBytes);
                    } else {
                        String tempStr = new String(tempBytes);
                        if (BitTorrentInfo.keyList.contains(tempStr)) {
                            key = tempStr;
                            if (tempStr.equals("announce-list")) {
                                btInfo.setAnnounceList(new LinkedList<String>());
                            } else if (tempStr.equals("info")) {
                                btInfo.setInfo(new Info());
                            } else if (tempStr.equals("files")) {
                                btInfo.getInfo().setFiles(new LinkedList<Files>());
                                btInfo.getInfo().getFiles().add(new Files());
                            } else if (tempStr.equals("length")) {
                                List<Files> tempFiles = btInfo.getInfo().getFiles();
                                if (tempFiles != null) {
                                    if (tempFiles.isEmpty() || tempFiles.get(tempFiles.size() - 1).getLength() != 0) {
                                        tempFiles.add(new Files());
                                    }
                                }
                            } else if (tempStr.equals("md5sum")) {
                                List<Files> tempFiles = btInfo.getInfo().getFiles();
                                if (tempFiles != null) {
                                    if (tempFiles.isEmpty() || tempFiles.get(tempFiles.size() - 1).getMd5sum() != null) {
                                        tempFiles.add(new Files());
                                    }
                                }
                            } else if (tempStr.equals("path")) {
                                List<Files> tempFilesList = btInfo.getInfo().getFiles();
                                if (tempFilesList.isEmpty()) {
                                    Files files = new Files();
                                    files.setPath(new LinkedList<String>());
                                    tempFilesList.add(files);
                                } else {
                                    Files files = tempFilesList.get(tempFilesList.size() - 1);
                                    if (files.getPath() == null) {
                                        files.setPath(new LinkedList<String>());
                                    }
                                }
                            }
                        } else {
                            btInfo.setValue(key, tempStr);
                        }
                    }
                    break;
            }
        }
        return btInfo;
    }

    public static void main(String[] args) throws Exception {
        BitTorrentInfo info=parse("D:\\java_project\\VlcjPlayer\\src\\main\\resources\\test.torrent");
        System.out.println("信息:"+info.getAnnounce()+"\t"+info.getComment()+"\t"+info.getCreateBy());
        Info it=info.getInfo();
        System.out.println("信息:"+it.getName()+"\t"+it.getPiecesLength()+"\t"+it.getLength()+"\t"+it.getMd5sum()+"\t"+it.getPieces());
        if(info.getAnnounceList().size()>0){
            for(String str:info.getAnnounceList()){
                System.out.println("信息2:"+str);
            }
        }
        if(it.getFiles().size()>0){
            for(Files file: it.getFiles()){
                System.out.println("信息3:"+file.getLength()+"\t"+file.getMd5sum());
                if(file.getPath().size()>0){
                    for(String str:file.getPath()){
                        System.out.println("信息4："+str);
                    }
                }
            }
        }
    }
}
