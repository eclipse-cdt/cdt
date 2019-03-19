/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia   - initial API and implementation
 *     Tomasz Wesolowski - Bug 348387
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.ConnectorNode;
import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.codan.internal.core.cfg.JumpNode;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.osgi.util.NLS;

/**
 * This class creates C control flow graph
 */
public class ControlFlowGraphBuilder {
	CxxStartNode start;
	Collection<IExitNode> exits;
	Collection<IBasicBlock> dead;
	CxxExitNode returnExit;
	CxxNodeFactory factory = new CxxNodeFactory();
	IConnectorNode outerBreak;
	IConnectorNode outerContinue;
	Map<String, IBranchNode> labels = new LinkedHashMap<>();

	/**
	 * Builds the graph.
	 */
	public CxxControlFlowGraph build(IASTFunctionDefinition def) {
		IASTStatement body = def.getBody();
		start = new CxxStartNode();
		exits = new ArrayList<>();
		dead = new LinkedHashSet<>();
		IBasicBlock last = createSubGraph(start, body);
		for (IBranchNode label : labels.values()) {
			IConnectorNode conn = (IConnectorNode) label.getOutgoing();
			if (conn.getIncomingSize() <= 1 && label.getIncoming() == null)
				dead.add(label);
			else {
				dead.remove(label);
				dead.remove(conn);
			}
		}
		if (!(last instanceof IExitNode || last instanceof IJumpNode || deadConnector(last))) {
			returnExit = factory.createExitNode(null);
			returnExit.setStartNode(start);
			addOutgoing(last, returnExit);
			exits.add(returnExit);
			for (IBasicBlock ds : dead) {
				IBasicBlock dl = findLast(ds);
				if (dl != null && !(dl instanceof IExitNode) && dl.getOutgoingSize() == 0 && dl != returnExit) {
					((AbstractBasicBlock) dl).addOutgoing(returnExit);
				}
			}
		}
		CxxControlFlowGraph graph = new CxxControlFlowGraph(start, exits);
		graph.setUnconnectedNodes(dead);
		return graph;
	}

	private boolean deadConnector(IBasicBlock conn) {
		if (conn instanceof IJumpNode || conn instanceof IConnectorNode) {
			if (conn.getIncomingSize() == 0) {
				return true;
			}
			if (conn instanceof IJumpNode) {
				IJumpNode jm = (IJumpNode) conn;
				if (jm.isBackwardArc())
					return false;
			}
			IBasicBlock[] conns = conn.getIncomingNodes();
			for (int i = 0; i < conns.length; i++) {
				IBasicBlock bb = conns[i];
				if (!deadConnector(bb))
					return false;
			}
			return true;
		}
		return false;
	}

	private IBasicBlock findLast(IBasicBlock node) {
		if (node instanceof IJumpNode)
			return null;
		if (node.getOutgoingSize() == 0)
			return node;
		if (node instanceof ISingleOutgoing) {
			return findLast(((ISingleOutgoing) node).getOutgoing());
		} else if (node instanceof IDecisionNode) {
			return findLast(((IDecisionNode) node).getMergeNode().getOutgoing());
		}
		return node;
	}

