/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

/**
 * Represents a reference to a constructor (instance), which cannot be resolved because 
 * it depends on a template parameter. A compiler would resolve it during instantiation.
 */
public class CPPUnknownConstructor extends CPPUnknownFunction implements ICPPConstructor {

	public CPPUnknownConstructor(ICPPClassType owner) {
		super(owner, owner.getNameCharArray());
	}

	public boolean isExplicit() throws DOMException {
		return false;
	}

	public boolean isDestructor() throws DOMException {
		return false;
	}

	public boolean isImplicit() {
		return false;
	}

	public boolean isPureVirtual() throws DOMException {
		return false;
	}

	public boolean isVirtual() throws DOMException {
		return false;
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return (ICPPClassType) getOwner();
	}

	public int getVisibility() throws DOMException {
		return v_public;
	}
}
