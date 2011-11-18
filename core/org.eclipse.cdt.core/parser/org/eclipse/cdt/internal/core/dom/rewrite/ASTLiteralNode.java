/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;

/**
 * Used for inserting literal code by means of the rewrite facility. The node
 * will never appear in an AST tree.
 * @since 5.0
 */
public class ASTLiteralNode implements IASTNode {
	private final String fCode;

	public ASTLiteralNode(String code) {
		fCode= code;
	}
	
	@Override
	public String getRawSignature() {
		return fCode;
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		if (visitor instanceof ASTWriterVisitor) {
			((ASTWriterVisitor) visitor).visit(this);
		}
		return true;
	}

	@Override
	public boolean contains(IASTNode node) {
		return false;
	}

	@Override
	public String getContainingFilename() {
		return null;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return null;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		return null;
	}

	@Override
	public IASTNode getParent() {
		return null;
	}
	
	@Override
	public IASTNode[] getChildren() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		return null;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return null;
	}

	@Override
	public boolean isPartOfTranslationUnitFile() {
		return false;
	}

	@Override
	public void setParent(IASTNode node) {
	}

	@Override
	public void setPropertyInParent(ASTNodeProperty property) {
	}

	@Override
	public IToken getSyntax() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IToken getLeadingSyntax() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IToken getTrailingSyntax() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isFrozen() {
		return false;
	}
	
	@Override
	public IASTNode copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTNode copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActive() {
		return true;
	}
}
