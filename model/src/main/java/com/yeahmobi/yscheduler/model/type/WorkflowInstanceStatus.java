package com.yeahmobi.yscheduler.model.type;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author atell
 */
public enum WorkflowInstanceStatus {

    INITED(1, "新建"), //
    RUNNING(10, "运行中"), //
    SUCCESS(20, "运行成功"), //
    FAILED(30, "运行失败"), //
    CANCELLED(40, "取消运行"), //
    SKIPPED(50, "被跳过"), //
    COMPLETE_WITH_UNKNOWN_STATUS(60, "未知的结束状态");

    private int    id;
    private String desc;

    WorkflowInstanceStatus(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static WorkflowInstanceStatus valueOf(int id) {
        switch (id) {
            case 1:
                return INITED;
            case 10:
                return RUNNING;
            case 20:
                return SUCCESS;
            case 30:
                return FAILED;
            case 40:
                return CANCELLED;
            case 50:
                return SKIPPED;
            case 60:
                return COMPLETE_WITH_UNKNOWN_STATUS;
        }
        return null;
    }

    public boolean isCompleted() {
        return FAILED.equals(this) || SUCCESS.equals(this) || SKIPPED.equals(this) || CANCELLED.equals(this)
               || COMPLETE_WITH_UNKNOWN_STATUS.equals(this);
    }

    public static List<WorkflowInstanceStatus> getUncompleted() {
        return Lists.asList(INITED, new WorkflowInstanceStatus[] { RUNNING });
    }

    public static List<WorkflowInstanceStatus> getCompleted() {
        return Lists.asList(FAILED, new WorkflowInstanceStatus[] { SUCCESS, CANCELLED, SKIPPED,
                COMPLETE_WITH_UNKNOWN_STATUS });
    }

}
