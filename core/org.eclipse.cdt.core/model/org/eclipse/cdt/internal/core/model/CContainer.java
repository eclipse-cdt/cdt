package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CContainer extends Openable implements ICContainer {

	public CContainer (ICElement parent, IResource res) {
		this (parent, res, ICElement.C_CCONTAINER);
	}

	public CContainer (ICElement parent, IResource res, int type) {
		super (parent, res, type);
	}

	/**
	 * Returns a the collection of binary files in this ccontainer
	 *
	 * @see ICContainer#getBinaries()
	 */
	public IBinary[] getBinaries() throws CModelException {
		ArrayList list = getChildrenOfType(C_BINARY);
		IBinary[] array = new IBinary[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * Returns a the collection of archive files in this ccontainer
	 *
	 * @see ICContainer#getArchives()
	 */
	public IArchive[] getArchives() throws CModelException {
		ArrayList list = getChildrenOfType(C_ARCHIVE);
		IArchive[] array = new IArchive[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * @see ICContainer#getTranslationUnits()
	 */
	public ITranslationUnit[] getTranslationUnit() throws CModelException {
		ArrayList list = getChildrenOfType(C_UNIT);
		ITranslationUnit[] array = new ITranslationUnit[list.size()];
		list.toArray(array);
		return array;
	}

	protected CElementInfo createElementInfo () {
		return new CContainerInfo(this);
	}
	
	// CHECKPOINT: folders will return the hash code of their path
	public int hashCode() {
		return getPath().hashCode();
	}

	/**
	 * @see Openable
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm,
		Map newElements, IResource underlyingResource) throws CModelException {

		boolean validInfo = false;
		try {
			IResource res = getResource();
			if (res != null && (res instanceof IWorkspaceRoot || res.getProject().isOpen())) {
				// put the info now, because computing the roots requires it
				CModelManager.getDefault().putInfo(this, info);
				validInfo = computeChildren(info, res);
			}
		} finally {
			if (!validInfo) {
				CModelManager.getDefault().removeInfo(this);
			}
		}
		return validInfo;
	}

	/* (non-Javadoc)
	 * Returns an array of non-c resources contained in the receiver.
	 * @see org.eclipse.cdt.core.model.ICContainer#getNonCResources()
	 */
	public Object[] getNonCResources() throws CModelException {
		return ((CContainerInfo)getElementInfo()).getNonCResources(getResource());
	}

	protected  boolean computeChildren(OpenableInfo info, IResource res) {
		ArrayList vChildren = new ArrayList();
		ArrayList notChildren = new ArrayList();
		try {
			IResource[] resources = null;
			if (res != null) {
				//System.out.println ("  Resource: " + res.getFullPath().toOSString());
				switch(res.getType()) {
					case IResource.ROOT:
					case IResource.PROJECT:
					case IResource.FOLDER:
						IContainer container = (IContainer)res;
						resources = container.members(false);
						break;

					case IResource.FILE:
						break;
				}
			}

			if (resources != null) {
				CModelManager factory = CModelManager.getDefault();
				for (int i = 0; i < resources.length; i++) {
					// Check for Valid C Element only.
					ICElement celement  = factory.create(this, resources[i]);
					if (celement != null) {
						vChildren.add(celement);
					} else {
						notChildren.add(resources[i]);
					}
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			e.printStackTrace();
		}
		ICElement[] children = new ICElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
		((CContainerInfo)getElementInfo()).setNonCResources(notChildren.toArray());
		return true;
	}
}
