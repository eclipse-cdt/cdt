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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (file.length() > 0) {
			buffer.append("line=\"").append(line);
			buffer.append("file=\"" + file + "\",");
			buffer.append("line_asm_insn=[");
		}
		buffer.append('{');
		buffer.append("address=\"" + Long.toHexString(address) +"\"");
		buffer.append(",func-name=\"" + func + "\"");
		buffer.append(",offset=\"").append(offset).append('"');
		buffer.append(",inst=\"" + inst + "\"");
		buffer.append('}');
		if (file.length() > 0) {
			buffer.append(']');
		} 
		return buffer.toString();
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
					address = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func-name")) {
				func = str;
			} else if (var.equals("offset")) {
				try {
					offset = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("inst")) {
				inst = str;
			} else if (var.equals("line")) {
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("file")) {
				file = str;
			}
		}
	}
}
