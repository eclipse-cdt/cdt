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
package org.eclipse.cdt.core.settings.model.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.COutputEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.internal.core.CdtVarPathEntryVariableManager;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.cdtvariables.CoreVariableSubstitutor;
import org.eclipse.cdt.internal.core.cdtvariables.DefaultVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.internal.core.model.APathEntry;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.cdt.internal.core.model.PathEntry;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

public class PathEntryTranslator {
	public static final int OP_ADD = 1;
	public static final int OP_REMOVE = 2;
	public static final int OP_REPLACE = 3;
	
	public static final int INCLUDE_BUILT_INS = 1;
	public static final int INCLUDE_USER = 1 << 1;
	public static final int INCLUDE_ALL = INCLUDE_BUILT_INS | INCLUDE_USER;
	
	
	static String PATH_ENTRY = "pathentry"; //$NON-NLS-1$
	static String ATTRIBUTE_KIND = "kind"; //$NON-NLS-1$
	static String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	static String ATTRIBUTE_BASE_PATH = "base-path"; //$NON-NLS-1$
	static String ATTRIBUTE_BASE_REF = "base-ref"; //$NON-NLS-1$
	static String ATTRIBUTE_EXPORTED = "exported"; //$NON-NLS-1$
	static String ATTRIBUTE_SOURCEPATH = "sourcepath"; //$NON-NLS-1$
	static String ATTRIBUTE_ROOTPATH = "roopath"; //$NON-NLS-1$
	static String ATTRIBUTE_PREFIXMAPPING = "prefixmapping"; //$NON-NLS-1$
	static String ATTRIBUTE_EXCLUDING = "excluding"; //$NON-NLS-1$
	static String ATTRIBUTE_INCLUDE = "include"; //$NON-NLS-1$
	static String ATTRIBUTE_INCLUDE_FILE= "include-file"; //$NON-NLS-1$
	static String ATTRIBUTE_LIBRARY = "library"; //$NON-NLS-1$
	static String ATTRIBUTE_SYSTEM = "system"; //$NON-NLS-1$
	static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	static String ATTRIBUTE_MACRO_FILE = "macro-file"; //$NON-NLS-1$
	static String VALUE_TRUE = "true"; //$NON-NLS-1$
	
	static final IPathEntry[] NO_PATHENTRIES = new IPathEntry[0];

	private static final char[] SPEC_CHARS = new char[]{'*', '?'}; 
	private PathSettingsContainer fRcDataHolder;
	private IProject fProject;
	private CConfigurationData fCfgData;
	private PathSettingsContainer fTranslatedFilters;
	private Map fResourceMap = new HashMap();
	private IWorkspaceRoot fRoot = ResourcesPlugin.getWorkspace().getRoot();
	
	
	private static class VarSubstitutor extends CoreVariableSubstitutor {
		ICConfigurationDescription fCfg;
		ICdtVariableManager fMngr = CCorePlugin.getDefault().getCdtVariableManager();
		
		public VarSubstitutor(ICConfigurationDescription cfg) {
			super(new DefaultVariableContextInfo(ICoreVariableContextInfo.CONTEXT_CONFIGURATION, cfg), "", " ");  //$NON-NLS-1$  //$NON-NLS-2$
			fCfg = cfg;
		}

		protected ResolvedMacro resolveMacro(ICdtVariable macro)
				throws CdtVariableException {
			if(!CdtVarPathEntryVariableManager.isPathEntryVariable(macro, fCfg, fMngr))
				return super.resolveMacro(macro);
			return new ResolvedMacro(macro.getName(), CdtVariableResolver.createVariableReference(macro.getName()));
		}
	}

	public static final class ReferenceSettingsInfo{
		private IPath[] fRefProjPaths;
		private ICExternalSetting[] fExtSettings;

		public ReferenceSettingsInfo(ICConfigurationDescription des){
			fExtSettings = des.getExternalSettings();
			Map map = des.getReferenceInfo();
			fRefProjPaths = new IPath[map.size()];
			int num = 0;
			for(Iterator iter = map.keySet().iterator(); iter.hasNext();){
				String proj = (String)iter.next();
				fRefProjPaths[num++] = new Path(proj).makeAbsolute();
			}
		}

		public ReferenceSettingsInfo(IPath[] projPaths, ICExternalSetting extSettings[]){
			if(projPaths != null)
				fRefProjPaths = (IPath[])projPaths.clone();
			if(extSettings != null)
				fExtSettings = (ICExternalSetting[])extSettings.clone();
		}

		public IPath[] getReferencedProjectsPaths(){
			if(fRefProjPaths != null)
				return (IPath[])fRefProjPaths.clone();
			return new IPath[0];
		}
		
		public Map getRefProjectsMap(){
			if(fRefProjPaths != null && fRefProjPaths.length != 0){
				Map map = new HashMap(fRefProjPaths.length);
				for(int i = 0; i < fRefProjPaths.length; i++){
					map.put(fRefProjPaths[i].segment(0), ""); //$NON-NLS-1$
				}
				return map;
			}
			return new HashMap(0);
		}
		
		public ICExternalSetting[] getExternalSettings(){
			if(fExtSettings != null)
				return (ICExternalSetting[])fExtSettings.clone();
			return new ICExternalSetting[0];
		}
	}

	private static class PathEntryKyndStore {
		private static final int INDEX_CDT_LIBRARY = 0;
		private static final int INDEX_CDT_PROJECT = 1;
		private static final int INDEX_CDT_SOURCE = 2;
		private static final int INDEX_CDT_INCLUDE = 3;
		private static final int INDEX_CDT_CONTAINER = 4;
		private static final int INDEX_CDT_MACRO = 5;
		private static final int INDEX_CDT_OUTPUT = 6;
		private static final int INDEX_CDT_INCLUDE_FILE = 7;
		private static final int INDEX_CDT_MACRO_FILE = 8;
		
		private static final int STORAGE_SIZE = 9;
	
		private static final int ENTRY_KINDS[] = new int[]{
			IPathEntry.CDT_LIBRARY,
			IPathEntry.CDT_PROJECT,
			IPathEntry.CDT_SOURCE,
			IPathEntry.CDT_INCLUDE,
			IPathEntry.CDT_CONTAINER,
			IPathEntry.CDT_MACRO,
			IPathEntry.CDT_OUTPUT,
			IPathEntry.CDT_INCLUDE_FILE,
			IPathEntry.CDT_MACRO_FILE,
		};
//		private static final int INEXISTENT_INDEX = -1;
		
		private Object[] fEntryStorage = new Object[STORAGE_SIZE];

		private int kindToIndex(int kind){
			switch (kind){
			case IPathEntry.CDT_LIBRARY:
				return INDEX_CDT_LIBRARY;
			case IPathEntry.CDT_PROJECT:
				return INDEX_CDT_PROJECT;
			case IPathEntry.CDT_SOURCE:
				return INDEX_CDT_SOURCE;
			case IPathEntry.CDT_INCLUDE:
				return INDEX_CDT_INCLUDE;
			case IPathEntry.CDT_CONTAINER:
				return INDEX_CDT_CONTAINER;
			case IPathEntry.CDT_MACRO:
				return INDEX_CDT_MACRO;
			case IPathEntry.CDT_OUTPUT:
				return INDEX_CDT_OUTPUT;
			case IPathEntry.CDT_INCLUDE_FILE:
				return INDEX_CDT_INCLUDE_FILE;
			case IPathEntry.CDT_MACRO_FILE:
				return INDEX_CDT_MACRO_FILE;
			}
			throw new IllegalArgumentException(UtilMessages.getString("PathEntryTranslator.0")); //$NON-NLS-1$
		}
		
		public static int[] getSupportedKinds(){
			return (int[])ENTRY_KINDS.clone();
		}

		private int indexToKind(int index){
			switch (index){
			case INDEX_CDT_LIBRARY:
				return IPathEntry.CDT_LIBRARY;
			case INDEX_CDT_PROJECT:
				return IPathEntry.CDT_PROJECT;
			case INDEX_CDT_SOURCE:
				return IPathEntry.CDT_SOURCE;
			case INDEX_CDT_INCLUDE:
				return IPathEntry.CDT_INCLUDE;
			case INDEX_CDT_CONTAINER:
				return IPathEntry.CDT_CONTAINER;
			case INDEX_CDT_MACRO:
				return IPathEntry.CDT_MACRO;
			case INDEX_CDT_OUTPUT:
				return IPathEntry.CDT_OUTPUT;
			case INDEX_CDT_INCLUDE_FILE:
				return IPathEntry.CDT_INCLUDE_FILE;
			case INDEX_CDT_MACRO_FILE:
				return IPathEntry.CDT_MACRO_FILE;
			}
			throw new IllegalArgumentException(UtilMessages.getString("PathEntryTranslator.1")); //$NON-NLS-1$
		}
		public Object get(int kind){
			return fEntryStorage[kindToIndex(kind)];
		}

		public Object put(int kind, Object object){
			int index = kindToIndex(kind);
			Object old = fEntryStorage[index];
			fEntryStorage[index] = object;
			return old;
		}
		
		private class KindBasedInfo implements IKindBasedInfo {
			int fIdex;
			int fKind;
			
			KindBasedInfo(int num, boolean isKind){
				if(isKind){
					fIdex = kindToIndex(num);
					fKind = num;
				} else {
					fIdex = num;
					fKind = indexToKind(num);
				}
			}
		
			public Object getInfo() {
				return fEntryStorage[fIdex];
			}

			public int getKind() {
				return fKind;
			}

			public Object setInfo(Object newInfo) {
				Object old = fEntryStorage[fIdex];
				fEntryStorage[fIdex] = newInfo;
				return old;
			}
			
		}
		
		public IKindBasedInfo[] getContents(){
			IKindBasedInfo infos[] = new IKindBasedInfo[STORAGE_SIZE];
			for(int i = 0; i < STORAGE_SIZE; i++){
				infos[i] = new KindBasedInfo(i, false);
			}
			return infos;
		}
		
		public IKindBasedInfo getInfo(int kind){
			return new KindBasedInfo(kind, true);
		}
		
		public void clear(){
			for(int i = 0; i < STORAGE_SIZE; i++){
				fEntryStorage[i] = null;
			}
		}
	}
	
	private static class LangEntryInfo {
		ICLanguageSettingEntry fLangEntry;
		ResolvedEntry fResolvedEntry;
		
		public LangEntryInfo(ICLanguageSettingEntry lEntry, ResolvedEntry re){
			fLangEntry = lEntry;
			fResolvedEntry = re;
		}
	}
	
	private class RcDesInfo {
		ResourceInfo fRcInfo;
		List fResolvedEntries;
		KindBasedStore fLangEntries;
//		boolean fIsExcluded;

		private RcDesInfo(ResourceInfo rcInfo){
			this.fRcInfo = rcInfo;
			fResolvedEntries = new ArrayList();
			fLangEntries = new KindBasedStore();
		}
		
//		public boolean isExcluded(){
//			return fIsExcluded;
//		}
//		
//		public void setExcluded(boolean excluded){
//			fIsExcluded = excluded;
//		}
		
		public ResolvedEntry[] getResolvedEntries(){
			return (ResolvedEntry[])fResolvedEntries.toArray(new ResolvedEntry[fResolvedEntries.size()]);
		}
		
