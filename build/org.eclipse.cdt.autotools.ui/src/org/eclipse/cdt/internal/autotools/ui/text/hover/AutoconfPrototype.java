/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.text.hover;

import java.util.ArrayList;
import java.util.List;

public class AutoconfPrototype {
	protected String name;
	protected int numPrototypes;
	protected List<Integer> minParms;
	protected List<Integer> maxParms;
	protected List<List<String>> parmList;

	public AutoconfPrototype() {
		numPrototypes = 0;
		minParms = new ArrayList<>();
		maxParms = new ArrayList<>();
		parmList = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public int getNumPrototypes() {
		return numPrototypes;
	}

	public void setNumPrototypes(int num) {
		numPrototypes = num;
	}

	public int getMinParms(int prototypeNum) {
		return minParms.get(prototypeNum).intValue();
	}

	public void setMinParms(int prototypeNum, int value) {
		minParms.add(prototypeNum, Integer.valueOf(value));
	}

	public int getMaxParms(int prototypeNum) {
		return maxParms.get(prototypeNum).intValue();
	}

	public void setMaxParms(int prototypeNum, int value) {
		maxParms.add(prototypeNum, Integer.valueOf(value));
	}

	public String getParmName(int prototypeNum, int parmNum) {
		List<String> parms = parmList.get(prototypeNum);
		return parms.get(parmNum);
	}

	// This function assumes that parms will be added in order starting
	// with lowest prototype first.
	public void setParmName(int prototypeNum, int parmNum, String value) {
		List<String> parms;
		if (parmList.size() == prototypeNum) {
			parms = new ArrayList<>();
			parmList.add(parms);
		} else
			parms = parmList.get(prototypeNum);
		if (parms.size() == parmNum)
			parms.add(value);
		else
			parms.set(parmNum, value);
	}

}
