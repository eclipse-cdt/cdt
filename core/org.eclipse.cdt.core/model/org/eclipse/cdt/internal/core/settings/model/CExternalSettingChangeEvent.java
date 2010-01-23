/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CExternalSettingChangeEvent {
	private List<CExternalSettingsContainerChangeInfo> fChangeInfoList = new ArrayList<CExternalSettingsContainerChangeInfo>();

	CExternalSettingChangeEvent(CExternalSettingsContainerChangeInfo[] infos){
		fChangeInfoList.addAll(Arrays.asList(infos));
	}
	
//	void add(CExternalSettingsContainerChangeInfo info){
//		fChangeInfoList.add(info);
//	}
	
	public CExternalSettingsContainerChangeInfo[] getChangeInfos(){
		return fChangeInfoList.toArray(
				new CExternalSettingsContainerChangeInfo[fChangeInfoList.size()]);
	}
}