	private IBasicBlock createSubGraph(IBasicBlock prev, IASTStatement body) {
		if (body instanceof IASTCompoundStatement) {
			IASTCompoundStatement comp = (IASTCompoundStatement) body;
			for (IASTStatement statement : comp.getStatements()) {
				prev = createSubGraph(prev, statement);
			}
		} else if (body instanceof IASTExpressionStatement || body instanceof IASTDeclarationStatement
				|| body instanceof IASTNullStatement) {
			if (isThrowStatement(body) || isExitStatement(body)) {
				CxxExitNode node = createExitNode(prev, body);
				return node;
			}
			CxxPlainNode node = factory.createPlainNode(body);
			addOutgoing(prev, node);
			return node;
		} else if (body instanceof IASTIfStatement) {
			return createIf(prev, (IASTIfStatement) body);
		} else if (body instanceof IASTWhileStatement) {
			return createWhile(prev, (IASTWhileStatement) body);
		} else if (body instanceof IASTForStatement) {
			return createFor(prev, (IASTForStatement) body);
		} else if (body instanceof ICPPASTRangeBasedForStatement) {
			return createRangeBasedFor(prev, (ICPPASTRangeBasedForStatement) body);
		} else if (body instanceof IASTDoStatement) {
			return createDoWhile(prev, (IASTDoStatement) body);
		} else if (body instanceof IASTReturnStatement) {
			CxxExitNode node = createExitNode(prev, body);
			return node;
		} else if (body instanceof IASTBreakStatement) {
			if (outerBreak != null)
				return addJump(prev, outerBreak);
			return prev;
		} else if (body instanceof IASTContinueStatement) {
			if (outerContinue != null)
				return addJump(prev, outerContinue);
			return prev;
		} else if (body instanceof IASTSwitchStatement) {
			return createSwitch(prev, (IASTSwitchStatement) body);
		} else if (body instanceof IASTLabelStatement) {
			IASTLabelStatement ast = (IASTLabelStatement) body;
			String labelName = ast.getName().toString();
			IBranchNode labNode = labels.get(labelName);
			IConnectorNode conn;
			if (labNode != null) {
				conn = (IConnectorNode) labNode.getOutgoing();
				addOutgoing(prev, labNode);
			} else {
				conn = createLabelNodes(prev, labelName);
			}
			return createSubGraph(conn, ast.getNestedStatement());
		} else if (body instanceof IASTGotoStatement) {
			IASTGotoStatement ast = (IASTGotoStatement) body;
			String labelName = ast.getName().toString();
			IConnectorNode conn;
			IBranchNode labNode = labels.get(labelName);
			if (labNode != null) {
				conn = (IConnectorNode) labNode.getOutgoing();
			} else {
				conn = createLabelNodes(null, labelName);
			}
			IJumpNode gotoNode = factory.createJumpNode();
			boolean backward = labNode != null; // This is not accurate XXX
			((JumpNode) gotoNode).setJump(conn, backward);
			((ConnectorNode) conn).addIncoming(gotoNode);
			addOutgoing(prev, gotoNode);
			return gotoNode;
		} else if (body instanceof IASTProblemStatement) {
			CxxPlainNode node = factory.createPlainNode(body);
			addOutgoing(prev, node);
			return node;
		} else if (body == null) {
			// skip - sometimes body is empty such as no else
		} else if (body instanceof ICPPASTTryBlockStatement) {
			return createTry(prev, (ICPPASTTryBlockStatement) body);
		} else {
			Activator.log(NLS.bind(Messages.ControlFlowGraphBuilder_unsupported_statement_type,
					body.getClass().getSimpleName()));
		}
		return prev;
	}

	private IBasicBlock createTry(IBasicBlock prev, ICPPASTTryBlockStatement body) {
		DecisionNode ifNode = factory.createDecisionNode(body);
		addOutgoing(prev, ifNode);
		IConnectorNode mergeNode = factory.createConnectorNode();
		ifNode.setMergeNode(mergeNode);
		IBranchNode tryBodyNode = factory.createBranchNode(IBranchNode.TRY_BODY);
		addOutgoing(ifNode, tryBodyNode);
		IBasicBlock tryBody = createSubGraph(tryBodyNode, body.getTryBody());
		addJump(tryBody, mergeNode);
		ICPPASTCatchHandler[] catchHandlers = body.getCatchHandlers();
		for (int i = 0; i < catchHandlers.length; i++) {
			ICPPASTCatchHandler handler = catchHandlers[i];
			IBranchNode handlerNode;
			IASTDeclaration declaration = handler.getDeclaration();
			if (declaration != null) {
				handlerNode = factory.createBranchNode(declaration);
			} else {
				handlerNode = factory.createBranchNode(IBranchNode.CATCH_ANY);
			}
			addOutgoing(ifNode, handlerNode);
			IBasicBlock els = createSubGraph(handlerNode, handler.getCatchBody());
			addJump(els, mergeNode);
		}
		return mergeNode;
	}

