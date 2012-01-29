/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Raphael Zulliger (Indel AG) - bug 284699: fixing issues when using same
 *                               macro names with different values
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Used to be factory/cache of setting entries. Superseded by {@link CDataUtil} pool of entries.
 * Not used in CDT anymore.
 *
 * @deprecated Since CDT 9.0. Use corresponding {@link CDataUtil} methods instead.
 */
@Deprecated
public class CSettingEntryFactory {
	public ICSettingEntry getEntry(ICSettingEntry entry) {
		return CDataUtil.getPooledEntry(entry);
	}
	public ICLanguageSettingEntry getLanguageSettingEntry(ICLanguageSettingEntry entry) {
		return CDataUtil.getPooledEntry(entry);
	}
	public ICSettingEntry getEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags, boolean create) {
		return CDataUtil.createEntry(kind, name, value, exclusionPatterns, flags);
	}
	public void clear() {
	}
}
