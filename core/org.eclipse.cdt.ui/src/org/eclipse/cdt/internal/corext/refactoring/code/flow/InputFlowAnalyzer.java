/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dmitry Stalnov (dstalnov@fusionone.com) - contributed fix for
 *       o inline call that is used in a field initializer
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38137)
 *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [extract method] Missing return value,
 *         while extracting code out of a loop - https://bugs.eclipse.org/bugs/show_bug.cgi?id=213519
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;

import org.eclipse.cdt.internal.corext.util.ASTNodes;

public class InputFlowAnalyzer extends FlowAnalyzer {

	private static class LoopReentranceVisitor extends FlowAnalyzer {
		private Selection fSelection;
		private IASTNode fLoopNode;

		public LoopReentranceVisitor(FlowContext context, Selection selection, IASTNode loopNode) {
			super(context);
			fSelection= selection;
			fLoopNode= loopNode;
		}

		@Override
		protected boolean traverseNode(IASTNode node) {
			return true; // end <= fSelection.end || fSelection.enclosedBy(start, end);
		}

		@Override
		protected boolean createReturnFlowInfo(IASTReturnStatement node) {
			// Make sure that the whole return statement is selected or located before the selection.
			return ASTNodes.endOffset(node) <= fSelection.getEnd();
		}

		protected IASTNode getLoopNode() {
			return fLoopNode;
		}

		public void process(IASTNode node) {
			try {
				fFlowContext.setLoopReentranceMode(true);
				node.accept(this);
			} finally {
				fFlowContext.setLoopReentranceMode(false);
			}
		}

		@Override
		public int leave(IASTDoStatement node) {
			if (skipNode(node))
				return PROCESS_SKIP;
			DoWhileFlowInfo info= createDoWhile();
			setFlowInfo(node, info);
			info.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
			// No need to merge the condition. It was already considered by the InputFlowAnalyzer.
			info.removeLabel(null);
			return PROCESS_SKIP;
		}

		@Override
		public int leave(ICPPASTRangeBasedForStatement node) {
			if (skipNode(node))
				return PROCESS_SKIP;
			FlowInfo paramInfo= getFlowInfo(node.getDeclaration());
			FlowInfo expressionInfo= getFlowInfo(node.getInitializerClause());
			FlowInfo actionInfo= getFlowInfo(node.getBody());
			RangeBasedForFlowInfo forInfo= createRangeBasedFor();
			setFlowInfo(node, forInfo);
			// If the for statement is the outermost loop then we only have to consider
			// the action. The parameter and expression are only evaluated once.
			if (node == fLoopNode) {
				forInfo.mergeAction(actionInfo, fFlowContext);
			} else {
				// Inner for loops are evaluated in the sequence expression, parameter,
				// action.
				forInfo.mergeInitializerClause(expressionInfo, fFlowContext);
				forInfo.mergeDeclaration(paramInfo, fFlowContext);
				forInfo.mergeAction(actionInfo, fFlowContext);
			}
			forInfo.removeLabel(null);
			return PROCESS_SKIP;
		}

		@Override
		public int leave(IASTForStatement node) {
			if (skipNode(node))
				return PROCESS_SKIP;
			FlowInfo initInfo= createSequential(node.getInitializerStatement());
			FlowInfo conditionInfo= getFlowInfo(node.getConditionExpression());
			FlowInfo incrementInfo= createSequential(node.getIterationExpression());
			FlowInfo actionInfo= getFlowInfo(node.getBody());
			ForFlowInfo forInfo= createFor();
			setFlowInfo(node, forInfo);
			// The for statement is the outermost loop. In this case we only have
			// to consider the increment, condition and action.
			if (node == fLoopNode) {
				forInfo.mergeIncrement(incrementInfo, fFlowContext);
				forInfo.mergeCondition(conditionInfo, fFlowContext);
				forInfo.mergeAction(actionInfo, fFlowContext);
			} else {
				// We have to merge two different cases. One if we reenter the for statement
				// immediatelly (that means we have to consider increments, condition and action)
				// and the other case if we reenter the for in the next loop of
				// the outer loop. Then we have to consider initializations, condtion and action.
				// For a conditional flow info that means:
				// (initializations | increments) & condition & action.
				GenericConditionalFlowInfo initIncr= new GenericConditionalFlowInfo();
				initIncr.merge(initInfo, fFlowContext);
				initIncr.merge(incrementInfo, fFlowContext);
				forInfo.mergeAccessModeSequential(initIncr, fFlowContext);
				forInfo.mergeCondition(conditionInfo, fFlowContext);
				forInfo.mergeAction(actionInfo, fFlowContext);
			}
			forInfo.removeLabel(null);
			return PROCESS_SKIP;
		}
	}

	private Selection fSelection;
	private boolean fDoLoopReentrance;
	private LoopReentranceVisitor fLoopReentranceVisitor;

