/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryElement;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryModule#getBinaryElements()
	 */
	public IBinaryElement[] getBinaryElements() throws CModelException {
		ICElement[] e = getChildren();
		IBinaryElement[] b = new IBinaryElement[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryElement#getAddress()
	 */
	public IAddress getAddress() throws CModelException {
		return null; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryElement#getBinary()
	 */
	public IBinary getBinary() {
		return (IBinary)getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() {
		IWorkspaceRoot root =  getCModel().getWorkspace().getRoot();
		IPath path = getPath();
		if (path != null) {
			return root.getFileForLocation(fPath);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#createElementInfo()
	 */
	protected CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#generateInfos(java.lang.Object, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void generateInfos(Object info, Map newElements, IProgressMonitor monitor) throws CModelException {
		newElements.put(this, info);
	}

}
