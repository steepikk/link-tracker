package backend.academy.common.dto;

import java.util.List;

public class ApiErrorResponse {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
    private List<String> stacktrace;

    public ApiErrorResponse(
            String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {
        this.description = description;
        this.code = code;
        this.exceptionName = exceptionName;
        this.exceptionMessage = exceptionMessage;
        this.stacktrace = stacktrace;
    }

    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public List<String> getStacktrace() {
        return stacktrace;
    }
}
