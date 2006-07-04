/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

/**
 * Represents a node in the include browser
 */
public class IBNode implements IAdaptable {
	private IBNode fParent;
    private IBFile fRepresentedFile;
    
    // navigation info
    private IBFile fDirectiveFile;
    private String fDirectiveName;
    private int fDirectiveCharacterOffset;
    private int fHashCode;
    
    private boolean fIsSystemInclude= false;
    private boolean fIsActive= true;
    private boolean fIsRecursive;
    private long fTimestamp;

    /**
     * Creates a new node for the include browser
     */
    public IBNode(IBNode parent, IBFile represents, IBFile fileOfDirective, String nameOfDirective,
    		int charOffset, long timestamp) {
        fParent= parent;
        fRepresentedFile= represents;
        fDirectiveFile= fileOfDirective;
        fDirectiveName= nameOfDirective;
        fDirectiveCharacterOffset= charOffset;
        fIsRecursive= computeIsRecursive(fParent, represents.getLocation());
        fHashCode= computeHashCode();
        fTimestamp= timestamp;
    }
    
    private int computeHashCode() {
        int hashCode= 0;
        if (fParent != null) {
            hashCode= fParent.hashCode() * 31;
        }
        if (fDirectiveName != null) {
            hashCode+= fDirectiveName.hashCode();
        }
        else if (fRepresentedFile != null) {
            IPath path= fRepresentedFile.getLocation();
            if (path != null) {
                hashCode+= path.hashCode();
            }
        }
        return hashCode;
    }   

    public int hashCode() {
        return fHashCode;
    }
    
    public boolean equals(Object o) {
		if (!(o instanceof IBNode)) {
			return false;
		}

		IBNode rhs = (IBNode) o;
		if (fHashCode != rhs.fHashCode) {
			return false;
		}

		return (CoreUtility.safeEquals(fRepresentedFile, rhs.fRepresentedFile) && 
				CoreUtility.safeEquals(fDirectiveName, rhs.fDirectiveName));
	}
    
    private boolean computeIsRecursive(IBNode parent, IPath path) {
        if (parent == null || path == null) {
            return false;
        }
        if (path.equals(parent.getRepresentedFile().getLocation())) {
            return true;
        }
        return computeIsRecursive(parent.fParent, path);
    }

	/**
     * Returns the parent node or <code>null</code> for the root node.
     */
    public IBNode getParent() {
        return fParent;
    }

    /**
     * Returns the translation unit that requests the inclusion
     */
    public IBFile getRepresentedFile() {
        return fRepresentedFile;
    }
        
    /**
     * Returns whether this is a system include (angle-brackets).
     */
    public boolean isSystemInclude() {
        return fIsSystemInclude;
    }
    
    /**
     * Defines whether this is a system include (angle-brackets).
     * @see FileInclusionNode#isSystemInclude().
     */
    public void setIsSystemInclude(boolean isSystemInclude) {
        fIsSystemInclude= isSystemInclude;
    }
    
    /**
     * Returns whether this inclusion is actually performed with the current set
     * of macro definitions. This is true unless the include directive appears within
     * a conditional compilation construct (#ifdef/#endif).
     */
    public boolean isActiveCode() {
        return fIsActive;
    }

    /**
     * Defines whether the inclusion appears in active code.
     * @see FileInclusionNode#isInActiveCode().
     */
    public void setIsActiveCode(boolean isActiveCode) {
        fIsActive= isActiveCode;
    }

    public boolean isRecursive() {
        return fIsRecursive;
    }

    public int getDirectiveCharacterOffset() {
        return fDirectiveCharacterOffset;
    }

    public IBFile getDirectiveFile() {
        return fDirectiveFile;
    }

    public String getDirectiveName() {
        return fDirectiveName;
    }

    public Object getAdapter(Class adapter) {
        if (fRepresentedFile != null) {
            if (adapter.isAssignableFrom(ITranslationUnit.class)) {
                return fRepresentedFile.getTranslationUnit();
            }
            if (adapter.isAssignableFrom(IFile.class)) {
                return fRepresentedFile.getResource();
            }
        }
        return null;
    }

	public ITranslationUnit getRepresentedTranslationUnit() {
		return fRepresentedFile == null ? null : fRepresentedFile.getTranslationUnit();
	}

	public IPath getRepresentedPath() {
		if (fRepresentedFile == null) {
			return null;
		}
		IFile file= fRepresentedFile.getResource();
		if (file != null) {
			return file.getFullPath();
		}
		return fRepresentedFile.getLocation();
	}

	public long getTimestamp() {
		return fTimestamp;
	}
}
