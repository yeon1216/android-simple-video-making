package com.example.changeapp.object;

public class Member {

    public int member_no;
    public String member_id;
    public String member_pw;

    public Member() {
    }

    public Member(int member_no, String member_id, String member_pw) {
        this.member_no = member_no;
        this.member_id = member_id;
        this.member_pw = member_pw;
    }

    public void setMember_no(int member_no) {
        this.member_no = member_no;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public void setMember_pw(String member_pw) {
        this.member_pw = member_pw;
    }

    public int getMember_no() {
        return member_no;
    }

    public String getMember_id() {
        return member_id;
    }

    public String getMember_pw() {
        return member_pw;
    }
}
