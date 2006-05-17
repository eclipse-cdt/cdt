/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a set name=value.
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
	 * [name="xxx",name="xxx",..]
	 * [{name="xxx"},{name="xxx"}]
	 */
	public static MIArg[] getMIArgs(MIList miList) {
		List aList = new ArrayList();
		MIValue[] values = miList.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MITuple) {
				MIArg arg = getMIArg((MITuple)values[i]);
				if (arg != null) {
					aList.add(arg);
				}
			}
		}
		MIResult[] results = miList.getMIResults();
		for (int i = 0; i < results.length; i++) {
			MIValue value = results[i].getMIValue();
			if (value instanceof MIConst) {
				String str = ((MIConst)value).getCString();
				aList.add(new MIArg(str, "")); //$NON-NLS-1$
			}
		}
		return ((MIArg[])aList.toArray(new MIArg[aList.size()]));
	}

	/**
	 * Parsing a MITuple of the form:
	 * {{name="xxx",value="yyy"},{name="xxx",value="yyy"},..}
	 * {name="xxx",name="xxx",..}
	 * {{name="xxx"},{name="xxx"}}
	 */
	public static MIArg[] getMIArgs(MITuple miTuple) {
		List aList = new ArrayList();
		MIValue[] values = miTuple.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MITuple) {
				MIArg arg = getMIArg((MITuple)values[i]);
				if (arg != null) {
					aList.add(arg);
				}
			}
		}
		MIResult[] results = miTuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			MIValue value = results[i].getMIValue();
			if (value instanceof MIConst) {
				String str = ((MIConst)value).getCString();
				aList.add(new MIArg(str, "")); //$NON-NLS-1$
			}
		}
		return ((MIArg[])aList.toArray(new MIArg[aList.size()]));
	}
	/**
	 * Parsing a MITuple of the form:
	 * {name="xxx",value="yyy"}
	 * {name="xxx"}
	 */
	public static MIArg getMIArg(MITuple tuple) {
		MIResult[] args = tuple.getMIResults();
		MIArg arg = null;
		if (args.length > 0) {
			// Name
			String aName = ""; //$NON-NLS-1$
			MIValue value = args[0].getMIValue();
			if (value != null && value instanceof MIConst) {
				aName = ((MIConst)value).getCString();
			} else {
				aName = ""; //$NON-NLS-1$
			}

			// Value
			String aValue = ""; //$NON-NLS-1$
			if (args.length == 2) {
				value = args[1].getMIValue();
				if (value != null && value instanceof MIConst) {
					aValue = ((MIConst)value).getCString();
				} else {
					aValue = ""; //$NON-NLS-1$
				}
			}

			arg = new MIArg(aName, aValue);
		}
		return arg;
	}

	public String toString() {
		return name + "=" + value; //$NON-NLS-1$
	}
}
