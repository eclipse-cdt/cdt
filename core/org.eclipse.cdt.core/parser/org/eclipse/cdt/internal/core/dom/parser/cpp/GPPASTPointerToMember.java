/*******************************************************************************
 *  Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
public class GPPASTPointerToMember extends CPPASTPointerToMember implements IGPPASTPointerToMember {

	public GPPASTPointerToMember() {
		super();
	}

	public GPPASTPointerToMember(IASTName n) {
		super(n);
	}

	@Override
	public GPPASTPointerToMember copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public GPPASTPointerToMember copy(CopyStyle style) {
		IASTName name = getName();
		GPPASTPointerToMember copy = new GPPASTPointerToMember(name == null ? null : name.copy(style));
		copy.setConst(isConst());
		copy.setVolatile(isVolatile());
		copy.setRestrict(isRestrict());
		return copy(copy, style);
	}
}
