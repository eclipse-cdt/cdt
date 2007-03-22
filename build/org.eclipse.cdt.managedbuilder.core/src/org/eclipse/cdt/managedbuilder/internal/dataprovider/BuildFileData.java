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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.core.runtime.IPath;

public class BuildFileData extends CFileData {
	private ResourceConfiguration fFileInfo;
	public BuildFileData(IFileInfo fileInfo){
		fFileInfo = (ResourceConfiguration)fileInfo;
	}
	
	public IFileInfo getFileInfo(){
		return fFileInfo;
	}

	public IPath getPath() {
		return fFileInfo.getPath();
	}

	public boolean isExcluded() {
		return fFileInfo.isExcluded();
	}

	public void setExcluded(boolean excluded) {
		fFileInfo.setExclude(excluded);
	}

	public void setPath(IPath path) {
		fFileInfo.setPath(path);
	}

	public String getId() {
		return fFileInfo.getId();
	}

	public String getName() {
		return fFileInfo.getName();
	}

	public void setName(String name) {
//		fFileInfo.setN
	}

	public boolean isValid() {
		return fFileInfo.isValid();
	}

	public CLanguageData getLanguageData() {
		CLanguageData datas[] = fFileInfo.getCLanguageDatas();
		if(datas.length > 0)
			return datas[0];
		return null;
	}

	public boolean hasCustomSettings() {
		return fFileInfo.hasCustomSettings();
	}
	
	void clearCachedData(){
		BuildLanguageData lData = (BuildLanguageData)getLanguageData();
		if(lData != null)
			lData.clearCachedData();
	}

}
