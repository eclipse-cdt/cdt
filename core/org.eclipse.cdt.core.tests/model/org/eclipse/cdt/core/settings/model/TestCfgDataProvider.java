/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.CTestPlugin;

/**
 * Basic CDefaultConfigurationDataProvider
 * 
 * This is extended to allow persisting Library path + Library file language
 * settings which aren't supported by the base default configuration data provider
 * i.e. cdt.core without managedbuild...
 */
public class TestCfgDataProvider extends CDefaultConfigurationDataProvider {
	public static final String PROVIDER_ID = CTestPlugin.PLUGIN_ID + ".testCfgDataProvider";

	
	// Overriden so it supports saving Library file and Library path entries
	private static final class TestDataFactory extends CDataFactory {

		@Override
		public CLanguageData createLanguageData(CConfigurationData cfg, 
				CResourceData rcBase,
				CLanguageData base,
				String id,
				boolean clone){
				if(id == null)
					id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
			return new CDefaultLanguageData(id, base);
		}

		@Override
		public CLanguageData createLanguageData(CConfigurationData cfg, 
				CResourceData rcBase,
				String id,
				String name,
				String languageId,
				int supportedEntryKinds,
				String[] rcTypes,
				boolean isContentTypes) {
			return super.createLanguageData(cfg, rcBase, id, name, languageId, 
					supportedEntryKinds | ICSettingEntry.LIBRARY_FILE | ICSettingEntry.LIBRARY_PATH, 
					rcTypes, isContentTypes);
		}

	}

	@Override
	protected CDataFactory getDataFactory(){
		return new TestDataFactory();
	}

}
