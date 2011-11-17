/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class CContainer extends Openable implements ICContainer {
	CModelManager factory = CModelManager.getDefault();

	public CContainer(ICElement parent, IResource res) {
		this(parent, res, ICElement.C_CCONTAINER);
	}

	public CContainer(ICElement parent, IResource res, int type) {
		super(parent, res, type);
	}

	/**
	 * Returns a the collection of binary files in this ccontainer
	 *
	 * @see ICContainer#getBinaries()
	 */
	@Override
	public IBinary[] getBinaries() throws CModelException {
		List<?> list = getChildrenOfType(C_BINARY);
		IBinary[] array = new IBinary[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * @see ICContainer#getBinary(String)
	 */
	@Override
	public IBinary getBinary(String name) {
		IFile file = getContainer().getFile(new Path(name));
		return getBinary(file);
	}

	public IBinary getBinary(IFile file) {
		IBinaryFile bin = factory.createBinaryFile(file);
		if (bin instanceof IBinaryObject) {
			return new Binary(this, file, (IBinaryObject) bin);
		}
		return new Binary(this, file, null);
	}

	/**
	 * Returns a the collection of archive files in this ccontainer
	 *
	 * @see ICContainer#getArchives()
	 */
	@Override
	public IArchive[] getArchives() throws CModelException {
		List<?> list = getChildrenOfType(C_ARCHIVE);
		IArchive[] array = new IArchive[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * @see ICContainer#getArchive(String)
	 */
	@Override
	public IArchive getArchive(String name) {
		IFile file = getContainer().getFile(new Path(name));
		return getArchive(file);
	}

	public IArchive getArchive(IFile file) {
		IBinaryFile ar = factory.createBinaryFile(file);
		if (ar != null && ar.getType() == IBinaryFile.ARCHIVE) {
			return new Archive(this, file, (IBinaryArchive) ar);
		}
		return new Archive(this, file, null);
	}

	/**
	 * @see ICContainer#getTranslationUnits()
	 */
	@Override
	public ITranslationUnit[] getTranslationUnits() throws CModelException {
		List<?> list = getChildrenOfType(C_UNIT);
		ITranslationUnit[] array = new ITranslationUnit[list.size()];
		list.toArray(array);
		return array;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.core.model.ICContainer#getTranslationUnit(java.lang.String)
	 */
	@Override
	public ITranslationUnit getTranslationUnit(String name) {
		IFile file = getContainer().getFile(new Path(name));
		return getTranslationUnit(file);
	}

	public ITranslationUnit getTranslationUnit(IFile file) {
		String id = CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
		return new TranslationUnit(this, file, id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.core.model.ICContainer#getCContainers()
	 */
	@Override
	public ICContainer[] getCContainers() throws CModelException {
		List<?> list = getChildrenOfType(C_CCONTAINER);
		ICContainer[] array = new ICContainer[list.size()];
		list.toArray(array);
		return array;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.core.model.ICContainer#getCContainer(java.lang.String)
	 */
	@Override
	public ICContainer getCContainer(String name) {
		IFolder folder = getContainer().getFolder(new Path(name));
		return getCContainer(folder);
	}

	public ICContainer getCContainer(IFolder folder) {
		return new CContainer(this, folder);
	}

	public IContainer getContainer() {
		return (IContainer) getResource();
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new CContainerInfo(this);
	}

	// CHECKPOINT: folders will return the hash code of their path
	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	/**
	 * @see Openable
	 */
	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements, IResource underlyingResource)
			throws CModelException {
		boolean validInfo = false;
		try {
			IResource res = getResource();
			if (res != null && res.isAccessible()) {
				validInfo = computeChildren(info, res);
			} else {
				throw newNotPresentException();
			}
		} finally {
			if (!validInfo) {
				CModelManager.getDefault().removeInfo(this);
			}
		}
		return validInfo;
	}

	/*
	 * (non-Javadoc) Returns an array of non-c resources contained in the
	 * receiver.
	 *
	 * @see org.eclipse.cdt.core.model.ICContainer#getNonCResources()
	 */
	@Override
	public Object[] getNonCResources() throws CModelException {
		return ((CContainerInfo) getElementInfo()).getNonCResources(getResource());
	}

	protected boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
		ArrayList<ICElement> vChildren = new ArrayList<ICElement>();
		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer) res;
				resources = container.members(false);
			}
			if (resources != null) {
				ICProject cproject = getCProject();
				for (IResource resource2 : resources) {
					ICElement celement = computeChild(resource2, cproject);
					if (celement != null) {
						vChildren.add(celement);
					}
				}
			}
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		info.setChildren(vChildren);
		if (info instanceof CContainerInfo) {
			((CContainerInfo) info).setNonCResources(null);
		}
		return true;
	}

	protected ICElement computeChild(IResource res, ICProject cproject) throws CModelException {
		ICElement celement = null;
		ISourceRoot sroot = getSourceRoot();
		switch (res.getType()) {
			case IResource.FILE: {
				IFile file = (IFile) res;
				boolean checkBinary = true;
				if (sroot != null && sroot.isOnSourceEntry(res)) {
					// Check for Valid C Element only.
					String id = CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
					if (id != null) {
						celement = new TranslationUnit(this, file, id);
						checkBinary = false;
					} else {
						checkBinary = true;
					}
				}
				if (checkBinary && cproject.isOnOutputEntry(file)) {
					IBinaryParser.IBinaryFile bin = factory.createBinaryFile(file);
					if (bin != null) {
						if (bin.getType() == IBinaryFile.ARCHIVE) {
							celement = new Archive(this, file, (IBinaryArchive) bin);
							ArchiveContainer vlib = (ArchiveContainer) cproject.getArchiveContainer();
							vlib.addChild(celement);
						} else {
							final Binary binElement= new Binary(this, file, (IBinaryObject) bin);
							celement= binElement;
							if (binElement.showInBinaryContainer()) {
								BinaryContainer vbin = (BinaryContainer) cproject.getBinaryContainer();
								vbin.addChild(celement);
							}
						}
					}
				}
				break;
			}
			case IResource.FOLDER:
				if (sroot != null && sroot.isOnSourceEntry(res) || (sroot == null && cproject.isOnOutputEntry(res))) {
					celement = new CContainer(this, res);
				}
				break;
		}
		return celement;
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		switch (token.charAt(0)) {
		case CEM_SOURCEFOLDER:
			String name;
			if (memento.hasMoreTokens()) {
				name = memento.nextToken();
				char firstChar = name.charAt(0);
				if (firstChar == CEM_TRANSLATIONUNIT) {
					token = name;
					name = ""; //$NON-NLS-1$
				} else {
					token = null;
				}
			} else {
				name = ""; //$NON-NLS-1$
				token = null;
			}
			CElement folder = (CElement)getCContainer(name);
			if (folder != null) {
				if (token == null) {
					return folder.getHandleFromMemento(memento);
				} else {
					return folder.getHandleFromMemento(token, memento);
				}
			}
			break;
		case CEM_TRANSLATIONUNIT:
			if (!memento.hasMoreTokens()) return this;
			String tuName = memento.nextToken();
			CElement tu = (CElement) getTranslationUnit(tuName);
			if (tu != null) {
				return tu.getHandleFromMemento(memento);
			}
		}
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		return CElement.CEM_SOURCEFOLDER;
	}

}
