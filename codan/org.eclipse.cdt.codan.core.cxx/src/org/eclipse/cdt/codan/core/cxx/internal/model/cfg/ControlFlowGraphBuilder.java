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
import org.eclipse.cdt.codan.internal.core.cfg.JumpNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IStartNode;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

/**
 * TODO: add description
 */
public class ControlFlowGraphBuilder {
	CxxStartNode start;
	Collection<IExitNode> exits;
	Collection<IBasicBlock> dead;
	CxxExitNode returnExit;
	CxxNodeFactory factory = new CxxNodeFactory();

	/**
	 * @param def
	 * @return
	 */
	public CxxControlFlowGraph build(IASTFunctionDefinition def) {
		IASTStatement body = def.getBody();
		start = new CxxStartNode();
		exits = new ArrayList<IExitNode>();
		dead = new ArrayList<IBasicBlock>();
		IBasicBlock last = createSubGraph(start, body);
		if (!(last instanceof IExitNode)) {
			returnExit = (CxxExitNode) factory.createExitNode(null);
			returnExit.setStartNode(start);
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
			CxxPlainNode node = factory.createPlainNode(body);
			addOutgoing(prev, node);
			return node;
		} else if (body instanceof IASTIfStatement) {
			DecisionNode node = factory.createDecisionNode(
					((IASTIfStatement) body).getConditionExpression());
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
		} else if (body instanceof IASTWhileStatement) {
			// add continue connector
			IConnectorNode nContinue = factory.createConnectorNode();
			addOutgoing(prev, nContinue);
			// decision node
			CxxDecisionNode decision = factory.createDecisionNode(
					((IASTWhileStatement) body).getCondition());
			addOutgoing(nContinue, decision);
			// add break connector
			IConnectorNode nBreak = factory.createConnectorNode();
			addOutgoing(decision, nBreak);
			decision.setConnectorNode(nBreak);
			// create body and jump to continue node
			IBasicBlock nBody = createSubGraph(decision,
					((IASTWhileStatement) body).getBody());
			JumpNode jumpContinue = new JumpNode();
			addOutgoing(nBody, jumpContinue);
			jumpContinue.setJump(nContinue, true);
			// connect with backward link
			addOutgoing(jumpContinue, nContinue);
			
			return nBreak;
		} else if (body instanceof IASTForStatement) {
			// add initializer
			IPlainNode init = factory.createPlainNode(((IASTForStatement) body).getInitializerStatement());
			addOutgoing(prev, init);
			prev = init;
			// add continue connector
			IConnectorNode nContinue2 = factory.createConnectorNode();
			addOutgoing(prev, nContinue2);
			// decision node
			CxxDecisionNode decision = factory.createDecisionNode(
					((IASTForStatement) body).getConditionExpression());
			addOutgoing(nContinue2, decision);
			// add break connector
			IConnectorNode nBreak = factory.createConnectorNode();
			addOutgoing(decision, nBreak);
			decision.setConnectorNode(nBreak);
			// create body and jump to continue node
			IBasicBlock nBody = createSubGraph(decision,
					((IASTForStatement) body).getBody());
			// inc
			IPlainNode inc = factory.createPlainNode(((IASTForStatement) body).getIterationExpression());
			addOutgoing(nBody, inc);
			JumpNode jumpContinue = new JumpNode();
			addOutgoing(inc, jumpContinue);
			jumpContinue.setJump(nContinue2, true);
			// connect with backward link
			addOutgoing(jumpContinue, nContinue2);
			return nBreak;
		} else if (body instanceof IASTReturnStatement) {
			CxxExitNode node = factory.createExitNode(body);
			node.setStartNode(start);
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
		if (!(node instanceof IStartNode))
			((AbstractBasicBlock) node).addIncoming(prev);
		if (prev instanceof IExitNode) {
			dead.add(node);
		} else if (prev instanceof AbstractBasicBlock) {
			((AbstractBasicBlock) prev).addOutgoing(node);
		}
	}
}
