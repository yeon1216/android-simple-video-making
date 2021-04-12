package com.example.changeapp;

//네이버 api -> response받을 class -> 얼굴의 pose 정면 추출, confidence
public class Face {
    Pose pose;

    public Pose getPose() {
        return pose;
    }

    public class Pose {
        public String value;

        public String getValue() {
            return value;
        }

    }
}