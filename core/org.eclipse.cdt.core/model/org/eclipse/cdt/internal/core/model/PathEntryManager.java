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
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.cdt.internal.core.CharOperation;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author alain
 *  
 */
public class PathEntryManager {

	static String CONTAINER_INITIALIZER_EXTPOINT_ID = "pathEntryContainerInitializer"; //$NON-NLS-1$
	static String PATH_ENTRY = "pathentry"; //$NON-NLS-1$
	static String PATH_ENTRY_ID = "org.eclipse.cdt.core.pathentry"; //$NON-NLS-1$
	static String ATTRIBUTE_KIND = "kind"; //$NON-NLS-1$
	static String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	static String ATTRIBUTE_EXPORTED = "exported"; //$NON-NLS-1$
	static String ATTRIBUTE_SOURCEPATH = "sourcepath"; //$NON-NLS-1$
	static String ATTRIBUTE_ROOTPATH = "roopath"; //$NON-NLS-1$
	static String ATTRIBUTE_PREFIXMAPPING = "prefixmapping"; //$NON-NLS-1$
	static String ATTRIBUTE_EXCLUDING = "excluding"; //$NON-NLS-1$
	static String ATTRIBUTE_RECUSIVE = "recusive"; //$NON-NLS-1$
	static String ATTRIBUTE_OUTPUT = "output"; //$NON-NLS-1$
	static String ATTRIBUTE_INCLUDE = "include"; //$NON-NLS-1$
	static String ATTRIBUTE_SYSTEM = "system"; //$NON-NLS-1$
	static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	static String VALUE_TRUE = "true"; //$NON-NLS-1$

	final static IPathEntry[] EMPTY = {};
    
	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	static final String[] NO_PREREQUISITES = new String[0];

	/**
	 * pathentry containers pool
	 */
	public static HashMap Containers = new HashMap(5);

	HashMap resolvedMap = new HashMap();

	private static PathEntryManager pathEntryManager;

	private PathEntryManager() {
	}

