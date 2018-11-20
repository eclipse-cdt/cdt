/*******************************************************************************
 * Copyright (c) 2000, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     James Blackburn (Broadcom Corporation)
 *     John Dallaway - Eliminate path entry removal job (bug 410529)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.PathEntryContainerChanged;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.core.settings.model.util.PathEntryResolveInfo;
import org.eclipse.cdt.core.settings.model.util.PathEntryResolveInfoElement;
import org.eclipse.cdt.core.settings.model.util.ThreadLocalMap;
import org.eclipse.cdt.internal.core.settings.model.AbstractCExtensionProxy;
import org.eclipse.cdt.internal.core.settings.model.ConfigBasedPathEntryStore;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author alain
 */
public class PathEntryManager implements IPathEntryStoreListener, IElementChangedListener {
	// PathEntry extension
	public final static String PATHENTRY_STORE_ID = "PathEntryStore"; //$NON-NLS-1$
	public final static String PATHENTRY_STORE_UNIQ_ID = CCorePlugin.PLUGIN_ID + "." + PATHENTRY_STORE_ID; //$NON-NLS-1$

	static String CONTAINER_INITIALIZER_EXTPOINT_ID = "PathEntryContainerInitializer"; //$NON-NLS-1$
	/**
	 * An empty array of strings indicating that a project doesn't have any
	 * prerequesite projects.
	 */
	static final String[] NO_PREREQUISITES = {};
	/**
	 * pathentry containers pool accessing the Container is done synch with the
	 * class
	 */
	private static HashMap<ICProject, Map<IPath, IPathEntryContainer>> Containers = new HashMap<>(5);

	static final IPathEntry[] NO_PATHENTRIES = {};

	static final IIncludeEntry[] NO_INCLUDE_ENTRIES = {};

	static final IIncludeFileEntry[] NO_INCLUDE_FILE_ENTRIES = {};

	static final IMacroEntry[] NO_MACRO_ENTRIES = {};

	static final IMacroFileEntry[] NO_MACRO_FILE_ENTRIES = {};

	static final IPathEntryContainer[] NO_PATHENTRYCONTAINERS = {};

	// Synchronized the access of the cache entries.
	private final Map<ICProject, Object> cacheComputationMarkers = new HashMap<>();
	protected Map<ICProject, ArrayList<IPathEntry>> resolvedMap = new Hashtable<>();
	private final Map<ICProject, PathEntryResolveInfo> resolvedInfoMap = new Hashtable<>();
	private final ThreadLocalMap resolveInfoValidState = new ThreadLocalMap();

	// Accessing the map is synch with the class
	private final Map<IProject, IPathEntryStore> storeMap = new HashMap<>();

	private static PathEntryManager pathEntryManager;

	protected ConcurrentLinkedQueue<PathEntryProblem> markerProblems = new ConcurrentLinkedQueue<>();

	// Setting up a generate markers job, it does not get scheduled
	Job markerTask = new GenerateMarkersJob("PathEntry Marker Job"); //$NON-NLS-1$

	private PathEntryManager() {
	}

	private class PathEntryProblem {
		IProject project;
		ICModelStatus[] problems;

		public PathEntryProblem(IProject project, ICModelStatus[] problems) {
			this.project = project;
			this.problems = problems;
		}
	}

	private class PathEntryContainerLock implements IPathEntryContainer {
		boolean runInitializer;

		public boolean isContainerInitialize() {
			return runInitializer;
		}

		public void setContainerInitialize(boolean init) {
			runInitializer = init;
		}

		@Override
		public IPathEntry[] getPathEntries() {
			return NO_PATHENTRIES;
		}

		@Override
		public String getDescription() {
			return "Lock container"; //$NON-NLS-1$
		}

		@Override
		public IPath getPath() {
			return Path.EMPTY;
		}
	}

	/**
	 * Returns the singleton.
	 */
	public static synchronized PathEntryManager getDefault() {
		if (pathEntryManager == null) {
			pathEntryManager = new PathEntryManager();
			CoreModel.getDefault().addElementChangedListener(pathEntryManager);
		}
		return pathEntryManager;
	}

