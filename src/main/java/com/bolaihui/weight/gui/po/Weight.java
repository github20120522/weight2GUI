package com.bolaihui.weight.gui.po;

/**
 * Created by fz on 2016/1/20.
 */
public class Weight {

    private String emsNo;

    private String weight;

    private String boxNo;

    private String operator;

    private String operateTime;

    private String status;

    public Weight() {}

    public Weight(String emsNo, String weight, String boxNo, String operator, String operateTime, String status) {
        this.emsNo = emsNo;
        this.weight = weight;
        this.boxNo = boxNo;
        this.operator = operator;
        this.operateTime = operateTime;
        this.status = status;
    }

    public String getEmsNo() {
        return emsNo;
    }

    public void setEmsNo(String emsNo) {
        this.emsNo = emsNo;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
