/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPConstructorTemplate extends PDOMCPPMethodTemplate implements
		ICPPConstructor {

	public PDOMCPPConstructorTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPConstructor method) 
			throws CoreException, DOMException {
		super(linkage, parent, method);
	}

	public PDOMCPPConstructorTemplate(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CONSTRUCTOR_TEMPLATE;
	}
}
