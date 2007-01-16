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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

public class THNode implements IAdaptable {
	private THNode fParent;
	private ICElement fRepresentedDecl;
	private List fChildren= Collections.EMPTY_LIST;
    
    private int fHashCode;
    private boolean fIsFiltered;

    /**
     * Creates a new node for the include browser
     */
    public THNode(THNode parent, ICElement decl) {
        fParent= parent;
        fRepresentedDecl= decl;
        fHashCode= computeHashCode();
    }
    
	private int computeHashCode() {
        int hashCode= 0;
        if (fParent != null) {
            hashCode= fParent.hashCode() * 31;
        }
        if (fRepresentedDecl != null) {
        	hashCode+= fRepresentedDecl.hashCode();
        }
        return hashCode;
    }   

    public int hashCode() {
        return fHashCode;
    }
    
    public boolean equals(Object o) {
		if (!(o instanceof THNode)) {
			return false;
		}

		THNode rhs = (THNode) o;
		if (fHashCode != rhs.fHashCode) {
			return false;
		}

		return CoreUtility.safeEquals(fRepresentedDecl, rhs.fRepresentedDecl);
    }
    
	/**
     * Returns the parent node or <code>null</code> for the root node.
     */
    public THNode getParent() {
        return fParent;
    }


	public ICElement getRepresentedDeclaration() {
		return fRepresentedDecl;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ICElement.class)) {
			return getRepresentedDeclaration();
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
		switch(fChildren.size()) {
		case 0:
			fChildren= Collections.singletonList(childNode);
			break;
		case 1:
			fChildren= new ArrayList(fChildren);
			fChildren.add(childNode);
			break;
		default:
			fChildren.add(childNode);
			break;
		}
	}

	public boolean hasChildren() {
		return !fChildren.isEmpty();
	}

	public Object[] getChildren() {
		return fChildren.toArray();
	}
}

