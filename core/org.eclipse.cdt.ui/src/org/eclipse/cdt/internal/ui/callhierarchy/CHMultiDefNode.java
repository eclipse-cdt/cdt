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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

public class CHMultiDefNode extends CHNode {

	private CHNode[] fChildren;

	public CHMultiDefNode(CHNode parent, ITranslationUnit tu, long timestamp, ICElement[] elements, int linkageID) {
		super(parent, tu, timestamp, null, linkageID);
		if (elements.length == 0) {
			throw new IllegalArgumentException();
		}
		fHashCode+= elements[0].hashCode();
		fChildren= new CHNode[elements.length];
		for (int i = 0; i < elements.length; i++) {
			ICElement element = elements[i];
			fChildren[i]= new CHMultiDefChildNode(this, tu, timestamp, element, linkageID);
		}
	}
	
	public CHNode[] getChildNodes() {
		return fChildren;
	}

	@Override
	public boolean isMacro() {
		return fChildren[0].isMacro();
	}

	@Override
	public boolean isVariableOrEnumerator() {
		return fChildren[0].isVariableOrEnumerator();
	}

	
	@Override
	public ICElement getOneRepresentedDeclaration() {
		return fChildren[0].getRepresentedDeclaration();
	}

	@Override
	public boolean isMultiDef() {
		return true;
	}
    
    @Override
	public boolean equals(Object o) {
    	if (!super.equals(o) || !(o instanceof CHMultiDefNode)) 
    		return false;

    	final CHMultiDefNode rhs = (CHMultiDefNode) o;
		return CoreUtility.safeEquals(getOneRepresentedDeclaration(), rhs.getOneRepresentedDeclaration());
    }

}
