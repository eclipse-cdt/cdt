/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.settings.model.ACSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class EntryContentsKey {
	ICSettingEntry fEntry;
	
	public EntryContentsKey(ICSettingEntry entry){
		fEntry = entry;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(!(obj instanceof EntryContentsKey))
			return false;
		return fEntry.equalsByContents(((EntryContentsKey)obj).fEntry);
	}

	@Override
	public int hashCode() {
		return ((ACSettingEntry)fEntry).codeForContentsKey();
	}
		
	public ICSettingEntry getEntry(){
		return fEntry;
	}
}
