/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeSpecification;

/**
 * Visitor to select nodes by image-location.
 * @since 5.0
 */
public class FindNodeByImageLocation extends ASTGenericVisitor {
	private final int fOffset;
	private final int fLength;
	private final ASTNodeSpecification<?> fNodeSpec;

	public FindNodeByImageLocation(int offset, int length, ASTNodeSpecification<?> nodeSpec) {
		super(!nodeSpec.requiresClass(IASTName.class));
		fNodeSpec= nodeSpec;
		fOffset = offset;
		fLength = length;

		shouldVisitNames = true;
		shouldVisitDeclarations= true;
	}

	@Override
	protected int genericVisit(IASTNode node) {
		return processNode(node);
	}

	private int processNode(IASTNode node) {
		if (node instanceof ASTNode) {
			final ASTNode astNode = (ASTNode) node;
			if (astNode.getOffset() > fOffset+fLength || astNode.getOffset() + astNode.getLength() < fOffset) {
				return PROCESS_SKIP;
			}

			if (fNodeSpec.isAcceptableNode(astNode)) {
				IASTImageLocation imageLocation= astNode.getImageLocation();
				if (imageLocation != null && imageLocation.getLocationKind() == IASTImageLocation.ARGUMENT_TO_MACRO_EXPANSION) {
					fNodeSpec.visit(astNode, imageLocation);
				}
			}
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		// use declarations to determine if the search has gone past the
		// offset (i.e. don't know the order the visitor visits the nodes)
		if (declaration instanceof ASTNode && ((ASTNode) declaration).getOffset() > fOffset + fLength)
			return PROCESS_ABORT;

		return processNode(declaration);
	}

	@Override
	public int visit(IASTName name) {
		if (name.toString() != null)
			return processNode(name);
		return PROCESS_CONTINUE;
	}
}