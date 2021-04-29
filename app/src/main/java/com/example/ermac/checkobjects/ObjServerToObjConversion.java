package com.example.ermac.checkobjects;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

public class ObjServerToObjConversion {

    public static List<Obj> parse(ServerObj... serverObjs){
        return Stream.of(serverObjs).map(
                so -> new Obj(so.getName(), Stream.of(so.getParts()).map(Obj::new).toArray(Obj[]::new))
        ).collect(Collectors.toList());
    }

}
