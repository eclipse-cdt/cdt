/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


public class AdjustmentContext {
//	static final int TYPE_CONFIGURATION = 1;
//	static final int TYPE_TOOL_CHAIN = 2;
//	static final int TYPE_TOOL = 3;
//	static final int TYPE_OPTION = 4;
//	static final int TYPE_OUTPUT_TYPE = 5;
	
//	private IBuildObject fObject;
//	private int fType;
	
//	class AfjustedInfo{
//		private boolean fAdjusted;
//
//		void addAdjustmentState(boolean adjusted){
//			if(!fAdjusted && adjusted){
//				fAdjusted = adjusted;
//			}
//		}
//		
//		boolean isAdjusted(){
//			return fAdjusted;
//		}
//	}
	
//	AdjustmentInfo(IConfiguration cfg){
//		fObject = cfg;
//		fType = TYPE_CONFIGURATION;
//	}
	
//	public int getType(){
//		return fType;
//	}
	
	private HashMap<String, Boolean> fMap = new HashMap<String, Boolean>();

	public void addAdjustedState(String attr, boolean adjusted){
		Boolean b = fMap.get(attr);
		if(b == null ||
				(adjusted &&
						!b.booleanValue())){
			fMap.put(attr, Boolean.valueOf(adjusted));
		}
	}
	
	public String[] getUnadjusted(){
		if(fMap.size() == 0)
			return new String[0];
		
		ArrayList<String> list = new ArrayList<String>(fMap.size());
		Set<Entry<String, Boolean>> entrySet = fMap.entrySet();
		for (Entry<String, Boolean> entry : entrySet) {
			Boolean b = entry.getValue();
			if(!b.booleanValue()){
				list.add(entry.getKey());
			}
		}
		return list.toArray(new String[list.size()]);
	}
}
