/*******************************************************************************
 * Copyright (c) 2012 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.ast;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class SemanticQueries {

	private static boolean isCallableWithNumberOfArguments(ICPPFunction function, int numArguments) {
		return function.getParameters().length >= numArguments
			&& function.getRequiredArgumentCount() <= numArguments; 
	}

	enum CopyOrMoveConstructorKind { COPY, MOVE, DONT_CARE }
	
	private static boolean isCopyOrMoveConstructor(ICPPConstructor constructor, CopyOrMoveConstructorKind kind) {
		// 12.8/2-3 [class.copy]: 
		// "A non-template constructor for class X is a copy [move] constructor
		//  if its first parameter is of type X&[&], const X&[&], volatile X&[&] 
		//  or const volatile X&[&], and either there are no other parametrs or
		//  else all other parametrs have default arguments."
		if (constructor instanceof ICPPFunctionTemplate)
			return false;
		if (!isCallableWithNumberOfArguments(constructor, 1))
			return false;
		IType firstArgumentType = constructor.getType().getParameterTypes()[0];
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, TDEF);
		if (!(firstArgumentType instanceof ICPPReferenceType))
			return false;
		ICPPReferenceType firstArgReferenceType = (ICPPReferenceType) firstArgumentType;
		boolean isRvalue = firstArgReferenceType.isRValueReference();
		if (isRvalue && kind == CopyOrMoveConstructorKind.COPY) 
			return false;
		if (!isRvalue && kind == CopyOrMoveConstructorKind.MOVE)
			return false;
		firstArgumentType = firstArgReferenceType.getType();
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, CVTYPE);
		return firstArgumentType.isSameType(constructor.getClassOwner());
	}
	
	public static boolean isCopyOrMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.DONT_CARE);
	}
	
	public static boolean isMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.MOVE);
	}

	public static boolean isCopyConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.COPY);
	}
}
