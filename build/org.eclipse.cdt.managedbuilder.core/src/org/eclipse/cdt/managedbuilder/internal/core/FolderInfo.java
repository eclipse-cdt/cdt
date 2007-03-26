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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IBuildPropertiesRestriction;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.buildproperties.BuildPropertyManager;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildFolderData;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildLanguageData;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ConfigurationDataProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class FolderInfo extends ResourceInfo implements IFolderInfo {
	private ToolChain toolChain;
	private boolean isExtensionElement;

	public FolderInfo(FolderInfo folderInfo, String id, String resourceName, IPath path){
		super(folderInfo, path, id, resourceName);
//		setParentFolder(folderInfo);
//		setParentFolderId(folderInfo.getId());
		
		isExtensionElement = folderInfo.isExtensionElement();
		if(!isExtensionElement)
			setResourceData(new BuildFolderData(this));
	
		if ( folderInfo.getParent() != null)
			setManagedBuildRevision(folderInfo.getParent().getManagedBuildRevision());
		
		IToolChain parTc = folderInfo.getToolChain();
		IToolChain extTc = parTc;
		for(; extTc != null && !extTc.isExtensionElement(); extTc = extTc.getSuperClass());
		if(extTc == null)
			extTc = parTc;
		
		String tcId = ManagedBuildManager.calculateChildId(extTc.getId(), null);
		createToolChain(extTc, tcId, parTc.getName(), false);
		
		toolChain.createOptions(parTc);
		
		ITool tools[] = parTc.getTools();
		String subId = new String();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			ITool extTool = tool;
			for(; extTool != null && !extTool.isExtensionElement(); extTool = extTool.getSuperClass());
			if(extTool == null)
				extTool = tool;
				
			subId = ManagedBuildManager.calculateChildId(extTool.getId(), null);				
			toolChain.createTool(tool, subId, tool.getName(), false);
		}

		
		setDirty(true);
		setRebuildState(true);
	}

	public FolderInfo(IConfiguration parent, IManagedConfigElement element, String managedBuildRevision, boolean hasBody) {
		super(parent, element, hasBody);
		
		isExtensionElement = true;
		IManagedConfigElement tcEl = null;
		if(!hasBody){
			setPath(Path.ROOT);
			setId(ManagedBuildManager.calculateChildId(parent.getId(), null));
			setName("/"); //$NON-NLS-1$
			tcEl = element;
		} else {
			IManagedConfigElement children[] = element.getChildren(IToolChain.TOOL_CHAIN_ELEMENT_NAME);
			if(children.length > 0)
				tcEl = children[0];
		}
		
		if(tcEl != null)
			toolChain = new ToolChain(this, tcEl, managedBuildRevision);
		
	}
	
	public FolderInfo(IConfiguration parent, ICStorageElement element, String managedBuildRevision, boolean hasBody) {
		super(parent, element, hasBody);

		isExtensionElement = false;
		setResourceData(new BuildFolderData(this));
		ICStorageElement tcEl = null;
		if(!hasBody){
			setPath(Path.ROOT);
			setId(ManagedBuildManager.calculateChildId(parent.getId(), null));
			setName("/"); //$NON-NLS-1$
			tcEl = element;
		} else {
			ICStorageElement nodes[] = element.getChildren();
			for(int i = 0; i < nodes.length; i++){
				ICStorageElement node = nodes[i];
				if(IToolChain.TOOL_CHAIN_ELEMENT_NAME.equals(node.getName()))
					tcEl = node;
			}
		}
		
		if(tcEl != null)
			toolChain = new ToolChain(this, tcEl, managedBuildRevision);
	}
/*TODO
	public FolderInfo(FolderInfo base, IPath path, String id, String name) {
		super(base, path, id, name);
	}
*/	
	public FolderInfo(IConfiguration parent, IPath path, String id, String name, boolean isExtensionElement) {
		super(parent, path, id, name);
		
		this.isExtensionElement = isExtensionElement;
		if(!isExtensionElement)
			setResourceData(new BuildFolderData(this));

	}
	
