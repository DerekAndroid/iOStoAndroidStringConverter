package com.kered.demosample;


public class DKLog {
	public static final boolean isAppendLog = false;
	
	public static void v(String tag, String msg) {
		System.out.println(String.format("v:%s\t\t%s", tag, msg));
	}

	public static void d(String tag, String msg) {
		System.out.println(String.format("d:%s\t\t%s", tag, msg));
	}

	public static void i(String tag, String msg) {
		System.out.println(String.format("i:%s\t\t%s", tag, msg));	

	}

	public static void w(String tag, String msg) {
		System.out.println(String.format("w:%s\t\t%s", tag, msg));
	}

	public static void e(String tag, String msg) {
		System.out.println(String.format("e:%s\t\t%s", tag, msg));
	}


}
