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
import org.eclipse.cdt.core.model.CPathContainerInitializer;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICPathContainer;
import org.eclipse.cdt.core.model.ICPathEntry;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author alain
 *  
 */
public class CPathEntryManager {

	static String CONTAINER_INITIALIZER_EXTPOINT_ID = "cpathContainerInitializer"; //$NON-NLS-1$
	static String PATH_ENTRY = "cpathentry"; //$NON-NLS-1$
	static String PATH_ENTRY_ID = "org.eclipse.cdt.core.cpathentry"; //$NON-NLS-1$
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
	static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	static String VALUE_TRUE = "true"; //$NON-NLS-1$

	/**
	 * Classpath containers pool
	 */
	public static HashMap Containers = new HashMap(5);
	public static HashMap PreviousSessionContainers = new HashMap(5);

	HashMap projectMap = new HashMap();
	final ICPathEntry[] EMPTY = new ICPathEntry[0];
	public final static ICPathContainer ContainerInitializationInProgress = new ICPathContainer() {
		public ICPathEntry[] getCPathEntries() {
			return null;
		}
		public String getDescription() {
			return "Container Initialization In Progress";
		} //$NON-NLS-1$
	};

	public ICPathEntry[] getResolvedEntries(ICProject cproject) throws CModelException {
		ICPathEntry[] entries = (ICPathEntry[]) projectMap.get(cproject);
		if (entries == null) {
			entries = getRawCPathEntries(cproject);
			ArrayList list = new ArrayList();
			for (int i = 0; i < entries.length; i++) {
				ICPathEntry entry = entries[i];
				// Expand the containers.
				if (entry.getEntryKind() == ICPathEntry.CDT_CONTAINER) {
					ICPathContainer container = getCPathContainer((IContainerEntry)entry, cproject);
					if (container != null) {
						ICPathEntry[] containerEntries = container.getCPathEntries();
						if (containerEntries != null) {
							for (int j = 0; j < containerEntries.length; j++) {
								ICPathEntry cEntry = containerEntries[i];
								if (cEntry.isExported()) {
									list.add(cEntry);
								}
							}
						}
					}
				} else {
					list.add(entry);
				}
			}
			entries = new ICPathEntry[list.size()];
			list.toArray(entries);
			projectMap.put(cproject, entries);
		}
		return entries;
	}

