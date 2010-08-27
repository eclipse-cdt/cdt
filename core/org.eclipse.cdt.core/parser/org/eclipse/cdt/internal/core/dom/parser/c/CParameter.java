/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		IASTName name = getPrimaryDeclaration();
		if (name != null)
			return name.toString();
		return CVisitor.EMPTY_STRING;
	}

	public char[] getNameCharArray() {
		IASTName name = getPrimaryDeclaration();
		if (name != null)
			return name.toCharArray();
		return CVisitor.EMPTY_CHAR_ARRAY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		// IASTParameterDeclaration or IASTSimpleDeclaration
		for (IASTName declaration : declarations) {
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
	 * @param name
	 */
	public void addDeclaration(IASTName name) {
		if (name != null && name.isActive())
			declarations = (IASTName[]) ArrayUtil.append(IASTName.class, declarations, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
	 */
	public boolean isExtern() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
	 */
	public boolean isAuto() {
		return hasStorageClass(IASTDeclSpecifier.sc_auto);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
	 */
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

	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	public IBinding getOwner() {
		if (declarations == null || declarations.length == 0)
			return null;

		return CVisitor.findEnclosingFunction(declarations[0]);
	}

	public IValue getInitialValue() {
		return null;
	}
}