	/**
	 * Return the singleton.
	 */
	public static PathEntryManager getDefault() {
		if (pathEntryManager == null) {
			pathEntryManager = new PathEntryManager();
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
					list.add(entry);
				}
			}
			entries = new IPathEntry[list.size()];
			list.toArray(entries);
			resolvedMap.put(cproject, entries);
		}
		return entries;
	}

	public void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		try {
			IPathEntry[] oldResolvedEntries = (IPathEntry[])resolvedMap.get(cproject);
			resolvedMap.put(cproject, null);
			SetPathEntriesOperation op = new SetPathEntriesOperation(cproject, oldResolvedEntries, newEntries);
			CModelManager.getDefault().runOperation(op, monitor);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
		ArrayList pathEntries = new ArrayList();
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(cproject.getProject());
			Element element = cdesc.getProjectData(PATH_ENTRY_ID);
			NodeList list = element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node childNode = list.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					if (childNode.getNodeName().equals(PATH_ENTRY)) {
						pathEntries.add(decodePathEntry(cproject, (Element) childNode));
					}
				}
			}
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return (IPathEntry[]) pathEntries.toArray(EMPTY);
	}

	public void setPathEntryContainer(ICProject[] affectedProjects, IPathEntryContainer newContainer, IProgressMonitor monitor) throws CModelException {

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
				// filter out this project - does not reference the container path
				modifiedProjects[i] = null;
				continue;
			}
			IPathEntryContainer oldContainer = containerGet(affectedProject, containerPath);
			if (oldContainer != null && newContainer != null && oldContainer.equals(newContainer)) {
				modifiedProjects[i] = null; // filter out this project - container did not change
				continue;
			}
			remaining++;
			oldResolvedEntries[i] = (IPathEntry[])resolvedMap.get(affectedProject);
			resolvedMap.put(affectedProject, null);
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
						ICProject affectedProject = (ICProject) modifiedProjects[i];
						if (affectedProject == null) {
							continue; // was filtered out
						}

						IPathEntry[] newEntries = getResolvedPathEntries(affectedProject);
						ICElementDelta[] deltas = generatePathEntryDeltas(affectedProject,
							oldResolvedEntries[i], newEntries);
						if (deltas.length > 0) {
							shouldFire = true;
							for (int j = 0; j < deltas.length; j++) {
								mgr.registerCModelDelta(deltas[j]);
							}
						}

						//affectedProject.setRawPathEntries(affectedProject.getRawPathEntries(), progressMonitor);
					}
					if (shouldFire) {
						mgr.fire();
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
					// wrap initializer call with Safe runnable in case initializer would be
					// causing some grief
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							//Util.log(exception, "Exception occurred in container initializer: "+initializer); //$NON-NLS-1$
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
						containerPut(project, containerPath, null); // flush cache
					}
				}
			}
		}
		return container;
	}

	/**
	 * Helper method finding the container initializer registered for a given container ID or <code>null</code>
	 * if none was found while iterating over the contributions to extension point to the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to identify the registered container initializer.
	 * <p>
	 * 
	 * @param containerID -
	 *            a containerID identifying a registered initializer
	 * @return PathEntryContainerInitializer - the registered container initializer or <code>null</code> if none was
	 *         found.
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
	public void saveRawPathEntries(ICProject cproject, IPathEntry[] newRawEntries) throws CModelException {
		try {
			ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(cproject.getProject());
			Element rootElement = descriptor.getProjectData(PATH_ENTRY_ID);
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}

			// Save the entries
			if (newRawEntries != null && newRawEntries.length > 0) {
				// Serialize the include paths
				Document doc = rootElement.getOwnerDocument();
				encodePathEntries(cproject.getPath(), doc, rootElement, newRawEntries);
			}
			descriptor.saveProjectData();
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public ICElementDelta[] generatePathEntryDeltas(ICProject cproject, IPathEntry[] oldEntries, IPathEntry[] newEntries) {
		ArrayList list = new ArrayList();
		CModelManager manager = CModelManager.getDefault();
		boolean needToUpdateDependents = false;
		boolean hasDelta = false;

		// Check the removed entries.
		if (oldEntries != null) {
			for (int i = 0; i < oldEntries.length; i++) {
				boolean found = false;
				if (newEntries != null) {
					for (int j = 0; j < newEntries.length; j++) {
						if (oldEntries[i].equals(newEntries[j])) {
							found = true;
							break;
						}
					}
				}
				// Was it deleted.
				if (!found) {
					ICElementDelta delta =
						makePathEntryDelta(cproject, oldEntries[i], true);
					if (delta != null) {
						list.add(delta);
					}
				}
			}
		}
		// Check the new entries.
		if (newEntries != null) {
			for (int i = 0; i < newEntries.length; i++) {
				boolean found = false;
				if (oldEntries != null) {
					for (int j = 0; j < oldEntries.length; j++) {
						if (newEntries[i].equals(oldEntries[j])) {
							found = true;
							break;
						}
					}
				}
				// is it new?
				if (!found) {
					ICElementDelta delta =
						makePathEntryDelta(cproject, newEntries[i], false);
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
		int kind = entry.getEntryKind();
		ICElement celement = null;
		int flag = 0;
		if (kind == IPathEntry.CDT_SOURCE) {
			ISourceEntry source = (ISourceEntry) entry;
			IPath path = source.getPath();
			celement = CoreModel.getDefault().create(path);
			flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_SOURCE : ICElementDelta.F_ADDED_PATHENTRY_SOURCE; 
		} else if (kind == IPathEntry.CDT_LIBRARY) {
			ILibraryEntry lib = (ILibraryEntry) entry;
			celement = CProject.getLibraryReference(cproject, null,lib);
			flag =  (removed) ? ICElementDelta.F_ADDED_PATHENTRY_LIBRARY : ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY; 
		} else if (kind == IPathEntry.CDT_PROJECT) {
			//IProjectEntry pentry = (IProjectEntry) entry;
			celement = cproject;
			flag = ICElementDelta.F_CHANGED_PATHENTRY_PROJECT; 
		} else if (kind == IPathEntry.CDT_INCLUDE) {
			IIncludeEntry include = (IIncludeEntry) entry;
			IPath path = include.getPath();
			celement = CoreModel.getDefault().create(path);
			flag = ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE; 
		} else if (kind == IPathEntry.CDT_MACRO) {
			IMacroEntry macro = (IMacroEntry) entry;
			IPath path = macro.getPath();
			celement = CoreModel.getDefault().create(path);
			flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO; 
		} else if (kind == IPathEntry.CDT_CONTAINER) {
			//IContainerEntry container = (IContainerEntry) entry;
			//celement = cproject;
			// SHOULD NOT BE HERE Container are resolved.
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

	static IPathEntry decodePathEntry(ICProject cProject, Element element) throws CModelException {
		IPath projectPath = cProject.getProject().getFullPath();

		// kind
		String kindAttr = element.getAttribute(ATTRIBUTE_KIND);
		int kind = PathEntry.kindFromString(kindAttr);

		// exported flag
		boolean isExported = false;
		if (element.hasAttribute(ATTRIBUTE_EXPORTED)) {
			isExported = element.getAttribute(ATTRIBUTE_EXPORTED).equals(VALUE_TRUE);
		}

		// ensure path is absolute
		String pathAttr = element.getAttribute(ATTRIBUTE_PATH);
		IPath path = new Path(pathAttr);
		if (kind != IPathEntry.CDT_VARIABLE && !path.isAbsolute()) {
			path = projectPath.append(path);
		}

		// source attachment info (optional)
		IPath sourceAttachmentPath =
			element.hasAttribute(ATTRIBUTE_SOURCEPATH) ? new Path(element.getAttribute(ATTRIBUTE_SOURCEPATH)) : null;
		IPath sourceAttachmentRootPath =
			element.hasAttribute(ATTRIBUTE_ROOTPATH) ? new Path(element.getAttribute(ATTRIBUTE_ROOTPATH)) : null;
		IPath sourceAttachmentPrefixMapping =
			element.hasAttribute(ATTRIBUTE_PREFIXMAPPING) ? new Path(element.getAttribute(ATTRIBUTE_PREFIXMAPPING)) : null;

		// exclusion patterns (optional)
		String exclusion = element.getAttribute(ATTRIBUTE_EXCLUDING);
		IPath[] exclusionPatterns = APathEntry.NO_EXCLUSION_PATTERNS;
		if (!exclusion.equals("")) { //$NON-NLS-1$
			char[][] patterns = CharOperation.splitOn('|', exclusion.toCharArray());
			int patternCount;
			if ((patternCount = patterns.length) > 0) {
				exclusionPatterns = new IPath[patternCount];
				for (int j = 0; j < patterns.length; j++) {
					exclusionPatterns[j] = new Path(new String(patterns[j]));
				}
			}
		}

		boolean isRecursive = false;
		if (element.hasAttribute(ATTRIBUTE_RECUSIVE)) {
			isRecursive = element.getAttribute(ATTRIBUTE_RECUSIVE).equals(VALUE_TRUE);
		}

		// recreate the CP entry

		switch (kind) {

			case IPathEntry.CDT_PROJECT :
				return CoreModel.newProjectEntry(path, isExported);

			case IPathEntry.CDT_LIBRARY :
				return CoreModel.newLibraryEntry(
					path,
					sourceAttachmentPath,
					sourceAttachmentRootPath,
					sourceAttachmentPrefixMapping,
					isExported);

			case IPathEntry.CDT_SOURCE :
				{
					// custom output location
					IPath outputLocation = element.hasAttribute(ATTRIBUTE_OUTPUT) ? projectPath.append(element.getAttribute(ATTRIBUTE_OUTPUT)) : null;
					// must be an entry in this project or specify another
					// project
					String projSegment = path.segment(0);
					if (projSegment != null && projSegment.equals(cProject.getElementName())) { // this project
						return CoreModel.newSourceEntry(path, outputLocation, isRecursive, exclusionPatterns);
					} else { // another project
						return CoreModel.newProjectEntry(path, isExported);
					}
				}

			case IPathEntry.CDT_INCLUDE :
				{
					// include path info
					IPath includePath =
						element.hasAttribute(ATTRIBUTE_INCLUDE) ? new Path(element.getAttribute(ATTRIBUTE_INCLUDE)) : null;
					// isSysteminclude
					boolean isSystemInclude = false;
					if (element.hasAttribute(ATTRIBUTE_SYSTEM)) {
						isSystemInclude = element.getAttribute(ATTRIBUTE_SYSTEM).equals(VALUE_TRUE);
					}
					return CoreModel.newIncludeEntry(
						path,
						includePath,
						isSystemInclude,
						isRecursive,
						exclusionPatterns);
				}

			case IPathEntry.CDT_MACRO :
				{
					String macroName = element.getAttribute(ATTRIBUTE_NAME);
					String macroValue = element.getAttribute(ATTRIBUTE_VALUE);
					return CoreModel.newMacroEntry(path, macroName, macroValue, isRecursive, exclusionPatterns, isExported);
				}

			case IPathEntry.CDT_CONTAINER :
				{
					IPath id = new Path(element.getAttribute(ATTRIBUTE_PATH));
					return CoreModel.newContainerEntry(id, isExported);
				}

			default :
				{
					ICModelStatus status = new CModelStatus(ICModelStatus.ERROR, "PathEntry: unknown kind (" + kindAttr + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CModelException(status);
				}
		}
	}

	static void encodePathEntries(IPath projectPath, Document doc, Element configRootElement, IPathEntry[] entries) {
		Element element;
		for (int i = 0; i < entries.length; i++) {
			element = doc.createElement(PATH_ENTRY);
			configRootElement.appendChild(element);
			int kind = entries[i].getEntryKind();

			// Set the kind
			element.setAttribute(ATTRIBUTE_KIND, PathEntry.kindToString(kind));

			// Save the exclusions attributes
			if (entries[i] instanceof APathEntry) {
				APathEntry entry = (APathEntry) entries[i];
				IPath[] exclusionPatterns = entry.getExclusionPatterns();
				if (exclusionPatterns.length > 0) {
					StringBuffer excludeRule = new StringBuffer(10);
					for (int j = 0, max = exclusionPatterns.length; j < max; j++) {
						if (j > 0) {
							excludeRule.append('|');
						}
						excludeRule.append(exclusionPatterns[j]);
					}
					element.setAttribute(ATTRIBUTE_EXCLUDING, excludeRule.toString());
				}
				if (entry.isRecursive()) {
					element.setAttribute(ATTRIBUTE_RECUSIVE, VALUE_TRUE);
				}
			}

			if (kind == IPathEntry.CDT_SOURCE) {
				ISourceEntry source = (ISourceEntry) entries[i];
				IPath path = source.getPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				IPath output = source.getOutputLocation();
				if (output != null && output.isEmpty()) {
					element.setAttribute(ATTRIBUTE_OUTPUT, output.toString());
				}
			} else if (kind == IPathEntry.CDT_LIBRARY) {
				ILibraryEntry lib = (ILibraryEntry) entries[i];
				IPath path = lib.getPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				if (lib.getSourceAttachmentPath() != null) {
					element.setAttribute(ATTRIBUTE_SOURCEPATH, lib.getSourceAttachmentPath().toString());
				}
				if (lib.getSourceAttachmentRootPath() != null) {
					element.setAttribute(ATTRIBUTE_ROOTPATH, lib.getSourceAttachmentRootPath().toString());
				}
				if (lib.getSourceAttachmentPrefixMapping() != null) {
					element.setAttribute(ATTRIBUTE_PREFIXMAPPING, lib.getSourceAttachmentPrefixMapping().toString());
				}
			} else if (kind == IPathEntry.CDT_PROJECT) {
				IProjectEntry pentry = (IProjectEntry) entries[i];
				IPath path = pentry.getPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
			} else if (kind == IPathEntry.CDT_INCLUDE) {
				IIncludeEntry include = (IIncludeEntry) entries[i];
				IPath path = include.getPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				IPath includePath = include.getIncludePath();
				element.setAttribute(ATTRIBUTE_INCLUDE, includePath.toString());
				if (include.isSystemInclude()) {
					element.setAttribute(ATTRIBUTE_SYSTEM, VALUE_TRUE);
				}
			} else if (kind == IPathEntry.CDT_MACRO) {
				IMacroEntry macro = (IMacroEntry) entries[i];
				IPath path = macro.getPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				element.setAttribute(ATTRIBUTE_NAME, macro.getMacroName());
				element.setAttribute(ATTRIBUTE_VALUE, macro.getMacroValue());
			} else if (kind == IPathEntry.CDT_CONTAINER) {
				IContainerEntry container = (IContainerEntry) entries[i];
				element.setAttribute(ATTRIBUTE_PATH, container.getPath().toString());
			}
			if (entries[i].isExported()) {
				element.setAttribute(ATTRIBUTE_EXPORTED, VALUE_TRUE);
			}
		}
	}

}
