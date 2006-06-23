/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;

/**
 * Contain info about the GDB/MI breakpoint info.
 *<ul>
 * <li>
 * -break-insert main
 * ^done,bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * (gdb)
 * </li>
 * <li>
 * -break-insert -t main
 * ^done,bkpt={number="2",type="breakpoint",disp="del",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * </li>
 * <li>
 * -break-insert -c 1 main
^done,bkpt={number="3",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",cond="1",times="0"}
 * </li>
 * <li>
 * -break-insert -h main
 * ^done,bkpt={number="4",type="hw breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * <li>
 * -break-insert -p 0 main
 * ^done,bkpt={number="5",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",thread="0",thread="0",times="0"}
 * </li>
 * <li>
 * -break-insert -a p
 * ^done,hw-awpt={number="2",exp="p"}
 * </li>
 * <li>
 * -break-watch -r p
 * ^done,hw-rwpt={number="4",exp="p"}
 * </li>
 * <li>
 * -break-watch p
 * ^done,wpt={number="6",exp="p"}
 * </li>
 *</ul>
 */
public class MIBreakpoint {

	int number;
	String type = ""; //$NON-NLS-1$
	String disp = ""; //$NON-NLS-1$
	boolean enabled;
	String address;
	String func = "";  //$NON-NLS-1$
	String file = ""; //$NON-NLS-1$
	int line;
	String cond = ""; //$NON-NLS-1$
	int times;
	String what = ""; //$NON-NLS-1$
	String threadId = ""; //$NON-NLS-1$
	int ignore;

	boolean isWpt;
	boolean isAWpt;
	boolean isRWpt;
	boolean isWWpt;
	boolean isHdw;

	public MIBreakpoint(MITuple tuple) {
		parse(tuple);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int num) {
		number = num;
	}

	public String getType() {
		return type;
	}

	public boolean isTemporary() {
		return getDisposition().equals("del"); //$NON-NLS-1$
	}

	public boolean isWatchpoint() {
		return isWpt;
	}

	public void setWatchpoint(boolean w) {
		isWpt = w;
	}

	public boolean isHardware() {
		return isHdw;
	}

	public void setHardware(boolean hd) {
		isWpt = hd;
		isHdw = hd;
	}

	public boolean isAccessWatchpoint() {
		return isAWpt;
	}

	public void setAccessWatchpoint(boolean a) {
		isWpt = a;
		isAWpt = a;
	}

	public boolean isReadWatchpoint() {
		return isRWpt;
	}

	public void setReadWatchpoint(boolean r) {
		isWpt = r;
		isRWpt = r;
	}

	public boolean isWriteWatchpoint() {
		return isWWpt;
	}

	public void setWriteWatchpoint(boolean w) {
		isWpt = w;
		isWWpt = w;
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

	public String getAddress() {
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

	public String getThreadId() {
		return threadId;
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("number")) { //$NON-NLS-1$
				try {
					number = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("type")) { //$NON-NLS-1$
				type = str;
				//type="hw watchpoint"
				if (type.startsWith("hw")) { //$NON-NLS-1$
					isHdw = true;
					isWWpt = true;
					isWpt = true;
				}
				//type="acc watchpoint"
				if (type.startsWith("acc")) { //$NON-NLS-1$
					isWWpt = true;
					isRWpt = true;
					isWpt = true;
				}
				//type="read watchpoint"
				if (type.startsWith("read")) { //$NON-NLS-1$
					isRWpt = true;
					isWpt = true;
				}
				// ??
				if (type.equals("watchpoint")) { //$NON-NLS-1$
					isWpt = true;
				}
				// type="breakpoint"
				// default ok.
			} else if (var.equals("disp")) { //$NON-NLS-1$
				disp = str;
			} else if (var.equals("enabled")) { //$NON-NLS-1$
				enabled = str.equals("y"); //$NON-NLS-1$
			} else if (var.equals("addr")) { //$NON-NLS-1$
				try {
					address = str.trim();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func")) { //$NON-NLS-1$
				func = str;
			} else if (var.equals("file")) { //$NON-NLS-1$
				file = str;
			} else if (var.equals("thread")) { //$NON-NLS-1$
				threadId = str;
			} else if (var.equals("line")) { //$NON-NLS-1$
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("times")) { //$NON-NLS-1$
				try {
					times = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("what") || var.equals("exp")) { //$NON-NLS-1$ //$NON-NLS-2$
				what = str;
			} else if (var.equals("ignore")) { //$NON-NLS-1$
				try {
					ignore = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("cond")) { //$NON-NLS-1$
				cond = str;
			}
		}
	}
}