	public InputFlowAnalyzer(FlowContext context, Selection selection, boolean doLoopReentrance) {
		super(context);
		fSelection= selection;
		Assert.isNotNull(fSelection);
		fDoLoopReentrance= doLoopReentrance;
	}

	public FlowInfo perform(IASTFunctionDefinition node) {
		node.accept(this);
		return getFlowInfo(node);
	}

	@Override
	protected boolean traverseNode(IASTNode node) {
		return ASTNodes.endOffset(node) > fSelection.getEnd();
	}

	@Override
	protected boolean createReturnFlowInfo(IASTReturnStatement node) {
		// Make sure that the whole return statement is located after the selection.
		// There can be cases like return i + [x + 10] * 10; In this case we must not create
		// a return info node.
		return ASTNodes.offset(node) >= fSelection.getEnd();
	}

	@Override
	public int visit(IASTDoStatement node) {
		createLoopReentranceVisitor(node);
		return super.visit(node);
	}

	@Override
	public int visit(ICPPASTRangeBasedForStatement node) {
		createLoopReentranceVisitor(node);
		return super.visit(node);
	}

	@Override
	public int visit(IASTForStatement node) {
		createLoopReentranceVisitor(node);
		return super.visit(node);
	}

	@Override
	public int visit(IASTWhileStatement node) {
		createLoopReentranceVisitor(node);
		return super.visit(node);
	}

	private void createLoopReentranceVisitor(IASTNode node) {
		if (fLoopReentranceVisitor == null && fDoLoopReentrance && fSelection.coveredBy(node)) {
			fLoopReentranceVisitor= new LoopReentranceVisitor(fFlowContext, fSelection, node);
		}
	}

	@Override
	public int leave(IASTConditionalExpression node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		IASTExpression thenPart= node.getPositiveResultExpression();
		IASTExpression elsePart= node.getNegativeResultExpression();
		if ((thenPart != null && fSelection.coveredBy(thenPart)) ||
				(elsePart != null && fSelection.coveredBy(elsePart))) {
			GenericSequentialFlowInfo info= createSequential();
			setFlowInfo(node, info);
			endVisitConditional(info, node.getLogicalConditionExpression(), new IASTNode[] { thenPart, elsePart });
			return PROCESS_SKIP;
		}
		return super.leave(node);
	}

	@Override
	public int leave(IASTDoStatement node) {
		super.leave(node);
		handleLoopReentrance(node);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTIfStatement node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		IASTStatement thenPart= node.getThenClause();
		IASTStatement elsePart= node.getElseClause();
		if ((thenPart != null && fSelection.coveredBy(thenPart)) ||
				(elsePart != null && fSelection.coveredBy(elsePart))) {
			GenericSequentialFlowInfo info= createSequential();
			setFlowInfo(node, info);
			endVisitConditional(info, node.getConditionExpression(), new IASTNode[] { thenPart, elsePart });
			return PROCESS_SKIP;
		}
		return super.leave(node);
	}

	@Override
	public int leave(ICPPASTRangeBasedForStatement node) {
		super.leave(node);
		handleLoopReentrance(node);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTForStatement node) {
		super.leave(node);
		handleLoopReentrance(node);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTSwitchStatement node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		SwitchData data= createSwitchData(node);
		IRegion[] ranges= data.getRanges();
		for (int i= 0; i < ranges.length; i++) {
			IRegion range= ranges[i];
			if (fSelection.coveredBy(range)) {
				GenericSequentialFlowInfo info= createSequential();
				setFlowInfo(node, info);
				info.merge(getFlowInfo(node.getControllerExpression()), fFlowContext);
				info.merge(data.getInfo(i), fFlowContext);
				info.removeLabel(null);
				return PROCESS_SKIP;
			}
		}
		return super.leave(node, data);
	}

	@Override
	public int leave(IASTWhileStatement node) {
		super.leave(node);
		handleLoopReentrance(node);
		return PROCESS_SKIP;
	}

	private void endVisitConditional(GenericSequentialFlowInfo info, IASTNode condition, IASTNode[] branches) {
		info.merge(getFlowInfo(condition), fFlowContext);
		for (int i= 0; i < branches.length; i++) {
			IASTNode branch= branches[i];
			if (branch != null && fSelection.coveredBy(branch)) {
				info.merge(getFlowInfo(branch), fFlowContext);
				break;
			}
		}
	}

	private void handleLoopReentrance(IASTNode node) {
		if (fLoopReentranceVisitor == null || fLoopReentranceVisitor.getLoopNode() != node)
			return;

		fLoopReentranceVisitor.process(node);
		GenericSequentialFlowInfo info= createSequential();
		info.merge(getFlowInfo(node), fFlowContext);
		info.merge(fLoopReentranceVisitor.getFlowInfo(node), fFlowContext);
		setFlowInfo(node, info);
	}
}
