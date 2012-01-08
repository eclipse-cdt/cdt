/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;

/**
 * C++-specific enumerator.
 */
public class CPPASTEnumerator extends ASTEnumerator {
	
    public CPPASTEnumerator() {
    	super();
	}

	public CPPASTEnumerator(IASTName name, IASTExpression value) {
		super(name, value);
	}
	
	@Override
	public CPPASTEnumerator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTEnumerator copy(CopyStyle style) {
		CPPASTEnumerator copy = new CPPASTEnumerator();
		copyAbstractEnumerator(copy, style);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
}
