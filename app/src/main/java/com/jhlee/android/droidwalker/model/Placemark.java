
package com.jhlee.android.droidwalker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "result",
    "error_code",
    "message"
})
public class Placemark {

    @JsonProperty("result")
    private Result result;


    @JsonProperty("error_code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("result")
    public Result getResult() {
        return result;
    }

    @JsonProperty("result")
    public void setResult(Result result) {
        this.result = result;
    }


    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("error_code")
    public int getCode() {
        return this.code;
    }

    @JsonProperty("error_code")
    public void setCode(int code) {
        this.code = code;
    }
}
