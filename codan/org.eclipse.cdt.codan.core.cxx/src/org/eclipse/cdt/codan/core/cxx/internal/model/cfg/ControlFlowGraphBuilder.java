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
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
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
			return createIf(prev, (IASTIfStatement) body);
		} else if (body instanceof IASTWhileStatement) {
			return createWhile(prev, (IASTWhileStatement) body);
		} else if (body instanceof IASTForStatement) {
			return createFor(prev, (IASTForStatement) body);
		} else if (body instanceof IASTReturnStatement) {
			CxxExitNode node = factory.createExitNode(body);
			node.setStartNode(start);
			addOutgoing(prev, node);
			return node;
		} else if (body instanceof IASTSwitchStatement) {
			return createSwitch(prev, (IASTSwitchStatement) body);
		}
		return prev;
	}

	/**
	 * @param prev
	 * @param body
	 * @return
	 */
	protected IBasicBlock createIf(IBasicBlock prev, IASTIfStatement body) {
		DecisionNode node = factory.createDecisionNode(body
				.getConditionExpression());
		addOutgoing(prev, node);
		ConnectorNode conn = new ConnectorNode();
		node.setConnectorNode(conn);
		IBasicBlock els = createSubGraph(node, body.getElseClause());
		conn.addIncoming(els);
		addOutgoing(els, conn);
		IBasicBlock then = createSubGraph(node, body.getThenClause());
		conn.addIncoming(then);
		addOutgoing(then, conn);
		return conn;
	}

	/**
	 * @param prev
	 * @param body
	 * @return
	 */
	private IBasicBlock createSwitch(IBasicBlock prev, IASTSwitchStatement body) {
		DecisionNode node = factory.createDecisionNode(body
				.getControllerExpression());
		addOutgoing(prev, node);
		ConnectorNode conn = new ConnectorNode();
		node.setConnectorNode(conn);
		createSwitchBody(node, conn, body.getBody());
		return conn;
	}

	/**
	 * @param switchNode
	 * @param conn
	 * @param def
	 * @param body
	 */
	private void createSwitchBody(DecisionNode switchNode, ConnectorNode conn,
			IASTStatement body) {
		if (!(body instanceof IASTCompoundStatement))
			return; // bad
		IASTCompoundStatement comp = (IASTCompoundStatement) body;
		IASTNode[] children = comp.getChildren();
		IBasicBlock prev = switchNode;
		int labelNum = 1;
		for (int i = 0; i < children.length; i++) {
			IASTNode elem = children[i];
			if (elem instanceof IASTCaseStatement) {
				IASTCaseStatement caseSt = (IASTCaseStatement) elem;
				IPlainNode nextSt = factory
						.createPlainNode();
				if (!(prev instanceof IExitNode) && prev != switchNode)
					addOutgoing(prev, nextSt);
				switchNode.addOutgoing(new CxxDecisionArc(switchNode, labelNum,
						nextSt, caseSt));
				labelNum++;
				((AbstractBasicBlock)nextSt).addIncoming(switchNode);
				prev = nextSt;
				continue;
			}
			if (elem instanceof IASTBreakStatement) {
				JumpNode nBreak = (JumpNode) factory.createJumpNode();
				addOutgoing(prev, nBreak);
				nBreak.setJump(conn, false);
				conn.addIncoming(nBreak);
				prev = nBreak;
				continue;
			}
			if (elem instanceof IASTDefaultStatement) {
				IASTDefaultStatement caseSt = (IASTDefaultStatement) elem;
				IPlainNode nextSt = factory
				.createPlainNode();
				if (!(prev instanceof IExitNode) && prev != switchNode)
					addOutgoing(prev, nextSt);
				switchNode.addOutgoing(new CxxDecisionArc(switchNode, 0,
						nextSt, caseSt));
				((AbstractBasicBlock)nextSt).addIncoming(switchNode);
				prev = nextSt;
				continue;
			}
			IBasicBlock last = createSubGraph(prev, elem);
			prev = last;
		}
	}

	/**
	 * @param prev
	 * @param forNode
	 * @return
	 */
	private IBasicBlock createFor(IBasicBlock prev, IASTForStatement forNode) {
		// add initializer
		IPlainNode init = factory.createPlainNode(forNode
				.getInitializerStatement());
		addOutgoing(prev, init);
		prev = init;
		// add continue connector
		IConnectorNode nContinue2 = factory.createConnectorNode();
		addOutgoing(prev, nContinue2);
		// decision node
		CxxDecisionNode decision = factory.createDecisionNode(forNode
				.getConditionExpression());
		addOutgoing(nContinue2, decision);
		// add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		addOutgoing(decision, nBreak);
		decision.setConnectorNode(nBreak);
		// create body and jump to continue node
		IBasicBlock nBody = createSubGraph(decision, forNode.getBody());
		// inc
		IPlainNode inc = factory.createPlainNode(forNode
				.getIterationExpression());
		addOutgoing(nBody, inc);
		JumpNode jumpContinue = new JumpNode();
		addOutgoing(inc, jumpContinue);
		jumpContinue.setJump(nContinue2, true);
		// connect with backward link
		addOutgoing(jumpContinue, nContinue2);
		return nBreak;
	}

	/**
	 * @param prev
	 * @param body
	 * @return
	 */
	protected IBasicBlock createWhile(IBasicBlock prev, IASTWhileStatement body) {
		// add continue connector
		IConnectorNode nContinue = factory.createConnectorNode();
		addOutgoing(prev, nContinue);
		// decision node
		CxxDecisionNode decision = factory.createDecisionNode(body
				.getCondition());
		addOutgoing(nContinue, decision);
		// add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		addOutgoing(decision, nBreak);
		decision.setConnectorNode(nBreak);
		// create body and jump to continue node
		IBasicBlock nBody = createSubGraph(decision, body.getBody());
		JumpNode jumpContinue = new JumpNode();
		addOutgoing(nBody, jumpContinue);
		jumpContinue.setJump(nContinue, true);
		// connect with backward link
		addOutgoing(jumpContinue, nContinue);
		return nBreak;
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
