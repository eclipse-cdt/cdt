/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Christian Walther (Indel AG) - [335344] test for changing language IDs
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IProject;

public class TestExtSettingsProvider extends CExternalSettingProvider {
	
	public static final String TEST_EXTERNAL_PROVIDER_ID = CTestPlugin.PLUGIN_ID + ".testExtSettingsProvider";

	private static CExternalSetting[] SETTINGS_1 = new CExternalSetting[]{
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
	
	private static CExternalSetting[] SETTINGS_2 = new CExternalSetting[]{
		new CExternalSetting(null, null, null, new ICSettingEntry[]{
				new CIncludePathEntry("ip_a2", 0),
				new CIncludePathEntry("ip_b2", 0),
				new CIncludeFileEntry("if_a2", 0),
				new CIncludeFileEntry("if_b2", 0),
				new CMacroEntry("m_a2", "mv_a2", 0),
				new CMacroEntry("m_b2", "mv_b2", 0),
				new CMacroFileEntry("mf_a2", 0),
				new CMacroFileEntry("mf_b2", 0),
				new CLibraryPathEntry("lp_a2", 0),
				new CLibraryPathEntry("lp_b2", 0),
				new CLibraryFileEntry("lf_a2", 0),
				new CLibraryFileEntry("lf_b2", 0),
				new CSourceEntry("sp_a2", null, 0),
				new CSourceEntry("sp_b2", null, 0),
				new COutputEntry("op_a2", null, 0),
				new COutputEntry("op_b2", null, 0),
		})
	};

	private static CExternalSetting[] SETTINGS_3 = new CExternalSetting[]{
		new CExternalSetting(
				new String[]{
						"org.eclipse.cdt.core.assembly"
				},
				null, null,
				new ICSettingEntry[]{
						new CMacroEntry("m_c", "mv_c", 0)
				}
		)
	};
	
	private static CExternalSetting[] SETTINGS_4 = new CExternalSetting[]{
		new CExternalSetting(
				new String[]{
						"org.eclipse.cdt.core.assembly",
						"org.eclipse.cdt.core.gcc"
				},
				null, null,
				new ICSettingEntry[]{
						new CMacroEntry("m_c", "mv_c", 0)
				}
		)
	};

	public static final CExternalSetting[][] SETTINGS_VARIANTS = new CExternalSetting[][]{
								SETTINGS_1, 
								SETTINGS_2,
								SETTINGS_3,
								SETTINGS_4};

	private static int variantNum;
	
	@Override
	public CExternalSetting[] getSettings(IProject project,
			ICConfigurationDescription cfg) {
		return SETTINGS_VARIANTS[variantNum].clone();
	}
	
	public static void setVariantNum(int num){
		if(num < 0 || num >= SETTINGS_VARIANTS.length)
			throw new IllegalArgumentException();
		variantNum = num;
	}
	
	public static int getVariantNum(){
		return variantNum;
	}

	public static int getMaxVariantNum(){
		return SETTINGS_VARIANTS.length - 1;
	}

}