		public void add(LangEntryInfo info){
			List list = (List)fLangEntries.get(info.fLangEntry.getKind());
			if(list == null){
				list = new ArrayList();
				fLangEntries.put(info.fLangEntry.getKind(), list);
			}
			list.add(info);
		}
		
//		public ICLanguageSettingEntry addLangInfo(ResolvedEntry entry){
//			ICLanguageSettingEntry le = createLangEntry(entry);
//			if(le != null){
//				List list = (List)fLangEntries.get(le.getKind());
//				if(list == null){
//					list = new ArrayList();
//					fLangEntries.put(le.getKind(), list);
//				}
//				list.add(new LangEntryInfo(le, entry));
//			}
//			return le;
//		}
		
		public ICLanguageSettingEntry[] getEntries(int kind){
			List list = (List)fLangEntries.get(kind);
			if(list != null){
				ICLanguageSettingEntry[] entries = new ICLanguageSettingEntry[list.size()];
				for(int i = 0; i < entries.length; i++){
					LangEntryInfo info = (LangEntryInfo)list.get(i);
					entries[i] = info.fLangEntry;
				}
				return entries;
			}
			return new ICLanguageSettingEntry[0];
			
			
		}
		
//		private void initThisEntries

	}
	
	private static ICLanguageSettingEntry createLangEntry(ResolvedEntry entry){
		PathEntryValueInfo value = entry.getResolvedValue();
		int flags = ICLanguageSettingEntry.RESOLVED;
		if(entry.isReadOnly())
			flags |= ICLanguageSettingEntry.READONLY;
		if(entry.isBuiltIn())
			flags |= ICLanguageSettingEntry.BUILTIN;
		
		switch(entry.fEntry.getEntryKind()){
			case IPathEntry.CDT_LIBRARY:{
				ILibraryEntry libEntry = (ILibraryEntry)entry.fEntry;
				IPath path = value.getFullPath();
				if(path != null){
					flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
				} else {
					path = value.getLocation();
				}
				
				if(path != null){
					return new CLibraryFileEntry(value.getName(), flags, 
							libEntry.getSourceAttachmentPath(),
							libEntry.getSourceAttachmentRootPath(),
							libEntry.getSourceAttachmentPrefixMapping());
				}
				break;
			}
//			case IPathEntry.CDT_PROJECT:
//				return ICLanguageSettingEntry;
//			case IPathEntry.CDT_SOURCE:
//				return INDEX_CDT_SOURCE;
			case IPathEntry.CDT_INCLUDE:{
				IPath path = value.getFullPath();
				if(path != null){
					flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
				} else {
					path = value.getLocation();
				}
				
				if(path != null){
					return new CIncludePathEntry(value.getName(), flags);
				}
				break;
			}
//			case IPathEntry.CDT_CONTAINER:
//				return INDEX_CDT_CONTAINER;
			case IPathEntry.CDT_MACRO:
				String name = value.getName();
				if(name.length() != 0){
					String mValue = value.getValue();
					return new CMacroEntry(name, mValue, flags);
				}
				break;
//			case IPathEntry.CDT_OUTPUT:
//				return INDEX_CDT_OUTPUT;
			case IPathEntry.CDT_INCLUDE_FILE:{
				IPath path = value.getFullPath();
				if(path != null){
					flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
				} else {
					path = value.getLocation();
				}
				
				if(path != null){
					return new CIncludeFileEntry(value.getName(), flags);
				}
				break;
			}
			case IPathEntry.CDT_MACRO_FILE:{
				IPath path = value.getFullPath();
				if(path != null){
					flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
				} else {
					path = value.getLocation();
				}
				
				if(path != null){
					return new CMacroFileEntry(value.getName(), flags);
				}
				break;
			}
		}
		return null;
	}

	private class ResourceInfo {
		IResource fRc;
		boolean fExists;
		
		public ResourceInfo(IResource rc, boolean exists) {
			fRc = rc;
			fExists = exists;
		}
	}
	
	private class PathEntryValueInfo {
		private ResourceInfo fResourceInfo;
//		private IPath fFullPath;
		private IPath fLocation;
		private String fName;
		private String fValue;
		private ResolvedEntry fResolvedEntry;
		
		private PathEntryValueInfo(ResolvedEntry rEntry){
			fResolvedEntry = rEntry;
			
			init();
		}
		
		public IPath getFullPath(){
			if(fResourceInfo != null)
				return fResourceInfo.fRc.getFullPath();
			return null;
		}
		
		public IPath getLocation(){
			if(fResourceInfo != null)
				return fResourceInfo.fRc.getLocation();
			return fLocation;
		}
		
		public String getName(){
			if(fName != null)
				return fName;
			return ""; //$NON-NLS-1$
		}
		
		public String getValue(){
			if(fValue != null)
				return fValue;
			return ""; //$NON-NLS-1$
		}
		
		private void init() {
			IPathEntry entry = fResolvedEntry.fEntry; 
			int peKind = entry.getEntryKind();
			IPath basePath = null, valuePath = null;
			boolean isFile = false;
			boolean calcPath = false;
			switch(peKind){
			case IPathEntry.CDT_MACRO:
				IMacroEntry me = (IMacroEntry)entry;
				fName = me.getMacroName();
				fValue = me.getMacroValue();
				break;
			case IPathEntry.CDT_LIBRARY:
				isFile = true;
				calcPath = true;
				ILibraryEntry le = (ILibraryEntry)entry;
				basePath = le.getBasePath();
				valuePath = le.getLibraryPath();
				break;
			case IPathEntry.CDT_INCLUDE:
				isFile = false;
				calcPath = true;
				IIncludeEntry ie = (IIncludeEntry)entry;
				basePath = ie.getBasePath();
				valuePath = ie.getIncludePath();
				break;
			case IPathEntry.CDT_INCLUDE_FILE:
				isFile = true;
				calcPath = true;
				IIncludeFileEntry ife = (IIncludeFileEntry)entry;
				basePath = ife.getBasePath();
				valuePath = ife.getIncludeFilePath();
				break;
			case IPathEntry.CDT_MACRO_FILE:
				isFile = true;
				calcPath = true;
				IMacroFileEntry mfe = (IMacroFileEntry)entry;
				basePath = mfe.getBasePath();
				valuePath = mfe.getMacroFilePath();
				break;
			case IPathEntry.CDT_PROJECT:
			case IPathEntry.CDT_SOURCE:
			case IPathEntry.CDT_CONTAINER:
			case IPathEntry.CDT_OUTPUT:
				fResourceInfo = fResolvedEntry.getResourceInfo();
//				fFullPath = fResourceInfo.fRc.getFullPath();
//				fLocation = fResourceInfo.fRc.getLocation();
				
//				fName = fValuePath.toString();
//				fValue = fValuePath.toString();
				break;
			}
			
			if(calcPath){
				do{
		//			IPath p;
		//			IPath inc = getIncludePath();
					IPath unresolvedBase = basePath;
					IPath unresolvedValue = valuePath;
					IPathEntryVariableManager mngr = CCorePlugin.getDefault().getPathEntryVariableManager();
					
					basePath = mngr.resolvePath(basePath);
					valuePath = mngr.resolvePath(valuePath);

					fName = unresolvedBase.isEmpty() ? unresolvedValue.toString() : unresolvedBase.append(unresolvedValue).toString();
					fValue = fName;
					
					if (!basePath.isEmpty()) {
						IPath loc = basePath;
						if (!loc.isAbsolute()) {
							ResourceInfo rcInfo = findResourceInfo(fRoot, loc.append(valuePath), !isFile);
							if (rcInfo.fExists) {
								fResourceInfo = rcInfo;
								fName = unresolvedBase.append(unresolvedValue).toString();
								fValue = fName;
//								fFullPath = fResourceInfo.fRc.getFullPath();
//								fLocation = fResourceInfo.fRc.getLocation();
//								fName = fValuePath.toString();
//								fValue = fValuePath.toString();
								break;							
							}
						}
						fLocation = loc.append(valuePath);
//						fName = fValuePath.toString();
//						fValue = fValuePath.toString();
						break;
					}
					
//					p = inc;
		
					if (!valuePath.isAbsolute()) {
						ResourceInfo rcInfo = fResolvedEntry.getResourceInfo();
//						
//						IPath resPath = getPath();
//						IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(resPath);
						if (rcInfo.fExists) {
							if (rcInfo.fRc.getType() == IResource.FILE) {
								rcInfo = findResourceInfo(fRoot, rcInfo.fRc.getFullPath().removeLastSegments(1), true);
							}
							IPath location = rcInfo.fRc.getLocation();
							if (location != null && rcInfo.fRc.getType() != IResource.FILE) {
								rcInfo = findResourceInfo((IContainer)rcInfo.fRc, valuePath, !isFile);
								fResourceInfo = rcInfo;
//								fFullPath = fResourceInfo.fRc.getFullPath();
//								fLocation = fResourceInfo.fRc.getLocation();
								break;
							}
						}
					}
					
					fLocation = valuePath;
				}while(false);
			}
		}
		
		public boolean isWorkspacePath(){
			return fResourceInfo != null;
		}
		
		
	}

	private class ResolvedEntry {
		private IPathEntry fEntry;
		private ResourceInfo fResourceInfo;
		private ResourceInfo[] fFilterInfos;
		private PathEntryValueInfo fResolvedValue;
		private PathEntryResolveInfoElement fResolveElement;
		private boolean fIsReadOnly;
		private boolean fIsBuiltIn;

		public ResolvedEntry(IPathEntry entry, PathEntryResolveInfoElement resolveElement) {
			fEntry = entry;
			fResolveElement = resolveElement;
			fIsReadOnly = areEntriesReadOnly(fResolveElement);
			fIsBuiltIn = fIsReadOnly;
		}

		public ResolvedEntry(IPathEntry entry, boolean isReadOnly) {
			fEntry = entry;
			fIsReadOnly = isReadOnly;
		}

		public boolean isReadOnly(){
			return fIsReadOnly;
		}

		public boolean isBuiltIn(){
			return fIsBuiltIn;
		}

		public PathEntryResolveInfoElement getResolveInfoElement(){
			return fResolveElement;
		}
		
		public ResourceInfo getResourceInfo(){
			if(fResourceInfo == null){
				fResourceInfo = findResourceInfo(fRoot, getEntryFullPath(fEntry), true);
			}
			return fResourceInfo;
		}
		
		public ResourceInfo[] getFilterInfos(){
			if(fFilterInfos == null){
				IPath[] paths = obtainFilters(fEntry);
				if(paths.length == 0){
					fFilterInfos = new ResourceInfo[0];
				} else {
					ResourceInfo rcInfo = getResourceInfo();
					if(rcInfo.fExists){
						if(rcInfo.fRc.getType() == IResource.FILE){
							fFilterInfos = new ResourceInfo[0];
						} else {
							List list = new ArrayList();
							for(int i = 0; i < paths.length; i++){
								list.addAll(Arrays.asList(processFilter((IContainer)rcInfo.fRc, paths[i])));
							}
							fFilterInfos = new ResourceInfo[list.size()];
							list.toArray(fFilterInfos);
						}
					} else {
						fFilterInfos = new ResourceInfo[paths.length];
						for(int i = 0; i < paths.length; i++){
							fFilterInfos[i] = processInexistingResourceFilter((IContainer)rcInfo.fRc, paths[i]);
						}
					}
				}
			}
			return fFilterInfos;
		}
		
		private ResourceInfo[] processFilter(IContainer container, IPath path){
			return resolveFilter(container, path);
		}

		private ResourceInfo processInexistingResourceFilter(IContainer container, IPath path){
			IFolder f = container.getFolder(path);
			ResourceInfo rcInfo = new ResourceInfo(f, false);
			addRcInfoToMap(rcInfo);
			
			addResolvedFilterToMap(container.getFullPath(), new ResourceInfo[]{rcInfo}, true);
			return rcInfo;
		}