	public void setRawCPathEntries(ICProject cproject, ICPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		//try {
		//	SetCPathEntriesOperation op = new SetCPathEntriesOperation(cproject, getRawCPathEntries(cproject), newEntries);
		//	runOperation(op, monitor);
		//} catch (CoreException e) {
		//	throw new CModelException(e);
		//}
		
		try {
			ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(cproject.getProject());
			Element rootElement = descriptor.getProjectData(CProject.PATH_ENTRY_ID);
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}

			// Save the entries
			if (newEntries != null && newEntries.length > 0) {
				// Serialize the include paths
				Document doc = rootElement.getOwnerDocument();
				encodeCPathEntries(cproject.getProject().getFullPath(), doc, rootElement, newEntries);
				descriptor.saveProjectData();
			}
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public ICPathEntry[] getRawCPathEntries(ICProject cproject) throws CModelException {
		ArrayList pathEntries = new ArrayList();
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(cproject.getProject());
			Element element = cdesc.getProjectData(PATH_ENTRY_ID);
			NodeList list = element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node childNode = list.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					if (childNode.getNodeName().equals(PATH_ENTRY)) {
						pathEntries.add(decodeCPathEntry(cproject, (Element) childNode));
					}
				}
			}
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return (ICPathEntry[]) pathEntries.toArray(new ICPathEntry[0]);
	}

	public void setCPathContainer(
		final IPath containerPath,
		ICProject[] affectedProjects,
		ICPathContainer[] respectiveContainers,
		IProgressMonitor monitor)
		throws CModelException {

		if (affectedProjects.length != respectiveContainers.length)
			Assert.isTrue(false, "Projects and containers collections should have the same size"); //$NON-NLS-1$

		if (monitor != null && monitor.isCanceled())
			return;

		final int projectLength = affectedProjects.length;
		final ICProject[] modifiedProjects = new ICProject[projectLength];
		System.arraycopy(affectedProjects, 0, modifiedProjects, 0, projectLength);
		ICPathEntry[][] oldResolvedPaths = new ICPathEntry[projectLength][];

		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++) {

			if (monitor != null && monitor.isCanceled())
				return;

			ICProject affectedProject = affectedProjects[i];
			ICPathContainer newContainer = respectiveContainers[i];

			if (newContainer == null)
				newContainer = ContainerInitializationInProgress; // 30920 - prevent infinite loop

			boolean found = false;
			if (CoreModel.getDefault().hasCCNature(affectedProject.getProject())) {
				ICPathEntry[] rawCPath = affectedProject.getRawCPathEntries();
				for (int j = 0, cpLength = rawCPath.length; j < cpLength; j++) {
					ICPathEntry entry = rawCPath[j];
					if (entry.getEntryKind() == ICPathEntry.CDT_CONTAINER) {
						IContainerEntry cont = (IContainerEntry) entry;
						if (cont.getId().equals(containerPath.segment(0))) {
							found = true;
							break;
						}
					}
				}
			}
			if (!found) {
				modifiedProjects[i] = null;
				// filter out this project - does not reference the container path, or isnt't yet Java project
				containerPut(affectedProject, containerPath, newContainer);
				continue;
			}
			ICPathContainer oldContainer = containerGet(affectedProject, containerPath);
			if (oldContainer == ContainerInitializationInProgress) {
				Map previousContainerValues = (Map) PreviousSessionContainers.get(affectedProject);
				if (previousContainerValues != null) {
					ICPathContainer previousContainer = (ICPathContainer) previousContainerValues.get(containerPath);
					if (previousContainer != null) {
						if (true) {
							System.out.println("CPContainer INIT - reentering access to project container: [" + affectedProject.getElementName() + "] " + containerPath + " during its initialization, will see previous value: " + previousContainer.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
						containerPut(affectedProject, containerPath, previousContainer);
					}
					oldContainer = null;
					//33695 - cannot filter out restored container, must update affected project to reset cached CP
				} else {
					oldContainer = null;
				}
			}
			if (oldContainer != null && oldContainer.equals(respectiveContainers[i])) {
				modifiedProjects[i] = null; // filter out this project - container did not change
				continue;
			}
			remaining++;
			oldResolvedPaths[i] = affectedProject.getResolvedCPathEntries();
			containerPut(affectedProject, containerPath, newContainer);
		}

		// Nothing change.
		if (remaining == 0)
			return;

		// trigger model refresh
		try {
			//final boolean canChangeResources = !ResourcesPlugin.getWorkspace().isTreeLocked();
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor progressMonitor) throws CoreException {
					for (int i = 0; i < projectLength; i++) {

						if (progressMonitor != null && progressMonitor.isCanceled())
							return;

						ICProject affectedProject = (ICProject) modifiedProjects[i];
						if (affectedProject == null)
							continue; // was filtered out

						if (true) {
							System.out.println("CPContainer SET  - updating affected project: [" + affectedProject.getElementName() + "] due to setting container: " + containerPath); //$NON-NLS-1$ //$NON-NLS-2$
						}

						// force a refresh of the affected project (will compute deltas)
						affectedProject.setRawCPathEntries(affectedProject.getRawCPathEntries(), progressMonitor);
					}
				}
			}, monitor);
		} catch (CoreException e) {
			if (true) {
				System.out.println("CPContainer SET  - FAILED DUE TO EXCEPTION: " + containerPath); //$NON-NLS-1$
				e.printStackTrace();
			}
			if (e instanceof CModelException) {
				throw (CModelException) e;
			} else {
				throw new CModelException(e);
			}
		} finally {
			for (int i = 0; i < projectLength; i++) {
				if (respectiveContainers[i] == null) {
					containerPut(affectedProjects[i], containerPath, null); // reset init in progress marker
				}
			}
		}

	}

	public ICPathContainer getCPathContainer(IContainerEntry entry, ICProject cproject) {
		return null;
	}

	public static String[] getRegisteredContainerIDs() {
		Plugin cdtCorePlugin = CCorePlugin.getDefault();
		if (cdtCorePlugin == null)
			return null;

		ArrayList containerIDList = new ArrayList(5);
		IExtensionPoint extension = cdtCorePlugin.getDescriptor().getExtensionPoint(CONTAINER_INITIALIZER_EXTPOINT_ID);
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

	/**
	 * Helper method finding the classpath container initializer registered for a given classpath container ID or <code>null</code>
	 * if none was found while iterating over the contributions to extension point to the extension point
	 * "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to identify the registered container initializer.
	 * <p>
	 * 
	 * @param containerID -
	 *            a containerID identifying a registered initializer
	 * @return ClasspathContainerInitializer - the registered classpath container initializer or <code>null</code> if none was
	 *         found.
	 * @since 2.1
	 */
	public static CPathContainerInitializer getCPathContainerInitializer(String containerID) {

		Plugin cdtCorePlugin = CCorePlugin.getDefault();
		if (cdtCorePlugin == null)
			return null;

		IExtensionPoint extension = cdtCorePlugin.getDescriptor().getExtensionPoint(CONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String initializerID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(containerID)) {
						if (true) {
							System.out.println("CPContainer INIT - found initializer: " + containerID + " --> " + configElements[j].getAttribute("class")); //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
						}
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof CPathContainerInitializer) {
								return (CPathContainerInitializer) execExt;
							}
						} catch (CoreException e) {
							// executable extension could not be created: ignore this initializer if
							System.out.println("CPContainer INIT - failed to instanciate initializer: " + containerID + " --> " + configElements[j].getAttribute("class")); //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
							e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}

	public static ICPathContainer containerGet(ICProject project, IPath containerPath) {
		Map projectContainers = (Map) Containers.get(project);
		if (projectContainers == null) {
			return null;
		}
		ICPathContainer container = (ICPathContainer) projectContainers.get(containerPath);
		return container;
	}

	public static void containerPut(ICProject project, IPath containerPath, ICPathContainer container) {

		Map projectContainers = (Map) Containers.get(project);
		if (projectContainers == null) {
			projectContainers = new HashMap(1);
			Containers.put(project, projectContainers);
		}

		if (container == null) {
			projectContainers.remove(containerPath);
			Map previousContainers = (Map) PreviousSessionContainers.get(project);
			if (previousContainers != null) {
				previousContainers.remove(containerPath);
			}
		} else {
			projectContainers.put(containerPath, container);
		}

		// do not write out intermediate initialization value
		if (container == ContainerInitializationInProgress) {
			return;
		}
	}

	ICPathEntry decodeCPathEntry(ICProject cProject, Element element) throws CModelException {
		IPath projectPath = cProject.getProject().getFullPath();

		// kind
		String kindAttr = element.getAttribute(ATTRIBUTE_KIND);
		int kind = CPathEntry.kindFromString(kindAttr);

		// exported flag
		boolean isExported = false;
		if (element.hasAttribute(ATTRIBUTE_EXPORTED)) {
			isExported = element.getAttribute(ATTRIBUTE_EXPORTED).equals(VALUE_TRUE);
		}

		// ensure path is absolute
		String pathAttr = element.getAttribute(ATTRIBUTE_PATH);
		IPath path = new Path(pathAttr);
		if (kind != ICPathEntry.CDT_VARIABLE && !path.isAbsolute()) {
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
		IPath[] exclusionPatterns = ACPathEntry.NO_EXCLUSION_PATTERNS;
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

			case ICPathEntry.CDT_PROJECT :
				return CoreModel.newProjectEntry(path, isExported);

			case ICPathEntry.CDT_LIBRARY :
				return CoreModel.newLibraryEntry(
					path,
					sourceAttachmentPath,
					sourceAttachmentRootPath,
					sourceAttachmentPrefixMapping,
					isExported);

			case ICPathEntry.CDT_SOURCE :
				{
					// custom output location
					IPath outputLocation = element.hasAttribute(ATTRIBUTE_OUTPUT) ? projectPath.append(element.getAttribute(ATTRIBUTE_OUTPUT)) : null; //$NON-NLS-1$ //$NON-NLS-2$
					// must be an entry in this project or specify another
					// project
					String projSegment = path.segment(0);
					if (projSegment != null && projSegment.equals(cProject.getElementName())) { // this project
						return CoreModel.newSourceEntry(path, outputLocation, isRecursive, exclusionPatterns);
					} else { // another project
						return CoreModel.newProjectEntry(path, isExported);
					}
				}

				//			case ICPathEntry.CDT_VARIABLE :
				//				return CoreModel.newVariableEntry(path,
				// sourceAttachmentPath, sourceAttachmentRootPath);

			case ICPathEntry.CDT_INCLUDE :
				{
					// include path info (optional
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
						exclusionPatterns,
						isExported);
				}

			case ICPathEntry.CDT_MACRO :
				{
					String macroName = element.getAttribute(ATTRIBUTE_NAME); //$NON-NLS-1$
					String macroValue = element.getAttribute(ATTRIBUTE_VALUE); //$NON-NLS-1$
					return CoreModel.newMacroEntry(path, macroName, macroValue, isRecursive, exclusionPatterns, isExported);
				}

			case ICPathEntry.CDT_CONTAINER :
				{
					String id = element.getAttribute(ATTRIBUTE_ID); //$NON-NLS-1$
					return CoreModel.newContainerEntry(id, isExported);
				}

			default :
				{
					ICModelStatus status = new CModelStatus(ICModelStatus.ERROR, "CPathEntry: unknown kind (" + kindAttr + ")"); //$NON-NLS-1$
					throw new CModelException(status);
				}
		}
	}

	void encodeCPathEntries(IPath projectPath, Document doc, Element configRootElement, ICPathEntry[] entries) {
		Element element;
		//IPath projectPath = getProject().getFullPath();
		for (int i = 0; i < entries.length; i++) {
			element = doc.createElement(PATH_ENTRY);
			configRootElement.appendChild(element);
			int kind = entries[i].getEntryKind();

			// Set the kind
			element.setAttribute(ATTRIBUTE_KIND, CPathEntry.kindToString(kind));

			// Save the exclusions attributes
			if (entries[i] instanceof ACPathEntry) {
				ACPathEntry entry = (ACPathEntry) entries[i];
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

			if (kind == ICPathEntry.CDT_SOURCE) {
				ISourceEntry source = (ISourceEntry) entries[i];
				IPath path = source.getSourcePath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				IPath output = source.getOutputLocation();
				if (output != null && output.isEmpty()) {
					element.setAttribute(ATTRIBUTE_OUTPUT, output.toString());
				}
			} else if (kind == ICPathEntry.CDT_LIBRARY) {
				ILibraryEntry lib = (ILibraryEntry) entries[i];
				IPath path = lib.getLibraryPath();
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
			} else if (kind == ICPathEntry.CDT_PROJECT) {
				IProjectEntry pentry = (IProjectEntry) entries[i];
				IPath path = pentry.getProjectPath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
			} else if (kind == ICPathEntry.CDT_INCLUDE) {
				IIncludeEntry include = (IIncludeEntry) entries[i];
				IPath path = include.getResourcePath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				IPath includePath = include.getIncludePath();
				element.setAttribute(ATTRIBUTE_INCLUDE, includePath.toString());
				if (include.isSystemInclude()) {
					element.setAttribute(ATTRIBUTE_SYSTEM, VALUE_TRUE);
				}
			} else if (kind == ICPathEntry.CDT_MACRO) {
				IMacroEntry macro = (IMacroEntry) entries[i];
				IPath path = macro.getResourcePath();
				element.setAttribute(ATTRIBUTE_PATH, path.toString());
				element.setAttribute(ATTRIBUTE_NAME, macro.getMacroName());
				element.setAttribute(ATTRIBUTE_VALUE, macro.getMacroValue());
			} else if (kind == ICPathEntry.CDT_CONTAINER) {
				IContainerEntry container = (IContainerEntry) entries[i];
				element.setAttribute(ATTRIBUTE_ID, container.getId());
			}
			if (entries[i].isExported()) {
				element.setAttribute(ATTRIBUTE_EXPORTED, VALUE_TRUE);
			}
		}
	}

}
