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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class ResourceInfo extends BuildObject implements IResourceInfo {
//	private IFolderInfo parentFolderInfo;
//	private String parentFolderInfoId;
	private IConfiguration config;
	private IPath path;
	boolean isDirty;
	private boolean isExcluded;
	boolean needsRebuild;
	private ResourceInfoContainer rcInfo;
	private CResourceData resourceData;
//	private boolean inheritParentInfo;
//	private IToolChain baseToolChain;
//	private String baseToolChainId;

	ResourceInfo(IConfiguration cfg, IManagedConfigElement element, boolean hasBody){
		config = cfg;
		if(hasBody)
			loadFromManifest(element);
	}

	ResourceInfo(IConfiguration cfg, ResourceInfo base, String id) {
		config = cfg;
		path = normalizePath(base.path);
		internalSetExclude(base.isExcluded);

		setId(id);
		setName(base.getName());

//		inheritParentInfo = base.inheritParentInfo;
/*		if(!isRoot()){
			IFolderInfo pfi = base.getParentFolderInfo();
			IResourceInfo pf = null;
			if(pfi != null){
				pf = config.getResourceInfo(pfi.getPath(), true);
			}
			
			if(pf instanceof IFolderInfo)
				this.parentFolderInfo = (IFolderInfo)pf;
			else
				this.parentFolderInfo = config.getRootFolderInfo();
	
	//		if()
			this.parentFolderInfoId = this.parentFolderInfo.getId();
		}
*/
		if(id.equals(base.getId())){
			isDirty = base.isDirty;
			needsRebuild = base.needsRebuild;
		} else {
			needsRebuild = true;
			isDirty = true;
		}
	}
	
	public boolean isRoot(){
		return path.segmentCount() == 0;
	}

	ResourceInfo(IConfiguration cfg, IPath path, String id, String name) {
		config = cfg;
		path = normalizePath(path);
		this.path = path;

//		inheritParentInfo = inherit;
		setId(id);
		setName(name);
	}

	ResourceInfo(IFileInfo base, IPath path, String id, String name) {
		config = base.getParent();

		setId(id);
		setName(name);
		
		path = normalizePath(path);
		
		this.path = path;
		internalSetExclude(base.isExcluded());
//		parentFolderInfoId = base.getId();
//		parentFolderInfo = base;
//		inheritParentInfo = false;
		needsRebuild = true;
		isDirty = true;
	}

	ResourceInfo(FolderInfo base, IPath path, String id, String name) {
		config = base.getParent();

		setId(id);
		setName(name);
		
		path = normalizePath(path);
		
		this.path = path;
		internalSetExclude(base.isExcluded());
//		parentFolderInfoId = base.getId();
//		parentFolderInfo = base;
//		inheritParentInfo = base.getPath().isPrefixOf(path);
		needsRebuild = true;
		isDirty = true;
	}

	ResourceInfo(IConfiguration cfg, ICStorageElement element, boolean hasBody){
		config = cfg;
		if(hasBody)
			loadFromProject(element);
	}
	
	private void loadFromManifest(IManagedConfigElement element) {
	
		// id
		setId(element.getAttribute(ID));
		
		// Get the name
		setName(element.getAttribute(NAME));
		
//		parentFolderInfoId = element.getAttribute(PARENT_FOLDER_INFO_ID);
//		baseToolChainId = element.getAttribute(BASE_TOOLCHAIN_ID);

		// resourcePath
		String tmp = element.getAttribute(RESOURCE_PATH);
		if(tmp != null){
			path = new Path(tmp);
			if(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME.equals(element.getName())){
				path = path.removeFirstSegments(1);
			}
			path = normalizePath(path);
		} else {
			//TODO
		}
		
//		inheritParentInfo = "true".equals(element.getAttribute(INHERIT_PARENT_INFO));

		// exclude
        String excludeStr = element.getAttribute(EXCLUDE);
        if (excludeStr != null){
    		internalSetExclude("true".equals(excludeStr)); //$NON-NLS-1$
        }
	}

	private void loadFromProject(ICStorageElement element) {
		
		// id
		setId(element.getAttribute(ID));

		// name
		if (element.getAttribute(NAME) != null) {
			setName(element.getAttribute(NAME));
		}
		
		// resourcePath
		if (element.getAttribute(RESOURCE_PATH) != null) {
			String tmp = element.getAttribute(RESOURCE_PATH);
			if(tmp != null){
				path = new Path(tmp);
				if(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME.equals(element.getName())){
					path = path.removeFirstSegments(1);
				}
				path = normalizePath(path);
			} else {
				//TODO
			}
		}

		// exclude
		if (element.getAttribute(EXCLUDE) != null) {
			String excludeStr = element.getAttribute(EXCLUDE);
			if (excludeStr != null){
				internalSetExclude("true".equals(excludeStr)); //$NON-NLS-1$
			}
		}

//		inheritParentInfo = "true".equals(element.getAttribute(INHERIT_PARENT_INFO));

//		parentFolderInfoId = element.getAttribute(PARENT_FOLDER_INFO_ID);
//		baseToolChainId = element.getAttribute(BASE_TOOLCHAIN_ID);
	}


	public IConfiguration getParent() {
		return config;
	}

/*	public IFolderInfo getParentFolderInfo() {
		if(parentFolderInfo == null && parentFolderInfoId != null){
			IResourceInfo rcInfo = config.getResourceInfoById(parentFolderInfoId);
			if(rcInfo instanceof IFolderInfo)
				parentFolderInfo = (IFolderInfo)rcInfo;
			else
				parentFolderInfo = config.getRootFolderInfo();
		}
		return parentFolderInfo;
	}
*/	
	public IPath getPath() {
		return normalizePath(path);
	}

	public boolean isDirty() {
		return isDirty;
	}

	public boolean isExcluded() {
		return isExcluded;
	}

	public boolean needsRebuild() {
		return needsRebuild;
	}

	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}

	public void setExclude(boolean excluded) {
		if (isExcluded != internalSetExclude(excluded)) {
			setDirty(true);
			setRebuildState(true);
		}
	}
	
	private boolean internalSetExclude(boolean excluded){
		if(excluded && isRoot())
			return isExcluded;

		return isExcluded = excluded;
	}

	public void setPath(IPath p) {
		p = normalizePath(p);
		if(path == null)
			path = p;
		else if (!p.equals(normalizePath(this.path))) {
			ResourceInfoContainer info = getRcInfo();
			info.changeCurrentPath(p, true);
			this.path = p;
			setDirty(true);
			setRebuildState(true);
		}

	}
	
	private ResourceInfoContainer getRcInfo(){
		if(rcInfo == null)
			rcInfo = ((Configuration)config).getRcInfoContainer(this);
		return rcInfo;
	}

	public void setRebuildState(boolean rebuild) {
		needsRebuild = rebuild;
	}
	
	void serialize(ICStorageElement element){
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}
		
		if (isExcluded) {
			element.setAttribute(IResourceInfo.EXCLUDE, "true"); //$NON-NLS-1$
		}

		if (path != null) {
			element.setAttribute(IResourceInfo.RESOURCE_PATH, path.toString());
		}
		
