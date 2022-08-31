package com.bienao.robot.exception;

/**
 * @author sedwt
 */
public interface BaseErrorInfoInterface {
	/**
	 * 错误码
	 * @return 错误码
	 */
	 String getResultCode();

	/**
	 * 错误描述
	 * @return 错误描述
	 */
	 String getResultMsg();

	/**
	 * 错误详情
	 * @return 错误详情
	 */
	 Object getData();
}