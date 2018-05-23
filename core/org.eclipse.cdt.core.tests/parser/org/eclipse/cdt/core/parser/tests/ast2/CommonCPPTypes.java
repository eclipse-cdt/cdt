/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;

/**
 *
 * Helper class for common type wrapping operations for tests.
 *
 */
public class CommonCPPTypes {
	public static IType char_ = CPPBasicType.CHAR;
	public static IType int_ = CPPBasicType.INT;
	public static IType void_ = CPPBasicType.VOID;
	public static IType double_ = new CPPBasicType(Kind.eDouble, 0);
	public static IType float_ = new CPPBasicType(Kind.eFloat, 0);
	public static IType constChar = constOf(char_);
	public static IType constInt = constOf(int_);
	public static IType pointerToInt = pointerTo(int_);
	public static IType pointerToConstChar = pointerTo(constChar);
	public static IType pointerToConstInt = pointerTo(constInt);
	public static IType referenceToInt = referenceTo(int_);
	public static IType referenceToConstInt = referenceTo(constInt);
	public static IType rvalueReferenceToInt = rvalueReferenceTo(int_);
	public static IType rvalueReferenceToConstInt = rvalueReferenceTo(constInt);

	public static IType pointerTo(IType type) {
		return new CPPPointerType(type);
	}

	public static IType constOf(IType type) {
		return new CPPQualifierType(type, true, false);
	}

	public static IType volatileOf(IType type) {
		return new CPPQualifierType(type, false, true);
	}

	public static IType constVolatileOf(IType type) {
		return new CPPQualifierType(type, true, true);
	}

	public static IType referenceTo(IType type) {
		return new CPPReferenceType(type, false);
	}

	public static IType rvalueReferenceTo(IType type) {
		return new CPPReferenceType(type, true);
	}
}