	private boolean isThrowStatement(IASTNode body) {
		if (!(body instanceof IASTExpressionStatement))
			return false;
		IASTExpression expression = ((IASTExpressionStatement) body).getExpression();
		if (!(expression instanceof IASTUnaryExpression))
			return false;
		return ((IASTUnaryExpression) expression).getOperator() == IASTUnaryExpression.op_throw;
	}

	private boolean isExitStatement(IASTNode body) {
		if (!(body instanceof IASTExpressionStatement))
			return false;
		IASTExpression expression = ((IASTExpressionStatement) body).getExpression();
		if (!(expression instanceof IASTFunctionCallExpression))
			return false;
		IASTExpression functionNameExpression = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
		if (functionNameExpression instanceof IASTIdExpression) {
			IASTName name = ((IASTIdExpression) functionNameExpression).getName();

			IBinding binding = name.resolveBinding();
			if (binding instanceof IFunction && ((IFunction) binding).isNoReturn()) {
				return true;
			}
		}
		return functionNameExpression.getRawSignature().equals("exit"); //$NON-NLS-1$
	}

	protected CxxExitNode createExitNode(IBasicBlock prev, IASTNode body) {
		CxxExitNode node = factory.createExitNode(body);
		node.setStartNode(start);
		addOutgoing(prev, node);
		exits.add(node);
		return node;
	}

	/**
	 * labeled statement consists of connector for jumps,
	 * branch for label and statement
	 *
	 * @param prev
	 * @param labelName
	 * @return
	 */
	protected IConnectorNode createLabelNodes(IBasicBlock prev, String labelName) {
		IBranchNode branch = factory.createBranchNode(labelName);
		if (prev == null || prev instanceof IJumpNode || prev instanceof IExitNode)
			;// don't do anything, leave dangling branch node
		else
			addOutgoing(prev, branch);
		labels.put(labelName, branch);
		IConnectorNode conn = factory.createConnectorNode();
		addOutgoing(branch, conn);
		return conn;
	}

	protected IBasicBlock createIf(IBasicBlock prev, IASTIfStatement body) {
		DecisionNode ifNode = factory.createDecisionNode(body.getConditionExpression());
		addOutgoing(prev, ifNode);
		IConnectorNode mergeNode = factory.createConnectorNode();
		ifNode.setMergeNode(mergeNode);
		IBranchNode thenNode = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(ifNode, thenNode);
		IBasicBlock then = createSubGraph(thenNode, body.getThenClause());
		addJump(then, mergeNode);
		IBranchNode elseNode = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(ifNode, elseNode);
		IBasicBlock els = createSubGraph(elseNode, body.getElseClause());
		addJump(els, mergeNode);
		fixConnector(mergeNode);
		return mergeNode;
	}

	protected void fixConnector(IConnectorNode mergeNode) {
		if (mergeNode.getIncomingSize() == 0)
			dead.add(mergeNode); // dead connector node
	}

	private IBasicBlock createSwitch(IBasicBlock prev, IASTSwitchStatement body) {
		DecisionNode node = factory.createDecisionNode(body.getControllerExpression());
		addOutgoing(prev, node);
		IConnectorNode mergeNode = factory.createConnectorNode();
		node.setMergeNode(mergeNode);
		createSwitchBody(node, mergeNode, body.getBody());
		fixConnector(mergeNode);
		return mergeNode;
	}