		public IPathEntry getEntry(){
			return fEntry;
		}
		
		public PathEntryValueInfo getResolvedValue(){
			if(fResolvedValue == null){
				fResolvedValue = new PathEntryValueInfo(this);
			}
			return fResolvedValue;
		}

		
	}
	
	private static class PathEntryComposer {
		private IPath fPath;
		private ICSettingEntry fLangEntry;
		private Set fFiltersSet;
		private boolean fIsExported;
		private IProject fProject;
//		private ICConfigurationDescription fCfg;

		PathEntryComposer(String projName, IProject project/*, ICConfigurationDescription cfg*/){
			this(new Path(projName).makeAbsolute(), project/*, cfg*/);
		}

		PathEntryComposer(IPath path, IProject project/*, ICConfigurationDescription cfg*/){
			fPath = toProjectPath(path);
			fProject = project;
//			fCfg = cfg;
		}

		private static IPath toProjectPath(IPath path){
			if(path.segmentCount() > 1)
				path = new Path(path.segment(0));

			return path.makeAbsolute();
		}

		PathEntryComposer(ICExclusionPatternPathEntry entry, IProject project/*, ICConfigurationDescription cfg*/){
			fPath = new Path(entry.getValue());
			fLangEntry = entry;
			fProject = project;
//			fCfg = cfg;
			IPath[] exclusions = entry.getExclusionPatterns();
			if(exclusions.length != 0){
				fFiltersSet = new HashSet(exclusions.length);
				fFiltersSet.addAll(Arrays.asList(entry.getExclusionPatterns()));
			}
		}

		PathEntryComposer(IPath path, ICLanguageSettingEntry entry, boolean exported, IProject project/*, ICConfigurationDescription cfg*/){
			fPath = path;
			fLangEntry = entry;
			fIsExported = exported;
			fProject = project;
//			fCfg = cfg;
		}
		
		public void addFilter(IPath path){
			if(fFiltersSet == null)
				fFiltersSet = new HashSet();
			
			fFiltersSet.add(path);
		}
		
		public ICSettingEntry getSettingEntry(){
			return fLangEntry;
		}
		
		public IPath getPath(){
			return fPath;
		}
		
		public IPath[] getExclusionPatterns(){
			if(fFiltersSet != null)
				return (IPath[])fFiltersSet.toArray(new IPath[fFiltersSet.size()]);
			return new IPath[0];
		}

		private IPath[] getEntryPath(ICSettingEntry entry, ICConfigurationDescription cfg){
			return valueToEntryPath(entry.getName(), (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0, cfg);
		}

		private IPath[] valueToEntryPath(String value, boolean isWsp, ICConfigurationDescription cfg){
			String pathVarValue = resolveKeepingPathEntryFars(value, cfg);
			String resolvedValue = resolveAll(value, cfg);
			IPath resolvedPath = new Path(resolvedValue);
			IPath pathVarPath = new Path(pathVarValue);
			IPath result[] = new IPath[2];
			if(isWsp){
				if(!resolvedPath.isAbsolute()){
					result[0] = fProject.getFullPath().makeRelative();
					result[1] = pathVarPath;
//					path = fProject.getFullPath().append(path);
				} else {
					if(resolvedPath.segmentCount() != 0){
						String projName = resolvedPath.segment(0);
						IPath valuePath = resolvedPath.removeFirstSegments(1).makeRelative();
						if(pathVarPath.segmentCount() != 0){
							String resolvedProjName = projName;
							String varProjName = pathVarPath.segment(0);
							IPath resolvedProjPath = CCorePlugin.getDefault().getPathEntryVariableManager().resolvePath(new Path(varProjName));
							if(resolvedProjPath.segmentCount() == 1 && resolvedProjName.equals(resolvedProjPath.segment(0))){
								projName = varProjName;
								valuePath = pathVarPath.removeFirstSegments(1).makeRelative();
							}
						}
						
						
						result[0] = new Path(projName);
						result[1] = valuePath;
					}
				}
//				path = path.makeRelative();
			} else {
				if(!resolvedPath.isAbsolute()){
					IPath location = fProject.getLocation();
					if(location != null)
						pathVarPath = location.append(pathVarPath);
				}
				result[1] = pathVarPath;
			}
			
			return result;
		}
		
		public IPathEntry toPathEntry(ICConfigurationDescription cfg, boolean keepPathInfo){
			IPath path = keepPathInfo ? fPath : fProject.getFullPath();
			
			if(fLangEntry != null){
				switch(fLangEntry.getKind()){
				case ICLanguageSettingEntry.INCLUDE_FILE:{
						IPath paths[] = getEntryPath(fLangEntry, cfg);
						return CoreModel.newIncludeFileEntry(path, null, paths[0], paths[1], getExclusionPatterns(), fIsExported);
					}
				case ICLanguageSettingEntry.INCLUDE_PATH:{
						IPath paths[] = getEntryPath(fLangEntry, cfg);
						ICIncludePathEntry ipe = (ICIncludePathEntry)fLangEntry;
						return CoreModel.newIncludeEntry(path, paths[0], paths[1], !ipe.isLocal(), getExclusionPatterns(), fIsExported);
					}
				case ICLanguageSettingEntry.MACRO:
					return CoreModel.newMacroEntry(path, fLangEntry.getName(), fLangEntry.getValue(), getExclusionPatterns(), fIsExported);
				case ICLanguageSettingEntry.MACRO_FILE:{
						IPath paths[] = getEntryPath(fLangEntry, cfg);
						return CoreModel.newMacroFileEntry(path, paths[0], null, paths[1], getExclusionPatterns(), fIsExported);
					}
				case ICLanguageSettingEntry.LIBRARY_PATH:
					return null;
				case ICLanguageSettingEntry.LIBRARY_FILE:{
						IPath paths[] = getEntryPath(fLangEntry, cfg);
						return CoreModel.newLibraryEntry(path, paths[0], paths[1], null, null, null, fIsExported);
					}
				case ICLanguageSettingEntry.OUTPUT_PATH:
					return CoreModel.newOutputEntry(fPath, getExclusionPatterns());
				case ICLanguageSettingEntry.SOURCE_PATH:
					return CoreModel.newSourceEntry(fPath, getExclusionPatterns());
				default:
					return null;
				}
			} else if(fPath != null){
				return CoreModel.newProjectEntry(fPath, fIsExported);
			}
			return null;
		}
	}
	
