/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI shared information
 */
public class MISignal {

	String signal = "";
	boolean stop;
	boolean print;
	boolean pass;
	String description = "";

	public MISignal (String name, boolean stp, boolean prnt, boolean ps, String desc) {
		signal = name;
		stop = stp;
		print = prnt;
		pass = ps;
		description = desc;
	}

	public String getName() {
		return signal;
	}

	public boolean isStop() {
		return stop;
	}

	public boolean isPrint() {
		return print;
	}

	public boolean isPass() {
		return pass;
	}

	public String getDescription() {
		return description;
	}

}
