/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;
import org.eclipse.cdt.core.dom.ast.ms.IMSASTDeclspecList;

/**
 * Abstract base class for node factories.
 */
public abstract class NodeFactory implements INodeFactory {
	@Override
	public final void setOffsets(IASTNode node, int offset, int endOffset) {
		((ASTNode) node).setOffsetAndLength(offset, endOffset - offset);
	}

	@Override
	public final void setEndOffset(IASTNode node, int endOffset) {
		ASTNode a = (ASTNode) node;
		a.setLength(endOffset - a.getOffset());
	}

	@Override
	public final void setEndOffset(IASTNode node, IASTNode endNode) {
		ASTNode a = (ASTNode) node;
		ASTNode e = (ASTNode) endNode;
		a.setLength(e.getOffset() + e.getLength() - a.getOffset());
	}

	@Deprecated
	@Override
	public org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeSpecifier newGCCAttributeSpecifier() {
		return new GCCASTAttributeList();
	}

	@Override
	public IGCCASTAttributeList newGCCAttributeList() {
		return new GCCASTAttributeList();
	}

	@Override
	public IMSASTDeclspecList newMSDeclspecList() {
		return new MSASTDeclspecList();
	}
}
