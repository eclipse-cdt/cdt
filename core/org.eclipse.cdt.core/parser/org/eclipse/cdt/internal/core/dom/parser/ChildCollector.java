/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Collector to find all children for an ast-node.
 */
class ChildCollector extends ASTGenericVisitor {
	private final IASTNode fNode;
	private List<IASTNode> fNodes;

	public ChildCollector(IASTNode node) {
		super(true);
		fNode= node;
	}

	public IASTNode[] getChildren() {
		fNode.accept(this);
		if (fNodes == null)
			return IASTNode.EMPTY_NODE_ARRAY;
		
		return fNodes.toArray(new IASTNode[fNodes.size()]);
	}
	
	@Override
	protected int genericVisit(IASTNode child) {
		if (fNodes == null) {
			if (child == fNode)
				return PROCESS_CONTINUE;
			fNodes= new ArrayList<IASTNode>();
		}
		fNodes.add(child);
		return PROCESS_SKIP;
	}
}
