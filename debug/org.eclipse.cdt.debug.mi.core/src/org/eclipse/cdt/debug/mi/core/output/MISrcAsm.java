/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a GDB Tuple MI assembly response.
 */
public class MISrcAsm {
	int line;
	String file = "";
	MIAsm[] asm;

	public MISrcAsm(MITuple tuple) {
		parse(tuple);
	}

	public int getLine() {
		return line;
	}

	public String getFile() {
		return file;
	}

	public MIAsm[] getMIAsms() {
		return asm;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("src_and_asm_line={");
		buffer.append("line=\"").append(line).append('"');
		buffer.append(",file=\"" + file + "\",");
		buffer.append("line_asm_insn=[");
		for (int i = 0; i < asm.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(asm[i].toString());
		}
		buffer.append(']');
		buffer.append('}');
		return buffer.toString();
	}

	void parse(MITuple tuple) {
		List aList = new ArrayList();
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";

			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("line_asm_insn")) {
				if (value instanceof MIList) {
					MIList list = (MIList)value;
					MIValue[] values = list.getMIValues();
					for (int j = 0; j < values.length; j++) {
						if (values[j] instanceof MITuple) {
							aList.add(new MIAsm((MITuple)values[j]));
						}
					}
				}
			} if (var.equals("line")) {
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("file")) {
				file = str;
			}
		}
		asm = (MIAsm[])aList.toArray(new MIAsm[aList.size()]);
	}
}
