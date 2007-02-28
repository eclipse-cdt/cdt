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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.util.EntryNameKey;

public class EntryInfo {
	private ICLanguageSettingEntry fEntry;
	private EntryNameKey fNameKey;
//	private IOption fOption;
//	private String fEnvVarName;
//	private int fPosition;
	private boolean fIsDiscovered;
	private boolean fIsOverRidden;

	EntryInfo(ICLanguageSettingEntry entry){
		fEntry = entry;
	}

/*	EntryInfo(ICLanguageSettingEntry entry, boolean discovered, boolean isOverridden){
		fEntry = entry;
		fIsDiscovered = discovered;
		fIsOverRidden = isOverridden;
	}
*/	
	public EntryNameKey getNameKey(){
		if(fNameKey == null){
			fNameKey = new EntryNameKey(fEntry);
		}
		return fNameKey;
	}

/*	EntryInfo(ICLanguageSettingEntry entry, boolean discovered, IOption option, int position){
		fEntry = entry;
		fIsDiscovered = discovered;
		fOption = option;
		fPosition = position;
	}

	EntryInfo(ICLanguageSettingEntry entry, boolean discovered, String envVarName, int position){
		fEntry = entry;
		fIsDiscovered = discovered;
		fEnvVarName = envVarName;
		fPosition = position;
	}
*/	
/*	public void setOptionInfo(IOption option, int pos){
		fOption = option;
		fPosition = pos;
	}
*/	
	public void makeOverridden(boolean overrridden){
/*		fOption = null;
		fEnvVarName = null;
		fPosition = 0;
*/
		fIsOverRidden = overrridden;
		
	}

/*	public void setEnvironmentInfo(String envVarName, int pos){
		fEnvVarName = envVarName;
		fPosition = pos;
	}
*/
	public ICLanguageSettingEntry getEntry(){
		return fEntry;
	}

	public boolean isDiscovered(){
		return fIsDiscovered;
	}
	
	public boolean isOverridden(){
//		return fOption != null || fEnvVarName != null;
		return fIsOverRidden;
	}
	
	public boolean isUndefined(){
		//TODO
		return false;
	}

/*	public IOption getOption(){
		return fOption;
	}
*/
/*	public String getEnvVarName(){
		return fEnvVarName;
	}
*/
/*	public int getPosition(){
		return fPosition;
	}
*/
}
