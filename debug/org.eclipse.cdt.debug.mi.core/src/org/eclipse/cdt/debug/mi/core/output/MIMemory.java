package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIMemory {
	long addr;
	long [] data = new long[0];
	String ascii = "";

	public MIMemory(MITuple tuple) {
		parse(tuple);
	}

	public long getAddress() {
		return addr;
	}

	public long [] getData() {
		return data;
	}

	public String getAscii() {
		return ascii;
	}

	public String toSting() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("addr=\"" + Long.toHexString(addr) + "\"");
		buffer.append("data=["); 
		for (int i = 0 ; i < data.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append('"').append(Long.toHexString(data[i])).append('"');
		}
		buffer.append(']');
		if (ascii.length() > 0) {
			buffer.append(",ascii=\"" + ascii + "\"");
		}
		return buffer.toString();
	}

	void parse(MITuple tuple) {
		MIResult[] results =  tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getString();
			}

			if (var.equals("addr")) {
				try {
					addr = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("data")) {
				if (value != null && value instanceof MIList) {
					parseData((MIList)value);
				}
			} else if (var.equals("ascii")) {
				ascii = str;
			}
		}
	}

	void parseData(MIList list) {
		MIValue[] values = list.getMIValues();
		data = new long[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				String str = ((MIConst)values[i]).getString();
				try {
					data[i] = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
					data[i] = 0;
				}
			}
		}
	}
}
