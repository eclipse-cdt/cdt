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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator.ReferenceSettingsInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigInfoFactory2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.BuilderFactory;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ProjectConverter implements ICProjectConverter {
	private final static String OLD_MAKE_BUILDER_ID = "org.eclipse.cdt.make.core.makeBuilder";	//$NON-NLS-1$
	private final static String OLD_MAKE_NATURE_ID = "org.eclipse.cdt.make.core.makeNature";	//$NON-NLS-1$
	private final static String OLD_MNG_BUILDER_ID = "org.eclipse.cdt.managedbuilder.core.genmakebuilder";	//$NON-NLS-1$
	private final static String OLD_MNG_NATURE_ID = "org.eclipse.cdt.managedbuilder.core.managedBuildNature";	//$NON-NLS-1$
	private final static String OLD_DISCOVERY_MODULE_ID = "scannerConfiguration";	//$NON-NLS-1$
	private final static String OLD_BINARY_PARSER_ID = "org.eclipse.cdt.core.BinaryParser";	//$NON-NLS-1$
	private final static String OLD_ERROR_PARSER_ID = "org.eclipse.cdt.core.ErrorParser";	//$NON-NLS-1$
	private final static String OLD_PATH_ENTRY_ID = "org.eclipse.cdt.core.pathentry"; //$NON-NLS-1$
	private final static String OLD_DISCOVERY_NATURE_ID = "org.eclipse.cdt.make.core.ScannerConfigNature"; //$NON-NLS-1$
	private final static String OLD_DISCOVERY_BUILDER_ID = "org.eclipse.cdt.make.core.ScannerConfigBuilder"; //$NON-NLS-1$

	
	public boolean canConvertProject(IProject project, String oldOwnerId, ICProjectDescription oldDes) {
		try {
			if(oldOwnerId == null || oldDes == null)
				return false;
		
			IProjectDescription eDes = project.getDescription();
			Set natureSet = new HashSet(Arrays.asList(eDes.getNatureIds()));
			if(natureSet.contains(OLD_MAKE_NATURE_ID))
				return true;
			
			if(natureSet.contains(OLD_MNG_NATURE_ID))
				return true;
			
		} catch (CoreException e) {
		}
		
		return false;
//		return ManagedBuildManager.canGetBuildInfo(project);
	}

	public ICProjectDescription convertProject(IProject project, IProjectDescription eDes, String oldOwnerId, ICProjectDescription oldDes)
			throws CoreException {
		Set natureSet = new HashSet(Arrays.asList(eDes.getNatureIds()));
		CoreModel model = CoreModel.getDefault();
		ICProjectDescription newDes = null;
		IManagedBuildInfo info = null;
		boolean adjustBinErrParsers = false;

		if(natureSet.contains(OLD_MNG_NATURE_ID)){
			newDes = model.createProjectDescription(project, false);
			info = convertManagedBuildInfo(project, newDes);
		} else if(natureSet.contains(OLD_MAKE_NATURE_ID)){
			adjustBinErrParsers = true;
			newDes = oldDes;
			ICConfigurationDescription des = newDes.getConfigurations()[0];
			info = ManagedBuildManager.createBuildInfo(project);
			ManagedProject mProj = new ManagedProject(newDes);
			info.setManagedProject(mProj);

			Configuration cfg = ConfigurationDataProvider.getClearPreference(des.getId());
			cfg.applyToManagedProject(mProj);

			des.setConfigurationData(ManagedBuildManager.CFG_DATA_PROVIDER_ID, cfg.getConfigurationData());
		} 

		if(newDes == null || !newDes.isValid() || newDes.getConfigurations().length == 0){
			newDes = null;
		} else {
			boolean changeEDes = false;
			if(natureSet.remove(OLD_MAKE_NATURE_ID))
				changeEDes = true;
			if(natureSet.remove(OLD_DISCOVERY_NATURE_ID))
				changeEDes = true;
				
			if(changeEDes)
				eDes.setNatureIds((String[])natureSet.toArray(new String[natureSet.size()]));
			
			changeEDes = false;
			ICommand[] cmds = eDes.getBuildSpec();
			List list = new ArrayList(Arrays.asList(cmds));
			ICommand makeBuilderCmd = null;
			for(Iterator iter = list.iterator(); iter.hasNext();){
				ICommand cmd = (ICommand)iter.next();
				if(OLD_MAKE_BUILDER_ID.equals(cmd.getBuilderName())){
					makeBuilderCmd = cmd;
					iter.remove();
					changeEDes = true;
				} else if(OLD_DISCOVERY_BUILDER_ID.equals(cmd.getBuilderName())){
					iter.remove();
					changeEDes = true;
				}
			}
			
			ICConfigurationDescription cfgDess[] = newDes.getConfigurations();
			for(int i = 0; i < cfgDess.length; i++){
				ICConfigurationDescription cfgDes = cfgDess[i];
				BuildConfigurationData data = (BuildConfigurationData)cfgDes.getConfigurationData();
				IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

				if(makeBuilderCmd != null)
					loadBuilderSettings(cfg, makeBuilderCmd);
				
				loadDiscoveryOptions(cfgDes, cfg);
				
				loadPathEntryInfo(project, cfgDes, data);
				
				if(adjustBinErrParsers){
					ICConfigExtensionReference refs[] = cfgDes.get(OLD_ERROR_PARSER_ID);
					String ids[] = idsFromRefs(refs);
					data.getTargetPlatformData().setBinaryParserIds(ids);
					
					refs = cfgDes.get(OLD_ERROR_PARSER_ID);
					ids = idsFromRefs(refs);
					data.getBuildData().setErrorParserIDs(ids);
				}
				
				try {
					ConfigurationDataProvider.writeConfiguration(cfgDes, data);
				} catch (CoreException e){
				}
			}
			
			if(changeEDes){
				cmds = (ICommand[])list.toArray(new ICommand[list.size()]);
				eDes.setBuildSpec(cmds);
			}
			
			info.setValid(true);
			
			
			try {
				ManagedBuildManager.setLoaddedBuildInfo(project, info);
			} catch (Exception e) {
			}
		}

		return newDes;
	}
	
	private void loadPathEntryInfo(IProject project, ICConfigurationDescription des, CConfigurationData data){
		try {
			ICStorageElement el = des.getStorage(OLD_PATH_ENTRY_ID, false);
			if(el != null){
				IPathEntry[] entries = PathEntryTranslator.decodePathEntries(project, el);
				if(entries.length != 0){
					List list = new ArrayList(Arrays.asList(entries));
					for(Iterator iter = list.iterator(); iter.hasNext();){
						IPathEntry entry = (IPathEntry)iter.next();
						if(entry.getEntryKind() == IPathEntry.CDT_CONTAINER){
							iter.remove();
							continue;
						}
					}
					
					if(list.size() != 0){
						PathEntryTranslator tr = new PathEntryTranslator(project, data);
						entries = (IPathEntry[])list.toArray(new IPathEntry[list.size()]);
						ReferenceSettingsInfo refInfo = tr.applyPathEntries(entries, null, PathEntryTranslator.OP_REPLACE);
						ICExternalSetting extSettings[] = refInfo.getExternalSettings();
						des.removeExternalSettings();
						if(extSettings.length != 0){
							ICExternalSetting setting;
							for(int i = 0; i < extSettings.length; i++){
								setting = extSettings[i];
								des.createExternalSetting(setting.getCompatibleLanguageIds(), 
										setting.getCompatibleContentTypeIds(), 
										setting.getCompatibleExtensions(), 
										setting.getEntries());
							}
						}

						IPath projPaths[] = refInfo.getReferencedProjectsPaths();
						if(projPaths.length != 0){
							Map map = new HashMap(projPaths.length);
							for(int i = 0; i < projPaths.length; i++){
								map.put(projPaths[i].segment(0), "");	//$NON-NLS-1$
							}
							des.setReferenceInfo(map);
						}
					}
				}
				des.removeStorage(OLD_PATH_ENTRY_ID);
			}
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}
	
	private String[] idsFromRefs(ICConfigExtensionReference refs[]){
		String ids[] = new String[refs.length];
		for(int i = 0; i < ids.length; i++){
			ids[i] = refs[i].getID();
		}
		return ids;
	}
	
	private void loadDiscoveryOptions(ICConfigurationDescription des, IConfiguration cfg){
		try {
			ICStorageElement discoveryStorage = des.getStorage(OLD_DISCOVERY_MODULE_ID, false);
			if(discoveryStorage != null){
				Configuration config = (Configuration)cfg;
				IScannerConfigBuilderInfo2 scannerConfigInfo = ScannerConfigInfoFactory2.create(new InfoContext(cfg), discoveryStorage, ScannerConfigProfileManager.NULL_PROFILE_ID);
				config.setPerRcTypeDiscovery(false);
				config.setScannerConfigInfo(scannerConfigInfo);
				des.removeStorage(OLD_DISCOVERY_MODULE_ID);
			}
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		
		
	}
	
	private void loadBuilderSettings(IConfiguration cfg, ICommand cmd){
		IBuilder builder = BuilderFactory.createBuilderFromCommand(cfg, cmd);
		if(builder.getCommand() != null && builder.getCommand().length() != 0){
			((ToolChain)cfg.getToolChain()).setBuilder((Builder)builder);
		}
	}
	
	private IManagedBuildInfo convertManagedBuildInfo(IProject project, ICProjectDescription newDes){
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfoLegacy(project);
		if(info != null && info.isValid()){
			IManagedProject mProj = info.getManagedProject();
			IConfiguration cfgs[] = mProj.getConfigurations();
			if(cfgs.length != 0){
				Configuration cfg;
				CConfigurationData data;

				for(int i = 0; i < cfgs.length; i++){
					cfg = (Configuration)cfgs[i];
					data = cfg.getConfigurationData();
					try {
						newDes.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
					} catch (WriteAccessException e) {
						ManagedBuilderCorePlugin.log(e);
					} catch (CoreException e) {
						ManagedBuilderCorePlugin.log(e);
					}
					cfg.exportArtifactInfo();
				}
			}
		}
		return info;
	}

}
