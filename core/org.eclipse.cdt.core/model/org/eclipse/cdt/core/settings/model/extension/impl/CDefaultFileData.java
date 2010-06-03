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

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.core.runtime.IPath;

public class CDefaultFileData extends CFileData {
	protected IPath fPath;
//	protected boolean fIsExcluded;
	protected String fName;
	protected String fId;
	protected CLanguageData fLanguageData;
	protected CConfigurationData fCfg;
	private CDataFactory fFactory;
	protected boolean fIsModified;

	public CDefaultFileData(CConfigurationData cfg, CDataFactory factory) {
		fCfg = cfg;
		if(factory == null)
			factory = new CDataFactory();
		fFactory = factory;
	}

	public CDefaultFileData(String id, IPath path, CConfigurationData cfg, CDataFactory factory) {
		this(cfg, factory);
		fId = id;
		fName = path.toString();
		fPath = path;
	}

	public CDefaultFileData(String id, IPath path, CFileData base, CConfigurationData cfg, CDataFactory factory, boolean clone) {
		this(id, path, cfg, factory);
		
		copyDataFrom(base, clone);
	}

	public CDefaultFileData(String id, IPath path, CFolderData base, CLanguageData baseLangData, CConfigurationData cfg, CDataFactory factory) {
		this(id, path, cfg, factory);
		
		copyDataFrom(base, baseLangData);
	}

	protected void copyDataFrom(CFileData base, boolean clone){
		CLanguageData baseLanguageData = base.getLanguageData();
		if(baseLanguageData != null)
			fLanguageData = copyLanguageData(baseLanguageData, clone);

//		fIsExcluded = base.isExcluded();
	}

	protected void copyDataFrom(CFolderData base, CLanguageData baseLanguageData){
//		fIsExcluded = base != null ? base.isExcluded() : false;
		if(baseLanguageData != null)
			fLanguageData = copyLanguageData(baseLanguageData, false);
	}

	protected CLanguageData copyLanguageData(CLanguageData data, boolean clone){
		return fFactory.createLanguageData(fCfg, this, data, null, clone);
	}


	@Override
	public IPath getPath() {
		return fPath;
	}

//	public boolean isExcluded() {
//		return fIsExcluded;
//	}

//	public void setExcluded(boolean excluded) {
//		if(excluded == fIsExcluded)
//			return;
//		
//		fIsExcluded = excluded;
//	}

	@Override
	public void setPath(IPath path) {
		fPath = path;
	}

	@Override
	public String getName() {
		return fName;
	}

	public void setName(String name) {
		fName = name;
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public boolean isValid() {
		return getId() != null;
	}

	@Override
	public CLanguageData getLanguageData() {
		return fLanguageData;
	}
	
	public boolean isModified(){
		if(fIsModified)
			return true;
		
		return fFactory.isModified(fLanguageData);
	}
	
	public void setModified(boolean modified){
		fIsModified = modified;
		
		if(!modified)
			fFactory.setModified(fLanguageData, false);
	}

	@Override
	public boolean hasCustomSettings() {
		return false;
	}

}