	private void createSwitchBody(IDecisionNode switchNode, IConnectorNode mergeNode, IASTStatement body) {
		if (!(body instanceof IASTCompoundStatement))
			return; // bad
		IASTCompoundStatement comp = (IASTCompoundStatement) body;
		IBasicBlock prev = switchNode;
		IConnectorNode savedBreak = outerBreak;
		outerBreak = mergeNode;
		boolean encounteredDefault = false;
		try {
			for (IASTStatement statement : comp.getStatements()) {
				if (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement) {
					IBranchNode lbl = null;
					if (statement instanceof IASTCaseStatement) {
						lbl = factory.createBranchNode(statement);
					} else if (statement instanceof IASTDefaultStatement) {
						lbl = factory.createBranchNode(IBranchNode.DEFAULT);
						encounteredDefault = true;
					}
					if (!(prev instanceof IExitNode) && prev != switchNode) {
						IConnectorNode here = factory.createConnectorNode();
						addJump(prev, here);
						addOutgoing(lbl, here);
						prev = here;
					} else {
						prev = lbl;
					}
					addOutgoing(switchNode, lbl);
					continue;
				}
				prev = createSubGraph(prev, statement);
			}
		} finally {
			outerBreak = savedBreak;
		}
		// If the switch didn't have an explicit 'default' case, we still have to
		// add an edge for the situation where no case was matched.
		if (!encounteredDefault) {
			if (!(prev instanceof IExitNode) && prev != switchNode) {
				addJump(prev, mergeNode);
			}
			IBranchNode defaultBranch = factory.createBranchNode(IBranchNode.DEFAULT);
			addOutgoing(switchNode, defaultBranch);
			prev = defaultBranch;
		}
		addJump(prev, mergeNode);
	}