/*	public IToolChain setToolChain(IToolChain toolChain, String id, String name){
		if
	}
*/
	public FolderInfo(IConfiguration cfg, FolderInfo cloneInfo, String id, Map superIdMap, boolean cloneChildren) {
		super(cfg, cloneInfo, id);
		
		isExtensionElement = cfg.isExtensionElement();
		if(!isExtensionElement)
			setResourceData(new BuildFolderData(this));

//		String subId;
		String subName;
		if(!cloneInfo.isExtensionElement)
			cloneChildren = true;
		
		boolean copyIds = cloneChildren && id.equals(cloneInfo.id);
		
		IToolChain cloneToolChain = cloneInfo.getToolChain();
		IToolChain extToolChain = cloneToolChain;
		for(; !extToolChain.isExtensionElement(); extToolChain = extToolChain.getSuperClass());
		
//		if(cloneInfo.isParentInfoInherited()){
//			IFolderInfo parent = (IFolderInfo)cfg.getResourceInfo(cloneInfo.getPath().removeLastSegments(1), false);
//			baseToolChain = parent.getToolChain();
//		} else {
//			for(baseToolChain = cloneInfo.getToolChain();
//				!baseToolChain.isExtensionElement();
//				baseToolChain=baseToolChain.getSuperClass());
//		}

		//getParentFolderInfo() != null ?
		//		cloneInfo.getParentFolderInfo().getToolChain() : cloneInfo.getToolChain();

		subName = cloneToolChain.getName();
		
		if (cloneChildren) {
			String subId = copyIds ? cloneToolChain.getId() : ManagedBuildManager.calculateChildId(extToolChain.getId(),
					null);
		    toolChain = new ToolChain(this, subId, subName, superIdMap, (ToolChain)cloneToolChain);
		    
		} else {
			// Add a tool-chain element that specifies as its superClass the 
			// tool-chain that is the child of the configuration.
//			ToolChain superChain = (ToolChain)cloneInfo.getToolChain();
			String subId = ManagedBuildManager.calculateChildId(
					extToolChain.getId(),
						null);
//			for(; !superChain.isExtensionElement(); superChain = (ToolChain)superChain.getSuperClass());
			
			IToolChain newChain = createToolChain(extToolChain, subId, extToolChain.getName(), false);
			
			// For each option/option category child of the tool-chain that is
			// the child of the selected configuration element, create an option/
			// option category child of the cloned configuration's tool-chain element
			// that specifies the original tool element as its superClass.
			newChain.createOptions(extToolChain);

			// For each tool element child of the tool-chain that is the child of 
			// the selected configuration element, create a tool element child of 
			// the cloned configuration's tool-chain element that specifies the 
			// original tool element as its superClass.
			ITool[] tools = extToolChain.getTools();
			for (int i=0; i<tools.length; i++) {
			    Tool toolChild = (Tool)tools[i];
			    subId = ManagedBuildManager.calculateChildId(toolChild.getId(),null);
			    newChain.createTool(toolChild, subId, toolChild.getName(), false);
			}
			
			ITargetPlatform tpBase = cloneInfo.getToolChain().getTargetPlatform();
			ITargetPlatform extTp = tpBase;
			for(;extTp != null && !extTp.isExtensionElement();extTp = extTp.getSuperClass());
			
			TargetPlatform tp;
			if(extTp != null){
				int nnn = ManagedBuildManager.getRandomNumber();
				subId = copyIds ? tpBase.getId() : extTp.getId() + "." + nnn;		//$NON-NLS-1$
//				subName = tpBase.getName();
				tp = new TargetPlatform(newChain, subId, tpBase.getName(), (TargetPlatform)tpBase);
			} else {
				subId = copyIds ? tpBase.getId() : ManagedBuildManager.calculateChildId(getId(), null);
				subName = tpBase != null ? tpBase.getName() : ""; //$NON-NLS-1$
				tp = new TargetPlatform((ToolChain)newChain, null, subId, subName, false);
			}

			((ToolChain)newChain).setTargetPlatform(tp);
		}
		
		if(copyIds){
			isDirty = cloneInfo.isDirty;
			needsRebuild = cloneInfo.needsRebuild;
		} else {
			setDirty(true);
			setRebuildState(true);
		}

	}

	public static ITool[] filtereTools(ITool localTools[], IManagedProject manProj) {
//		ITool[] localTools = toolChain.getTools();
//		IManagedProject manProj = getParent().getManagedProject();
		if (manProj == null) {
			//  If this is not associated with a project, then there is nothing to filter with
			return localTools;
		}
		IProject project = (IProject)manProj.getOwner();
		Vector tools = new Vector(localTools.length);
		for (int i = 0; i < localTools.length; i++) {
			ITool tool = localTools[i];
			if(!tool.isEnabled())
				continue;
			
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							tools.add(tool);
						}
						break;
					case ITool.FILTER_CC:
						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							tools.add(tool);
						}
						break;
					case ITool.FILTER_BOTH:
						tools.add(tool);
						break;
					default:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
		}
		
		// Answer the filtered tools as an array
		return (ITool[])tools.toArray(new ITool[tools.size()]);
	}

	public ITool[] getFilteredTools() {
		if (toolChain == null) {
			return new ITool[0];
		}
		ITool[] localTools = toolChain.getTools();
		IManagedProject manProj = getParent().getManagedProject();
		return filtereTools(localTools, manProj);
//		if (manProj == null) {
//			//  If this is not associated with a project, then there is nothing to filter with
//			return localTools;
//		}
//		IProject project = (IProject)manProj.getOwner();
//		Vector tools = new Vector(localTools.length);
//		for (int i = 0; i < localTools.length; i++) {
//			ITool tool = localTools[i];
//			if(!tool.isEnabled())
//				continue;
//			
//			try {
//				// Make sure the tool is right for the project
//				switch (tool.getNatureFilter()) {
//					case ITool.FILTER_C:
//						if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
//							tools.add(tool);
//						}
//						break;
//					case ITool.FILTER_CC:
//						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
//							tools.add(tool);
//						}
//						break;
//					case ITool.FILTER_BOTH:
//						tools.add(tool);
//						break;
//					default:
//						break;
//				}
//			} catch (CoreException e) {
//				continue;
//			}
//		}
//		
//		// Answer the filtered tools as an array
//		return (ITool[])tools.toArray(new ITool[tools.size()]);
	}

	public final int getKind() {
		return ICSettingBase.SETTING_FOLDER;
	}

	public boolean isDirty() {
		if(super.isDirty())
			return true;
		
		if (toolChain.isDirty()) return true;
		
		return false;
	}

	public boolean needsRebuild() {
		if(super.needsRebuild())
			return true;
		
		if(toolChain.needsRebuild())
			return true;

//		ITool tools[] = getFilteredTools();
//		for(int i = 0; i < tools.length; i++){
//			if(tools[i].needsRebuild())
//				return true;
//		}


		return false;
	}

	public void setRebuildState(boolean rebuild) {
		super.setRebuildState(rebuild);
		
		if(!rebuild){
			toolChain.setRebuildState(false);
	
//			ITool tools[] = getFilteredTools();
//			for(int i = 0; i < tools.length; i++){
//				tools[i].setRebuildState(false);
//			}
		}

	}

	public IToolChain getToolChain() {
		return toolChain;
	}

	public ITool[] getTools() {
		return toolChain.getTools();
	}

	public ITool getTool(String id) {
		return toolChain.getTool(id);
	}
	
	public ITool[] getToolsBySuperClassId(String id) {
		return toolChain.getToolsBySuperClassId(id);
	}

	ToolChain createToolChain(IToolChain superClass, String Id, String name, boolean isExtensionElement) {
		toolChain = new ToolChain(this, superClass, Id, name, isExtensionElement);
		setDirty(true);
		return toolChain;
	}

	void serialize(ICStorageElement element){
		super.serialize(element);
		
		ICStorageElement toolChainElement = element.createChild(IToolChain.TOOL_CHAIN_ELEMENT_NAME);
		toolChain.serialize(toolChainElement);
	}
	
	void resolveReferences(){
//		super.resolveReferences();
		if(toolChain != null)
			toolChain.resolveReferences();
	}
	
	public void updateManagedBuildRevision(String revision){
		super.updateManagedBuildRevision(revision);
		
		if(toolChain != null)
			toolChain.updateManagedBuildRevision(revision);
	}
	
	public boolean isExtensionElement(){
		return isExtensionElement;
	}
	
	public String getErrorParserIds(){
		if(toolChain != null)
			return toolChain.getErrorParserIds(getParent());
		return null;
	}
	
	public CFolderData getFolderData(){
		return (CFolderData)getResourceData();
	}

	public CLanguageData[] getCLanguageDatas() {
		ITool tools[] = getFilteredTools();
		List list = new ArrayList();
		for(int i = 0; i < tools.length; i++){
			CLanguageData datas[] = tools[i].getCLanguageDatas();
			for(int j = 0; j < datas.length; j++){
				list.add(datas[j]);
			}
		}
		return (BuildLanguageData[])list.toArray(new BuildLanguageData[list.size()]);
	}
	
	public ITool getToolFromOutputExtension(String extension) {
		// Treat a null argument as an empty string
		String ext = extension == null ? "" : extension; //$NON-NLS-1$
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				return tool;
			}
		}
		return null;		
	}
	
	public ITool getToolFromInputExtension(String sourceExtension) {
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(sourceExtension)) {
				return tool;
			}
		}
		return null;		
	}

	public void propertiesChanged() {
		if(isExtensionElement)
			return;

		toolChain.propertiesChanged();

		super.propertiesChanged();
	}

