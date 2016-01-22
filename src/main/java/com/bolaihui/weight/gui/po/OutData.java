package com.bolaihui.weight.gui.po;

/**
 * Created by fz on 2016/1/20.
 */
public class OutData {

    private String emsNo;

    private String status;

    private String operator;

    private String operateTime;

    public OutData() {}

    public OutData(String emsNo, String status, String operator, String operateTime) {
        this.emsNo = emsNo;
        this.status = status;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public String getEmsNo() {
        return emsNo;
    }

    public void setEmsNo(String emsNo) {
        this.emsNo = emsNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
