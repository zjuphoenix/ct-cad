package com.zju.lab.ct.cache;

/**
 * @author wuhaitao
 * @date 2016/4/21 22:24
 */
public class CTPageKey {

    private int pageIndex;
    private int pageSize;
    private int recordId;

    public CTPageKey() {
    }

    public CTPageKey(int pageIndex, int pageSize, int recordId) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.recordId = recordId;
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

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CTPageKey)) return false;

        CTPageKey ctPageKey = (CTPageKey) o;

        if (getPageIndex() != ctPageKey.getPageIndex()) return false;
        if (getPageSize() != ctPageKey.getPageSize()) return false;
        return getRecordId() == ctPageKey.getRecordId();

    }

    @Override
    public int hashCode() {
        int result = getPageIndex();
        result = 31 * result + getPageSize();
        result = 31 * result + getRecordId();
        return result;
    }
}
