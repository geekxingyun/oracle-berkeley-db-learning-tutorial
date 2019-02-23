package com.xingyun.exception;

import com.sleepycat.db.Environment;
import com.sleepycat.db.ErrorHandler;

/**
 * BDB XML 文档编写
 * **/
public class MyErrorHandler implements ErrorHandler {

	/**
	 * 自定义拦截异常
	 * */
	public void error(Environment env, String errpfx, String msg) {
		System.err.println(env+":"+errpfx + " : " + msg);
	}

}
