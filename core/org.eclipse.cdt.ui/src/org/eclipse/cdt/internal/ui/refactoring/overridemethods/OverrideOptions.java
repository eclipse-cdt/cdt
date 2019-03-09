/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.ICProject;

public class OverrideOptions {

	private boolean fIgnoreVirtual;
	private boolean fAddOverride;

	public OverrideOptions(ICProject project) {
		fAddOverride = CCorePreferenceConstants.getPreference(CCorePreferenceConstants.ADD_OVERRIDE_KEYWORD, project,
				CCorePreferenceConstants.DEFAULT_ADD_OVERRIDE_KEYWORD);
		fIgnoreVirtual = CCorePreferenceConstants.getPreference(CCorePreferenceConstants.IGNORE_VIRTUAL_KEYWORD,
				project, CCorePreferenceConstants.DEFAULT_IGNORE_VIRTUAL_KEYWORD);
	}

	public boolean ignoreVirtual() {
		return fIgnoreVirtual;
	}

	public void setIgnoreVirtual(boolean ignoreVirtual) {
		this.fIgnoreVirtual = ignoreVirtual;
	}

	public boolean addOverride() {
		return fAddOverride;
	}

	public void setAddOverride(boolean addOverride) {
		this.fAddOverride = addOverride;
	}
}
