/**********************************************************************
 * Created on 25-Mar-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * @author alain
 *  
 */
public class PathEntryManager implements IPathEntryStoreListener, IElementChangedListener {

	static String CONTAINER_INITIALIZER_EXTPOINT_ID = "PathEntryContainerInitializer"; //$NON-NLS-1$
	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	static final String[] NO_PREREQUISITES = new String[0];
	/**
	 * pathentry containers pool
	 */
	public static HashMap Containers = new HashMap(5);

	static final IPathEntry[] NO_PATHENTRIES = new IPathEntry[0];

	HashMap resolvedMap = new HashMap();

	HashMap storeMap = new HashMap();

	private static PathEntryManager pathEntryManager;
	private PathEntryManager() {
	}

	/**
	 * Return the singleton.
	 */
	public static PathEntryManager getDefault() {
		if (pathEntryManager == null) {
			pathEntryManager = new PathEntryManager();
			CoreModel.getDefault().addElementChangedListener(pathEntryManager);
		}
		return pathEntryManager;
	}

	public IPathEntry[] getResolvedPathEntries(ICProject cproject) throws CModelException {
		IPathEntry[] entries = (IPathEntry[]) resolvedMap.get(cproject);
		if (entries == null) {
			entries = getRawPathEntries(cproject);
			ArrayList list = new ArrayList();
			for (int i = 0; i < entries.length; i++) {
				IPathEntry entry = entries[i];
				// Expand the containers.
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry centry = (IContainerEntry) entry;
					IPathEntryContainer container = getPathEntryContainer(centry, cproject);
					if (container != null) {
						IPathEntry[] containerEntries = container.getPathEntries();
						if (containerEntries != null) {
							for (int j = 0; j < containerEntries.length; j++) {
								list.add(containerEntries[i]);
							}
						}
					}
				} else {
					IPathEntry e = getExpandedPathEntry(entry, cproject);
					if (e != null) {
						list.add(entry);
					}
				}
			}
			entries = new IPathEntry[list.size()];
			list.toArray(entries);
			resolvedMap.put(cproject, entries);
		}
		return entries;
	}

	private IPathEntry getExpandedPathEntry(IPathEntry entry, ICProject cproject) throws CModelException {
		switch(entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE: {
				IIncludeEntry includeEntry = (IIncludeEntry)entry;
				IPath refPath = includeEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					IPath includePath = includeEntry.getIncludePath();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							ICProject refCProject = CoreModel.getDefault().create((IProject)res);
							if (refCProject != null) {
								IPathEntry[] entries = getResolvedPathEntries(refCProject);
								for (int i = 0; i < entries.length; i++) {
									if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
										IIncludeEntry refEntry = (IIncludeEntry)entries[i];
										if (refEntry.getIncludePath().equals(includePath)) {
											IPath newBasePath = refEntry.getBasePath();
											return CoreModel.newIncludeEntry(includeEntry.getPath(), newBasePath, includePath);											
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
									IIncludeEntry refEntry = (IIncludeEntry)entries[i];
									if (refEntry.getIncludePath().equals(includePath)) {
										IPath newBasePath = refEntry.getBasePath();
										return CoreModel.newIncludeEntry(includeEntry.getPath(), newBasePath, includePath);											
									}
								}
							}
						}
					}
				}
				break;
			}

			case IPathEntry.CDT_MACRO: {
				IMacroEntry macroEntry = (IMacroEntry)entry;
				IPath refPath = macroEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					String name = macroEntry.getMacroName();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							ICProject refCProject = CoreModel.getDefault().create((IProject)res);
							if (refCProject != null) {
								IPathEntry[] entries = getResolvedPathEntries(refCProject);
								for (int i = 0; i < entries.length; i++) {
									if (entries[i].getEntryKind() == IPathEntry.CDT_MACRO) {
										IMacroEntry refEntry = (IMacroEntry)entries[i];
										if (refEntry.getMacroName().equals(name)) {
											String value = refEntry.getMacroValue();
											return CoreModel.newMacroEntry(macroEntry.getPath(), name, value);											
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_MACRO) {
									IMacroEntry refEntry = (IMacroEntry)entries[i];
									if (refEntry.getMacroName().equals(name)) {
										String value = refEntry.getMacroValue();
										return CoreModel.newMacroEntry(macroEntry.getPath(), name, value);											
									}
								}
							}
						}
					}
				}
				break;
			}

			case IPathEntry.CDT_LIBRARY: {
				ILibraryEntry libEntry = (ILibraryEntry)entry;
				IPath refPath = libEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					IPath libraryPath = libEntry.getPath();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							ICProject refCProject = CoreModel.getDefault().create((IProject)res);
							if (refCProject != null) {
								IPathEntry[] entries = getResolvedPathEntries(refCProject);
								for (int i = 0; i < entries.length; i++) {
									if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
										ILibraryEntry refEntry = (ILibraryEntry)entries[i];
										if (refEntry.getPath().equals(libraryPath)) {
											return CoreModel.newLibraryEntry(refEntry.getBasePath(),
													refEntry.getPath(), refEntry.getSourceAttachmentPath(),
													refEntry.getSourceAttachmentRootPath(),
													refEntry.getSourceAttachmentPrefixMapping(), false);											
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
									ILibraryEntry refEntry = (ILibraryEntry)entries[i];
									if (refEntry.getPath().equals(libraryPath)) {
										return CoreModel.newLibraryEntry(refEntry.getBasePath(),
												refEntry.getPath(), refEntry.getSourceAttachmentPath(),
												refEntry.getSourceAttachmentRootPath(),
												refEntry.getSourceAttachmentPrefixMapping(), false);											
									}
								}
							}
						}
					}
				}
				break;
			}

		}
		return entry;
	}

	public void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		try {
			IPathEntry[] oldResolvedEntries = (IPathEntry[]) resolvedMap.get(cproject);
			SetPathEntriesOperation op = new SetPathEntriesOperation(cproject, oldResolvedEntries, newEntries);
			CModelManager.getDefault().runOperation(op, monitor);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
		IProject project = cproject.getProject();
		if (!(CoreModel.hasCNature(project) || CoreModel.hasCCNature(project))) {
			return NO_PATHENTRIES;
		}
		IPathEntry[] pathEntries = NO_PATHENTRIES;
		try {
			IPathEntryStore store = getPathEntryStore(project);
			pathEntries = store.getRawPathEntries(project);
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
					IContainerEntry cont = (IContainerEntry) entry;
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
				continue;
			}
			IPathEntryContainer oldContainer = containerGet(affectedProject, containerPath);
			if (oldContainer != null && newContainer != null && oldContainer.equals(newContainer)) {
				modifiedProjects[i] = null; // filter out this project -
				// container did not change
				continue;
			}
			remaining++;
			oldResolvedEntries[i] = (IPathEntry[]) resolvedMap.remove(affectedProject);
			containerPut(affectedProject, containerPath, newContainer);
		}
		// Nothing change.
		if (remaining == 0) {
			return;
		}
		// trigger model refresh
		try {
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
						IPathEntry[] newEntries = getResolvedPathEntries(affectedProject);
						ICElementDelta[] deltas = generatePathEntryDeltas(affectedProject, oldResolvedEntries[i], newEntries);
						if (deltas.length > 0) {
							shouldFire = true;
							for (int j = 0; j < deltas.length; j++) {
								mgr.registerCModelDelta(deltas[j]);
							}
						}
						//affectedProject.setRawPathEntries(affectedProject.getRawPathEntries(),
						// progressMonitor);
					}
					if (shouldFire) {
						mgr.fire(ElementChangedEvent.POST_CHANGE);
					}
				}
			}, monitor);
		} catch (CoreException e) {
			if (e instanceof CModelException) {
				throw (CModelException) e;
			} else {
				throw new CModelException(e);
			}
		}
	}

	public IPathEntryContainer getPathEntryContainer(IContainerEntry entry, ICProject cproject) throws CModelException {
		return getPathEntryContainer(entry.getPath(), cproject);
	}

	public IPathEntryContainer getPathEntryContainer(final IPath containerPath, final ICProject project) throws CModelException {
		// Try the cache.
		IPathEntryContainer container = containerGet(project, containerPath);
		if (container == null) {
			final PathEntryContainerInitializer initializer = getPathEntryContainerInitializer(containerPath.segment(0));
			if (initializer != null) {
				containerPut(project, containerPath, container);
				boolean ok = false;
				try {
					// wrap initializer call with Safe runnable in case
					// initializer would be
					// causing some grief
					Platform.run(new ISafeRunnable() {

						public void handleException(Throwable exception) {
							//Util.log(exception, "Exception occurred in
							// container initializer: "+initializer);
							// //$NON-NLS-1$
						}

						public void run() throws Exception {
							initializer.initialize(containerPath, project);
						}
					});
					// retrieve value (if initialization was successful)
					container = containerGet(project, containerPath);
					ok = true;
				} finally {
					if (!ok) {
						containerPut(project, containerPath, null); // flush
						// cache
					}
				}
			}
		}
		return container;
	}

	/**
	 * Helper method finding the container initializer registered for a given container ID or <code>null</code> if none was found
	 * while iterating over the contributions to extension point to the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to identify the registered container initializer.
	 * <p>
	 * 
	 * @param containerID -
	 *            a containerID identifying a registered initializer
	 * @return PathEntryContainerInitializer - the registered container initializer or <code>null</code> if none was found.
	 */
	public PathEntryContainerInitializer getPathEntryContainerInitializer(String containerID) {
		Plugin core = CCorePlugin.getDefault();
		if (core == null) {
			return null;
		}
		IExtensionPoint extension = core.getDescriptor().getExtensionPoint(CONTAINER_INITIALIZER_EXTPOINT_ID);
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
								return (PathEntryContainerInitializer) execExt;
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

	public IPathEntryContainer containerGet(ICProject project, IPath containerPath) {
		Map projectContainers = (Map) Containers.get(project);
		if (projectContainers == null) {
			projectContainers = new HashMap();
			Containers.put(project, projectContainers);
		}
		IPathEntryContainer container = (IPathEntryContainer) projectContainers.get(containerPath);
		return container;
	}

	public void containerPut(ICProject project, IPath containerPath, IPathEntryContainer container) {
		Map projectContainers = (Map) Containers.get(project);
		if (projectContainers == null) {
			projectContainers = new HashMap();
			Containers.put(project, projectContainers);
		}
		projectContainers.put(containerPath, container);
	}

	public String[] projectPrerequisites(IPathEntry[] entries) throws CModelException {
		if (entries != null) {
			ArrayList prerequisites = new ArrayList();
			for (int i = 0, length = entries.length; i < length; i++) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_PROJECT) {
					IProjectEntry entry = (IProjectEntry) entries[i];
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

	public void saveRawPathEntries(ICProject cproject, IPathEntry[] newRawEntries) throws CModelException {
		try {
			IProject project = cproject.getProject();
			IPathEntryStore store = getPathEntryStore(project);
			store.setRawPathEntries(project, newRawEntries);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public ICElementDelta[] generatePathEntryDeltas(ICProject cproject, IPathEntry[] oldEntries, IPathEntry[] newEntries) {
		ArrayList list = new ArrayList();

		// Sanity checks
		if (oldEntries == null) {
			oldEntries = new IPathEntry[0];
		}
		if (newEntries == null) {
			newEntries = new IPathEntry[0];
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
				case IPathEntry.CDT_SOURCE: {
					ISourceEntry source = (ISourceEntry) entry;
					IPath path = source.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_SOURCE : ICElementDelta.F_ADDED_PATHENTRY_SOURCE;
					break;
				}
				case IPathEntry.CDT_LIBRARY: {
					celement = cproject;
					flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY : ICElementDelta.F_ADDED_PATHENTRY_LIBRARY;
					break;
				}
				case IPathEntry.CDT_PROJECT: {
					//IProjectEntry pentry = (IProjectEntry) entry;
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
				case IPathEntry.CDT_MACRO: {
					IMacroEntry macro = (IMacroEntry) entry;
					IPath path = macro.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
					break;
				}
				case IPathEntry.CDT_CONTAINER: {
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
		IExtensionPoint extension = core.getDescriptor().getExtensionPoint(CONTAINER_INITIALIZER_EXTPOINT_ID);
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

	IPathEntryStore getPathEntryStore(IProject project) throws CoreException {
		IPathEntryStore store = (IPathEntryStore)storeMap.get(project);
		if (store == null) {
			store = CCorePlugin.getDefault().getPathEntryStore(project);
			storeMap.put(project, store);
			store.addPathEntryStoreListener(this);
		}
		return store;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStoreListener#pathEntryStoreChanged(org.eclipse.cdt.core.resources.PathEntryChangedEvent)
	 */
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		IProject project = event.getProject();
		
		// sanity
		if (project == null) {
			return;
		}

		IPathEntryStore store = (IPathEntryStore)storeMap.get(project);
		if (store != null) {
			if (event.hasClosed()) {
				storeMap.remove(project);
				store.removePathEntryStoreListener(this);
			}
			if (project.isAccessible()) {
				CModelManager manager = CModelManager.getDefault();
				ICProject cproject = manager.create(project);
				try {
					IPathEntry[] oldResolvedEntries = getResolvedPathEntries(cproject);
					// Clear the old cache entries.
					resolvedMap.remove(cproject);
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
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
		}
	}

	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();

		//System.out.println("Processing " + element);

		// handle closing and removing of projects
		if (((flags & ICElementDelta.F_CLOSED) != 0) || (kind == ICElementDelta.REMOVED)) {
			if (element.getElementType() == ICElement.C_PROJECT) {
				IProject project = element.getCProject().getProject();
				IPathEntryStore store = (IPathEntryStore)storeMap.get(project);
				if (store != null) {
					store.fireClosedEvent(project);
				}
			}
		}
		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}
}