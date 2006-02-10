/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchTreeContentProvider implements ITreeContentProvider {

	private TreeViewer viewer;
	private PDOMSearchResult result;
	private HashMap tree;
	
	public Object[] getChildren(Object parentElement) {
		Set children = (Set)tree.get(parentElement);
		if (children == null)
			return new Object[0];
		return children.toArray();
	}

	public Object getParent(Object element) {
		return null;
	}

 	public boolean hasChildren(Object element) {
 		return tree.get(element) != null;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(result);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		result = (PDOMSearchResult)newInput;
		tree = new HashMap();
	}

	private void insertChild(Object parent, Object child) {
		Set children = (Set)tree.get(parent);
		if (children == null) {
			children = new HashSet();
			tree.put(parent, children);
		}
		children.add(child);
	}
	
	private void insertName(PDOMName name) {
		IASTFileLocation loc = name.getFileLocation();
		IPath path = new Path(loc.getFileName());
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		if (files.length > 0) {
			for (int j = 0; j < files.length; ++j) {
				insertChild(files[j], name);
				insertResource(files[j]);
			}
		} else {
			String pathName = path.toOSString(); 
			insertChild(pathName, name);
			insertChild(result, pathName);
		}
	}
	
	private void insertResource(IResource resource) {
		if (resource instanceof IProject) {
			insertChild(result, resource);
		} else {
			IContainer parent = resource.getParent();
			insertChild(parent, resource);
			insertResource(parent);
		}
	}
	
	public void elementsChanged(Object[] elements) {
		if (elements == null || elements.length == 0)
			return;
		
		for (int i = 0; i < elements.length; ++i) {
			PDOMName name = (PDOMName)elements[i];
			insertName(name);
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			};
		});
	}
	
	public void clear() {
	}
}
