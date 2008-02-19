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
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class ResourceInfo extends BuildObject implements IResourceInfo {
//	private IFolderInfo parentFolderInfo;
//	private String parentFolderInfoId;
	private Configuration config;
	private IPath path;
	boolean isDirty;
//	private boolean isExcluded;
	boolean needsRebuild;
	private ResourceInfoContainer rcInfo;
	private CResourceData resourceData;
//	private boolean inheritParentInfo;
//	private IToolChain baseToolChain;
//	private String baseToolChainId;

	ResourceInfo(IConfiguration cfg, IManagedConfigElement element, boolean hasBody){
		config = (Configuration)cfg;
		if(hasBody)
			loadFromManifest(element);
	}

	ResourceInfo(IConfiguration cfg, ResourceInfo base, String id) {
		config = (Configuration)cfg;
		path = normalizePath(base.path);
//		internalSetExclude(base.isExcluded);

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
		config = (Configuration)cfg;
		path = normalizePath(path);
		this.path = path;

//		inheritParentInfo = inherit;
		setId(id);
		setName(name);
	}

	ResourceInfo(IFileInfo base, IPath path, String id, String name) {
		config = (Configuration)base.getParent();

		setId(id);
		setName(name);
		
		path = normalizePath(path);
		
		this.path = path;
//		internalSetExclude(base.isExcluded());
//		parentFolderInfoId = base.getId();
//		parentFolderInfo = base;
//		inheritParentInfo = false;
		needsRebuild = true;
		isDirty = true;
	}

	ResourceInfo(FolderInfo base, IPath path, String id, String name) {
		config = (Configuration)base.getParent();

		setId(id);
		setName(name);
		
		path = normalizePath(path);
		
		this.path = path;
//		internalSetExclude(base.isExcluded());
//		parentFolderInfoId = base.getId();
//		parentFolderInfo = base;
//		inheritParentInfo = base.getPath().isPrefixOf(path);
		needsRebuild = true;
		isDirty = true;
	}

	ResourceInfo(IConfiguration cfg, ICStorageElement element, boolean hasBody){
		config = (Configuration)cfg;
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
    		config.setExcluded(getPath(), isFolderInfo(), ("true".equals(excludeStr))); //$NON-NLS-1$
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
	    		config.setExcluded(getPath(), isFolderInfo(), ("true".equals(excludeStr))); //$NON-NLS-1$
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
		return config.isExcluded(getPath());
	}

	public boolean needsRebuild() {
		return needsRebuild;
	}

	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}

	public void setExclude(boolean excluded) {
		if(isExcluded() == excluded)
			return;
		
		config.setExcluded(getPath(), isFolderInfo(), excluded);
		
		setDirty(true);
		setRebuildState(true);
	}
	
	public boolean canExclude(boolean exclude) {
		return config.canExclude(getPath(), isFolderInfo(), exclude);
	}

	public abstract boolean isFolderInfo();
	
//	private boolean internalSetExclude(boolean excluded){
////		if(excluded/* && isRoot()*/)
////			return isExcluded;
//		return isExcluded = excluded;
//	}

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
		
