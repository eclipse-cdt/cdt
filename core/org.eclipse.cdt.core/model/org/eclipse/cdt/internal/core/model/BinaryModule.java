/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryElement;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class BinaryModule extends Parent implements IBinaryModule {

	IPath fPath;

	public BinaryModule(Binary parent, IPath p) {
		super(parent, p.lastSegment(), ICElement.C_VCONTAINER);
		fPath = p;
	}

	@Override
	public IBinaryElement[] getBinaryElements() throws CModelException {
		ICElement[] e = getChildren();
		IBinaryElement[] b = new IBinaryElement[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	@Override
	public IAddress getAddress() throws CModelException {
		return null;
	}

	@Override
	public IBinary getBinary() {
		return (IBinary) getParent();
	}

	@Override
	public IResource getResource() {
		IWorkspaceRoot root = getCModel().getWorkspace().getRoot();
		IPath path = getPath();
		if (path != null) {
			return root.getFileForLocation(fPath);
		}
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}

	@Override
	public IPath getPath() {
		return fPath;
	}

	@Override
	protected void generateInfos(CElementInfo info, Map<ICElement, CElementInfo> newElements, IProgressMonitor monitor)
			throws CModelException {
		newElements.put(this, info);
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
