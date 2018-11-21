/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.COutputEntry;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CLocationOutputTab extends CLocationTab {

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		label.setText(Messages.CLocationOutputTab_0);
	}

	@Override
	public ICExclusionPatternPathEntry[] getEntries(ICResourceDescription cfgd) {
		return cfgd.getConfiguration().getBuildSetting().getOutputDirectories();
	}

	@Override
	public void setEntries(ICResourceDescription cfgd, ICExclusionPatternPathEntry[] data) {
		ICOutputEntry[] out = null;
		if (data != null) {
			out = new ICOutputEntry[data.length];
			for (int i = 0; i < out.length; i++)
				out[i] = (ICOutputEntry) data[i];
		}
		cfgd.getConfiguration().getBuildSetting().setOutputDirectories(out);
	}

	@Override
	public ICExclusionPatternPathEntry newEntry(IPath p, IPath[] ex, boolean isWorkspacePath) {
		return new COutputEntry(p, ex, isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0);
	}

	@Override
	public ICExclusionPatternPathEntry newEntry(IFolder f, IPath[] ex, boolean isWorkspacePath) {
		return new COutputEntry(f, ex, isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0);
	}
}