	public IIncludeFileEntry[] getIncludeFileEntries(IPath resPath) throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getIncludeFileEntries((ITranslationUnit) celement);
		}
		if (celement != null) {
			// Get project include file entries.
			List<IPathEntry> entryList = new ArrayList<>();
			ICProject cproject = celement.getCProject();
			ArrayList<IPathEntry> resolvedListEntries = getResolvedPathEntries(cproject, false);
			IPathEntry[] pathEntries = getCachedResolvedPathEntries(resolvedListEntries, cproject);
			for (IPathEntry entry : pathEntries) {
				if ((entry.getEntryKind() & IPathEntry.CDT_INCLUDE_FILE) != 0) {
					entryList.add(entry);
				}
			}
			IIncludeFileEntry[] incFiles = entryList.toArray(new IIncludeFileEntry[entryList.size()]);
			return incFiles;
		}
		return NO_INCLUDE_FILE_ENTRIES;
	}

	public IIncludeFileEntry[] getIncludeFileEntries(ITranslationUnit cunit) throws CModelException {
		List<IPathEntry> list = getPathEntries(cunit, IPathEntry.CDT_INCLUDE_FILE);
		IIncludeFileEntry[] incFiles = list.toArray(new IIncludeFileEntry[list.size()]);
		return incFiles;
	}

	public IIncludeEntry[] getIncludeEntries(IPath resPath) throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getIncludeEntries((ITranslationUnit) celement);
		}
		if (celement != null) {
			// get project include entries
			List<IPathEntry> entryList = new ArrayList<>();
			ICProject cproject = celement.getCProject();
			ArrayList<IPathEntry> resolvedListEntries = getResolvedPathEntries(cproject, false);
			IPathEntry[] pathEntries = getCachedResolvedPathEntries(resolvedListEntries, cproject);
			for (IPathEntry entry : pathEntries) {
				if ((entry.getEntryKind() & IPathEntry.CDT_INCLUDE) != 0) {
					entryList.add(entry);
				}
			}
			IIncludeEntry[] includes = entryList.toArray(new IIncludeEntry[entryList.size()]);
			return includes;
		}
		return NO_INCLUDE_ENTRIES;
	}

	public IIncludeEntry[] getIncludeEntries(ITranslationUnit cunit) throws CModelException {
		List<IPathEntry> list = getPathEntries(cunit, IPathEntry.CDT_INCLUDE);
		IIncludeEntry[] includes = list.toArray(new IIncludeEntry[list.size()]);
		return includes;
	}

	public IMacroEntry[] getMacroEntries(IPath resPath) throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getMacroEntries((ITranslationUnit) celement);
		}
		if (celement != null) {
			// get project macro entries
			List<IPathEntry> entryList = new ArrayList<>();
			ICProject cproject = celement.getCProject();
			ArrayList<IPathEntry> resolvedListEntries = getResolvedPathEntries(cproject, false);
			IPathEntry[] pathEntries = getCachedResolvedPathEntries(resolvedListEntries, cproject);
			for (IPathEntry entry : pathEntries) {
				if ((entry.getEntryKind() & IPathEntry.CDT_MACRO) != 0) {
					entryList.add(entry);
				}
			}
			IMacroEntry[] macros = entryList.toArray(new IMacroEntry[entryList.size()]);
			return macros;
		}
		return NO_MACRO_ENTRIES;
	}

	private IMacroEntry[] getMacroEntries(ITranslationUnit cunit) throws CModelException {
		ArrayList<IPathEntry> macroList = new ArrayList<>();
		ICProject cproject = cunit.getCProject();
		IPath resPath = cunit.getPath();
		// Do this first so the containers get inialized.
		ArrayList<IPathEntry> resolvedListEntries = getResolvedPathEntries(cproject, false);
		for (int i = 0; i < resolvedListEntries.size(); ++i) {
			IPathEntry entry = resolvedListEntries.get(i);
			if (entry.getEntryKind() == IPathEntry.CDT_MACRO) {
				macroList.add(entry);
			}
		}
		IPathEntryContainer[] containers = getPathEntryContainers(cproject);
		for (IPathEntryContainer container : containers) {
			if (container instanceof IPathEntryContainerExtension) {
				IPathEntryContainerExtension extension = (IPathEntryContainerExtension) container;
				IPathEntry[] incs = extension.getPathEntries(resPath, IPathEntry.CDT_MACRO);
				macroList.addAll(Arrays.asList(incs));
			}
		}

		IMacroEntry[] macros = macroList.toArray(new IMacroEntry[macroList.size()]);
		macroList.clear();

		// For the macros the closest symbol will override
		// /projec/src/file.c --> NDEBUG=1
		// /project/src --> NDEBUG=0
		//
		// We will use NDEBUG=1 only
		int count = resPath.segmentCount();
		Map<String, IMacroEntry> symbolMap = new HashMap<>();
		for (int i = 0; i < count; i++) {
			IPath newPath = resPath.removeLastSegments(i);
			for (IMacroEntry macro : macros) {
				IPath otherPath = macro.getPath();
				if (newPath.equals(otherPath)) {
					String key = macro.getMacroName();
					if (!symbolMap.containsKey(key)) {
						symbolMap.put(key, macro);
					}
				}
			}
		}

		// Add the Project contributions last.
		for (int i = 0; i < resolvedListEntries.size(); i++) {
			IPathEntry entry = resolvedListEntries.get(i);
			if (entry != null && entry.getEntryKind() == IPathEntry.CDT_PROJECT) {
				IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(entry.getPath());
				if (res != null && res.getType() == IResource.PROJECT) {
					ICProject refCProject = CoreModel.getDefault().create((IProject) res);
					if (refCProject != null) {
						IPathEntry[] projEntries = refCProject.getResolvedPathEntries();
						for (IPathEntry projEntry : projEntries) {
							if (projEntry.isExported()) {
								if (projEntry.getEntryKind() == IPathEntry.CDT_MACRO) {
									IMacroEntry macro = (IMacroEntry) entry;
									String key = macro.getMacroName();
									if (!symbolMap.containsKey(key)) {
										symbolMap.put(key, macro);
									}
								}
							}
						}
					}
				}
			}
		}

		return symbolMap.values().toArray(NO_MACRO_ENTRIES);
	}

	public IMacroFileEntry[] getMacroFileEntries(IPath resPath) throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getMacroFileEntries((ITranslationUnit) celement);
		}
		return NO_MACRO_FILE_ENTRIES;
	}

	public IMacroFileEntry[] getMacroFileEntries(ITranslationUnit cunit) throws CModelException {
		List<IPathEntry> list = getPathEntries(cunit, IPathEntry.CDT_MACRO_FILE);
		IMacroFileEntry[] macFiles = list.toArray(new IMacroFileEntry[list.size()]);
		return macFiles;
	}

	private List<IPathEntry> getPathEntries(ITranslationUnit cunit, int type) throws CModelException {
		ArrayList<IPathEntry> entryList = new ArrayList<>();
		ICProject cproject = cunit.getCProject();
		IPath resPath = cunit.getPath();
		// Do this first so the containers get inialized.
		ArrayList<IPathEntry> resolvedListEntries = getResolvedPathEntries(cproject, false);
		for (int i = 0; i < resolvedListEntries.size(); ++i) {
			IPathEntry entry = resolvedListEntries.get(i);
			if ((entry.getEntryKind() & type) != 0) {
				entryList.add(entry);
			}
		}
		IPathEntryContainer[] containers = getPathEntryContainers(cproject);
		for (IPathEntryContainer container : containers) {
			if (container instanceof IPathEntryContainerExtension) {
				IPathEntryContainerExtension extension = (IPathEntryContainerExtension) container;
				IPathEntry[] incs = extension.getPathEntries(resPath, type);
				entryList.addAll(Arrays.asList(incs));
			}
		}

		IPathEntry[] entries = entryList.toArray(new IPathEntry[entryList.size()]);
		// Clear the list since we are reusing it.
		entryList.clear();

		// We need to reorder the include/macros:
		// includes with the closest match to the resource will come first
		// /project/src/file.c --> /usr/local/include
		// /project --> /usr/include
		//
		// /usr/local/include must come first.
		//
		int count = resPath.segmentCount();
		for (int i = 0; i < count; i++) {
			IPath newPath = resPath.removeLastSegments(i);
			for (IPathEntry entry : entries) {
				IPath otherPath = entry.getPath();
				if (newPath.equals(otherPath)) {
					entryList.add(entry);
				}
			}
		}

		// Since the include that comes from a project contribution are not
		// tied to a resource they are added last.
		for (int i = 0; i < resolvedListEntries.size(); i++) {
			IPathEntry entry = resolvedListEntries.get(i);
			if (entry != null && entry.getEntryKind() == IPathEntry.CDT_PROJECT) {
				IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(entry.getPath());
				if (res != null && res.getType() == IResource.PROJECT) {
					ICProject refCProject = CoreModel.getDefault().create((IProject) res);
					if (refCProject != null) {
						IPathEntry[] projEntries = refCProject.getResolvedPathEntries();
						for (IPathEntry projEntry : projEntries) {
							if (projEntry.isExported()) {
								if ((projEntry.getEntryKind() & type) != 0) {
									entryList.add(projEntry);
								}
							}
						}
					}
				}
			}
		}
		return entryList;
	}

	/**
	 * Return the cached entries, if no cache null.
	 *
	 * @param cproject
	 */
	protected IPathEntry[] getCachedResolvedPathEntries(ICProject cproject) {
		ArrayList<IPathEntry> resolvedListEntries = resolvedMap.get(cproject);
		if (resolvedListEntries != null) {
			try {
				return getCachedResolvedPathEntries(resolvedListEntries, cproject);
			} catch (CModelException e) {
				//
			}
		}
		return null;
	}

	public PathEntryResolveInfo getResolveInfo(ICProject cproject, boolean useCache) throws CModelException {
		PathEntryResolveInfo info = resolvedInfoMap.get(cproject);
		if (info == null && useCache) {
			getResolvedPathEntries(cproject);
			info = resolvedInfoMap.get(cproject);
		}
		if (info == null || !useCache || !getResolveInfoValidState(cproject)) {
			Object[] resolved = getResolvedPathEntries(cproject, false, false);
			if (resolved != null)
				info = (PathEntryResolveInfo) resolved[1];
		}
		return info;
	}

	private void setResolveInfoValidState(ICProject cproject, boolean valid) {
		Object v = valid ? null : Boolean.FALSE;
		resolveInfoValidState.set(cproject, v);
	}

	private boolean getResolveInfoValidState(ICProject cproject) {
		return resolveInfoValidState.get(cproject) == null;
	}

	protected IPathEntry[] removeCachedResolvedPathEntries(ICProject cproject) {
		ArrayList<IPathEntry> resolvedListEntries;
		synchronized (cacheComputationMarkers) {
			// Remove a potential marker, such that a result in progress is not
			// written to the cache.
			cacheComputationMarkers.remove(cproject);
			resolvedListEntries = resolvedMap.remove(cproject);
		}
		resolvedInfoMap.remove(cproject);
		if (resolvedListEntries != null) {
			try {
				return getCachedResolvedPathEntries(resolvedListEntries, cproject);
			} catch (CModelException e) {
				// Ignore
			}
		}
		return null;
	}

	private IPathEntry[] getCachedResolvedPathEntries(ArrayList<IPathEntry> resolvedListEntries, ICProject cproject)
			throws CModelException {
		IPathEntry[] entries = resolvedListEntries.toArray(NO_PATHENTRIES);
		boolean hasContainerExtension = false;
		for (IPathEntry entry : entries) {
			if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
				IContainerEntry centry = (IContainerEntry) entry;
				IPathEntryContainer container = getPathEntryContainer(centry, cproject);
				if (container instanceof IPathEntryContainerExtension) {
					hasContainerExtension = true;
					break;
				}
			}
		}
		if (hasContainerExtension) {
			IPath projectPath = cproject.getPath();
			ArrayList<IPathEntry> listEntries = new ArrayList<>(entries.length);
			for (IPathEntry entrie : entries) {
				if (entrie.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry centry = (IContainerEntry) entrie;
					IPathEntryContainer container = getPathEntryContainer(centry, cproject);
					if (container != null) {
						IPathEntry[] containerEntries = container.getPathEntries();
						if (containerEntries != null) {
							for (IPathEntry containerEntry : containerEntries) {
								IPathEntry newEntry = PathEntryUtil.cloneEntryAndExpand(projectPath, containerEntry);
								listEntries.add(newEntry);
							}
						}
					}
				} else {
					listEntries.add(entrie);
				}
			}
			entries = listEntries.toArray(NO_PATHENTRIES);
		}
		return entries;
	}

	public IPathEntry[] getResolvedPathEntries(ICProject cproject) throws CModelException {
		boolean treeLock = cproject.getProject().getWorkspace().isTreeLocked();
		ArrayList<IPathEntry> resolvedListEntries = getResolvedPathEntries(cproject, !treeLock);
		return getCachedResolvedPathEntries(resolvedListEntries, cproject);
	}

	/**
	 * This method will not expand container extending
	 * IPathEntryContainerExtension
	 *
	 * @param cproject
	 * @param generateMarkers
	 * @return
	 * @throws CModelException
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<IPathEntry> getResolvedPathEntries(ICProject cproject, boolean generateMarkers)
			throws CModelException {
		Object[] result = getResolvedPathEntries(cproject, generateMarkers, true);
		if (result != null) {
			return (ArrayList<IPathEntry>) result[0];
		}
		return null;
	}

	private Object[] getResolvedPathEntries(ICProject cproject, boolean generateMarkers, boolean useCache)
			throws CModelException {
		ArrayList<IPathEntry> resolvedEntries = null;
		PathEntryResolveInfo rInfo = null;
		if (useCache) {
			resolvedEntries = resolvedMap.get(cproject);
			rInfo = resolvedInfoMap.get(cproject);
		}
		if (resolvedEntries == null) {
			Object marker = null;
			if (useCache) {
				// Mark the fact that we are computing a result for the cache
				marker = new Object();
				synchronized (cacheComputationMarkers) {
					cacheComputationMarkers.put(cproject, marker);
				}
			}

			List<PathEntryResolveInfoElement> resolveInfoList = new ArrayList<>();
			IPath projectPath = cproject.getPath();
			IPathEntry[] rawEntries = getRawPathEntries(cproject);
			resolvedEntries = new ArrayList<>();
			for (IPathEntry entry : rawEntries) {
				// Expand the containers.
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry centry = (IContainerEntry) entry;
					IPathEntryContainer container = getPathEntryContainer(centry, cproject);
					if (container != null) {
						// For backward compatibility we need to expand and
						// cache container that
						// are not IPathEntryContainerExtension.
						if (!(container instanceof IPathEntryContainerExtension)) {
							IPathEntry[] containerEntries = container.getPathEntries();
							List<IPathEntry> resolvedList = new ArrayList<>();
							if (containerEntries != null) {
								for (IPathEntry containerEntry : containerEntries) {
									IPathEntry newEntry = PathEntryUtil.cloneEntryAndExpand(projectPath,
											containerEntry);
									resolvedEntries.add(newEntry);
									resolvedList.add(newEntry);
								}
							}
							resolveInfoList.add(new PathEntryResolveInfoElement(centry, resolvedList));
						} else {
							IPathEntry resolved = PathEntryUtil.cloneEntryAndExpand(projectPath, entry);
							resolvedEntries.add(resolved);
							resolveInfoList.add(new PathEntryResolveInfoElement(entry, resolved));
						}
					}
				} else {
					IPathEntry clone = PathEntryUtil.cloneEntryAndExpand(projectPath, entry);
					IPathEntry e = PathEntryUtil.getExpandedPathEntry(clone, cproject);
					if (e != null) {
						resolvedEntries.add(e);
					}
					resolveInfoList.add(new PathEntryResolveInfoElement(entry, e));
				}
			}
			resolvedEntries.trimToSize();

			if (generateMarkers) {
				IPathEntry[] finalEntries = resolvedEntries.toArray(NO_PATHENTRIES);
				ArrayList<ICModelStatus> problemList = new ArrayList<>();
				ICModelStatus status = validatePathEntry(cproject, finalEntries);
				if (!status.isOK()) {
					problemList.add(status);
				}
				for (IPathEntry finalEntry : finalEntries) {
					status = PathEntryUtil.validatePathEntry(cproject, finalEntry, true, false);
					if (!status.isOK()) {
						problemList.add(status);
					}
				}
				ICModelStatus[] problems = new ICModelStatus[problemList.size()];
				problemList.toArray(problems);
				IProject project = cproject.getProject();
				if (PathEntryUtil.hasPathEntryProblemMarkersChange(project, problems)) {
					addProblemMarkers(project, problems);
				}
			}

			// Check for duplication in the sources
			List<IPathEntry> dups = PathEntryUtil.checkForDuplication(resolvedEntries, IPathEntry.CDT_SOURCE);
			if (dups.size() > 0) {
				resolvedEntries.removeAll(dups);
			}

			// Check for duplication in the outputs
			dups = PathEntryUtil.checkForDuplication(resolvedEntries, IPathEntry.CDT_OUTPUT);
			if (dups.size() > 0) {
				resolvedEntries.removeAll(dups);
			}

			rInfo = new PathEntryResolveInfo(resolveInfoList);
			if (useCache) {
				synchronized (cacheComputationMarkers) {
					// Only if our marker is still here we are allowed to cache the result
					// Otherwise, the cache may have been cleared after we started our computation.
					if (cacheComputationMarkers.get(cproject) == marker) {
						cacheComputationMarkers.remove(cproject);
						resolvedMap.put(cproject, resolvedEntries);
						resolvedInfoMap.put(cproject, rInfo);
					}
				}
			}
		}
		return new Object[] { resolvedEntries, rInfo };
	}

	public void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor)
			throws CModelException {
		try {
			IPathEntry[] oldResolvedEntries = getCachedResolvedPathEntries(cproject);
			SetPathEntriesOperation op = new SetPathEntriesOperation(cproject, oldResolvedEntries, newEntries);
			op.runOperation(monitor);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
		IProject project = cproject.getProject();
		// Check if the Project is accessible.
		if (!CoreModel.hasCNature(project) && !CoreModel.hasCCNature(project)) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, cproject));
		}
		IPathEntry[] pathEntries;
		try {
			IPathEntryStore store = getPathEntryStore(project, true);
			pathEntries = store.getRawPathEntries();
		} catch (CoreException e) {
			throw new CModelException(e);
		}

		// Checks/hacks for backward compatibility ..
		// if no output is specified we return the project
		// if no source is specified we return the project
		boolean foundSource = false;
		boolean foundOutput = false;
		for (IPathEntry rawEntry : pathEntries) {
			switch (rawEntry.getEntryKind()) {
			case IPathEntry.CDT_SOURCE:
				foundSource = true;
				break;
			case IPathEntry.CDT_OUTPUT:
				foundOutput = true;
				break;
			}
		}

		if (!foundSource) {
			IPathEntry[] newEntries = new IPathEntry[pathEntries.length + 1];
			System.arraycopy(pathEntries, 0, newEntries, 0, pathEntries.length);
			newEntries[pathEntries.length] = CoreModel.newSourceEntry(cproject.getPath());
			pathEntries = newEntries;
		}
		if (!foundOutput) {
			IPathEntry[] newEntries = new IPathEntry[pathEntries.length + 1];
			System.arraycopy(pathEntries, 0, newEntries, 0, pathEntries.length);
			newEntries[pathEntries.length] = CoreModel.newOutputEntry(cproject.getPath());
			pathEntries = newEntries;
		}
		return pathEntries;
	}

	public void setPathEntryContainer(ICProject[] affectedProjects, IPathEntryContainer newContainer,
			IProgressMonitor monitor) throws CModelException {

		SetPathEntryContainerOperation op = new SetPathEntryContainerOperation(affectedProjects, newContainer);
		op.runOperation(monitor);
	}

	public void clearPathEntryContainer(ICProject[] affectedProjects, IPath containerPath, IProgressMonitor monitor)
			throws CModelException {

		SetPathEntryContainerOperation op = new SetPathEntryContainerOperation(affectedProjects, containerPath);
		op.runOperation(monitor);
	}

	public synchronized IPathEntryContainer[] getPathEntryContainers(ICProject cproject) {
		IPathEntryContainer[] pcs = NO_PATHENTRYCONTAINERS;
		Map<IPath, IPathEntryContainer> projectContainers = Containers.get(cproject);
		if (projectContainers != null) {
			Collection<IPathEntryContainer> collection = projectContainers.values();
			pcs = collection.toArray(NO_PATHENTRYCONTAINERS);
		}
		return pcs;
	}

	public IPathEntryContainer getPathEntryContainer(IContainerEntry entry, ICProject cproject) throws CModelException {
		return getPathEntryContainer(entry.getPath(), cproject);
	}

	public IPathEntryContainer getPathEntryContainer(final IPath containerPath, final ICProject project)
			throws CModelException {
		// Try the cache.
		IPathEntryContainer container = containerGet(project, containerPath, true);
		if (container instanceof PathEntryContainerLock) {
			boolean runInitializer = false;
			PathEntryContainerLock lock = (PathEntryContainerLock) container;
			synchronized (lock) {
				if (!lock.isContainerInitialize()) {
					runInitializer = true;
					lock.setContainerInitialize(runInitializer);
				} else if (!Thread.holdsLock(lock)) {
					// FIXME: Use Thread.holdsLock(lock) to break the cycle.
					// This seem to happend when the container(say the auto
					// discovery)
					// trigger a resource change, the CoreModel will try to get
					// the pathentries .. deadlock.

					// Wait for the inialization to finish.
					while (containerGet(project, containerPath, true) instanceof PathEntryContainerLock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							// e.printStackTrace();
						}
					}
				}
			}
			if (runInitializer) {
				// Remove the lock.
				final PathEntryContainerInitializer initializer = getPathEntryContainerInitializer(
						containerPath.segment(0));
				final boolean[] ok = { false };
				if (initializer != null) {
					// wrap initializer call with Safe runnable in case
					// initializer would be
					// causing some grief
					SafeRunner.run(new ISafeRunnable() {
						@Override
						public void handleException(Throwable exception) {
							IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
									"Exception occurred in container initializer: " + initializer, exception); //$NON-NLS-1$
							CCorePlugin.log(status);
						}

						@Override
						public void run() throws Exception {
							initializer.initialize(containerPath, project);
							ok[0] = true;
						}
					});
				}
				if (!ok[0]) {
					containerPut(project, containerPath, null); // flush and
																// notify
				}
			}
			// Retrieve new value
			container = containerGet(project, containerPath, false);
		}
		return container;
	}

	/**
	 * Helper method finding the container initializer registered for a given
	 * container ID or <code>null</code> if none was found while iterating over
	 * the contributions to extension point to the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to
	 * identify the registered container initializer.
	 * <p>
	 *
	 * @param containerID
	 *            - a containerID identifying a registered initializer
	 * @return PathEntryContainerInitializer - the registered container
	 *         initializer or <code>null</code> if none was found.
	 */
	public PathEntryContainerInitializer getPathEntryContainerInitializer(String containerID) {
		Plugin core = CCorePlugin.getDefault();
		if (core == null) {
			return null;
		}
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					String initializerID = configElement.getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(containerID)) {
						try {
							Object execExt = configElement.createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof PathEntryContainerInitializer) {
								return (PathEntryContainerInitializer) execExt;
							}
						} catch (CoreException e) {
							// Executable extension could not be created:
							CCorePlugin.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	synchronized IPathEntryContainer containerGet(ICProject cproject, IPath containerPath, boolean bCreateLock) {
		Map<IPath, IPathEntryContainer> projectContainers = Containers.get(cproject);
		if (projectContainers == null) {
			projectContainers = new HashMap<>();
			Containers.put(cproject, projectContainers);
		}
		IPathEntryContainer container = projectContainers.get(containerPath);
		// Initialize the first time with a lock
		if (bCreateLock && container == null) {
			container = new PathEntryContainerLock();
			projectContainers.put(containerPath, container);
		}
		return container;
	}

	synchronized void containerPut(ICProject cproject, IPath containerPath, IPathEntryContainer container) {
		Map<IPath, IPathEntryContainer> projectContainers = Containers.get(cproject);
		if (projectContainers == null) {
			projectContainers = new HashMap<>();
			Containers.put(cproject, projectContainers);
		}
		IPathEntryContainer oldContainer;
		if (container == null) {
			oldContainer = projectContainers.remove(containerPath);
		} else {
			oldContainer = projectContainers.put(containerPath, container);
		}
		if (oldContainer instanceof PathEntryContainerLock) {
			synchronized (oldContainer) {
				oldContainer.notifyAll();
			}
		}
	}

	synchronized void containerRemove(ICProject cproject) {
		Containers.remove(cproject);
	}

	public void pathEntryContainerUpdates(IPathEntryContainerExtension container, PathEntryContainerChanged[] events,
			IProgressMonitor monitor) {
		PathEntryContainerUpdatesOperation op = new PathEntryContainerUpdatesOperation(container, events);
		try {
			op.runOperation(monitor);
		} catch (CModelException e) {
			//
		}
	}

	public String[] projectPrerequisites(IPathEntry[] entries) throws CModelException {
		if (entries != null) {
			ArrayList<String> prerequisites = new ArrayList<>();
			for (IPathEntry entry : entries) {
				if (entry.getEntryKind() == IPathEntry.CDT_PROJECT) {
					IProjectEntry projectEntry = (IProjectEntry) entry;
					prerequisites.add(projectEntry.getPath().lastSegment());
				}
			}
			int size = prerequisites.size();
			if (size != 0) {
				String[] result = new String[size];
				prerequisites.toArray(result);
				return result;
			}
		}
		return NO_PREREQUISITES;
	}

	public void saveRawPathEntries(ICProject cproject, IPathEntry[] entries) throws CModelException {
		// Sanity
		if (entries == null) {
			entries = NO_PATHENTRIES;
		}

		ArrayList<IPathEntry> list = new ArrayList<>(entries.length);
		IPath projectPath = cproject.getPath();
		for (IPathEntry pathEntry : entries) {
			int kind = pathEntry.getEntryKind();

			// Translate the project prefix.
			IPath resourcePath = pathEntry.getPath();
			if (resourcePath == null) {
				resourcePath = Path.EMPTY;
			}

			// Do not do this for container, the path is the ID.
			if (kind != IPathEntry.CDT_CONTAINER) {
				// Translate to project relative from absolute (unless a device
				// path)
				if (resourcePath.isAbsolute()) {
					if (projectPath != null && projectPath.isPrefixOf(resourcePath)) {
						if (resourcePath.segment(0).equals(projectPath.segment(0))) {
							resourcePath = resourcePath.removeFirstSegments(1);
							resourcePath = resourcePath.makeRelative();
						} else {
							resourcePath = resourcePath.makeAbsolute();
						}
					}
				}
			}

			// Specifics to the entries
			IPathEntry entry;
			switch (kind) {
			case IPathEntry.CDT_INCLUDE: {
				IIncludeEntry include = (IIncludeEntry) pathEntry;
				IPath baseRef = include.getBaseReference();
				if (baseRef == null || baseRef.isEmpty()) {
					entry = CoreModel.newIncludeEntry(resourcePath, include.getBasePath(), include.getIncludePath(),
							include.isSystemInclude(), include.getExclusionPatterns(), include.isExported());
				} else {
					entry = CoreModel.newIncludeRefEntry(resourcePath, baseRef, include.getIncludePath());
				}
				break;
			}
			case IPathEntry.CDT_INCLUDE_FILE: {
				IIncludeFileEntry includeFile = (IIncludeFileEntry) pathEntry;
				entry = CoreModel.newIncludeFileEntry(resourcePath, includeFile.getBasePath(),
						includeFile.getBaseReference(), includeFile.getIncludeFilePath(),
						includeFile.getExclusionPatterns(), includeFile.isExported());
				break;
			}
			case IPathEntry.CDT_LIBRARY: {
				ILibraryEntry library = (ILibraryEntry) pathEntry;
				IPath sourcePath = library.getSourceAttachmentPath();
				if (sourcePath != null) {
					// Translate to project relative from absolute
					if (projectPath != null && projectPath.isPrefixOf(sourcePath)) {
						if (sourcePath.segment(0).equals(projectPath.segment(0))) {
							sourcePath = sourcePath.removeFirstSegments(1);
							sourcePath = sourcePath.makeRelative();
						}
					}
				}
				IPath baseRef = library.getBaseReference();
				if (baseRef == null || baseRef.isEmpty()) {
					entry = CoreModel.newLibraryEntry(resourcePath, library.getBasePath(), library.getLibraryPath(),
							sourcePath, library.getSourceAttachmentRootPath(),
							library.getSourceAttachmentPrefixMapping(), library.isExported());
				} else {
					entry = CoreModel.newLibraryRefEntry(resourcePath, baseRef, library.getLibraryPath());
				}
				break;
			}
			case IPathEntry.CDT_MACRO: {
				IMacroEntry macro = (IMacroEntry) pathEntry;
				IPath baseRef = macro.getBaseReference();
				if (baseRef == null || baseRef.isEmpty()) {
					entry = CoreModel.newMacroEntry(resourcePath, macro.getMacroName(), macro.getMacroValue(),
							macro.getExclusionPatterns(), macro.isExported());
				} else {
					entry = CoreModel.newMacroRefEntry(resourcePath, baseRef, macro.getMacroName());
				}
				break;
			}
			case IPathEntry.CDT_MACRO_FILE: {
				IMacroFileEntry macro = (IMacroFileEntry) pathEntry;
				entry = CoreModel.newMacroFileEntry(resourcePath, macro.getBasePath(), macro.getBaseReference(),
						macro.getMacroFilePath(), macro.getExclusionPatterns(), macro.isExported());
				break;
			}
			case IPathEntry.CDT_OUTPUT: {
				IOutputEntry out = (IOutputEntry) pathEntry;
				entry = CoreModel.newOutputEntry(resourcePath, out.getExclusionPatterns());
				break;
			}
			case IPathEntry.CDT_PROJECT: {
				IProjectEntry projEntry = (IProjectEntry) pathEntry;
				entry = CoreModel.newProjectEntry(projEntry.getPath(), projEntry.isExported());
				break;
			}
			case IPathEntry.CDT_SOURCE: {
				ISourceEntry source = (ISourceEntry) pathEntry;
				entry = CoreModel.newSourceEntry(resourcePath, source.getExclusionPatterns());
				break;
			}
			case IPathEntry.CDT_CONTAINER:
				entry = CoreModel.newContainerEntry(pathEntry.getPath(), pathEntry.isExported());
				break;
			default:
				entry = pathEntry;
			}
			list.add(entry);
		}
		try {
			IPathEntry[] newRawEntries = new IPathEntry[list.size()];
			list.toArray(newRawEntries);
			IProject project = cproject.getProject();
			IPathEntryStore store = getPathEntryStore(project, true);
			setResolveInfoValidState(cproject, false);
			store.setRawPathEntries(newRawEntries);
			setResolveInfoValidState(cproject, true);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * Collects path entry errors for each project and generates error markers
	 * for these errors
	 *
	 * @param project
	 *            - Project with path entry errors
	 * @param problems
	 *            - The path entry errors associated with the project
	 */
	public void addProblemMarkers(final IProject project, final ICModelStatus[] problems) {
		PathEntryProblem problem = new PathEntryProblem(project, problems);
		// Queue up the problems to be logged
		markerProblems.add(problem);
		// Generate the error markers
		markerTask.schedule();
	}

	private class GenerateMarkersJob extends WorkspaceJob {
		public GenerateMarkersJob(String name) {
			super(name);
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			while (markerProblems.peek() != null && !monitor.isCanceled()) {
				PathEntryProblem problem = markerProblems.poll();
				IProject project = problem.project;
				ICModelStatus[] problems = problem.problems;
				PathEntryUtil.flushPathEntryProblemMarkers(project);
				for (ICModelStatus problem2 : problems) {
					PathEntryUtil.createPathEntryProblemMarker(project, problem2);
				}
			}
			return Status.OK_STATUS;
		}
	}

	private boolean needDelta(ICProject cproject) {
		try {
			PathEntryStoreProxy store = (PathEntryStoreProxy) getPathEntryStore(cproject.getProject(), false);
			return store == null || !(store.getStore() instanceof ConfigBasedPathEntryStore);
		} catch (CoreException e) {
		}
		return false;
	}

	public ICElementDelta[] generatePathEntryDeltas(ICProject cproject, IPathEntry[] oldEntries,
			IPathEntry[] newEntries) {
		if (!needDelta(cproject))
			return new ICElementDelta[0];

		ArrayList<ICElementDelta> list = new ArrayList<>();

		// If nothing was known before do not generate any deltas.
		if (oldEntries == null) {
			return new ICElementDelta[0];
		}
		// Sanity checks
		if (newEntries == null) {
			newEntries = NO_PATHENTRIES;
		}

		// Check the removed entries.
		for (IPathEntry oldEntry : oldEntries) {
			boolean found = false;
			for (IPathEntry newEntrie : newEntries) {
				if (oldEntry.equals(newEntrie)) {
					found = true;
					break;
				}
			}
			// Was it deleted.
			if (!found) {
				ICElementDelta delta = makePathEntryDelta(cproject, oldEntry, true);
				if (delta != null) {
					list.add(delta);
				}
			}
		}

		// Check the new entries.
		for (IPathEntry newEntry : newEntries) {
			boolean found = false;
			for (IPathEntry oldEntry : oldEntries) {
				if (newEntry.equals(oldEntry)) {
					found = true;
					break;
				}
			}
			// Is it new?
			if (!found) {
				ICElementDelta delta = makePathEntryDelta(cproject, newEntry, false);
				if (delta != null) {
					list.add(delta);
				}
			}
		}

		// Check for reorder
		if (list.size() == 0 && oldEntries.length == newEntries.length) {
			for (int i = 0; i < newEntries.length; i++) {
				if (!newEntries[i].equals(oldEntries[i])) {
					ICElementDelta delta = makePathEntryDelta(cproject, null, false);
					if (delta != null) {
						list.add(delta);
					}
				}
			}
		}
		// They may have remove some duplications, catch here .. consider it as
		// reordering.
		if (list.size() == 0 && oldEntries.length != newEntries.length) {
			ICElementDelta delta = makePathEntryDelta(cproject, null, true);
			if (delta != null) {
				list.add(delta);
			}
		}
		ICElementDelta[] deltas = new ICElementDelta[list.size()];
		list.toArray(deltas);
		return deltas;
	}

	/**
	 * Returns a delta, with the specified change flag.
	 */
	protected ICElementDelta makePathEntryDelta(ICProject cproject, IPathEntry entry, boolean removed) {
		ICElement celement = null;
		int flag = ICElementDelta.F_PATHENTRY_REORDER;
		if (entry == null) {
			celement = cproject;
			flag = ICElementDelta.F_PATHENTRY_REORDER;
		} else {
			int kind = entry.getEntryKind();
			switch (kind) {
			case IPathEntry.CDT_SOURCE: {
				ISourceEntry source = (ISourceEntry) entry;
				IPath path = source.getPath();
				celement = CoreModel.getDefault().create(path);
				flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_SOURCE : ICElementDelta.F_ADDED_PATHENTRY_SOURCE;
				break;
			}
			case IPathEntry.CDT_LIBRARY: {
				celement = cproject;
				flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY
						: ICElementDelta.F_ADDED_PATHENTRY_LIBRARY;
				break;
			}
			case IPathEntry.CDT_PROJECT: {
				// IProjectEntry pentry = (IProjectEntry) entry;
				celement = cproject;
				flag = ICElementDelta.F_CHANGED_PATHENTRY_PROJECT;
				break;
			}
			case IPathEntry.CDT_INCLUDE: {
				IIncludeEntry include = (IIncludeEntry) entry;
				IPath path = include.getPath();
				celement = CoreModel.getDefault().create(path);
				flag = ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				break;
			}
			case IPathEntry.CDT_INCLUDE_FILE: {
				IIncludeFileEntry includeFile = (IIncludeFileEntry) entry;
				IPath path = includeFile.getPath();
				celement = CoreModel.getDefault().create(path);
				flag = ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				break;
			}
			case IPathEntry.CDT_MACRO: {
				IMacroEntry macro = (IMacroEntry) entry;
				IPath path = macro.getPath();
				celement = CoreModel.getDefault().create(path);
				flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				break;
			}
			case IPathEntry.CDT_MACRO_FILE: {
				IMacroFileEntry macro = (IMacroFileEntry) entry;
				IPath path = macro.getPath();
				celement = CoreModel.getDefault().create(path);
				flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				break;
			}
			case IPathEntry.CDT_CONTAINER: {
				// IContainerEntry container = (IContainerEntry) entry;
				// celement = cproject;
				// SHOULD NOT BE HERE Container are resolved.
				break;
			}
			}
		}
		if (celement == null) {
			celement = cproject;
		}
		CElementDelta delta = new CElementDelta(cproject.getCModel());
		delta.changed(celement, flag);
		return delta;
	}

	static String[] getRegisteredContainerIDs() {
		Plugin core = CCorePlugin.getDefault();
		if (core == null) {
			return null;
		}
		ArrayList<String> containerIDList = new ArrayList<>(5);
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					String idAttribute = configElement.getAttribute("id"); //$NON-NLS-1$
					if (idAttribute != null)
						containerIDList.add(idAttribute);
				}
			}
		}
		String[] containerIDs = new String[containerIDList.size()];
		containerIDList.toArray(containerIDs);
		return containerIDs;
	}

	public void setPathEntryStore(IProject project, IPathEntryStore newStore) {
		IPathEntryStore oldStore = null;
		synchronized (storeMap) {
			oldStore = storeMap.remove(project);
			if (newStore != null) {
				storeMap.put(project, newStore);
			}
		}
		if (oldStore != null) {
			// Remove ourselves before closing
			oldStore.removePathEntryStoreListener(this);
			oldStore.close();
		}
	}

	public IPathEntryStore getPathEntryStore(IProject project, boolean create) throws CoreException {
		synchronized (storeMap) {
			IPathEntryStore store = storeMap.get(project);
			if (store == null) {
				if (create == true) {
					store = createPathEntryStore(project);
					storeMap.put(project, store);
					store.addPathEntryStoreListener(this);
				}
			} else if (store instanceof AbstractCExtensionProxy) {
				((AbstractCExtensionProxy) store).updateProject(project);
			}
			return store;
		}
	}

	public IPathEntryStore createPathEntryStore(IProject project) throws CoreException {
		return new PathEntryStoreProxy(project);
		// IPathEntryStore store = null;
		// if (project != null) {
		// try {
		// ICDescriptor cdesc =
		// CCorePlugin.getDefault().getCProjectDescription(project, false);
		// if (cdesc != null) {
		// ICExtensionReference[] cextensions =
		// cdesc.get(PATHENTRY_STORE_UNIQ_ID, true);
		// if (cextensions.length > 0) {
		// for (int i = 0; i < cextensions.length; i++) {
		// try {
		// store = (IPathEntryStore)cextensions[i].createExtension();
		// break;
		// } catch (ClassCastException e) {
		// //
		// CCorePlugin.log(e);
		// }
		// }
		// }
		// }
		// } catch (CoreException e) {
		// // ignore since we fall back to a default....
		// }
		// }
		// if (store == null) {
		// store = createDefaultStore(project);
		// }
		// return store;
	}

	// private IPathEntryStore createDefaultStore(IProject project){
	// if
	// (CProjectDescriptionManager.getInstance().isNewStyleIndexCfg(project)){
	// return new ConfigBasedPathEntryStore(project);
	// }
	// return new DefaultPathEntryStore(project);
	// }

	@Override
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		IProject project = event.getProject();

		// sanity
		if (project == null) {
			return;
		}

		CModelManager manager = CModelManager.getDefault();
		final ICProject cproject = manager.create(project);
		if (event.hasClosed()) {
			setPathEntryStore(project, null);
			containerRemove(cproject);
		}
		if (project.isAccessible()) {
			try {
				CModelOperation op = new PathEntryStoreChangedOperation(cproject);
				op.runOperation(null);
			} catch (CModelException e) {
				CCorePlugin.log(e);
			}
		} else {
			resolvedMap.remove(cproject);
			resolvedInfoMap.remove(cproject);
			containerRemove(cproject);
		}
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		try {
			if (processDelta(event.getDelta()) == true) {
				ICProject[] cProjects = resolvedMap.keySet().toArray(new ICProject[0]);
				for (ICProject project2 : cProjects) {
					IPathEntry[] entries = getCachedResolvedPathEntries(project2);
					if (entries != null) {
						IProject project = project2.getProject();
						try {
							IMarker[] markers = project.findMarkers(ICModelMarker.PATHENTRY_PROBLEM_MARKER, false,
									IResource.DEPTH_ZERO);
							if (markers != null && markers.length > 0) {
								ArrayList<ICModelStatus> problemList = new ArrayList<>();
								for (IPathEntry entry : entries) {
									ICModelStatus status = PathEntryUtil.validatePathEntry(project2, entry, true,
											false);
									if (!status.isOK()) {
										problemList.add(status);
									}
								}
								ICModelStatus[] problems = new ICModelStatus[problemList.size()];
								problemList.toArray(problems);
								if (PathEntryUtil.hasPathEntryProblemMarkersChange(project, problems)) {
									addProblemMarkers(project, problems);
								}
							}
						} catch (CoreException e) {
							// Ignore the exception.
						}
					}
				}
			}
		} catch (CModelException e) {
		}
	}

	protected boolean processDelta(ICElementDelta delta) throws CModelException {
		int kind = delta.getKind();
		ICElement element = delta.getElement();
		int type = element.getElementType();

		// Handle open, closing and removing of projects
		if (type == ICElement.C_PROJECT) {
			ICProject cproject = (ICProject) element;
			if ((kind == ICElementDelta.REMOVED || kind == ICElementDelta.ADDED)) {
				if (kind == ICElementDelta.REMOVED) {
					IProject project = cproject.getProject();
					IPathEntryStore store = null;
					try {
						store = getPathEntryStore(project, false);
						if (store != null) {
							store.close();
						}
					} catch (CoreException e) {
						throw new CModelException(e);
					} finally {
						if (store == null) {
							resolvedMap.remove(cproject);
							resolvedInfoMap.remove(cproject);
							containerRemove(cproject);
						}
					}
				}
				return true;
			}
			// Project change, traverse children.
		}
		if (element instanceof IWorkingCopy) {
			return false;
		}
		if (kind == ICElementDelta.ADDED || kind == ICElementDelta.REMOVED) {
			return true; // add/remove we validate all paths
		}
		if (type == ICElement.C_MODEL || type == ICElement.C_CCONTAINER || type == ICElement.C_PROJECT) {
			ICElementDelta[] affectedChildren = delta.getAffectedChildren();
			boolean result = false;
			for (ICElementDelta element2 : affectedChildren) {
				if (processDelta(element2)) {
					result = true;
				}
			}
			return result;
		}
		return false;
	}

	public ICModelStatus validatePathEntry(ICProject cProject, IPathEntry[] entries) {
		return PathEntryUtil.validatePathEntry(cProject, entries);
	}

	public ICModelStatus validatePathEntry(ICProject cProject, IPathEntry entry, boolean checkSourceAttachment,
			boolean recurseInContainers) {
		return PathEntryUtil.validatePathEntry(cProject, entry, checkSourceAttachment, recurseInContainers);
	}
}
