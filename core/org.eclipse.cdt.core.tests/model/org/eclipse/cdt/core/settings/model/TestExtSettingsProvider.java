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
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
import org.eclipse.core.resources.IProject;

public class TestExtSettingsProvider extends CExternalSettingProvider {
	private static CExternalSetting[] SETTINGS = new CExternalSetting[]{
		new CExternalSetting(null, null, null, new ICSettingEntry[]{
				new CIncludePathEntry("ip_a", 0),
				new CIncludePathEntry("ip_b", 0),
				new CIncludeFileEntry("if_a", 0),
				new CIncludeFileEntry("if_b", 0),
				new CMacroEntry("m_a", "mv_a", 0),
				new CMacroEntry("m_b", "mv_b", 0),
				new CMacroFileEntry("mf_a", 0),
				new CMacroFileEntry("mf_b", 0),
				new CLibraryPathEntry("lp_a", 0),
				new CLibraryPathEntry("lp_b", 0),
				new CLibraryFileEntry("lf_a", 0),
				new CLibraryFileEntry("lf_b", 0),
				new CSourceEntry("sp_a", null, 0),
				new CSourceEntry("sp_b", null, 0),
				new COutputEntry("op_a", null, 0),
				new COutputEntry("op_b", null, 0),
		})
	};

	public CExternalSetting[] getSettings(IProject project,
			ICConfigurationDescription cfg) {
		return (CExternalSetting[])SETTINGS.clone();
	}

}
