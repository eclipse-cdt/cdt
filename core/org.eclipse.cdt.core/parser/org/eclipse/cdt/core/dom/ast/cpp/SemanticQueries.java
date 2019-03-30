/*******************************************************************************
 * Copyright (c) 2012, 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
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

	private static final String OPERATOR_EQ = "operator ="; //$NON-NLS-1$

	public static boolean isCopyOrMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveKind.COPY_OR_MOVE);
	}

	public static boolean isMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveKind.MOVE);
	}

	public static boolean isCopyConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveKind.COPY);
	}

	private enum CopyOrMoveKind {
		COPY, MOVE, COPY_OR_MOVE
	}

	/**
	 * @since 6.9
	 */
	public static boolean isCopyAssignmentOperator(ICPPMethod method) {
		return isAssignmentOperator(method, CopyOrMoveKind.COPY);
	}

	/**
	 * @since 6.9
	 */
	public static boolean isCopyOrMoveAssignmentOperator(ICPPMethod method) {
		return isAssignmentOperator(method, CopyOrMoveKind.COPY_OR_MOVE);
	}

	/**
	 * @since 6.9
	 */
	public static boolean isMoveAssignmentOperator(ICPPMethod method) {
		return isAssignmentOperator(method, CopyOrMoveKind.MOVE);
	}

	/**
	 * Check if the method is a copy assignment operator, i.e. an overload of "operator="
	 * with one parameter which is of the same class type.
	 * @param method The method to be checked
	 * @return True if the method is a copy assignment operator, false otherwise
	 */
	private static boolean isAssignmentOperator(ICPPMethod method, CopyOrMoveKind kind) {
		if (!OPERATOR_EQ.equals(method.getName()))
			return false;
		if (!isCallableWithNumberOfArguments(method, 1))
			return false;
		IType firstArgumentType = method.getType().getParameterTypes()[0];
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, TDEF);
		if (!(firstArgumentType instanceof ICPPReferenceType))
			return false;
		if (kind == CopyOrMoveKind.COPY && ((ICPPReferenceType) firstArgumentType).isRValueReference())
			return false;
		if (kind == CopyOrMoveKind.MOVE && !((ICPPReferenceType) firstArgumentType).isRValueReference())
			return false;
		ICPPReferenceType firstArgReferenceType = (ICPPReferenceType) firstArgumentType;
		firstArgumentType = firstArgReferenceType.getType();
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, CVTYPE);
		ICPPClassType classType = method.getClassOwner();
		if (classType instanceof ICPPClassTemplate)
			classType = CPPTemplates.createDeferredInstance((ICPPClassTemplate) classType);
		return firstArgumentType.isSameType(classType);
	}

	private static boolean isCopyOrMoveConstructor(ICPPConstructor constructor, CopyOrMoveKind kind) {
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
		if (isRvalue && kind == CopyOrMoveKind.COPY)
			return false;
		if (!isRvalue && kind == CopyOrMoveKind.MOVE)
			return false;
		firstArgumentType = firstArgReferenceType.getType();
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, CVTYPE);
		ICPPClassType classType = constructor.getClassOwner();
		if (classType instanceof ICPPClassTemplate)
			classType = CPPTemplates.createDeferredInstance((ICPPClassTemplate) classType);
		return firstArgumentType.isSameType(classType);
	}

	private static boolean isCallableWithNumberOfArguments(ICPPFunction function, int numArguments) {
		return function.getParameters().length >= numArguments && function.getRequiredArgumentCount() <= numArguments;
	}

	/**
	 * Returns all pure virtual methods of a class. Inherited pure virtual methods
	 * that have not been implemented are also returned.
	 *
	 * NOTE: The method produces complete results for template instantiations but
	 * doesn't take into account base classes and methods dependent on unspecified
	 * template parameters.
	 *
	 * @param classType
	 *            the class whose pure virtual methods should be returned
	 * @return an array containing all pure virtual methods of the class
	 * @since 6.4
	 */
	public static ICPPMethod[] getPureVirtualMethods(ICPPClassType classType) {
		FinalOverriderMap finalOverriderMap = CPPInheritance.getFinalOverriderMap(classType);
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

	/**
	 * @deprecated Use {@link SemanticQueries}{@link #getPureVirtualMethods(ICPPClassType)} instead.
	 * @since 5.6
	 */
	@Deprecated
	public static ICPPMethod[] getPureVirtualMethods(ICPPClassType classType, IASTNode point) {
		return getPureVirtualMethods(classType);
	}

	/**
	 * Returns whether a problem binding represents a name resolution error due to an unknown built-in.
	 * Importantly, this will not return true for a misuse of a known builtin, which we want to diagnose.
	 * @param binding The problem binding to test.
	 * @param node Any node in the AST. Used to access the AST root.
	 * @since 6.3
	 */
	public static boolean isUnknownBuiltin(IProblemBinding binding, IASTNode node) {
		char[] name = binding.getNameCharArray();
		boolean isBuiltin = binding.getID() == IProblemBinding.SEMANTIC_NAME_NOT_FOUND
				&& CharArrayUtils.startsWith(name, "__builtin_"); //$NON-NLS-1$
		if (isBuiltin) {
			if (node != null) {
				IASTTranslationUnit tu = node.getTranslationUnit();
				if (tu instanceof ASTTranslationUnit) {
					return !((ASTTranslationUnit) tu).isKnownBuiltin(name);
				}
			}
			return true;
		}
		return false;
	}
}
