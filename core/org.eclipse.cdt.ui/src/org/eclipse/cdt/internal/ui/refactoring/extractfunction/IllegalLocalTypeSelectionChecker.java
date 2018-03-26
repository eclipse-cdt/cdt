/*******************************************************************************
 * Copyright (c) 2008, 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;

class IllegalLocalTypeSelectionChecker extends AbstractSelectionChecker {
	private final List<IASTNode> nodes;
	private final IASTFunctionDefinition functionDefinition;
	private final Set<IType> selectedTypes = new HashSet<IType>();
	private final Set<IType> localTypes = new HashSet<IType>();
	private boolean hasLocalTypesInSelection;

	public IllegalLocalTypeSelectionChecker(List<IASTNode> nodes, IASTFunctionDefinition functionDefinition) {
		this.nodes = nodes;
		this.functionDefinition = functionDefinition;
	}

	@Override
	public boolean check() {
		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitExpressions = true;
					shouldVisitDeclSpecifiers = true;
					shouldVisitDeclarators = true;
				}

				@Override
				public int visit(IASTExpression expression) {
					if (expression instanceof IASTIdExpression) {
						IASTIdExpression idExpression = (IASTIdExpression) expression;
						selectedTypes.add(idExpression.getExpressionType());
					}
					return PROCESS_CONTINUE;
				}

				@Override
				public int visit(IASTDeclSpecifier declSpec) {
					if (declSpec instanceof IASTCompositeTypeSpecifier
							|| declSpec instanceof IASTEnumerationSpecifier) {
						hasLocalTypesInSelection = true;
						return PROCESS_ABORT;
					}
					return PROCESS_CONTINUE;
				}

				@Override
				public int visit(IASTDeclarator declarator) {
					IASTName name = declarator.getName();
					if (name != null) {
						IBinding binding = name.getBinding();
						if (binding instanceof IVariable) {
							IVariable variable = (IVariable) binding;
							selectedTypes.add(variable.getType());
						}
					}
					return PROCESS_CONTINUE;
				}

			});
		}

		if (hasLocalTypesInSelection) {
			errorMessage = Messages.ExtractFunctionRefactoring_LocalType;
			return false;
		}

		functionDefinition.accept(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof IASTCompositeTypeSpecifier) {
					IASTCompositeTypeSpecifier compositeTypeSpecifier = (IASTCompositeTypeSpecifier) declSpec;
					IBinding binding = compositeTypeSpecifier.getName().getBinding();
					if (binding instanceof ICPPClassType) {
						localTypes.add((ICPPClassType) binding);
					}
				} else if (declSpec instanceof IASTEnumerationSpecifier) {
					IASTEnumerationSpecifier enumerationSpecifier = (IASTEnumerationSpecifier) declSpec;
					IBinding binding = enumerationSpecifier.getName().getBinding();
					if (binding instanceof ICPPEnumeration) {
						localTypes.add((ICPPEnumeration) binding);
					}
				}
				return PROCESS_CONTINUE;
			}
		});

		for (IType fieldType : selectedTypes) {
			if (localTypes.contains(fieldType)) {
				errorMessage = Messages.ExtractFunctionRefactoring_LocalType;
				return false;
			}
		}
		return true;
	}

}