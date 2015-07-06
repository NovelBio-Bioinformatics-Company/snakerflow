package org.snaker.engine;

public interface IPosition {

	/**
	 * 根据职位编码,找到该职位的所有employee的Id.
	 * @param fnumbers
	 * @return
	 */
	public String[] findUserIdsByPositonFnumbers(String[] fnumbers);
}
