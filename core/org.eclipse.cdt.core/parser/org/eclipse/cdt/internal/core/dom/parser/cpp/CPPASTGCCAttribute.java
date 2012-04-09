/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTGCCAttribute;

/**
 * C++-specific attribute.
 */
public class CPPASTGCCAttribute extends ASTGCCAttribute {
	
    public CPPASTGCCAttribute() {
    	super();
	}

	public CPPASTGCCAttribute(IASTName name) {
		super(name);
	}
	
	@Override
	public CPPASTGCCAttribute copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTGCCAttribute copy(CopyStyle style) {
		return copy(new CPPASTGCCAttribute(), style);
	}
}
