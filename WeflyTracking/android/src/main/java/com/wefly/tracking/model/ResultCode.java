package com.wefly.tracking.model;

/**
 * Created by admin on 28/09/2018.
 */

public enum ResultCode {
    PRECISED_POINT_NOT_FOUND(1),
    GET_CURRENT_LOCATION(2),
    SUSCRIBE_LOCATION_UPDATE(3),
    DESUSCRIBE_LOCATION_UPDATE(4),
    DEFAULT(0);

    private int data;

    private ResultCode(int val){
        this.data = val;
    }

    public boolean _equals(int value) {
        return this.data == value;
    }


    public static ResultCode find(int value){
        ResultCode resp = DEFAULT;
        for(int var2 = 0; var2 < values().length; ++var2) {
            if(values()[var2]._equals(value)) {
                resp = values()[var2];
                break;
            }
        }

        return resp;
    }

    public final int toInteger(){
        return this.ordinal() + 1;
    }

}
