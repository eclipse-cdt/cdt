/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;

/**
 * Visitor to search for nodes by file offsets.
 * @since 5.0
 */
public class FindNodeForOffsetAction extends ASTGenericVisitor {
	private final ASTNodeSpecification<?> fNodeSpec;

	public FindNodeForOffsetAction(ASTNodeSpecification<?> nodeSpec) {
		super(!nodeSpec.requiresClass(IASTName.class));
		fNodeSpec = nodeSpec;

		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		includeInactiveNodes = true;

		// only visit implicit names if asked
		shouldVisitImplicitNames = shouldVisitImplicitNameAlternates = nodeSpec.requiresClass(IASTImplicitName.class);
	}

	@Override
	public int genericVisit(IASTNode node) {
		if (node instanceof ASTNode) {
			final ASTNode astNode = (ASTNode) node;
			if (!fNodeSpec.canContainMatches(astNode)) {
				return PROCESS_SKIP;
			}
			fNodeSpec.visit(astNode);
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		// use declarations to determine if the search has gone past the
		// offset (i.e. don't know the order the visitor visits the nodes)
		if (declaration instanceof ASTNode && ((ASTNode) declaration).getOffset() > fNodeSpec.getSequenceEnd())
			return PROCESS_ABORT;

		return genericVisit(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		int ret = genericVisit(declarator);

		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for (int i = 0; i < ops.length; i++)
			genericVisit(ops[i]);

		return ret;
	}
}