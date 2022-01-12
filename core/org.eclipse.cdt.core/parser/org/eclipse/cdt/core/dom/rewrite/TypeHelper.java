/*******************************************************************************
 * Copyright (c) 2011, 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;

/**
 * A collection of static methods related to types.
 * @since 5.4
 */
public class TypeHelper {
	// Do not instantiate - all methods are static.
	private TypeHelper() {
	}

	/**
	 * Returns <code>true</code> if it is preferable to pass parameters of the given type to methods
	 * by reference, not by value. A parameter should be passed by reference if it is
	 * a <code>class</code>, <code>struct</code>, or <code>union</code>, and either has a nontrivial
	 * copy constructor or nontrivial destructor, or is larger than pointer.
	 *
	 * @param type the type in question.
	 * @param ast the AST used as a context.
	 * @return <code>true</code> is passing by reverence is preferable.
	 */
	public static boolean shouldBePassedByReference(IType type, IASTTranslationUnit ast) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE | SemanticUtil.TDEF);
		if (type instanceof ICompositeType) {
			try {
				CPPSemantics.pushLookupPoint(ast);
				if (type instanceof ICPPClassType) {
					ICPPClassType classType = ((ICPPClassType) type);
					if (!TypeTraits.hasTrivialCopyCtor(classType) || !TypeTraits.hasTrivialDestructor(classType)) {
						return true;
					}
				}
				SizeofCalculator calc = ((ASTTranslationUnit) ast).getSizeofCalculator();
				SizeAndAlignment sizeofPointer = calc.sizeAndAlignmentOfPointer();
				long maxSize = sizeofPointer != null ? sizeofPointer.size : 4;
				SizeAndAlignment sizeofType = calc.sizeAndAlignment(type);
				if (sizeofType == null || sizeofType.size > maxSize)
					return true;
			} finally {
				CPPSemantics.popLookupPoint();
			}
		}
		return false;
	}

	public static IType createType(IASTDeclarator declarator) {
		if (declarator.getTranslationUnit() instanceof ICPPASTTranslationUnit) {
			return CPPVisitor.createType(declarator);
		} else {
			return CVisitor.createType(declarator);
		}
	}
}
