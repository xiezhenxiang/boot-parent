package indi.shine.boot.base.model;

import lombok.Data;

@Data
public class ValidationErrorBean {

    private String message;
    private String path;
    private String invalidValue;
}
