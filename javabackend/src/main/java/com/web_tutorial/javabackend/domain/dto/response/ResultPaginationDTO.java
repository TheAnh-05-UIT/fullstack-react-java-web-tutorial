package com.web_tutorial.javabackend.domain.dto.response;

import java.util.List;

public class ResultPaginationDTO {
    private Object meta;
    private Object result;

    public ResultPaginationDTO() {
    }

    public static class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;

        public Meta() {
        }

        public Meta(int page, int pageSize, int pages, long total) {
            this.page = page;
            this.pageSize = pageSize;
            this.pages = pages;
            this.total = total;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }

        public int getPages() { return pages; }
        public void setPages(int pages) { this.pages = pages; }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }

    // Để tương thích trực tiếp với FE đang dùng response.content và response.totalPages
    private List<?> content;
    private int totalPages;
    private long totalElements;
    private int pageNumber;
    private int pageSize;

    public ResultPaginationDTO(List<?> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.result = content;
        this.meta = new Meta(pageNumber + 1, pageSize, totalPages, totalElements);
    }

    public List<?> getContent() { return content; }
    public void setContent(List<?> content) { 
        this.content = content; 
        this.result = content;
    }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public Object getMeta() { return meta; }
    public void setMeta(Object meta) { this.meta = meta; }

    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
}
