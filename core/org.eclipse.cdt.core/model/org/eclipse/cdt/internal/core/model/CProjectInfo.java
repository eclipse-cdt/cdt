package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/** 
 * Info for ICProject.
 */

class CProjectInfo extends CContainerInfo {

	BinaryContainer vBin;
	ArchiveContainer vLib;

	/**
	 */
	public CProjectInfo(CElement element) {
		super(element);
		vBin = null;
		vLib = null;
	}

	synchronized public IBinaryContainer getBinaryContainer() {
		if (vBin == null) {
			vBin = new BinaryContainer((CProject)getElement());
		}
		return vBin;
	}

	synchronized public IArchiveContainer getArchiveContainer() {
		if (vLib == null) {
			vLib = new ArchiveContainer((CProject)getElement());
		}
		return vLib;
	}

	/**
	 * @return
	 */
	public Object[] getNonCResources(IResource res) {
		if (nonCResources != null)
			return nonCResources;

		// determine if src == project and/or if bin == project
		IPath projectPath = res.getProject().getFullPath();
		ISourceRoot root = null;
		ICElement[] elements = getChildren();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof ISourceRoot) {
				ISourceRoot source = (ISourceRoot)elements[i];
				if (getElement().getPath().equals(source.getPath())) {
					root = source;
					break;
				}
			}
		}

		ArrayList notChildren = new ArrayList();
		ICElement parent = getElement();
		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer)res;
				resources = container.members(false);
			}

			if (resources != null) {
				CModelManager factory = CModelManager.getDefault();
				ICElement[] children;
				if (root == null) {
					children = getChildren();
				} else {
					children = root.getChildren();
				}
				for (int i = 0; i < resources.length; i++) {
					boolean found = false;
					for (int j = 0; j < children.length; j++) {
						IResource r = children[j].getResource();
						if (r != null && r.equals(resources[i])){
							found = true;
							break;
						}
					}
					if (!found) {
						notChildren.add(resources[i]);
					}
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			//e.printStackTrace();
		}
		setNonCResources(notChildren.toArray());	
		return nonCResources;
	}

}
