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

import org.eclipse.cdt.core.settings.model.ACLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

public class EntryNameKey {
	ICLanguageSettingEntry fEntry;
		
		public EntryNameKey(ICLanguageSettingEntry entry){
			fEntry = entry;
		}

		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			
			if(!(obj instanceof EntryNameKey))
				return false;
			return fEntry.equalsByName(((EntryNameKey)obj).fEntry);
		}

		public int hashCode() {
			return ((ACLanguageSettingEntry)fEntry).codeForNameKey();
		}
		
		public ICLanguageSettingEntry getEntry(){
			return fEntry;
		}
}
