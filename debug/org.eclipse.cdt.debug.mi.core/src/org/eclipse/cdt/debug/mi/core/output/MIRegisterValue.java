package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;



/**
 */
public class MIRegisterValue {
	int name;
	long value;

	public MIRegisterValue(int number, long value) {
		this.name = name;
		this.value = value;
	}

	public int getNumber() {
		return name;
	}

	public long getValue() {
		return value;
	} 

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("number=\"").append(name).append('"');
		buffer.append(',').append("value=\"" + Long.toHexString(value) + "\"");
		return buffer.toString();
	}

	/**
	 * Parsing a MIList of the form:
	 * [{number="1",value="0xffff"},{number="xxx",value="yyy"},..]
	 */
	public static MIRegisterValue[] getMIRegisterValues(MIList miList) {
		List aList = new ArrayList();
		MIValue[] values = miList.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MITuple) {
				MIRegisterValue reg = getMIRegisterValue((MITuple)values[i]);
				if (reg != null) {
					aList.add(reg);
				}
			}
		}
		return ((MIRegisterValue[])aList.toArray(new MIRegisterValue[aList.size()]));
	}

	/**
	 * Parsing a MITuple of the form:
	 * {number="xxx",value="yyy"}
	 */
	public static MIRegisterValue getMIRegisterValue(MITuple tuple) {
		MIResult[] args = tuple.getMIResults();
		MIRegisterValue arg = null;
		if (args.length == 2) {
			// Name
			String aName = "";
			MIValue value = args[0].getMIValue();
			if (value != null && value instanceof MIConst) {
				aName = ((MIConst)value).getString();
			} else {
				aName = "";
			}

			// Value
			String aValue = "";
			value = args[1].getMIValue();
			if (value != null && value instanceof MIConst) {
				aValue = ((MIConst)value).getString();
			} else {
				aValue = "";
			}

			try {
				int reg = Integer.parseInt(aName.trim());
				long val = Long.decode(aValue.trim()).longValue();
				arg = new MIRegisterValue(reg, val);
			} catch (NumberFormatException e) {
			}
		}
		return arg;
	}
}
