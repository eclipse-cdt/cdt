/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

public class SettingsChangeEvent {
	private IResourceInfo fRcInfo;
	private IHoldsOptions fHolder;
	private IOption fOption;
	private Object fOldValue;

	private int fChangeType;
	private int fSettingType;
	public static final int CHANGED = 1;
	public static final int ADDED = 1 << 1;
	public static final int REMOVED = 1 << 2;

	public static final int OPTION = 1;

	SettingsChangeEvent(int changeType, IResourceInfo rcInfo, IHoldsOptions holder, IOption option, Object oldValue) {
		fSettingType = OPTION;
		fChangeType = changeType;
		fRcInfo = rcInfo;
		fHolder = holder;
		fOption = option;
		fOldValue = oldValue;
	}

	public IResourceInfo getRcInfo() {
		return fRcInfo;
	}

	public IHoldsOptions getHolder() {
		return fHolder;
	}

	public IOption getOption() {
		return fOption;
	}

	public Object getOldValue() {
		return fOldValue;
	}

	public int getChangeType() {
		return fChangeType;
	}

	public int getSettingType() {
		return fSettingType;
	}
}
