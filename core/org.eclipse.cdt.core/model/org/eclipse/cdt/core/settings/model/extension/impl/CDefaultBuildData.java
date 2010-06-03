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
package org.eclipse.cdt.core.settings.model.extension.impl;

import java.util.Arrays;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.IPath;

public class CDefaultBuildData extends CBuildData {
	protected IPath fCWD;
	protected String[] fErrorParserIDs;
	protected ICOutputEntry fOutputEntries[];
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
	protected static final ICOutputEntry[] EMPTY_OUTPUT_ENTRIES_ARRAY = new ICOutputEntry[0];
	protected String fName;
	protected String fId;
	protected boolean fIsModified;
//	protected CConfigurationData fCfg;
//	private CDataFacroty fFactory;


//	public CDefaultBuildData(CConfigurationData cfg, CDataFacroty factory) {
//		fCfg = cfg;
//		if(factory == null)
//			factory = new CDataFacroty();
//		fFactory = factory;
//	}
	
	public CDefaultBuildData(){
		
	}
	
	public CDefaultBuildData(String id, CBuildData base) {
		fId = id;
		
		copySettingsFrom(base);
	}
	
	protected void copySettingsFrom(CBuildData data){
		if(data != null){
			fName = data.getName();
			fCWD = data.getBuilderCWD();
			fErrorParserIDs = data.getErrorParserIDs();
			fOutputEntries = data.getOutputDirectories();
		}
	}

	@Override
	public IPath getBuilderCWD() {
		return fCWD;
	}

	@Override
	public String[] getErrorParserIDs() {
		if(fErrorParserIDs != null && fErrorParserIDs.length != 0)
			return fErrorParserIDs.clone();
		return EMPTY_STRING_ARRAY;
	}

	@Override
	public ICOutputEntry[] getOutputDirectories() {
		if(fOutputEntries != null && fOutputEntries.length != 0)
			return fOutputEntries.clone();
		return EMPTY_OUTPUT_ENTRIES_ARRAY;
	}

	@Override
	public void setBuilderCWD(IPath path) {
		if(CDataUtil.objectsEqual(path, fCWD))
			return;
		
		fCWD = path;
		
		setModified(true);
	}

	@Override
	public void setErrorParserIDs(String[] ids) {
		if(Arrays.equals(ids, fErrorParserIDs))
			return;
		if(ids != null && ids.length != 0)
			fErrorParserIDs = ids.clone();
		else
			fErrorParserIDs = ids;
		
		setModified(true);
	}

	@Override
	public void setOutputDirectories(ICOutputEntry[] entries) {
		if(Arrays.equals(entries, fOutputEntries))
			return;
		
		if(entries != null && entries.length != 0)
			fOutputEntries = entries.clone();
		else
			fOutputEntries = entries; 

		setModified(true);
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public boolean isValid() {
		return getId() != null;
	}

	@Override
	public IEnvironmentContributor getBuildEnvironmentContributor() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isModified(){
		return fIsModified;
	}
	
	public void setModified(boolean modified){
		fIsModified = modified;
	}
}
