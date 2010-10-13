/*******************************************************************************
 *  Copyright (c) 2004, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;

/**
 * @deprecated
 */
@Deprecated
public class GPPASTPointerToMember extends CPPASTPointerToMember implements
        IGPPASTPointerToMember {

    public GPPASTPointerToMember() {
		super();
	}

	public GPPASTPointerToMember(IASTName n) {
		super(n);
	}

	@Override
	public GPPASTPointerToMember copy() {
		IASTName name = getName();
		GPPASTPointerToMember copy = new GPPASTPointerToMember(name == null ? null : name.copy());
		copy.setConst(isConst());
		copy.setVolatile(isVolatile());
		copy.setRestrict(isRestrict());
		copy.setOffsetAndLength(this);
		return copy;
	}
}
