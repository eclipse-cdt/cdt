package org.eclipse.cdt.debug.mi.core.output;

/**
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

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getString();
			}

			if (var.equals("number")) {
				try {
					number = Integer.parseInt(str);
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
					address = Long.parseLong(str);
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func")) {
				func = str;
			} else if (var.equals("file")) {
				file = str;
			} else if (var.equals("line")) {
				try {
					line = Integer.parseInt(str);
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("times")) {
				try {
					times = Integer.parseInt(str);
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("what") || var.equals("exp")) {
				what = str;
			}
		}
	}
}
