/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.INodeFactory;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.NodeFactory;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxNodeFactory extends NodeFactory implements INodeFactory {
	public CxxNodeFactory() {
		super();
	}

	public IPlainNode createPlainNode() {
		return new CxxPlainNode();
	}

	public IDecisionNode createDecisionNode() {
		return new CxxDecisionNode();
	}

	public IExitNode createExitNode() {
		return new CxxExitNode();
	}

	public CxxPlainNode createPlainNode(IASTNode ast) {
		IPlainNode node = createPlainNode();
		((AbstractBasicBlock) node).setData(ast);
		return (CxxPlainNode) node;
	}

	public CxxDecisionNode createDecisionNode(IASTNode ast) {
		IDecisionNode node = createDecisionNode();
		((AbstractBasicBlock) node).setData(ast);
		return (CxxDecisionNode) node;
	}

	public CxxExitNode createExitNode(IASTNode ast) {
		IExitNode node = createExitNode();
		((AbstractBasicBlock) node).setData(ast);
		return (CxxExitNode) node;
	}

	/**
	 * @param caseSt
	 * @return
	 */
	public IBranchNode createBranchNode(IASTNode caseSt) {
		IBranchNode node = createBranchNode(caseSt.getRawSignature());
		((AbstractBasicBlock) node).setData(caseSt);
		return node;
	}
}
