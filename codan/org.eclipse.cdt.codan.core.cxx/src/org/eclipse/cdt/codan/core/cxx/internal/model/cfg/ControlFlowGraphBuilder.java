/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia   - initial API and implementation
 *     Tomasz Wesolowski - Bug 348387
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.Value;

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
	HashMap<String, IBasicBlock> labels = new HashMap<>();
	private IValue value;

	/**
	 * Builds the graph.
	 */
	public CxxControlFlowGraph build(IASTFunctionDefinition def) {
		IASTStatement body = def.getBody();
		start = new CxxStartNode();
		exits = new ArrayList<>();
		dead = new ArrayList<>();
		IBasicBlock last = createSubGraph(start, body);
		if (!(last instanceof IExitNode) && !deadConnector(last)) {
			returnExit = factory.createExitNode(null);
			returnExit.setStartNode(start);
			addOutgoing(last, returnExit);
			exits.add(returnExit);
			for (IBasicBlock ds : dead) {
				IBasicBlock dl = findLast(ds);
				if (dl != null && !(dl instanceof IExitNode) && dl.getOutgoingSize() == 0 &&
						dl != returnExit) {
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

	private IBasicBlock createSubGraph(IBasicBlock prev, IASTNode body) {
		if (body instanceof IASTCompoundStatement) {
			IASTCompoundStatement comp = (IASTCompoundStatement) body;
			IASTNode[] children = comp.getChildren();
			for (int i = 0; i < children.length; i++) {
				IASTNode node = children[i];
				IBasicBlock last = createSubGraph(prev, node);
				prev = last;
			}
		} else if (body instanceof IASTExpressionStatement || body instanceof IASTDeclarationStatement || body instanceof IASTNullStatement) {
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
			IBranchNode labNode = (IBranchNode) labels.get(labelName);
			IConnectorNode conn;
			if (labNode != null) {
				conn = (IConnectorNode) labNode.getOutgoing();
				addOutgoing(prev, labNode);
			} else {
				// labeled statement contains of connector for jumps, branch for
				// label
				// and nested statement
				conn = createLabelNodes(prev, labelName);
			}
			return createSubGraph(conn, ast.getNestedStatement());
		} else if (body instanceof IASTGotoStatement) {
			IASTGotoStatement ast = (IASTGotoStatement) body;
			String labelName = ast.getName().toString();
			IConnectorNode conn;
			IBranchNode labNode = (IBranchNode) labels.get(labelName);
			if (labNode != null) {
				conn = (IConnectorNode) labNode.getOutgoing();
			} else {
				conn = createLabelNodes(null, labelName);
			}
			IJumpNode gotoNode = factory.createJumpNode();
			((JumpNode) gotoNode).setJump(conn, labNode != null);
			addOutgoing(prev, gotoNode);
			return gotoNode;
		} else if (body instanceof IASTProblemStatement) {
			// System.err.println("problem");
			CxxPlainNode node = factory.createPlainNode(body);
			addOutgoing(prev, node);
			return node;
		} else if (body == null) {
			// skip - sometimes body is empty such as no else
		} else if (body instanceof ICPPASTTryBlockStatement) {
			return createTry(prev, (ICPPASTTryBlockStatement) body);
		} else {
			System.err.println("unknown statement for cfg: " + body); //$NON-NLS-1$
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
		return functionNameExpression.getRawSignature().equals("exit"); //$NON-NLS-1$
	}

	protected CxxExitNode createExitNode(IBasicBlock prev, IASTNode body) {
		CxxExitNode node = factory.createExitNode(body);
		node.setStartNode(start);
		addOutgoing(prev, node);
		exits.add(node);
		return node;
	}

	protected IConnectorNode createLabelNodes(IBasicBlock prev, String labelName) {
		IBranchNode branch = factory.createBranchNode(labelName);
		if (prev != null)
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
		return mergeNode;
	}

	private IBasicBlock createSwitch(IBasicBlock prev, IASTSwitchStatement body) {
		DecisionNode node = factory.createDecisionNode(body.getControllerExpression());
		addOutgoing(prev, node);
		IConnectorNode conn = factory.createConnectorNode();
		node.setMergeNode(conn);
		createSwitchBody(node, conn, body.getBody());
		return conn;
	}

	private void createSwitchBody(DecisionNode switchNode, IConnectorNode mergeNode, IASTStatement body) {
		if (!(body instanceof IASTCompoundStatement))
			return; // bad
		IASTCompoundStatement comp = (IASTCompoundStatement) body;
		IASTNode[] children = comp.getChildren();
		IBasicBlock prev = switchNode;
		for (int i = 0; i < children.length; i++) {
			IASTNode elem = children[i];
			if (elem instanceof IASTCaseStatement || elem instanceof IASTDefaultStatement) {
				IBranchNode lbl = null;
				if (elem instanceof IASTCaseStatement) {
					IASTCaseStatement caseSt = (IASTCaseStatement) elem;
					lbl = factory.createBranchNode(caseSt);
				} else if (elem instanceof IASTDefaultStatement) {
					lbl = factory.createBranchNode(IBranchNode.DEFAULT);
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
			if (elem instanceof IASTBreakStatement) {
				prev = addJump(prev, mergeNode);
				continue;
			}
			IBasicBlock last = createSubGraph(prev, elem);
			prev = last;
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
		IBasicBlock endBody = createSubGraph(loopStart, forNode.getBody());
		outerContinue = savedContinue;
		outerBreak = savedBreak;
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
		IBasicBlock endBody = createSubGraph(loopStart, body.getBody());
		// Restore
		outerContinue = savedContinue;
		outerBreak = savedBreak;
		// Backward jump
		addJump(endBody, nContinue, true);
		// Connect with else branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		addJump(loopEnd, nBreak);
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
		IJumpNode jump = factory.createJumpNode();
		addOutgoing(prev, jump);
		addOutgoing(jump, conn);
		((JumpNode) jump).setBackward(backward);
		return jump;
	}

	private void addOutgoing(IBasicBlock prev, IBasicBlock node) {
		if (prev instanceof IExitNode || prev == null) {
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
				IValue dvalue = Value.create((IASTExpression) ast, 5);
				Long numericalValue = dvalue.numericalValue();
				if (numericalValue==null) return false;
				return (numericalValue==testvalue);
			}
		}
		return false;
	}
}
