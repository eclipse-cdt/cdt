/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion;

public class CPPArithmeticConversion extends ArithmeticConversion {
	private static CPPArithmeticConversion sInstance = new CPPArithmeticConversion();

	public static IType convertCppOperandTypes(int operator, IType t1, IType t2) {
		return sInstance.convertOperandTypes(operator, t1, t2);
	}

	public static IType promoteCppType(IType t) {
		return sInstance.promoteType(t);
	}

	private CPPArithmeticConversion() {
	}

	@Override
	protected IBasicType createBasicType(Kind kind, int modifiers) {
		return new CPPBasicType(kind, modifiers);
	}
}
