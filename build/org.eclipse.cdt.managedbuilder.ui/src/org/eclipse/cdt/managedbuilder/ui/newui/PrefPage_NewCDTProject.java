/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.AbstractPrefPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;
import org.eclipse.core.runtime.CoreException;

public class PrefPage_NewCDTProject extends AbstractPrefPage {
	private ICConfigurationDescription prefCfgd = null;

	protected boolean isSingle() { return false; }

	public ICResourceDescription getResDesc() {
		if (prefCfgd == null)
			try {
				prefCfgd = CCorePlugin.getDefault().getPreferenceConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID);
			} catch (CoreException e) { return null; }
		return prefCfgd.getRootFolderDescription();
	}
	
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		try {
			CCorePlugin.getDefault().setPreferenceConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, prefCfgd);
		} catch (CoreException e) { return false; }
		return true;
	}

	protected boolean needsHeader() { return true; }
}
