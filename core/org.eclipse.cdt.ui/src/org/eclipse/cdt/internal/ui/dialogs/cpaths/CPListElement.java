/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class CPListElement {

	public static final String SOURCEATTACHMENT = "sourcepath"; //$NON-NLS-1$
	public static final String SOURCEATTACHMENTROOT = "rootpath"; //$NON-NLS-1$
	public static final String EXCLUSION = "exclusion"; //$NON-NLS-1$
	public static final String INCLUDE = "includepath"; //$NON-NLS-1$
	public static final String DEFINE = "define"; //$NON-NLS-1$

	private ICProject fProject;

	private int fEntryKind;
	private IPath fPath;
	private IResource fResource;
	private boolean fIsExported;
	private boolean fIsMissing;

	private CPListElement fParentContainer;

	private IPathEntry fCachedEntry;
	private ArrayList fChildren;

	public CPListElement(ICProject project, int entryKind, IPath path, IResource res) {
		fProject = project;

		fEntryKind = entryKind;
		fPath = path;
		fChildren = new ArrayList();
		fResource = res;
		fIsExported = false;

		fIsMissing = false;
		fCachedEntry = null;
		fParentContainer = null;

		switch (entryKind) {
			case IPathEntry.CDT_OUTPUT:
				createAttributeElement(EXCLUSION, new Path[0]);
				break;
			case IPathEntry.CDT_SOURCE:
				createAttributeElement(EXCLUSION, new Path[0]);
				break;
			case IPathEntry.CDT_LIBRARY:
				createAttributeElement(SOURCEATTACHMENT, null);
				break;
			case IPathEntry.CDT_PROJECT:
			case IPathEntry.CDT_INCLUDE:
				createAttributeElement(INCLUDE, null);
				break;
			case IPathEntry.CDT_MACRO:
				createAttributeElement(DEFINE, null);
				break;
			case IPathEntry.CDT_CONTAINER:
				try {
					IPathEntryContainer container = CoreModel.getDefault().getPathEntryContainer(fPath, fProject);
					if (container != null) {
						IPathEntry[] entries = container.getPathEntries();
						for (int i = 0; i < entries.length; i++) {
							CPListElement curr = createFromExisting(entries[i], fProject);
							curr.setParentContainer(this);
							fChildren.add(curr);
						}
					}
				} catch (CModelException e) {
				}
				break;
			default:
		}
	}

	public IPathEntry getPathEntry() {
		if (fCachedEntry == null) {
			fCachedEntry = newPathEntry();
		}
		return fCachedEntry;
	}

	private IPathEntry newPathEntry() {
		IPath[] exclusionPattern;
		switch (fEntryKind) {
			case IPathEntry.CDT_OUTPUT:
				exclusionPattern = (IPath[]) getAttribute(EXCLUSION);
				return CoreModel.newOutputEntry(fPath, exclusionPattern);
			case IPathEntry.CDT_SOURCE:
				exclusionPattern = (IPath[]) getAttribute(EXCLUSION);
				return CoreModel.newSourceEntry(fPath, exclusionPattern);
			case IPathEntry.CDT_LIBRARY:
				IPath attach = (IPath) getAttribute(SOURCEATTACHMENT);
				return CoreModel.newLibraryEntry(fPath, attach, null, null, isExported());
			case IPathEntry.CDT_PROJECT:
				return CoreModel.newProjectEntry(fPath, isExported());
			case IPathEntry.CDT_CONTAINER:
				return CoreModel.newContainerEntry(fPath, isExported());
			case IPathEntry.CDT_INCLUDE:
				exclusionPattern = (IPath[]) getAttribute(EXCLUSION);
				return CoreModel.newIncludeEntry(fPath, (IPath) getAttribute(INCLUDE));
			case IPathEntry.CDT_MACRO:
				exclusionPattern = (IPath[]) getAttribute(EXCLUSION);
				return CoreModel.newMacroEntry(fPath, (String) getAttribute(DEFINE), null);
			default:
				return null;
		}
	}
	
	public static StringBuffer appendEncodePath(IPath path, StringBuffer buf) {
		if (path != null) {
			String str= path.toString();
			buf.append('[').append(str.length()).append(']').append(str);
		} else {
			buf.append('[').append(']');
		}
		return buf;
	}
	
	/**
	 * @return
	 */
	public StringBuffer appendEncodedSettings(StringBuffer buf) {
		buf.append(fEntryKind).append(';');
		appendEncodePath(fPath, buf).append(';');
		buf.append(Boolean.valueOf(fIsExported)).append(';');
		switch (fEntryKind) {
			case IPathEntry.CDT_OUTPUT:
			case IPathEntry.CDT_SOURCE:
			case IPathEntry.CDT_INCLUDE:
			case IPathEntry.CDT_MACRO:
				IPath[] exclusion= (IPath[]) getAttribute(EXCLUSION);
				buf.append('[').append(exclusion.length).append(']');
				for (int i= 0; i < exclusion.length; i++) {
					appendEncodePath(exclusion[i], buf).append(';');
				}
				break;
			case IPathEntry.CDT_LIBRARY:
				IPath sourceAttach= (IPath) getAttribute(SOURCEATTACHMENT);
				appendEncodePath(sourceAttach, buf).append(';');
				break;
			default:
			
		}
		return buf;
	}

	/**
	 * Gets the path entry path.
	 * 
	 * @see IPathEntry#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}

	/**
	 * Gets the classpath entry kind.
	 * 
	 * @see IPathEntry#getEntryKind()
	 */
	public int getEntryKind() {
		return fEntryKind;
	}

	/**
	 * Entries without resource are either non existing or a variable entry
	 * External jars do not have a resource
	 */
	public IResource getResource() {
		return fResource;
	}

	public CPListElementAttribute setAttribute(String key, Object value) {
		CPListElementAttribute attribute = findAttributeElement(key);
		if (attribute == null) {
			return null;
		}
		attribute.setValue(value);
		attributeChanged(key);
		return attribute;
	}

	private CPListElementAttribute findAttributeElement(String key) {
		for (int i = 0; i < fChildren.size(); i++) {
			Object curr = fChildren.get(i);
			if (curr instanceof CPListElementAttribute) {
				CPListElementAttribute elem = (CPListElementAttribute) curr;
				if (key.equals(elem.getKey())) {
					return elem;
				}
			}
		}
		return null;
	}

	public Object getAttribute(String key) {
		CPListElementAttribute attrib = findAttributeElement(key);
		if (attrib != null) {
			return attrib.getValue();
		}
		return null;
	}

	private void createAttributeElement(String key, Object value) {
		fChildren.add(new CPListElementAttribute(this, key, value));
	}

	public Object[] getChildren() {
		if (fEntryKind == IPathEntry.CDT_OUTPUT || fEntryKind == IPathEntry.CDT_SOURCE || fEntryKind == IPathEntry.CDT_INCLUDE || fEntryKind == IPathEntry.CDT_MACRO) {

			return new Object[] { findAttributeElement(EXCLUSION)};

		}
		return fChildren.toArray();
	}

	private void setParentContainer(CPListElement element) {
		fParentContainer = element;
	}

	public CPListElement getParentContainer() {
		return fParentContainer;
	}

	private void attributeChanged(String key) {
		fCachedEntry = null;
	}

	/*
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other != null && other.getClass().equals(getClass())) {
			CPListElement elem = (CPListElement) other;
			return elem.fEntryKind == fEntryKind && elem.fPath.equals(fPath);
		}
		return false;
	}

	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fPath.hashCode() + fEntryKind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getPathEntry().toString();
	}

	/**
	 * Returns if a entry is missing.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isMissing() {
		return fIsMissing;
	}

	/**
	 * Sets the 'missing' state of the entry.
	 */
	public void setIsMissing(boolean isMissing) {
		fIsMissing = isMissing;
	}

	/**
	 * Returns if a entry is exported (only applies to libraries)
	 * 
	 * @return Returns a boolean
	 */
	public boolean isExported() {
		return fIsExported;
	}

	/**
	 * Sets the export state of the entry.
	 */
	public void setExported(boolean isExported) {
		if (isExported != fIsExported) {
			fIsExported = isExported;

			attributeChanged(null);
		}
	}

	/**
	 * Gets the project.
	 * 
	 * @return Returns a ICProject
	 */
	public ICProject getCProject() {
		return fProject;
	}

	public static CPListElement createFromExisting(IPathEntry curr, ICProject project) {
		IPath path = curr.getPath();
		IWorkspaceRoot root = project.getProject().getWorkspace().getRoot();
		IPath sourceAttachment = null;
		IPath[] exclusion = null;
		// get the resource
		IResource res = null;
		boolean isMissing = false;
		//		URL javaDocLocation = null;

		switch (curr.getEntryKind()) {
			case IPathEntry.CDT_CONTAINER:
				res = null;
				try {
					isMissing = (CoreModel.getDefault().getPathEntryContainer(path, project) == null);
				} catch (CModelException e) {
				}
				break;
			case IPathEntry.CDT_LIBRARY:
				res = root.findMember(path);
				if (res == null) {
					if (!ArchiveFileFilter.isArchivePath(path)) {
						if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()
								&& root.getProject(path.segment(0)).exists()) {
							res = root.getFolder(path);
						}
					}
					isMissing = !path.toFile().isFile(); // look for external
				}
				sourceAttachment = ((ILibraryEntry) curr).getSourceAttachmentPath();
				break;
			case IPathEntry.CDT_SOURCE:
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
					isMissing = true;
				}
				exclusion = ((ISourceEntry) curr).getExclusionPatterns();
				break;
			case IPathEntry.CDT_OUTPUT:
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
					isMissing = true;
				}
				exclusion = ((IOutputEntry) curr).getExclusionPatterns();
				break;
			case IPathEntry.CDT_INCLUDE:
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
					isMissing = !path.toFile().isFile(); // look for external
				}
				exclusion = ((IIncludeEntry) curr).getExclusionPatterns();
				break;
			case IPathEntry.CDT_MACRO:
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
					isMissing = !path.toFile().isFile(); // look for external
				}
				exclusion = ((IMacroEntry) curr).getExclusionPatterns();
				break;
			case IPathEntry.CDT_PROJECT:
				res = root.findMember(path);
				isMissing = (res == null);
				break;
		}
		CPListElement elem = new CPListElement(project, curr.getEntryKind(), path, res);
		elem.setAttribute(SOURCEATTACHMENT, sourceAttachment);
		elem.setAttribute(EXCLUSION, exclusion);
		elem.setExported(curr.isExported());

		if (project.exists()) {
			elem.setIsMissing(isMissing);
		}
		return elem;
	}
}
