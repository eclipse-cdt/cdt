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
package org.eclipse.cdt.core.settings.model.extension.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.IPath;

public class CDefaultFolderData extends CFolderData {
	protected IPath fPath;
	protected boolean fIsExcluded;
	protected List fLanguageDatas = new ArrayList();
	protected String fName;
	protected String fId;
	protected CConfigurationData fCfg;
	private CDataFacroty fFactory;
	protected boolean fIsModified;

	public CDefaultFolderData(CConfigurationData cfg, CDataFacroty factory) {
		fCfg = cfg;
		if(factory == null)
			factory = new CDataFacroty();
		fFactory = factory;
	}

	public CDefaultFolderData(String id, IPath path, CConfigurationData cfg, CDataFacroty factory) {
		this(cfg, factory);
		fId = id;
		fName = path.toString();
		fPath = path;
	}

	public CDefaultFolderData(String id, IPath path, CFolderData base, CConfigurationData cfg, CDataFacroty factory, boolean clone) {
		this(id, path, cfg, factory);
		
		copyDataFrom(base, clone);
	}
	
	protected void copyDataFrom(CFolderData base, boolean clone){
		if(base != null){
			fIsExcluded = base.isExcluded();
			
			CLanguageData lDatas[] = base.getLanguageDatas();
			for(int i = 0; i < lDatas.length; i++){
				fLanguageDatas.add(copyLanguageData(lDatas[i], clone));
			}
		}
	}

	protected CLanguageData copyLanguageData(CLanguageData base, boolean clone){
		return fFactory.createLanguageData(fCfg, this, base, clone);
	}
	
	public CLanguageData[] getLanguageDatas() {
		return (CLanguageData[])fLanguageDatas.toArray(new CLanguageData[fLanguageDatas.size()]);
	}

	public IPath getPath() {
		return fPath;
	}

	public boolean isExcluded() {
		return fIsExcluded;
	}

	public void setExcluded(boolean excluded) {
		if(excluded == fIsExcluded)
			return;

		fIsExcluded = excluded;
		setModified(true);
	}

	public void setPath(IPath path) {
		if(CDataUtil.objectsEqual(path, fPath))
			return;
		
		fPath = path;
		setModified(true);
	}

	public String getName() {
		return fName;
	}

	public String getId() {
		return fId;
	}

	public boolean isValid() {
		return getId() != null;
	}

	protected CLanguageData doCreateLanguageDataForContentTypes(String languageId,
			String[] typesIds) {
		return fFactory.createLanguageData(fCfg, this, languageId, typesIds, true);
	}

	protected CLanguageData doCreateLanguageDataForExtensions(String languageId,
			String[] extensions) {
		return fFactory.createLanguageData(fCfg, this, languageId, extensions, false);
	}
	
	public CLanguageData createLanguageDataForContentTypes(String languageId,
			String[] typesIds) {
		CLanguageData data = doCreateLanguageDataForContentTypes(languageId, typesIds);
		if(data != null){
			fLanguageDatas.add(data);
			setModified(true);
		}
		return data;
	}

	public CLanguageData createLanguageDataForExtensions(String languageId,
			String[] extensions) {
		CLanguageData data = doCreateLanguageDataForExtensions(languageId, extensions);
		if(data != null){
			fLanguageDatas.add(data);
			setModified(true);
		}
		return data;
	}
	
	public boolean isModified(){
		if(fIsModified)
			return true;
		
		CLanguageData lDatas[] = getLanguageDatas();
		for(int i = 0; i < lDatas.length; i++){
			if(fFactory.isModified(lDatas[i]))
				return true;
		}
		
		return false;
	}
	
	public void setModified(boolean modified){
		fIsModified = modified;

		if(!modified){
			CLanguageData lDatas[] = getLanguageDatas();
			for(int i = 0; i < lDatas.length; i++){
				fFactory.setModified(lDatas[i], false);
			}
		}

	}

	public boolean hasCustomSettings() {
		return false;
	}
}
