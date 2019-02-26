/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPDependentEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;

/**
 * Binding for a non-type template parameter.
 */
public class CPPTemplateNonTypeParameter extends CPPTemplateParameter implements ICPPTemplateNonTypeParameter {
	private IType type;

	public CPPTemplateNonTypeParameter(IASTName name) {
		super(name);
	}

	@Override
	public IASTExpression getDefault() {
		IASTInitializerClause def = getDefaultClause();
		if (def instanceof IASTExpression) {
			return (IASTExpression) def;
		}

		return null;
	}

	public IASTInitializerClause getDefaultClause() {
		IASTName[] nds = getDeclarations();
		if (nds == null || nds.length == 0)
			return null;

		for (IASTName name : nds) {
			if (name != null) {
				IASTNode parent = name.getParent();
				assert parent instanceof IASTDeclarator;
				if (parent instanceof IASTDeclarator) {
					IASTDeclarator dtor = ASTQueries.findOutermostDeclarator((IASTDeclarator) parent);
					IASTInitializer initializer = dtor.getInitializer();
					if (initializer instanceof IASTEqualsInitializer) {
						return ((IASTEqualsInitializer) initializer).getInitializerClause();
					}
				}
			}
		}
		return null;
	}

	@Override
	public ICPPTemplateArgument getDefaultValue() {
		IASTInitializerClause dc = getDefault();
		IASTExpression d = null;
		if (dc instanceof IASTExpression) {
			d = (IASTExpression) dc;
		} else if (dc instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList list = (ICPPASTInitializerList) dc;
			switch (list.getSize()) {
			case 0:
				return new CPPTemplateNonTypeArgument(IntegralValue.create(0), getType());
			case 1:
				dc = list.getClauses()[0];
				if (dc instanceof IASTExpression) {
					d = (IASTExpression) dc;
				}
			}
		}

		if (d == null)
			return null;

		IValue val = ValueFactory.create(d);
		IType t = getType();
		return new CPPTemplateNonTypeArgument(val, t);
	}

	@Override
	public IType getType() {
		if (type == null) {
			IASTNode parent = getPrimaryDeclaration().getParent();
			while (parent != null) {
				if (parent instanceof ICPPASTParameterDeclaration) {
					type = CPPVisitor.createType((ICPPASTParameterDeclaration) parent, true);
					break;
				}
				parent = parent.getParent();
			}

			// C++17 template<auto>
			if (type instanceof CPPPlaceholderType) {
				type = getDependentTypeForAuto();
			}
			// template<auto...>
			if (type instanceof CPPParameterPackType
					&& ((CPPParameterPackType) type).getType() instanceof CPPPlaceholderType) {
				type = new CPPParameterPackType(getDependentTypeForAuto());
			}
		}
		return type;
	}

	private IType getDependentTypeForAuto() {
		CPPDependentEvaluation eval = new EvalBinding(this, null, getPrimaryDeclaration());
		TypeOfDependentExpression replacementType = new TypeOfDependentExpression(eval);
		replacementType.setForTemplateAuto(true);
		return replacementType;
	}

	@Override
	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
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
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return false;
	}

	@Override
	public ICPPScope asScope() throws DOMException {
		// A non-type template parameter can never appear on the left hand side
		// of a scope resolution operator.
		return null;
	}
}
