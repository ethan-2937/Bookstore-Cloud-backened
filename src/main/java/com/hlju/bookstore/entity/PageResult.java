package com.hlju.bookstore.entity;

import com.github.pagehelper.PageInfo;

import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public PageResult() {}

    public static <T> PageResult<T> from(PageInfo<T> pageInfo) {
        PageResult<T> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setHasPreviousPage(pageInfo.isHasPreviousPage());
        result.setHasNextPage(pageInfo.isHasNextPage());
        return result;
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
    public boolean isHasPreviousPage() { return hasPreviousPage; }
    public void setHasPreviousPage(boolean hasPreviousPage) { this.hasPreviousPage = hasPreviousPage; }
    public boolean isHasNextPage() { return hasNextPage; }
    public void setHasNextPage(boolean hasNextPage) { this.hasNextPage = hasNextPage; }
}
