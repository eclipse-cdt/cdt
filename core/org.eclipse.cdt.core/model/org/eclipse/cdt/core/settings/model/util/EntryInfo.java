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
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

public class EntryInfo {
	private ICLanguageSettingEntry fEntry;
	private EntryNameKey fNameKey;
	private boolean fIsOverRidden;

	EntryInfo(ICLanguageSettingEntry entry){
		fEntry = entry;
	}

	public EntryNameKey getNameKey(){
		if(fNameKey == null){
			fNameKey = new EntryNameKey(fEntry);
		}
		return fNameKey;
	}
	
	public void makeOverridden(boolean overrridden){
		fIsOverRidden = overrridden;
	}

	public ICLanguageSettingEntry getEntry(){
		return fEntry;
	}

	public boolean isOverridden(){
		return fIsOverRidden;
	}

}

