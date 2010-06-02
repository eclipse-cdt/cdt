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
package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Represents a child of a node with multiple definitions.
 */
public class CHMultiDefChildNode extends CHNode {

    public CHMultiDefChildNode(CHMultiDefNode parent, ITranslationUnit fileOfReferences, long timestamp, ICElement decl, int linkageID) {
    	super(parent, fileOfReferences, timestamp, decl, linkageID);
    }
    
	@Override
	public int getReferenceCount() {
		return getParent().getReferenceCount();
	}
	
	@Override
	public CHReferenceInfo getReference(int idx) {
		return getParent().getReference(idx);
	}
	
	@Override
	public int getFirstReferenceOffset() {
		return getParent().getFirstReferenceOffset();
	}
	
	@Override
	public void addReference(CHReferenceInfo info) {
		assert false;
	}
}
