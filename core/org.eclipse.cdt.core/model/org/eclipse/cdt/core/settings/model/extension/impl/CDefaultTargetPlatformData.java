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

import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;

public class CDefaultTargetPlatformData extends CTargetPlatformData {
	protected String fName;
	protected String fId;
	protected String[] fBinaryParserIds;
//	protected CConfigurationData fCfg;
//	private CDataFacroty fFactory;
	protected boolean fIsModified;

//	public CDefaultTargetPlatformData(CConfigurationData cfg, CDataFacroty factory) {
//		fCfg = cfg;
//		if(factory == null)
//			factory = new CDataFacroty();
//		fFactory = factory;
//	}

	protected CDefaultTargetPlatformData(){
		
	}
	
	public CDefaultTargetPlatformData(String id, String name) {
		fId = id;
		fName = name;
	}

	public CDefaultTargetPlatformData(String id, CTargetPlatformData base) {
		fId = id;
		
		copyDataFrom(base);
	}
	
	protected void copyDataFrom(CTargetPlatformData base){
		if(base != null){
			fName = base.getName();
	
			fBinaryParserIds = base.getBinaryParserIds();
		}
	}

	@Override
	public String[] getBinaryParserIds() {
		if(fBinaryParserIds != null)
			return fBinaryParserIds.clone();
		return new String[0];
	}

	@Override
	public void setBinaryParserIds(String[] ids) {
		if(Arrays.equals(ids, fBinaryParserIds))
			return;
		
		if(ids != null)
			fBinaryParserIds = ids.clone();
		else
			fBinaryParserIds = null;
		
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

	public boolean isModified(){
		return fIsModified;
	}
	
	public void setModified(boolean modified){
		fIsModified = modified;
	}

}
