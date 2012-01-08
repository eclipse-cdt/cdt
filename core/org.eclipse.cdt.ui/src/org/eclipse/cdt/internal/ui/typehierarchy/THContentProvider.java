/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class THContentProvider implements ITreeContentProvider {
	private static final Object[] NO_CHILDREN= new Object[0];
	private THHierarchyModel fModel; 

	public THContentProvider() {
	}
    
    @Override
	final public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	fModel= (THHierarchyModel) newInput;
    }

    @Override
	public void dispose() {
    	fModel= null;
    }

    @Override
	final public Object[] getElements(Object inputElement) {
    	if (fModel == null) {
    		return NO_CHILDREN;
    	}
        return fModel.getHierarchyRootElements();
    }

    @Override
	final public boolean hasChildren(Object element) {
    	if (element instanceof THNode) {
    		return ((THNode) element).hasChildren();
    	}
    	return false;
    }

    @Override
	public Object[] getChildren(Object element) {
    	if (element instanceof THNode) {
    		return ((THNode) element).getChildren();
    	}
    	return NO_CHILDREN;
    }

	@Override
	public Object getParent(Object element) {
    	if (element instanceof THNode) {
    		return ((THNode) element).getParent();
    	}
    	return null;
	}
}