	private IBasicBlock createFor(IBasicBlock prev, IASTForStatement forNode) {
		// Add initializer
		IPlainNode init = factory.createPlainNode(forNode.getInitializerStatement());
		addOutgoing(prev, init);
		prev = init;
		// Add continue connector
		IConnectorNode beforeCheck = factory.createConnectorNode();
		addOutgoing(prev, beforeCheck);
		// Decision node
		CxxDecisionNode decision = factory.createDecisionNode(forNode.getConditionExpression());
		addOutgoing(beforeCheck, decision);
		// Add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		decision.setMergeNode(nBreak);
		// Create body and jump to continue node
		IBranchNode loopStart = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, loopStart);
		// Set break/continue
		IConnectorNode nContinue = factory.createConnectorNode();
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = decision;
		try {
			endBody = createSubGraph(loopStart, forNode.getBody());
		} finally {
			outerContinue = savedContinue;
			outerBreak = savedBreak;
		}
		// inc
		IPlainNode inc = factory.createPlainNode(forNode.getIterationExpression());
		addOutgoing(endBody, nContinue);
		addOutgoing(nContinue, inc);
		// Connect with backward link
		addJump(inc, beforeCheck, true);
		// Add "else" branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		addJump(loopEnd, nBreak);
		fixConnector(nBreak);
		return nBreak;
	}

	private IBasicBlock createRangeBasedFor(IBasicBlock prev, ICPPASTRangeBasedForStatement forNode) {
		// Add initializer
		IPlainNode init = factory.createPlainNode(forNode.getDeclaration());
		addOutgoing(prev, init);
		prev = init;
		// Add continue connector
		IConnectorNode beforeCheck = factory.createConnectorNode();
		addOutgoing(prev, beforeCheck);
		// Decision node
		CxxDecisionNode decision = factory.createDecisionNode(forNode.getInitializerClause()); // XXX test expression
		addOutgoing(beforeCheck, decision);
		// Add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		decision.setMergeNode(nBreak);
		// Create body and jump to continue node
		IBranchNode loopStart = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, loopStart);
		// Set break/continue
		IConnectorNode nContinue = factory.createConnectorNode();
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = createSubGraph(loopStart, forNode.getBody());
		outerContinue = savedContinue;
		outerBreak = savedBreak;
		// inc
		IPlainNode inc = factory.createPlainNode(); // XXX increment
		addOutgoing(endBody, nContinue);
		addOutgoing(nContinue, inc);
		// Connect with backward link
		addJump(inc, beforeCheck, true);
		// Add "else" branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		addJump(loopEnd, nBreak);
		fixConnector(nBreak);
		return nBreak;
	}

	protected IBasicBlock createWhile(IBasicBlock prev, IASTWhileStatement body) {
		// Add continue connector
		IConnectorNode nContinue = factory.createConnectorNode();
		addOutgoing(prev, nContinue);
		// Decision node
		CxxDecisionNode decision = factory.createDecisionNode(body.getCondition());
		addOutgoing(nContinue, decision);
		// Add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		decision.setMergeNode(nBreak);
		// Create body and jump to continue node
		IBranchNode loopStart = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, loopStart);
		// Set break/continue
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = decision;
		try {
			endBody = createSubGraph(loopStart, body.getBody());
		} finally {
			// Restore
			outerContinue = savedContinue;
			outerBreak = savedBreak;
		}
		// Backward jump
		addJump(endBody, nContinue, true);
		// Connect with else branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		addJump(loopEnd, nBreak);
		fixConnector(nBreak);
		return nBreak;
	}

	protected IBasicBlock createDoWhile(IBasicBlock prev, IASTDoStatement body) {
		// Create body and jump to continue node
		IConnectorNode loopStart = factory.createConnectorNode();
		addOutgoing(prev, loopStart);
		// continue/break
		IConnectorNode nContinue = factory.createConnectorNode();
		IConnectorNode nBreak = factory.createConnectorNode();
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = createSubGraph(loopStart, body.getBody());
		// Restore
		outerContinue = savedContinue;
		outerBreak = savedBreak;
		// Add continue connector
		addOutgoing(endBody, nContinue);
		// Decision node
		CxxDecisionNode decision = factory.createDecisionNode(body.getCondition());
		addOutgoing(nContinue, decision);
		// then branch
		IBranchNode thenNode = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, thenNode);
		IJumpNode jumpToStart = factory.createJumpNode();
		addOutgoing(thenNode, jumpToStart);
		((JumpNode) jumpToStart).setBackward(true);
		// Connect with backward link
		addOutgoing(jumpToStart, loopStart);
		// Connect with else branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		// Add break connector
		decision.setMergeNode(nBreak);
		addJump(loopEnd, nBreak);
		fixConnector(nBreak);
		return nBreak;
	}

	private IJumpNode addJump(IBasicBlock prev, IConnectorNode conn) {
		return addJump(prev, conn, false);
	}

	private IJumpNode addJump(IBasicBlock prev, IConnectorNode conn, boolean backward) {
		if (prev instanceof IJumpNode)
			return (IJumpNode) prev;
		if (prev instanceof IExitNode)
			return null;
		JumpNode jump = (JumpNode) factory.createJumpNode();
		addOutgoing(prev, jump);
		jump.setJump(conn, backward);
		((ConnectorNode) conn).addIncoming(jump);
		return jump;
	}

	private void addOutgoing(IBasicBlock prev, IBasicBlock node) {
		if (prev instanceof IExitNode || prev instanceof IJumpNode || prev == null) {
			dead.add(node);
			return;
		}
		if (prev instanceof IDecisionNode) {
			if (node instanceof IBranchNode) {
				IDecisionNode decisionNode = (IDecisionNode) prev;
				if (isConstant(decisionNode, 1) && ((IBranchNode) node).getLabel().equals(IBranchNode.ELSE)) {
					dead.add(node);
					return;
				} else if (isConstant(decisionNode, 0) && ((IBranchNode) node).getLabel().equals(IBranchNode.THEN)) {
					dead.add(node);
					return;
				}
			} else {
				dead.add(node);
				return;
			}
		}
		((AbstractBasicBlock) prev).addOutgoing(node);
		if (!(node instanceof IStartNode))
			((AbstractBasicBlock) node).addIncoming(prev);
	}

	private boolean isConstant(IDecisionNode node, long testvalue) {
		if (node instanceof ICfgData) {
			IASTNode ast = (IASTNode) ((ICfgData) node).getData();
			if (ast instanceof IASTExpression) {
				Number numericalValue = ValueFactory.getConstantNumericalValue((IASTExpression) ast);
				if (numericalValue != null)
					return numericalValue.longValue() == testvalue;
			}
		}
		return false;
	}
}
