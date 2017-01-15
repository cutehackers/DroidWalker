
package com.jhlee.android.droidwalker.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "total",
    "userquery",
    "items"
})
public class Result {

    @JsonProperty("total")
    private Integer total;
    @JsonProperty("userquery")
    private String userquery;
    @JsonProperty("items")
    private List<Item> items = null;

    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    @JsonProperty("userquery")
    public String getUserquery() {
        return userquery;
    }

    @JsonProperty("userquery")
    public void setUserquery(String userquery) {
        this.userquery = userquery;
    }

    @JsonProperty("items")
    public List<Item> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<Item> items) {
        this.items = items;
    }

}
