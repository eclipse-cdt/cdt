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

import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.core.runtime.IPath;

public class BuildFolderData extends CFolderData {
	private FolderInfo fFolderInfo;
	public BuildFolderData(IFolderInfo folderInfo){
		fFolderInfo = (FolderInfo)folderInfo;
	}
	
	public IFolderInfo getFolderInfo(){
		return fFolderInfo;
	}

	public CLanguageData[] getLanguageDatas() {
		return fFolderInfo.getCLanguageDatas();
	}

	public IPath getPath() {
		return fFolderInfo.getPath();
	}

//	public boolean isExcluded() {
//		return fFolderInfo.isExcluded();
//	}
//
//	public void setExcluded(boolean excluded) {
//		fFolderInfo.setExclude(excluded);
//	}

	public void setPath(IPath path) {
		fFolderInfo.setPath(path);
	}

	public String getId() {
		return fFolderInfo.getId();
	}

	public String getName() {
		return fFolderInfo.getName();
	}

	public void setName(String name) {
		// TODO Auto-generated method stub
	}

	public boolean isValid() {
		return fFolderInfo.isValid();
	}

	public CLanguageData createLanguageDataForContentTypes(String languageId,
			String[] typesIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public CLanguageData createLanguageDataForExtensions(String languageId,
			String[] extensions) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasCustomSettings() {
		return fFolderInfo.hasCustomSettings();
	}
	
	void clearCachedData(){
		CLanguageData[] lDatas = getLanguageDatas();
		for(int i = 0; i < lDatas.length; i++){
			((BuildLanguageData)lDatas[i]).clearCachedData();
		}
	}

	public boolean containsScannerInfo() {
		return fFolderInfo.containsDiscoveredScannerInfo();
	}
	
	public void setContainsDiscoveredScannerInfo(boolean contains) {
		fFolderInfo.setContainsDiscoveredScannerInfo(contains);
	}

}
