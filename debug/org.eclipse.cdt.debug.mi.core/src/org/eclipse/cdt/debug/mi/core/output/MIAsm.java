package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIAsm {
	long address;
	String func = "";
	long offset;
	String inst = "";
	int line;
	String file = "";

	public MIAsm(MITuple tuple) {
		parse(tuple);
	}

	public long getAddress() {
		return address;
	}

	public String getFunction() {
		return func;
	}

	public long getOffset() {
		return offset;
	}

	public String getInstruction() {
		return inst;
	}

	public int getLine() {
		return line;
	}

	public String getFile() {
		return file;
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";

			if (value instanceof MITuple) {
				parse((MITuple)value);
				continue;
			}

			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getString();
			}

			if (var.equals("address")) {
				try {
					address = Long.parseLong(str);
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func-name")) {
				func = str;
			} else if (var.equals("offset")) {
				try {
					offset = Long.parseLong(str);
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("inst")) {
				inst = str;
			} else if (var.equals("line")) {
				try {
					line = Integer.parseInt(str);
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("file")) {
				file = str;
			}
		}
	}
}
