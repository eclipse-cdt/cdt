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
	public static final String SYSTEM_INCLUDE = "systeminclude"; //$NON-NLS-1$
	public static final String MACRO_NAME = "macroname"; //$NON-NLS-1$
	public static final String MACRO_VALUE = "macrovalue"; //$NON-NLS-1$
	public static final String BASE_REF = "baseref"; //$NON-NLS-1$
	public static final String BASE = "base"; //$NON-NLS-1$

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
			case IPathEntry.CDT_INCLUDE:
				createAttributeElement(INCLUDE, new Path("")); //$NON-NLS-1$
				createAttributeElement(EXCLUSION, new Path[0]);
				createAttributeElement(SYSTEM_INCLUDE, Boolean.valueOf(false));
				createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
				createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
				break;
			case IPathEntry.CDT_MACRO:
				createAttributeElement(MACRO_NAME, ""); //$NON-NLS-1$
				createAttributeElement(MACRO_VALUE, ""); //$NON-NLS-1$
				createAttributeElement(EXCLUSION, new Path[0]);
				createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
				createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
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
		IPath[] exclusionPattern = (IPath[]) getAttribute(EXCLUSION);
		IPath base = (IPath) getAttribute(BASE);
		IPath baseRef = (IPath) getAttribute(BASE_REF);
		switch (fEntryKind) {
			case IPathEntry.CDT_OUTPUT:
				return CoreModel.newOutputEntry(fPath, exclusionPattern);
			case IPathEntry.CDT_SOURCE:
				return CoreModel.newSourceEntry(fPath, exclusionPattern);
			case IPathEntry.CDT_LIBRARY:
				IPath attach = (IPath) getAttribute(SOURCEATTACHMENT);
				return CoreModel.newLibraryEntry(fPath, base, attach, null, null, isExported());
			case IPathEntry.CDT_PROJECT:
				return CoreModel.newProjectEntry(fPath, isExported());
			case IPathEntry.CDT_CONTAINER:
				return CoreModel.newContainerEntry(fPath, isExported());
			case IPathEntry.CDT_INCLUDE:
				if (base != null) {
					return CoreModel.newIncludeEntry(fPath, (IPath) getAttribute(INCLUDE), base,
							((Boolean) getAttribute(SYSTEM_INCLUDE)).booleanValue(), exclusionPattern);
				} else {

				}
			case IPathEntry.CDT_MACRO:
				if (base != null) {
					return CoreModel.newMacroEntry(fPath, base, (String) getAttribute(MACRO_NAME),
							(String) getAttribute(MACRO_VALUE), exclusionPattern);
				} else {

				}
			default:
				return null;
		}
	}

	public static StringBuffer appendEncodePath(IPath path, StringBuffer buf) {
		if (path != null) {
			String str = path.toString();
			buf.append('[').append(str.length()).append(']').append(str);
		} else {
			buf.append('[').append(']');
		}
		return buf.append(';');
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
				IPath[] exclusion = (IPath[]) getAttribute(EXCLUSION);
				buf.append('[').append(exclusion.length).append(']');
				for (int i = 0; i < exclusion.length; i++) {
					appendEncodePath(exclusion[i], buf);
				}
				switch (fEntryKind) {
					case IPathEntry.CDT_INCLUDE:
						IPath baseRef = (IPath) getAttribute(BASE_REF);
						appendEncodePath(baseRef, buf);
						IPath base = (IPath) getAttribute(BASE);
						appendEncodePath(base, buf);
						IPath include = (IPath) getAttribute(INCLUDE);
						appendEncodePath(include, buf);
						break;
					case IPathEntry.CDT_MACRO:
						baseRef = (IPath) getAttribute(BASE_REF);
						appendEncodePath(baseRef, buf);
						base = (IPath) getAttribute(BASE);
						appendEncodePath(base, buf);
						String symbol = (String) getAttribute(MACRO_NAME);
						buf.append(symbol).append(';');
					default:
				}
				break;
			case IPathEntry.CDT_LIBRARY:
				IPath base = (IPath) getAttribute(BASE);
				appendEncodePath(base, buf);
				IPath sourceAttach = (IPath) getAttribute(SOURCEATTACHMENT);
				appendEncodePath(sourceAttach, buf);
				break;
			default:
		}
		buf.setLength(buf.length()-1);
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
		if (fEntryKind == IPathEntry.CDT_OUTPUT || fEntryKind == IPathEntry.CDT_SOURCE) {
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
			if (elem.fEntryKind != fEntryKind || !elem.fPath.equals(fPath)) {
				return false;
			}
			switch (fEntryKind) {
				case IPathEntry.CDT_LIBRARY:
					return getAttribute(BASE).equals(elem.getAttribute(BASE));
				case IPathEntry.CDT_INCLUDE:
					return (getAttribute(INCLUDE).equals(elem.getAttribute(INCLUDE))
							&& getAttribute(BASE_REF).equals(elem.getAttribute(BASE_REF)) && getAttribute(BASE).equals(
							elem.getAttribute(BASE)));
				case IPathEntry.CDT_MACRO:
					return (getAttribute(MACRO_NAME).equals(elem.getAttribute(MACRO_NAME))
							&& getAttribute(BASE_REF).equals(elem.getAttribute(BASE_REF)) && getAttribute(BASE).equals(
							elem.getAttribute(BASE)));
			}
			return true;
		}
		return false;
	}

	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		int hashCode = 0;
		switch (fEntryKind) {
			case IPathEntry.CDT_LIBRARY:
				hashCode = getAttribute(BASE).hashCode();
			case IPathEntry.CDT_INCLUDE:
				hashCode = getAttribute(INCLUDE).hashCode() + getAttribute(BASE_REF).hashCode() + getAttribute(BASE).hashCode();
			case IPathEntry.CDT_MACRO:
				hashCode = getAttribute(MACRO_NAME).hashCode() + getAttribute(BASE_REF).hashCode() + getAttribute(BASE).hashCode();
		}
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
		IPath include = null;
		String macroName = null;
		String macroValue = null;
		boolean sysInclude = false;
		IPath baseRef = null;
		IPath base = null;

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
					//					if (!ArchiveFileFilter.isArchivePath(path)) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()
							&& root.getProject(path.segment(0)).exists()) {
						res = root.getFolder(path);
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
				}
				if (res.getType() != IResource.PROJECT) {
					isMissing = !project.isOnSourceRoot(res);
				}
				exclusion = ((IIncludeEntry) curr).getExclusionPatterns();
				sysInclude = ((IIncludeEntry) curr).isSystemInclude();
				baseRef = ((IIncludeEntry) curr).getBasePath();
				base = new Path("");
				//				base = ((IIncludeEntry) curr).getBasePath();
				include = ((IIncludeEntry) curr).getIncludePath();
				break;
			case IPathEntry.CDT_MACRO:
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
				}
				if (res.getType() != IResource.PROJECT) {
					isMissing = !project.isOnSourceRoot(res);
				}
				exclusion = ((IMacroEntry) curr).getExclusionPatterns();
				macroName = ((IMacroEntry) curr).getMacroName();
				macroValue = ((IMacroEntry) curr).getMacroValue();
				baseRef = ((IMacroEntry) curr).getBasePath();
				base = new Path("");
				//				base = ((IIncludeEntry) curr).getBasePath();
				break;
			case IPathEntry.CDT_PROJECT:
				res = root.findMember(path);
				isMissing = (res == null);
				break;
		}
		CPListElement elem = new CPListElement(project, curr.getEntryKind(), path, res);
		elem.setAttribute(SOURCEATTACHMENT, sourceAttachment);
		elem.setAttribute(EXCLUSION, exclusion);
		elem.setAttribute(INCLUDE, include);
		elem.setAttribute(MACRO_NAME, macroName);
		elem.setAttribute(MACRO_VALUE, macroValue);
		elem.setAttribute(SYSTEM_INCLUDE, Boolean.valueOf(sysInclude));
		elem.setAttribute(BASE_REF, baseRef);
		elem.setAttribute(BASE, base);
		elem.setExported(curr.isExported());

		if (project.exists()) {
			elem.setIsMissing(isMissing);
		}
		return elem;
	}
}