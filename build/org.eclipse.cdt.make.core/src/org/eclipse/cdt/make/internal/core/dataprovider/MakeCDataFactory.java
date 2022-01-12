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
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.dataprovider;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

public class MakeCDataFactory extends CDataFactory {
	private static MakeCDataFactory fInstance;

	public static CDataFactory getDefault() {
		if (fInstance == null) {
			fInstance = new MakeCDataFactory();
		}
		return fInstance;
	}

	@Override
	public CLanguageData createLanguageData(CConfigurationData cfg, CResourceData rcBase, CLanguageData base, String id,
			boolean clone) {
		if (id == null)
			id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
		return new MakeLanguageData(id, base);
	}

	@Override
	public CLanguageData createLanguageData(CConfigurationData cfg, CResourceData rcBase, String id, String name,
			String languageId, int supportedKinds, String[] rcTypes, boolean isContentTypes) {
		if (id == null)
			id = CDataUtil.genId(rcBase.getId());
		MakeLanguageData lData = new MakeLanguageData(id, name, languageId, supportedKinds, rcTypes, isContentTypes);
		return lData;
	}
}