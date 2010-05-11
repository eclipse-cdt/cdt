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
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
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
	private Map<Object, IScannerInfo> fIdToLanguageSettingsMap = Collections.synchronizedMap(new HashMap<Object, IScannerInfo>());
	private String fCurrentFileDescriptionId;
	private IScannerInfo fCurrentFileScannerInfo;
	private static final ScannerInfo INEXISTENT_SCANNER_INFO = new ScannerInfo();
	private boolean fInited;

	DescriptionScannerInfoProvider(IProject project){
		fProject = project;

		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADED);
	}

	private void updateProjCfgInfo(ICProjectDescription des){
		fInited = true;
		fProjDes = des;
		if(fProjDes != null){
			fCfgDes = des.getDefaultSettingConfiguration();
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
			info = fIdToLanguageSettingsMap.get(mapKey);
		else {
			if(fCurrentFileScannerInfo != null && rcDes != null){
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
			else if (rcDes != null){
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
		ICLanguageSettingEntry entries[] = ls.getResolvedSettingEntries(ICSettingEntry.MACRO);
		ICMacroEntry macroEntries[] = new ICMacroEntry[entries.length];
		System.arraycopy(entries, 0, macroEntries, 0, entries.length);

		return macroEntries;
	}

	private IScannerInfo createProjectScannerInfo(){
		ICFolderDescription foDes = fCfgDes.getRootFolderDescription();
		ICLanguageSetting[] lSettings = foDes.getLanguageSettings();
		ICLanguageSettingPathEntry pathEntries[] = getPathEntries(lSettings, ICSettingEntry.INCLUDE_PATH);
		String incs[] = getValues(pathEntries);

		pathEntries = getPathEntries(lSettings, ICSettingEntry.INCLUDE_FILE);
		String incFiles[] = getValues(pathEntries);

		pathEntries = getPathEntries(lSettings, ICSettingEntry.MACRO_FILE);
		String macroFiles[] = getValues(pathEntries);

		ICMacroEntry macroEntries[] = getMacroEntries(lSettings);
		Map<String, String> macrosMap = getValues(macroEntries);

		return new ExtendedScannerInfo(macrosMap, incs, macroFiles, incFiles);
	}


	private ICMacroEntry[] getMacroEntries(ICLanguageSetting[] settings){
		LinkedHashSet<ICLanguageSettingEntry> set = getEntriesSet(ICSettingEntry.MACRO, settings);
		return set.toArray(new ICMacroEntry[set.size()]);
	}

	private ICLanguageSettingPathEntry[] getPathEntries(ICLanguageSetting[] settings, int kind){
		LinkedHashSet<ICLanguageSettingEntry> set = getEntriesSet(kind, settings);
		return set.toArray(new ICLanguageSettingPathEntry[set.size()]);
	}

	private LinkedHashSet<ICLanguageSettingEntry> getEntriesSet(int kind, ICLanguageSetting[] settings){
		LinkedHashSet<ICLanguageSettingEntry> set = new LinkedHashSet<ICLanguageSettingEntry>();
		ICLanguageSettingEntry[] langEntries;
		for (ICLanguageSetting setting : settings) {
			langEntries = setting.getResolvedSettingEntries(kind);
			if(langEntries.length != 0){
				set.addAll(Arrays.asList(langEntries));
			}
		}
		return set;
	}

	private IScannerInfo createScannerInfo(ICLanguageSetting ls){
		if(ls == null)
			return createProjectScannerInfo();

		ICLanguageSettingPathEntry pathEntries[] = getPathEntries(ls, ICSettingEntry.INCLUDE_PATH);
		String incs[] = getValues(pathEntries);

		pathEntries = getPathEntries(ls, ICSettingEntry.INCLUDE_FILE);
		String incFiles[] = getValues(pathEntries);

		pathEntries = getPathEntries(ls, ICSettingEntry.MACRO_FILE);
		String macroFiles[] = getValues(pathEntries);

		ICMacroEntry macroEntries[] = getMacroEntries(ls);
		Map<String, String> macrosMap = getValues(macroEntries);

		return new ExtendedScannerInfo(macrosMap, incs, macroFiles, incFiles);
	}

	private Map<String, String> getValues(ICMacroEntry macroEntries[]){
		Map<String, String> macrosMap = new HashMap<String, String>(macroEntries.length);
		String name;
		String value;

		for (ICMacroEntry macroEntry : macroEntries) {
			name = macroEntry.getName();
			value = macroEntry.getValue();
			macrosMap.put(name, value);
		}
		return macrosMap;
	}

	private String[] getValues(ICLanguageSettingPathEntry pathEntries[]){
		String values[] = new String[pathEntries.length];
		IPath path;
		int num = 0;
		for (ICLanguageSettingPathEntry pathEntry : pathEntries) {
			String p = pathEntry.getValue();
			if(p == null)
				continue;
			//TODO: obtain location from pathEntries when entries are resolved
			path = new Path(p);//p.getLocation();
			if(pathEntry.isValueWorkspacePath()){
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource rc = root.findMember(path);
				if(rc != null){
					path = rc.getLocation();
				}
			} else if (!path.isAbsolute()) {
				IPath projLocation = fProject != null ? fProject.getLocation() : null;
				if(projLocation != null)
					path = projLocation.append(path);
			}
			if(path != null)
				values[num++] = p;
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
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		if(!event.getProject().equals(fProject))
			return;

		//TODO: check delta and notify listeners

		updateProjCfgInfo(event.getNewCProjectDescription());
	}

}
