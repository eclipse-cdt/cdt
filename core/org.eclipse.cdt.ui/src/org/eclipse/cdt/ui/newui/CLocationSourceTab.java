/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;

public class CLocationSourceTab extends CLocationTab {
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		label.setText("Source folders on build path:"); //$NON-NLS-1$
	}
	
	public ICExclusionPatternPathEntry newEntry(IPath p, IPath[] ex, boolean isWorkspacePath) {
		return new CSourceEntry(p, ex, isWorkspacePath ? ICLanguageSettingEntry.VALUE_WORKSPACE_PATH : 0);
	}
	public ICExclusionPatternPathEntry newEntry(IFolder f, IPath[] ex, boolean isWorkspacePath) {
		return new CSourceEntry(f, ex, isWorkspacePath ? ICLanguageSettingEntry.VALUE_WORKSPACE_PATH : 0);
	}
	public ICExclusionPatternPathEntry[] getEntries(ICResourceDescription cfgd) {
		return cfgd.getConfiguration().getSourceEntries();
	}
	public void setEntries(ICResourceDescription cfgd, ICExclusionPatternPathEntry[] data) {
		ICSourceEntry[] out = null;
		if (data != null) {
			out = new ICSourceEntry[data.length];
			for (int i=0; i<out.length; i++) out[i] = (ICSourceEntry)data[i];
		}
		try {
			cfgd.getConfiguration().setSourceEntries(out);
		} catch (CoreException e) {}
	}
}
