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
import java.util.HashMap;

import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class CDefaultConfigurationData extends CConfigurationData {
	protected String fDescription;
	private HashMap<IPath, CResourceData> fResourceDataMap = new HashMap<IPath, CResourceData>();
	protected CFolderData fRootFolderData; 
	protected String fName;
	protected String fId;
	protected CTargetPlatformData fTargetPlatformData;
	protected CBuildData fBuildData;
	protected ICSourceEntry[] fSourceEntries;
	private CDataFactory fFactory;
	protected boolean fIsModified;
	private CConfigurationStatus fStatus = CConfigurationStatus.CFG_STATUS_OK;
	
	protected CDefaultConfigurationData(CDataFactory factory){
		if(factory == null)
			factory = new CDataFactory();
		fFactory = factory;
	}

//	public CDefaultConfigurationData(String id, String name) {
//		this(id, name, (CDataFacroty)null);
//	}

	public CDataFactory getFactory(){
		return fFactory;
	}

	public CDefaultConfigurationData(String id, String name, CDataFactory factory) {
		fId = id;
		fName = name;
		if(factory == null)
			factory = new CDataFactory();
		fFactory = factory;
	}

//	public CDefaultConfigurationData(String id, String name, CConfigurationData base) {
//		this(id, name, base, null);
//	}

	public CDefaultConfigurationData(String id, String name, CConfigurationData base, CDataFactory facroty, boolean clone) {
		this(id, name, facroty);

		copySettingsFrom(base, clone);
	}

	protected IPath standardizePath(IPath path){
		return path.makeRelative().setDevice(null);
	}
	
	protected void addRcData(CResourceData data){
		IPath path = standardizePath(data.getPath());
		if(path.segmentCount() == 0){
			if(data.getType() == ICSettingBase.SETTING_FOLDER)
				fRootFolderData = (CFolderData)data;
			else
				return;
		}
		fResourceDataMap.put(path, data);
	}
	
	protected void removeRcData(IPath path){
		path = standardizePath(path);
		fResourceDataMap.remove(path);
		if(path.segmentCount() == 0)
			fRootFolderData = null;
	}
	
	protected void copySettingsFrom(CConfigurationData base, boolean clone){
		if(base == null)
			return;
		fDescription = base.getDescription();
		
		fTargetPlatformData = copyTargetPlatformData(base.getTargetPlatformData(), clone);
		fSourceEntries = base.getSourceEntries();
		fBuildData = copyBuildData(base.getBuildData(), clone);
		
		fStatus = base.getStatus();
		if(fStatus == null)
			fStatus = CConfigurationStatus.CFG_STATUS_OK;
		
		CFolderData baseRootFolderData = base.getRootFolderData();
		fRootFolderData = copyFolderData(baseRootFolderData.getPath(), baseRootFolderData, clone);
		addRcData(fRootFolderData);

		CResourceData[] rcDatas = filterRcDatasToCopy(base);

		for(int i = 0; i < rcDatas.length; i++){
			CResourceData rcData = rcDatas[i];
			if(baseRootFolderData == rcData)
				continue;
			
			if(rcData instanceof CFolderData)
				addRcData(copyFolderData(rcData.getPath(), (CFolderData)rcData, clone));
			else if(rcData instanceof CFileData)
				addRcData(copyFileData(rcData.getPath(), (CFileData)rcData, clone));
		}
	}
	
	protected CResourceData[] filterRcDatasToCopy(CConfigurationData base){
		return base.getResourceDatas();
	}

	protected CFolderData copyFolderData(IPath path, CFolderData base, boolean clone){
		return fFactory.createFolderData(this, base, null, clone, path);
	}
	
	

	protected CFileData copyFileData(IPath path, CFileData base, boolean clone){
		return fFactory.createFileData(this, base, null, null, clone, path);
	}

	protected CFileData copyFileData(IPath path, CFolderData base, CLanguageData langData){
		return fFactory.createFileData(this, base, langData, null, false, path);
	}

	protected CTargetPlatformData copyTargetPlatformData(CTargetPlatformData base, boolean clone){
		return fFactory.createTargetPlatformData(this, base, null, base != null ? base.getName() : null, clone);
	}
	
	protected CBuildData copyBuildData(CBuildData data, boolean clone){
		return fFactory.createBuildData(this, data, null, data != null ? data.getName() : null, clone);
	}

	@Override
	public CFolderData createFolderData(IPath path, CFolderData base) throws CoreException{
		CFolderData data = copyFolderData(path, base, false);
		addRcData(data);
		
		setModified(true);

		return data;
	}

	@Override
	public CFileData createFileData(IPath path, CFileData base) throws CoreException{
		CFileData data = copyFileData(path, base, false);
		addRcData(data);
		
		setModified(true);

		return data;
	}

	@Override
	public CFileData createFileData(IPath path, CFolderData base, CLanguageData langData) throws CoreException{
		CFileData data = copyFileData(path, base, langData);
		addRcData(data);
		
		setModified(true);

		return data;
	}

	@Override
	public String getDescription() {
		return fDescription;
	}
	
	@Override
	public void setDescription(String description) {
		if(CDataUtil.objectsEqual(description, fDescription))
			return;
		fDescription = description;
		
		setModified(true);
	}

	@Override
	public CResourceData[] getResourceDatas() {
		return fResourceDataMap.values().toArray(new CResourceData[fResourceDataMap.size()]);
	}

	@Override
	public CFolderData getRootFolderData() {
		return fRootFolderData;
	}
	
	public CFolderData createRootFolderData() throws CoreException{
		if(fRootFolderData == null){
			createFolderData(new Path(""), null);	//$NON-NLS-1$
		}
		return fRootFolderData;
	}

	@Override
	public void removeResourceData(CResourceData data) throws CoreException {
		if(data == getResourceData(data.getPath())){
			IPath path = standardizePath(data.getPath());
			removeRcData(path);
			
			setModified(true);
		}
	}
	
	public CResourceData getResourceData(IPath path){
		path = standardizePath(path);
		return fResourceDataMap.get(path);
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public void setName(String name) {
		if(CDataUtil.objectsEqual(name, fName))
			return;
		fName = name;
		setModified(true);
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
	public CTargetPlatformData getTargetPlatformData() {
		return fTargetPlatformData;
	}

	@Override
	public ICSourceEntry[] getSourceEntries() {
		return fSourceEntries != null ? (ICSourceEntry[])fSourceEntries.clone() : null;
	}

	@Override
	public void setSourceEntries(ICSourceEntry[] entries) {
		if(Arrays.equals(entries, fSourceEntries))
			return;
		
		fSourceEntries = entries != null ? (ICSourceEntry[])entries.clone() : null;
		setModified(true);
	}

	@Override
	public CBuildData getBuildData() {
		return fBuildData;
	}

	public void initEmptyData() throws CoreException{
		if(getRootFolderData() == null)
			createRootFolderData();
		
		if(getTargetPlatformData() == null)
			createTargetPlatformData();
		
		if(getBuildData() == null)
			createBuildData();
	}
	
	public CTargetPlatformData createTargetPlatformData(){
		fTargetPlatformData = copyTargetPlatformData(null, false);
		setModified(true);
		return fTargetPlatformData;
	}

	public CBuildData createBuildData(){
		fBuildData = copyBuildData(null, false);
		setModified(true);
		return fBuildData;
	}

	@Override
	public ICdtVariablesContributor getBuildVariablesContributor() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isModified(){
		if(fIsModified)
			return true;
		
		CResourceData[] rcDatas = getResourceDatas();
		for(int i = 0; i < rcDatas.length; i++){
			if(fFactory.isModified(rcDatas[i]))
				return true;
		}
		
		return false;
	}
	
	public void setModified(boolean modified){
		fIsModified = modified;

		if(!modified){
			CResourceData[] rcDatas = getResourceDatas();
			for(int i = 0; i < rcDatas.length; i++){
				fFactory.setModified(rcDatas[i], false);
			}
		}
	}

	@Override
	public CConfigurationStatus getStatus() {
		return fStatus;
	}
}
