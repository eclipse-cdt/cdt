package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;



/**
 */
public class MIArg {
	String name;
	String value;

	public MIArg(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	} 

	/**
	 * Parsing a MIList of the form:
	 * [{name="xxx",value="yyy"},{name="xxx",value="yyy"},..]
	 */
	public static MIArg[] getMIArgs(MIList miList) {
		List aList = new ArrayList();
		MIResult[] results = miList.getMIResults();
		for (int i = 0; i < results.length; i++) {
			MIValue value = results[i].getMIValue();
			if (value instanceof MITuple) {
				MIArg arg = getMIArg((MITuple)value);
				if (arg != null) {
					aList.add(arg);
				}
			}
		}
		return ((MIArg[])aList.toArray(new MIArg[aList.size()]));
	}

	/**
	 * Parsing a MITuple of the form:
	 * {name="xxx",value="yyy"}
	 */
	public static MIArg getMIArg(MITuple tuple) {
		MIResult[] args = tuple.getMIResults();
		MIArg arg = null;
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

			arg = new MIArg(aName, aValue);
		}
		return arg;
	}
}
