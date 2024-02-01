/*******************************************************************************
 * Copyright (c) 2000, 2024 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     John Dallaway - Adapt for IBinaryFile (#413)
 *     John Dallaway - Fix object path processing (#630)
 *     John Dallaway - Use common buildCWD lookup methods (#652)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class Archive extends Openable implements IArchive {

	IBinaryArchive binaryArchive;

	public Archive(ICElement parent, IFile file, IBinaryArchive ar) {
		super(parent, file, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	public Archive(ICElement parent, IPath path, IBinaryArchive ar) {
		super(parent, path, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	@Override
	public IBinary[] getBinaries() throws CModelException {
		ICElement[] e = getChildren();
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() throws CModelException {
		return (ArchiveInfo) getElementInfo();
	}

	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements,
			IResource underlyingResource) throws CModelException {
		return computeChildren(info, underlyingResource);
	}

	public boolean computeChildren(OpenableInfo info, IResource res) {
		IBinaryArchive ar = getBinaryArchive();
		IPath location = res.getLocation();
		if (ar != null && location != null) {
			// find the build CWD for the archive file
			IPath buildCWD = Optional.ofNullable(InternalCoreModelUtil.findBuildConfiguration(res))
					.map(InternalCoreModelUtil::getBuildCWD).orElse(location.removeLastSegments(1));
			for (IBinaryObject obj : ar.getObjects()) {
				// assume object names are paths as specified on the archiver command line ("ar -P")
				IPath objPath = new Path(obj.getName());
				if (!objPath.isAbsolute()) {
					// assume path is relative to the build CWD
					objPath = buildCWD.append(objPath);
				}
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(objPath);
				if (file == null) { // if object path is external to the workspace
					Binary binary = new Binary(this, URIUtil.toURI(objPath), obj);
					info.addChild(binary);
				} else {
					Binary binary = new Binary(this, objPath, obj);
					info.addChild(binary);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IBinaryArchive.class)) {
			return adapter.cast(getBinaryArchive());
		}
		return super.getAdapter(adapter);
	}

	IBinaryArchive getBinaryArchive() {
		return binaryArchive;
	}

	@Override
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}

	@Override
	protected void closing(Object info) throws CModelException {
		ICProject cproject = getCProject();
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(cproject);
		if (pinfo != null && pinfo.vLib != null) {
			pinfo.vLib.removeChild(this);
		}
		super.closing(info);
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		return null;
	}

	@Override
	public String getHandleMemento() {
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
		return 0;
	}

}
