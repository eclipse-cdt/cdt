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
//	protected boolean fIsExcluded;
	protected List<CLanguageData> fLanguageDatas = new ArrayList<CLanguageData>();
	protected String fName;
	protected String fId;
	protected CConfigurationData fCfg;
	private CDataFactory fFactory;
	protected boolean fIsModified;

	public CDefaultFolderData(CConfigurationData cfg, CDataFactory factory) {
		fCfg = cfg;
		if(factory == null)
			factory = new CDataFactory();
		fFactory = factory;
	}

	public CDefaultFolderData(String id, IPath path, CConfigurationData cfg, CDataFactory factory) {
		this(cfg, factory);
		fId = id;
		fName = path.toString();
		fPath = path;
	}

	public CDefaultFolderData(String id, IPath path, CFolderData base, CConfigurationData cfg, CDataFactory factory, boolean clone) {
		this(id, path, cfg, factory);
		
		copyDataFrom(base, clone);
	}
	
	protected void copyDataFrom(CFolderData base, boolean clone){
		if(base != null){
			CLanguageData lDatas[] = base.getLanguageDatas();
			for (CLanguageData lData : lDatas) {
				fLanguageDatas.add(copyLanguageData(lData, clone));
			}
			
//			fIsExcluded = base.isExcluded();
		}
	}

	protected CLanguageData copyLanguageData(CLanguageData base, boolean clone){
		return fFactory.createLanguageData(fCfg, this, base, null, clone);
	}
	
	@Override
	public CLanguageData[] getLanguageDatas() {
		return fLanguageDatas.toArray(new CLanguageData[fLanguageDatas.size()]);
	}

	@Override
	public IPath getPath() {
		return fPath;
	}

//	public boolean isExcluded() {
//		return fIsExcluded;
//	}
//
//	public void setExcluded(boolean excluded) {
//		if(excluded == fIsExcluded)
//			return;
//
//		fIsExcluded = excluded;
//		setModified(true);
//	}

	@Override
	public void setPath(IPath path) {
		if(CDataUtil.objectsEqual(path, fPath))
			return;
		
		fPath = path;
		setModified(true);
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public boolean isValid() {
		return getId() != null;
	}

//	protected CLanguageData doCreateLanguageDataForContentTypes(String languageId,
//			String[] typesIds) {
//		return fFactory.createLanguageData(fCfg, this, null, null, languageId, typesIds, true);
//	}
//
//	protected CLanguageData doCreateLanguageDataForExtensions(String languageId,
//			String[] extensions) {
//		return fFactory.createLanguageData(fCfg, this, null, null, languageId, extensions, false);
//	}
	
	@Override
	public CLanguageData createLanguageDataForContentTypes(String languageId,
			String[] typesIds) {
		throw new UnsupportedOperationException();
//		CLanguageData data = doCreateLanguageDataForContentTypes(languageId, typesIds);
//		if(data != null){
//			fLanguageDatas.add(data);
//			setModified(true);
//		}
//		return data;
	}

	@Override
	public CLanguageData createLanguageDataForExtensions(String languageId,
			String[] extensions) {
		throw new UnsupportedOperationException();
//		CLanguageData data = doCreateLanguageDataForExtensions(languageId, extensions);
//		if(data != null){
//			fLanguageDatas.add(data);
//			setModified(true);
//		}
//		return data;
	}
	
	public boolean isModified(){
		if(fIsModified)
			return true;
		
		CLanguageData lDatas[] = getLanguageDatas();
		for (CLanguageData lData : lDatas) {
			if(fFactory.isModified(lData))
				return true;
		}
		
		return false;
	}
	
	public void setModified(boolean modified){
		fIsModified = modified;

		if(!modified){
			CLanguageData lDatas[] = getLanguageDatas();
			for (CLanguageData lData : lDatas) {
				fFactory.setModified(lData, false);
			}
		}

	}

	@Override
	public boolean hasCustomSettings() {
		return false;
	}
}
