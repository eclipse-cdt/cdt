/*******************************************************************************
 * Copyright (c) 2008, 2013 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial Implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpansionExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.core.runtime.Assert;

/**
 * Ambiguity node for deciding between type-id and expression in a template argument.
 */
public class CPPASTAmbiguousTemplateArgument extends ASTAmbiguousNode implements ICPPASTAmbiguousTemplateArgument {
	private List<IASTNode> fNodes;

	/**
	 * @param nodes  nodes of type {@link IASTTypeId}, {@link IASTIdExpression}
	 * or {@link ICPPASTPackExpansionExpression}.
	 */
	public CPPASTAmbiguousTemplateArgument(IASTNode... nodes) {
		fNodes = new ArrayList<>(2);
		for (IASTNode node : nodes) {
			if (node instanceof IASTTypeId || node instanceof IASTExpression) {
				fNodes.add(node);
			} else {
				Assert.isLegal(false, node == null ? "null" : node.getClass().getName()); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected void beforeAlternative(IASTNode node) {
		// If the expression is an id-expression, the name may be shared
		// between the alternatives (see bug 316704), so make sure its parent
		// is set correctly.
		if (node instanceof IASTTypeId) {
			IASTDeclSpecifier declSpec = ((IASTTypeId) node).getDeclSpecifier();
			if (declSpec instanceof IASTNamedTypeSpecifier) {
				IASTNamedTypeSpecifier namedTypeSpec = (IASTNamedTypeSpecifier) declSpec;
				final IASTName name = namedTypeSpec.getName();
				name.setBinding(null);
				namedTypeSpec.setName(name);
			}
		} else {
			// Unwrap variadic pack expansion if necessary.
			if (node instanceof ICPPASTPackExpansionExpression)
				node = ((ICPPASTPackExpansionExpression) node).getPattern();

			if (node instanceof IASTIdExpression) {
				IASTIdExpression id = (IASTIdExpression) node;
				final IASTName name = id.getName();
				name.setBinding(null);
				id.setName(name);
			}
		}
	}

	@Override
	protected void afterResolution(ASTVisitor resolver, IASTNode best) {
		beforeAlternative(best);
	}

	@Override
	public IASTNode copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public IASTNode copy(CopyStyle style) {
		int sizeOfNodes = fNodes.size();
		IASTNode[] copyNodes = new IASTNode[sizeOfNodes];
		int arrayIndex = 0;
		for (IASTNode node : fNodes) {
			if (node != null) {
				copyNodes[arrayIndex] = node.copy(style);
			} else {
				copyNodes[arrayIndex] = null;
			}
			arrayIndex++;
		}

		ICPPASTAmbiguousTemplateArgument ambiguityNode = new CPPASTAmbiguousTemplateArgument(copyNodes);
		return ambiguityNode;
	}

	@Override
	public IASTNode[] getNodes() {
		return fNodes.toArray(new IASTNode[fNodes.size()]);
	}

	@Override
	public void addTypeId(IASTTypeId typeId) {
		assertNotFrozen();
		addNode(typeId);
	}

	@Override
	public void addExpression(IASTExpression expression) {
		assertNotFrozen();
		addNode(expression);
	}

	@Override
	public void addIdExpression(IASTIdExpression idExpression) {
		addExpression(idExpression);
	}

	@Deprecated
	@Override
	public void addIdExpression(IASTExpression idExpression) {
		addExpression(idExpression);
	}

	private void addNode(IASTNode node) {
		fNodes.add(node);
		node.setParent(this);
		node.setPropertyInParent(ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT);
	}
}