/*	public IToolChain getBaseToolChain() {
		if(toolChain.isExtensionElement())
			return toolChain;
		return toolChain.getSuperClass();
	}
*/
	
	public void setDirty(boolean isDirty) {
 		if (isExtensionElement && isDirty) return;
 		
 		super.setDirty(isDirty);

 		// Propagate "false" to the children
		if (!isDirty) {
			if(toolChain != null)
				toolChain.setDirty(false);
		}
	}
	
	private Map typeIdsToMap(String [] ids){
		Map map = new HashMap(ids.length);
		for(int i = 0; i < ids.length; i++){
			map.put(ids[i], null);
		}
		
		return map;
	}

	private Map propsToMap(IBuildProperty props[]){
		Map map = new HashMap(props.length);
		for(int i = 0; i < props.length; i++){
			map.put(props[i].getPropertyType().getId(), props[i].getValue().getId());
		}
		
		return map;
	}

	private void checkPropertiesModificationCompatibility(IBuildPropertiesRestriction r, Map unspecifiedRequiredProps, Map unspecifiedProps, Set undefinedSet){
		IBuildObjectProperties props = null;
		IConfiguration cfg = getParent();
		if(cfg != null){
			props = cfg.getBuildProperties();
		}
		
		unspecifiedProps.clear();
		unspecifiedRequiredProps.clear();

		if(props != null){
			String[] requiredIds = props.getRequiredTypeIds();
			
			IBuildPropertyType[] supportedTypes = props.getSupportedTypes();
			if(supportedTypes.length != 0 || requiredIds.length != 0){
				Map requiredMap = typeIdsToMap(requiredIds);
				getUnsupportedProperties(requiredMap, r, unspecifiedRequiredProps, undefinedSet);
				unspecifiedProps.putAll(unspecifiedRequiredProps);
				
				IBuildProperty[] ps = props.getProperties();
				Map propsMap = propsToMap(ps);
				getUnsupportedProperties(propsMap, r, unspecifiedProps, undefinedSet);
			}
		}
	}

	private void getUnsupportedProperties(Map props, IBuildPropertiesRestriction restriction, Map unsupported, Set inexistent){
		BuildPropertyManager mngr = BuildPropertyManager.getInstance();
		for(Iterator iter = props.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String propId = (String)entry.getKey();
			String valueId = (String)entry.getValue();
			IBuildPropertyType type = mngr.getPropertyType(propId);
			if(type == null){
				if(inexistent != null){
					inexistent.add(propId);
				}
			}
			
			if(!restriction.supportsType(propId)){
				unsupported.put(propId, null);
			} else if (!restriction.supportsValue(propId, valueId)){
				unsupported.put(propId, valueId);
			}
		}
	}

	public void checkPropertiesModificationCompatibility(final ITool tools[], Map unspecifiedRequiredProps, Map unspecifiedProps, Set undefinedSet){
		final ToolChain tc = (ToolChain)getToolChain();
		IBuildPropertiesRestriction r = new IBuildPropertiesRestriction(){
			public boolean supportsType(String typeId) {
				if(tc.supportsType(typeId, false))
					return true;
				
				for(int i = 0; i < tools.length; i++){
					if(((Tool)tools[i]).supportsType(typeId))
						return true;
				}
				return false;
			}

			public boolean supportsValue(String typeId, String valueId) {
				if(tc.supportsValue(typeId, valueId, false))
					return true;
				
				for(int i = 0; i < tools.length; i++){
					if(((Tool)tools[i]).supportsValue(typeId, valueId))
						return true;
				}
				return false;
			}

			public String[] getRequiredTypeIds() {
				List list = new ArrayList();
				
				list.addAll(Arrays.asList(tc.getRequiredTypeIds(false)));
				
				for(int i = 0; i < tools.length; i++){
					list.addAll(Arrays.asList(((Tool)tools[i]).getRequiredTypeIds()));
				}

				return (String[])list.toArray(new String[list.size()]);
			}

			public String[] getSupportedTypeIds() {
				List list = new ArrayList();
				
				list.addAll(Arrays.asList(tc.getSupportedTypeIds(false)));
				
				for(int i = 0; i < tools.length; i++){
					list.addAll(Arrays.asList(((Tool)tools[i]).getSupportedTypeIds()));
				}

				return (String[])list.toArray(new String[list.size()]);
			}

			public String[] getSupportedValueIds(String typeId) {
				List list = new ArrayList();
				
				list.addAll(Arrays.asList(tc.getSupportedValueIds(typeId, false)));
				
				for(int i = 0; i < tools.length; i++){
					list.addAll(Arrays.asList(((Tool)tools[i]).getSupportedValueIds(typeId)));
				}

				return (String[])list.toArray(new String[list.size()]);
			}

			public boolean requiresType(String typeId) {
				if(tc.requiresType(typeId, false))
					return true;
				
				for(int i = 0; i < tools.length; i++){
					if(((Tool)tools[i]).requiresType(typeId))
						return true;
				}
				return false;
			}
		};
		
		checkPropertiesModificationCompatibility(r, unspecifiedRequiredProps, unspecifiedProps, undefinedSet);
	}

	public void checkPropertiesModificationCompatibility(IToolChain tc, Map unspecifiedRequiredProps, Map unspecifiedProps, Set undefinedSet){
		checkPropertiesModificationCompatibility((IBuildPropertiesRestriction)tc, unspecifiedRequiredProps, unspecifiedProps, undefinedSet);
	}

	public boolean isPropertiesModificationCompatible(IToolChain tc){
		Map requiredMap = new HashMap();
		Map unsupportedMap = new HashMap();
		Set undefinedSet = new HashSet();
		checkPropertiesModificationCompatibility(tc, requiredMap, unsupportedMap, undefinedSet);
		if(requiredMap.size() != 0)
			return false;
		
		return true;
//		boolean compatible = false;
//
//		IBuildObjectProperties props = null;
//		IConfiguration cfg = getParent();
//		if(cfg != null){
//			props = cfg.getBuildProperties();
//		}
//		
//		if(props != null){
//			String[] requiredIds = props.getRequiredTypeIds();
//			IBuildPropertyType[] supportedTypes = props.getSupportedTypes();
//			if(supportedTypes.length != 0 || requiredIds.length != 0){
//				ToolChain toolChain = (ToolChain)tc;
//				if(requiredIds.length != 0){
//					int i = 0;
//					for(; i < requiredIds.length; i++){
//						IBuildPropertyType type = BuildPropertyManager.getInstance().getPropertyType(requiredIds[i]);
//						if(type == null)
//							break;
//						
//						if(!toolChain.supportsType(type))
//							break;
//					}
//					
//					if(i == requiredIds.length)
//						compatible = true;
//				} else {
//					int i = 0;
//					for(; i < supportedTypes.length; i++){
//						if(!toolChain.supportsType(supportedTypes[i]))
//							break;
//					}
//					if(i == supportedTypes.length)
//						compatible = true;
//				}
//			}
//		}
//		return compatible;
	}
	
	private Set getRequiredUnspecifiedProperties(){
		IBuildObjectProperties props = null;
		IConfiguration cfg = getParent();
		if(cfg != null){
			props = cfg.getBuildProperties();
		}
		
		Set set = new HashSet();
		if(props != null){
			String[] requiredIds = props.getRequiredTypeIds();
			for(int i = 0; i < requiredIds.length; i++){
				if(props.getProperty(requiredIds[i]) == null)
					set.add(requiredIds[i]);
			}
		}
		
		return set;
		
	}
	
	public boolean isToolChainCompatible(IToolChain tCh){
		boolean compatible = false;
		
		if(tCh != null){
			if(getToolChainConverterElement(tCh) != null)
				compatible = true;
			
			if(!compatible)
				compatible = isPropertiesModificationCompatible(tCh);
		} else {
			compatible = this.toolChain != null && this.toolChain.isPreferenceToolChain();
		}
		return compatible;
	}

	public IToolChain changeToolChain(IToolChain newSuperClass, String Id, String name) throws BuildException{
		IToolChain curReal = ManagedBuildManager.getRealToolChain(toolChain);
		IToolChain newReal = ManagedBuildManager.getRealToolChain(newSuperClass);
		
		if(newReal != curReal){
			ToolChain oldToolChain = toolChain;
			IConfigurationElement el = getToolChainConverterElement(newSuperClass);
			ITool oldTools[] = oldToolChain.getTools();
			
			if(el != null){
				updateToolChainWithConverter(el, newSuperClass, Id, name);
			} else {
				updateToolChainWithProperties(newSuperClass, Id, name);
			}
			
			BuildSettingsUtil.disconnectDepentents(getParent(), oldTools);
		}
		return toolChain;
	}
		
	void updateToolChainWithProperties(IToolChain newSuperClass, String Id, String name) {
		ToolChain oldTc = (ToolChain)getToolChain();
		if(newSuperClass != null) {
			createToolChain(newSuperClass, Id, name, false);
		} else {
			Configuration cfg = ConfigurationDataProvider.getClearPreference(null);
			ToolChain prefTch = (ToolChain)cfg.getRootFolderInfo().getToolChain();
			
			toolChain = new ToolChain(this, ManagedBuildManager.calculateChildId(prefTch.getSuperClass().getId(), null), prefTch.getName(), new HashMap(), (ToolChain)prefTch);
		}
		toolChain.propertiesChanged();

		//TODO: copy includes, symbols, etc.
	}
	
	void updateToolChainWithConverter(IConfigurationElement el, IToolChain newSuperClass, String Id, String name) throws BuildException{
		IBuildObject bo = ManagedBuildManager.convert(getToolChain(), newSuperClass.getId(), true);
		if(!(bo instanceof ToolChain)){
			throw new BuildException(ManagedMakeMessages.getResourceString("FolderInfo.4")); //$NON-NLS-1$
		}
		if(toolChain != bo){
			setUpdatedToolChain((ToolChain)bo);
		}
		
		toolChain.setName(name);
	}
	
	void setUpdatedToolChain(ToolChain tch){
		toolChain = tch;
		tch.updateParentFolderInfo(this);
	}
	
	private IConfigurationElement getToolChainConverterElement(IToolChain tCh){
		if(tCh == null)
			return null;
		
		ToolChain curTc = (ToolChain)getToolChain();
		if(curTc != null){
			return curTc.getConverterModificationElement(tCh);
		}
		return null;
	}
	
	private ITool[][] checkDups(ITool[] removed, ITool[] added){
		LinkedHashMap removedMap = createRealMap(removed);
		LinkedHashMap addedMap = createRealMap(added);
		LinkedHashMap rmCopy = (LinkedHashMap)removedMap.clone();
		
		removedMap.keySet().removeAll(addedMap.keySet());
		addedMap.keySet().removeAll(rmCopy.keySet());
		
		if(removedMap.size() != 0){
			LinkedHashMap curMap = createRealMap(getTools());
			for(Iterator iter = removedMap.keySet().iterator(); iter.hasNext();){
				Object key = iter.next();
				if(!curMap.containsKey(key))
					iter.remove();
			}
		}
		ITool[][] result = new Tool[2][];
		result[0] = (Tool[])removedMap.values().toArray(new Tool[removedMap.size()]);
		result[1] = (Tool[])addedMap.values().toArray(new Tool[addedMap.size()]);
		return result;
	}
	
	private LinkedHashMap createRealMap(ITool[] tools){
		LinkedHashMap map = new LinkedHashMap();
		for(int i = 0; i < tools.length; i++){
			Tool realTool = (Tool)ManagedBuildManager.getRealTool(tools[i]);
			Object key = realTool.getMatchKey();
			map.put(key, tools[i]);
		}
		
		return map;
	}
	
	public void modifyToolChain(ITool[] removed, ITool[] added){
		ITool[][] checked = checkDups(removed, added);
		removed = checked[0];
		added = checked[1];
		if(added.length == 0 && removed.length == 0)
			return;
		
		List remainingRemoved = new ArrayList();
		List remainingAdded = new ArrayList();
		Map converterMap = calculateConverterTools(removed, added, remainingRemoved, remainingAdded);
		invokeConverters(converterMap);
		List newTools = new ArrayList(added.length);
		for(Iterator iter = converterMap.values().iterator(); iter.hasNext();){
			ConverterInfo info = (ConverterInfo)iter.next();
			if(info.fIsConversionPerformed){
				Tool newTool = (Tool)info.fToObject;
				newTool.updateParent(getToolChain());
				newTools.add(newTool);
			} else {
				remainingAdded.add(info.fToObject);
			}
		}

		for(Iterator iter = remainingAdded.iterator(); iter.hasNext();){
			Tool tool = (Tool)iter.next();
			Tool newTool = new Tool(toolChain, tool, ManagedBuildManager.calculateChildId(tool.getId(), null), tool.getName(), false);
			newTools.add(newTool);
		}
		
		performToolChainModification(removed, (Tool[])newTools.toArray(new Tool[newTools.size()]));
	}
	
	private void performToolChainModification(ITool removed[], ITool[] added){
		BuildSettingsUtil.disconnectDepentents(getParent(), removed);
		
		for(int i = 0; i < removed.length; i++){
			toolChain.removeTool((Tool)removed[i]);
		}
		
		for(int i = 0; i < added.length; i++){
			toolChain.addTool((Tool)added[i]);
		}
		
		adjustTargetTools(removed, getTools());
		
		toolChain.propertiesChanged();
	}
	
	private void adjustTargetTools(ITool removed[], ITool allTools[]){
		if(!isRoot())
			return;
		
		Set set = new HashSet();
		String [] ids = toolChain.getTargetToolList();
		boolean targetToolsModified = false;
		set.addAll(Arrays.asList(ids));
		
		for(int i = 0; i < removed.length; i++){
			ITool target = getTargetTool(removed[i]);
			
			if(target == null)
				continue;
			
			List list = BuildSettingsUtil.calcDependentTools(allTools, target, null);
			if(list.size() != 0)
				continue;
			
			ITool newTargetTool = findCompatibleTargetTool(target, allTools);
			if(newTargetTool != null){
				set.remove(target.getId());
				set.add(newTargetTool.getId());
				targetToolsModified = true;
			}
		}
		
		if(targetToolsModified){
			toolChain.setTargetToolIds(CDataUtil.arrayToString((String[])set.toArray(new String[set.size()]), ";")); //$NON-NLS-1$
		}
	}
	
	private ITool findCompatibleTargetTool(ITool tool, ITool allTools[]){
		IProject project = getParent().getOwner().getProject();
		String exts[] = ((Tool)tool).getAllOutputExtensions(project);
		Set extsSet = new HashSet(Arrays.asList(exts));
		ITool compatibleTool = null;
		for(int i = 0; i < allTools.length; i++){
			String otherExts[] = ((Tool)allTools[i]).getAllOutputExtensions(project);
			for(int k = 0; k < otherExts.length; k++){
				if(extsSet.contains(otherExts[k])){
					compatibleTool = allTools[i];
					break;
				}
			}
			if(compatibleTool != null)
				break;
		}
		
		return compatibleTool;
	}
	
	private ITool getTargetTool(ITool tool){
		String [] ids = toolChain.getTargetToolList();
		
		for(int i = 0; i < ids.length; i++){
			String id = ids[i];
			ITool target = tool;
			for(; target != null; target = target.getSuperClass()){
				if(id.equals(target.getId()))
					break;
			}
			if(target != null)
				return target;
				
		}
		return null;
	}
	
