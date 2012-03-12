/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.text.hover;

import java.util.ArrayList;

public class AutoconfPrototype {
	protected String name;
	protected int numPrototypes;
	protected ArrayList<Integer> minParms;
	protected ArrayList<Integer> maxParms;
	protected ArrayList<ArrayList<String>> parmList;
	
	public AutoconfPrototype() {
		numPrototypes = 0;
		minParms = new ArrayList<Integer>();
		maxParms = new ArrayList<Integer>();
		parmList = new ArrayList<ArrayList<String>>();
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
		return ((Integer)minParms.get(prototypeNum)).intValue();
	}
	
	public void setMinParms(int prototypeNum, int value) {
		minParms.add(prototypeNum, Integer.valueOf(value));
	}
	
	public int getMaxParms(int prototypeNum) {
		return ((Integer)maxParms.get(prototypeNum)).intValue();
	}
	
	public void setMaxParms(int prototypeNum, int value) {
		maxParms.add(prototypeNum, Integer.valueOf(value));
	}
	
	public String getParmName(int prototypeNum, int parmNum) {
		ArrayList<String> parms = parmList.get(prototypeNum);
		return (String)parms.get(parmNum);
	}

	// This function assumes that parms will be added in order starting
	// with lowest prototype first.
	public void setParmName(int prototypeNum, int parmNum, String value) {
		ArrayList<String> parms;
		if (parmList.size() == prototypeNum) {
			parms = new ArrayList<String>();
			parmList.add(parms);
		}
		else
			parms = parmList.get(prototypeNum);
		if (parms.size() == parmNum)
			parms.add(value);	
		else
			parms.set(parmNum, value);
	}
	
}
