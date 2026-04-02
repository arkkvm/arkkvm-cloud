package com.ensurebit.ark.kvm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "API response")
public class BaseResult<T> implements Serializable {

    @Schema(description = "Success flag, true for success and false for failure", type = "boolean")
    private Boolean success = true;

    @Schema(description = "Message, only present on errors", type = "string")
    private String message;

    @Schema(description = "Return code: 1 for success, non-1 for failure", type = "int")
    private int code = 0;

    private T data;

    public BaseResult() {
        super();
    }

    public static <T> BaseResult<T> create() {
        return new BaseResult<T>();
    }

    public static <T> BaseResult<T> success(T data) {
        BaseResult<T> result = BaseResult.create();
        result.setSuccess(true);
        result.setCode(1);
        result.setMessage("");
        result.setData(data);
        return result;
    }

    public static <T> BaseResult<T> success() {
        return success(null);
    }

    public static <T> BaseResult<T> fail(String message) {
        BaseResult<T> result = BaseResult.create();
        result.setSuccess(false);
        result.setCode(0);
        result.setMessage(message);
        return result;
    }

    public static <T> BaseResult<T> fail(int code, String message) {
        BaseResult<T> result = BaseResult.create();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
