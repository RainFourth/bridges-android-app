package com.example.ermac.checkobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Obj {
    private String name;
    private List<Obj> parts;

    public Obj(String name, Obj... parts){
        if (parts==null) this.name = name;
        else {
            this.name = name;
            this.parts = new ArrayList<>();
            Collections.addAll(this.parts, parts);
        }
    }

    public Obj(String name) {
        this.name = name;
        parts = null;
    }

    public String getName() {
        return name;
    }

    public List<Obj> getParts() {
        return parts;
    }
}
