package org.eclipse.cdt.debug.mi.core;

/**
 * @author alain
 *
 */
public class MIBreakpointInfo extends MIInfo {

	int line, number;
	String function, file;

	public MIBreakpointInfo(int no, String func, String filename, int lineno) {
		number = no;
		function = func;
		file = filename;
		line = lineno;
	}

	public int getNumber() {
		return number;
	}

	public String getFunction() {
		return function;
	}

	public String getFile() {
		return file;
	}

	public int getLineNumber() {
		return line;
	}
}
