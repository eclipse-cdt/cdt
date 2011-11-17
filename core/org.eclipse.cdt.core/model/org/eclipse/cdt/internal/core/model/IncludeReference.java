/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * IncludeReference
 */
public class IncludeReference extends Openable implements IIncludeReference {

	final IIncludeEntry fIncludeEntry;
	final IPath fPath;

	public IncludeReference(ICProject cproject, IIncludeEntry entry) {
		this(cproject, entry, entry.getFullIncludePath());
	}

	public IncludeReference(ICElement celement, IIncludeEntry entry, IPath path) {
		super(celement, null, path.toString(), ICElement.C_VCONTAINER);
		fIncludeEntry = entry;
		fPath = PathUtil.getCanonicalPathWindows(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	@Override
	public IResource getResource() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.internal.core.model.CElement#exists()
	 */
	@Override
	public boolean exists() {
		File file = null;
		if (fPath != null) {
			file = fPath.toFile();
		} else if (fIncludeEntry != null) {
			file = fIncludeEntry.getFullIncludePath().toFile();
		}
		return file != null && file.isDirectory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#createElementInfo()
	 */
	@Override
	protected CElementInfo createElementInfo() {
		return new OpenableInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeReference#getIncludeEntry()
	 */
	@Override
	public IIncludeEntry getIncludeEntry() {
		return fIncludeEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements, IResource underlyingResource) throws CModelException {
		return computeChildren(info, pm, underlyingResource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeReference#getAffectedPath()
	 */
	@Override
	public IPath getAffectedPath() {
		return fIncludeEntry.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CContainer#computeChildren(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.resources.IResource)
	 */
	protected boolean computeChildren(OpenableInfo info, IProgressMonitor pm, IResource res) throws CModelException {
		ArrayList<ICElement> vChildren = new ArrayList<ICElement>();
		IPath filePath = null;
		if (fPath != null) {
			filePath = fPath;
		} else if (fIncludeEntry != null) {
			filePath = fIncludeEntry.getFullIncludePath();
		}
		if (filePath != null) {
			if (!filePath.isUNC()) {
				File file = filePath.toFile();
				String[] names = null;
				if (file != null && file.isDirectory()) {
					names = file.list();

					if (names != null) {
						IPath path = new Path(file.getAbsolutePath());
						for (String name : names) {
							File child = new File(file, name);
							ICElement celement = null;
							if (child.isDirectory()) {
								celement = new IncludeReference(this, fIncludeEntry, new Path(child.getAbsolutePath()));
							} else if (child.isFile()){
								String id = CoreModel.getRegistedContentTypeId(getCProject().getProject(), name);
								if (id != null) {
									// TODO:  should use URI
									celement = new ExternalTranslationUnit(this, URIUtil.toURI(path.append(name)), id);
								}
							}
							if (celement != null) {
								vChildren.add(celement);
							}
						}
					}
				}
			} else {
				try {
					IFileStore store = EFS.getStore(UNCPathConverter.getInstance().toURI(filePath));
					IFileStore children[] = store.childStores(EFS.NONE, pm);
					for (IFileStore child : children) {
						ICElement celement = null;
						if (child.fetchInfo().isDirectory()) {
							celement = new IncludeReference(this, fIncludeEntry, filePath.append(child.getName()));
						} else {
							String id = CoreModel.getRegistedContentTypeId(getCProject().getProject(), child.getName());
							if (id != null) {
								// TODO:  should use URI
								celement = new ExternalTranslationUnit(this, child.toURI(), id);
							}
						}
						if (celement != null) {
							vChildren.add(celement);
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		info.setChildren(vChildren);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeReference#isOnIncludeEntry(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean isOnIncludeEntry(IPath path) {
		if (fIncludeEntry.getFullIncludePath().isPrefixOf(path)
				&& !CoreModelUtil.isExcluded(path, fIncludeEntry.fullExclusionPatternChars())) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	@Override
	public IPath getPath() {
		return fPath;
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		return null;
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		((CElement)getParent()).getHandleMemento(buff);
	}

	@Override
	protected char getHandleMementoDelimiter() {
		Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
		return 0;
	}

}
