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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class DescriptionScannerInfoProvider implements IScannerInfoProvider, ICProjectDescriptionListener {
	private IProject fProject;
	private ICProjectDescription fProjDes;
	private ICConfigurationDescription fCfgDes;
	private Map fIdToLanguageSettingsMap = Collections.synchronizedMap(new HashMap());
	private String fCurrentFileDescriptionId;
	private IScannerInfo fCurrentFileScannerInfo;
	private static final ScannerInfo INEXISTENT_SCANNER_INFO = new ScannerInfo();
	private boolean fInited;
	
	DescriptionScannerInfoProvider(IProject project){
		fProject = project;
		
		CProjectDescriptionManager.getInstance().addListener(this, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADDED);
	}
	
	private void updateProjCfgInfo(ICProjectDescription des){
		fInited = true;
		fProjDes = des;
		if(fProjDes != null){
			fCfgDes = ((CProjectDescription)des).getIndexConfiguration();
		}
		
		fIdToLanguageSettingsMap.clear();
		fCurrentFileDescriptionId = null;
		fCurrentFileScannerInfo = null;
	}

	public IProject getProject(){
		return fProject;
	}

	public IScannerInfo getScannerInformation(IResource resource) {
		if(!fInited)
			updateProjCfgInfo(CProjectDescriptionManager.getInstance().getProjectDescription(fProject, false));
		
		if(fCfgDes == null)
			return INEXISTENT_SCANNER_INFO;

		ICLanguageSetting setting = null;
		ICResourceDescription rcDes = null;
		if(resource.getType() != IResource.PROJECT){
			IPath rcPath = resource.getProjectRelativePath();
			rcDes = fCfgDes.getResourceDescription(rcPath, false);
	
			if(rcDes.getType() == ICSettingBase.SETTING_FILE){
				setting = ((ICFileDescription)rcDes).getLanguageSetting();
			} else {
				if(resource.getType() == IResource.FILE)
					setting = ((ICFolderDescription)rcDes).getLanguageSettingForFile(rcPath.lastSegment());
				else {
					ICLanguageSetting settings[] = ((ICFolderDescription)rcDes).getLanguageSettings();
					if(settings.length > 0){
						setting = settings[0];
					}
				}
			}
		}
		return getScannerInfo(rcDes, setting);
	}
	
	private IScannerInfo getScannerInfo(ICResourceDescription rcDes, ICLanguageSetting ls){
		Object mapKey = ls != null ? ls.getId() : null;
//		if(ls == null)
//			return INEXISTENT_SCANNER_INFO;
		boolean useMap = rcDes == null || rcDes.getType() == ICSettingBase.SETTING_FOLDER;
		
		IScannerInfo info;
		if(useMap)
			info = (IScannerInfo)fIdToLanguageSettingsMap.get(mapKey);
		else {
			if(fCurrentFileScannerInfo != null){
				if(rcDes.getId().equals(fCurrentFileDescriptionId))
					info = fCurrentFileScannerInfo;
				else {
					info = null;
					fCurrentFileScannerInfo = null;
					fCurrentFileDescriptionId = null;
				}
			} else {
				info = null;
			}
		}
		if(info == null){
			info = createScannerInfo(ls);
			if(useMap)
				fIdToLanguageSettingsMap.put(mapKey, info);
			else {
				fCurrentFileScannerInfo = info;
				fCurrentFileDescriptionId = rcDes.getId();
			}
		}
		return info;
	}
	
	private static ICLanguageSettingPathEntry[] getPathEntries(ICLanguageSetting ls, int kind){
		ICLanguageSettingEntry entries[] = ls.getResolvedSettingEntries(kind);
		ICLanguageSettingPathEntry pathEntries[] = new ICLanguageSettingPathEntry[entries.length];
		System.arraycopy(entries, 0, pathEntries, 0, entries.length);
		
		return pathEntries;
	}

	private static ICMacroEntry[] getMacroEntries(ICLanguageSetting ls){
		ICLanguageSettingEntry entries[] = ls.getResolvedSettingEntries(ICLanguageSettingEntry.MACRO);
		ICMacroEntry macroEntries[] = new ICMacroEntry[entries.length];
		System.arraycopy(entries, 0, macroEntries, 0, entries.length);
		
		return macroEntries;
	}

	private IScannerInfo createProjectStannerInfo(){
		ICFolderDescription foDes = fCfgDes.getRootFolderDescription();
		ICLanguageSetting[] lSettings = foDes.getLanguageSettings();
		ICLanguageSettingPathEntry pathEntries[] = getPathEntries(lSettings, ICLanguageSettingEntry.INCLUDE_PATH);
		String incs[] = getValues(pathEntries);
		
		pathEntries = getPathEntries(lSettings, ICLanguageSettingEntry.INCLUDE_FILE);
		String incFiles[] = getValues(pathEntries);

		pathEntries = getPathEntries(lSettings, ICLanguageSettingEntry.MACRO_FILE);
		String macroFiles[] = getValues(pathEntries);
		
		ICMacroEntry macroEntries[] = getMacroEntries(lSettings);
		Map macrosMap = getValues(macroEntries);
		
		return new ExtendedScannerInfo(macrosMap, incs, macroFiles, incFiles);
	}
	
	
	private ICMacroEntry[] getMacroEntries(ICLanguageSetting[] settings){
		LinkedHashSet set = getEntriesSet(ICLanguageSettingEntry.MACRO, settings);
		return (ICMacroEntry[])set.toArray(new ICMacroEntry[set.size()]);
	}

	private ICLanguageSettingPathEntry[] getPathEntries(ICLanguageSetting[] settings, int kind){
		LinkedHashSet set = getEntriesSet(kind, settings);
		return (ICLanguageSettingPathEntry[])set.toArray(new ICLanguageSettingPathEntry[set.size()]);
	}
	
	private LinkedHashSet getEntriesSet(int kind, ICLanguageSetting[] settings){
		LinkedHashSet set = new LinkedHashSet();
		ICLanguageSettingEntry[] langEntries;
		for(int i = 0; i < settings.length; i++){
			langEntries = settings[i].getResolvedSettingEntries(kind);
			if(langEntries.length != 0){
				set.addAll(Arrays.asList(langEntries));
			}
		}
		return set;
	}

	private IScannerInfo createScannerInfo(ICLanguageSetting ls){
		if(ls == null)
			return createProjectStannerInfo();
		
		ICLanguageSettingPathEntry pathEntries[] = getPathEntries(ls, ICLanguageSettingEntry.INCLUDE_PATH);
		String incs[] = getValues(pathEntries);
		
		pathEntries = getPathEntries(ls, ICLanguageSettingEntry.INCLUDE_FILE);
		String incFiles[] = getValues(pathEntries);

		pathEntries = getPathEntries(ls, ICLanguageSettingEntry.MACRO_FILE);
		String macroFiles[] = getValues(pathEntries);
		
		ICMacroEntry macroEntries[] = getMacroEntries(ls);
		Map macrosMap = getValues(macroEntries);
		
		return new ExtendedScannerInfo(macrosMap, incs, macroFiles, incFiles);
	}

	private Map getValues(ICMacroEntry macroEntries[]){
		Map macrosMap = new HashMap(macroEntries.length);
		String name;
		String value;
		
		for(int i = 0; i < macroEntries.length; i++){
			name = macroEntries[i].getName();
			value = macroEntries[i].getValue();
			macrosMap.put(name, value);
		}
		return macrosMap;
	}

	private String[] getValues(ICLanguageSettingPathEntry pathEntries[]){
		String values[] = new String[pathEntries.length];
		IPath path;
		int num = 0;
		for(int i = 0; i < pathEntries.length; i++){
			String p = pathEntries[i].getValue();
			if(p == null)
				continue;
			//TODO: obtain location from pathEntries when entries are resolved
			path = new Path(p);//pathEntries[i].getLocation();
			if(pathEntries[i].isValueWorkspacePath()){
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource rc = root.findMember(path);
				if(rc != null){
					path = rc.getLocation(); 
				}
			}
			if(path != null)
				values[num++] = path.toOSString();
		}

		if(num < pathEntries.length){
			String tmp[] = new String[num];
			System.arraycopy(values, 0, tmp, 0, num);
			values = tmp;
		}
		
		return values;
	}

	public void subscribe(IResource resource,
			IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	public void unsubscribe(IResource resource,
			IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}
	
	public void close(){
		CProjectDescriptionManager.getInstance().removeListener(this);
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		if(!event.getProject().equals(fProject))
			return;

		//TODO: check delta and notify listeners
		
		updateProjCfgInfo(event.getNewCProjectDescription());
	}

}
