package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/** 
 * Info for ICProject.
 */

class CProjectInfo extends OpenableInfo {

	BinaryContainer vBin;
	ArchiveContainer vLib;
	ILibraryReference[] libReferences;
	IIncludeReference[] incReferences;
	ISourceRoot[] sourceRoots;
	IOutputEntry[] outputEntries;

	Object[] nonCResources = null;

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

		// determine if src == project
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
		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer)res;
				resources = container.members(false);
			}

			if (resources != null) {
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

	/**
	 * @param container
	 * @return
	 */
	public void setNonCResources(Object[] resources) {
		nonCResources = resources;
	}

	/*
	 * Reset the source roots and other caches
	 */
	public void resetCaches() {
		if (libReferences != null) {
			for (int i = 0; i < libReferences.length; i++) {
				try {
					((CElement)libReferences[i]).close();
				} catch (CModelException e) {
					//
				}
			}
		}
		if (incReferences != null) {
			for (int i = 0; i < incReferences.length; i++) {
				try {
					((CElement)incReferences[i]).close();
				} catch (CModelException e) {
					//
				}
			}
		}
		sourceRoots = null;
		outputEntries = null;
		setNonCResources(null);
	}

}
