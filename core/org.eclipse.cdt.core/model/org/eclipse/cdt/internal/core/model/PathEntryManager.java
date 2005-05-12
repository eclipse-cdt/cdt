/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
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
import org.eclipse.cdt.core.model.IOpenable;
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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author alain
 *  
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
	static final String[] NO_PREREQUISITES = new String[0];
	/**
	 * pathentry containers pool accessing the Container is done synch with the
	 * class
	 */
	private static HashMap Containers = new HashMap(5);

	static final IPathEntry[] NO_PATHENTRIES = new IPathEntry[0];

	static final IIncludeEntry[] NO_INCLUDE_ENTRIES = new IIncludeEntry[0];

	static final IIncludeFileEntry[] NO_INCLUDE_FILE_ENTRIES = new IIncludeFileEntry[0];

	static final IMacroEntry[] NO_MACRO_ENTRIES = new IMacroEntry[0];

	static final IMacroFileEntry[] NO_MACRO_FILE_ENTRIES = new IMacroFileEntry[0];

	static final IPathEntryContainer[] NO_PATHENTRYCONTAINERS = new IPathEntryContainer[0];

	// Synchronized the access of the cache entries.
	protected Map resolvedMap = new Hashtable();

	// Accessing the map is synch with the class
	private Map storeMap = new HashMap();

	private static PathEntryManager pathEntryManager;
	private PathEntryManager() {
	}

	private class PathEntryContainerLock implements IPathEntryContainer {

		boolean runInitializer;

		public boolean isContainerInitialize() {
			return runInitializer;
		}

		public void setContainerInitialize(boolean init) {
			runInitializer = init;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getPathEntries()
		 */
		public IPathEntry[] getPathEntries() {
			return NO_PATHENTRIES;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getDescription()
		 */
		public String getDescription() {
			return new String("Lock container"); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getPath()
		 */
		public IPath getPath() {
			return Path.EMPTY;
		}
	}

	/**
	 * Return the singleton.
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
			return getIncludeFileEntries((ITranslationUnit)celement);
		}
		return NO_INCLUDE_FILE_ENTRIES;
	}

	public IIncludeFileEntry[] getIncludeFileEntries(ITranslationUnit cunit) throws CModelException {
		List list = getPathEntries(cunit, IPathEntry.CDT_INCLUDE_FILE);
		IIncludeFileEntry[] incFiles = (IIncludeFileEntry[]) list.toArray(new IIncludeFileEntry[list.size()]);
		return incFiles;
	}

	public IIncludeEntry[] getIncludeEntries(IPath resPath) throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getIncludeEntries((ITranslationUnit)celement);
		}
		return NO_INCLUDE_ENTRIES;
	}

	public IIncludeEntry[] getIncludeEntries(ITranslationUnit cunit) throws CModelException {
		List list = getPathEntries(cunit, IPathEntry.CDT_INCLUDE);
		IIncludeEntry[] includes = (IIncludeEntry[]) list.toArray(new IIncludeEntry[list.size()]);
		return includes;
	}

	public IMacroEntry[] getMacroEntries(IPath resPath)  throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getMacroEntries((ITranslationUnit)celement);
		}
		return NO_MACRO_ENTRIES;
	}

	private IMacroEntry[] getMacroEntries(ITranslationUnit cunit) throws CModelException  {
		ArrayList macroList = new ArrayList();
		ICProject cproject = cunit.getCProject();
		IPath resPath = cunit.getPath();
		// Do this first so the containers get inialized.
		ArrayList resolvedListEntries = getResolvedPathEntries(cproject, false);
		for (int i = 0; i < resolvedListEntries.size(); ++i) {
			IPathEntry entry = (IPathEntry)resolvedListEntries.get(i);
			if (entry.getEntryKind() == IPathEntry.CDT_MACRO) {
				macroList.add(entry);
			}
		}
		IPathEntryContainer[] containers = getPathEntryContainers(cproject);
		for (int i = 0; i < containers.length; ++i) {
			if (containers[i] instanceof IPathEntryContainerExtension) {
				IPathEntryContainerExtension extension = (IPathEntryContainerExtension)containers[i];
                IPathEntry[] incs = (IPathEntry[])extension.getPathEntries(resPath, IPathEntry.CDT_MACRO);
				macroList.addAll(Arrays.asList(incs));
			}
		}

		IMacroEntry[] macros = (IMacroEntry[]) macroList.toArray(new IMacroEntry[macroList.size()]);
		macroList.clear();
		
		// For the macros the closest symbol will override 
		// /projec/src/file.c --> NDEBUG=1
		// /project/src       --> NDEBUG=0
		//
		// We will use NDEBUG=1 only
		int count = resPath.segmentCount();
		Map symbolMap = new HashMap();
		for (int i = 0; i < count; i++) {
			IPath newPath = resPath.removeLastSegments(i);
			for (int j = 0; j < macros.length; j++) {
				IPath otherPath = macros[j].getPath();
				if (newPath.equals(otherPath)) {
					String key = macros[j].getMacroName();
					if (!symbolMap.containsKey(key)) {
						symbolMap.put(key, macros[j]);
					}
				}
			}
		}

		// Add the Project contributions last.
		for (int i = 0; i < resolvedListEntries.size(); i++) {
			IPathEntry entry = (IPathEntry)resolvedListEntries.get(i);
			if (entry != null && entry.getEntryKind() == IPathEntry.CDT_PROJECT) {
				IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(entry.getPath());
				if (res != null && res.getType() == IResource.PROJECT) {
					ICProject refCProject = CoreModel.getDefault().create((IProject)res);
					if (refCProject != null) {
						IPathEntry[] projEntries = refCProject.getResolvedPathEntries();
						for (int j = 0; j < projEntries.length; j++) {
							IPathEntry projEntry = projEntries[j];
							if (projEntry.isExported()) {
								if (projEntry.getEntryKind() == IPathEntry.CDT_MACRO) {
									IMacroEntry macro = (IMacroEntry)entry;
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

		return (IMacroEntry[])symbolMap.values().toArray(NO_MACRO_ENTRIES);

	}

	public IMacroFileEntry[] getMacroFileEntries(IPath resPath)  throws CModelException {
		ICElement celement = CoreModel.getDefault().create(resPath);
		if (celement instanceof ITranslationUnit) {
			return getMacroFileEntries((ITranslationUnit)celement);
		}
		return NO_MACRO_FILE_ENTRIES;
	}

	public IMacroFileEntry[] getMacroFileEntries(ITranslationUnit cunit) throws CModelException  {
		List list = getPathEntries(cunit, IPathEntry.CDT_MACRO_FILE);
		IMacroFileEntry[] macFiles = (IMacroFileEntry[]) list.toArray(new IMacroFileEntry[list.size()]);
		return macFiles;
	}

	private List getPathEntries(ITranslationUnit cunit, int type) throws CModelException {
		ArrayList entryList = new ArrayList();
		ICProject cproject = cunit.getCProject();
		IPath resPath = cunit.getPath();
		// Do this first so the containers get inialized.
		ArrayList resolvedListEntries = getResolvedPathEntries(cproject, false);
		for (int i = 0; i < resolvedListEntries.size(); ++i) {
			IPathEntry entry = (IPathEntry)resolvedListEntries.get(i);
			if ((entry.getEntryKind() & type) != 0) {
				entryList.add(entry);
			}
		}
		IPathEntryContainer[] containers = getPathEntryContainers(cproject);
		for (int i = 0; i < containers.length; ++i) {
			if (containers[i] instanceof IPathEntryContainerExtension) {
				IPathEntryContainerExtension extension = (IPathEntryContainerExtension)containers[i];
				IPathEntry[] incs = extension.getPathEntries(resPath, type);
				entryList.addAll(Arrays.asList(incs));
			}
		}

		IPathEntry[] entries = (IPathEntry[]) entryList.toArray(new IPathEntry[entryList.size()]);
		// Clear the list since we are reusing it.
		entryList.clear();

		// We need to reorder the include/macros:
		// includes with the closest match to the resource will come first
		// /project/src/file.c  --> /usr/local/include
		// /project             --> /usr/include
		// 
		//  /usr/local/include must come first.
		//
		int count = resPath.segmentCount();
		for (int i = 0; i < count; i++) {
			IPath newPath = resPath.removeLastSegments(i);
			for (int j = 0; j < entries.length; j++) {
				IPath otherPath = entries[j].getPath();
				if (newPath.equals(otherPath)) {
					entryList.add(entries[j]);
				}
			}
		}

		// Since the include that comes from a project contribution are not
		// tied to a resource they are added last.
		for (int i = 0; i < resolvedListEntries.size(); i++) {
			IPathEntry entry = (IPathEntry)resolvedListEntries.get(i);
			if (entry != null && entry.getEntryKind() == IPathEntry.CDT_PROJECT) {
				IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(entry.getPath());
				if (res != null && res.getType() == IResource.PROJECT) {
					ICProject refCProject = CoreModel.getDefault().create((IProject)res);
					if (refCProject != null) {
						IPathEntry[] projEntries = refCProject.getResolvedPathEntries();
						for (int j = 0; j < projEntries.length; j++) {
							IPathEntry projEntry = projEntries[j];
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
	 * @param cproject
	 * @return
	 */
	protected IPathEntry[] getCachedResolvedPathEntries(ICProject cproject) {
		ArrayList resolvedListEntries = (ArrayList)resolvedMap.get(cproject);
		if (resolvedListEntries != null) {
			try {
				return getCachedResolvedPathEntries(resolvedListEntries, cproject);
			} catch (CModelException e) {
				//
			}
		}
		return null;
	}

	protected IPathEntry[] removeCachedResolvedPathEntries(ICProject cproject) {
		ArrayList resolvedListEntries = (ArrayList)resolvedMap.remove(cproject);
		if (resolvedListEntries != null) {
			try {
				return getCachedResolvedPathEntries(resolvedListEntries, cproject);
			} catch (CModelException e) {
				//
			}
		}
		return null;
	}

	private IPathEntry[] getCachedResolvedPathEntries(ArrayList resolvedListEntries, ICProject cproject) throws CModelException {
		IPathEntry[] entries = (IPathEntry[])resolvedListEntries.toArray(NO_PATHENTRIES);
		boolean hasContainerExtension = false;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == IPathEntry.CDT_CONTAINER) {
				IContainerEntry centry = (IContainerEntry)entries[i];
				IPathEntryContainer container = getPathEntryContainer(centry, cproject);
				if (container instanceof IPathEntryContainerExtension) {
					hasContainerExtension = true;
					break;
				}
			}
		}
		if (hasContainerExtension) {
			IPath projectPath = cproject.getPath();
			ArrayList listEntries = new ArrayList(entries.length);
			for (int i = 0; i < entries.length; ++i) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry centry = (IContainerEntry)entries[i];
					IPathEntryContainer container = getPathEntryContainer(centry, cproject);
					if (container != null) {
						IPathEntry[] containerEntries = container.getPathEntries();
						if (containerEntries != null) {
							for (int j = 0; j < containerEntries.length; j++) {
								IPathEntry newEntry = PathEntryUtil.cloneEntryAndExpand(projectPath, containerEntries[j]);
								listEntries.add(newEntry);
							}
						}
					}
				} else {
					listEntries.add(entries[i]);
				}
			}
			entries = (IPathEntry[])listEntries.toArray(NO_PATHENTRIES);
		}
		return entries;
	}

	public IPathEntry[] getResolvedPathEntries(ICProject cproject) throws CModelException {
		boolean treeLock = cproject.getProject().getWorkspace().isTreeLocked();
		ArrayList resolvedListEntries = getResolvedPathEntries(cproject, !treeLock);
		return getCachedResolvedPathEntries(resolvedListEntries, cproject);
	}

	/**
	 * This method will not expand container extending IPathEntryContainerExtension
	 * 
	 * @param cproject
	 * @param generateMarkers
	 * @return
	 * @throws CModelException
	 */
	private ArrayList getResolvedPathEntries(ICProject cproject, boolean generateMarkers) throws CModelException {
		ArrayList resolvedEntries = (ArrayList)resolvedMap.get(cproject);
		if (resolvedEntries == null) {
			IPath projectPath = cproject.getPath();
			IPathEntry[] rawEntries = getRawPathEntries(cproject);
			resolvedEntries = new ArrayList();
			for (int i = 0; i < rawEntries.length; i++) {
				IPathEntry entry = rawEntries[i];
				// Expand the containers.
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry centry = (IContainerEntry)entry;
					IPathEntryContainer container = getPathEntryContainer(centry, cproject);
					if (container != null) {
						// For backward compatibility we need to expand and cache container that
						// are not IPathEntryContainerExtension.
						if (!(container instanceof IPathEntryContainerExtension)) {
							IPathEntry[] containerEntries = container.getPathEntries();
							if (containerEntries != null) {
								for (int j = 0; j < containerEntries.length; j++) {
									IPathEntry newEntry = PathEntryUtil.cloneEntryAndExpand(projectPath, containerEntries[j]);
									resolvedEntries.add(newEntry);
								}
							}
						} else {
							resolvedEntries.add(PathEntryUtil.cloneEntryAndExpand(projectPath, entry));
						}
					}
				} else {
					IPathEntry clone = PathEntryUtil.cloneEntryAndExpand(projectPath, entry);
					IPathEntry e = PathEntryUtil.getExpandedPathEntry(clone, cproject);
					if (e != null) {
						resolvedEntries.add(e);
					}
				}
			}
			resolvedEntries.trimToSize();

			if (generateMarkers) {
				IPathEntry[] finalEntries = (IPathEntry[])resolvedEntries.toArray(NO_PATHENTRIES);
				ArrayList problemList = new ArrayList();
				ICModelStatus status = validatePathEntry(cproject, finalEntries);
				if (!status.isOK()) {
					problemList.add(status);
				}
				for (int j = 0; j < finalEntries.length; j++) {
					status = PathEntryUtil.validatePathEntry(cproject, finalEntries[j], true, false);
					if (!status.isOK()) {
						problemList.add(status);
					}
				}
				ICModelStatus[] problems = new ICModelStatus[problemList.size()];
				problemList.toArray(problems);
				IProject project = cproject.getProject();
				if (PathEntryUtil.hasPathEntryProblemMarkersChange(project, problems)) {
					generateMarkers(project, problems);
				}
			}

			// Check for duplication in the sources
			List dups = PathEntryUtil.checkForDuplication(resolvedEntries, IPathEntry.CDT_SOURCE);
			if (dups.size() > 0) {
				resolvedEntries.removeAll(dups);
			}

			// Check for duplication in the outputs
			dups = PathEntryUtil.checkForDuplication(resolvedEntries, IPathEntry.CDT_OUTPUT);
			if (dups.size() > 0) {
				resolvedEntries.removeAll(dups);
			}

			resolvedMap.put(cproject, resolvedEntries);
		}
		return resolvedEntries;
	}

	public void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		try {
			IPathEntry[] oldResolvedEntries = getCachedResolvedPathEntries(cproject);
			SetPathEntriesOperation op = new SetPathEntriesOperation(cproject, oldResolvedEntries, newEntries);
			CModelManager.getDefault().runOperation(op, monitor);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
		IProject project = cproject.getProject();
		// Check if the Project is accesible.
		if (! (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project))) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
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
		for (int i = 0; i < pathEntries.length; i++) {
			IPathEntry rawEntry = pathEntries[i];
			if (rawEntry.getEntryKind() == IPathEntry.CDT_SOURCE) {
				foundSource = true;
			}
			if (rawEntry.getEntryKind() == IPathEntry.CDT_OUTPUT) {
				foundOutput = true;
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

	public void setPathEntryContainer(ICProject[] affectedProjects, IPathEntryContainer newContainer, IProgressMonitor monitor)
			throws CModelException {
		if (monitor != null && monitor.isCanceled()) {
			return;
		}
		IPath containerPath = (newContainer == null) ? new Path("") : newContainer.getPath(); //$NON-NLS-1$
		final int projectLength = affectedProjects.length;
		final ICProject[] modifiedProjects = new ICProject[projectLength];
		System.arraycopy(affectedProjects, 0, modifiedProjects, 0, projectLength);
		final IPathEntry[][] oldResolvedEntries = new IPathEntry[projectLength][];
		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			ICProject affectedProject = affectedProjects[i];
			boolean found = false;
			IPathEntry[] rawPath = getRawPathEntries(affectedProject);
			for (int j = 0, cpLength = rawPath.length; j < cpLength; j++) {
				IPathEntry entry = rawPath[j];
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry cont = (IContainerEntry)entry;
					if (cont.getPath().equals(containerPath)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				// filter out this project - does not reference the container
				// path
				modifiedProjects[i] = null;
				// Still add it to the cache
				containerPut(affectedProject, containerPath, newContainer);
				continue;
			}
			IPathEntryContainer oldContainer = containerGet(affectedProject, containerPath, true);
			if (oldContainer != null && newContainer != null && oldContainer.equals(newContainer)) {
				modifiedProjects[i] = null; // filter out this project -
				// container did not change
				continue;
			}
			remaining++;
			oldResolvedEntries[i] = removeCachedResolvedPathEntries(affectedProject);
			containerPut(affectedProject, containerPath, newContainer);
		}

		// Nothing change.
		if (remaining == 0) {
			return;
		}

		// trigger model refresh
		try {
			//final boolean canChangeResources =
			// !ResourcesPlugin.getWorkspace().isTreeLocked();
			CoreModel.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor progressMonitor) throws CoreException {

					boolean shouldFire = false;
					CModelManager mgr = CModelManager.getDefault();
					for (int i = 0; i < projectLength; i++) {
						if (progressMonitor != null && progressMonitor.isCanceled()) {
							return;
						}
						ICProject affectedProject = modifiedProjects[i];
						if (affectedProject == null) {
							continue; // was filtered out
						}
						// Only fire deltas if we had previous cache
						if (oldResolvedEntries[i] != null) {
							IPathEntry[] newEntries = getResolvedPathEntries(affectedProject);
							ICElementDelta[] deltas = generatePathEntryDeltas(affectedProject, oldResolvedEntries[i], newEntries);
							if (deltas.length > 0) {
								affectedProject.close();
								shouldFire = true;
								for (int j = 0; j < deltas.length; j++) {
									mgr.registerCModelDelta(deltas[j]);
								}
							}
						}
					}
					if (shouldFire) {
						mgr.fire(ElementChangedEvent.POST_CHANGE);
					}
				}
			}, monitor);
		} catch (CoreException e) {
			//
		}
	}

	public synchronized IPathEntryContainer[] getPathEntryContainers(ICProject cproject) {
		IPathEntryContainer[] pcs = NO_PATHENTRYCONTAINERS;
		Map projectContainers = (Map)Containers.get(cproject);
		if (projectContainers != null) {
			Collection collection = projectContainers.values();
			pcs = (IPathEntryContainer[]) collection.toArray(NO_PATHENTRYCONTAINERS);
		}
		return pcs;
	}

	public IPathEntryContainer getPathEntryContainer(IContainerEntry entry, ICProject cproject) throws CModelException {
		return getPathEntryContainer(entry.getPath(), cproject);
	}

	public IPathEntryContainer getPathEntryContainer(final IPath containerPath, final ICProject project) throws CModelException {
		// Try the cache.
		IPathEntryContainer container = containerGet(project, containerPath, true);
		if (container instanceof PathEntryContainerLock) {
			boolean runInitializer = false;
			PathEntryContainerLock lock = (PathEntryContainerLock)container;
			synchronized (lock) {
				if (!lock.isContainerInitialize()) {
					runInitializer = true;
					lock.setContainerInitialize(runInitializer);
				} else if (! Thread.holdsLock(lock)){
					// FIXME: Use Thread.holdsLock(lock) to break the cycle.
					// This seem to happend when the container(say the auto discovery)
					// trigger a resource change, the CoreModel will try to get the pathentries .. deadlock.

					// Wait for the inialization to finish.
					while (containerGet(project, containerPath, true) instanceof PathEntryContainerLock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							//e.printStackTrace();
						}
					}
				}
			}
			if (runInitializer) {
				// remove the lock.
				final PathEntryContainerInitializer initializer = getPathEntryContainerInitializer(containerPath.segment(0));
				final boolean[] ok = {false};
				if (initializer != null) {
					// wrap initializer call with Safe runnable in case
					// initializer would be
					// causing some grief
					Platform.run(new ISafeRunnable() {

						public void handleException(Throwable exception) {
							IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR,
									"Exception occurred in container initializer: " + initializer, exception); //$NON-NLS-1$
							CCorePlugin.log(status);
						}

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
			// retrieve new value
			container = containerGet(project, containerPath, false);
		}
		return container;
	}

	/**
	 * Helper method finding the container initializer registered for a given
	 * container ID or <code>null</code> if none was found while iterating
	 * over the contributions to extension point to the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to
	 * identify the registered container initializer.
	 * <p>
	 * 
	 * @param containerID -
	 *            a containerID identifying a registered initializer
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
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String initializerID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(containerID)) {
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof PathEntryContainerInitializer) {
								return (PathEntryContainerInitializer)execExt;
							}
						} catch (CoreException e) {
							// executable extension could not be created:
							// ignore this initializer if
							//e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}

	private synchronized IPathEntryContainer containerGet(ICProject cproject, IPath containerPath, boolean bCreateLock) {
		Map projectContainers = (Map)Containers.get(cproject);
		if (projectContainers == null) {
			projectContainers = new HashMap();
			Containers.put(cproject, projectContainers);
		}
		IPathEntryContainer container = (IPathEntryContainer)projectContainers.get(containerPath);
		// Initialize the first time with a lock
		if (bCreateLock && container == null) {
			container = new PathEntryContainerLock();
			projectContainers.put(containerPath, container);
		}
		return container;
	}

	private synchronized void containerPut(ICProject cproject, IPath containerPath, IPathEntryContainer container) {
		Map projectContainers = (Map)Containers.get(cproject);
		if (projectContainers == null) {
			projectContainers = new HashMap();
			Containers.put(cproject, projectContainers);
		}
		IPathEntryContainer oldContainer;
		if (container == null) {
			oldContainer = (IPathEntryContainer)projectContainers.remove(containerPath);
		} else {
			oldContainer = (IPathEntryContainer)projectContainers.put(containerPath, container);
		}
		if (oldContainer instanceof PathEntryContainerLock) {
			synchronized (oldContainer) {
				oldContainer.notifyAll();
			}
		}
	}

	private synchronized void containerRemove(ICProject cproject) {
		Containers.remove(cproject);
	}


	public void pathEntryContainerUpdates(IPathEntryContainerExtension container, PathEntryContainerChanged[] events, IProgressMonitor monitor) {
		
		ArrayList list = new ArrayList(events.length);
		for (int i = 0; i < events.length; ++i) {
			PathEntryContainerChanged event = events[i];
			ICElement celement = CoreModel.getDefault().create(event.getPath());
			if (celement != null) {
				// Sanity check the container __must__ be set on the project.
				boolean foundContainer = false;
				IPathEntryContainer[] containers = getPathEntryContainers(celement.getCProject());
				for (int k = 0 ; k < containers.length; ++k) {
					if (containers[k].getPath().equals(container.getPath())) {
						foundContainer = true;
						break;
					}
				}
				if (!foundContainer) {
					continue;
				}
				// remove the element info caching.
				if (celement instanceof IOpenable) {
					try {
						((IOpenable)celement).close();
						// Make sure we clear the cache on the project too
						if (!(celement instanceof ICProject)) {
							celement.getCProject().close();
						}
					} catch (CModelException e) {
						// ignore.
					}
				}
				int flag =0;
				if (event.isIncludeChange()) {
					flag |= ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				} 
                if (event.isMacroChange()) {
					flag |= ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				}
				CElementDelta delta = new CElementDelta(celement.getCModel());
				delta.changed(celement, flag);
				list.add(delta);
			}
		}
		if (list.size() > 0) {
			final ICElementDelta[] deltas = new ICElementDelta[list.size()];
			list.toArray(deltas);
			try {
				CoreModel.run(new IWorkspaceRunnable() {
					
					public void run(IProgressMonitor progressMonitor) throws CoreException {
						CModelManager manager = CModelManager.getDefault();
						for (int i = 0; i < deltas.length; i++) {
							manager.registerCModelDelta(deltas[i]);
						}
						manager.fire(ElementChangedEvent.POST_CHANGE);
					}
				}, monitor);
			} catch (CoreException e) {
				// log the error.
			}
		}
	}

	public String[] projectPrerequisites(IPathEntry[] entries) throws CModelException {
		if (entries != null) {
			ArrayList prerequisites = new ArrayList();
			for (int i = 0, length = entries.length; i < length; i++) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_PROJECT) {
					IProjectEntry entry = (IProjectEntry)entries[i];
					prerequisites.add(entry.getPath().lastSegment());
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
		// sanity
		if (entries == null) {
			entries = NO_PATHENTRIES;
		}

		ArrayList list = new ArrayList(entries.length);
		IPath projectPath = cproject.getPath();
		for (int i = 0; i < entries.length; i++) {
			IPathEntry entry;

			int kind = entries[i].getEntryKind();

			// translate the project prefix.
			IPath resourcePath = entries[i].getPath();
			if (resourcePath == null) {
				resourcePath = Path.EMPTY;
			}

			// Do not do this for container, the path is the ID.
			if (kind != IPathEntry.CDT_CONTAINER) {
				// translate to project relative from absolute (unless a device
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
			switch (kind) {
				case IPathEntry.CDT_INCLUDE : {
					IIncludeEntry include = (IIncludeEntry)entries[i];
					IPath baseRef = include.getBaseReference();
					if (baseRef == null || baseRef.isEmpty()) {
						entry = CoreModel.newIncludeEntry(resourcePath, include.getBasePath(), include.getIncludePath(),
								include.isSystemInclude(), include.getExclusionPatterns(), include.isExported());
					} else {
						entry = CoreModel.newIncludeRefEntry(resourcePath, baseRef, include.getIncludePath());
					}
					break;
				}
				case IPathEntry.CDT_INCLUDE_FILE : {
					IIncludeFileEntry includeFile = (IIncludeFileEntry)entries[i];
					entry = CoreModel.newIncludeFileEntry(resourcePath, includeFile.getBasePath(),
							includeFile.getBaseReference(), includeFile.getIncludeFilePath(),
							includeFile.getExclusionPatterns(), includeFile.isExported());
					break;
				}
				case IPathEntry.CDT_LIBRARY : {
					ILibraryEntry library = (ILibraryEntry)entries[i];
					IPath sourcePath = library.getSourceAttachmentPath();
					if (sourcePath != null) {
						// translate to project relative from absolute
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
								sourcePath, library.getSourceAttachmentRootPath(), library.getSourceAttachmentPrefixMapping(),
								library.isExported());
					} else {
						entry = CoreModel.newLibraryRefEntry(resourcePath, baseRef, library.getLibraryPath());
					}
					break;
				}
				case IPathEntry.CDT_MACRO : {
					IMacroEntry macro = (IMacroEntry)entries[i];
					IPath baseRef = macro.getBaseReference();
					if (baseRef == null || baseRef.isEmpty()) {
						entry = CoreModel.newMacroEntry(resourcePath, macro.getMacroName(), macro.getMacroValue(),
								macro.getExclusionPatterns(), macro.isExported());
					} else {
						entry = CoreModel.newMacroRefEntry(resourcePath, baseRef, macro.getMacroName());
					}
					break;
				}
				case IPathEntry.CDT_MACRO_FILE : {
					IMacroFileEntry macro = (IMacroFileEntry)entries[i];
					entry = CoreModel.newMacroFileEntry(resourcePath, macro.getBasePath(),
							macro.getBaseReference(), macro.getMacroFilePath(),
							macro.getExclusionPatterns(), macro.isExported());
					break;
				}
				case IPathEntry.CDT_OUTPUT : {
					IOutputEntry out = (IOutputEntry)entries[i];
					entry = CoreModel.newOutputEntry(resourcePath, out.getExclusionPatterns());
					break;
				}
				case IPathEntry.CDT_PROJECT : {
					IProjectEntry projEntry = (IProjectEntry)entries[i];
					entry = CoreModel.newProjectEntry(projEntry.getPath(), projEntry.isExported());
					break;
				}
				case IPathEntry.CDT_SOURCE : {
					ISourceEntry source = (ISourceEntry)entries[i];
					entry = CoreModel.newSourceEntry(resourcePath, source.getExclusionPatterns());
					break;
				}
				case IPathEntry.CDT_CONTAINER :
					entry = CoreModel.newContainerEntry(entries[i].getPath(), entries[i].isExported());
					break;
				default :
					entry = entries[i];
			}
			list.add(entry);
		}
		try {
			IPathEntry[] newRawEntries = new IPathEntry[list.size()];
			list.toArray(newRawEntries);
			IProject project = cproject.getProject();
			IPathEntryStore store = getPathEntryStore(project, true);
			store.setRawPathEntries(newRawEntries);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}
	
	public void generateMarkers(final IProject project, final ICModelStatus[] problems) {
		Job markerTask = new Job("PathEntry Marker Job") { //$NON-NLS-1$
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					CCorePlugin.getWorkspace().run(new IWorkspaceRunnable() {
						
						/* (non-Javadoc)
						 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
						 */
						public void run(IProgressMonitor mon) throws CoreException {
							PathEntryUtil.flushPathEntryProblemMarkers(project);
							for (int i = 0; i < problems.length; ++i) {
								PathEntryUtil.createPathEntryProblemMarker(project, problems[i]);
							}
						}
					}, null);
				} catch (CoreException e) {
					return e.getStatus();
				}

				return Status.OK_STATUS;
			}
		};
		ISchedulingRule rule = project.getWorkspace().getRuleFactory().markerRule(project);
		markerTask.setRule(rule);
		markerTask.schedule();
	}

	public ICElementDelta[] generatePathEntryDeltas(ICProject cproject, IPathEntry[] oldEntries, IPathEntry[] newEntries) {
		ArrayList list = new ArrayList();

		// if nothing was known before do not generate any deltas.
		if (oldEntries == null) {
			return new ICElementDelta[0];
		}
		// Sanity checks
		if (newEntries == null) {
			newEntries = NO_PATHENTRIES;
		}

		// Check the removed entries.
		for (int i = 0; i < oldEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < newEntries.length; j++) {
				if (oldEntries[i].equals(newEntries[j])) {
					found = true;
					break;
				}
			}
			// Was it deleted.
			if (!found) {
				ICElementDelta delta = makePathEntryDelta(cproject, oldEntries[i], true);
				if (delta != null) {
					list.add(delta);
				}
			}
		}

		// Check the new entries.
		for (int i = 0; i < newEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < oldEntries.length; j++) {
				if (newEntries[i].equals(oldEntries[j])) {
					found = true;
					break;
				}
			}
			// is it new?
			if (!found) {
				ICElementDelta delta = makePathEntryDelta(cproject, newEntries[i], false);
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
		// They may have remove some duplications, catch here .. consider it as reordering.
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
	 * return a delta, with the specified change flag.
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
				case IPathEntry.CDT_SOURCE : {
					ISourceEntry source = (ISourceEntry)entry;
					IPath path = source.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_SOURCE : ICElementDelta.F_ADDED_PATHENTRY_SOURCE;
					break;
				}
				case IPathEntry.CDT_LIBRARY : {
					celement = cproject;
					flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY : ICElementDelta.F_ADDED_PATHENTRY_LIBRARY;
					break;
				}
				case IPathEntry.CDT_PROJECT : {
					//IProjectEntry pentry = (IProjectEntry) entry;
					celement = cproject;
					flag = ICElementDelta.F_CHANGED_PATHENTRY_PROJECT;
					break;
				}
				case IPathEntry.CDT_INCLUDE : {
					IIncludeEntry include = (IIncludeEntry)entry;
					IPath path = include.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
					break;
				}
				case IPathEntry.CDT_INCLUDE_FILE: {
					IIncludeFileEntry includeFile = (IIncludeFileEntry)entry;
					IPath path = includeFile.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
					break;
				}
				case IPathEntry.CDT_MACRO : { 
					IMacroEntry macro = (IMacroEntry)entry;
					IPath path = macro.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
					break;
				}
				case IPathEntry.CDT_MACRO_FILE: {
					IMacroFileEntry macro = (IMacroFileEntry)entry;
					IPath path = macro.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
					break;
				}
				case IPathEntry.CDT_CONTAINER : {
					//IContainerEntry container = (IContainerEntry) entry;
					//celement = cproject;
					//SHOULD NOT BE HERE Container are resolved.
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
		ArrayList containerIDList = new ArrayList(5);
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String idAttribute = configElements[j].getAttribute("id"); //$NON-NLS-1$
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
		synchronized (this) {
			oldStore = (IPathEntryStore)storeMap.remove(project);
			if (newStore != null) {
				storeMap.put(project, newStore);
			}
		}
		if (oldStore != null) {
			// remove are self before closing
			oldStore.removePathEntryStoreListener(this);
			oldStore.close();
		}
	}

	public synchronized IPathEntryStore getPathEntryStore(IProject project, boolean create) throws CoreException {
		IPathEntryStore store = (IPathEntryStore)storeMap.get(project);
		if (store == null && create == true) {
			store = createPathEntryStore(project);
			storeMap.put(project, store);
			store.addPathEntryStoreListener(this);
		}
		return store;
	}

	public IPathEntryStore createPathEntryStore(IProject project) throws CoreException {
		IPathEntryStore store = null;
		if (project != null) {
			try {
				ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
				if (cdesc != null) {
					ICExtensionReference[] cextensions = cdesc.get(PATHENTRY_STORE_UNIQ_ID, true);
					if (cextensions.length > 0) {
						for (int i = 0; i < cextensions.length; i++) {
							try {
								store = (IPathEntryStore)cextensions[i].createExtension();
								break;
							} catch (ClassCastException e) {
								//
								CCorePlugin.log(e);
							}
						}
					}
				}
			} catch (CoreException e) {
				// ignore since we fall back to a default....
			}
		}
		if (store == null) {
			store = new DefaultPathEntryStore(project);
		}
		return store;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.IPathEntryStoreListener#pathEntryStoreChanged(org.eclipse.cdt.core.resources.PathEntryChangedEvent)
	 */
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		IProject project = event.getProject();

		// sanity
		if (project == null) {
			return;
		}

		CModelManager manager = CModelManager.getDefault();
		ICProject cproject = manager.create(project);
		if (event.hasClosed()) {
			setPathEntryStore(project, null);
			containerRemove(cproject);
		}
		if (project.isAccessible()) {
			try {
				// Clear the old cache entries.
				IPathEntry[] oldResolvedEntries = removeCachedResolvedPathEntries(cproject);
				IPathEntry[] newResolvedEntries = getResolvedPathEntries(cproject);
				ICElementDelta[] deltas = generatePathEntryDeltas(cproject, oldResolvedEntries, newResolvedEntries);
				if (deltas.length > 0) {
					cproject.close();
					for (int i = 0; i < deltas.length; i++) {
						manager.registerCModelDelta(deltas[i]);
					}
					manager.fire(ElementChangedEvent.POST_CHANGE);
				}
			} catch (CModelException e) {
				CCorePlugin.log(e);
			}
		} else {
			resolvedMap.remove(cproject);
			containerRemove(cproject);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		try {
			if (processDelta(event.getDelta()) == true) {
				ICProject[] cProjects = (ICProject [])resolvedMap.keySet().toArray(new ICProject[0]);
				for(int i = 0; i < cProjects.length; i++) {
					IPathEntry[] entries = getCachedResolvedPathEntries(cProjects[i]);
					if (entries != null) {
						IProject project = cProjects[i].getProject();
						try {
							IMarker[] markers = project.findMarkers(ICModelMarker.PATHENTRY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
							if (markers != null && markers.length > 0) {
								ArrayList problemList = new ArrayList();
								for (int j = 0; j < entries.length; j++) {
									ICModelStatus status = PathEntryUtil.validatePathEntry(cProjects[i], entries[j], true, false);
									if (!status.isOK()) {
										problemList.add(status);
									}
								}
								ICModelStatus[] problems = new ICModelStatus[problemList.size()];
								problemList.toArray(problems);
								if (PathEntryUtil.hasPathEntryProblemMarkersChange(project, problems)) {
									generateMarkers(project, problems);
								}
							}
						} catch (CoreException e) {
							// ignore the exception.
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

		// handle open, closing and removing of projects
		if ( type == ICElement.C_PROJECT) {
			ICProject cproject = (ICProject)element;
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
							containerRemove(cproject);
						}
					}
				} 
				return true;
			}
			// project change, traverse children.
		} 
		if (element instanceof IWorkingCopy) {
			return false;
		}
		if (kind == ICElementDelta.ADDED || kind == ICElementDelta.REMOVED) {
			return true; // add/remove we validate all paths
		}
		if (type == ICElement.C_MODEL || type == ICElement.C_CCONTAINER || type == ICElement.C_PROJECT) {
			ICElementDelta[] affectedChildren = delta.getAffectedChildren();
			for (int i = 0; i < affectedChildren.length; i++) {
				if (processDelta(affectedChildren[i]) == true) {
					return true;
				}
			}
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