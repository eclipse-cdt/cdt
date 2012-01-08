/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariableDeclaration;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

/**
 * Represents a node in the include browser
 */
public class CHNode implements IAdaptable {	
	private CHNode fParent;
	private ICElement fRepresentedDecl;
	private ITranslationUnit fFileOfReferences;
    private List<CHReferenceInfo> fReferences;
    
    protected int fHashCode;
    private long fTimestamp;
    private boolean fIsRecursive;
	private boolean fIsInitializer;
	private boolean fIsReadAccess;
	private boolean fIsWriteAccess;
	private final int fLinkageID;

    /**
     * Creates a new node for the include browser
     */
    public CHNode(CHNode parent, ITranslationUnit fileOfReferences, long timestamp, ICElement decl, int linkageID) {
        fParent= parent;
        fFileOfReferences= fileOfReferences;
        fReferences= Collections.emptyList();
        fRepresentedDecl= decl;
        fIsRecursive= computeIsRecursive(fParent, decl);
        fHashCode= computeHashCode();
        fTimestamp= timestamp;
        fLinkageID= linkageID;
    }
    
	private int computeHashCode() {
        int hashCode= 1;
        if (fParent != null) {
            hashCode= fParent.hashCode() * 31;
        }
        if (fRepresentedDecl != null) {
        	hashCode+= fRepresentedDecl.hashCode();
        }
        return hashCode;
    }   

    @Override
	public int hashCode() {
        return fHashCode;
    }
    
    @Override
	public boolean equals(Object o) {
		if (!(o instanceof CHNode)) {
			return false;
		}

		CHNode rhs = (CHNode) o;
		if (fHashCode != rhs.fHashCode) {
			return false;
		}

		return CoreUtility.safeEquals(fRepresentedDecl, rhs.fRepresentedDecl);
    }
    
    private boolean computeIsRecursive(CHNode parent, ICElement decl) {
        if (parent == null || decl == null) {
            return false;
        }
        if (decl.equals(parent.fRepresentedDecl)) {
            return true;
        }
        return computeIsRecursive(parent.fParent, decl);
    }

	/**
     * Returns the parent node or <code>null</code> for the root node.
     */
    public CHNode getParent() {
        return fParent;
    }

    public int getLinkageID() {
    	return fLinkageID;
    }

	public boolean isRecursive() {
        return fIsRecursive;
    }

	public int getReferenceCount() {
		return fReferences.size();
	}
	
	public CHReferenceInfo getReference(int idx) {
		return fReferences.get(idx);
	}
	
	public ICElement getRepresentedDeclaration() {
		return fRepresentedDecl;
	}

	public long getTimestamp() {
		return fTimestamp;
	}

	public boolean isMacro() {
		return fRepresentedDecl instanceof IMacro;
	}

	public boolean isVariableOrEnumerator() {
		return fRepresentedDecl instanceof IVariableDeclaration ||
			fRepresentedDecl instanceof IEnumerator;
	}
	
	public int getFirstReferenceOffset() {
		return fReferences.isEmpty() ? -1 : getReference(0).getOffset();
	}
	
	public void addReference(CHReferenceInfo info) {
		switch (fReferences.size()) {
		case 0:
			fReferences= Collections.singletonList(info);
			return;
		case 1:
			fReferences= new ArrayList<CHReferenceInfo>(fReferences);
			break;
		}
		fReferences.add(info);
	}

	public ITranslationUnit getFileOfReferences() {
		return fFileOfReferences;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ICElement.class)) {
			return getRepresentedDeclaration();
		}
		return null;
	}
	
	public boolean isMultiDef() {
		return false;
	}

	public ICElement getOneRepresentedDeclaration() {
		return getRepresentedDeclaration();
	}

	public boolean isInitializer() {
		return fIsInitializer;
	}

	public void setInitializer(boolean isInitializer) {
		fIsInitializer = isInitializer;
	}

	public void sortReferencesByOffset() {
		if (fReferences.size() > 1) {
			Collections.sort(fReferences, CHReferenceInfo.COMPARE_OFFSET);
		}
	}

	public void setRWAccess(boolean readAccess, boolean writeAccess) {
		fIsReadAccess= readAccess;
		fIsWriteAccess= writeAccess;
	}

	public boolean isReadAccess() {
		return fIsReadAccess;
	}

	public boolean isWriteAccess() {
		return fIsWriteAccess;
	}
}
