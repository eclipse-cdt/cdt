/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPConstructor extends PDOMCPPMethod implements ICPPConstructor {

	public PDOMCPPConstructor(PDOMCPPLinkage linkage, PDOMNode parent, ICPPConstructor method)
			throws CoreException, DOMException {
		super(linkage, parent, method);
	}

	public PDOMCPPConstructor(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CONSTRUCTOR;
	}
	
	@Override
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		return 0;
	}
}
