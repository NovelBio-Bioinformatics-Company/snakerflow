package org.snaker.engine.model;


/**
 * 构造前台easyui的datagrid所需数据的json对象.
 * @author novelbio
 *
 */
public class Datagrid {
	Object rows;
	long total;
	
	/**
	 * 构造前台easyui的datagrid所需数据的json对象.
	 * @param total 数据总条数
	 * @param rows	具体数据
	 */
	public Datagrid(long total,Object rows) {
		this.total = total;
		this.rows = rows;
	}

	public Object getRows() {
		return rows;
	}

	public void setRows(Object rows) {
		this.rows = rows;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}
	
	
}
