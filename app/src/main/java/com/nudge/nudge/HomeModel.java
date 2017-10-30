package com.nudge.nudge;

/**
 * Created by STAR on 10/13/2017.
 */

public class HomeModel {

    private String nickName;
    private String nudgeTime;

    // Set Methods

    public HomeModel (String nickName, String nudgeTime) {
        this.nickName = nickName;
        this.nudgeTime = nudgeTime;
    }

    // Get Methods

    public  String getNickName() { return this.nickName; }

    public  String getNudgeTime() { return this.nudgeTime; }


}
