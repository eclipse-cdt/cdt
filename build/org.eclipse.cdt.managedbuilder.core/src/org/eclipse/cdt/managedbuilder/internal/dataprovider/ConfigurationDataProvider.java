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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigInfoFactory2;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.IModificationContext;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.BuilderFactory;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ISettingsChangeListener;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.NotificationManager;
import org.eclipse.cdt.managedbuilder.internal.core.SettingsChangeEvent;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentType;

/**
 * The main hook ManagedBuild uses to connect to cdt.core's project model.
 * Provides & Persists Build configuration data in the project model storage.
 */
public class ConfigurationDataProvider extends CConfigurationDataProvider implements ISettingsChangeListener {
	private static final String BUILD_SYSTEM_DATA_MODULE_NAME = "cdtBuildSystem";	//$NON-NLS-1$
	private static final String VERSION_ATTRIBUTE = "version";	//$NON-NLS-1$
	private static final String PREF_CFG_ID = "org.eclipse.cdt.build.core.prefbase.cfg";	//$NON-NLS-1$
	public static final String PREF_TC_ID = "org.eclipse.cdt.build.core.prefbase.toolchain";	//$NON-NLS-1$
	private static final String PREF_TOOL_ID = "org.eclipse.cdt.build.core.settings.holder";	//$NON-NLS-1$
	private static final QualifiedName CFG_PERSISTED_PROPERTY = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "configPersisted");	//$NON-NLS-1$
	private static final QualifiedName NATURES_USED_ON_CACHE_PROPERTY = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "naturesUsedOnCache");	//$NON-NLS-1$
	private static final QualifiedName BUILD_INFO_PROPERTY = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "buildInfo");	//$NON-NLS-1$

	private static boolean registered;

	public ConfigurationDataProvider(){
		if(!registered){
			registered = true;
			NotificationManager.getInstance().subscribe(this);
		}
	}

	private static class DesApplyRunnable implements IWorkspaceRunnable {
		IBuilder fBuilder;
		IProject fProject;

		DesApplyRunnable(IProject project, IBuilder builder){
			fProject = project;
			fBuilder = builder;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			try {
				IProjectDescription eDes = fProject.getDescription();
				if(BuilderFactory.applyBuilder(eDes, fBuilder) == BuilderFactory.CMD_CHANGED) {
						fProject.setDescription(eDes, monitor);
				}
			} catch (Exception e){
				ManagedBuilderCorePlugin.log(e);
			}
		}

	}
	static BuildConfigurationData writeConfiguration(ICConfigurationDescription des,
			BuildConfigurationData base) throws CoreException {
		BuildConfigurationData appliedCfg = base;
		ICStorageElement rootElement = des.getStorage(BUILD_SYSTEM_DATA_MODULE_NAME, true);
		rootElement.clear();
		rootElement.setAttribute(VERSION_ATTRIBUTE, ManagedBuildManager.getVersion().toString());
		ICStorageElement cfgElemen = rootElement.createChild(IConfiguration.CONFIGURATION_ELEMENT_NAME);
		Configuration cfg = (Configuration)appliedCfg.getConfiguration();
		Builder b = (Builder)cfg.getEditableBuilder();
		// Need to ensure that build macro supplier can get the description for this configuration during the write...
		cfg.setConfigurationDescription(des);
		if(b != null && b.isManagedBuildOn() && b.getBuildPathAttribute(false) == null){
			String bPath = b.getDefaultBuildPath();
			b.setBuildPathAttribute(bPath);
		}
//		cfg.setConfigurationDescription(des);
//		ManagedBuildManager.performValueHandlerEvent(cfg, IManagedOptionValueHandler.EVENT_APPLY);
		cfg.serialize(cfgElemen);

		return appliedCfg;
	}

	protected CConfigurationData applyPreferences(
			ICConfigurationDescription des, CConfigurationData base) throws CoreException{

		BuildConfigurationData appliedCfg = writeConfiguration(des, (BuildConfigurationData)base);

		IConfiguration cfg = ((BuildConfigurationData)base).getConfiguration();
		try {
			CfgScannerConfigInfoFactory2.savePreference(cfg);
		} catch (CoreException e){
			ManagedBuilderCorePlugin.log(e);
		}

		return appliedCfg;
	}

	@Override
	public CConfigurationData applyConfiguration(
			ICConfigurationDescription des,
			ICConfigurationDescription baseDescription,
			CConfigurationData base,
			IModificationContext context,
			IProgressMonitor monitor)
			throws CoreException {
		if(des.isPreferenceConfiguration())
			return applyPreferences(des, base);

		BuildConfigurationData baseCfgData = (BuildConfigurationData)base;
		IConfiguration baseCfg = baseCfgData.getConfiguration();
		BuildConfigurationData appliedCfg;
		if(context.isBaseDataCached() && !baseCfg.isDirty()){
			appliedCfg = baseCfgData;
			context.setConfigurationSettingsFlags(IModificationContext.CFG_DATA_STORAGE_UNMODIFIED | IModificationContext.CFG_DATA_SETTINGS_UNMODIFIED);
		} else {
			appliedCfg = writeConfiguration(des, baseCfgData);

			IManagedBuildInfo info = getBuildInfo(des);
			ManagedProject mProj = (ManagedProject)info.getManagedProject();
			mProj.applyConfiguration((Configuration)appliedCfg.getConfiguration());
			writeManagedProjectInfo(des.getProjectDescription(), mProj);
			try {
				CfgScannerConfigInfoFactory2.save(appliedCfg, des.getProjectDescription(), baseDescription.getProjectDescription(), !isPersistedCfg(des));
			} catch (CoreException e){
				ManagedBuilderCorePlugin.log(e);
			}
			info.setValid(true);
			// Update the ManagedBuildInfo in the ManagedBuildManager map. Doing this creates a barrier for subsequent
			// ManagedBuildManager#getBuildInfo(...) see Bug 305146 for more
			ManagedBuildManager.setLoaddedBuildInfo(des.getProjectDescription().getProject(), info);

			setPersistedFlag(des);
			cacheNaturesIdsUsedOnCache(des);

			if(des.isActive()){
				IConfiguration cfg = appliedCfg.getConfiguration();
				IBuilder builder = cfg.getEditableBuilder();
				IProject project = context.getProject();
				IProjectDescription eDes = context.getEclipseProjectDescription();
				switch(BuilderFactory.applyBuilder(eDes, builder)){
				case BuilderFactory.CMD_UNDEFINED:
					IWorkspaceRunnable applyR = new DesApplyRunnable(project, builder);
					context.addWorkspaceRunnable(applyR);
					break;
				case BuilderFactory.CMD_CHANGED:
					context.setEclipseProjectDescription(eDes);
					break;
				}
			}
		}
		return appliedCfg;
	}

	private void setPersistedFlag(ICConfigurationDescription cfg){
		cfg.setSessionProperty(CFG_PERSISTED_PROPERTY, Boolean.TRUE);
	}

	private static void writeManagedProjectInfo(ICProjectDescription des,
			ManagedProject mProj) throws CoreException {
		ICStorageElement rootElement = des.getStorage(BUILD_SYSTEM_DATA_MODULE_NAME, true);
		rootElement.clear();
		rootElement.setAttribute(VERSION_ATTRIBUTE, ManagedBuildManager.getVersion().toString());
		ICStorageElement mProjElem = rootElement.createChild(IManagedProject.MANAGED_PROJECT_ELEMENT_NAME);
		mProj.serializeProjectInfo(mProjElem);
	}


	protected CConfigurationData createPreferences(
			ICConfigurationDescription des, CConfigurationData base)
			throws CoreException {
		Configuration cfg = (Configuration)((BuildConfigurationData)base).getConfiguration();
		Configuration newCfg = new Configuration((ManagedProject)cfg.getManagedProject(), cfg, des.getId(), true, true, true);
		newCfg.setConfigurationDescription(des);
		newCfg.setName(des.getName());
//		if(!newCfg.getId().equals(cfg.getId())){
//			newCfg.exportArtifactInfo();
//		}

		return newCfg.getConfigurationData();
	}


	@Override
	public CConfigurationData createConfiguration(
			ICConfigurationDescription des,
			ICConfigurationDescription baseDescription,
			CConfigurationData base, boolean clone,
			IProgressMonitor monitor)
			throws CoreException {
		if(des.isPreferenceConfiguration())
			return createPreferences(des, base);

		Configuration cfg = (Configuration)((BuildConfigurationData)base).getConfiguration();
		Configuration newCfg = copyCfg(cfg, des);

		if(!newCfg.getId().equals(cfg.getId()) && newCfg.canExportedArtifactInfo()){
			// Bug 335001: Remove existing exported settings as they point at this configuration
			for (ICExternalSetting extSetting : newCfg.getConfigurationDescription().getExternalSettings())
				newCfg.getConfigurationDescription().removeExternalSetting(extSetting);
			// Now export the new settings
			newCfg.exportArtifactInfo();
		}

		setPersistedFlag(des);

		return newCfg.getConfigurationData();
	}

	public static Configuration copyCfg(Configuration cfg, ICConfigurationDescription des){
		IManagedBuildInfo info = getBuildInfo(des);
		ManagedProject mProj = (ManagedProject)info.getManagedProject();

		Configuration newCfg = new Configuration(mProj, cfg, des.getId(), true, true, false);
		newCfg.setConfigurationDescription(des);
		newCfg.setName(des.getName());

		des.setConfigurationData(ManagedBuildManager.CFG_DATA_PROVIDER_ID, newCfg.getConfigurationData());

		ManagedBuildManager.performValueHandlerEvent(newCfg, IManagedOptionValueHandler.EVENT_OPEN);

//		if(!newCfg.getId().equals(cfg.getId())){
//			newCfg.exportArtifactInfo();
//		}

		return newCfg;
	}

	private static IManagedBuildInfo getBuildInfo(ICConfigurationDescription des){
		ICProjectDescription projDes = des.getProjectDescription();
		IProject project = projDes.getProject();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project, false);
		if(info == null)
			info = ManagedBuildManager.createBuildInfo(project);

		setLoaddedBuildInfo(projDes, info);

		getManagedProject(des, info);

		return info;
	}

	private static void setLoaddedBuildInfo(ICProjectDescription des, IManagedBuildInfo info){
		des.setSessionProperty(BUILD_INFO_PROPERTY, info);
	}

	public static ManagedBuildInfo getLoaddedBuildInfo(ICProjectDescription des){
		return (ManagedBuildInfo)des.getSessionProperty(BUILD_INFO_PROPERTY);
	}

	private static IManagedProject getManagedProject(ICConfigurationDescription des, IManagedBuildInfo info){
		IManagedProject mProj = info.getManagedProject();
		if(mProj == null){
			mProj = createManagedProject(info, des.getProjectDescription());
		}
		return mProj;
	}

	private static IManagedProject createManagedProject(IManagedBuildInfo info, ICProjectDescription des){
		IManagedProject mProj = null;
		try {
			ICStorageElement rootElem = des.getStorage(BUILD_SYSTEM_DATA_MODULE_NAME, false);
			if(rootElem != null){
				String version = rootElem.getAttribute(VERSION_ATTRIBUTE);
				ICStorageElement children[] = rootElem.getChildren();
				for(int i = 0; i < children.length; i++){
					if(IManagedProject.MANAGED_PROJECT_ELEMENT_NAME.equals(children[i].getName())){
						mProj = new ManagedProject((ManagedBuildInfo)info, children[i], false, version);
						break;
					}
				}
			}
		} catch (CoreException e) {
			mProj = null;
		}

		if(mProj == null){
			mProj = new ManagedProject(des);
			info.setManagedProject(mProj);
		}

		return mProj;
	}

	public static String[] getNaturesIdsUsedOnCache(IConfiguration cfg){
		ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(cfg);
		if(cfgDes != null)
			return getNaturesIdsUsedOnCache(cfgDes);
		return null;
	}

	public static String[] getNaturesIdsUsedOnCache(ICConfigurationDescription cfg){
		String[] strs = (String[])cfg.getSessionProperty(NATURES_USED_ON_CACHE_PROPERTY);
		return strs != null && strs.length != 0 ? (String[])strs.clone() : strs;
	}

	public static void cacheNaturesIdsUsedOnCache(ICConfigurationDescription des){
		IProject project = des.getProjectDescription().getProject();
		try {
			IProjectDescription eDes = project.getDescription();
			String[] natures = eDes.getNatureIds();
			setNaturesIdsUsedOnCache(des, natures);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}

	private static void setNaturesIdsUsedOnCache(ICConfigurationDescription cfg, String ids[]){
		ids = ids != null && ids.length != 0 ? (String[])ids.clone() : ids;
		cfg.setSessionProperty(NATURES_USED_ON_CACHE_PROPERTY, ids);
	}

	private Configuration load(ICConfigurationDescription des, ManagedProject mProj, boolean isPreference) throws CoreException{
		ICStorageElement rootElement = des.getStorage(BUILD_SYSTEM_DATA_MODULE_NAME, true);
		ICStorageElement children[] = rootElement.getChildren();
		String version = rootElement.getAttribute(VERSION_ATTRIBUTE);
		Configuration cfg = null;

		for(int i = 0; i < children.length; i++){
			if(IConfiguration.CONFIGURATION_ELEMENT_NAME.equals(children[i].getName())){
				cfg = new Configuration(mProj, children[i], version, isPreference);
				ManagedBuildManager.performValueHandlerEvent(cfg, IManagedOptionValueHandler.EVENT_OPEN);
				break;
			}
		}
		return cfg;
	}

	protected CConfigurationData loadPreferences(ICConfigurationDescription des)
								throws CoreException {

		Configuration cfg = load(des, null, true);

		cfg = updatePreferenceOnLoad(cfg, des);

		cfg.setConfigurationDescription(des);

		return cfg.getConfigurationData();
	}

	public static Configuration getClearPreference(String id){
		Configuration cfg = createEmptyPrefConfiguration(id, null);

		cfg = adjustPreferenceConfig(cfg);

		return cfg;
	}

	private static Configuration updatePreferenceOnLoad(Configuration cfg, ICConfigurationDescription des){
		if(cfg == null)
			cfg = createEmptyPrefConfiguration(des.getId(), des.getName());

		cfg = adjustPreferenceConfig(cfg);

		return cfg;
	}

	private static Configuration adjustPreferenceConfig(Configuration cfg){
		LanguageManager mngr = LanguageManager.getInstance();
		ILanguageDescriptor dess[] = mngr.getLanguageDescriptors();
		Map<String, ILanguageDescriptor[]> map = mngr.getContentTypeIdToLanguageDescriptionsMap();

		IResourceInfo[] rcInfos = cfg.getResourceInfos();
		for(int i = 0; i < rcInfos.length; i++){
			if(rcInfos[i] instanceof IFolderInfo){
				adjustFolderInfo((IFolderInfo)rcInfos[i], dess, new HashMap<Object, ILanguageDescriptor[]>(map));
			}
		}

		return cfg;
	}

	private static void adjustFolderInfo(IFolderInfo info, ILanguageDescriptor dess[], HashMap<Object, ILanguageDescriptor[]> map){
		IToolChain tch = info.getToolChain();
		Map<String, ILanguageDescriptor> langMap = new HashMap<String, ILanguageDescriptor>();
		for(int i = 0; i < dess.length; i++){
			langMap.put(dess[i].getId(), dess[i]);
		}
		if(PREF_TC_ID.equals(tch.getSuperClass().getId())){
			ITool[] tools = tch.getTools();
			for(int i = 0; i < tools.length; i++){
				Tool tool = (Tool)tools[i];
				IInputType types[] = tool.getAllInputTypes();
				for(int k = 0; k < types.length; k++){
					InputType type = (InputType)types[k];
					String langId = type.getLanguageId(tool);
					if(langId != null){
						ILanguageDescriptor des = langMap.remove(langId);
						if(des != null)
							adjustInputType(tool, type, des);
						continue;
					} else {
						IContentType[] cTypes = type.getSourceContentTypes();
						for(int c = 0; c < cTypes.length; c++){
							IContentType cType = cTypes[c];
							ILanguageDescriptor[] langs = map.remove(cType.getId());
							if(langs != null && langs.length != 0){
								for(int q = 0; q < langs.length; q++){
									langMap.remove(langs[q].getId());
								}

								adjustInputType(tool, type, langs[0]);
							}
						}
					}
				}
			}

			if(!langMap.isEmpty()){
				addTools(tch, langMap, map);
			}
		}
	}

	private static InputType adjustInputType(Tool tool, InputType type, ILanguageDescriptor des){
		String [] cTypeIds = des.getContentTypeIds();
		String srcIds[] = type.getSourceContentTypeIds();
		String hIds[] = type.getHeaderContentTypeIds();

		Set<String> landTypes = new HashSet<String>(Arrays.asList(cTypeIds));
		landTypes.removeAll(Arrays.asList(srcIds));
		landTypes.removeAll(Arrays.asList(hIds));

		if(landTypes.size() != 0){
			List<String> srcList = new ArrayList<String>();
			srcList.addAll(landTypes);
			type = (InputType)tool.getEditableInputType(type);
			type.setSourceContentTypeIds(srcList.toArray(new String[srcList.size()]));
		}

		if(!des.getId().equals(type.getLanguageId(tool))){
			type = (InputType)tool.getEditableInputType(type);
			type.setLanguageIdAttribute(des.getId());
			type.setLanguageNameAttribute(des.getName());
		}
		return type;
	}

	private static void addTools(IToolChain tc, Map<String, ILanguageDescriptor> langMap, Map<Object, ILanguageDescriptor[]> cTypeToLangMap){
		ITool extTool = ManagedBuildManager.getExtensionTool(PREF_TOOL_ID);
		List<ILanguageDescriptor> list = new ArrayList<ILanguageDescriptor>(langMap.values());
		ILanguageDescriptor des;
		while(list.size() != 0){
			des = list.remove(list.size() - 1);
			String[] ctypeIds = des.getContentTypeIds();
			boolean addLang = false;
			for(int i = 0; i < ctypeIds.length; i++){
				ILanguageDescriptor[] langs = cTypeToLangMap.remove(ctypeIds[i]);
				if(langs != null && langs.length != 0){
					addLang = true;
					for(int q = 0; q < langs.length; q++){
						list.remove(langs[q]);
					}
				}
			}

			if(addLang){
				String id = ManagedBuildManager.calculateChildId(extTool.getId(), null);
				String name = des.getName();
				Tool tool = (Tool)tc.createTool(extTool, id, name, false);
				InputType type = (InputType)tool.getInputTypes()[0];
				type = (InputType)tool.getEditableInputType(type);
				type.setSourceContentTypes(des.getContentTypes());
				type.setLanguageNameAttribute(des.getName());
				type.setName(des.getName());
				type.setLanguageIdAttribute(des.getId());
			}
		}
	}


	private static Configuration createEmptyPrefConfiguration(String id, String name){
		Configuration extCfg = (Configuration)ManagedBuildManager.getExtensionConfiguration(PREF_CFG_ID);
		Configuration emptyPrefCfg = null;
		if(extCfg != null){
			if(id == null)
				id = ManagedBuildManager.calculateChildId(extCfg.getId(), null);
			if(name == null)
				name = extCfg.getName();
			emptyPrefCfg = new Configuration(null, extCfg, id, false, true, true);
			emptyPrefCfg.setName(name);
			emptyPrefCfg.setPerRcTypeDiscovery(false);
		}

		return emptyPrefCfg;
	}

	@Override
	public CConfigurationData loadConfiguration(ICConfigurationDescription des,
			IProgressMonitor monitor)
			throws CoreException {
		if(des.isPreferenceConfiguration())
			return loadPreferences(des);

		IManagedBuildInfo info = getBuildInfo(des);
		Configuration cfg = load(des, (ManagedProject)info.getManagedProject(), false);

		if(cfg != null){
			cfg.setConfigurationDescription(des);
			info.setValid(true);
			setPersistedFlag(des);
			cacheNaturesIdsUsedOnCache(des);
			// Update the ManagedBuildInfo in the ManagedBuildManager map. Doing this creates a barrier for subsequent
			// ManagedBuildManager#getBuildInfo(...) see Bug 305146 for more
			ManagedBuildManager.setLoaddedBuildInfo(des.getProjectDescription().getProject(), info);
			return cfg.getConfigurationData();
		}
		return null;
	}

	private boolean isPersistedCfg(ICConfigurationDescription cfgDes){
		return cfgDes.getSessionProperty(CFG_PERSISTED_PROPERTY) != null;
	}

	@Override
	public void settingsChanged(SettingsChangeEvent event) {
		BuildLanguageData datas[] = (BuildLanguageData[])event.getRcInfo().getCLanguageDatas();
		IOption option = event.getOption();

		try {
			int type = option.getValueType();
			for(int i = 0; i < datas.length; i++){
				datas[i].optionsChanged(type);
			}
		} catch (BuildException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}

	@Override
	public void removeConfiguration(ICConfigurationDescription des,
			CConfigurationData data,
			IProgressMonitor monitor) {
		IConfiguration cfg = ((BuildConfigurationData)data).getConfiguration();
		ManagedBuildManager.performValueHandlerEvent(cfg, IManagedOptionValueHandler.EVENT_CLOSE);
		IManagedBuildInfo info = getBuildInfo(des);
		IManagedProject mProj = info.getManagedProject();
		mProj.removeConfiguration(cfg.getId());
	}

	@Override
	public void dataCached(ICConfigurationDescription cfgDes,
			CConfigurationData data,
			IProgressMonitor monitor) {
		BuildConfigurationData cfgData = (BuildConfigurationData)data;
		((Configuration)cfgData.getConfiguration()).setConfigurationDescription(cfgDes);
		cfgData.clearCachedData();
	}

}