//	private void disconnectDepentents(ITool[] tools){
//		for(int i = 0; i < tools.length; i++){
//			disconnectDepentents(tools[i]);
//		}
//	}
//
//	private void disconnectDepentents(ITool tool){
//		ITool deps[] = ManagedBuildManager.getDependentTools(getParent(), tool);
//		for(int i = 0; i < deps.length; i++){
//			disconnect(deps[i], tool);
//		}
//	}
//	
//	private void disconnect(ITool child, ITool superClass){
//		ITool directChild = child;
//		for(;directChild != null; directChild = directChild.getSuperClass()){
//			if(superClass.equals(directChild.getSuperClass()))
//				break;
//		}
//		
//		if(directChild == null)
//			return;
//		
//		((Tool)directChild).copyNonoverriddenSettings((Tool)superClass);
//		((Tool)directChild).setSuperClass(superClass.getSuperClass());
//	}
//
	
	private List invokeConverters(Map converterMap){
		List failed = new ArrayList();
		for(Iterator iter = converterMap.values().iterator();iter.hasNext();){
			ConverterInfo info = (ConverterInfo)iter.next();
			IBuildObject converted = ManagedBuildManager.convert(info.getFromObject(), info.getToObject().getId(), true);
			if(converted == null || !converted.getClass().equals(info.getFromObject().getClass())){
				failed.add(info);
			} else {
				info.fToObject = converted;
				info.fIsConversionPerformed = true;
			}
		}
		return failed;
	}
	
	private Map calculateConverterTools(ITool[] removed, ITool[] added, List remainingRemoved, List remainingAdded){
		if(remainingAdded == null)
			remainingAdded = new ArrayList(added.length);
		if(remainingRemoved == null)
			remainingRemoved = new ArrayList(removed.length);
		
		remainingAdded.clear();
		remainingRemoved.clear();
		
		remainingAdded.addAll(Arrays.asList(added));
		remainingRemoved.addAll(Arrays.asList(removed));
		
		Map resultMap = new HashMap();
		
		for(Iterator rIter = remainingRemoved.iterator(); rIter.hasNext();){
			ITool r = (ITool)rIter.next();
			
			if(r.getParentResourceInfo() != this)
				continue;
			

			Map map = ManagedBuildManager.getConversionElements(r);
			if(map.size() == 0)
				continue;

			for(Iterator aIter = remainingAdded.iterator(); aIter.hasNext();){
				ITool a = (ITool)aIter.next();
				
				if(a.getParentResourceInfo() == this)
					continue;
				
				IConfigurationElement el = getToolConverterElement(r, a);
				if(el != null){
					resultMap.put(r, new ConverterInfo(r, a, el));
					rIter.remove();
					aIter.remove();
					break;
				}
			}
		}
		
		return resultMap;
	}
	
	private ITool[] calculateToolsArray(ITool[] removed, ITool[] added){
		ITool tools[] = getTools();
		Map map = calcExtToolIdToToolMap(tools);
		Map removedMap = calcExtToolIdToToolMap(removed);
		for(Iterator iter = removedMap.keySet().iterator(); iter.hasNext();){
			map.remove(iter.next());
		}
		map.putAll(calcExtToolIdToToolMap(added));
		
		return (ITool[])map.values().toArray(new ITool[map.size()]);
	}
	
	private Map calcExtToolIdToToolMap(ITool tools[]){
		Map map = new HashMap();
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			ITool extTool = ManagedBuildManager.getExtensionTool(tool);
			if(extTool == null)
				extTool = tool;
			
			map.put(extTool.getId(), tool);
		}
		
		return map;
	}
	
	private ITool[][] calculateConflictingTools(ITool[] newTools){
		HashSet set = new HashSet();
		set.addAll(Arrays.asList(newTools));
		List result = new ArrayList();
		for(Iterator iter = set.iterator(); iter.hasNext();){
			ITool t = (ITool)iter.next();
			iter.remove();
			HashSet tmp = (HashSet)set.clone();
			List list = new ArrayList();
			for(Iterator tmpIt = tmp.iterator(); tmpIt.hasNext();){
				ITool other = (ITool)tmpIt.next();
				String conflicts[] = getConflictingInputExts(t, other);
				if(conflicts.length != 0){
					list.add(other);
					tmpIt.remove();
				}
			}
			
			if(list.size() != 0){
				list.add(t);
				result.add(list.toArray(new Tool[list.size()]));
			}
			set = tmp;
			iter = set.iterator();
		}
		
		return (ITool[][])result.toArray(new ITool[result.size()][]);
	}
	
	private String[] getConflictingInputExts(ITool tool1, ITool tool2){
		IProject project = getParent().getOwner().getProject();
		String ext1[] = ((Tool)tool1).getAllInputExtensions(project);
		String ext2[] = ((Tool)tool2).getAllInputExtensions(project);
		Set set1 = new HashSet(Arrays.asList(ext1));
		Set result = new HashSet();
		for(int i = 0; i < ext2.length; i++){
			if(set1.remove(ext2[i]))
				result.add(ext2[i]);
		}
		return (String[])result.toArray(new String[result.size()]);
	}

	
	public IModificationStatus getToolChainModificationStatus(ITool[] removed, ITool[] added){
//		Map converterMap = calculateConverterTools(removed, added, null, null);
		ITool newTools[] = calculateToolsArray(removed, added);
		ITool[][] conflicting = calculateConflictingTools(filtereTools(newTools, getParent().getManagedProject()));
		Map unspecifiedRequiredProps = new HashMap();
		Map unspecifiedProps = new HashMap();
		Set undefinedSet = new HashSet();
		IConfiguration cfg = getParent();
		ITool[] nonManagedTools = null;
		if(cfg.isManagedBuildOn() && cfg.supportsBuild(true)){
			List list = new ArrayList();
			for(int i = 0; i < newTools.length; i++){
				if(!newTools[i].supportsBuild(true)){
					list.add(newTools[i]);
				}
			}
			if(list.size() != 0){
				nonManagedTools = (ITool[])list.toArray(new Tool[list.size()]);
			}
		}
		return new ModificationStatus(unspecifiedRequiredProps, unspecifiedProps, undefinedSet, conflicting, nonManagedTools);
	}
	
	private IConfigurationElement getToolConverterElement(ITool fromTool, ITool toTool){
		ToolChain curTc = (ToolChain)getToolChain();
		if(curTc != null){
			return curTc.getConverterModificationElement(fromTool, toTool);
		}
		return null;
	}

	public boolean supportsBuild(boolean managed) {
		Set set = getRequiredUnspecifiedProperties();
		if(set.size() != 0)
			return false;

		ToolChain tCh = (ToolChain)getToolChain();
		if(tCh == null || !tCh.getSupportsManagedBuildAttribute())
			return !managed;
		
		ITool tools[] = getFilteredTools();
		for(int i = 0; i < tools.length; i++){
			if(!tools[i].supportsBuild(managed))
				return false;
		}
		
		return true;
	}
	
	public boolean buildsFileType(String srcExt) {
		// Check to see if there is a rule to build a file with this extension
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool != null && tool.buildsFileType(srcExt)) {
				return true;
			}
		}
		return false;
	}
	
	public String getOutputExtension(String resourceExtension) {
		String outputExtension = null;
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			outputExtension = tool.getOutputExtension(resourceExtension);
			if (outputExtension != null) {
				return outputExtension;
			}
		}
		return null;
	}
	
	public boolean isHeaderFile(String ext) {
		// Check to see if there is a rule to build a file with this extension
		IManagedProject manProj = getParent().getManagedProject();
		IProject project = null;
		if (manProj != null) {
			project = (IProject)manProj.getOwner();
		}
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				if (project != null) {
					// Make sure the tool is right for the project
					switch (tool.getNatureFilter()) {
						case ITool.FILTER_C:
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.isHeaderFile(ext);
							}
							break;
						case ITool.FILTER_CC:
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.isHeaderFile(ext);
							}
							break;
						case ITool.FILTER_BOTH:
							return tool.isHeaderFile(ext);
					}
				} else {
					return tool.isHeaderFile(ext);
				}
			} catch (CoreException e) {
				continue;
			}
		}
		return false;
	}

	public Set contributeErrorParsers(Set set){
		if(set == null)
			set = new HashSet();
		
		if(toolChain != null)
			toolChain.contributeErrorParsers(this, set, true);
		
		return set;
	}

	public void resetErrorParsers() {
		if(toolChain != null)
			toolChain.resetErrorParsers(this);
	}

	void removeErrorParsers(Set set) {
		if(toolChain != null)
			toolChain.removeErrorParsers(this, set);
	}

	public ITool getToolById(String id) {
		if(toolChain != null)
			return toolChain.getTool(id);
		return null;
	}

	void resolveProjectReferences(boolean onLoad){
		if(toolChain != null)
			toolChain.resolveProjectReferences(onLoad);
	}
	
	public void resetOptionSettings() {
		// We just need to remove all Options
		ITool[] tools = getTools();
		IToolChain toolChain = getToolChain();
		IOption[] opts;
		
		// Send out the event to notify the options that they are about to be removed.
		// Do not do this for the child resource configurations as they are handled when
		// the configuration itself is destroyed.
//		ManagedBuildManager.performValueHandlerEvent(this, IManagedOptionValueHandler.EVENT_CLOSE, false);
		// Remove the configurations		
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				tool.removeOption(opts[j]);
			}
		}
		opts = toolChain.getOptions();
		for (int j = 0; j < opts.length; j++) {
			toolChain.removeOption(opts[j]);
		}
		
//		rebuildNeeded = true;
	}
	
	public boolean hasCustomSettings(){
		return toolChain.hasCustomSettings();
	}
}
