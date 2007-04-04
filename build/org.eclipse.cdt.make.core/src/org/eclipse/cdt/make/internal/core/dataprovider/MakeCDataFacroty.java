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
package org.eclipse.cdt.make.internal.core.dataprovider;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFacroty;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

public class MakeCDataFacroty extends CDataFacroty {
	private static MakeCDataFacroty fInstance;
	
	public static CDataFacroty getDefault(){
		if(fInstance == null){
			fInstance = new MakeCDataFacroty();
		}
		return fInstance;
	}

	public CLanguageData createLanguageData(CConfigurationData cfg,
			CResourceData rcBase, CLanguageData base, String id, boolean clone) {
		if(id == null)
			id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
		return new MakeLanguageData(id, base);
	}

	public CLanguageData createLanguageData(CConfigurationData cfg,
			CResourceData rcBase, String id, String name, String languageId,
			int supportedKinds, String[] rcTypes, boolean isContentTypes) {
		if(id == null)
			id = CDataUtil.genId(rcBase.getId());
		MakeLanguageData lData = new MakeLanguageData(id, name, languageId, supportedKinds, rcTypes, isContentTypes);
		return lData;
	}
}