package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestData<T> {
    private List<T> rsData;
    private Long rsCount;

    public RestData() {
    }

    public RestData(List<T> rsData, Integer count) {
        this(rsData, (long)count.intValue());
    }

    public RestData(List<T> rsData, Long count) {
        this.rsData = rsData;
        this.rsCount = count;
    }

    public List<T> getRsData() {
        return this.rsData;
    }

    public void setRsData(List<T> rsData) {
        this.rsData = rsData;
    }

    public Long getRsCount() {
        return this.rsCount;
    }

    public void setRsCount(Long rsCount) {
        this.rsCount = rsCount;
    }
}
