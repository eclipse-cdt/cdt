/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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

public class CHMultiDefNode extends CHNode {

	private CHNode[] fChildren;

	public CHMultiDefNode(CHNode parent, ITranslationUnit tu, long timestamp, ICElement[] elements) {
		super(parent, tu, timestamp, null);
		if (elements.length == 0) {
			throw new IllegalArgumentException();
		}
		fChildren= new CHNode[elements.length];
		for (int i = 0; i < elements.length; i++) {
			ICElement element = elements[i];
			fChildren[i]= new CHNode(this, null, 0, element);
		}
	}
	
	public CHNode[] getChildNodes() {
		return fChildren;
	}

	public boolean isMacro() {
		return fChildren[0].isMacro();
	}

	public boolean isVariableOrEnumerator() {
		return fChildren[0].isVariableOrEnumerator();
	}

	
	public ICElement getOneRepresentedDeclaration() {
		return fChildren[0].getRepresentedDeclaration();
	}

	public boolean isMultiDef() {
		return true;
	}
}
