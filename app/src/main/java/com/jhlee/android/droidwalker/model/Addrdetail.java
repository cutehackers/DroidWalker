
package com.jhlee.android.droidwalker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "country",
    "sido",
    "sigugun",
    "dongmyun",
    "rest"
})
public class Addrdetail {

    @JsonProperty("country")
    private String country;
    @JsonProperty("sido")
    private String sido;
    @JsonProperty("sigugun")
    private String sigugun;
    @JsonProperty("dongmyun")
    private String dongmyun;
    @JsonProperty("rest")
    private String rest;

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("sido")
    public String getSido() {
        return sido;
    }

    @JsonProperty("sido")
    public void setSido(String sido) {
        this.sido = sido;
    }

    @JsonProperty("sigugun")
    public String getSigugun() {
        return sigugun;
    }

    @JsonProperty("sigugun")
    public void setSigugun(String sigugun) {
        this.sigugun = sigugun;
    }

    @JsonProperty("dongmyun")
    public String getDongmyun() {
        return dongmyun;
    }

    @JsonProperty("dongmyun")
    public void setDongmyun(String dongmyun) {
        this.dongmyun = dongmyun;
    }

    @JsonProperty("rest")
    public String getRest() {
        return rest;
    }

    @JsonProperty("rest")
    public void setRest(String rest) {
        this.rest = rest;
    }

}
