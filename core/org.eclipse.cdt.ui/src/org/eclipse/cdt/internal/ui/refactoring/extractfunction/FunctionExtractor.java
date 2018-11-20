/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation.Indirection;
import org.eclipse.text.edits.TextEditGroup;

/**
 * @author Mirko Stocker
 */
public abstract class FunctionExtractor {

	public static FunctionExtractor createFor(List<IASTNode> list) {
		if (list.get(0) instanceof IASTExpression) {
			return new ExpressionExtractor();
		}
		return new StatementExtractor();
	}

	public abstract boolean canChooseReturnValue();

	public abstract void constructMethodBody(IASTCompoundStatement compound, List<IASTNode> nodes,
			List<NameInformation> parameters, ASTRewrite rewrite, TextEditGroup group);

	/**
	 * Returns the declarator specifier and the pointer operators for the return type of
	 * the extracted function.
	 *
	 * @param extractedNode the first extracted node, used for expression extraction
	 * @param returnVariable the return variable or {@code null} if the there is no return variable
	 * @param pointerOperators output parameter - pointer operators for the function declarator
	 * @return the declarator specifier of the function
	 */
	public abstract IASTDeclSpecifier determineReturnType(IASTNode extractedNode, NameInformation returnVariable,
			List<IASTPointerOperator> pointerOperators);

	public abstract IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt,
			IASTExpression callExpression);

	IASTStandardFunctionDeclarator createFunctionDeclarator(IASTName name,
			IASTStandardFunctionDeclarator functionDeclarator, NameInformation returnVariable,
			List<IASTNode> nodesToWrite, Collection<NameInformation> allUsedNames, INodeFactory nodeFactory) {
		IASTStandardFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(name);

		if (functionDeclarator instanceof ICPPASTFunctionDeclarator
				&& declarator instanceof ICPPASTFunctionDeclarator) {
			if (((ICPPASTFunctionDeclarator) functionDeclarator).isConst()) {
				((ICPPASTFunctionDeclarator) declarator).setConst(true);
			}
		}

		for (IASTParameterDeclaration param : getParameterDeclarations(allUsedNames, nodeFactory)) {
			declarator.addParameterDeclaration(param);
		}

		return declarator;
	}

	public List<IASTParameterDeclaration> getParameterDeclarations(Collection<NameInformation> parameterNames,
			INodeFactory nodeFactory) {
		List<IASTParameterDeclaration> result = new ArrayList<>(parameterNames.size());
		for (NameInformation param : parameterNames) {
			result.add(param.getParameterDeclaration(nodeFactory));
		}
		return result;
	}

	/**
	 * Adjusts parameter references under the given node to account for renamed parameters and
	 * parameters passed by pointer.
	 *
	 * @param node the root node of the AST subtree to be adjusted
	 * @param changedParameters the map from references to changed parameters to parameters
	 *     themselves
	 * @param rewrite the rewrite for the node
	 * @param group the edit group to add the changes to
	 */
	protected static void adjustParameterReferences(IASTNode node,
			final Map<IASTName, NameInformation> changedParameters, final INodeFactory nodeFactory,
			final ASTRewrite rewrite, final TextEditGroup group) {
		if (changedParameters.isEmpty())
			return;
		node.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				NameInformation param = changedParameters.get(name.getOriginalNode());
				if (param != null) {
					IASTName newName = null;
					if (param.isRenamed()) {
						newName = nodeFactory.newName(param.getNewName().toCharArray());
					}
					if (param.getIndirection() == Indirection.POINTER
							&& name.getPropertyInParent() == IASTIdExpression.ID_NAME) {
						IASTIdExpression idExp = (IASTIdExpression) name.getParent();
						if (idExp.getPropertyInParent() == IASTFieldReference.FIELD_OWNER
								&& !((IASTFieldReference) idExp.getParent()).isPointerDereference()) {
							IASTFieldReference dotRef = (IASTFieldReference) idExp.getParent();
							IASTFieldReference arrowRef = dotRef.copy(CopyStyle.withLocations);
							arrowRef.setIsPointerDereference(true);
							if (newName != null) {
								idExp = (IASTIdExpression) arrowRef.getFieldOwner();
								idExp.setName(newName);
							}
							rewrite.replace(dotRef, arrowRef, group);
						} else {
							IASTIdExpression newIdExp = idExp.copy(CopyStyle.withLocations);
							IASTUnaryExpression starExp = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_star,
									newIdExp);
							if (newName != null) {
								newIdExp.setName(newName);
							}
							rewrite.replace(idExp, starExp, group);
						}
					} else if (newName != null) {
						rewrite.replace(name, newName, group);
					}
				}
				return super.visit(name);
			}
		});
	}

	/**
	 * Returns a map from references to parameters inside the body of the extracted function to
	 * the parameters themselves. The map includes only the parameters that are either renamed,
	 * or passed by pointer.
	 *
	 * @param parameters the function parameters.
	 * @return a map from references to parameters to parameters themselves.
	 */
	protected static Map<IASTName, NameInformation> getChangedParameterReferences(List<NameInformation> parameters) {
		final Map<IASTName, NameInformation> referenceLookupMap = new HashMap<>();
		for (NameInformation param : parameters) {
			if (param.isRenamed() || param.getIndirection() == Indirection.POINTER) {
				for (IASTName name : param.getReferencesInSelection()) {
					referenceLookupMap.put(name, param);
				}
			}
		}
		return referenceLookupMap;
	}
}