	private static String resolveAll(String value, ICConfigurationDescription cfg){
		try {
			return CCorePlugin.getDefault().getCdtVariableManager().resolveValue(value, "", " ", cfg); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		return value;
	}

	private static String resolveKeepingPathEntryFars(String value, ICConfigurationDescription cfg){
		try {
			VarSubstitutor substitutor = new VarSubstitutor(cfg);
			
			return CdtVariableResolver.resolveToString(value, substitutor);
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		return value;
	}

	private static int lsKindToPeKind(int kind){
		switch(kind){
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return IPathEntry.CDT_INCLUDE_FILE;
		case ICLanguageSettingEntry.INCLUDE_PATH:
			return IPathEntry.CDT_INCLUDE;
		case ICLanguageSettingEntry.MACRO:
			return IPathEntry.CDT_MACRO;
		case ICLanguageSettingEntry.MACRO_FILE:
			return IPathEntry.CDT_MACRO_FILE;
		case ICLanguageSettingEntry.LIBRARY_PATH:
			return 0;
		case ICLanguageSettingEntry.LIBRARY_FILE:
			return IPathEntry.CDT_LIBRARY;
		case ICLanguageSettingEntry.OUTPUT_PATH:
			return IPathEntry.CDT_OUTPUT;
		case ICLanguageSettingEntry.SOURCE_PATH:
			return IPathEntry.CDT_SOURCE;
		default:
			return 0;
		}
	}
	public static class PathEntryCollector {
		private PathSettingsContainer fStorage;
		private KindBasedStore fStore;
		private KindBasedStore fNameKeyMapStore; //utility map, does not contain all entries, only those added explicitly
		private LinkedHashMap fRefProjMap;
		private IProject fProject;
//		private ICConfigurationDescription fCfg;
		
		private PathEntryCollector(IProject project/*, ICConfigurationDescription cfg*/){
			fStorage = PathSettingsContainer.createRootContainer();
			fStorage.setValue(this);
			fStore = new KindBasedStore(false);
			fNameKeyMapStore = new KindBasedStore(false);
//			fCfg = cfg;
			fProject = project;
		}

		private PathEntryCollector(PathSettingsContainer container, KindBasedStore store, IProject project/*, ICConfigurationDescription cfg*/){
			fStorage = container;
			fStore = store;
			fNameKeyMapStore = new KindBasedStore(false);
//			fCfg = cfg;
			fProject = project;
		}
		
		public void setSourceOutputEntries(int kind, ICExclusionPatternPathEntry entries[]){
			Map map = getEntriesMap(kind, true);
			Map nameKeyMap = getEntriesNameKeyMap(kind, true);
			for(int i = 0; i < entries.length; i++){
				ICExclusionPatternPathEntry entry = entries[i];
				entry = CDataUtil.makeAbsolute(fProject, entry, true);
				EntryNameKey nameKey = new EntryNameKey(entry);
				PathEntryComposer old = (PathEntryComposer)nameKeyMap.get(nameKey);
				if(old != null){
					entry = CDataUtil.addRemoveExclusionsToEntry(entry, 
							((ICExclusionPatternPathEntry)old.fLangEntry).getExclusionPatterns(), 
							true);
				}
				PathEntryComposer newComposer = new PathEntryComposer(entry, fProject/*, fCfg*/); 
				map.put(entry, newComposer);
				nameKeyMap.put(nameKey, newComposer);
			}
		}
		
		public void setRefProjects(IPath []paths){
			if(paths == null || paths.length == 0)
				fRefProjMap = null;
			else {
				fRefProjMap = new LinkedHashMap();
				for(int i = 0; i < paths.length; i++){
					PathEntryComposer cs = new PathEntryComposer(paths[i], fProject/*, fCfg*/);
					IPath path = cs.getPath();
					fRefProjMap.put(path, cs);
				}
			}
		}
		
		public PathEntryCollector createChild(IPath path){
			if(path.segmentCount() == 0)
				return this;
			
			PathEntryCollector cr = (PathEntryCollector)fStorage.getChildContainer(path, false, false).getValue();
			if(cr != this){
				IPath basePath = cr.getPath();
				path = path.removeFirstSegments(basePath.segmentCount());
				return cr.createChild(path);
			} 
			
			PathSettingsContainer newContainer = fStorage.getChildContainer(path, true, true);
			KindBasedStore cloneStore = (KindBasedStore)fStore.clone();
			IKindBasedInfo info[] = cloneStore.getContents();
			for(int i = 0; i < info.length; i++){
				LinkedHashMap map = (LinkedHashMap)info[i].getInfo();
				if(map != null){
					info[i].setInfo((LinkedHashMap)map.clone());
				}
			}
			PathEntryCollector newCr = new PathEntryCollector(newContainer, cloneStore, fProject/*, fCfg*/);
			newContainer.setValue(newCr);
			return newCr;
		}
		
		public IPath getPath(){
			return fStorage.getPath();
		}
		
		public void setEntries(int kind, ICLanguageSettingEntry entries[], Set exportedEntries){
			IPath path = getPath();
			HashSet parentSet = getEntriesSetCopy(kind);
			HashSet removedParentSet = (HashSet)parentSet.clone();
			HashSet addedThisSet = new HashSet(Arrays.asList(entries));
			removedParentSet.removeAll(addedThisSet);
			addedThisSet.removeAll(parentSet);
			
			
			if(removedParentSet.size() != 0){
				PathEntryCollector parent = getParent();
				IPath parentPath = parent.getPath();
				
				int segsToRemove = parentPath.segmentCount();
				if(segsToRemove > path.segmentCount())
					segsToRemove = path.segmentCount() - 1;
				if(segsToRemove < 0)
					segsToRemove = 0;
				
				IPath filterPath = path.removeFirstSegments(segsToRemove);
				
				if(parent != null){
					parent.addFilter(kind, filterPath, removedParentSet);
				}
				
				Map map = getEntriesMap(kind, true);
				for(Iterator iter = removedParentSet.iterator(); iter.hasNext();){
					map.remove(iter.next());
				}
			}
			
			if(addedThisSet.size() != 0){
				Map map = getEntriesMap(kind, true);
				IPath fullPath = fProject.getFullPath().append(path);
				for(int i = 0; i < entries.length; i++){
					if(!addedThisSet.remove(entries[i]))
						continue;

					ICLanguageSettingEntry entry = entries[i];
					map.put(entry, new PathEntryComposer(fullPath, entry, exportedEntries.contains(entry), fProject/*, fCfg*/));
				}
			}
		}
		
		private LinkedHashMap getEntriesMap(int kind, boolean create){
			LinkedHashMap map = (LinkedHashMap)fStore.get(kind);
			if(map == null && create){
				map = new LinkedHashMap();
				fStore.put(kind, map);
			}
			return map;
		}
		
		private LinkedHashMap getEntriesNameKeyMap(int kind, boolean create){
			LinkedHashMap map = (LinkedHashMap)fNameKeyMapStore.get(kind);
			if(map == null && create){
				map = new LinkedHashMap();
				fNameKeyMapStore.put(kind, map);
			}
			return map;
		}
		
		private void addFilter(int kind, IPath path, Set entriesSet){
			if(entriesSet.size() == 0)
				return;
			
			Map map = (Map)fStore.get(kind);
			for(Iterator iter = entriesSet.iterator(); iter.hasNext();){
				PathEntryComposer cs = (PathEntryComposer)map.get(iter.next());
				cs.addFilter(path);
			}
		}
		
		public PathEntryCollector getParent(){
			if(fStorage.isRoot())
				return null;
			PathSettingsContainer cr = fStorage.getParentContainer();
			return (PathEntryCollector)cr.getValue();
		}
		
		private HashSet getEntriesSetCopy(int kind){
			Map map = getEntriesMap(kind, false);
			if(map != null){
				return new HashSet(map.keySet());
			}
			return new HashSet(0);
		}
		
		private List getCollectedEntriesList(final int kind){
			final List list = new ArrayList();
			final Set set = new HashSet();
			fStorage.accept(new IPathSettingsContainerVisitor(){

				public boolean visit(PathSettingsContainer container) {
					PathEntryCollector clr = (PathEntryCollector)container.getValue();
					clr.getLocalCollectedEntries(kind, list, set);
					return true;
				}
				
			});
			
			return list;
		}
		
		private void getLocalCollectedEntries(int kind, List list, Set addedEntries){
			Map map = getEntriesMap(kind, false);
			if(map == null)
				return;
			
			for(Iterator iter = map.values().iterator(); iter.hasNext();){
				Object o = iter.next();

				if(addedEntries.add(o)){
					list.add(o);
				}
			}
		}
		
		public List getEntries(int peKind, List list, int flags, ICConfigurationDescription cfg){
			if(list == null){
				list = new ArrayList();
			}
			
			int sKind = peKindToSettingKind(peKind);
			List composerList = null;
			if(sKind != 0){
				composerList = getCollectedEntriesList(sKind);
			} else if(peKind == IPathEntry.CDT_PROJECT){
				if(fRefProjMap != null && fRefProjMap.size() != 0){
					composerList = new ArrayList(fRefProjMap.values()); 
				}
			}
			if(composerList != null){
				PathEntryKyndStore store = new PathEntryKyndStore();
				
				for(Iterator iter = composerList.iterator(); iter.hasNext();){
					PathEntryComposer cs = (PathEntryComposer)iter.next();
					ICSettingEntry entry = cs.getSettingEntry();
					if(checkFilter(cs, entry, flags)){
						IPathEntry pe = null;
						if(isBuiltIn(entry) && cs.getPath().segmentCount() > 1){
							String name = entry.getName();
							Map map = (Map)store.get(peKind);
							if(map == null){
								map = new HashMap();
								store.put(peKind, map);
							}
							if(!map.containsKey(name)){
								pe = cs.toPathEntry(cfg, false);
								if(pe != null){
									map.put(name, pe);
								}
							}
						} else {
							pe = cs.toPathEntry(cfg, true); 
						}
						if(pe != null)
							list.add(pe);
					}
				}
			}
			
			return list;
		}
		
		private static boolean checkFilter(PathEntryComposer cs, ICSettingEntry entry, int flags){
			boolean builtIn = isBuiltIn(entry);
			
//			if(builtIn && cs.getPath().segmentCount() > 1)
//				return false;
			if((flags & INCLUDE_BUILT_INS) != 0 && builtIn)
				return true;
			if((flags & INCLUDE_USER) != 0 && !builtIn)
				return true;
			return false;
		}
		
		private static boolean isBuiltIn(ICSettingEntry entry){
			return entry != null ?
					entry.isBuiltIn() || entry.isReadOnly() : false;
		}
		
		
		public List getEntries(List list, int flags, ICConfigurationDescription cfg){
			if(list == null)
				list = new ArrayList();
			int peKinds[] = PathEntryKyndStore.getSupportedKinds();
			for(int i = 0; i < peKinds.length; i++){
				getEntries(peKinds[i], list, flags, cfg);
			}
			
			return list;
		}
		
		public IPathEntry[] getEntries(int flags, ICConfigurationDescription cfg){
			List list = getEntries(null, flags,cfg);
			IPathEntry[] entries = (IPathEntry[])list.toArray(new IPathEntry[list.size()]);
			return entries;
		}

		
//		public IPathEntry[] getRawEntries(String containerId){
//			List list = getEntries(null, false, false);
//			if(containerId != null)
//				list.add(CoreModel.newContainerEntry(new Path(containerId)));
//			IPathEntry[] entries = (IPathEntry[])list.toArray(new IPathEntry[list.size()]);
//			return entries;
//		}
//	
//		public IPathEntry[] getResolvedEntries(){
//			List list = getEntries(null, true, true);
//			IPathEntry[] entries = (IPathEntry[])list.toArray(new IPathEntry[list.size()]);
//			return entries;
//		}
	}
	
	private static LangEntryInfo createLangEntryInfo(ResolvedEntry entry){
		ICLanguageSettingEntry le = createLangEntry(entry);
		if(le != null){
			return new LangEntryInfo(le, entry);
		}
		return null;
	}

	
	private static boolean areEntriesReadOnly(PathEntryResolveInfoElement el){
		switch(el.getRawEntry().getEntryKind()){
		case IPathEntry.CDT_LIBRARY:
		case IPathEntry.CDT_INCLUDE:
		case IPathEntry.CDT_MACRO:
		case IPathEntry.CDT_INCLUDE_FILE:
		case IPathEntry.CDT_MACRO_FILE:
		case IPathEntry.CDT_SOURCE:
		case IPathEntry.CDT_OUTPUT:
			return false;
//		case IPathEntry.CDT_PROJECT:
//		case IPathEntry.CDT_CONTAINER:
		}
		return true;
	}
	
	
	private IPath getEntryFullPath(IPathEntry entry){
		IPath path = entry.getPath();
		if(path == null)
			return fProject.getFullPath();
		else if(path.isAbsolute())
			return path;
		return fProject.getFullPath().append(path);
		
	}
	
	private IPath[] obtainFilters(IPathEntry entry){
		switch (entry.getEntryKind()) {
		case IPathEntry.CDT_INCLUDE:
		case IPathEntry.CDT_INCLUDE_FILE:
		case IPathEntry.CDT_MACRO:
		case IPathEntry.CDT_OUTPUT:
		case IPathEntry.CDT_SOURCE:
			return ((APathEntry)entry).getExclusionPatterns();
		}
		return new IPath[0];
	}
	
	public PathEntryTranslator(IProject project, CConfigurationData data){
		fProject = project;
		fCfgData = data;
		fRcDataHolder = createRcDataHolder(data);
		fTranslatedFilters = PathSettingsContainer.createRootContainer();
		fTranslatedFilters.setValue(new ResourceInfo[]{new ResourceInfo(fRoot, true)});
	}
	
	private static PathSettingsContainer createRcDataHolder(CConfigurationData data){
		return CDataUtil.createRcDataHolder(data);
	}
	
	public ReferenceSettingsInfo applyPathEntries(PathEntryResolveInfo info, int op){
		ResolvedEntry[] rEntries = getResolvedEntries(info);
		return addPathEntries(rEntries, op);
	}
	
//	public static ICSourceEntry[] calculateSourceEntriesFromPaths(IProject project, PathSettingsContainer rcDatas, IPath paths[]){
//		if(paths == null || paths.length == 0)
//			paths = new IPath[]{new Path("")}; //$NON-NLS-1$
//		
////		Set set = new HashSet(paths.length);
//		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
//		IPath pi, pj;
//		List entriesList = new ArrayList(paths.length);
//		IPath projPath = project != null ? project.getFullPath() : null;
//		
//		for(int i = 0; i < paths.length; i++){
//			pi = paths[i];
////			set.clear();
//			cr.removeChildren();
//			cr.setValue(null);
//			for(int j = 0; j < paths.length; j++){
//				pj = paths[j];
//				if(pi != pj && pi.isPrefixOf(pj)){
////					set.add(pj);
//					cr.getChildContainer(pj, true, true);
//				}
//			}
//			
//			PathSettingsContainer children[] = rcDatas.getDirectChildrenForPath(pi);
//			for(int k = 0; k < children.length; k++){
//				PathSettingsContainer child = children[k];
//				IPath childPath = child.getPath();
//				PathSettingsContainer parentExclusion = cr.getChildContainer(childPath, false, false);
//				IPath parentExclusionPath = parentExclusion.getPath();
//				if(parentExclusionPath.segmentCount() > 0 && !parentExclusionPath.equals(childPath) && parentExclusionPath.isPrefixOf(childPath))
//					continue;
//				
//				CResourceData rcData = (CResourceData)child.getValue();
//				if(rcData.isExcluded()){
////					set.add(rcDes.getPath());
//					cr.getChildContainer(childPath, true, true);
//				}
//			}
//			
//			PathSettingsContainer exclusions[] = cr.getChildren(false);
////			IPath exlusionPaths[] = new IPath[set.size()];
//			IPath exlusionPaths[] = new IPath[exclusions.length];
////			int k = 0;
//			int segCount = pi.segmentCount();
////			for(Iterator iter = set.iterator(); iter.hasNext(); k++) {
////				IPath path = (IPath)iter.next();
////				exlusionPaths[k] = path.removeFirstSegments(segCount).makeRelative();
////			}
//			for(int k = 0; k < exlusionPaths.length; k++) {
//				exlusionPaths[k] = exclusions[k].getPath().removeFirstSegments(segCount).makeRelative();
//			}
//			if(projPath != null)
//				pi = projPath.append(pi);
//			entriesList.add(new CSourceEntry(pi, exlusionPaths, 0));
//		}
//
//		return (ICSourceEntry[])entriesList.toArray(new ICSourceEntry[entriesList.size()]);
//	}
	
//	private IPath[] setSourceEntries(PathSettingsContainer crontainer, List res) {
////		ICSourceEntry entry;
//		IPath entryPath;
////		IPath paths[];
//		Set srcPathSet = new HashSet();
//		IPath projPath = fProject != null ? fProject.getFullPath() : null;
//		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
//		cr.setValue(Boolean.TRUE);
//
////		Map exclusionMap = new HashMap();
//		
////		HashSet pathSet = new HashSet();
//		for(Iterator iter = res.iterator(); iter.hasNext();){
//			ResolvedEntry re = (ResolvedEntry)iter.next();
//			ResourceInfo rcInfo = re.getResourceInfo();
//			entryPath = rcInfo.fRc.getFullPath();
//			if(projPath != null){
//				if(projPath.isPrefixOf(entryPath)){
//					entryPath = entryPath.removeFirstSegments(projPath.segmentCount());
//				} else {
//					continue;
//				}
//			} 
////			else {
////				if(entryPath.segmentCount() > 0)
////					entryPath = entryPath.removeFirstSegments(1);
////				else
////					continue;
////			}
//			if(srcPathSet.add(entryPath)){
//	//			exclusionMap.put(entryPath, Boolean.TRUE);
//				PathSettingsContainer entryCr = cr.getChildContainer(entryPath, true, true);
//				entryCr.setValue(Boolean.TRUE);
//	
//				ResourceInfo[] filters = re.getFilterInfos();
//				
////				paths = entry.getExclusionPatterns();
//				
//				for(int j = 0; j < filters.length; j++){
//					IPath path = filters[j].fRc.getFullPath();
//					path = path.removeFirstSegments(entryPath.segmentCount());
//					PathSettingsContainer exclusion = entryCr.getChildContainer(path, true, true);
//					if(exclusion.getValue() == null)
//						exclusion.setValue(Boolean.FALSE);
//	//				if(null == exclusionMap.get(path))
//	//					exclusionMap.put(path, Boolean.FALSE);
//				}
//			}
//		}
//
////		CConfigurationData data = getConfigurationData(true);
////		data.setSourcePaths((IPath[])srcPathSet.toArray(new IPath[srcPathSet.size()]));
////		ICResourceDescription rcDess[] = getResourceDescriptions();
////		ICResourceDescription rcDes;
//		Set pathSet = new HashSet();
//		PathSettingsContainer children[] = crontainer.getChildren(true);
//		
//		for(int i = 0; i < children.length; i++){
//			PathSettingsContainer child = children[i];
//			RcDesInfo rcDesInfo = (RcDesInfo)child.getValue();
////			rcDes = rcDess[i];
//			IPath path  = child.getPath();
//			pathSet.add(path);
////			Boolean b = (Boolean)exclusionMap.remove(path);
//			Boolean b = (Boolean)cr.getChildContainer(path, false, false).getValue();
//			assert (b != null);
//			if(Boolean.TRUE == b) {
//				if(rcDesInfo.isExcluded())
//					rcDesInfo.setExcluded(false);
//			} else {
//				if(path.segmentCount() != 0)
//					rcDesInfo.setExcluded(true);
//			}
//		}
//		
//		PathSettingsContainer crs[] = cr.getChildren(true);
//		for(int i= 0; i < crs.length; i++){
//			PathSettingsContainer c = crs[i];
//			IPath path = c.getPath();
//			if(!pathSet.remove(path)){
//				Boolean b = (Boolean)c.getValue();
//				assert (b != null);
//				PathSettingsContainer baseCr = crontainer.getChildContainer(path, false, false); 
//				RcDesInfo baseInfo = (RcDesInfo)baseCr.getValue();
//				if(b == Boolean.TRUE){
//					if(baseInfo.isExcluded()){
//						RcDesInfo newInfo = new RcDesInfo(findResourceInfo(fProject, path, true));
//						PathSettingsContainer newCr = crontainer.getChildContainer(path, true, true);
//						newCr.setValue(newInfo);
////						if(newInfo == null){
////							ICResourceDescription fo = getResourceDescription(path, false);
////							if(fo.getType() == ICSettingBase.SETTING_FILE){
////								fo = getResourceDescription(path.removeLastSegments(1), false);
////							}
////							newDes = createFolderDescription(path, (ICFolderDescription)fo);
////						}
//						newInfo.setExcluded(false);
//					}
//				} else {
//					if(!baseInfo.isExcluded()){
////						ICResourceDescription newDes = createResourceDescription(path, base);
//						RcDesInfo newInfo = new RcDesInfo(findResourceInfo(fProject, path, true));
//						PathSettingsContainer newCr = crontainer.getChildContainer(path, true, true);
//						newCr.setValue(newInfo);
//
////						if(newDes == null){
////							ICResourceDescription fo = getResourceDescription(path, false);
////							if(fo.getType() == ICSettingBase.SETTING_FILE){
////								fo = getResourceDescription(path.removeLastSegments(1), false);
////							}
////							newDes = createFolderDescription(path, (ICFolderDescription)fo);
////						}
//						newInfo.setExcluded(true);
//					}
//				}
//			}
//		}
//		return (IPath[])pathSet.toArray(new IPath[pathSet.size()]);
//	}
	
	private RcDesInfo getRcDesInfo(PathSettingsContainer cr, ResourceInfo rcInfo){
		IResource rc = rcInfo.fRc;
		IPath projPath = rc.getProjectRelativePath();
		PathSettingsContainer child = cr.getChildContainer(projPath, true, true);
		RcDesInfo rcDes = (RcDesInfo)child.getValue();
		if(rcDes == null){
			rcDes = new RcDesInfo(rcInfo);
			child.setValue(rcDes);
		}
		return rcDes;
	}
	
	private ReferenceSettingsInfo addPathEntries(ResolvedEntry[] rEntries, int op){
		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
		cr.setValue(new RcDesInfo(new ResourceInfo(fProject, true)));
		List srcList = new ArrayList();
		List outList = new ArrayList();
		List projList = new ArrayList();
		List exportSettingsList = new ArrayList();
		ICSourceEntry srcEntries[] = null;
		ICOutputEntry outEntries[] = null;
//		PathSettingsContainer child;
		ResolvedEntry rEntry;
		IPath projPath;
		IResource rc;
		ResourceInfo rcInfo;
		for(int i = 0; i < rEntries.length; i++){
			rEntry = rEntries[i];
			if(rEntry.isReadOnly())
				continue;
			if(toLanguageEntryKind(rEntry.fEntry.getEntryKind()) == 0){
				switch(rEntry.fEntry.getEntryKind()){
				case IPathEntry.CDT_SOURCE:
					srcList.add(rEntry.fEntry);
					break;
				case IPathEntry.CDT_OUTPUT:
					outList.add(rEntry.fEntry);
					break;
				case IPathEntry.CDT_PROJECT:
					projList.add(rEntry);
					break;
				}
				continue;
			}

			if(rEntry.getEntry().isExported()){
				exportSettingsList.add(rEntry);
			}
			rcInfo = rEntry.getResourceInfo();
			RcDesInfo rcDes = getRcDesInfo(cr, rcInfo);

			rcDes.fResolvedEntries.add(rEntry);
			
			ResourceInfo[] fInfos = rEntry.getFilterInfos();
			for(int k = 0; k < fInfos.length; k++){
				getRcDesInfo(cr, fInfos[k]);
			}
		}
		
		if(srcList.size() != 0){
			srcEntries = toCSourceEntries(srcList);
		} else {
//			srcPaths = new IPath[]{new Path("")}; //$NON-NLS-1$
		}
		if(outList.size() != 0){
			outEntries = toCOutputEntries(outList);
		} else {
//			srcPaths = new IPath[]{new Path("")}; //$NON-NLS-1$
		}
		
//		cr.accept(new IPathSettingsContainerVisitor(){
//
//			public boolean visit(PathSettingsContainer container) {
//				RcDesInfo info = (RcDesInfo)container.getValue();
//				if(info != null){
//					if(info.fResolvedEntries.size() != 0){
//						for(Iterator iter = info.fResolvedEntries.iterator(); iter.hasNext();){
//							ResolvedEntry entry = (ResolvedEntry)iter.next();
//							info.addLangInfo(entry);
//						}
//					}
//				}
//				return true;
//			}
//		});
		
		propagateValues(cr, new ArrayList(0));
		
		//applying settings

		//applySourcePaths(srcPaths, op);
		applyOutputEntries(outEntries, op);
		applySourceEntries(srcEntries, op);
		applyLangSettings(cr, op);
		
		IPath refProjPaths[] = new IPath[projList.size()];
		for(int i = 0; i < refProjPaths.length; i++){
			ResolvedEntry e = (ResolvedEntry)projList.get(i);
			refProjPaths[i] = e.getResourceInfo().fRc.getFullPath();
		}
		
		ICExternalSetting extSettings[];
		if(exportSettingsList.size() != 0){
			extSettings = new ICExternalSetting[1];
			List list = new ArrayList(exportSettingsList.size());
			for(int i = 0; i < exportSettingsList.size(); i++){
				ResolvedEntry re = (ResolvedEntry)exportSettingsList.get(i);
				ICLanguageSettingEntry le = createLangEntry(re);
				if(le != null)
					list.add(le);
			}
			ICLanguageSettingEntry expEntries[] = (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);
			extSettings[0] = new CExternalSetting(null, null, null, expEntries);
		} else {
			extSettings = new ICExternalSetting[0];
		}
		
		return new ReferenceSettingsInfo(refProjPaths, extSettings);
	}
	
	private static ICSourceEntry[] toCSourceEntries(List list){
		ICSourceEntry[] entries = new ICSourceEntry[list.size()];
		for(int i = 0; i < entries.length; i++){
			entries[i] = toCSourceEntry((ISourceEntry)list.get(i), true);
		}
		return entries;
	}
	
	private static ICOutputEntry[] toCOutputEntries(List list){
		ICOutputEntry[] entries = new ICOutputEntry[list.size()];
		for(int i = 0; i < entries.length; i++){
			entries[i] = toCOutputEntry((IOutputEntry)list.get(i), true);
		}
		return entries;
	}

	
	private static ICSourceEntry toCSourceEntry(ISourceEntry entry, boolean makeProjRelative){
		IPath path = entry.getPath();
		if(makeProjRelative && path.isAbsolute())
			path = path.removeFirstSegments(1);
		return new CSourceEntry(path,
				entry.getExclusionPatterns(), 
				ICSettingEntry.VALUE_WORKSPACE_PATH
				| ICSourceEntry.RESOLVED);
	}
	
	private static ICOutputEntry toCOutputEntry(IOutputEntry entry, boolean makeProjRelative){
		IPath path = entry.getPath();
		if(makeProjRelative && path.isAbsolute())
			path = path.removeFirstSegments(1);
		return new COutputEntry(path,
				entry.getExclusionPatterns(), 
				ICSettingEntry.VALUE_WORKSPACE_PATH
				| ICSourceEntry.RESOLVED);
	}
	
	private static ICSettingEntry[] replaceUserEntries(ICSettingEntry[] oldEntries, ICSettingEntry[] newUsrEntries){
		Set set = new LinkedHashSet();
		Class componentType = null;
		
		if(newUsrEntries != null){
			for(int i = 0; i < newUsrEntries.length; i++ ){
				ICSettingEntry entry = newUsrEntries[i];
				if(entry.isBuiltIn() || entry.isReadOnly())
					continue;
				set.add(entry);
			}
			componentType = newUsrEntries.getClass().getComponentType();
		}
		
		if(oldEntries != null){
			for(int i = 0; i < oldEntries.length; i++ ){
				ICSettingEntry entry = oldEntries[i];
				if(entry.isBuiltIn() || entry.isReadOnly())
					set.add(entry);;
			}
			if(componentType == null)
				componentType = oldEntries.getClass().getComponentType();
		}

		if(componentType != null){
			ICSettingEntry[] result = (ICSettingEntry[])Array.newInstance(componentType, set.size());
			set.toArray(result);
			return result;
		}
		return null;
	}

	private void applySourceEntries(ICSourceEntry entries[], int op){
		ICSourceEntry[] oldEntries = fCfgData.getSourceEntries();
		oldEntries = (ICSourceEntry[])CDataUtil.makeRelative(fProject, oldEntries, true);
		entries = (ICSourceEntry[])CDataUtil.makeRelative(fProject, entries, true);
		entries = (ICSourceEntry[])replaceUserEntries(oldEntries, entries);
		
		switch (op) {
		case OP_ADD:
			if(entries != null && entries.length != 0){
				Set set = new LinkedHashSet();
				set.addAll(Arrays.asList(oldEntries));
				set.addAll(Arrays.asList(entries));
				fCfgData.setSourceEntries((ICSourceEntry[])set.toArray(new ICSourceEntry[set.size()]));
			}
			break;
		case OP_REMOVE:
			if(entries != null && entries.length != 0){
				Set set = new HashSet();
				set.addAll(Arrays.asList(oldEntries));
				set.removeAll(Arrays.asList(entries));
				fCfgData.setSourceEntries((ICSourceEntry[])set.toArray(new ICSourceEntry[set.size()]));
			}
			break;
		case OP_REPLACE:
		default:
			if(entries != null){
				fCfgData.setSourceEntries(entries);
			} else {
				fCfgData.setSourceEntries(new ICSourceEntry[0]);
			}
			break;
		}		
	}
	
	private void applyOutputEntries(ICOutputEntry entries[], int op){
		CBuildData bData = fCfgData.getBuildData();
		if(bData == null){
			CCorePlugin.log("PathEntryTranslator: failed to apply output entries: Build Data is null, ignoring..");
			return;
		}
		
		ICOutputEntry[] oldEntries = bData.getOutputDirectories();
		oldEntries = (ICOutputEntry[])CDataUtil.makeRelative(fProject, oldEntries, true);
		entries = (ICOutputEntry[])CDataUtil.makeRelative(fProject, entries, true);
		entries = (ICOutputEntry[])replaceUserEntries(oldEntries, entries);
		
		switch (op) {
		case OP_ADD:
			if(entries != null && entries.length != 0){
				Set set = new LinkedHashSet();
				set.addAll(Arrays.asList(oldEntries));
				set.addAll(Arrays.asList(entries));
				bData.setOutputDirectories((ICOutputEntry[])set.toArray(new ICOutputEntry[set.size()]));
			}
			break;
		case OP_REMOVE:
			if(entries != null && entries.length != 0){
				Set set = new HashSet();
				set.addAll(Arrays.asList(oldEntries));
				set.removeAll(Arrays.asList(entries));
				bData.setOutputDirectories((ICOutputEntry[])set.toArray(new ICOutputEntry[set.size()]));
			}
			break;
		case OP_REPLACE:
		default:
			if(entries != null){
				bData.setOutputDirectories(entries);
			} else {
				bData.setOutputDirectories(new ICOutputEntry[0]);
			}
			break;
		}		
	}
	
	private void applyLangSettings(PathSettingsContainer cr, int op){
		PathSettingsContainer crs[] = cr.getChildren(true);
		for(int i = 0; i < crs.length; i++){
			PathSettingsContainer cur = crs[i];
			RcDesInfo desInfo = (RcDesInfo)cur.getValue();
			try {
				CResourceData rcData = getResourceData(cur.getPath(), true, true);
				applyEntries(rcData, desInfo, op);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		
		CResourceData[] rcDatas = getResourceDatas();
		for(int i = 0; i < rcDatas.length; i++){
			CResourceData rcData = rcDatas[i];
			PathSettingsContainer c = cr.getChildContainer(rcData.getPath(), false, false);
			if(cr.getPath().makeRelative().equals(rcData.getPath().makeRelative())){
				continue;
			}
			
			RcDesInfo desInfo = (RcDesInfo)c.getValue();
//			rcData.setExcluded(desInfo.isExcluded());
			
			applyEntries(rcData, desInfo, op);
		}
	}

	private CResourceData[] getResourceDatas(){
		PathSettingsContainer crs[] = fRcDataHolder.getChildren(true);
		List list = new ArrayList(crs.length);
		for(int i = 0; i < crs.length; i++){
			list.add(crs[i].getValue());
		}
		return (CResourceData[])list.toArray(new CResourceData[list.size()]);
	}

	private CResourceData getResourceData(IPath path, boolean create, boolean exactPath) throws CoreException{
		PathSettingsContainer rcDataH = fRcDataHolder.getChildContainer(path, false, exactPath);
		if(rcDataH != null){
			return (CResourceData)rcDataH.getValue();
		} else if (create) {
			ResourceInfo rcInfo = findResourceInfo(fProject, path, true);
			CResourceData base = getResourceData(path, false, false);
			
			CResourceData newRcData;
			if(rcInfo.fRc.getType() == IResource.FILE){
				if(base.getType() == ICSettingBase.SETTING_FILE){
					newRcData = fCfgData.createFileData(path, (CFileData)base);
				} else {
					CFolderData folderData = (CFolderData)base;
					CLanguageData lDatas[] = folderData.getLanguageDatas();
					CLanguageData baseLData = CDataUtil.findLanguagDataForFile(rcInfo.fRc.getFullPath().lastSegment(), fProject, lDatas);
					newRcData = fCfgData.createFileData(path, folderData, baseLData);
				}
			} else {
				while(base.getType() == ICSettingBase.SETTING_FILE){
					base = getResourceData(base.getPath().removeLastSegments(1), false, false);
				}
				
				newRcData = fCfgData.createFolderData(path, (CFolderData)base);
			}
			
			fRcDataHolder.getChildContainer(path, true, true).setValue(newRcData);
			return newRcData;
		}
		return null;
	}
	
	private void applyEntries(CResourceData data, RcDesInfo info, int op){
		CLanguageData lDatas[] = data.getType() == ICSettingBase.SETTING_FILE ? 
				new CLanguageData[]{((CFileData)data).getLanguageData()} 
				: ((CFolderData)data).getLanguageDatas();
				
		for(int i = 0; i < lDatas.length; i++){
			CLanguageData lData = lDatas[i];
			if(lData == null)
				continue;
			
			applyEntries(lData, info, op);
		}
	}
	
	
	private void applyEntries(CLanguageData lData, RcDesInfo info, int op){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int supported = lData.getSupportedEntryKinds();
		for(int i = 0; i < kinds.length; i++){
			int kind = kinds[i];
			if((supported & kind) == 0)
				continue;
			
			ICLanguageSettingEntry opEntries[] = info.getEntries(kind);
			ICLanguageSettingEntry oldEntries[] = lData.getEntries(kind);
			opEntries = (ICLanguageSettingEntry[])replaceUserEntries(oldEntries, opEntries);
			
			if(op == OP_REPLACE)
				oldEntries = null;
//			ICLanguageSettingEntry oldEntries[] = op != OP_REPLACE ? lData.getEntries(kind) : null;
			ICLanguageSettingEntry result[] = composeNewEntries(oldEntries, opEntries, op);
			lData.setEntries(kind, result);
		}
	}
	
	private ICLanguageSettingEntry[] composeNewEntries(ICLanguageSettingEntry oldEntries[],
			ICLanguageSettingEntry newEntries[],
			int op){
		ICLanguageSettingEntry result[];
		switch(op){
		case OP_ADD:{
			Set oldSet = new HashSet(Arrays.asList(oldEntries));
			Set newSet = new HashSet(Arrays.asList(newEntries));
			newSet.removeAll(oldSet);
			if(newSet.size() == 0){
				result = oldEntries;
			} else {
				result = new ICLanguageSettingEntry[oldEntries.length + newSet.size()];
				newSet.toArray(result);
				System.arraycopy(oldEntries, 0, result, newSet.size(), oldEntries.length);
			}
			break;
		}
		case OP_REMOVE:{
			Set oldSet = new HashSet(Arrays.asList(oldEntries));
			Set newSet = new HashSet(Arrays.asList(newEntries));
			oldSet.removeAll(newSet);
			if(oldSet.size() == 0){
				result = new ICLanguageSettingEntry[0];
			} else {
				result = new ICLanguageSettingEntry[oldSet.size()];
				oldSet.toArray(result);
			}
			break;
		}
		case OP_REPLACE:
		default:
			result = newEntries;
			break;
		}
		
		return result;
	}

	private PathEntryKyndStore sort(ResolvedEntry[] rEntries, PathEntryKyndStore store){
		if(store == null){
			store = new PathEntryKyndStore();
		}
		
		ResolvedEntry rEntry;
		for(int i = 0; i < rEntries.length; i++){
			rEntry = rEntries[i];
			List list = (List)store.get(rEntry.fEntry.getEntryKind());
			if(list == null){
				list = new ArrayList();
				store.put(rEntry.fEntry.getEntryKind(), list);
			}
			list.add(rEntry);
		}
		
		return store;
	}
	
//	private void listStoreToArrayStore(PathEntryKyndStore store, boolean nullAsEmptyArray){
//		int[]kinds = store.getSupportedKinds();
//		int kind;
//		
//		for(int i = 0; i < kinds.length; i++){
//			kind = kinds[i];
//			ResolvedE
//			List list = (List)store.get(kind);
//			if(list == null && nullAsEmptyArray){
//				
//			}
//		}
//	}
	

	public ReferenceSettingsInfo applyPathEntries(IPathEntry[] usrEntries, IPathEntry[] sysEntries, int op){
		ResolvedEntry[] rEntries = getResolvedEntries(usrEntries, sysEntries);
		return addPathEntries(rEntries, op);
	}

	private void propagateValues(PathSettingsContainer cr, List langEntryInfoList){
		
		RcDesInfo rcDes = (RcDesInfo)cr.getValue();
		if(rcDes != null){
			List rEntries = rcDes.fResolvedEntries;
			List curLanfInfos = new ArrayList(rEntries.size() + langEntryInfoList.size());
			for(Iterator iter = rEntries.iterator(); iter.hasNext();){
				ResolvedEntry re = (ResolvedEntry)iter.next();
				LangEntryInfo li = createLangEntryInfo(re);
				if(li != null){
					curLanfInfos.add(li);
				}
			}
			
			curLanfInfos.addAll(langEntryInfoList);
			langEntryInfoList = curLanfInfos;
		}
		
		for(Iterator iter = langEntryInfoList.iterator(); iter.hasNext();){
			LangEntryInfo li = (LangEntryInfo)iter.next();
			rcDes.add(li);
		}
		
		PathSettingsContainer directChildren[] = cr.getDirectChildren();
		for(int i = 0; i < directChildren.length; i++){
			filterAndPropagate(directChildren[i], langEntryInfoList);
		}
	}
	
	private void filterAndPropagate(PathSettingsContainer cr, List list){
		list = new ArrayList(list);
		IPath path = cr.getPath();
		for(Iterator iter = list.iterator(); iter.hasNext();){
			LangEntryInfo li = (LangEntryInfo)iter.next();
			ResolvedEntry re = li.fResolvedEntry;
			ResourceInfo[] filters = re.getFilterInfos();
			for(int i = 0; i < filters.length; i++){
				IResource rc = filters[i].fRc;
				IPath projPath = rc.getProjectRelativePath();
				if(projPath.isPrefixOf(path.makeRelative())){
					iter.remove();
					break;
				}
			}
		}
		
		propagateValues(cr, list);
	}

//	private void propagateValues(PathSettingsContainer cr){
//		RcDesInfo rcDes = (RcDesInfo)cr.getValue();
//		if(rcDes == null)
//			return;
//		
//		final List rEntries = rcDes.fResolvedEntries;
//
//		int size = rEntries.size();
//		if(size == 0)
//			return;
//		
//		ArrayList[] skipLists = new ArrayList[size];
//		ArrayList skipList;
//		ResolvedEntry rEntry;
//		ResourceInfo[] filters;
//		ResourceInfo filter;
//		for(int i = 0; i < size; i++){
//			rEntry = (ResolvedEntry)rEntries.get(i);
//			filters = rEntry.getFilterInfos();
//			if(filters.length != 0){
//				
//			}
//		}
//		
//		cr.accept(new IPathSettingsContainerVisitor(){
//
//			public boolean visit(PathSettingsContainer container) {
//				if(container == cr){
//					
//				} else {
//					for(Iterator iter = rEntries.iterator(); iter.hasNext();){
//						ResolvedEntry re = (ResolvedEntry)iter.next();
//						ResourceInfo filters[] = re.getFilterInfos();
//						
//					}
//				}
//				return true;
//			}
//			
//		});
//	}
	
	private int toLanguageEntryKind(int peKind){
		switch(peKind){
		case IPathEntry.CDT_LIBRARY:
			return ICLanguageSettingEntry.LIBRARY_FILE;
//		case IPathEntry.CDT_PROJECT:
//			return ICLanguageSettingEntry;
//		case IPathEntry.CDT_SOURCE:
//			return INDEX_CDT_SOURCE;
		case IPathEntry.CDT_INCLUDE:
			return ICLanguageSettingEntry.INCLUDE_PATH;
//		case IPathEntry.CDT_CONTAINER:
//			return INDEX_CDT_CONTAINER;
		case IPathEntry.CDT_MACRO:
			return ICLanguageSettingEntry.MACRO;
//		case IPathEntry.CDT_OUTPUT:
//			return INDEX_CDT_OUTPUT;
		case IPathEntry.CDT_INCLUDE_FILE:
			return ICLanguageSettingEntry.INCLUDE_FILE;
		case IPathEntry.CDT_MACRO_FILE:
			return ICLanguageSettingEntry.MACRO_FILE;
		}
		return 0;
	}
	
	private static int peKindToSettingKind(int peKind){
		switch(peKind){
		case IPathEntry.CDT_LIBRARY:
			return ICLanguageSettingEntry.LIBRARY_FILE;
//		case IPathEntry.CDT_PROJECT:
//			return ICLanguageSettingEntry;
		case IPathEntry.CDT_SOURCE:
			return ICLanguageSettingEntry.SOURCE_PATH;
		case IPathEntry.CDT_INCLUDE:
			return ICLanguageSettingEntry.INCLUDE_PATH;
//		case IPathEntry.CDT_CONTAINER:
//			return INDEX_CDT_CONTAINER;
		case IPathEntry.CDT_MACRO:
			return ICLanguageSettingEntry.MACRO;
		case IPathEntry.CDT_OUTPUT:
			return ICLanguageSettingEntry.OUTPUT_PATH;
		case IPathEntry.CDT_INCLUDE_FILE:
			return ICLanguageSettingEntry.INCLUDE_FILE;
		case IPathEntry.CDT_MACRO_FILE:
			return ICLanguageSettingEntry.MACRO_FILE;
		}
		return 0;
	}
	
	private ResolvedEntry[] getResolvedEntries(PathEntryResolveInfo info){
		PathEntryResolveInfoElement els[] = info.getElements();
		List list = new ArrayList();
		for(int i = 0; i < els.length; i++){
			getResolvedEntries(els[i], list);
		}
		return (ResolvedEntry[])list.toArray(new ResolvedEntry[list.size()]);
	}
	
	private List getResolvedEntries(PathEntryResolveInfoElement el, List list){
		if(list == null)
			list = new ArrayList();
		
		IPathEntry[] rpEntries = el.getResolvedEntries();
		IPathEntry rpEntry;
		ResolvedEntry resolvedE;
		IPathEntry rawEntry = el.getRawEntry();
		if(rawEntry.getEntryKind() == IPathEntry.CDT_PROJECT){
			resolvedE = createResolvedEntry(rawEntry, el);
			if(resolvedE != null)
				list.add(resolvedE);
		}
		for(int i = 0; i < rpEntries.length; i++){
			rpEntry = rpEntries[i];
			resolvedE = createResolvedEntry(rpEntry, el);
			if(resolvedE != null)
				list.add(resolvedE);
		}
		return list;
	}
	
	private ResolvedEntry createResolvedEntry(IPathEntry entry, PathEntryResolveInfoElement el){
		switch(entry.getEntryKind()){
//		case IPathEntry.CDT_PROJECT:
//			//should not be here
		case IPathEntry.CDT_CONTAINER:
			//the case of extension path entry container 
			return null;
		}
		return new ResolvedEntry(entry, el);
	}

	
	private ResolvedEntry[] getResolvedEntries(IPathEntry[] usrEntries, IPathEntry[] sysEntries){
		int length = usrEntries != null ? usrEntries.length : 0;
		if(sysEntries != null)
			length += sysEntries.length;
		ResolvedEntry[] rEntries = new ResolvedEntry[length];
		int num = 0;
		if(usrEntries != null){
			for(int i = 0; i < usrEntries.length; i++){
				rEntries[num++] = new ResolvedEntry(usrEntries[i], false);
			}
		}
		
		if(sysEntries != null){
			for(int i = 0; i < sysEntries.length; i++){
				rEntries[num++] = new ResolvedEntry(sysEntries[i], true);
			}
		}
		return rEntries;
	}
	
	private ResourceInfo[] resolveFilter(IContainer container, IPath path){
		IPath containerFullPath = container.getFullPath();
		IPath fullPath = containerFullPath.append(path);
		PathSettingsContainer cr = fTranslatedFilters.getChildContainer(fullPath, false, false);
		ResourceInfo[] baseInfos = (ResourceInfo[])cr.getValue();
		ResourceInfo[] result;
		if(!baseInfos[0].fExists){
			//resource does not exis, always create new rc info and not add it to map
			ResourceInfo inexistent = new ResourceInfo(container.getFolder(path), false);
			result = new ResourceInfo[]{inexistent};
		} else {
			//base exists
			IPath baseTranslatedPath = cr.getPath();
			if(baseTranslatedPath.equals(fullPath)){
				result = baseInfos;
			} else if(containerFullPath.isPrefixOf(baseTranslatedPath)){
				IPath filterToTranslate = fullPath.removeFirstSegments(baseTranslatedPath.segmentCount());
				result = performTranslation(baseTranslatedPath, baseInfos, filterToTranslate);
			} else {
				//should never be here
				throw new IllegalStateException();
			}
		}
		
		return result;
	}
	
	private ResourceInfo[] performTranslation(IPath basePath, ResourceInfo baseInfos[], IPath filter){
		ResourceInfo result[];
		int segCount = filter.segmentCount();
		String seg;
		int i = 0;
		for(; i < segCount; i++){
			if(!baseInfos[0].fExists)
				break;
			seg = filter.segment(0);
			baseInfos = performTranslation(basePath, baseInfos, seg);
			basePath = basePath.append(seg);
			filter = filter.removeFirstSegments(1);
		}
		
		if(i < segCount){
			result = new ResourceInfo[baseInfos.length];
			ResourceInfo baseInfo;
			IFolder rc;
			
			for(int k = 0; k < baseInfos.length; k++){
				baseInfo = baseInfos[k]; 
				rc = (IFolder)baseInfo.fRc;
				rc = rc.getFolder(filter);
				result[k] = new ResourceInfo(rc, false);
			}
		} else {
			result = baseInfos;
		}
		
		return result;
	}
	
	private ResourceInfo[] performTranslation(IPath basePath, ResourceInfo[] baseInfos, String seg){
		IPath filterFullPath = basePath.append(seg);
		boolean needsParsing = hasSpecChars(seg);
		ResourceInfo baseInfo;
		List list = new ArrayList();
		char[] segChars = seg.toCharArray();
		IResource baseRc;
		for(int i = 0; i < baseInfos.length; i++){
			baseInfo = baseInfos[i];
			baseRc = baseInfo.fRc;
			if(baseRc.getType() == IResource.FILE){
				continue;
			} else {
				IContainer baseCr = (IContainer)baseRc;
				IResource rc = baseCr.findMember(seg);
				if(rc != null){
					ResourceInfo rcInfo = new ResourceInfo(rc, true);
					addRcInfoToMap(rcInfo);
					list.add(rcInfo);
				} else if (needsParsing){
					try {
						IResource children[] = baseCr.members();
						ResourceInfo rcInfo;
						for(int k = 0; k < children.length; k++){
							if(CoreModelUtil.match(segChars, children[i].getName().toCharArray(), true)){
								rcInfo = new ResourceInfo(children[i], true);
								addRcInfoToMap(rcInfo);
								list.add(rcInfo);
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
		
		if(list.size() == 0){
			IFolder f = fRoot.getFolder(filterFullPath);
			ResourceInfo rcInfo = new ResourceInfo(f, false);
			addRcInfoToMap(rcInfo);
			list.add(rcInfo);
		}

		ResourceInfo[] result = new ResourceInfo[list.size()];
		list.toArray(result);
		addResolvedFilterToMap(filterFullPath, result, false);
		return result;
	}
	
	private boolean hasSpecChars(String str){
		for(int i = 0; i < SPEC_CHARS.length; i++){
			if(str.indexOf(SPEC_CHARS[i]) != -1)
				return true;
		}
		return false;
	}

	private void addResolvedFilterToMap(IPath fullFilterPath ,ResourceInfo[] resolved, boolean check){
		if(check){
			PathSettingsContainer cr = fTranslatedFilters.getChildContainer(fullFilterPath, false, false);
			ResourceInfo[] infos = (ResourceInfo[])cr.getValue();
			if(!infos[0].fExists)
				return;
		}

		PathSettingsContainer cr = fTranslatedFilters.getChildContainer(fullFilterPath, true, true);
		cr.setValue(resolved);
	}

	private ResourceInfo findResourceInfo(IContainer container, IPath relPath, boolean folderIfNotExist){
		IPath fullPath = container.getFullPath().append(relPath);
		ResourceInfo rcInfo = (ResourceInfo)fResourceMap.get(fullPath);
		
		if(rcInfo == null){
			IResource rc = container.findMember(relPath);
			boolean exists = true;
			if(rc == null){
				exists = false;
				if(container.getType() == IResource.ROOT && relPath.segmentCount() == 1){
					rc = fRoot.getProject(relPath.segment(0));
				} else if(folderIfNotExist) {
					rc = container.getFolder(relPath);
				} else {
					rc = container.getFile(relPath);
				}
			}
			
			rcInfo = new ResourceInfo(rc, exists);
			addRcInfoToMap(rcInfo);
		}
		return rcInfo;
	}
	
	private void addRcInfoToMap(ResourceInfo rcInfo){
		IPath fullPath = rcInfo.fRc.getFullPath();
		fResourceMap.put(fullPath, rcInfo);
		addResolvedFilterToMap(fullPath, new ResourceInfo[]{rcInfo}, true);
	}
	
	public static IPathEntry[] decodePathEntries(IProject project, ICStorageElement el){
		ArrayList pathEntries = new ArrayList();
		ICStorageElement children[] = el.getChildren();
		for (int i = 0; i < children.length; i++) {
			ICStorageElement child = children[i];
			if (child.getName().equals(PATH_ENTRY)) {
				try {
					pathEntries.add(decodePathEntry(project, child));
				} catch (CModelException e) {
					CCorePlugin.log(e);
				}
			}
		}
		IPathEntry[] entries = new IPathEntry[pathEntries.size()]; 
		pathEntries.toArray(entries);
		return entries;
	}
	
	private static String getAttribute(ICStorageElement el, String attr){
		String v = el.getAttribute(attr);
		if(v != null)
			return v;
		return ""; //$NON-NLS-1$
	}
	
	static IPathEntry decodePathEntry(IProject project, ICStorageElement element) throws CModelException {
		IPath projectPath = project.getFullPath();
		
		// kind
		String kindAttr = getAttribute(element, ATTRIBUTE_KIND);
		int kind = PathEntry.kindFromString(kindAttr);
		
		// exported flag
		boolean isExported = false;
		if (element.getAttribute(ATTRIBUTE_EXPORTED) != null) {
			isExported = element.getAttribute(ATTRIBUTE_EXPORTED).equals(VALUE_TRUE);
		}

		// get path and ensure it is absolute
		IPath path;
		if (element.getAttribute(ATTRIBUTE_PATH) != null) {
			path = new Path(element.getAttribute(ATTRIBUTE_PATH));
		} else {
			path = new Path(""); //$NON-NLS-1$
		}
		if (!path.isAbsolute()) {
			path = projectPath.append(path);
		}

		// check fo the base path
		IPath basePath = new Path(getAttribute(element, ATTRIBUTE_BASE_PATH));

		// get the base ref
		IPath baseRef = new Path(getAttribute(element, ATTRIBUTE_BASE_REF));

		// exclusion patterns (optional)
		String exclusion = getAttribute(element, ATTRIBUTE_EXCLUDING);
		IPath[] exclusionPatterns = APathEntry.NO_EXCLUSION_PATTERNS;
		if (exclusion != null && exclusion.length() > 0) {
			char[][] patterns = CharOperation.splitOn('|', exclusion.toCharArray());
			int patternCount;
			if ((patternCount = patterns.length) > 0) {
				exclusionPatterns = new IPath[patternCount];
				for (int j = 0; j < patterns.length; j++) {
					exclusionPatterns[j] = new Path(new String(patterns[j]));
				}
			}
		}
		
		// recreate the entry
		switch (kind) {
			case IPathEntry.CDT_PROJECT :
				return CoreModel.newProjectEntry(path, isExported);
			case IPathEntry.CDT_LIBRARY : {
				IPath libraryPath = new Path(getAttribute(element, ATTRIBUTE_LIBRARY));				
				// source attachment info (optional)
				IPath sourceAttachmentPath = element.getAttribute(ATTRIBUTE_SOURCEPATH) != null ? new Path(
						element.getAttribute(ATTRIBUTE_SOURCEPATH)) : null;
				IPath sourceAttachmentRootPath = element.getAttribute(ATTRIBUTE_ROOTPATH) != null ? new Path(
						element.getAttribute(ATTRIBUTE_ROOTPATH)) : null;
				IPath sourceAttachmentPrefixMapping = element.getAttribute(ATTRIBUTE_PREFIXMAPPING) != null ? new Path(
						element.getAttribute(ATTRIBUTE_PREFIXMAPPING)) : null;
				
				if (baseRef != null && !baseRef.isEmpty()) {
					return CoreModel.newLibraryRefEntry(path, baseRef, libraryPath);
				}
				return CoreModel.newLibraryEntry(path, basePath, libraryPath, sourceAttachmentPath, sourceAttachmentRootPath,
					sourceAttachmentPrefixMapping, isExported);
			}
			case IPathEntry.CDT_SOURCE : {
				// must be an entry in this project or specify another
				// project
				String projSegment = path.segment(0);
				if (projSegment != null && projSegment.equals(project.getName())) { // this
					// project
					return CoreModel.newSourceEntry(path, exclusionPatterns);
				}
				// another project
				return CoreModel.newProjectEntry(path, isExported);
			}
			case IPathEntry.CDT_OUTPUT :
				return CoreModel.newOutputEntry(path, exclusionPatterns);
			case IPathEntry.CDT_INCLUDE : {
				// include path info
				IPath includePath = new Path(getAttribute(element, ATTRIBUTE_INCLUDE));
				// isSysteminclude
				boolean isSystemInclude = false;
				if (element.getAttribute(ATTRIBUTE_SYSTEM) != null) {
					isSystemInclude = getAttribute(element, ATTRIBUTE_SYSTEM).equals(VALUE_TRUE);
				}
				if (baseRef != null && !baseRef.isEmpty()) {
					return CoreModel.newIncludeRefEntry(path, baseRef, includePath);
				}
				return CoreModel.newIncludeEntry(path, basePath, includePath, isSystemInclude, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_INCLUDE_FILE: {
				// include path info
				IPath includeFilePath = new Path(getAttribute(element, ATTRIBUTE_INCLUDE_FILE));
				return CoreModel.newIncludeFileEntry(path, basePath, baseRef, includeFilePath, exclusionPatterns, isExported);				
			}
			case IPathEntry.CDT_MACRO : {
				String macroName = getAttribute(element, ATTRIBUTE_NAME);
				String macroValue = getAttribute(element, ATTRIBUTE_VALUE);
				if (baseRef != null && !baseRef.isEmpty()) {
					return CoreModel.newMacroRefEntry(path, baseRef, macroName);
				}
				return CoreModel.newMacroEntry(path, macroName, macroValue, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_MACRO_FILE : {
				IPath macroFilePath = new Path(getAttribute(element, ATTRIBUTE_MACRO_FILE));
				return CoreModel.newMacroFileEntry(path, basePath, baseRef, macroFilePath, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_CONTAINER : {
				IPath id = new Path(getAttribute(element, ATTRIBUTE_PATH));
				return CoreModel.newContainerEntry(id, isExported);
			}
			default : {
				ICModelStatus status = new CModelStatus(IStatus.ERROR, "PathEntry: unknown kind (" + kindAttr + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new CModelException(status);
			}
		}
	}

	private static CConfigurationData getCfgData(ICConfigurationDescription cfgDes){
		return cfgDes instanceof CConfigurationDescriptionCache ? 
				(CConfigurationData)cfgDes : ((IInternalCCfgInfo)cfgDes).getConfigurationData(false);
	}
	
	private static void addOutputEntries(PathEntryCollector cr, CConfigurationData data){
		CBuildData bData = data.getBuildData();
		if(bData != null){
			ICOutputEntry oEntries[] = bData.getOutputDirectories();
			if(oEntries != null && oEntries.length != 0){
				cr.setSourceOutputEntries(ICSettingEntry.OUTPUT_PATH, oEntries);
			}
		}
	}

	public static PathEntryCollector collectEntries(IProject project, ICConfigurationDescription des){
		CConfigurationData data = getCfgData(des);
				
		ReferenceSettingsInfo refInfo = new ReferenceSettingsInfo(des);
		ICConfigurationDescription[] allCfgs = des.isPreferenceConfiguration() ? 
				new ICConfigurationDescription[]{des}
				: des.getProjectDescription().getConfigurations();
		
		CConfigurationData[] allDatas = new CConfigurationData[allCfgs.length];
		for(int i = 0; i < allCfgs.length; i++){
			allDatas[i] = getCfgData(allCfgs[i]);
		}
//		return collectEntries(project, data, info);
//	}
//
//	public static PathEntryCollector collectEntries(IProject project, CConfigurationData data, ReferenceSettingsInfo refInfo){
		final PathEntryCollector cr = new PathEntryCollector(project/*, des*/);
		PathSettingsContainer rcDatas = createRcDataHolder(data);
		ICSourceEntry sEntries[] = data.getSourceEntries();
//		ICSourceEntry sEntries[] = calculateSourceEntriesFromPaths(project, rcDatas, srcPaths);
		if(sEntries != null && sEntries.length != 0){
			cr.setSourceOutputEntries(ICSettingEntry.SOURCE_PATH, sEntries);
		}
		for(int i = 0; i < allDatas.length; i++){
			addOutputEntries(cr, allDatas[i]);
		}
		final HashSet exportedSettings = new HashSet();
		if(refInfo != null){
			cr.setRefProjects(refInfo.getReferencedProjectsPaths());
			ICExternalSetting[] settings = refInfo.getExternalSettings();
			for(int i = 0; i < settings.length; i++){
				exportedSettings.addAll(Arrays.asList(settings[i].getEntries()));
			}
		}
		
		final int kinds[] = KindBasedStore.getLanguageEntryKinds();
		rcDatas.accept(new IPathSettingsContainerVisitor(){

			public boolean visit(PathSettingsContainer container) {
				CResourceData data = (CResourceData)container.getValue();
				PathEntryCollector child = cr.createChild(container.getPath());
				for(int i = 0; i < kinds.length; i++){
					List list = new ArrayList();
					if(collectEntries(kinds[i], data, list)){
						ICLanguageSettingEntry[] entries = (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);
						child.setEntries(kinds[i], entries, exportedSettings);
					}
				}
				return true;
			}
			
		});
		return cr;
	}
	
	private static boolean collectEntries(int kind, CResourceData data, List list){
		if(data.getType() == ICSettingBase.SETTING_FOLDER){
			return collectEntries(kind, (CFolderData)data, list);
		} 
		return collectEntries(kind, (CFileData)data, list);
	}

	private static boolean collectEntries(int kind, CFolderData data, List list){
		
		CLanguageData lDatas[] = data.getLanguageDatas();
		boolean supported = false;
		if(lDatas != null && lDatas.length != 0){
			for(int i = 0; i < lDatas.length; i++){
				if(collectEntries(kind, lDatas[i], list))
					supported = true;
			}
		}
		return supported;
	}

	private static boolean collectEntries(int kind, CFileData data, List list){
		
		CLanguageData lData = data.getLanguageData();
		if(lData != null){
			return collectEntries(kind, lData, list);
		}
		return false;
	}
	
	private static boolean collectEntries(int kind, CLanguageData lData, List list){
//		if(list == null)
//			list = new ArrayList();
		
		if((kind & lData.getSupportedEntryKinds()) != 0){
			ICLanguageSettingEntry[] entries = lData.getEntries(kind);
			if(entries != null && entries.length != 0){
				list.addAll(Arrays.asList(entries));
			}
			return true;
		}
		
		return false;
	}

//	public static IPathEntry[] getPathEntries(IProject project, CConfigurationData data, ReferenceSettingsInfo refInfo, int flags){
	public static IPathEntry[] getPathEntries(IProject project, ICConfigurationDescription cfg, int flags){
		PathEntryCollector cr = collectEntries(project, cfg);
		return cr.getEntries(flags, cfg);
	}
}