//		if(parentFolderInfoId != null){
//			element.setAttribute(IResourceInfo.PARENT_FOLDER_INFO_ID, parentFolderInfoId);
//		}

//		if(baseToolChainId != null){
//			element.setAttribute(IResourceInfo.BASE_TOOLCHAIN_ID, baseToolChainId);
//		}
	}

	void resolveReferences(){
//		getParentFolderInfo();
	}
	
//	void setParentFolderId(String id){
//		parentFolderInfoId = id;
//	}
	
//	void setParentFolder(IFolderInfo info){
//		parentFolderInfo = info;
//	}
	
	public CResourceData getResourceData(){
		return resourceData;
	}
	
	protected void setResourceData(CResourceData data){
		resourceData = data;
	}
	
	void removed(){
		config = null;
	}
	
	public boolean isValid(){
		return config != null;
	}

//	public boolean isParentInfoInherited() {
//		return inheritParentInfo;
//	}
	
	public IOption setOption(IHoldsOptions parent, IOption option, boolean value) throws BuildException {
		// Is there a change?
		IOption retOpt = option;
		boolean oldVal = option.getBooleanValue();
		if (oldVal != value) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
//			if(resourceData != null)
//				((ISettingsChangeListener)resourceData).optionChanged(this, parent, option, new Boolean(oldVal));
			NotificationManager.getInstance().optionChanged(this, parent, option, new Boolean(oldVal));
//			rebuildNeeded = true;
		}
		return retOpt;
	}

	public IOption setOption(IHoldsOptions parent, IOption option, String value) throws BuildException {
		IOption retOpt = option;
		String oldValue;
		oldValue = option.getStringValue(); 
		if (oldValue != null && !oldValue.equals(value)) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
//			if(resourceData != null)
//				((ISettingsChangeListener)resourceData).optionChanged(this, parent, option, oldValue);
			NotificationManager.getInstance().optionChanged(this, parent, option, oldValue);
//			rebuildNeeded = true;
		}
		return retOpt;
	}

	public IOption setOption(IHoldsOptions parent, IOption option, String[] value) throws BuildException {
		IOption retOpt = option;
		// Is there a change?
		String[] oldValue;
		switch (option.getBasicValueType()) {
			case IOption.STRING_LIST :
				oldValue = option.getBasicStringListValue();
				break;
//			case IOption.STRING_LIST :
//				oldValue = option.getStringListValue();
//				break;
//			case IOption.INCLUDE_PATH :
//				oldValue = option.getIncludePaths();
//				break;
//			case IOption.PREPROCESSOR_SYMBOLS :
//				oldValue = option.getDefinedSymbols();
//				break;
//			case IOption.LIBRARIES :
//				oldValue = option.getLibraries();
//				break;
//			case IOption.OBJECTS :
//				oldValue = option.getUserObjects();
//				break;
			default :
				oldValue = new String[0];
				break;
		}
		if(!Arrays.equals(value, oldValue)) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
//			if(resourceData != null)
//				((ISettingsChangeListener)resourceData).optionChanged(this, parent, option, oldValue);
			NotificationManager.getInstance().optionChanged(this, parent, option, oldValue);
//			rebuildNeeded = true;
		} 
		return retOpt;
	}
	
	public void propertiesChanged(){
		if(isExtensionElement())
			return;
		
		ITool tools[] = getTools();
		for(int i = 0; i < tools.length; i++){
			((Tool)tools[i]).propertiesChanged();
		}
	}
	
	public abstract boolean isExtensionElement();
	
	public abstract Set contributeErrorParsers(Set set);
	
	protected Set contributeErrorParsers(ITool[] tools, Set set){
		if(set == null)
			set = new HashSet();
		for(int i = 0; i < tools.length; i++){
			Tool tool = (Tool)tools[i];
			tool.contributeErrorParsers(set);
		}
		return set;
	}
	
	public abstract void resetErrorParsers();
	
	protected void resetErrorParsers(ITool tools[]){
		for(int i = 0; i < tools.length; i++){
			Tool tool = (Tool)tools[i];
			tool.resetErrorParsers();
		}
	}
	
	abstract void removeErrorParsers(Set set);
	
	protected void removeErrorParsers(ITool tools[], Set set){
		for(int i = 0; i < tools.length; i++){
			Tool tool = (Tool)tools[i];
			tool.removeErrorParsers(set);
		}
	}
	
	public ITool getToolById(String id) {
		ITool[] tools = getTools();
		for(int i = 0; i < tools.length; i++){
			if(id.equals(tools[i].getId()))
				return tools[i];
		}
		return null;
	}
	
	public static IPath normalizePath(IPath path){
		return path.makeRelative();
	}

	abstract void resolveProjectReferences(boolean onLoad);

}
