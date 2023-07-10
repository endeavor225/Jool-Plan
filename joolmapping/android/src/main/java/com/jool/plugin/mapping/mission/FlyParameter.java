package com.jool.plugin.mapping.mission;

import androidx.annotation.NonNull;

import com.jool.plugin.mapping.model.DroneModel;
import com.jool.plugin.mapping.utils.Constants;
import com.jool.plugin.mapping.utils.Utils;


public class FlyParameter {
    private float altitude;
    private float speed;
    private float shootIntervalInSec;

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getShootIntervalInSec() {
        return shootIntervalInSec;
    }

    public void setShootIntervalInSec(float shootIntervalInSec) {
        this.shootIntervalInSec = shootIntervalInSec;
    }

    public FlyParameter(float baseAltitude,double area, DroneModel model){
        setAltitude(baseAltitude);
        if (Utils.isNull(model)){
            return;
        }else {
            switch (model){
                case PHANTOM_4_ADVANCED:
                    computePhantom4Advenced(area,baseAltitude);
                    break;
                default:
                    setSpeed(Constant.FLOAT_NULL);
                    setShootIntervalInSec(Constant.FLOAT_NULL);
                    break;
            }
        }
    }

    private void computePhantom4Advenced(double area, float baseAltitude) {
        if (area > Constants.HECTARE_2){
            if (baseAltitude >= Constants.ALTITUDE_100 ){
                setSpeed(12f);
                setShootIntervalInSec(2.575f);
            }else {
                if (baseAltitude >= Constants.ALTITUDE_90){
                    setSpeed(11f);
                    setShootIntervalInSec(2.422f);
                }else {
                    if (baseAltitude >= Constants.ALTITUDE_80){
                        setSpeed(9f);
                        setShootIntervalInSec(2.406f);
                    }else {
                        if (baseAltitude >= Constants.ALTITUDE_70){
                            setSpeed(8f);
                            setShootIntervalInSec(2.232f);
                        }else {
                            if (baseAltitude >= Constants.ALTITUDE_60){
                                setSpeed(7f);
                                setShootIntervalInSec(2.075f);
                            }else {
                                if (baseAltitude >= Constants.ALTITUDE_50){
                                    setSpeed(6f);
                                    setShootIntervalInSec(2.136f);
                                }
                            }
                        }
                    }
                }
            }
            return;
        }else {
            if (area > Constants.HECTARE_1){
                if (baseAltitude >= Constants.ALTITUDE_100 ){
                    setSpeed(10f);
                    setShootIntervalInSec(9f);
                }else {
                    if (baseAltitude >= Constants.ALTITUDE_90){
                        setSpeed(11f);
                        setShootIntervalInSec(9.842f);
                    }else {
                        if (baseAltitude >= Constants.ALTITUDE_80){
                            setSpeed(9f);
                            setShootIntervalInSec(8f);
                        }else {
                            if (baseAltitude >= Constants.ALTITUDE_70){
                                setSpeed(8f);
                                setShootIntervalInSec(7f);
                            }else {
                                if (baseAltitude >= Constants.ALTITUDE_60){
                                    setSpeed(7f);
                                    setShootIntervalInSec(6f);
                                }else {
                                    if (baseAltitude >= Constants.ALTITUDE_50){
                                        setSpeed(6f);
                                        setShootIntervalInSec(4.464f);
                                    }
                                }
                            }
                        }
                    }
                }
                return;
            }else {
                if (area > Constants.HECTARE_0){
                    // For All
                    setSpeed(3f);
                    if (baseAltitude >= Constants.ALTITUDE_100 ){
                        // Already Set
                        setShootIntervalInSec(3.125f);
                    }else {
                        if (baseAltitude >= Constants.ALTITUDE_90){
                            // Already Set
                            setShootIntervalInSec(2.5f);
                        }else {
                            if (baseAltitude >= Constants.ALTITUDE_80){
                                // Already Set
                                setShootIntervalInSec(2.375f);
                            }else {
                                if (baseAltitude >= Constants.ALTITUDE_70){
                                    // Already Set
                                    setShootIntervalInSec(2.937f);
                                }else {
                                    if (baseAltitude >= Constants.ALTITUDE_60){
                                        // Already Set
                                        setShootIntervalInSec(2.812f);
                                    }else {
                                        if (baseAltitude >= Constants.ALTITUDE_50){
                                            // Already Set
                                            setShootIntervalInSec(2.125f);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return;
            }
        }
    }

    public boolean isValid(){
        if (getAltitude()== Constant.FLOAT_NULL
                || getAltitude()== Constant.FLOAT_NULL_2
                || getAltitude() > Constant.MAX_ALTITUDE_IN_CI
                || getAltitude() < Constant.MIN_ALTITUDE_IN_CI){
            toLog("isValid getAltitude() NOT VALID");
            return false;
        }else {
            if (getSpeed()  == Constant.FLOAT_NULL
                    || getSpeed() == Constant.FLOAT_NULL_2){
                toLog("isValid getSpeed() NOT VALID");
                return false;
            }else {
                if (getShootIntervalInSec() == Constant.FLOAT_NULL
                        || getShootIntervalInSec() == Constant.FLOAT_NULL_2 ){
                    toLog("isValid getShootIntervalInSec() NOT VALID");
                    return false;
                }else {
                    return true;
                }
            }
        }
    }

    private void toLog( @NonNull String msg){
        Utils.toLog(getClass().getSimpleName(), msg);
    }

}
