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

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Represents a node in the include browser
 */
public class CHMultiDefChildNode extends CHNode {

    /**
     * Creates a new node for the include browser
     */
    public CHMultiDefChildNode(CHMultiDefNode parent, ITranslationUnit fileOfReferences, long timestamp, ICElement decl) {
    	super(parent, fileOfReferences, timestamp, decl);
    }
    
	public int getReferenceCount() {
		return getParent().getReferenceCount();
	}
	
	public CHReferenceInfo getReference(int idx) {
		return getParent().getReference(idx);
	}
	
	public int getFirstReferenceOffset() {
		return getParent().getFirstReferenceOffset();
	}
	
	public void addReference(CHReferenceInfo info) {
		assert false;
	}
}
