package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

/**
 * Implements basic UI support for C elements.
 */
public class CElementAdapterFactory implements IAdapterFactory {
	
	private static Class[] PROPERTIES= new Class[] {
		IPropertySource.class,
		IResource.class,
		IWorkbenchAdapter.class,
		IProject.class,
		IWorkspaceRoot.class
	};
	
	private static CWorkbenchAdapter fgCWorkbenchAdapter= new CWorkbenchAdapter();
	
	/**
	 * @see CElementAdapterFactory#getAdapterList
	 */
	public Class[] getAdapterList() {
		return PROPERTIES;
	}

	/**
	 * @see CElementAdapterFactory#getAdapter
	 */	
	public Object getAdapter(Object element, Class key) {
		ICElement celem = (ICElement) element;
		IResource res = null;
		
		try {
			if (IPropertySource.class.equals(key)) {
				if (celem instanceof IBinary) {
					return new BinaryPropertySource((IBinary)celem);				
				} else if (celem.getElementType() == ICElement.C_UNIT) {
					IResource file = celem.getResource();
					if (file != null && file instanceof IFile) {
						return new FilePropertySource((IFile)file);
					}
				} else {
					res = celem.getResource();
					if (res != null) {
						return new ResourcePropertySource(res);
					}
				}
				return new CElementPropertySource(celem);
			} else if (IWorkspaceRoot.class.equals(key)) {
				 res = celem.getUnderlyingResource();
				if (res != null)
					return res.getWorkspace().getRoot();
			} else if (IProject.class.equals(key)) {
				res = celem.getResource();
				if (res != null)
					return res.getProject();
			} else if (IResource.class.equals(key)) {
				return celem.getResource();
			} else if (IWorkbenchAdapter.class.equals(key)) {
				return fgCWorkbenchAdapter;
			}
		} catch (CModelException e) {
		}
		return null; 
	}
}
