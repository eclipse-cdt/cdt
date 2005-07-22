/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.CoreModel;

public class ResourceAdapterFactory implements IAdapterFactory {

	private static Class[] PROPERTIES= new Class[] {
		ICElement.class
	};
	
	//private static CElementFactory celementFactory= new CElementFactory();
	private static CoreModel celementFactory= CoreModel.getDefault();

	/**
	 * @see IAdapterFactory#getAdapterList
	 */	
	public Class[] getAdapterList() {
		return PROPERTIES;
	}
	
	/**
	 * @see IAdapterFactory#getAdapter
	 */
	public Object getAdapter(Object element, Class key) {
		if (ICElement.class.equals(key)) {
			//try {
				if (element instanceof IFile) {
					return celementFactory.create((IFile)element);
				} else if (element instanceof IFolder) {
					return celementFactory.create((IFolder)element);
				} else if (element instanceof IProject) {
					return celementFactory.create((IProject)element);
				} else if (element instanceof IWorkspaceRoot) {
					return CoreModel.create((IWorkspaceRoot)element);
				} else if (element instanceof IResource) {
					return celementFactory.create((IResource)element);
				}
			//} catch (CoreException e) {
			//	CUIPlugin.getDefault().getLog().log(e.getStatus());
			//}
		}
		return null;
	}	
}
