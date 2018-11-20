/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents the parameter of a function.
 */
public class CParameter extends PlatformObject implements IParameter {
	public static class CParameterProblem extends ProblemBinding implements IParameter {
		public CParameterProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
	}

	private IASTName[] declarations;
	private IType type = null;

	public CParameter(IASTName parameterName) {
		this.declarations = new IASTName[] { parameterName };
	}

	@Override
	public IType getType() {
		if (type == null && declarations[0].getParent() instanceof IASTDeclarator)
			type = CVisitor.createType((IASTDeclarator) declarations[0].getParent());

		return type;
	}

	private IASTName getPrimaryDeclaration() {
		if (declarations != null) {
			for (int i = 0; i < declarations.length && declarations[i] != null; i++) {
				IASTNode node = declarations[i].getParent();
				while (!(node instanceof IASTDeclaration))
					node = node.getParent();

				if (node.getPropertyInParent() == ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER
						|| node instanceof IASTFunctionDefinition) {
					return declarations[i];
				}
			}
			return declarations[0];
		}
		return null;
	}

	@Override
	public String getName() {
		IASTName name = getPrimaryDeclaration();
		if (name != null)
			return name.toString();
		return CVisitor.EMPTY_STRING;
	}

	@Override
	public char[] getNameCharArray() {
		IASTName name = getPrimaryDeclaration();
		if (name != null)
			return name.toCharArray();
		return CharArrayUtils.EMPTY_CHAR_ARRAY;
	}

	@Override
	public IScope getScope() {
		// IASTParameterDeclaration or IASTSimpleDeclaration
		for (IASTName declaration : declarations) {
			if (declaration == null)
				break; // Skip nulls at the end of the declarations array

			IASTNode parent = declaration.getParent();
			if (parent instanceof ICASTKnRFunctionDeclarator) {
				parent = parent.getParent();
				return ((IASTCompoundStatement) ((IASTFunctionDefinition) parent).getBody()).getScope();
			}

			IASTNode fdtorNode = parent.getParent().getParent();
			if (fdtorNode instanceof IASTFunctionDeclarator) {
				IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) fdtorNode;
				parent = fdtor.getParent();
				if (parent instanceof IASTFunctionDefinition) {
					return ((IASTCompoundStatement) ((IASTFunctionDefinition) parent).getBody()).getScope();
				}
			}
		}
		// TODO: if not definition, find definition
		return null;
	}

	/**
	 * @param name the name from a parameter declaration
	 */
	public void addDeclaration(IASTName name) {
		if (name != null && name.isActive())
			declarations = ArrayUtil.append(IASTName.class, declarations, name);
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isAuto() {
		return hasStorageClass(IASTDeclSpecifier.sc_auto);
	}

	@Override
	public boolean isRegister() {
		return hasStorageClass(IASTDeclSpecifier.sc_register);
	}

	public boolean hasStorageClass(int storage) {
		if (declarations == null)
			return false;

		for (int i = 0; i < declarations.length && declarations[i] != null; i++) {
			IASTNode parent = declarations[i].getParent();
			while (!(parent instanceof IASTParameterDeclaration) && !(parent instanceof IASTDeclaration))
				parent = parent.getParent();

			IASTDeclSpecifier declSpec = null;
			if (parent instanceof IASTSimpleDeclaration) {
				declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
			} else if (parent instanceof IASTParameterDeclaration) {
				declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
			}
			if (declSpec != null)
				return declSpec.getStorageClass() == storage;
		}
		return false;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		if (declarations == null || declarations.length == 0)
			return null;

		return CVisitor.findEnclosingFunction(declarations[0]);
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}
}
