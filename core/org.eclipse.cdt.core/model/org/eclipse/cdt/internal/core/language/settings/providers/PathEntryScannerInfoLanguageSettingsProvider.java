/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.DefaultPathEntryStore;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.cdt.internal.core.model.PathEntryUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Wrapper class intended to provide backward compatibility with ScannerInfo supplied by {@link PathEntryManager}.
 */
public class PathEntryScannerInfoLanguageSettingsProvider extends LanguageSettingsBaseProvider {
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId) {
		if (cfgDescription == null) {
			return null;
		}
		ICProjectDescription prjDescription = cfgDescription.getProjectDescription();
		if (prjDescription == null) {
			return null;
		}

		IProject project = prjDescription.getProject();
		ICProject cproject = CModelManager.getDefault().getCModel().getCProject(project);
		IPath projectPath = cproject.getPath();

		// using map of sets to support specific ordering of entries
		LinkedHashMap<Integer, Set<IPathEntry>> pathEntriesMap = new LinkedHashMap<>();
		pathEntriesMap.put(IPathEntry.CDT_INCLUDE, new LinkedHashSet<IPathEntry>());
		// keep macros sorted
		pathEntriesMap.put(IPathEntry.CDT_MACRO, new TreeSet<>(new Comparator<IPathEntry>() {
			@Override
			public int compare(IPathEntry macro1, IPathEntry macro2) {
				if (macro1 instanceof IMacroEntry && macro2 instanceof IMacroEntry) {
					return ((IMacroEntry) macro1).getMacroName().compareTo(((IMacroEntry) macro2).getMacroName());
				}
				return 0;
			}
		}));
		pathEntriesMap.put(IPathEntry.CDT_INCLUDE_FILE, new LinkedHashSet<IPathEntry>());
		pathEntriesMap.put(IPathEntry.CDT_MACRO_FILE, new LinkedHashSet<IPathEntry>());
		pathEntriesMap.put(IPathEntry.CDT_LIBRARY, new LinkedHashSet<IPathEntry>());

		IPathEntryStore pathEntryStore = new DefaultPathEntryStore(project);
		int typesMask = IPathEntry.CDT_INCLUDE | IPathEntry.CDT_MACRO | IPathEntry.CDT_INCLUDE_FILE
				| IPathEntry.CDT_MACRO_FILE | IPathEntry.CDT_LIBRARY;
		try {
			IPathEntry[] storePathEntries = pathEntryStore.getRawPathEntries();
			for (IPathEntry storePathEntry : storePathEntries) {
				if (storePathEntry instanceof IContainerEntry) {
					try {
						IPathEntryContainer container = PathEntryManager.getDefault()
								.getPathEntryContainer((IContainerEntry) storePathEntry, cproject);
						if (container != null) {
							IPathEntry[] pathEntries = null;
							if (container instanceof IPathEntryContainerExtension) {
								pathEntries = ((IPathEntryContainerExtension) container)
										.getPathEntries(rc.getFullPath(), typesMask);
							} else {
								pathEntries = container.getPathEntries();
							}
							if (pathEntries != null) {
								for (IPathEntry pathEntry : pathEntries) {
									collectPathEntry(pathEntriesMap, projectPath, pathEntry);
								}
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				} else {
					collectPathEntry(pathEntriesMap, projectPath, storePathEntry);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		Set<ICLanguageSettingEntry> lsEntries = new LinkedHashSet<>();
		for (Entry<Integer, Set<IPathEntry>> entrySet : pathEntriesMap.entrySet()) {
			Set<IPathEntry> pathEntries = entrySet.getValue();
			for (IPathEntry pathEntry : pathEntries) {
				ICLanguageSettingEntry lsEntry = toLanguageSettingsEntry(pathEntry);
				if (lsEntry != null) {
					lsEntries.add(lsEntry);
				}
			}
		}

		return LanguageSettingsSerializableStorage.getPooledList(new ArrayList<>(lsEntries));
	}

	private void collectPathEntry(LinkedHashMap<Integer, Set<IPathEntry>> pathEntriesMap, IPath projectPath,
			IPathEntry pathEntry) {
		switch (pathEntry.getEntryKind()) {
		case IPathEntry.CDT_INCLUDE:
		case IPathEntry.CDT_MACRO:
		case IPathEntry.CDT_INCLUDE_FILE:
		case IPathEntry.CDT_MACRO_FILE:
		case IPathEntry.CDT_LIBRARY:
			IPathEntry resolvedPathEntry = PathEntryUtil.cloneEntryAndExpand(projectPath, pathEntry);
			Set<IPathEntry> set = pathEntriesMap.get(resolvedPathEntry.getEntryKind());
			if (set != null) {
				set.add(resolvedPathEntry);
			}
		}
	}

	private ICLanguageSettingEntry toLanguageSettingsEntry(IPathEntry pathEntry) {
		switch (pathEntry.getEntryKind()) {
		case IPathEntry.CDT_INCLUDE:
			IIncludeEntry includeEntry = (IIncludeEntry) pathEntry;
			return CDataUtil.createCIncludePathEntry(includeEntry.getFullIncludePath().toOSString(),
					includeEntry.isSystemInclude() ? 0 : ICSettingEntry.LOCAL);
		case IPathEntry.CDT_MACRO:
			IMacroEntry macroEntry = (IMacroEntry) pathEntry;
			return CDataUtil.createCMacroEntry(macroEntry.getMacroName(), macroEntry.getMacroValue(), 0);
		case IPathEntry.CDT_INCLUDE_FILE:
			IIncludeFileEntry includeFileEntry = (IIncludeFileEntry) pathEntry;
			return CDataUtil.createCIncludeFileEntry(includeFileEntry.getFullIncludeFilePath().toOSString(), 0);
		case IPathEntry.CDT_MACRO_FILE:
			IMacroFileEntry macroFileEntry = (IMacroFileEntry) pathEntry;
			return CDataUtil.createCMacroFileEntry(macroFileEntry.getFullMacroFilePath().toOSString(), 0);
		case IPathEntry.CDT_LIBRARY:
			ILibraryEntry libraryEntry = (ILibraryEntry) pathEntry;
			return CDataUtil.createCLibraryFileEntry(libraryEntry.getFullLibraryPath().toOSString(), 0);
		}
		return null;
	}
}
