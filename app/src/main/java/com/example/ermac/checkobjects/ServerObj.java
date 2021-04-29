package com.example.ermac.checkobjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServerObj {

    @SerializedName("Name")
    @Expose
    private String name;

    @SerializedName("Parts")
    @Expose
    private List<String> parts = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }

}
