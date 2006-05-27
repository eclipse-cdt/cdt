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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
public class PDOMSearchTreeContentProvider implements ITreeContentProvider, IPDOMSearchContentProvider {

	private TreeViewer viewer;
	private PDOMSearchResult result;
	private Map tree = new HashMap();
	
	public Object[] getChildren(Object parentElement) {
		Set children = (Set)tree.get(parentElement);
		if (children == null)
			return new Object[0];
		return children.toArray();
	}

	public Object getParent(Object element) {
		Iterator p = tree.keySet().iterator();
		while (p.hasNext()) {
			Object parent = p.next();
			Set children = (Set)tree.get(parent);
			if (children.contains(element))
				return parent;
		}
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
		tree.clear();
		if (result != null) {
			Object[] elements = result.getElements();
			for (int i = 0; i < elements.length; ++i) {
				insertSearchElement((PDOMSearchElement)elements[i]);
			}
		}
	}

	private void insertChild(Object parent, Object child) {
		Set children = (Set)tree.get(parent);
		if (children == null) {
			children = new HashSet();
			tree.put(parent, children);
		}
		children.add(child);
	}
	
	private void insertSearchElement(PDOMSearchElement element) {
		IPath path = new Path(element.getFileName());
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		if (files.length > 0) {
			for (int j = 0; j < files.length; ++j) {
				ICElement celement = CoreModel.getDefault().create(files[j]);
				insertChild(celement, element);
				insertCElement(celement);
			}
		} else {
			String pathName = path.toOSString(); 
			insertChild(pathName, element);
			insertChild(result, pathName);
		}
	}
	
	private void insertCElement(ICElement element) {
		if (element instanceof ICProject)
			insertChild(result, element);
		else {
			ICElement parent = element.getParent();
			if (parent instanceof ISourceRoot && parent.getUnderlyingResource() instanceof IProject)
				// Skip source roots that are projects
				parent = parent.getParent();
			insertChild(parent, element);
			insertCElement(parent);
		}
	}
	
	public void elementsChanged(Object[] elements) {
		if (elements != null)
			for (int i = 0; i < elements.length; ++i) {
				PDOMSearchElement element = (PDOMSearchElement)elements[i];
				if (result.getMatchCount(element) > 0)
					insertSearchElement(element);
				else
					remove(element);
			}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			};
		});
	}
	
	public void clear() {
		tree.clear();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			};
		});
	}
	
	protected void remove(Object element) {
		Object parent = getParent(element);
		if (parent == null)
			// reached the search result
			return;
		
		Set siblings = (Set)tree.get(parent);
		siblings.remove(element);
		
		if (siblings.isEmpty()) {
			// remove the parent
			remove(parent);
			tree.remove(parent);
		}
	}
	
}
