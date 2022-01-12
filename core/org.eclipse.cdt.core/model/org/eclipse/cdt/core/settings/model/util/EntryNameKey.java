/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.settings.model.ACSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class EntryNameKey {
	ICSettingEntry fEntry;

	public EntryNameKey(ICSettingEntry entry) {
		fEntry = entry;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof EntryNameKey))
			return false;
		return fEntry.equalsByName(((EntryNameKey) obj).fEntry);
	}

	@Override
	public int hashCode() {
		return ((ACSettingEntry) fEntry).codeForNameKey();
	}

	public ICSettingEntry getEntry() {
		return fEntry;
	}
}
