/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;

/**
 * Contain info about the GDB/MI breakpoint info.
 * -break-insert -t -c 2 main
 * ^done,bkpt={number="1",type="breakpoint",disp="del",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",cond="2",times="0"}
 * (gdb) 
 * -break-insert -h -i 2 main
 * ^done,bkpt={number="2",type="hw breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0",ignore="2"}
 * (gdb) 
 */
public class MIBreakPoint {

	int number;
	String type = "";
	String disp = "";
	boolean enabled;
	long address;
	String func = ""; 
	String file = "";
	int line;
	int times;
	String what = "";
	String cond = "";
	int ignore;

	public MIBreakPoint(MITuple tuple) {
		parse(tuple);
	}

	public int getNumber() {
		return number;
	}

	public String getType() {
		return type;
	}

	public String getDisposition() {
		return disp;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean e) {
		enabled = e;
	}

	public long getAddress() {
		return address;
	}

	public String getFunction() {
		return func;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public int getTimes() {
		return times;
	}

	public String getWhat() {
		return what;
	}

	public int getIgnoreCount() {
		return ignore;
	}

	public String getCondition() {
		return cond;
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("number")) {
				try {
					number = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("type")) {
				type = str;
			} else if (var.equals("disp")) {
				disp = str;
			} else if (var.equals("enabled")) {
				enabled = str.equals("y");
			} else if (var.equals("addr")) {
				try {
					address = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func")) {
				func = str;
			} else if (var.equals("file")) {
				file = str;
			} else if (var.equals("line")) {
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("times")) {
				try {
					times = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("what") || var.equals("exp")) {
				what = str;
			} else if (var.equals("ignore")) {
				try {
					ignore = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("cond")) {
				cond = str;
			}
		}
	}
}
