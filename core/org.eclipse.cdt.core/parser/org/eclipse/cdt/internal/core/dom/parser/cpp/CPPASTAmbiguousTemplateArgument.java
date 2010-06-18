/*******************************************************************************
 * Copyright (c) 2008, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial Implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
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
 * Ambiguity node for deciding between type-id and id-expression in a template argument.
 */
public class CPPASTAmbiguousTemplateArgument extends ASTAmbiguousNode implements ICPPASTAmbiguousTemplateArgument {

	private List<IASTNode> fNodes;
	
	/**
	 * @param nodes  nodes of type {@link IASTTypeId}, {@link IASTIdExpression} or {@link ICPPASTPackExpansionExpression}.
	 */
	public CPPASTAmbiguousTemplateArgument(IASTNode... nodes) {
		fNodes= new ArrayList<IASTNode>(2);
		for(IASTNode node : nodes) {
			if (node instanceof IASTTypeId || node instanceof IASTIdExpression) {
				fNodes.add(node);
			} else if (node instanceof ICPPASTPackExpansionExpression) {
				final IASTExpression pattern = ((ICPPASTPackExpansionExpression) node).getPattern();
				if (pattern instanceof IASTIdExpression) {
					fNodes.add(node);
				} else {
					Assert.isLegal(false, pattern == null ? "null" : pattern.getClass().getName()); //$NON-NLS-1$
				}
			} else {
				Assert.isLegal(false, node == null ? "null" : node.getClass().getName()); //$NON-NLS-1$
			}
		}
	}

	
	@Override
	protected void beforeAlternative(IASTNode node) {
		// The name may be shared between the alternatives make sure it's parent is set correctly
		if (node instanceof IASTTypeId) {
			IASTDeclSpecifier declSpec = ((IASTTypeId) node).getDeclSpecifier();
			if (declSpec instanceof IASTNamedTypeSpecifier) {
				IASTNamedTypeSpecifier namedTypeSpec= (IASTNamedTypeSpecifier) declSpec;
				final IASTName name = namedTypeSpec.getName();
				name.setBinding(null);
				namedTypeSpec.setName(name);
			}
		} else if (node instanceof IASTIdExpression) {
			IASTIdExpression id= (IASTIdExpression) node;
			final IASTName name = id.getName();
			name.setBinding(null);
			id.setName(name);
		}
	}


	@Override
	protected void afterResolution(ASTVisitor resolver, IASTNode best) {
		beforeAlternative(best);
	}


	public IASTNode copy() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IASTNode[] getNodes() {
		return fNodes.toArray(new IASTNode[fNodes.size()]);
	}

	public void addTypeId(IASTTypeId typeId) {
        assertNotFrozen();
		addNode(typeId);
	}
	
	public void addIdExpression(IASTIdExpression idExpression) {
        assertNotFrozen();
		addNode(idExpression);
	}

	public void addIdExpression(IASTExpression idExpression) {
        assertNotFrozen();
		addNode(idExpression);
	}

	private void addNode(IASTNode node) {
		fNodes.add(node);
		node.setParent(this);
		node.setPropertyInParent(ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT);
	}
}

