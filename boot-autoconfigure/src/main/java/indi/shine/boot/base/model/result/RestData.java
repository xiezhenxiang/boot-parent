package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RestData<T> {

    private List<T> rsData;
    private Long rsCount;

    public RestData() {
    }

    public RestData(List<T> rsData, Integer count) {
        this(rsData, (long)count);
    }

    public RestData(List<T> rsData, Long count) {
        this.rsData = rsData;
        this.rsCount = count;
    }
}
