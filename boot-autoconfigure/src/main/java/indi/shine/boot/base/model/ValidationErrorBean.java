package indi.shine.boot.base.model;

public class ValidationErrorBean {
    private String message;
    private String path;
    private String invalidValue;

    public ValidationErrorBean() {
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInvalidValue() {
        return this.invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }
}
