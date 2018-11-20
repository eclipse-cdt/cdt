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
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoProcessor;

public class MakeDiscoveredInfoProcessor extends CDataDiscoveredInfoProcessor {
	private static MakeDiscoveredInfoProcessor fInstance;

	public static MakeDiscoveredInfoProcessor getDefault() {
		if (fInstance == null)
			fInstance = new MakeDiscoveredInfoProcessor();
		return fInstance;
	}

	@Override
	protected void setInfoForData(CConfigurationData cfgData, CResourceData rcData, CLanguageData data, PathInfo pi,
			CResourceData baseRcData, CLanguageData baseLangData) {
		MakeLanguageData mld = (MakeLanguageData) data;
		mld.setDiscoveredInfo(pi);
	}

}
