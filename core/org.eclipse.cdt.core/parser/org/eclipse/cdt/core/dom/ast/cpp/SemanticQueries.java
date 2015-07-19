/*******************************************************************************
 * Copyright (c) 2012, 2015 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPInheritance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPInheritance.FinalOverriderMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * This class exposes semantic queries about C++ code to clients such
 * as code analysis.
 * 
 * @since 5.5
 */
public class SemanticQueries {

	public static boolean isCopyOrMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.COPY_OR_MOVE);
	}

	public static boolean isMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.MOVE);
	}

	public static boolean isCopyConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.COPY);
	}

	private enum CopyOrMoveConstructorKind { COPY, MOVE, COPY_OR_MOVE }

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
		ICPPClassType classType = constructor.getClassOwner();
		if (classType instanceof ICPPClassTemplate)
			classType = CPPTemplates.createDeferredInstance((ICPPClassTemplate) classType);
		return firstArgumentType.isSameType(classType);
	}

	private static boolean isCallableWithNumberOfArguments(ICPPFunction function, int numArguments) {
		return function.getParameters().length >= numArguments
			&& function.getRequiredArgumentCount() <= numArguments;
	}

	/**
	 * Returns all pure virtual methods of a class. Inherited pure virtual methods
	 * that have not been implemented are also returned. 
	 *
	 * NOTE: The method produces complete results for template instantiations
	 * but doesn't take into account base classes and methods dependent on unspecified
	 * template parameters.
	 * 
	 * @param classType the class whose pure virtual methods should be returned
	 * @param point the point of template instantiation, if applicable
	 * @return an array containing all pure virtual methods of the class
	 * @since 5.6
	 */
	public static ICPPMethod[] getPureVirtualMethods(ICPPClassType classType, IASTNode point) {
		FinalOverriderMap finalOverriderMap = CPPInheritance.getFinalOverriderMap(classType, point);
		List<ICPPMethod> pureVirtualMethods = new ArrayList<>();
		for (ICPPMethod method : finalOverriderMap.getMap().keySet()) {
			if (method.isPureVirtual()) {
				Map<Integer, List<ICPPMethod>> finalOverriders = finalOverriderMap.getMap().get(method);
				for (Integer subobjectNumber : finalOverriders.keySet()) {
					List<ICPPMethod> overridersForSubobject = finalOverriders.get(subobjectNumber);
					if (overridersForSubobject.size() == 1 && overridersForSubobject.get(0) == method) {
						pureVirtualMethods.add(method);
					}
				}
			}
		}
		return pureVirtualMethods.toArray(new ICPPMethod[pureVirtualMethods.size()]);
	}
}
