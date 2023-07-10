package com.jool.plugin.mapping.mission;

import com.jool.plugin.mapping.utils.Utils;

import java.util.concurrent.CopyOnWriteArrayList;

import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.triggers.Trigger;

public class Container {
    private TimelineElement timelineElement;
    private CopyOnWriteArrayList<Trigger> reachList;

    public TimelineElement getTimelineElement() {
        return timelineElement;
    }

    public void setTimelineElement(TimelineElement timelineElement) {
        this.timelineElement = timelineElement;
    }

    public CopyOnWriteArrayList<Trigger> getReachList() {
        return reachList;
    }

    public void setReachList(CopyOnWriteArrayList<Trigger> reachList) {
        this.reachList = reachList;
    }

    public boolean isValid(){
        if (Utils.isNull(getReachList())){
            return false;
        }else {
            if (Utils.isNull(getTimelineElement())){
                return false;
            }else {
                return true;
            }
        }
    }
}
