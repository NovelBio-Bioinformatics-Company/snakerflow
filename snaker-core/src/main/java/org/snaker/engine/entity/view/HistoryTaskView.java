package org.snaker.engine.entity.view;

public class HistoryTaskView {

	 /**
     * 任务处理者ID
     */
    private String operator;
    
    /**
     * 任务审核结果.
     * @link SnakerEngine 0-不同意,1-同意.
     */
    private String auditRes;
    
    /**
     * 审批意见.
     */
    private String auditOpinion;
    /**
     * 任务创建时间
     */
    private String createTime;
    /**
     * 任务完成时间
     */
    private String finishTime;
    
    
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getAuditRes() {
		return auditRes;
	}
	public void setAuditRes(String auditRes) {
		if ("1".equals(auditRes)) {
			this.auditRes = "同意";
		} else {
			this.auditRes = "不同意";
		}
	}
	
	public String getAuditOpinion() {
		return auditOpinion;
	}
	public void setAuditOpinion(String auditOpinion) {
		this.auditOpinion = auditOpinion;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
    
    
}
