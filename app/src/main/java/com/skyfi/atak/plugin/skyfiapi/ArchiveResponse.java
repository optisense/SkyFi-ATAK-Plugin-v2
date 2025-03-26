package com.skyfi.atak.plugin.skyfiapi;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class ArchiveResponse {
    private ArchivesRequest request;
    private ArrayList<Archive> archives;
    private Archive archive;
    private String nextPage;
    private Integer total;

    @NonNull
    @Override
    public String toString() {
        return "ArchiveResponse{" +
                "archive=" + archive +
                ", request=" + request +
                ", archives=" + archives +
                ", nextPage='" + nextPage + '\'' +
                ", total=" + total +
                '}';
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public ArrayList<Archive> getArchives() {
        return archives;
    }

    public void setArchives(ArrayList<Archive> archives) {
        this.archives = archives;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public ArchivesRequest getRequest() {
        return request;
    }

    public void setRequest(ArchivesRequest request) {
        this.request = request;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
