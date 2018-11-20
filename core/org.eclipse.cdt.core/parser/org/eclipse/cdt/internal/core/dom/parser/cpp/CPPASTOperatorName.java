/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;

public class CPPASTOperatorName extends CPPASTName implements ICPPASTOperatorName {

	public CPPASTOperatorName() {
		super();
	}

	/**
	 * Primary constructor that should be used to initialize the CPPASTOperatorName.
	 * @throws NullPointerException if operator is null
	 */
	public CPPASTOperatorName(char[] name) {
		super(name);
	}

	@Override
	public CPPASTOperatorName copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTOperatorName copy(CopyStyle style) {
		char[] name = toCharArray();
		CPPASTOperatorName copy = new CPPASTOperatorName(name == null ? null : name.clone());
		return copy(copy, style);
	}
}
