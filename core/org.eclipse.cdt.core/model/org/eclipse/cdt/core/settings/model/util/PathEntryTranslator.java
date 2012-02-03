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
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
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
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
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
import org.eclipse.core.runtime.Status;

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

	private static final char[] SPEC_CHARS = new char[] { '*', '?' };
	private PathSettingsContainer fRcDataHolder;
	private IProject fProject;
	private CConfigurationData fCfgData;
	private PathSettingsContainer fTranslatedFilters;
	private Map<IPath, ResourceInfo> fResourceMap = new HashMap<IPath, ResourceInfo>();
	private IWorkspaceRoot fRoot = ResourcesPlugin.getWorkspace().getRoot();

	private static class VarSubstitutor extends CoreVariableSubstitutor {
		ICConfigurationDescription fCfg;
		ICdtVariableManager fMngr = CCorePlugin.getDefault().getCdtVariableManager();

		public VarSubstitutor(ICConfigurationDescription cfgDescription) {
			super(new DefaultVariableContextInfo(ICoreVariableContextInfo.CONTEXT_CONFIGURATION, cfgDescription), "", " ");  //$NON-NLS-1$  //$NON-NLS-2$
			fCfg = cfgDescription;
		}

		@Override
		protected ResolvedMacro resolveMacro(ICdtVariable macro)
				throws CdtVariableException {
			if (!CdtVarPathEntryVariableManager.isPathEntryVariable(macro, fCfg, fMngr))
				return super.resolveMacro(macro);
			return new ResolvedMacro(macro.getName(), CdtVariableResolver.createVariableReference(macro.getName()));
		}
	}

	public static final class ReferenceSettingsInfo{
		private IPath[] fRefProjPaths;
		private ICExternalSetting[] fExtSettings;

		public ReferenceSettingsInfo(ICConfigurationDescription cfgDescription) {
			fExtSettings = cfgDescription.getExternalSettings();
			Map<String, String> map = cfgDescription.getReferenceInfo();
			fRefProjPaths = new IPath[map.size()];
			int num = 0;
			for (String proj : map.keySet()) {
				fRefProjPaths[num++] = new Path(proj).makeAbsolute();
			}
		}

		public ReferenceSettingsInfo(IPath[] projPaths, ICExternalSetting extSettings[]) {
			if (projPaths != null)
				fRefProjPaths = projPaths.clone();
			if (extSettings != null)
				fExtSettings = extSettings.clone();
		}

		public IPath[] getReferencedProjectsPaths() {
			if (fRefProjPaths != null)
				return fRefProjPaths.clone();
			return new IPath[0];
		}

		public Map<String, String> getRefProjectsMap() {
			if (fRefProjPaths != null && fRefProjPaths.length != 0) {
				Map<String, String> map = new HashMap<String, String>(fRefProjPaths.length);
				for (IPath fRefProjPath : fRefProjPaths) {
					map.put(fRefProjPath.segment(0), ""); //$NON-NLS-1$
				}
				return map;
			}
			return new HashMap<String, String>(0);
		}

		public ICExternalSetting[] getExternalSettings() {
			if (fExtSettings != null)
				return fExtSettings.clone();
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

		@SuppressWarnings("unchecked")
		private Map<String, IPathEntry>[] fEntryStorage = new Map[STORAGE_SIZE];

		private int kindToIndex(int kind) {
			switch (kind) {
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

		public static int[] getSupportedKinds() {
			return ENTRY_KINDS.clone();
		}

		public Map<String, IPathEntry> get(int kind) {
			return fEntryStorage[kindToIndex(kind)];
		}

		public Map<String, IPathEntry> put(int kind, Map<String, IPathEntry> object) {
			int index = kindToIndex(kind);
			Map<String, IPathEntry> old = fEntryStorage[index];
			fEntryStorage[index] = object;
			return old;
		}
	}

	private static class LangEntryInfo {
		ICLanguageSettingEntry fLangEntry;
		ResolvedEntry fResolvedEntry;

		public LangEntryInfo(ICLanguageSettingEntry lEntry, ResolvedEntry re) {
			fLangEntry = lEntry;
			fResolvedEntry = re;
		}
	}

	private class RcDesInfo {
		List<ResolvedEntry> fResolvedEntries;
		KindBasedStore<List<LangEntryInfo>> fLangEntries;

		private RcDesInfo(ResourceInfo rcInfo) {
			fResolvedEntries = new ArrayList<ResolvedEntry>();
			fLangEntries = new KindBasedStore<List<LangEntryInfo>>();
		}

		public void add(LangEntryInfo info) {
			List<LangEntryInfo> list = fLangEntries.get(info.fLangEntry.getKind());
			if (list == null) {
				list = new ArrayList<LangEntryInfo>();
				fLangEntries.put(info.fLangEntry.getKind(), list);
			}
			list.add(info);
		}

		public ICLanguageSettingEntry[] getEntries(int kind) {
			List<LangEntryInfo> list = fLangEntries.get(kind);
			if (list != null) {
				ICLanguageSettingEntry[] entries = new ICLanguageSettingEntry[list.size()];
				for (int i = 0; i < entries.length; i++) {
					LangEntryInfo info = list.get(i);
					entries[i] = info.fLangEntry;
				}
				return entries;
			}
			return new ICLanguageSettingEntry[0];


		}
	}

	private static ICLanguageSettingEntry createLangEntry(ResolvedEntry entry) {
		PathEntryValueInfo pathEntryValue = entry.getResolvedValue();
		String name = pathEntryValue.getName();

		int flags = ICSettingEntry.RESOLVED;
		if (entry.isReadOnly())
			flags |= ICSettingEntry.READONLY;
		if (entry.isBuiltIn())
			flags |= ICSettingEntry.BUILTIN;

		IPath path = pathEntryValue.getFullPath();
		if (path != null) {
			flags |= ICSettingEntry.VALUE_WORKSPACE_PATH;
		} else {
			path = pathEntryValue.getLocation();
		}

		int kind = entry.fEntry.getEntryKind();
		switch (kind) {
			case IPathEntry.CDT_LIBRARY:{
				if (path != null) {
					ILibraryEntry libEntry = (ILibraryEntry)entry.fEntry;
					return (ICLanguageSettingEntry) CDataUtil.createEntry(ICSettingEntry.LIBRARY_FILE, name, null, null, flags,
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
				if (path != null) {
					return CDataUtil.createCIncludePathEntry(name, flags);
				}
				break;
			}
//			case IPathEntry.CDT_CONTAINER:
//				return INDEX_CDT_CONTAINER;
			case IPathEntry.CDT_MACRO:
				if (name.length() != 0) {
					String value = pathEntryValue.getValue();
					return CDataUtil.createCMacroEntry(name, value, flags);
				}
				break;
//			case IPathEntry.CDT_OUTPUT:
//				return INDEX_CDT_OUTPUT;
			case IPathEntry.CDT_INCLUDE_FILE:{
				if (path != null) {
					return CDataUtil.createCIncludeFileEntry(name, flags);
				}
				break;
			}
			case IPathEntry.CDT_MACRO_FILE:{
				if (path != null) {
					return CDataUtil.createCMacroFileEntry(name, flags);
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
		private IPath fLocation;
		private String fName;
		private String fValue;
		private ResolvedEntry fResolvedEntry;

		private PathEntryValueInfo(ResolvedEntry rEntry) {
			fResolvedEntry = rEntry;

			init();
		}

		public IPath getFullPath() {
			if (fResourceInfo != null)
				return fResourceInfo.fRc.getFullPath();
			return null;
		}

		public IPath getLocation() {
			if (fResourceInfo != null)
				return fResourceInfo.fRc.getLocation();
			return fLocation;
		}

		public String getName() {
			if (fName != null)
				return fName;
			return ""; //$NON-NLS-1$
		}

		public String getValue() {
			if (fValue != null)
				return fValue;
			return ""; //$NON-NLS-1$
		}

		private void init() {
			IPathEntry entry = fResolvedEntry.fEntry;
			int peKind = entry.getEntryKind();
			IPath basePath = null, valuePath = null;
			boolean isFile = false;
			boolean calcPath = false;
			switch (peKind) {
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
				break;
			}

			if (calcPath) {
				do {
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
								break;
							}
						}
						fLocation = loc.append(valuePath);
						break;
					}

					if (!valuePath.isAbsolute()) {
						ResourceInfo rcInfo = fResolvedEntry.getResourceInfo();
						if (rcInfo.fExists) {
							if (rcInfo.fRc.getType() == IResource.FILE) {
								rcInfo = findResourceInfo(fRoot, rcInfo.fRc.getFullPath().removeLastSegments(1), true);
							}
							IPath location = rcInfo.fRc.getLocation();
							if (location != null && rcInfo.fRc.getType() != IResource.FILE) {
								rcInfo = findResourceInfo((IContainer)rcInfo.fRc, valuePath, !isFile);
								fResourceInfo = rcInfo;
								break;
							}
						}
					}

					fLocation = valuePath;
				} while (false);
			}
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

		public boolean isReadOnly() {
			return fIsReadOnly;
		}

		public boolean isBuiltIn() {
			return fIsBuiltIn;
		}

		public ResourceInfo getResourceInfo() {
			if (fResourceInfo == null) {
				fResourceInfo = findResourceInfo(fRoot, getEntryFullPath(fEntry), true);
			}
			return fResourceInfo;
		}

		public ResourceInfo[] getFilterInfos() {
			if (fFilterInfos == null) {
				IPath[] paths = obtainFilters(fEntry);
				if (paths.length == 0) {
					fFilterInfos = new ResourceInfo[0];
				} else {
					ResourceInfo rcInfo = getResourceInfo();
					if (rcInfo.fExists) {
						if (rcInfo.fRc.getType() == IResource.FILE) {
							fFilterInfos = new ResourceInfo[0];
						} else {
							List<ResourceInfo> list = new ArrayList<ResourceInfo>();
							for (IPath path : paths) {
								list.addAll(Arrays.asList(processFilter((IContainer)rcInfo.fRc, path)));
							}
							fFilterInfos = new ResourceInfo[list.size()];
							list.toArray(fFilterInfos);
						}
					} else {
						fFilterInfos = new ResourceInfo[paths.length];
						for (int i = 0; i < paths.length; i++) {
							fFilterInfos[i] = processInexistingResourceFilter((IContainer)rcInfo.fRc, paths[i]);
						}
					}
				}
			}
			return fFilterInfos;
		}

		private ResourceInfo[] processFilter(IContainer container, IPath path) {
			return resolveFilter(container, path);
		}

		private ResourceInfo processInexistingResourceFilter(IContainer container, IPath path) {
			IFolder f = container.getFolder(path);
			ResourceInfo rcInfo = new ResourceInfo(f, false);
			addRcInfoToMap(rcInfo);

			addResolvedFilterToMap(container.getFullPath(), new ResourceInfo[]{rcInfo}, true);
			return rcInfo;
		}

		public IPathEntry getEntry() {
			return fEntry;
		}

		public PathEntryValueInfo getResolvedValue() {
			if (fResolvedValue == null) {
				fResolvedValue = new PathEntryValueInfo(this);
			}
			return fResolvedValue;
		}


	}

	private static class PathEntryComposer {
		private IPath fPath;
		private ICSettingEntry fLangEntry;
		private Set<IPath> fFiltersSet;
		private boolean fIsExported;
		private IProject fProject;

		PathEntryComposer(IPath path, IProject project/*, ICConfigurationDescription cfgDescription*/) {
			fPath = toProjectPath(path);
			fProject = project;
		}

		private static IPath toProjectPath(IPath path) {
			if (path.segmentCount() > 1)
				path = new Path(path.segment(0));

			return path.makeAbsolute();
		}

		PathEntryComposer(ICExclusionPatternPathEntry entry, IProject project/*, ICConfigurationDescription cfgDescription*/) {
			fPath = new Path(entry.getValue());
			fLangEntry = entry;
			fProject = project;
			IPath[] exclusions = entry.getExclusionPatterns();
			if (exclusions.length != 0) {
				fFiltersSet = new HashSet<IPath>(exclusions.length);
				fFiltersSet.addAll(Arrays.asList(entry.getExclusionPatterns()));
			}
		}

		PathEntryComposer(IPath path, ICLanguageSettingEntry entry, boolean exported, IProject project/*, ICConfigurationDescription cfgDescription*/) {
			fPath = path;
			fLangEntry = entry;
			fIsExported = exported;
			fProject = project;
		}

		public void addFilter(IPath path) {
			if (fFiltersSet == null)
				fFiltersSet = new HashSet<IPath>();

			fFiltersSet.add(path);
		}

		public ICSettingEntry getSettingEntry() {
			return fLangEntry;
		}

		public IPath getPath() {
			return fPath;
		}

		public IPath[] getExclusionPatterns() {
			if (fFiltersSet != null)
				return fFiltersSet.toArray(new IPath[fFiltersSet.size()]);
			return new IPath[0];
		}

		private IPath[][] getEntryPath(ICSettingEntry entry, ICConfigurationDescription cfgDescription) {
			return valueToEntryPath(entry.getName(), (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0, cfgDescription);
		}

		private IPath[][] valueToEntryPath(String value, boolean isWsp, ICConfigurationDescription cfgDescription) {
			String[] pathVarValues = resolveKeepingPathEntryFars(value, cfgDescription);
			IPath result[][] = new IPath[2][pathVarValues.length];
			for (int i = 0; i < pathVarValues.length; i++) {
				String resolvedValue = resolveAll(value, cfgDescription);
				IPath resolvedPath = new Path(resolvedValue);
				IPath pathVarPath = new Path(pathVarValues[i]);
				if (isWsp) {
					if (!resolvedPath.isAbsolute()) {
						result[0][i] = fProject.getFullPath().makeRelative();
						result[1][i] = pathVarPath;
					} else {
						if (resolvedPath.segmentCount() != 0) {
							String projName = resolvedPath.segment(0);
							IPath valuePath = resolvedPath.removeFirstSegments(1).makeRelative();
							if (pathVarPath.segmentCount() != 0) {
								String resolvedProjName = projName;
								String varProjName = pathVarPath.segment(0);
								IPath resolvedProjPath = CCorePlugin.getDefault().getPathEntryVariableManager().resolvePath(new Path(varProjName));
								if (resolvedProjPath.segmentCount() == 1 && resolvedProjName.equals(resolvedProjPath.segment(0))) {
									projName = varProjName;
									valuePath = pathVarPath.removeFirstSegments(1).makeRelative();
								}
							}

							result[0][i] = new Path(projName);
							result[1][i] = valuePath;
						} else if (pathVarPath.isRoot()) {
							result[1][i] = ResourcesPlugin.getWorkspace().getRoot().getLocation();
						}
					}
				} else {
					if (!resolvedPath.isAbsolute()) {
						IPath location = fProject.getLocation();
						if (location != null)
							pathVarPath = location.append(pathVarPath);
					}
					result[1][i] = pathVarPath;
				}
			}

			return result;
		}

		public IPathEntry[] toPathEntry(ICConfigurationDescription cfgDescription, boolean keepPathInfo) {
			IPath path = keepPathInfo ? fPath : fProject.getFullPath();
			IPathEntry[] result = new IPathEntry[0];
			if (fLangEntry != null) {
				switch (fLangEntry.getKind()) {
				case ICSettingEntry.INCLUDE_FILE:{
						IPath paths[][] = getEntryPath(fLangEntry, cfgDescription);
						result = new IPathEntry[paths[0].length];
						for (int i = 0; i < result.length; i++)
							result[i] = CoreModel.newIncludeFileEntry(path, null, paths[0][i], paths[1][i], getExclusionPatterns(), fIsExported);
						return result;
					}
				case ICSettingEntry.INCLUDE_PATH:{
						IPath paths[][] = getEntryPath(fLangEntry, cfgDescription);
						ICIncludePathEntry ipe = (ICIncludePathEntry)fLangEntry;

						result = new IPathEntry[paths[0].length];
						for (int i = 0; i < result.length; i++)
							result[i] = CoreModel.newIncludeEntry(path, paths[0][i], paths[1][i], !ipe.isLocal(), getExclusionPatterns(), fIsExported);
						return result;
					}
				case ICSettingEntry.MACRO:
					result = new IPathEntry[1];
					result[0] = CoreModel.newMacroEntry(path, fLangEntry.getName(), fLangEntry.getValue(), getExclusionPatterns(), fIsExported);
					return result;
				case ICSettingEntry.MACRO_FILE:{
						IPath paths[][] = getEntryPath(fLangEntry, cfgDescription);
						result = new IPathEntry[paths[0].length];
						for (int i = 0; i < result.length; i++)
							result[i] = CoreModel.newMacroFileEntry(path, paths[0][i], null, paths[1][i], getExclusionPatterns(), fIsExported);
						return result;
					}
				case ICSettingEntry.LIBRARY_PATH:
				case ICSettingEntry.LIBRARY_FILE:
					// Bug 100844 don't contribute library files back to the CModel as a library files, as supplied by the build system,
					// aren't currently resolved
					return null;
//				case ICSettingEntry.LIBRARY_FILE:{
//						IPath paths[][] = getEntryPath(fLangEntry, cfgDescription);
//						result = new IPathEntry[paths[0].length];
//						for (int i = 0; i < result.length; i++)
//							result[i] = CoreModel.newLibraryEntry(path, paths[0][i], paths[1][i], null, null, null, fIsExported);
//						return result;
//					}
				case ICSettingEntry.OUTPUT_PATH:
					result = new IPathEntry[1];
					result[0] = CoreModel.newOutputEntry(fPath, getExclusionPatterns());
					return result;
				case ICSettingEntry.SOURCE_PATH:
					result = new IPathEntry[1];
					result[0] = CoreModel.newSourceEntry(fPath, getExclusionPatterns());
					return result;
				default:
					return result; // empty
				}
			} else if (fPath != null) {
				result = new IPathEntry[1];
				result[0] = CoreModel.newProjectEntry(fPath, fIsExported);
				return result;
			}
			return result; // empty
		}
	}

	private static String resolveAll(String value, ICConfigurationDescription cfgDescription) {
		try {
			return CCorePlugin.getDefault().getCdtVariableManager().resolveValue(value, "", " ", cfgDescription); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		return value;
	}

	private static String[] resolveKeepingPathEntryFars(String value, ICConfigurationDescription cfgDescription) {
		String[] result = new String[] { value }; // default value;
		try {
			VarSubstitutor substitutor = new VarSubstitutor(cfgDescription);

			result = CdtVariableResolver.resolveToStringList(value, substitutor);
			if (result == null || result.length == 0)
				result = new String[] {	CdtVariableResolver.resolveToString(value, substitutor) };
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		return result;
	}

	public static class PathEntryCollector {
		private PathSettingsContainer fStorage;
		private KindBasedStore<LinkedHashMap<ICSettingEntry, PathEntryComposer>> fStore;
		private KindBasedStore<LinkedHashMap<EntryNameKey, PathEntryComposer>> fNameKeyMapStore; //utility map, does not contain all entries, only those added explicitly
		private LinkedHashMap<IPath, PathEntryComposer> fRefProjMap;
		private IProject fProject;

		private PathEntryCollector(IProject project/*, ICConfigurationDescription cfgDescription*/) {
			fStorage = PathSettingsContainer.createRootContainer();
			fStorage.setValue(this);
			fStore = new KindBasedStore<LinkedHashMap<ICSettingEntry, PathEntryComposer>>(false);
			fNameKeyMapStore = new KindBasedStore<LinkedHashMap<EntryNameKey, PathEntryComposer>>(false);
			fProject = project;
		}

		private PathEntryCollector(PathSettingsContainer container, KindBasedStore<LinkedHashMap<ICSettingEntry, PathEntryComposer>> store, IProject project/*, ICConfigurationDescription cfgDescription*/) {
			fStorage = container;
			fStore = store;
			fNameKeyMapStore = new KindBasedStore<LinkedHashMap<EntryNameKey, PathEntryComposer>>(false);
			fProject = project;
		}

		public void setSourceOutputEntries(int kind, ICExclusionPatternPathEntry entries[]) {
			Map<ICSettingEntry, PathEntryComposer> map = getEntriesMap(kind, true);
			Map<EntryNameKey, PathEntryComposer> nameKeyMap = getEntriesNameKeyMap(kind, true);
			for (ICExclusionPatternPathEntry entry : entries) {
				entry = CDataUtil.makeAbsolute(fProject, entry, true);
				EntryNameKey nameKey = new EntryNameKey(entry);
				PathEntryComposer old = nameKeyMap.get(nameKey);
				if (old != null) {
					entry = CDataUtil.addRemoveExclusionsToEntry(entry,
							((ICExclusionPatternPathEntry)old.fLangEntry).getExclusionPatterns(),
							true);
				}
				PathEntryComposer newComposer = new PathEntryComposer(entry, fProject/*, fCfg*/);
				map.put(entry, newComposer);
				nameKeyMap.put(nameKey, newComposer);
			}
		}

		public void setRefProjects(IPath []paths) {
			if (paths == null || paths.length == 0) {
				fRefProjMap = null;
			} else {
				fRefProjMap = new LinkedHashMap<IPath, PathEntryComposer>();
				for (IPath path : paths) {
					PathEntryComposer cs = new PathEntryComposer(path, fProject/*, fCfg*/);
					IPath composerPath = cs.getPath();
					fRefProjMap.put(composerPath, cs);
				}
			}
		}

		public PathEntryCollector createChild(IPath path) {
			if (path.segmentCount() == 0)
				return this;

			PathEntryCollector cr = (PathEntryCollector)fStorage.getChildContainer(path, false, false).getValue();
			if (cr != this) {
				IPath basePath = cr.getPath();
				path = path.removeFirstSegments(basePath.segmentCount());
				return cr.createChild(path);
			}

			PathSettingsContainer newContainer = fStorage.getChildContainer(path, true, true);
			@SuppressWarnings("unchecked")
			KindBasedStore<LinkedHashMap<ICSettingEntry, PathEntryComposer>> cloneStore =
				(KindBasedStore<LinkedHashMap<ICSettingEntry, PathEntryComposer>>)fStore.clone();
			IKindBasedInfo<LinkedHashMap<ICSettingEntry, PathEntryComposer>> info[] = cloneStore.getContents();
			for (IKindBasedInfo<LinkedHashMap<ICSettingEntry, PathEntryComposer>> kindInfo : info) {
				LinkedHashMap<ICSettingEntry, PathEntryComposer> map = kindInfo.getInfo();
				if (map != null) {
					@SuppressWarnings("unchecked")
					LinkedHashMap<ICSettingEntry, PathEntryComposer> clone = (LinkedHashMap<ICSettingEntry, PathEntryComposer>)map.clone();
					kindInfo.setInfo(clone);
				}
			}
			PathEntryCollector newCr = new PathEntryCollector(newContainer, cloneStore, fProject/*, fCfg*/);
			newContainer.setValue(newCr);
			return newCr;
		}

		public IPath getPath() {
			return fStorage.getPath();
		}

		public void setEntries(int kind, ICLanguageSettingEntry entries[], Set<ICSettingEntry> exportedEntries) {
			IPath path = getPath();
			HashSet<ICSettingEntry> parentSet = getEntriesSetCopy(kind);
			@SuppressWarnings("unchecked")
			HashSet<ICSettingEntry> removedParentSet = (HashSet<ICSettingEntry>)parentSet.clone();
			HashSet<ICLanguageSettingEntry> addedThisSet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(entries));
			removedParentSet.removeAll(addedThisSet);
			addedThisSet.removeAll(parentSet);


			if (removedParentSet.size() != 0) {
				PathEntryCollector parent = getParent();
				IPath parentPath = parent.getPath();

				int segsToRemove = parentPath.segmentCount();
				if (segsToRemove > path.segmentCount())
					segsToRemove = path.segmentCount() - 1;
				if (segsToRemove < 0)
					segsToRemove = 0;

				IPath filterPath = path.removeFirstSegments(segsToRemove);

				parent.addFilter(kind, filterPath, removedParentSet);

				Map<ICSettingEntry, PathEntryComposer> map = getEntriesMap(kind, true);
				for (ICSettingEntry item : removedParentSet) {
					map.remove(item);
				}
			}

			if (addedThisSet.size() != 0) {
				Map<ICSettingEntry, PathEntryComposer> map = getEntriesMap(kind, true);
				IPath fullPath = fProject.getFullPath().append(path);
				for (int i = 0; i < entries.length; i++) {
					if (!addedThisSet.remove(entries[i]))
						continue;

					ICLanguageSettingEntry entry = entries[i];
					map.put(entry, new PathEntryComposer(fullPath, entry, exportedEntries.contains(entry), fProject/*, fCfg*/));
				}
			}
		}

		private LinkedHashMap<ICSettingEntry, PathEntryComposer> getEntriesMap(int kind, boolean create) {
			LinkedHashMap<ICSettingEntry, PathEntryComposer> map = fStore.get(kind);
			if (map == null && create) {
				map = new LinkedHashMap<ICSettingEntry, PathEntryComposer>();
				fStore.put(kind, map);
			}
			return map;
		}

		private LinkedHashMap<EntryNameKey, PathEntryComposer> getEntriesNameKeyMap(int kind, boolean create) {
			LinkedHashMap<EntryNameKey, PathEntryComposer> map = fNameKeyMapStore.get(kind);
			if (map == null && create) {
				map = new LinkedHashMap<EntryNameKey, PathEntryComposer>();
				fNameKeyMapStore.put(kind, map);
			}
			return map;
		}

		private void addFilter(int kind, IPath path, Set<ICSettingEntry> entriesSet) {
			if (entriesSet.size() == 0)
				return;

			Map<ICSettingEntry, PathEntryComposer> map = fStore.get(kind);
			for (ICSettingEntry fltPath : entriesSet) {
				PathEntryComposer cs = map.get(fltPath);
				cs.addFilter(path);
			}
		}

		public PathEntryCollector getParent() {
			if (fStorage.isRoot())
				return null;
			PathSettingsContainer cr = fStorage.getParentContainer();
			return (PathEntryCollector)cr.getValue();
		}

		private HashSet<ICSettingEntry> getEntriesSetCopy(int kind) {
			Map<ICSettingEntry, PathEntryComposer> map = getEntriesMap(kind, false);
			if (map != null) {
				return new HashSet<ICSettingEntry>(map.keySet());
			}
			return new HashSet<ICSettingEntry>(0);
		}

		private List<PathEntryComposer> getCollectedEntriesList(final int kind) {
			final List<PathEntryComposer> list = new ArrayList<PathEntryComposer>();
			final Set<PathEntryComposer> set = new HashSet<PathEntryComposer>();
			fStorage.accept(new IPathSettingsContainerVisitor() {

				@Override
				public boolean visit(PathSettingsContainer container) {
					PathEntryCollector clr = (PathEntryCollector)container.getValue();
					clr.getLocalCollectedEntries(kind, list, set);
					return true;
				}

			});

			return list;
		}

		private void getLocalCollectedEntries(int kind, List<PathEntryComposer> list, Set<PathEntryComposer> addedEntries) {
			Map<ICSettingEntry, PathEntryComposer> map = getEntriesMap(kind, false);
			if (map == null)
				return;

			for (PathEntryComposer pathEntryComposer : map.values()) {
				if (addedEntries.add(pathEntryComposer)) {
					list.add(pathEntryComposer);
				}
			}
		}

		public List<IPathEntry> getEntries(int peKind, List<IPathEntry> list, int flags, ICConfigurationDescription cfgDescription) {
			if (list == null) {
				list = new ArrayList<IPathEntry>();
			}

			int sKind = peKindToSettingKind(peKind);
			List<PathEntryComposer> composerList = null;
			if (sKind != 0) {
				composerList = getCollectedEntriesList(sKind);
			} else if (peKind == IPathEntry.CDT_PROJECT) {
				if (fRefProjMap != null && fRefProjMap.size() != 0) {
					composerList = new ArrayList<PathEntryComposer>(fRefProjMap.values());
				}
			}
			if (composerList != null) {
				PathEntryKyndStore store = new PathEntryKyndStore();
				for (PathEntryComposer cs : composerList) {
					ICSettingEntry entry = cs.getSettingEntry();
					if (checkFilter(cs, entry, flags)) {
						IPathEntry[] pe = null;
						if (isBuiltIn(entry) && cs.getPath().segmentCount() > 1) {
							String name = entry.getName();
							Map<String, IPathEntry> map = store.get(peKind);
							if (map == null) {
								map = new HashMap<String, IPathEntry>();
								store.put(peKind, map);
							}
							if (!map.containsKey(name)) {
								pe = cs.toPathEntry(cfgDescription, false);
								if (pe != null) {
									if (pe.length > 1) {
										System.out.println();
									}
									map.put(name, pe[0]);
								}
							}
						} else {
							pe = cs.toPathEntry(cfgDescription, true);
						}
						if (pe != null)
							list.addAll(Arrays.asList(pe));
					}
				}
			}

			return list;
		}

		private static boolean checkFilter(PathEntryComposer cs, ICSettingEntry entry, int flags) {
			boolean builtIn = isBuiltIn(entry);

//			if (builtIn && cs.getPath().segmentCount() > 1)
//				return false;
			if ((flags & INCLUDE_BUILT_INS) != 0 && builtIn)
				return true;
			if ((flags & INCLUDE_USER) != 0 && !builtIn)
				return true;
			return false;
		}

		private static boolean isBuiltIn(ICSettingEntry entry) {
			return entry != null &&	(entry.isBuiltIn() || entry.isReadOnly());
		}

		public List<IPathEntry> getEntries(List<IPathEntry> list, int flags, ICConfigurationDescription cfgDescription) {
			if (list == null)
				list = new ArrayList<IPathEntry>();
			int peKinds[] = PathEntryKyndStore.getSupportedKinds();
			for (int peKind : peKinds) {
				getEntries(peKind, list, flags, cfgDescription);
			}

			return list;
		}

		public IPathEntry[] getEntries(int flags, ICConfigurationDescription cfgDescription) {
			List<IPathEntry> list = getEntries(null, flags,cfgDescription);
			IPathEntry[] entries = list.toArray(new IPathEntry[list.size()]);
			return entries;
		}
	}

	private static LangEntryInfo createLangEntryInfo(ResolvedEntry entry) {
		ICLanguageSettingEntry le = createLangEntry(entry);
		if (le != null) {
			return new LangEntryInfo(le, entry);
		}
		return null;
	}


	private static boolean areEntriesReadOnly(PathEntryResolveInfoElement el) {
		switch (el.getRawEntry().getEntryKind()) {
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


	private IPath getEntryFullPath(IPathEntry entry) {
		IPath path = entry.getPath();
		if (path == null)
			return fProject.getFullPath();
		else if (path.isAbsolute())
			return path;
		return fProject.getFullPath().append(path);

	}

	private IPath[] obtainFilters(IPathEntry entry) {
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

	public PathEntryTranslator(IProject project, CConfigurationData cfgData) {
		fProject = project;
		fCfgData = cfgData;
		fRcDataHolder = createRcDataHolder(cfgData);
		fTranslatedFilters = PathSettingsContainer.createRootContainer();
		fTranslatedFilters.setValue(new ResourceInfo[]{new ResourceInfo(fRoot, true)});
	}

	private static PathSettingsContainer createRcDataHolder(CConfigurationData cfgData) {
		return CDataUtil.createRcDataHolder(cfgData);
	}

	public ReferenceSettingsInfo applyPathEntries(PathEntryResolveInfo info, int op) {
		ResolvedEntry[] rEntries = getResolvedEntries(info);
		return addPathEntries(rEntries, op);
	}

	private RcDesInfo getRcDesInfo(PathSettingsContainer cr, ResourceInfo rcInfo) {
		IResource rc = rcInfo.fRc;
		IPath projPath = rc.getProjectRelativePath();
		PathSettingsContainer child = cr.getChildContainer(projPath, true, true);
		RcDesInfo rcDes = (RcDesInfo)child.getValue();
		if (rcDes == null) {
			rcDes = new RcDesInfo(rcInfo);
			child.setValue(rcDes);
		}
		return rcDes;
	}

	private ReferenceSettingsInfo addPathEntries(ResolvedEntry[] rEntries, int op) {
		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
		cr.setValue(new RcDesInfo(new ResourceInfo(fProject, true)));
		List<IPathEntry> srcList = new ArrayList<IPathEntry>();
		List<IPathEntry> outList = new ArrayList<IPathEntry>();
		List<ResolvedEntry> projList = new ArrayList<ResolvedEntry>();
		List<ResolvedEntry> exportSettingsList = new ArrayList<ResolvedEntry>();
		ICSourceEntry srcEntries[] = null;
		ICOutputEntry outEntries[] = null;
		ResourceInfo rcInfo;
		for (ResolvedEntry rEntry : rEntries) {
			if (rEntry.isReadOnly())
				continue;
			if (toLanguageEntryKind(rEntry.fEntry.getEntryKind()) == 0) {
				switch (rEntry.fEntry.getEntryKind()) {
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

			if (rEntry.getEntry().isExported()) {
				exportSettingsList.add(rEntry);
			}
			rcInfo = rEntry.getResourceInfo();
			RcDesInfo rcDes = getRcDesInfo(cr, rcInfo);

			rcDes.fResolvedEntries.add(rEntry);

			ResourceInfo[] fInfos = rEntry.getFilterInfos();
			for (ResourceInfo fInfo : fInfos) {
				getRcDesInfo(cr, fInfo);
			}
		}

		if (srcList.size() != 0) {
			srcEntries = toCSourceEntries(srcList);
		}
		if (outList.size() != 0) {
			outEntries = toCOutputEntries(outList);
		}

		propagateValues(cr, new ArrayList<LangEntryInfo>(0));

		//applying settings
		applyOutputEntries(outEntries, op);
		applySourceEntries(srcEntries, op);
		applyLangSettings(cr, op);

		IPath refProjPaths[] = new IPath[projList.size()];
		for (int i = 0; i < refProjPaths.length; i++) {
			ResolvedEntry e = projList.get(i);
			refProjPaths[i] = e.getResourceInfo().fRc.getFullPath();
		}

		ICExternalSetting extSettings[];
		if (exportSettingsList.size() != 0) {
			extSettings = new ICExternalSetting[1];
			List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>(exportSettingsList.size());
			for (int i = 0; i < exportSettingsList.size(); i++) {
				ResolvedEntry re = exportSettingsList.get(i);
				ICLanguageSettingEntry le = createLangEntry(re);
				if (le != null)
					list.add(le);
			}
			ICLanguageSettingEntry expEntries[] = list.toArray(new ICLanguageSettingEntry[list.size()]);
			extSettings[0] = new CExternalSetting(null, null, null, expEntries);
		} else {
			extSettings = new ICExternalSetting[0];
		}

		return new ReferenceSettingsInfo(refProjPaths, extSettings);
	}

	private static ICSourceEntry[] toCSourceEntries(List<IPathEntry> list) {
		ICSourceEntry[] entries = new ICSourceEntry[list.size()];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = toCSourceEntry((ISourceEntry)list.get(i), true);
		}
		return entries;
	}

	private static ICOutputEntry[] toCOutputEntries(List<IPathEntry> list) {
		ICOutputEntry[] entries = new ICOutputEntry[list.size()];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = toCOutputEntry((IOutputEntry)list.get(i), true);
		}
		return entries;
	}


	private static ICSourceEntry toCSourceEntry(ISourceEntry entry, boolean makeProjRelative) {
		IPath path = entry.getPath();
		if (makeProjRelative && path.isAbsolute())
			path = path.removeFirstSegments(1);
		return new CSourceEntry(path,
				entry.getExclusionPatterns(),
				ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
	}

	private static ICOutputEntry toCOutputEntry(IOutputEntry entry, boolean makeProjRelative) {
		IPath path = entry.getPath();
		if (makeProjRelative && path.isAbsolute())
			path = path.removeFirstSegments(1);
		return new COutputEntry(path,
				entry.getExclusionPatterns(),
				ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
	}

	private static ICSettingEntry[] replaceUserEntries(ICSettingEntry[] oldEntries, ICSettingEntry[] newUsrEntries) {
		Set<ICSettingEntry> set = new LinkedHashSet<ICSettingEntry>();
		Class<?> componentType = null;

		if (newUsrEntries != null) {
			for (ICSettingEntry entry : newUsrEntries) {
				if (entry.isBuiltIn() || entry.isReadOnly())
					continue;
				set.add(entry);
			}
			componentType = newUsrEntries.getClass().getComponentType();
		}

		if (oldEntries != null) {
			for (ICSettingEntry entry : oldEntries) {
				if (entry.isBuiltIn() || entry.isReadOnly())
					set.add(entry);
			}
			if (componentType == null)
				componentType = oldEntries.getClass().getComponentType();
		}

		if (componentType != null) {
			ICSettingEntry[] result = (ICSettingEntry[])Array.newInstance(componentType, set.size());
			set.toArray(result);
			return result;
		}
		return null;
	}

	private void applySourceEntries(ICSourceEntry entries[], int op) {
		ICSourceEntry[] oldEntries = fCfgData.getSourceEntries();
		oldEntries = (ICSourceEntry[])CDataUtil.makeRelative(fProject, oldEntries, true);
		entries = (ICSourceEntry[])CDataUtil.makeRelative(fProject, entries, true);
		entries = (ICSourceEntry[])replaceUserEntries(oldEntries, entries);

		switch (op) {
		case OP_ADD:
			if (entries != null && entries.length != 0) {
				Set<ICSourceEntry> set = new LinkedHashSet<ICSourceEntry>();
				set.addAll(Arrays.asList(oldEntries));
				set.addAll(Arrays.asList(entries));
				fCfgData.setSourceEntries(set.toArray(new ICSourceEntry[set.size()]));
			}
			break;
		case OP_REMOVE:
			if (entries != null && entries.length != 0) {
				Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
				set.addAll(Arrays.asList(oldEntries));
				set.removeAll(Arrays.asList(entries));
				fCfgData.setSourceEntries(set.toArray(new ICSourceEntry[set.size()]));
			}
			break;
		case OP_REPLACE:
		default:
			if (entries != null) {
				fCfgData.setSourceEntries(entries);
			} else {
				fCfgData.setSourceEntries(new ICSourceEntry[0]);
			}
			break;
		}
	}

	private void applyOutputEntries(ICOutputEntry entries[], int op) {
		CBuildData bData = fCfgData.getBuildData();
		if (bData == null) {
			CCorePlugin.log(UtilMessages.getString("PathEntryTranslator.2")); //$NON-NLS-1$
			return;
		}

		ICOutputEntry[] oldEntries = bData.getOutputDirectories();
		oldEntries = (ICOutputEntry[])CDataUtil.makeRelative(fProject, oldEntries, true);
		entries = (ICOutputEntry[])CDataUtil.makeRelative(fProject, entries, true);
		entries = (ICOutputEntry[])replaceUserEntries(oldEntries, entries);

		switch (op) {
		case OP_ADD:
			if (entries != null && entries.length != 0) {
				Set<ICOutputEntry> set = new LinkedHashSet<ICOutputEntry>();
				set.addAll(Arrays.asList(oldEntries));
				set.addAll(Arrays.asList(entries));
				bData.setOutputDirectories(set.toArray(new ICOutputEntry[set.size()]));
			}
			break;
		case OP_REMOVE:
			if (entries != null && entries.length != 0) {
				Set<ICOutputEntry> set = new HashSet<ICOutputEntry>();
				set.addAll(Arrays.asList(oldEntries));
				set.removeAll(Arrays.asList(entries));
				bData.setOutputDirectories(set.toArray(new ICOutputEntry[set.size()]));
			}
			break;
		case OP_REPLACE:
		default:
			if (entries != null) {
				bData.setOutputDirectories(entries);
			} else {
				bData.setOutputDirectories(new ICOutputEntry[0]);
			}
			break;
		}
	}

	private void applyLangSettings(PathSettingsContainer cr, int op) {
		PathSettingsContainer crs[] = cr.getChildren(true);
		for (PathSettingsContainer cur : crs) {
			RcDesInfo desInfo = (RcDesInfo)cur.getValue();
			try {
				CResourceData rcData = getResourceData(cur.getPath(), true, true);
				applyEntries(rcData, desInfo, op);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}

		CResourceData[] rcDatas = getResourceDatas();
		for (CResourceData rcData : rcDatas) {
			PathSettingsContainer c = cr.getChildContainer(rcData.getPath(), false, false);
			if (cr.getPath().makeRelative().equals(rcData.getPath().makeRelative())) {
				continue;
			}

			RcDesInfo desInfo = (RcDesInfo) c.getValue();
			applyEntries(rcData, desInfo, op);
		}
	}

	private CResourceData[] getResourceDatas() {
		PathSettingsContainer crs[] = fRcDataHolder.getChildren(true);
		List<CResourceData> list = new ArrayList<CResourceData>(crs.length);
		for (PathSettingsContainer cur : crs) {
			list.add((CResourceData)cur.getValue());
		}
		return list.toArray(new CResourceData[list.size()]);
	}

	private CResourceData getResourceData(IPath path, boolean create, boolean exactPath) throws CoreException{
		PathSettingsContainer rcDataH = fRcDataHolder.getChildContainer(path, false, exactPath);
		if (rcDataH != null) {
			return (CResourceData)rcDataH.getValue();
		} else if (create) {
			ResourceInfo rcInfo = findResourceInfo(fProject, path, true);
			CResourceData base = getResourceData(path, false, false);

			CResourceData newRcData;
			if (rcInfo.fRc.getType() == IResource.FILE) {
				if (base.getType() == ICSettingBase.SETTING_FILE) {
					newRcData = fCfgData.createFileData(path, (CFileData)base);
				} else {
					CFolderData folderData = (CFolderData)base;
					CLanguageData lDatas[] = folderData.getLanguageDatas();
					CLanguageData baseLData = CDataUtil.findLanguagDataForFile(rcInfo.fRc.getFullPath().lastSegment(), fProject, lDatas);
					newRcData = fCfgData.createFileData(path, folderData, baseLData);
				}
			} else {
				while (base.getType() == ICSettingBase.SETTING_FILE) {
					base = getResourceData(base.getPath().removeLastSegments(1), false, false);
				}

				newRcData = fCfgData.createFolderData(path, (CFolderData)base);
			}

			fRcDataHolder.getChildContainer(path, true, true).setValue(newRcData);
			return newRcData;
		}
		return null;
	}

	private void applyEntries(CResourceData rcData, RcDesInfo info, int op) {
		CLanguageData lDatas[] = rcData.getType() == ICSettingBase.SETTING_FILE ?
				new CLanguageData[] { ((CFileData)rcData).getLanguageData() } :
				((CFolderData) rcData).getLanguageDatas();

		for (CLanguageData lData : lDatas) {
			if (lData == null)
				continue;

			applyEntries(lData, info, op);
		}
	}


	private void applyEntries(CLanguageData lData, RcDesInfo info, int op) {
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int supported = lData.getSupportedEntryKinds();
		for (int kind : kinds) {
			if ((supported & kind) == 0)
				continue;

			ICLanguageSettingEntry opEntries[] = info.getEntries(kind);
			ICLanguageSettingEntry oldEntries[] = lData.getEntries(kind);
			opEntries = (ICLanguageSettingEntry[])replaceUserEntries(oldEntries, opEntries);

			if (op == OP_REPLACE)
				oldEntries = null;
			ICLanguageSettingEntry result[] = composeNewEntries(oldEntries, opEntries, op);
			lData.setEntries(kind, result);
		}
	}

	private ICLanguageSettingEntry[] composeNewEntries(ICLanguageSettingEntry oldEntries[],
			ICLanguageSettingEntry newEntries[],
			int op) {
		ICLanguageSettingEntry result[];
		switch (op) {
		case OP_ADD:{
			Set<ICLanguageSettingEntry> oldSet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(oldEntries));
			Set<ICLanguageSettingEntry> newSet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(newEntries));
			newSet.removeAll(oldSet);
			if (newSet.size() == 0) {
				result = oldEntries;
			} else {
				result = new ICLanguageSettingEntry[oldEntries.length + newSet.size()];
				newSet.toArray(result);
				System.arraycopy(oldEntries, 0, result, newSet.size(), oldEntries.length);
			}
			break;
		}
		case OP_REMOVE:{
			Set<ICLanguageSettingEntry> oldSet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(oldEntries));
			Set<ICLanguageSettingEntry> newSet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(newEntries));
			oldSet.removeAll(newSet);
			if (oldSet.size() == 0) {
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

	public ReferenceSettingsInfo applyPathEntries(IPathEntry[] usrEntries, IPathEntry[] sysEntries, int op) {
		ResolvedEntry[] rEntries = getResolvedEntries(usrEntries, sysEntries);
		return addPathEntries(rEntries, op);
	}

	private void propagateValues(PathSettingsContainer cr, List<LangEntryInfo> langEntryInfoList) {

		RcDesInfo rcDes = (RcDesInfo)cr.getValue();
		if (rcDes != null) {
			List<ResolvedEntry> rEntries = rcDes.fResolvedEntries;
			List<LangEntryInfo> curLanfInfos = new ArrayList<LangEntryInfo>(rEntries.size() + langEntryInfoList.size());
			for (ResolvedEntry re : rEntries) {
				LangEntryInfo li = createLangEntryInfo(re);
				if (li != null) {
					curLanfInfos.add(li);
				}
			}

			curLanfInfos.addAll(langEntryInfoList);
			langEntryInfoList = curLanfInfos;

			for (LangEntryInfo li : langEntryInfoList) {
				rcDes.add(li);
			}
		}

		PathSettingsContainer directChildren[] = cr.getDirectChildren();
		for (PathSettingsContainer directChild : directChildren) {
			filterAndPropagate(directChild, langEntryInfoList);
		}
	}

	private void filterAndPropagate(PathSettingsContainer cr, List<LangEntryInfo> list) {
		list = new ArrayList<LangEntryInfo>(list);
		IPath path = cr.getPath();
		for (Iterator<LangEntryInfo> iter = list.iterator(); iter.hasNext();) {
			LangEntryInfo li = iter.next();
			ResolvedEntry re = li.fResolvedEntry;
			ResourceInfo[] filters = re.getFilterInfos();
			for (ResourceInfo filter : filters) {
				IResource rc = filter.fRc;
				IPath projPath = rc.getProjectRelativePath();
				if (projPath.isPrefixOf(path.makeRelative())) {
					iter.remove();
					break;
				}
			}
		}

		propagateValues(cr, list);
	}

	private int toLanguageEntryKind(int peKind) {
		switch (peKind) {
		case IPathEntry.CDT_LIBRARY:
			return ICSettingEntry.LIBRARY_FILE;
//		case IPathEntry.CDT_PROJECT:
//			return ICLanguageSettingEntry;
//		case IPathEntry.CDT_SOURCE:
//			return INDEX_CDT_SOURCE;
		case IPathEntry.CDT_INCLUDE:
			return ICSettingEntry.INCLUDE_PATH;
//		case IPathEntry.CDT_CONTAINER:
//			return INDEX_CDT_CONTAINER;
		case IPathEntry.CDT_MACRO:
			return ICSettingEntry.MACRO;
//		case IPathEntry.CDT_OUTPUT:
//			return INDEX_CDT_OUTPUT;
		case IPathEntry.CDT_INCLUDE_FILE:
			return ICSettingEntry.INCLUDE_FILE;
		case IPathEntry.CDT_MACRO_FILE:
			return ICSettingEntry.MACRO_FILE;
		}
		return 0;
	}

	private static int peKindToSettingKind(int peKind) {
		switch (peKind) {
		case IPathEntry.CDT_LIBRARY:
			return ICSettingEntry.LIBRARY_FILE;
//		case IPathEntry.CDT_PROJECT:
//			return ICLanguageSettingEntry;
		case IPathEntry.CDT_SOURCE:
			return ICSettingEntry.SOURCE_PATH;
		case IPathEntry.CDT_INCLUDE:
			return ICSettingEntry.INCLUDE_PATH;
//		case IPathEntry.CDT_CONTAINER:
//			return INDEX_CDT_CONTAINER;
		case IPathEntry.CDT_MACRO:
			return ICSettingEntry.MACRO;
		case IPathEntry.CDT_OUTPUT:
			return ICSettingEntry.OUTPUT_PATH;
		case IPathEntry.CDT_INCLUDE_FILE:
			return ICSettingEntry.INCLUDE_FILE;
		case IPathEntry.CDT_MACRO_FILE:
			return ICSettingEntry.MACRO_FILE;
		}
		return 0;
	}

	private ResolvedEntry[] getResolvedEntries(PathEntryResolveInfo info) {
		PathEntryResolveInfoElement els[] = info.getElements();
		List<ResolvedEntry> list = new ArrayList<ResolvedEntry>();
		for (PathEntryResolveInfoElement el : els) {
			getResolvedEntries(el, list);
		}
		return list.toArray(new ResolvedEntry[list.size()]);
	}

	private List<ResolvedEntry> getResolvedEntries(PathEntryResolveInfoElement el, List<ResolvedEntry> list) {
		if (list == null)
			list = new ArrayList<ResolvedEntry>();

		IPathEntry[] rpEntries = el.getResolvedEntries();
		ResolvedEntry resolvedE;
		IPathEntry rawEntry = el.getRawEntry();
		if (rawEntry.getEntryKind() == IPathEntry.CDT_PROJECT) {
			resolvedE = createResolvedEntry(rawEntry, el);
			if (resolvedE != null)
				list.add(resolvedE);
		}
		for (IPathEntry rpEntry : rpEntries) {
			resolvedE = createResolvedEntry(rpEntry, el);
			if (resolvedE != null)
				list.add(resolvedE);
		}
		return list;
	}

	private ResolvedEntry createResolvedEntry(IPathEntry entry, PathEntryResolveInfoElement el) {
		switch (entry.getEntryKind()) {
//		case IPathEntry.CDT_PROJECT:
//			//should not be here
		case IPathEntry.CDT_CONTAINER:
			//the case of extension path entry container
			return null;
		}
		return new ResolvedEntry(entry, el);
	}


	private ResolvedEntry[] getResolvedEntries(IPathEntry[] usrEntries, IPathEntry[] sysEntries) {
		int length = usrEntries != null ? usrEntries.length : 0;
		if (sysEntries != null)
			length += sysEntries.length;
		ResolvedEntry[] rEntries = new ResolvedEntry[length];
		int num = 0;
		if (usrEntries != null) {
			for (IPathEntry usrEntry : usrEntries) {
				rEntries[num++] = new ResolvedEntry(usrEntry, false);
			}
		}

		if (sysEntries != null) {
			for (IPathEntry sysEntry : sysEntries) {
				rEntries[num++] = new ResolvedEntry(sysEntry, true);
			}
		}
		return rEntries;
	}

	private ResourceInfo[] resolveFilter(IContainer container, IPath path) {
		IPath containerFullPath = container.getFullPath();
		IPath fullPath = containerFullPath.append(path);
		PathSettingsContainer cr = fTranslatedFilters.getChildContainer(fullPath, false, false);
		ResourceInfo[] baseInfos = (ResourceInfo[])cr.getValue();
		ResourceInfo[] result;
		if (!baseInfos[0].fExists) {
			// resource does not exist, always create new rc info and not add it to map
			ResourceInfo inexistent = new ResourceInfo(container.getFolder(path), false);
			result = new ResourceInfo[]{inexistent};
		} else {
			// base exists
			IPath baseTranslatedPath = cr.getPath();
			if (baseTranslatedPath.equals(fullPath)) {
				result = baseInfos;
			} else if (containerFullPath.isPrefixOf(baseTranslatedPath)) {
				IPath filterToTranslate = fullPath.removeFirstSegments(baseTranslatedPath.segmentCount());
				result = performTranslation(baseTranslatedPath, baseInfos, filterToTranslate);
			} else {
				// should never be here
				throw new IllegalStateException();
			}
		}

		return result;
	}

	private ResourceInfo[] performTranslation(IPath basePath, ResourceInfo baseInfos[], IPath filter) {
		ResourceInfo result[];
		int segCount = filter.segmentCount();
		String seg;
		int i = 0;
		for (; i < segCount; i++) {
			if (!baseInfos[0].fExists)
				break;
			seg = filter.segment(0);
			baseInfos = performTranslation(basePath, baseInfos, seg);
			basePath = basePath.append(seg);
			filter = filter.removeFirstSegments(1);
		}

		if (i < segCount) {
			result = new ResourceInfo[baseInfos.length];
			ResourceInfo baseInfo;
			IFolder rc;

			for (int k = 0; k < baseInfos.length; k++) {
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

	private ResourceInfo[] performTranslation(IPath basePath, ResourceInfo[] baseInfos, String seg) {
		IPath filterFullPath = basePath.append(seg);
		boolean needsParsing = hasSpecChars(seg);
		List<ResourceInfo> list = new ArrayList<ResourceInfo>();
		char[] segChars = seg.toCharArray();
		for (ResourceInfo baseInfo : baseInfos) {
			IResource baseRc = baseInfo.fRc;
			if (baseRc.getType() == IResource.FILE) {
				continue;
			} else {
				IContainer baseCr = (IContainer)baseRc;
				IResource rc = baseCr.findMember(seg);
				if (rc != null) {
					ResourceInfo rcInfo = new ResourceInfo(rc, true);
					addRcInfoToMap(rcInfo);
					list.add(rcInfo);
				} else if (needsParsing) {
					try {
						IResource children[] = baseCr.members();
						ResourceInfo rcInfo;
						for (IResource child : children) {
							if (CoreModelUtil.match(segChars, child.getName().toCharArray(), true)) {
								rcInfo = new ResourceInfo(child, true);
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

		if (list.size() == 0) {
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

	private boolean hasSpecChars(String str) {
		for (char ch : SPEC_CHARS) {
			if (str.indexOf(ch) != -1)
				return true;
		}
		return false;
	}

	private void addResolvedFilterToMap(IPath fullFilterPath ,ResourceInfo[] resolved, boolean check) {
		if (check) {
			PathSettingsContainer cr = fTranslatedFilters.getChildContainer(fullFilterPath, false, false);
			ResourceInfo[] infos = (ResourceInfo[])cr.getValue();
			if (!infos[0].fExists)
				return;
		}

		PathSettingsContainer cr = fTranslatedFilters.getChildContainer(fullFilterPath, true, true);
		cr.setValue(resolved);
	}

	private ResourceInfo findResourceInfo(IContainer container, IPath relPath, boolean folderIfNotExist) {
		IPath fullPath = container.getFullPath().append(relPath);
		ResourceInfo rcInfo = fResourceMap.get(fullPath);

		if (rcInfo == null) {
			IResource rc = container.findMember(relPath);
			boolean exists = true;
			if (rc == null) {
				exists = false;
				if (container.getType() == IResource.ROOT && relPath.segmentCount() == 1) {
					rc = fRoot.getProject(relPath.segment(0));
				} else if (folderIfNotExist) {
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

	private void addRcInfoToMap(ResourceInfo rcInfo) {
		IPath fullPath = rcInfo.fRc.getFullPath();
		fResourceMap.put(fullPath, rcInfo);
		addResolvedFilterToMap(fullPath, new ResourceInfo[]{rcInfo}, true);
	}

	public static IPathEntry[] decodePathEntries(IProject project, ICStorageElement el) {
		ArrayList<IPathEntry> pathEntries = new ArrayList<IPathEntry>();
		ICStorageElement children[] = el.getChildren();
		for (ICStorageElement child : children) {
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

	private static String getAttribute(ICStorageElement el, String attr) {
		String v = el.getAttribute(attr);
		if (v != null)
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
			case IPathEntry.CDT_PROJECT:
				return CoreModel.newProjectEntry(path, isExported);
			case IPathEntry.CDT_LIBRARY: {
				IPath libraryPath = new Path(getAttribute(element, ATTRIBUTE_LIBRARY));
				// source attachment info (optional)
				IPath sourceAttachmentPath = element.getAttribute(ATTRIBUTE_SOURCEPATH) != null ? new Path(
						element.getAttribute(ATTRIBUTE_SOURCEPATH)) : null;
				IPath sourceAttachmentRootPath = element.getAttribute(ATTRIBUTE_ROOTPATH) != null ? new Path(
						element.getAttribute(ATTRIBUTE_ROOTPATH)) : null;
				IPath sourceAttachmentPrefixMapping = element.getAttribute(ATTRIBUTE_PREFIXMAPPING) != null ? new Path(
						element.getAttribute(ATTRIBUTE_PREFIXMAPPING)) : null;

				if (!baseRef.isEmpty()) {
					return CoreModel.newLibraryRefEntry(path, baseRef, libraryPath);
				}
				return CoreModel.newLibraryEntry(path, basePath, libraryPath, sourceAttachmentPath, sourceAttachmentRootPath,
					sourceAttachmentPrefixMapping, isExported);
			}
			case IPathEntry.CDT_SOURCE: {
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
			case IPathEntry.CDT_OUTPUT:
				return CoreModel.newOutputEntry(path, exclusionPatterns);
			case IPathEntry.CDT_INCLUDE: {
				// include path info
				IPath includePath = new Path(getAttribute(element, ATTRIBUTE_INCLUDE));
				// isSysteminclude
				boolean isSystemInclude = false;
				if (element.getAttribute(ATTRIBUTE_SYSTEM) != null) {
					isSystemInclude = getAttribute(element, ATTRIBUTE_SYSTEM).equals(VALUE_TRUE);
				}
				if (!baseRef.isEmpty()) {
					return CoreModel.newIncludeRefEntry(path, baseRef, includePath);
				}
				return CoreModel.newIncludeEntry(path, basePath, includePath, isSystemInclude, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_INCLUDE_FILE: {
				// include path info
				IPath includeFilePath = new Path(getAttribute(element, ATTRIBUTE_INCLUDE_FILE));
				return CoreModel.newIncludeFileEntry(path, basePath, baseRef, includeFilePath, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_MACRO: {
				String macroName = getAttribute(element, ATTRIBUTE_NAME);
				String macroValue = getAttribute(element, ATTRIBUTE_VALUE);
				if (!baseRef.isEmpty()) {
					return CoreModel.newMacroRefEntry(path, baseRef, macroName);
				}
				return CoreModel.newMacroEntry(path, macroName, macroValue, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_MACRO_FILE: {
				IPath macroFilePath = new Path(getAttribute(element, ATTRIBUTE_MACRO_FILE));
				return CoreModel.newMacroFileEntry(path, basePath, baseRef, macroFilePath, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_CONTAINER: {
				IPath id = new Path(getAttribute(element, ATTRIBUTE_PATH));
				return CoreModel.newContainerEntry(id, isExported);
			}
			default: {
				ICModelStatus status = new CModelStatus(IStatus.ERROR, "PathEntry: unknown kind (" + kindAttr + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new CModelException(status);
			}
		}
	}

	private static CConfigurationData getCfgData(ICConfigurationDescription cfgDescription) {
		return cfgDescription instanceof CConfigurationDescriptionCache ?
				(CConfigurationData)cfgDescription : ((IInternalCCfgInfo)cfgDescription).getConfigurationData(false);
	}

	private static void addOutputEntries(PathEntryCollector cr, CConfigurationData cfgData) {
		CBuildData bData = cfgData.getBuildData();
		if (bData != null) {
			ICOutputEntry oEntries[] = bData.getOutputDirectories();
			if (oEntries != null && oEntries.length != 0) {
				cr.setSourceOutputEntries(ICSettingEntry.OUTPUT_PATH, oEntries);
			}
		}
	}

	public static PathEntryCollector collectEntries(IProject project, final ICConfigurationDescription cfgDescription) {
		CConfigurationData cfgData = getCfgData(cfgDescription);

		ReferenceSettingsInfo refInfo = new ReferenceSettingsInfo(cfgDescription);
		ICConfigurationDescription[] allCfgDescriptions = cfgDescription.isPreferenceConfiguration() ?
				new ICConfigurationDescription[] { cfgDescription } :
				cfgDescription.getProjectDescription().getConfigurations();

		CConfigurationData[] allDatas = new CConfigurationData[allCfgDescriptions.length];
		for (int i = 0; i < allCfgDescriptions.length; i++) {
			allDatas[i] = getCfgData(allCfgDescriptions[i]);
		}

		final PathEntryCollector collector = new PathEntryCollector(project/*, cfgDescription*/);
		PathSettingsContainer rcDatas = createRcDataHolder(cfgData);
		ICSourceEntry sEntries[] = cfgData.getSourceEntries();
		if (sEntries != null && sEntries.length != 0) {
			collector.setSourceOutputEntries(ICSettingEntry.SOURCE_PATH, sEntries);
		}
		for (CConfigurationData allData : allDatas) {
			addOutputEntries(collector, allData);
		}
		final HashSet<ICSettingEntry> exportedSettings = new HashSet<ICSettingEntry>();
		collector.setRefProjects(refInfo.getReferencedProjectsPaths());
		ICExternalSetting[] settings = refInfo.getExternalSettings();
		for (ICExternalSetting setting : settings) {
			exportedSettings.addAll(Arrays.asList(setting.getEntries()));
		}

		final int kinds[] = KindBasedStore.getLanguageEntryKinds();
		rcDatas.accept(new IPathSettingsContainerVisitor() {
			@Override
			public boolean visit(PathSettingsContainer container) {
				CResourceData rcData = (CResourceData)container.getValue();
				if (rcData != null) {
					PathEntryCollector child = collector.createChild(container.getPath());
					for (int kind : kinds) {
						List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
						if (collectResourceDataEntries(cfgDescription, kind, rcData, list)) {
							ICLanguageSettingEntry[] entries = list.toArray(new ICLanguageSettingEntry[list.size()]);
							child.setEntries(kind, entries, exportedSettings);
						}
					}
				}
				return true;
			}
		});
		return collector;
	}

	private static boolean collectResourceDataEntries(ICConfigurationDescription cfgDescription, int kind, CResourceData rcData, List<ICLanguageSettingEntry> list) {
		CLanguageData[] lDatas = null;
		if (rcData instanceof CFolderData) {
			lDatas = ((CFolderData)rcData).getLanguageDatas();
		} else if (rcData instanceof CFileData) {
			CLanguageData lData = ((CFileData)rcData).getLanguageData();
			if (lData != null)
				lDatas = new CLanguageData[] {lData};
		} else {
			Exception e = new Exception(UtilMessages.getString("PathEntryTranslator.1") + rcData.getClass().getName()); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, e.getMessage(), e);
			CCorePlugin.log(status);
		}
		if (lDatas == null || lDatas.length == 0) {
			return false;
		}

		IProject project = cfgDescription.getProjectDescription().getProject();
		if (ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(project)) {
			IResource rc = findResourceInWorkspace(project, rcData.getPath());
			for (CLanguageData lData : lDatas) {
				list.addAll(LanguageSettingsProvidersSerializer.getSettingEntriesByKind(cfgDescription, rc, lData.getLanguageId(), kind));
			}
			return list.size()>0;

		}
		// Legacy logic
		boolean supported = false;
		for (CLanguageData lData : lDatas) {
			if (collectLanguageDataEntries(kind, lData, list))
				supported = true;
		}
		return supported;
	}

	private static boolean collectLanguageDataEntries(int kind, CLanguageData lData, List<ICLanguageSettingEntry> list) {
		if ((kind & lData.getSupportedEntryKinds()) != 0) {
			ICLanguageSettingEntry[] entries = lData.getEntries(kind);
			if (entries != null && entries.length != 0) {
				list.addAll(Arrays.asList(entries));
			}
			return true;
		}

		return false;
	}

	public static IPathEntry[] getPathEntries(IProject project, ICConfigurationDescription cfgDescription, int flags) {
		PathEntryCollector cr = collectEntries(project, cfgDescription);
		return cr.getEntries(flags, cfgDescription);
	}

	private static IResource findResourceInWorkspace(IProject project, IPath workspacePath) {
		IResource rc;
		if (project != null) {
			rc = project.findMember(workspacePath);
		} else {
			rc = ResourcesPlugin.getWorkspace().getRoot().findMember(workspacePath);
		}
		return rc;
	}
}
