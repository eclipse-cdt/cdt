/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * Represent a GDB Tuple MI assembly response.
 */
public class MIAsm {
	long address;
	String function = "";
	long offset;
	String instruction = "";

	public MIAsm (MITuple tuple) {
		parse(tuple);
	}

	public long getAddress() {
		return address;
	}

	public String getFunction() {
		return function;
	}

	public long getOffset() {
		return offset;
	}

	public String getInstruction() {
		return instruction;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('{');
		buffer.append("address=\"" + Long.toHexString(address) +"\"");
		buffer.append(",func-name=\"" + function + "\"");
		buffer.append(",offset=\"").append(offset).append('"');
		buffer.append(",inst=\"" + instruction + "\"");
		buffer.append('}');
		return buffer.toString();
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

			if (var.equals("address")) {
				try {
					address = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func-name")) {
				function = str;
			} else if (var.equals("offset")) {
				try {
					offset = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("inst")) {
				instruction = str;
			}
		}
	}
}
