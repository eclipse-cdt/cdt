/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArchiveContainer extends Openable implements IArchiveContainer {

	public ArchiveContainer(CProject cProject) {
		super(cProject, null, CCorePlugin.getResourceString("CoreModel.ArchiveContainer.Archives"), //$NON-NLS-1$
				ICElement.C_VCONTAINER);
	}

	@Override
	public IArchive[] getArchives() throws CModelException {
		((ArchiveContainerInfo) getElementInfo()).sync();
		ICElement[] e = getChildren();
		IArchive[] a = new IArchive[e.length];
		System.arraycopy(e, 0, a, 0, e.length);
		return a;
	}

	@Override
	public CElementInfo getElementInfo(IProgressMonitor monitor) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		synchronized (manager) {
			CElementInfo info = (CElementInfo) manager.getInfo(this);
			if (info != null) {
				return info;
			}
			info = createElementInfo();
			openWhenClosed(info, monitor);
			return info;
		}
	}

	@Override
	public CElementInfo createElementInfo() {
		return new ArchiveContainerInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements,
			IResource underlyingResource) throws CModelException {
		// this will bootstrap/start the runner for the project.
		CModelManager.getDefault().getBinaryRunner(getCProject());
		return true;
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
