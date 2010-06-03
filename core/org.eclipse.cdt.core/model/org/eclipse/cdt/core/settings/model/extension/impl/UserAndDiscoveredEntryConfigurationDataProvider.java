/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension.impl;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.CDataSerializer;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.UserAndDiscoveredEntryDataSerializer;
import org.eclipse.cdt.core.settings.model.util.UserAndDiscoveredEntryLanguageData;

public abstract class UserAndDiscoveredEntryConfigurationDataProvider extends
		CDefaultConfigurationDataProvider {
	private static DataFactory fDataFactory;
	
	protected class LanguageData extends UserAndDiscoveredEntryLanguageData {

		public LanguageData() {
			super();
		}

		public LanguageData(String id, CLanguageData base) {
			super(id, base);
		}

		public LanguageData(String id, String languageId, String[] ids,
				boolean isContentTypes) {
			super(id, languageId, ids, isContentTypes);
		}
		
		public LanguageData(String id, String name, String languageId, int kinds, String[] ids,
				boolean isContentTypes) {
			super(id, languageId, ids, isContentTypes);
			fName = name;
			fSupportedKinds = kinds;
		}

		@Override
		protected ICLanguageSettingEntry[] getAllDiscoveredEntries(int kind) {
			return UserAndDiscoveredEntryConfigurationDataProvider.this.getAllDiscoveredEntries(this, kind);
		}
	}
	
	protected class DataFactory extends CDataFactory {
		@Override
		public CLanguageData createLanguageData(CConfigurationData cfg,
				CResourceData rcBase, CLanguageData base, String id, boolean clone) {
			if(id == null)
				id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
			return new LanguageData(id, base);
		}

		@Override
		public CLanguageData createLanguageData(CConfigurationData cfg,
				CResourceData rcBase, String id, String name, String languageId,
				int supportedKinds, String[] rcTypes, boolean isContentTypes) {
			if(id == null)
				id = CDataUtil.genId(rcBase.getId());
			LanguageData lData = new LanguageData(id, name, languageId, supportedKinds, rcTypes, isContentTypes);
			return lData;
		}		
	}
	
	@Override
	protected CDataSerializer getDataSerializer() {
		return UserAndDiscoveredEntryDataSerializer.getDefault();
	}

	@Override
	protected CDataFactory getDataFactory() {
		if(fDataFactory == null){
			fDataFactory = new DataFactory();
		}
		return fDataFactory;
	}
	
	protected abstract ICLanguageSettingEntry[] getAllDiscoveredEntries(LanguageData data, int kind);

}
