package com.zju.lab.ct.cache;

/**
 * Created by wuhaitao on 2016/4/18.
 */
public class RecordPageKey {
    private int pageIndex;
    private int pageSize;
    private String username;

    public RecordPageKey() {
    }

    public RecordPageKey(int pageIndex, int pageSize, String username) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.username = username;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordPageKey)) return false;

        RecordPageKey recordPageKey = (RecordPageKey) o;

        if (getPageIndex() != recordPageKey.getPageIndex()) return false;
        if (getPageSize() != recordPageKey.getPageSize()) return false;
        return getUsername() != null ? getUsername().equals(recordPageKey.getUsername()) : recordPageKey.getUsername() == null;

    }

    @Override
    public int hashCode() {
        int result = getPageIndex();
        result = 31 * result + getPageSize();
        result = 31 * result + (getUsername() != null ? getUsername().hashCode() : 0);
        return result;
    }
}
