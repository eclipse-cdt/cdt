/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * PathEntryStore
 */
public class DefaultPathEntryStore implements IPathEntryStore, ICDescriptorListener {

	static String PATH_ENTRY = "pathentry"; //$NON-NLS-1$
	static String PATH_ENTRY_ID = "org.eclipse.cdt.core.pathentry"; //$NON-NLS-1$
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
	static String ATTRIBUTE_LIBRARY = "library"; //$NON-NLS-1$
	static String ATTRIBUTE_SYSTEM = "system"; //$NON-NLS-1$
	static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	static String VALUE_TRUE = "true"; //$NON-NLS-1$

	static final IPathEntry[] NO_PATHENTRIES = new IPathEntry[0];

	List listeners;
	IProject fProject;
	
	/**
	 * 
	 */
	public DefaultPathEntryStore(IProject project) {
		fProject = project;
		listeners = Collections.synchronizedList(new ArrayList());
		// Register the Core Model on the Descriptor
		// Manager, it needs to know about changes.
		CCorePlugin.getDefault().getCDescriptorManager().addDescriptorListener(this);
	}

	public IPathEntry[] getRawPathEntries() throws CoreException {
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(fProject, false);
		if (cdesc != null) {
			ArrayList pathEntries = new ArrayList();
			Element element = cdesc.getProjectData(PATH_ENTRY_ID);
			NodeList list = element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node childNode = list.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					if (childNode.getNodeName().equals(PATH_ENTRY)) {
						pathEntries.add(decodePathEntry(fProject, (Element) childNode));
					}
				}
			}
			IPathEntry[] entries = new IPathEntry[pathEntries.size()]; 
			pathEntries.toArray(entries);
			return entries;
		}
		return NO_PATHENTRIES;
	}

	public void setRawPathEntries(IPathEntry[] newRawEntries) throws CoreException {
		if (Arrays.equals(newRawEntries, getRawPathEntries())) {
			return;
		}	
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
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
			encodePathEntries(fProject.getFullPath(), doc, rootElement, newRawEntries);
		}
		descriptor.saveProjectData();
	}

	static IPathEntry decodePathEntry(IProject project, Element element) throws CModelException {
		IPath projectPath = project.getFullPath();
		
		// kind
		String kindAttr = element.getAttribute(ATTRIBUTE_KIND);
		int kind = PathEntry.kindFromString(kindAttr);
		
		// exported flag
		boolean isExported = false;
		if (element.hasAttribute(ATTRIBUTE_EXPORTED)) {
			isExported = element.getAttribute(ATTRIBUTE_EXPORTED).equals(VALUE_TRUE);
		}

		// get path and ensure it is absolute
		IPath path;
		if (element.hasAttribute(ATTRIBUTE_PATH)) {
			path = new Path(element.getAttribute(ATTRIBUTE_PATH));
		} else {
			path = new Path(""); //$NON-NLS-1$
		}
		if (!path.isAbsolute()) {
			path = projectPath.append(path);
		}

		// check fo the base path
		IPath basePath = new Path(element.getAttribute(ATTRIBUTE_BASE_PATH));

		// get the base ref
		IPath baseRef = new Path(element.getAttribute(ATTRIBUTE_BASE_REF));

		// exclusion patterns (optional)
		String exclusion = element.getAttribute(ATTRIBUTE_EXCLUDING);
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
				IPath libraryPath = new Path(element.getAttribute(ATTRIBUTE_LIBRARY));				
				// source attachment info (optional)
				IPath sourceAttachmentPath = element.hasAttribute(ATTRIBUTE_SOURCEPATH) ? new Path(
						element.getAttribute(ATTRIBUTE_SOURCEPATH)) : null;
				IPath sourceAttachmentRootPath = element.hasAttribute(ATTRIBUTE_ROOTPATH) ? new Path(
						element.getAttribute(ATTRIBUTE_ROOTPATH)) : null;
				IPath sourceAttachmentPrefixMapping = element.hasAttribute(ATTRIBUTE_PREFIXMAPPING) ? new Path(
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
				} else { // another project
					return CoreModel.newProjectEntry(path, isExported);
				}
			}
			case IPathEntry.CDT_OUTPUT :
				return CoreModel.newOutputEntry(path, exclusionPatterns);
			case IPathEntry.CDT_INCLUDE : {
				// include path info
				IPath includePath = new Path(element.getAttribute(ATTRIBUTE_INCLUDE));
				// isSysteminclude
				boolean isSystemInclude = false;
				if (element.hasAttribute(ATTRIBUTE_SYSTEM)) {
					isSystemInclude = element.getAttribute(ATTRIBUTE_SYSTEM).equals(VALUE_TRUE);
				}
				if (baseRef != null && !baseRef.isEmpty()) {
					return CoreModel.newIncludeRefEntry(path, baseRef, includePath);
				}
				return CoreModel.newIncludeEntry(path, basePath, includePath, isSystemInclude, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_MACRO : {
				String macroName = element.getAttribute(ATTRIBUTE_NAME);
				String macroValue = element.getAttribute(ATTRIBUTE_VALUE);
				if (baseRef != null && !baseRef.isEmpty()) {
					return CoreModel.newMacroRefEntry(path, baseRef, macroName);
				}
				return CoreModel.newMacroEntry(path, macroName, macroValue, exclusionPatterns, isExported);
			}
			case IPathEntry.CDT_CONTAINER : {
				IPath id = new Path(element.getAttribute(ATTRIBUTE_PATH));
				return CoreModel.newContainerEntry(id, isExported);
			}
			default : {
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

			// translate the project prefix.
			IPath xmlPath = entries[i].getPath();
			if (xmlPath == null) {
				xmlPath = new Path(""); //$NON-NLS-1$
			}
			if (kind != IPathEntry.CDT_CONTAINER) {
				// translate to project relative from absolute (unless a device path)
				if (xmlPath.isAbsolute()) {
					if (projectPath != null && projectPath.isPrefixOf(xmlPath)) {
						if (xmlPath.segment(0).equals(projectPath.segment(0))) {
							xmlPath = xmlPath.removeFirstSegments(1);
							xmlPath = xmlPath.makeRelative();
						} else {
							xmlPath = xmlPath.makeAbsolute();
						}
					}
				}
			}

			// Save the path
			element.setAttribute(ATTRIBUTE_PATH, xmlPath.toString());

			// Specifics to the entries
			switch(kind) {
				case IPathEntry.CDT_SOURCE:
				case IPathEntry.CDT_OUTPUT:
				case IPathEntry.CDT_PROJECT:
				case IPathEntry.CDT_CONTAINER:
					break;
				case IPathEntry.CDT_LIBRARY: {
					ILibraryEntry lib = (ILibraryEntry) entries[i];
					IPath libraryPath = lib.getLibraryPath();
					element.setAttribute(ATTRIBUTE_LIBRARY, libraryPath.toString());
					IPath sourcePath = lib.getSourceAttachmentPath();
					if (sourcePath != null) {
						// translate to project relative from absolute 
						if (projectPath != null && projectPath.isPrefixOf(sourcePath)) {
							if (sourcePath.segment(0).equals(projectPath.segment(0))) {
								sourcePath = sourcePath.removeFirstSegments(1);
								sourcePath = sourcePath.makeRelative();
							}
						}
						element.setAttribute(ATTRIBUTE_SOURCEPATH, sourcePath.toString());
					}
					if (lib.getSourceAttachmentRootPath() != null) {
						element.setAttribute(ATTRIBUTE_ROOTPATH, lib.getSourceAttachmentRootPath().toString());
					}
					if (lib.getSourceAttachmentPrefixMapping() != null) {
						element.setAttribute(ATTRIBUTE_PREFIXMAPPING, lib.getSourceAttachmentPrefixMapping().toString());
					}
					break;
				}
				case IPathEntry.CDT_INCLUDE: {
					IIncludeEntry include = (IIncludeEntry) entries[i];
					IPath includePath = include.getIncludePath();
					element.setAttribute(ATTRIBUTE_INCLUDE, includePath.toString());
					if (include.isSystemInclude()) {
						element.setAttribute(ATTRIBUTE_SYSTEM, VALUE_TRUE);
					}
					break;
				}
				case IPathEntry.CDT_MACRO: {
					IMacroEntry macro = (IMacroEntry) entries[i];
					element.setAttribute(ATTRIBUTE_NAME, macro.getMacroName());
					element.setAttribute(ATTRIBUTE_VALUE, macro.getMacroValue());
					break;
				}
			}

			if (entries[i] instanceof APathEntry) {
				APathEntry entry = (APathEntry) entries[i];

				// save the basePath or the baseRef
				IPath basePath = entry.getBasePath();
				IPath baseRef = entry.getBaseReference();
				if (basePath != null && !basePath.isEmpty()) {
					element.setAttribute(ATTRIBUTE_BASE_PATH, basePath.toString());
				} else if (baseRef != null && !baseRef.isEmpty()) {
					element.setAttribute(ATTRIBUTE_BASE_REF, baseRef.toString());
				}

				// Save the exclusions attributes
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
			}

			// Save the export attribute
			if (entries[i].isExported()) {
				element.setAttribute(ATTRIBUTE_EXPORTED, VALUE_TRUE);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.ICDescriptorListener#descriptorChanged(org.eclipse.cdt.core.CDescriptorEvent)
	 */
	public void descriptorChanged(CDescriptorEvent event) {
		if (event.getType() == CDescriptorEvent.CDTPROJECT_CHANGED) {
			ICDescriptor cdesc = event.getDescriptor();
			if (cdesc != null && cdesc.getProject() == fProject){
				// Call the listeners.
				fireContentChangedEvent(fProject);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#addPathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	public void addPathEntryStoreListener(IPathEntryStoreListener listener) {		
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#removePathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	public void removePathEntryStoreListener(IPathEntryStoreListener listener) {
		listeners.remove(listener);
	}

	private void fireContentChangedEvent(IProject project) {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, project, PathEntryStoreChangedEvent.CONTENT_CHANGED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[listeners.size()];
		listeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#fireClosedChangedEvent(IProject)
	 */
	public void close() {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, fProject, PathEntryStoreChangedEvent.STORE_CLOSED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[listeners.size()];
		listeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
		CCorePlugin.getDefault().getCDescriptorManager().removeDescriptorListener(this);
	}

	public IProject getProject() {
		return fProject;
	}

	public ICExtensionReference getExtensionReference() {
		return null;
	}
}
