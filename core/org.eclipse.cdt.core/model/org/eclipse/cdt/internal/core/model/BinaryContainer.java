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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

public class BinaryContainer extends Openable implements IBinaryContainer {

	public BinaryContainer(CProject cProject) {
		super(cProject, null, CCorePlugin.getResourceString("CoreModel.BinaryContainer.Binaries"), //$NON-NLS-1$
				ICElement.C_VCONTAINER);
	}

	void sync() {
		BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(getCProject());
		if (runner != null) {
			runner.waitIfRunning();
		}
	}

	@Override
	public IBinary[] getBinaries() throws CModelException {
		sync();
		ICElement[] e = getChildren();
		ArrayList<IBinary> list = new ArrayList<>(e.length);
		for (ICElement element : e) {
			if (element instanceof IBinary) {
				IBinary bin = (IBinary) element;
				if (bin.showInBinaryContainer()) {
					list.add(bin);
				}
			}
		}
		IBinary[] b = new IBinary[list.size()];
		list.toArray(b);
		return b;
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
		return new BinaryContainerInfo(this);
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
