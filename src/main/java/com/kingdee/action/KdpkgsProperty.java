package com.kingdee.action;

public class KdpkgsProperty {
    private String kdpkgID;
    private String sourcePath;
    private String md5;

    public KdpkgsProperty(String kdpkgID, String sourcePath, String md5) {
        this.kdpkgID = kdpkgID;
        this.sourcePath = sourcePath;
        this.md5 = md5;
    }

    public String getKdpkgID() {
        return this.kdpkgID;
    }

    public String getSourcePath() {
        return this.sourcePath;
    }

    public String getMd5() {
        return this.md5;
    }
}
