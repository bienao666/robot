package com.bienao.robot.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sedwt
 */
@Data
public class Result {
	/**
	 * 响应代码
	 */
	private String code;

	/**
	 * 响应消息
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String message;

	/**
	 * 响应结果
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Object data;

	public Result() {
	}

	public Result(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	/**
	 *
	 * @return result
	 */
	public static Result success() {
		Result rb = new Result();
		rb.setCode("200");
		rb.setMessage("成功");
		rb.setData(null);
		return rb;
	}

	/**
	 * @param data 返回的数据
	 * @return result
	 */
	public static Result success(Object data) {
		Result rb = new Result();
		rb.setCode("200");
		rb.setMessage("成功");
		rb.setData(data);
		return rb;
	}


	/**
	 *
	 * @param code 错误码
	 * @param message 失败信息
	 * @return result
	 */
	public static Result error(String code, String message) {
		Result rb = new Result();
		rb.setCode(code);
		rb.setMessage(message);
		rb.setData(null);
		return rb;
	}

	/**
	 *
	 * @param code 错误码
	 * @param message 错误信息
	 * @param data 返回的数据
	 * @return result
	 */
	public static Result error(String code, String message,Object data) {
		Result rb = new Result();
		rb.setCode(code);
		rb.setMessage(message);
		rb.setData(data);
		return rb;
	}

}
