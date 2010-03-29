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

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.ConnectorNode;
import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IExitNode;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * TODO: add description
 */
public class ControlFlowGraphBuilder {
	CxxStartNode start;
	Collection<IExitNode> exits;
	Collection<IBasicBlock> dead;
	CxxExitNode returnExit;

	/**
	 * @param def
	 * @return
	 */
	public CxxControlFlowGraph build(IASTFunctionDefinition def) {
		IASTStatement body = def.getBody();
		start = new CxxStartNode(null);
		exits = new ArrayList<IExitNode>();
		dead = new ArrayList<IBasicBlock>();
		IBasicBlock last = createSubGraph(start, body);
		if (!(last instanceof IExitNode)) {
			returnExit = new CxxExitNode(last, start, null);
			addOutgoing(last, returnExit);
		}
		return new CxxControlFlowGraph(start, exits);
	}

	/**
	 * @param start2
	 * @param body
	 */
	private IBasicBlock createSubGraph(IBasicBlock prev, IASTNode body) {
		if (body instanceof IASTCompoundStatement) {
			IASTCompoundStatement comp = (IASTCompoundStatement) body;
			IASTNode[] children = comp.getChildren();
			for (int i = 0; i < children.length; i++) {
				IASTNode node = children[i];
				IBasicBlock last = createSubGraph(prev, node);
				prev = last;
			}
		} else if (body instanceof IASTExpressionStatement
				|| body instanceof IASTDeclarationStatement) {
			CxxPlainNode node = new CxxPlainNode(prev, body);
			addOutgoing(prev, node);
			return node;
		} else if (body instanceof IASTIfStatement) {
			DecisionNode node = new DecisionNode(prev);
			addOutgoing(prev, node);
			ConnectorNode conn = new ConnectorNode();
			node.setConnectorNode(conn);
			IBasicBlock els = createSubGraph(node, ((IASTIfStatement) body)
					.getElseClause());
			conn.addIncoming(els);
			addOutgoing(els, conn);
			IBasicBlock then = createSubGraph(node, ((IASTIfStatement) body)
					.getThenClause());
			conn.addIncoming(then);
			addOutgoing(then, conn);
			return conn;
		} else if (body instanceof IASTReturnStatement) {
			CxxExitNode node = new CxxExitNode(prev, start, body);
			addOutgoing(prev, node);
			return node;
		}
		return prev;
	}

	/**
	 * @param prev
	 * @param node
	 */
	private void addOutgoing(IBasicBlock prev, IBasicBlock node) {
		if (prev instanceof IExitNode) {
			dead.add(node);
		} else if (prev instanceof AbstractBasicBlock) {
			((AbstractBasicBlock) prev).addOutgoing(node);
		}
	}
}
