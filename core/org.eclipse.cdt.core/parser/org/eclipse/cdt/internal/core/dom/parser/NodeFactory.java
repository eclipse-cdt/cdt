/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.INodeFactory;

/**
 * Abstract base class for node factories.
 */
public abstract class NodeFactory implements INodeFactory {

	@Override
	public final void setOffsets(IASTNode node, int offset, int endOffset) {
		((ASTNode) node).setOffsetAndLength(offset, endOffset-offset);
	}

	@Override
	public final void setEndOffset(IASTNode node, int endOffset) {
		ASTNode a= (ASTNode) node;
		a.setLength(endOffset - a.getOffset());
	}

	@Override
	public final void setEndOffset(IASTNode node, IASTNode endNode) {
		ASTNode a= (ASTNode) node;
		ASTNode e= (ASTNode) endNode;
		a.setLength(e.getOffset() + e.getLength() - a.getOffset());
	}
}
