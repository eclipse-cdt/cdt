/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

public class THNode implements IAdaptable {
	private THNode fParent;
	private ICElement fElement;
	private List<THNode> fChildren= Collections.emptyList();
    
    private int fHashCode;
    private boolean fIsFiltered;
    private boolean fIsImplementor;

    /**
     * Creates a new node for the include browser
     */
    public THNode(THNode parent, ICElement decl) {
        fParent= parent;
        fElement= decl;
        fHashCode= computeHashCode();
    }
    
	private int computeHashCode() {
        int hashCode= 0;
        if (fParent != null) {
            hashCode= fParent.hashCode() * 31;
        }
        if (fElement != null) {
        	hashCode+= fElement.hashCode();
        }
        return hashCode;
    }   

    @Override
	public int hashCode() {
        return fHashCode;
    }
    
    @Override
	public boolean equals(Object o) {
		if (!(o instanceof THNode)) {
			return false;
		}

		THNode rhs = (THNode) o;
		if (fHashCode != rhs.fHashCode) {
			return false;
		}

		return CoreUtility.safeEquals(fElement, rhs.fElement);
    }
    
	/**
     * Returns the parent node or <code>null</code> for the root node.
     */
    public THNode getParent() {
        return fParent;
    }


	public ICElement getElement() {
		return fElement;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ICElement.class)) {
			return getElement();
		}
		return null;
	}
	
	public boolean isFiltered() {
		return fIsFiltered;
	}

	public void setIsFiltered(boolean val) {
		fIsFiltered= val;
	}

	public void addChild(THNode childNode) {
		if (fChildren.isEmpty()) {
			fChildren= new ArrayList<THNode>();
		}			
		fChildren.add(childNode);
	}

	public boolean hasChildren() {
		return !fChildren.isEmpty();
	}

	public THNode[] getChildren() {
		return fChildren.toArray(new THNode[fChildren.size()]);
	}

	public void setIsImplementor(boolean val) {
		fIsImplementor= val;
	}

	public boolean isImplementor() {
		return fIsImplementor;
	}

	public void removeFilteredLeafs() {
		for (Iterator<THNode> iterator = fChildren.iterator(); iterator.hasNext();) {
			THNode child = iterator.next();
			child.removeFilteredLeafs();
			if (child.isFiltered() && !child.hasChildren()) {
				iterator.remove();
			}
		}
	}

	public void removeNonImplementorLeafs() {
		for (Iterator<THNode> iterator = fChildren.iterator(); iterator.hasNext();) {
			THNode child = iterator.next();
			child.removeNonImplementorLeafs();
			if (!child.isImplementor() && !child.hasChildren()) {
				iterator.remove();
			}
		}
	}
}

