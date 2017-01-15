
package com.jhlee.android.droidwalker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "address",
    "addrdetail",
    "isRoadAddress",
    "point"
})
public class Item {

    @JsonProperty("address")
    private String address;
    @JsonProperty("addrdetail")
    private Addrdetail addrdetail;
    @JsonProperty("isRoadAddress")
    private Boolean isRoadAddress;
    @JsonProperty("point")
    private Point point;

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("addrdetail")
    public Addrdetail getAddrdetail() {
        return addrdetail;
    }

    @JsonProperty("addrdetail")
    public void setAddrdetail(Addrdetail addrdetail) {
        this.addrdetail = addrdetail;
    }

    @JsonProperty("isRoadAddress")
    public Boolean getIsRoadAddress() {
        return isRoadAddress;
    }

    @JsonProperty("isRoadAddress")
    public void setIsRoadAddress(Boolean isRoadAddress) {
        this.isRoadAddress = isRoadAddress;
    }

    @JsonProperty("point")
    public Point getPoint() {
        return point;
    }

    @JsonProperty("point")
    public void setPoint(Point point) {
        this.point = point;
    }

}
