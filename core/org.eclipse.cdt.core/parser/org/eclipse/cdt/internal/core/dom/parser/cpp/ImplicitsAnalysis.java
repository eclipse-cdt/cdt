/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Helps analysis of the class declaration for user declared members relevant for deciding
 * which implicit bindings to declare.
 *
 * @see chapter 12 of the ISO specification
 */
final class ImplicitsAnalysis {
	private final ICPPClassType classType;
	private boolean hasConstructor;
	private boolean hasCopyConstructor;
	private boolean hasCopyAssignmentOperator;
	private boolean hasDestructor;
	private boolean hasNonStaticFields;

	ImplicitsAnalysis(ICPPASTCompositeTypeSpecifier compositeTypeSpecifier, ICPPClassType classType) {
		this.classType = classType;
		analyzeMembers(compositeTypeSpecifier);
	}

	public boolean hasUserDeclaredConstructor() {
		return hasConstructor;
	}

	public boolean hasUserDeclaredCopyConstructor() {
		return hasCopyConstructor;
	}

	public boolean hasUserDeclaredCopyAssignmentOperator() {
		return hasCopyAssignmentOperator;
	}

	public boolean hasUserDeclaredDestructor() {
		return hasDestructor;
	}

	/**
	 * Returns the number of implicit methods to declare not counting the inherited constructors.
	 */
	public int getImplicitsToDeclareCount() {
		return (!hasDestructor ? 1 : 0) + (!hasConstructor ? 1 : 0) + (!hasCopyConstructor ? 1 : 0)
				+ (!hasCopyAssignmentOperator ? 1 : 0);
	}

	private void analyzeMembers(ICPPASTCompositeTypeSpecifier compositeTypeSpecifier) {
		IASTDeclaration[] members = compositeTypeSpecifier.getMembers();
		char[] name = compositeTypeSpecifier.getName().getLookupKey();
		for (IASTDeclaration member : members) {
			IASTDeclarator dcltor = null;
			IASTDeclSpecifier spec = null;
			if (member instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) member).getDeclarators();
				if (dtors.length != 1)
					continue;
				dcltor = dtors[0];
				spec = ((IASTSimpleDeclaration) member).getDeclSpecifier();
			} else if (member instanceof IASTFunctionDefinition) {
				dcltor = ((IASTFunctionDefinition) member).getDeclarator();
				spec = ((IASTFunctionDefinition) member).getDeclSpecifier();
			}

			if (!(dcltor instanceof ICPPASTFunctionDeclarator))
				continue;

			char[] declName = ASTQueries.findInnermostDeclarator(dcltor).getName().getLookupKey();

			if (spec instanceof IASTSimpleDeclSpecifier
					&& ((IASTSimpleDeclSpecifier) spec).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
				if (CharArrayUtils.equals(declName, name)) {
					hasConstructor = true;
					IASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) dcltor).getParameters();
					if (params.length != 0 && hasTypeReferenceToClassType(params[0])
							&& parametersHaveInitializers(params, 1)) {
						hasCopyConstructor = true;
					}
				} else if (declName.length != 0 && declName[0] == '~'
						&& CharArrayUtils.equals(declName, 1, name.length, name)) {
					hasDestructor = true;
				}
			} else if (CharArrayUtils.equals(declName, OverloadableOperator.ASSIGN.toCharArray())) {
				IASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) dcltor).getParameters();
				if (params.length == 1 && hasTypeReferenceToClassType(params[0]))
					hasCopyAssignmentOperator = true;
			}

			if (hasCopyConstructor && hasDestructor && hasCopyAssignmentOperator)
				break;
		}
	}

	private boolean hasTypeReferenceToClassType(IASTParameterDeclaration decl) {
		if (decl instanceof ICPPASTParameterDeclaration) {
			IType t = CPPVisitor.createType((ICPPASTParameterDeclaration) decl, false);
			if (t != null) {
				t = SemanticUtil.getNestedType(t, TDEF);
				if (t instanceof ICPPReferenceType && !((ICPPReferenceType) t).isRValueReference()) {
					t = SemanticUtil.getNestedType(t, TDEF | REF | CVTYPE);
					return classType.isSameType(t);
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether all parameters starting at offset have initializers.
	 */
	private boolean parametersHaveInitializers(IASTParameterDeclaration[] params, int offset) {
		for (int i = offset; i < params.length; i++) {
			if (params[i].getDeclarator().getInitializer() == null)
				return false;
		}
		return true;
	}

	public boolean isDefaultConstructorConstexpr() {
		// The following condition is stronger than necessary. It is sufficient if the class doesn't contain
		// non-static fields that don't have constexpr default constructors.
		// TODO(sprigogin): Relax the condition in accordance with [dcl.constexpr] 7.1.5-4.
		if (hasNonStaticFields)
			return false;

		ICPPBase[] bases = classType.getBases();
		for (ICPPBase base : bases) {
			if (base.isVirtual())
				return false;
		}

		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType);
		for (ICPPClassType baseClass : baseClasses) {
			ICPPConstructor ctor = getDefaultConstructor(baseClass);
			if (ctor == null || !ctor.isConstexpr())
				return false;
		}
		return true;
	}

	/**
	 * Returns a user-defined or implicit default constructor for the given class.
	 */
	private static ICPPConstructor getDefaultConstructor(ICPPClassType classType) {
		for (ICPPConstructor ctor : classType.getConstructors()) {
			if (ClassTypeHelper.getMethodKind(classType, ctor) == ClassTypeHelper.MethodKind.DEFAULT_CTOR)
				return ctor;
		}
		return null;
	}
}