//		if (isExcluded) {
//			element.setAttribute(IResourceInfo.EXCLUDE, "true"); //$NON-NLS-1$
//		}

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

	private void propagate(IHoldsOptions parent, IOption option, Object oldValue, Object value) {                            
		if (! (parent instanceof ITool))                                                                                 
			return;                                                                                                  
		ITool tool = (ITool)parent;                                                                                      
		String sup = option.getId();                                                                                     
		IOption op = option;                                                                                             
		while (op.getSuperClass() != null) {                                                                             
			op = op.getSuperClass();                                                                                 
			sup = op.getId();                                                                                        
		}                      
		IResourceInfo[] ris = getChildResourceInfos();
		for (int i=0; i<ris.length; i++) {
			IResourceInfo ri = ris[i];
			ITool[] ts = ri.getTools();                      
			for (int j=0; j<ts.length; j++ ) {
				ITool t = ts[j];
				if (t.getDefaultInputExtension() != tool.getDefaultInputExtension())                             
					continue;                                                                                
				op = t.getOptionBySuperClassId(sup);                                                             
				if (op == null)                                                                                  
					continue;                                                                                
				try {                                                                                            
					if (value instanceof Boolean) {                                                          
						boolean b = ((Boolean)oldValue).booleanValue();                                  
						if (b == op.getBooleanValue() && b != ((Boolean)value).booleanValue())           
							ri.setOption(t, op, ((Boolean)value).booleanValue());                    
					} else if (value instanceof String) {                                                    
						String s = (String)oldValue;                                                     
						if (s.equals(op.getStringValue()) && ! s.equals((String)value))                  
							ri.setOption(t, op, (String)value);                                      
					} else if (value instanceof String[]) {                                                  
						String[] s = (String[])oldValue;                                                 
						if (Arrays.equals(s, op.getStringListValue()) &&                                 
								! Arrays.equals(s, (String[])value))                             
							ri.setOption(t, op, (String[])value);                                    
					} else if (value instanceof OptionStringValue[]) {                                       
						OptionStringValue[] s = (OptionStringValue[])oldValue;                           
						if (Arrays.equals(s, op.getBasicStringListValueElements()) &&                    
								! Arrays.equals(s, (OptionStringValue[])value))                  
							ri.setOption(t, op, (OptionStringValue[])value);                         
					}                                                                                        
					break;                                                                                   
				} catch (BuildException e) {}                                                                    
			}                                                                                                        
		}                                                                                                                
		                                                                                                                 
	}                                                                                                                        
	
	public IOption setOption(IHoldsOptions parent, IOption option, boolean value) throws BuildException {
		// Is there a change?
		IOption retOpt = option;
		boolean oldVal = option.getBooleanValue();
		if (oldVal != value) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
			propagate(parent, option,                                
					(oldVal ? Boolean.TRUE : Boolean.FALSE), 
					(value  ? Boolean.TRUE : Boolean.FALSE));
			NotificationManager.getInstance().optionChanged(this, parent, option, new Boolean(oldVal));
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
			propagate(parent, option, oldValue, value);
			NotificationManager.getInstance().optionChanged(this, parent, option, oldValue);
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
			default :
				oldValue = new String[0];
				break;
		}
		if(!Arrays.equals(value, oldValue)) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
			propagate(parent, option, oldValue, value);
			NotificationManager.getInstance().optionChanged(this, parent, option, oldValue);
		} 
		return retOpt;
	}
	
	public IOption setOption(IHoldsOptions parent, IOption option, OptionStringValue[] value) throws BuildException {
		IOption retOpt = option;
		// Is there a change?
		OptionStringValue[] oldValue;
		switch (option.getBasicValueType()) {
			case IOption.STRING_LIST :
				oldValue = ((Option)option).getBasicStringListValueElements();
				break;
			default :
				oldValue = new OptionStringValue[0];
				break;
		}
		if(!Arrays.equals(value, oldValue)) {
			retOpt = parent.getOptionToSet(option, false);
			((Option)retOpt).setValue(value);
			propagate(parent, option, oldValue, value);
			NotificationManager.getInstance().optionChanged(this, parent, option, oldValue);
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
//		if(set == null)
//			set = new HashSet();
		for(int i = 0; i < tools.length; i++){
			Tool tool = (Tool)tools[i];
			set = tool.contributeErrorParsers(set);
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

	public ResourceInfo getParentResourceInfo(){
		if(isRoot())
			return null;
		
		IPath path = getPath();
		path = path.removeLastSegments(1);
		return (ResourceInfo)getParent().getResourceInfo(path, false);
	}

	public IFolderInfo getParentFolderInfo(){
		ResourceInfo parentRc = getParentResourceInfo();
		for(; parentRc != null && !parentRc.isFolderInfo(); parentRc = parentRc.getParentResourceInfo());

		return (IFolderInfo)parentRc;
	}

	abstract void resolveProjectReferences(boolean onLoad);

	abstract public boolean hasCustomSettings();
	
	public ToolListModificationInfo getToolListModificationInfo(ITool[] tools) {
		ITool[] curTools = getTools();
		return ToolChainModificationHelper.getModificationInfo(this, curTools, tools);
	}

	static ITool[][] getRealPairs(ITool[] tools){
		ITool[][] pairs = new ITool[tools.length][];
		for(int i = 0; i < tools.length; i++){
			ITool[] pair = new ITool[2];
			pair[0] = ManagedBuildManager.getRealTool(tools[i]);
			if(pair[0] == null)
				pair[0] = tools[i];
			pair[1] = tools[i];
			pairs[i] = pair;
		}
		return pairs;
	}
	
	abstract void applyToolsInternal(ITool[] resultingTools, ToolListModificationInfo info);

	void doApply(ToolListModificationInfo info){
		ITool[] resulting = info.getResultingTools();
		
		ITool[] removed = info.getRemovedTools();
		
		BuildSettingsUtil.disconnectDepentents(getParent(), removed);
		
		applyToolsInternal(resulting, info);

		performPostModificationAdjustments(info);
	}
	
	void performPostModificationAdjustments(ToolListModificationInfo info){
		propertiesChanged();
	}
	
	public IResourceInfo[] getDirectChildResourceInfos(){
		ResourceInfoContainer cr = getRcInfo();
		return cr.getDirectChildResourceInfos();
	}
	
	public IResourceInfo[] getChildResourceInfos(){
		ResourceInfoContainer cr = getRcInfo();
		return cr.getResourceInfos();
	}
	
	public List getChildResourceInfoList(boolean includeCurrent){
		return getRcInfo().getRcInfoList(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER, includeCurrent);
	}
}
