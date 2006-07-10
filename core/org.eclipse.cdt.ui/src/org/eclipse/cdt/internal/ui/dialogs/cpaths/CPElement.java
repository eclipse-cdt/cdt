/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class CPElement {

	public static final String SOURCEATTACHMENT = "sourcepath"; //$NON-NLS-1$
	public static final String SOURCEATTACHMENTROOT = "rootpath"; //$NON-NLS-1$
	public static final String EXCLUSION = "exclusion"; //$NON-NLS-1$
	public static final String INCLUDE = "includepath"; //$NON-NLS-1$
	public static final String LIBRARY = "librarypath"; //$NON-NLS-1$
	public static final String SYSTEM_INCLUDE = "systeminclude"; //$NON-NLS-1$
	public static final String MACRO_NAME = "macroname"; //$NON-NLS-1$
	public static final String MACRO_VALUE = "macrovalue"; //$NON-NLS-1$
	public static final String BASE_REF = "base-ref"; //$NON-NLS-1$
	public static final String BASE = "base-path"; //$NON-NLS-1$
	public static final String PARENT = "parent"; //$NON-NLS-1$
	public static final String PARENT_CONTAINER = "parent-container"; //$NON-NLS-1$
    public static final String INCLUDE_FILE = "includefile"; //$NON-NLS-1$
    public static final String MACROS_FILE = "macrosfile"; //$NON-NLS-1$

	private final int fEntryKind;
	private final IPath fPath;
	private final ICProject fCProject;
	private final IResource fResource;
	private final ArrayList fChildren = new ArrayList(1);

	private boolean fIsExported;

	private IPathEntry fCachedEntry;
	private CPElement Inherited; // used when the path is duplicated on a child
								 // resource but is inherited from a parent
								 // resource these are not real path entries

	private IStatus fStatus;
	
	// create a inherited element and apply to path/resource
	public CPElement(CPElement element, IPath path, IResource res) {
		this(element.getCProject(), element.getEntryKind(), path, res);
		setExported(element.isExported());
		fChildren.clear();
		for(int i = 0; i < element.fChildren.size(); i++) {
			CPElementAttribute attrib = (CPElementAttribute)element.fChildren.get(i);
			fChildren.add(new CPElementAttribute(this, attrib.getKey(), attrib.getValue()));
		}
		Inherited = element;
	}

	public CPElement(ICProject project, int entryKind, IPath path, IResource res) {
		fCProject = project;
		fEntryKind = entryKind;
		fPath = path;
		fResource = res;

		fIsExported = false;
		fCachedEntry = null;

		switch (entryKind) {
			case IPathEntry.CDT_OUTPUT :
				createAttributeElement(EXCLUSION, new Path[0]);
				break;
			case IPathEntry.CDT_SOURCE :
				createAttributeElement(EXCLUSION, new Path[0]);
				break;
			case IPathEntry.CDT_LIBRARY :
				createAttributeElement(LIBRARY, new Path("")); //$NON-NLS-1$
				createAttributeElement(SOURCEATTACHMENT, null);
				createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
				createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
				break;
			case IPathEntry.CDT_INCLUDE :
				createAttributeElement(INCLUDE, new Path("")); //$NON-NLS-1$
				createAttributeElement(EXCLUSION, new Path[0]);
				createAttributeElement(SYSTEM_INCLUDE, Boolean.valueOf(true));
				createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
				createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
				break;
            case IPathEntry.CDT_INCLUDE_FILE :
                createAttributeElement(INCLUDE_FILE, new Path("")); //$NON-NLS-1$
                createAttributeElement(EXCLUSION, new Path[0]);
                createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
                createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
                break;
			case IPathEntry.CDT_MACRO :
				createAttributeElement(MACRO_NAME, ""); //$NON-NLS-1$
				createAttributeElement(MACRO_VALUE, ""); //$NON-NLS-1$
				createAttributeElement(EXCLUSION, new Path[0]);
				createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
				createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
				break;
            case IPathEntry.CDT_MACRO_FILE :
                createAttributeElement(MACROS_FILE, new Path("")); //$NON-NLS-1$
                createAttributeElement(EXCLUSION, new Path[0]);
                createAttributeElement(BASE_REF, new Path("")); //$NON-NLS-1$
                createAttributeElement(BASE, new Path("")); //$NON-NLS-1$
                break;
			case IPathEntry.CDT_CONTAINER :
				try {
					IPathEntryContainer container = CoreModel.getPathEntryContainer(fPath, fCProject);
					if (container != null) {
                        IPathEntry[] entries = null;
                        if (container instanceof IPathEntryContainerExtension &&
                                res instanceof IFile) {
                            IPathEntryContainerExtension extContainer = (IPathEntryContainerExtension) container;
                            entries = extContainer.getPathEntries(res.getFullPath(),
                                    IPathEntry.CDT_INCLUDE | IPathEntry.CDT_MACRO |
                                    IPathEntry.CDT_INCLUDE_FILE | IPathEntry.CDT_MACRO_FILE);
                        }
                        else {
                            entries = container.getPathEntries();
                        }
						for (int i = 0; i < entries.length; i++) {
							CPElement curr = createFromExisting(entries[i], fCProject);
							curr.createAttributeElement(PARENT_CONTAINER, this);
							CPElementGroup group = new CPElementGroup(this, curr.getEntryKind());
							int indx = fChildren.indexOf(group);
							if (indx == -1) {
								fChildren.add(group);
							} else {
								group = (CPElementGroup)fChildren.get(indx);
							}
							group.addChild(curr);
						}
					}
				} catch (CModelException e) {
				}
				break;
			default :
		}
	}

	public IPathEntry getPathEntry() {
		if (Inherited != null) {
			return null;
		}
		if (fCachedEntry == null) {
			fCachedEntry = newPathEntry();
		}
		return fCachedEntry;
	}

	private IPathEntry newPathEntry() {
		IPath[] exclusionPattern = (IPath[])getAttribute(EXCLUSION);
		IPath base = (IPath)getAttribute(BASE);
		IPath baseRef = (IPath)getAttribute(BASE_REF);
		switch (fEntryKind) {
			case IPathEntry.CDT_OUTPUT :
				return CoreModel.newOutputEntry(fPath, exclusionPattern);
			case IPathEntry.CDT_SOURCE :
				return CoreModel.newSourceEntry(fPath, exclusionPattern);
			case IPathEntry.CDT_LIBRARY :
				IPath libraryPath = (IPath)getAttribute(LIBRARY);
				IPath attach = (IPath)getAttribute(SOURCEATTACHMENT);
				if (!baseRef.isEmpty()) {
					return CoreModel.newLibraryRefEntry(fPath, baseRef, libraryPath);
				}
				return CoreModel.newLibraryEntry(fPath, base, libraryPath, attach, null, null, isExported());
			case IPathEntry.CDT_PROJECT :
				return CoreModel.newProjectEntry(fPath, isExported());
			case IPathEntry.CDT_CONTAINER :
				return CoreModel.newContainerEntry(fPath, isExported());
			case IPathEntry.CDT_INCLUDE :
				IPath include = (IPath)getAttribute(INCLUDE);
				if (!baseRef.isEmpty()) {
					return CoreModel.newIncludeRefEntry(fPath, baseRef, include);
				}
				return CoreModel.newIncludeEntry(fPath, base, include, ((Boolean)getAttribute(SYSTEM_INCLUDE)).booleanValue(),
						exclusionPattern, isExported());
            case IPathEntry.CDT_INCLUDE_FILE:
                IPath includeFile = (IPath)getAttribute(INCLUDE_FILE);
                return CoreModel.newIncludeFileEntry(fPath, baseRef, base, includeFile,
                        exclusionPattern, isExported());
			case IPathEntry.CDT_MACRO :
				String macroName = (String)getAttribute(MACRO_NAME);
				String macroValue = (String)getAttribute(MACRO_VALUE);
				if (!baseRef.isEmpty()) {
					return CoreModel.newMacroRefEntry(fPath, baseRef, macroName);
				}
				return CoreModel.newMacroEntry(fPath, macroName, macroValue, exclusionPattern, isExported());
            case IPathEntry.CDT_MACRO_FILE :
                IPath macrosFile = (IPath)getAttribute(MACROS_FILE);
                return CoreModel.newMacroFileEntry(fPath, baseRef, base, macrosFile,
                        exclusionPattern, isExported());
			default :
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

	public StringBuffer appendEncodedSettings(StringBuffer buf) {
		buf.append(fEntryKind).append(';');
		appendEncodePath(fPath, buf).append(';');
		buf.append(Boolean.valueOf(fIsExported)).append(';');
		switch (fEntryKind) {
			case IPathEntry.CDT_OUTPUT :
			case IPathEntry.CDT_SOURCE :
			case IPathEntry.CDT_INCLUDE :
            case IPathEntry.CDT_INCLUDE_FILE :
			case IPathEntry.CDT_MACRO :
            case IPathEntry.CDT_MACRO_FILE :
				IPath[] exclusion = (IPath[])getAttribute(EXCLUSION);
				buf.append('[').append(exclusion.length).append(']');
				for (int i = 0; i < exclusion.length; i++) {
					appendEncodePath(exclusion[i], buf);
				}
				switch (fEntryKind) {
					case IPathEntry.CDT_INCLUDE :
						IPath baseRef = (IPath)getAttribute(BASE_REF);
						appendEncodePath(baseRef, buf);
						IPath base = (IPath)getAttribute(BASE);
						appendEncodePath(base, buf);
						IPath include = (IPath)getAttribute(INCLUDE);
						appendEncodePath(include, buf);
						break;
                    case IPathEntry.CDT_INCLUDE_FILE :
                        baseRef = (IPath)getAttribute(BASE_REF);
                        appendEncodePath(baseRef, buf);
                        base = (IPath)getAttribute(BASE);
                        appendEncodePath(base, buf);
                        IPath includeFile = (IPath)getAttribute(INCLUDE_FILE);
                        appendEncodePath(includeFile, buf);
                        break;
					case IPathEntry.CDT_MACRO :
						baseRef = (IPath)getAttribute(BASE_REF);
						appendEncodePath(baseRef, buf);
						base = (IPath)getAttribute(BASE);
						appendEncodePath(base, buf);
						String symbol = (String)getAttribute(MACRO_NAME);
						buf.append(symbol).append(';');
                    case IPathEntry.CDT_MACRO_FILE :
                        baseRef = (IPath)getAttribute(BASE_REF);
                        appendEncodePath(baseRef, buf);
                        base = (IPath)getAttribute(BASE);
                        appendEncodePath(base, buf);
                        IPath macrosFile = (IPath)getAttribute(MACROS_FILE);
                        appendEncodePath(macrosFile, buf);
                        break;
					default :
				}
				break;
			case IPathEntry.CDT_LIBRARY :
				IPath baseRef = (IPath)getAttribute(BASE_REF);
				appendEncodePath(baseRef, buf);
				IPath base = (IPath)getAttribute(BASE);
				appendEncodePath(base, buf);
				IPath sourceAttach = (IPath)getAttribute(SOURCEATTACHMENT);
				appendEncodePath(sourceAttach, buf);
				IPath library = (IPath)getAttribute(LIBRARY);
				appendEncodePath(library, buf);
				break;
			default :
		}
		buf.setLength(buf.length() - 1);
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

	public CPElement getParentContainer() {
		CPElementAttribute attribute = findAttributeElement(PARENT_CONTAINER);
		if (attribute != null) {
			return (CPElement)attribute.getValue();
		}
		return null;
	}

	public void setParent(CPElementGroup group) {
		CPElementAttribute attribute = findAttributeElement(PARENT);
		if (attribute == null && group != null) {
			createAttributeElement(PARENT, group);
			return;
		}
		attribute.setValue(group);
	}

	public CPElementGroup getParent() {
		CPElementAttribute attribute = findAttributeElement(PARENT);
		if (attribute != null) {
			return (CPElementGroup)attribute.getValue();
		}
		return null;
	}

	public CPElementAttribute setAttribute(String key, Object value) {
		CPElementAttribute attribute = findAttributeElement(key);
		if (attribute == null) {
			return null;
		}
		attribute.setValue(value);
		attributeChanged(key);
		return attribute;
	}

	private CPElementAttribute findAttributeElement(String key) {
		for (int i = 0; i < fChildren.size(); i++) {
			Object curr = fChildren.get(i);
			if (curr instanceof CPElementAttribute) {
				CPElementAttribute elem = (CPElementAttribute)curr;
				if (key.equals(elem.getKey())) {
					return elem;
				}
			}
		}
		return null;
	}

	public Object getAttribute(String key) {
		CPElementAttribute attrib = findAttributeElement(key);
		if (attrib != null) {
			return attrib.getValue();
		}
		return null;
	}

	private void createAttributeElement(String key, Object value) {
		fChildren.add(new CPElementAttribute(this, key, value));
	}

	public Object[] getChildren() {
		switch (fEntryKind) {
			case IPathEntry.CDT_OUTPUT :
			case IPathEntry.CDT_SOURCE :
			case IPathEntry.CDT_INCLUDE :
            case IPathEntry.CDT_INCLUDE_FILE :
			case IPathEntry.CDT_MACRO :
            case IPathEntry.CDT_MACRO_FILE :
				if (getInherited() == null && getParentContainer() == null) {
					return new Object[]{findAttributeElement(EXCLUSION)};
				}
				break;
			//			case IPathEntry.CDT_LIBRARY :
			//				return new Object[] { findAttributeElement(SOURCEATTACHMENT) };

			case IPathEntry.CDT_CONTAINER : {
				List list = new ArrayList();
				for (int i = 0; i < fChildren.size(); i++) {
					Object curr = fChildren.get(i);
					if (curr instanceof CPElementGroup) {
						list.add(curr);
					}
				}
				return list.toArray();
			}
		}
		return new Object[0];
	}
	
	private void attributeChanged(String key) {
		fCachedEntry = null;
		fStatus = null;
	}

	/*
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other != null && other.getClass().equals(getClass())) {
			CPElement elem = (CPElement)other;
			if (elem.fEntryKind != fEntryKind || !elem.fPath.equals(fPath)) {
				return false;
			}
			switch (fEntryKind) {
				case IPathEntry.CDT_LIBRARY :
					return (getAttribute(LIBRARY).equals(elem.getAttribute(LIBRARY))
							&& getAttribute(BASE).equals(elem.getAttribute(BASE)) && getAttribute(BASE_REF).equals(
							elem.getAttribute(BASE_REF)));
				case IPathEntry.CDT_INCLUDE :
					return (getAttribute(INCLUDE).equals(elem.getAttribute(INCLUDE))
							&& getAttribute(BASE_REF).equals(elem.getAttribute(BASE_REF)) && getAttribute(BASE).equals(
							elem.getAttribute(BASE)));
                case IPathEntry.CDT_INCLUDE_FILE :
                    return (getAttribute(INCLUDE_FILE).equals(elem.getAttribute(INCLUDE_FILE))
                            && getAttribute(BASE_REF).equals(elem.getAttribute(BASE_REF)) && getAttribute(BASE).equals(
                            elem.getAttribute(BASE)));
				case IPathEntry.CDT_MACRO :
					return (getAttribute(MACRO_NAME).equals(elem.getAttribute(MACRO_NAME))
							&& getAttribute(BASE_REF).equals(elem.getAttribute(BASE_REF)) && getAttribute(BASE).equals(
							elem.getAttribute(BASE)));
                case IPathEntry.CDT_MACRO_FILE :
                    return (getAttribute(MACROS_FILE).equals(elem.getAttribute(MACROS_FILE))
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
		final int HASH_FACTOR = 89;
		int hashCode = fPath.hashCode() + fEntryKind;
		switch (fEntryKind) {
			case IPathEntry.CDT_LIBRARY :
				hashCode = hashCode * HASH_FACTOR + getAttribute(LIBRARY).hashCode();
				hashCode = hashCode * HASH_FACTOR + getAttribute(BASE).hashCode();
				hashCode = hashCode * HASH_FACTOR + getAttribute(BASE_REF).hashCode();
				break;
			case IPathEntry.CDT_INCLUDE :
				hashCode = hashCode * HASH_FACTOR + getAttribute(INCLUDE).hashCode();
				hashCode = hashCode * HASH_FACTOR + getAttribute(BASE_REF).hashCode();
				hashCode = hashCode * HASH_FACTOR + getAttribute(BASE).hashCode();
				break;
            case IPathEntry.CDT_INCLUDE_FILE :
                hashCode = hashCode * HASH_FACTOR + getAttribute(INCLUDE_FILE).hashCode();
                hashCode = hashCode * HASH_FACTOR + getAttribute(BASE_REF).hashCode();
                hashCode = hashCode * HASH_FACTOR + getAttribute(BASE).hashCode();
                break;
			case IPathEntry.CDT_MACRO :
				hashCode = hashCode * HASH_FACTOR + getAttribute(MACRO_NAME).hashCode();
				hashCode = hashCode * HASH_FACTOR + getAttribute(BASE_REF).hashCode();
				hashCode = hashCode * HASH_FACTOR + getAttribute(BASE).hashCode();
				break;
            case IPathEntry.CDT_MACRO_FILE :
                hashCode = hashCode * HASH_FACTOR + getAttribute(MACROS_FILE).hashCode();
                hashCode = hashCode * HASH_FACTOR + getAttribute(BASE_REF).hashCode();
                hashCode = hashCode * HASH_FACTOR + getAttribute(BASE).hashCode();
                break;
		}
		return hashCode;
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
	public IStatus getStatus() {
		if (Inherited != null) {
			return Inherited.getStatus();
		}
		if (fStatus == null) {
			fStatus = Status.OK_STATUS;
			IResource res = null;
			IPath path;
			IWorkspaceRoot root = CUIPlugin.getWorkspace().getRoot();
			IPathEntry entry = getPathEntry();
			switch (getEntryKind()) {
				case IPathEntry.CDT_CONTAINER :
					try {
						if ((CoreModel.getPathEntryContainer(fPath, fCProject) == null)) {
							fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1,
									CPathEntryMessages.getString("CPElement.status.pathContainerMissing"), null); //$NON-NLS-1$
						}
					} catch (CModelException e) {
					}
					break;
				case IPathEntry.CDT_LIBRARY :
					if (!((ILibraryEntry)entry).getFullLibraryPath().toFile().exists()) {
						fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.libraryPathNotFound"), null); //$NON-NLS-1$
					}
					break;
				case IPathEntry.CDT_SOURCE :
					path = fPath.removeTrailingSeparator();
					res = root.findMember(path);
					if (res == null) {
						if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
							res = root.getFolder(path);
						}
						fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.sourcePathMissing"), null); //$NON-NLS-1$
					}
					break;
				case IPathEntry.CDT_OUTPUT :
					path = fPath.removeTrailingSeparator();
					res = root.findMember(path);
					if (res == null) {
						if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
							res = root.getFolder(path);
						}
						fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.outputPathMissing"), null); //$NON-NLS-1$
					}
					break;
				case IPathEntry.CDT_INCLUDE :
					path = fPath.removeTrailingSeparator();
					res = root.findMember(path);
					if (res == null) {
						if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
							res = root.getFolder(path);
						}
					}
					if (res.getType() != IResource.ROOT && res.getType() != IResource.PROJECT && fCProject != null) {
						if (!fCProject.isOnSourceRoot(res)) {
							fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.notOnSourcePath"), null); //$NON-NLS-1$
						}
					}
					if (!((IIncludeEntry)entry).getFullIncludePath().toFile().exists()) {
						fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.includePathNotFound"), null); //$NON-NLS-1$
					}
					break;
                case IPathEntry.CDT_INCLUDE_FILE :
                    path = fPath.removeTrailingSeparator();
                    res = root.findMember(path);
                    if (res == null) {
                        if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
                            res = root.getFolder(path);
                        }
                    }
                    if (res.getType() != IResource.ROOT && res.getType() != IResource.PROJECT && fCProject != null) {
                        if (!fCProject.isOnSourceRoot(res)) {
                            fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.notOnSourcePath"), null); //$NON-NLS-1$
                        }
                    }
                    if (!((IIncludeFileEntry)entry).getFullIncludeFilePath().toFile().exists()) {
                        fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.includeFilePathNotFound"), null); //$NON-NLS-1$
                    }
                    break;
				case IPathEntry.CDT_MACRO :
					path = fPath.removeTrailingSeparator();
					res = root.findMember(path);
					if (res == null) {
						if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
							res = root.getFolder(path);
						}
					}
					if (res.getType() != IResource.ROOT && res.getType() != IResource.PROJECT && fCProject != null) {
						if (!fCProject.isOnSourceRoot(res)) {
							fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.notOnSourcePath"), null); //$NON-NLS-1$
						}
					}
					break;
                case IPathEntry.CDT_MACRO_FILE :
                    path = fPath.removeTrailingSeparator();
                    res = root.findMember(path);
                    if (res == null) {
                        if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
                            res = root.getFolder(path);
                        }
                    }
                    if (res.getType() != IResource.ROOT && res.getType() != IResource.PROJECT && fCProject != null) {
                        if (!fCProject.isOnSourceRoot(res)) {
                            fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.notOnSourcePath"), null); //$NON-NLS-1$
                        }
                    }
                    if (!((IMacroFileEntry)entry).getFullMacroFilePath().toFile().exists()) {
                        fStatus = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.macrosFilePathNotFound"), null); //$NON-NLS-1$
                    }
                    break;
				case IPathEntry.CDT_PROJECT :
					res = root.findMember(fPath);
					if (res == null) {
						fStatus = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, -1, CPathEntryMessages.getString("CPElement.status.missingProjectPath"), null); //$NON-NLS-1$
					}
					break;
			}
		}
		return fStatus;
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

	public CPElement getInherited() {
		return Inherited;
	}

	/**
	 * Gets the project.
	 * 
	 * @return Returns a ICProject
	 */
	public ICProject getCProject() {
		return fCProject;
	}

	public static CPElement createFromExisting(IPathEntry curr, ICElement element) {
		IPath path = curr.getPath();
		IWorkspaceRoot root = CUIPlugin.getWorkspace().getRoot();
		IPath sourceAttachment = null;
		IPath[] exclusion = null;
		IPath include = null;
        IPath includeFile = null;
		IPath library = null;
		String macroName = null;
		String macroValue = null;
        IPath macrosFile = null;
		boolean sysInclude = false;
		IPath baseRef = null;
		IPath base = null;

		// get the resource
		IResource res = null;

		switch (curr.getEntryKind()) {
			case IPathEntry.CDT_CONTAINER :
				res = (element instanceof ICProject) ? null : element.getResource();
				break;
			case IPathEntry.CDT_LIBRARY :
				library = ((ILibraryEntry)curr).getLibraryPath();
				sourceAttachment = ((ILibraryEntry)curr).getSourceAttachmentPath();
				base = ((ILibraryEntry)curr).getBasePath();
				baseRef = ((ILibraryEntry)curr).getBaseReference();
				break;
			case IPathEntry.CDT_SOURCE :
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
				}
				exclusion = ((ISourceEntry)curr).getExclusionPatterns();
				break;
			case IPathEntry.CDT_OUTPUT :
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
				}
				exclusion = ((IOutputEntry)curr).getExclusionPatterns();
				break;
			case IPathEntry.CDT_INCLUDE :
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
				}
				exclusion = ((IIncludeEntry)curr).getExclusionPatterns();
				sysInclude = ((IIncludeEntry)curr).isSystemInclude();
				baseRef = ((IIncludeEntry)curr).getBaseReference();
				base = ((IIncludeEntry)curr).getBasePath();
				include = ((IIncludeEntry)curr).getIncludePath();
				break;
            case IPathEntry.CDT_INCLUDE_FILE :
                path = path.removeTrailingSeparator();
                res = root.findMember(path);
                if (res == null) {
                    if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
                        res = root.getFolder(path);
                    }
                }
                exclusion = ((IIncludeFileEntry)curr).getExclusionPatterns();
                includeFile = ((IIncludeFileEntry)curr).getIncludeFilePath();
                baseRef = ((IIncludeFileEntry)curr).getBaseReference();
                base = ((IIncludeFileEntry)curr).getBasePath();
                break;
			case IPathEntry.CDT_MACRO :
				path = path.removeTrailingSeparator();
				res = root.findMember(path);
				if (res == null) {
					if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
						res = root.getFolder(path);
					}
				}
				exclusion = ((IMacroEntry)curr).getExclusionPatterns();
				macroName = ((IMacroEntry)curr).getMacroName();
				macroValue = ((IMacroEntry)curr).getMacroValue();
				baseRef = ((IMacroEntry)curr).getBaseReference();
				base = ((IMacroEntry)curr).getBasePath();
				break;
            case IPathEntry.CDT_MACRO_FILE :
                path = path.removeTrailingSeparator();
                res = root.findMember(path);
                if (res == null) {
                    if (root.getWorkspace().validatePath(path.toString(), IResource.FOLDER).isOK()) {
                        res = root.getFolder(path);
                    }
                }
                exclusion = ((IMacroFileEntry)curr).getExclusionPatterns();
                macrosFile = ((IMacroFileEntry)curr).getMacroFilePath();
                baseRef = ((IMacroFileEntry)curr).getBaseReference();
                base = ((IMacroFileEntry)curr).getBasePath();
                break;
			case IPathEntry.CDT_PROJECT :
				res = root.findMember(path);
				break;
		}
		CPElement elem = new CPElement((element == null) ? null : element.getCProject(), curr.getEntryKind(), path, res);
		elem.setAttribute(SOURCEATTACHMENT, sourceAttachment);
		elem.setAttribute(EXCLUSION, exclusion);
		elem.setAttribute(INCLUDE, include);
        elem.setAttribute(INCLUDE_FILE, includeFile);
		elem.setAttribute(LIBRARY, library);
		elem.setAttribute(MACRO_NAME, macroName);
		elem.setAttribute(MACRO_VALUE, macroValue);
        elem.setAttribute(MACROS_FILE, macrosFile);
		elem.setAttribute(SYSTEM_INCLUDE, Boolean.valueOf(sysInclude));
		elem.setAttribute(BASE_REF, baseRef);
		elem.setAttribute(BASE, base);
		elem.setExported(curr.isExported());
		return elem;
	}
}
