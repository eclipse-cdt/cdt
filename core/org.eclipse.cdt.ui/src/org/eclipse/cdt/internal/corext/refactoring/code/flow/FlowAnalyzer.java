/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Special flow analyzer to determine the return value of the extracted method
 * and the variables which have to be passed to the method.
 *
 * Note: This analyzer doesn't do a full flow analysis. For example it doesn't
 * do dead code analysis or variable initialization analysis. It analyzes
 * the first access to a variable (read or write) and if all execution paths
 * return a value.
 */
abstract class FlowAnalyzer extends ASTGenericVisitor {

	static protected class SwitchData {
		private boolean fHasDefaultCase;
		private final List<IRegion> fRanges= new ArrayList<IRegion>(4);
		private final List<FlowInfo> fInfos= new ArrayList<FlowInfo>(4);

		public void setHasDefaultCase() {
			fHasDefaultCase= true;
		}

		public boolean hasDefaultCase() {
			return fHasDefaultCase;
		}

		public void add(IRegion range, FlowInfo info) {
			fRanges.add(range);
			fInfos.add(info);
		}

		public IRegion[] getRanges() {
			return fRanges.toArray(new IRegion[fRanges.size()]);
		}

		public FlowInfo[] getInfos() {
			return fInfos.toArray(new FlowInfo[fInfos.size()]);
		}

		public FlowInfo getInfo(int index) {
			return fInfos.get(index);
		}
	}

	private final HashMap<IASTNode, FlowInfo> fData = new HashMap<IASTNode, FlowInfo>(100);
	FlowContext fFlowContext;

	public FlowAnalyzer(FlowContext context) {
		super(true);
		fFlowContext= context;
	}

	protected abstract boolean createReturnFlowInfo(IASTReturnStatement node);

	protected abstract boolean traverseNode(IASTNode node);

	protected boolean skipNode(IASTNode node) {
		return !traverseNode(node);
	}

	@Override
	protected final int genericVisit(IASTNode node) {
		return traverseNode(node) ? PROCESS_CONTINUE : PROCESS_SKIP;
	}

	//---- Hooks to create Flow info objects. User may introduce their own infos.

	protected ReturnFlowInfo createReturn(IASTReturnStatement statement) {
		return new ReturnFlowInfo(statement);
	}

	protected ThrowFlowInfo createThrow() {
		return new ThrowFlowInfo();
	}

	protected BranchFlowInfo createBranch(IASTName label) {
		return new BranchFlowInfo(label, fFlowContext);
	}

	protected GenericSequentialFlowInfo createSequential() {
		return new GenericSequentialFlowInfo();
	}

	protected ConditionalFlowInfo createConditional() {
		return new ConditionalFlowInfo();
	}

	protected RangeBasedForFlowInfo createRangeBasedFor() {
		return new RangeBasedForFlowInfo();
	}

	protected ForFlowInfo createFor() {
		return new ForFlowInfo();
	}

	protected TryFlowInfo createTry() {
		return new TryFlowInfo();
	}

	protected WhileFlowInfo createWhile() {
		return new WhileFlowInfo();
	}

	protected IfFlowInfo createIf() {
		return new IfFlowInfo();
	}

	protected DoWhileFlowInfo createDoWhile() {
		return new DoWhileFlowInfo();
	}

	protected SwitchFlowInfo createSwitch() {
		return new SwitchFlowInfo();
	}

	protected BlockFlowInfo createBlock() {
		return new BlockFlowInfo();
	}

	protected FunctionCallFlowInfo createFunctionCallFlowInfo() {
		return new FunctionCallFlowInfo();
	}

	protected FlowContext getFlowContext() {
		return fFlowContext;
	}

	//---- Helpers to access flow analysis objects ----------------------------------------

	protected FlowInfo getFlowInfo(IASTNode node) {
		return fData.remove(node);
	}

	protected void setFlowInfo(IASTNode node, FlowInfo info) {
		fData.put(node, info);
	}

	protected FlowInfo assignFlowInfo(IASTNode target, IASTNode source) {
		FlowInfo result= getFlowInfo(source);
		setFlowInfo(target, result);
		return result;
	}

	protected FlowInfo accessFlowInfo(IASTNode node) {
		return fData.get(node);
	}

	//---- Helpers to process sequential flow infos -------------------------------------

	protected GenericSequentialFlowInfo processSequential(IASTNode parent, IASTNode[] nodes) {
		GenericSequentialFlowInfo result= createSequential(parent);
		process(result, nodes);
		return result;
	}

	protected GenericSequentialFlowInfo processSequential(IASTNode parent, Iterable<IASTNode> nodes) {
		GenericSequentialFlowInfo result= createSequential(parent);
		process(result, nodes);
		return result;
	}

	protected GenericSequentialFlowInfo processSequential(IASTNode parent, IASTNode node) {
		GenericSequentialFlowInfo result= createSequential(parent);
		if (node != null)
			result.merge(getFlowInfo(node), fFlowContext);
		return result;
	}

	protected GenericSequentialFlowInfo processSequential(IASTNode parent, IASTNode node1, IASTNode node2) {
		GenericSequentialFlowInfo result= createSequential(parent);
		if (node1 != null)
			result.merge(getFlowInfo(node1), fFlowContext);
		if (node2 != null)
			result.merge(getFlowInfo(node2), fFlowContext);
		return result;
	}

	protected GenericSequentialFlowInfo createSequential(IASTNode parent) {
		GenericSequentialFlowInfo result= createSequential();
		setFlowInfo(parent, result);
		return result;
	}

	protected GenericSequentialFlowInfo createSequential(IASTNode[] nodes) {
		GenericSequentialFlowInfo result= createSequential();
		process(result, nodes);
		return result;
	}

	//---- Generic merge methods --------------------------------------------------------

	protected void process(GenericSequentialFlowInfo info, IASTNode[] nodes) {
		if (nodes == null)
			return;
		for (IASTNode node : nodes) {
			info.merge(getFlowInfo(node), fFlowContext);
		}
	}

	protected void process(GenericSequentialFlowInfo info, Iterable<IASTNode> nodes) {
		if (nodes == null)
			return;
		for (IASTNode node : nodes) {
			info.merge(getFlowInfo(node), fFlowContext);
		}
	}

	protected void process(GenericSequentialFlowInfo info, IASTNode node) {
		if (node != null)
			info.merge(getFlowInfo(node), fFlowContext);
	}

	protected void process(GenericSequentialFlowInfo info, IASTNode node1, IASTNode node2) {
		if (node1 != null)
			info.merge(getFlowInfo(node1), fFlowContext);
		if (node2 != null)
			info.merge(getFlowInfo(node2), fFlowContext);
	}

	//---- special visit methods -------------------------------------------------------

	@Override
	public int visit(IASTStatement node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		if (node instanceof IASTBreakStatement) {
			return visit((IASTBreakStatement) node);
		} else if (node instanceof IASTCaseStatement) {
			return visit((IASTCaseStatement) node);
		} else if (node instanceof IASTCompoundStatement) {
			return visit((IASTCompoundStatement) node);
		} else if (node instanceof IASTContinueStatement) {
			return visit((IASTContinueStatement) node);
		} else if (node instanceof IASTDeclarationStatement) {
			return visit((IASTDeclarationStatement) node);
		} else if (node instanceof IASTDefaultStatement) {
			return visit((IASTDefaultStatement) node);
		} else if (node instanceof IASTDoStatement) {
			return visit((IASTDoStatement) node);
		} else if (node instanceof IASTExpressionStatement) {
			return visit((IASTExpressionStatement) node);
		} else if (node instanceof IASTForStatement) {
			return visit((IASTForStatement) node);
		} else if (node instanceof IASTGotoStatement) {
			return visit((IASTGotoStatement) node);
		} else if (node instanceof IASTIfStatement) {
			return visit((IASTIfStatement) node);
		} else if (node instanceof IASTLabelStatement) {
			return visit((IASTLabelStatement) node);
		} else if (node instanceof IASTNullStatement) {
			return visit((IASTNullStatement) node);
		} else if (node instanceof IASTReturnStatement) {
			return visit((IASTReturnStatement) node);
		} else if (node instanceof IASTSwitchStatement) {
			return visit((IASTSwitchStatement) node);
		} else if (node instanceof IASTWhileStatement) {
			return visit((IASTWhileStatement) node);
		} else if (node instanceof ICPPASTCatchHandler) {
			return visit((ICPPASTCatchHandler) node);
		} else if (node instanceof ICPPASTRangeBasedForStatement) {
			return visit((ICPPASTRangeBasedForStatement) node);
		} else if (node instanceof ICPPASTTryBlockStatement) {
			return visit((ICPPASTTryBlockStatement) node);
		}
		return PROCESS_CONTINUE;
	}

	public int visit(IASTBreakStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTCaseStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTDefaultStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTCompoundStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTContinueStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTDeclarationStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTDoStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTExpressionStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTForStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTGotoStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTIfStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTLabelStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTNullStatement node) {
		// Null statements aren't of any interest.
		return PROCESS_SKIP;
	}
	
	public int visit(ICPPASTRangeBasedForStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(ICPPASTTryBlockStatement node) {
		if (traverseNode(node)) {
			fFlowContext.pushExceptions(node);
			node.getTryBody().accept(this);
			fFlowContext.popExceptions();
			for (ICPPASTCatchHandler catchHandler : node.getCatchHandlers()) {
				catchHandler.accept(this);
			}
		}
		return PROCESS_SKIP;
	}
	
	public int visit(ICPPASTCatchHandler node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTReturnStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTSwitchStatement node) {
		return PROCESS_CONTINUE;
	}
	
	public int visit(IASTWhileStatement node) {
		return PROCESS_CONTINUE;
	}

	//---- Helper to process switch statement ----------------------------------------

	protected SwitchData createSwitchData(IASTSwitchStatement node) {
		SwitchData result= new SwitchData();
		IASTStatement[] statements;
		IASTStatement body = node.getBody();
		if (body instanceof IASTCompoundStatement) {
			statements = ((IASTCompoundStatement) body).getStatements();
		} else {
			statements = new IASTStatement[] { body };
		}
		if (statements.length == 0)
			return result;

		int start= -1;
		int end= -1;
		GenericSequentialFlowInfo info= null;

		for (IASTStatement statement : statements) {
			IASTFileLocation location = statement.getFileLocation();
			if (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement) {
				if (statement instanceof IASTDefaultStatement) {
					result.setHasDefaultCase();
				}
				if (info == null) {
					info= createSequential();
					start= location.getNodeOffset();
				} else {
					if (info.isReturn() || info.isPartialReturn() || info.branches()) {
						result.add(new Region(start, end - start + 1), info);
						info= createSequential();
						start= location.getNodeOffset();
					}
				}
			} else {
				if (info == null) {
					info= createSequential();
					start= location.getNodeOffset();
				} else {
					info.merge(getFlowInfo(statement), fFlowContext);
				}
			}
			end= location.getNodeOffset() + location.getNodeLength() - 1;
		}
		result.add(new Region(start, end - start + 1), info);
		return result;
	}

	//---- Concrete leave methods ---------------------------------------------------

	@Override
	public int leave(IASTStatement node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		if (node instanceof IASTBreakStatement) {
			return leave((IASTBreakStatement) node);
		} else if (node instanceof IASTCaseStatement) {
			return leave((IASTCaseStatement) node);
		} else if (node instanceof IASTCompoundStatement) {
			return leave((IASTCompoundStatement) node);
		} else if (node instanceof IASTContinueStatement) {
			return leave((IASTContinueStatement) node);
		} else if (node instanceof IASTDeclarationStatement) {
			return leave((IASTDeclarationStatement) node);
		} else if (node instanceof IASTDefaultStatement) {
			return leave((IASTDefaultStatement) node);
		} else if (node instanceof IASTDoStatement) {
			return leave((IASTDoStatement) node);
		} else if (node instanceof IASTExpressionStatement) {
			return leave((IASTExpressionStatement) node);
		} else if (node instanceof IASTForStatement) {
			return leave((IASTForStatement) node);
		} else if (node instanceof IASTGotoStatement) {
			return leave((IASTGotoStatement) node);
		} else if (node instanceof IASTIfStatement) {
			return leave((IASTIfStatement) node);
		} else if (node instanceof IASTLabelStatement) {
			return leave((IASTLabelStatement) node);
		} else if (node instanceof IASTNullStatement) {
			return leave((IASTNullStatement) node);
		} else if (node instanceof IASTReturnStatement) {
			return leave((IASTReturnStatement) node);
		} else if (node instanceof IASTSwitchStatement) {
			return leave((IASTSwitchStatement) node);
		} else if (node instanceof IASTWhileStatement) {
			return leave((IASTWhileStatement) node);
		} else if (node instanceof ICPPASTCatchHandler) {
			return leave((ICPPASTCatchHandler) node);
		} else if (node instanceof ICPPASTRangeBasedForStatement) {
			return leave((ICPPASTRangeBasedForStatement) node);
		} else if (node instanceof ICPPASTTryBlockStatement) {
			return leave((ICPPASTTryBlockStatement) node);
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTBreakStatement node) {
		setFlowInfo(node, createBranch(null));
		return PROCESS_SKIP;
	}

	public int leave(IASTCaseStatement node) {
		// Nothing to do
		return PROCESS_SKIP;
	}

	public int leave(IASTDefaultStatement node) {
		// Nothing to do
		return PROCESS_SKIP;
	}

	public int leave(IASTCompoundStatement node) {
		BlockFlowInfo info= createBlock();
		setFlowInfo(node, info);
		process(info, node.getStatements());
		return PROCESS_SKIP;
	}

	public int leave(IASTContinueStatement node) {
		setFlowInfo(node, createBranch(null));
		return PROCESS_SKIP;
	}

	public int leave(IASTDeclarationStatement node) {
		processSequential(node, node.getDeclaration());
		return PROCESS_SKIP;
	}

	public int leave(IASTDoStatement node) {
		DoWhileFlowInfo info= createDoWhile();
		setFlowInfo(node, info);
		info.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		info.mergeCondition(getFlowInfo(node.getCondition()), fFlowContext);
		info.removeLabel(null);
		return PROCESS_SKIP;
	}

	public int leave(IASTExpressionStatement node) {
		assignFlowInfo(node, node.getExpression());
		return PROCESS_SKIP;
	}

	public int leave(IASTForStatement node) {
		ForFlowInfo forInfo= createFor();
		setFlowInfo(node, forInfo);
		forInfo.mergeInitializer(createSequential(node.getInitializerStatement()), fFlowContext);
		forInfo.mergeCondition(getFlowInfo(node.getConditionExpression()), fFlowContext);
		forInfo.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		// Increments are executed after the body.
		forInfo.mergeIncrement(createSequential(node.getIterationExpression()), fFlowContext);
		forInfo.removeLabel(null);
		return PROCESS_SKIP;
	}

	public int leave(IASTGotoStatement node) {
		// TODO(sprigogin): Implement goto support
		return PROCESS_SKIP;
	}

	public int leave(IASTIfStatement node) {
		IfFlowInfo info= createIf();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getConditionExpression()), fFlowContext);
		info.merge(getFlowInfo(node.getThenClause()), getFlowInfo(node.getElseClause()), fFlowContext);
		return PROCESS_SKIP;
	}

	public int leave(IASTLabelStatement node) {
		FlowInfo info= assignFlowInfo(node, node.getNestedStatement());
		if (info != null)
			info.removeLabel(node.getName());
		return PROCESS_SKIP;
	}

	public int leave(IASTNullStatement node) {
		// Leaf node.
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTRangeBasedForStatement node) {
		RangeBasedForFlowInfo forInfo= createRangeBasedFor();
		setFlowInfo(node, forInfo);
		forInfo.mergeDeclaration(getFlowInfo(node.getDeclaration()), fFlowContext);
		forInfo.mergeInitializerClause(getFlowInfo(node.getInitializerClause()), fFlowContext);
		forInfo.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		forInfo.removeLabel(null);
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTTryBlockStatement node) {
		TryFlowInfo info= createTry();
		setFlowInfo(node, info);
		info.mergeTry(getFlowInfo(node.getTryBody()), fFlowContext);
		for (ICPPASTCatchHandler catchHandler : node.getCatchHandlers()) {
			info.mergeCatch(getFlowInfo(catchHandler), fFlowContext);
		}
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTCatchHandler node) {
		processSequential(node, node.getDeclaration(), node.getCatchBody());
		return PROCESS_SKIP;
	}

	public int leave(IASTReturnStatement node) {
		if (createReturnFlowInfo(node)) {
			ReturnFlowInfo info= createReturn(node);
			setFlowInfo(node, info);
			info.merge(getFlowInfo(node.getReturnArgument()), fFlowContext);
		} else {
			assignFlowInfo(node, node.getReturnArgument());
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTSwitchStatement node) {
		return leave(node, createSwitchData(node));
	}

	protected int leave(IASTSwitchStatement node, SwitchData data) {
		SwitchFlowInfo switchFlowInfo= createSwitch();
		setFlowInfo(node, switchFlowInfo);
		switchFlowInfo.mergeTest(getFlowInfo(node.getControllerExpression()), fFlowContext);
		FlowInfo[] cases= data.getInfos();
		for (int i= 0; i < cases.length; i++) {
			switchFlowInfo.mergeCase(cases[i], fFlowContext);
		}
		switchFlowInfo.mergeDefault(data.hasDefaultCase(), fFlowContext);
		switchFlowInfo.removeLabel(null);
		return PROCESS_SKIP;
	}

	public int leave(IASTWhileStatement node) {
		WhileFlowInfo info= createWhile();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getCondition()), fFlowContext);
		info.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		info.removeLabel(null);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTExpression node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		if (node instanceof IASTArraySubscriptExpression) {
			return leave((IASTArraySubscriptExpression) node);
		} else if (node instanceof IASTConditionalExpression) {
			return leave((IASTConditionalExpression) node);
		} else if (node instanceof IASTFunctionCallExpression) {
			return leave((IASTFunctionCallExpression) node);
		} else if (node instanceof IASTExpressionList) {
			return leave((IASTExpressionList) node);
		} else if (node instanceof IASTTypeIdExpression) {
			return leave((IASTTypeIdExpression) node);
		} else if (node instanceof IASTBinaryExpression) {
			return leave((IASTBinaryExpression) node);
		} else if (node instanceof IASTLiteralExpression) {
			return leave((IASTLiteralExpression) node);
		} else if (node instanceof IASTIdExpression) {
			return leave((IASTIdExpression) node);
		} else if (node instanceof IASTCastExpression) {
			return leave((IASTCastExpression) node);
		} else if (node instanceof IASTUnaryExpression) {
			return leave((IASTUnaryExpression) node);
		} else if (node instanceof IASTFieldReference) {
			return leave((IASTFieldReference) node);
		} else if (node instanceof IASTTypeIdInitializerExpression) {
			return leave((IASTTypeIdInitializerExpression) node);
		} else if (node instanceof ICPPASTNewExpression) {
			return leave((ICPPASTNewExpression) node);
		} else if (node instanceof ICPPASTDeleteExpression) {
			return leave((ICPPASTDeleteExpression) node);
		} else if (node instanceof ICPPASTSimpleTypeConstructorExpression) {
			return leave((ICPPASTSimpleTypeConstructorExpression) node);
		} else if (node instanceof IASTProblemExpression) {
			return leave(node);
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTArraySubscriptExpression node) {
		processSequential(node, node.getArrayExpression(), node.getArgument());
		return PROCESS_SKIP;
	}

	public int leave(IASTConditionalExpression node) {
		ConditionalFlowInfo info= createConditional();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getLogicalConditionExpression()), fFlowContext);
		info.merge(getFlowInfo(node.getPositiveResultExpression()),
				getFlowInfo(node.getNegativeResultExpression()), fFlowContext);
		return PROCESS_SKIP;
	}

	public int leave(IASTFunctionCallExpression node) {
		processFunctionCall(node, node.getFunctionNameExpression(), node.getArguments());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclaration node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		if (node instanceof IASTFunctionDefinition) {
			return leave((IASTFunctionDefinition) node);
		} else if (node instanceof IASTSimpleDeclaration) {
			return leave((IASTSimpleDeclaration) node);
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTFunctionDefinition node) {
		GenericSequentialFlowInfo info= processSequential(node, node.getDeclSpecifier());
		process(info, node.getDeclarator());
		process(info, node.getBody());
		if (node instanceof ICPPASTFunctionWithTryBlock) {
			process(info, ((ICPPASTFunctionWithTryBlock) node).getCatchHandlers());
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTSimpleDeclaration node) {
		GenericSequentialFlowInfo info= processSequential(node, node.getDeclSpecifier());
		process(info, node.getDeclarators());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTParameterDeclaration node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		GenericSequentialFlowInfo info= processSequential(node, node.getDeclSpecifier());
		process(info, node.getDeclarator());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclarator node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		IASTNode nestedOrName = node.getNestedDeclarator();
		if (nestedOrName == null)
			nestedOrName = node.getName();
		GenericSequentialFlowInfo info= processSequential(node, nestedOrName);
		if (node instanceof IASTArrayDeclarator)
			process(info, ((IASTArrayDeclarator) node).getArrayModifiers());
		IASTNode[] parameters = null;
		if (node instanceof IASTStandardFunctionDeclarator) {
			parameters = ((IASTStandardFunctionDeclarator) node).getParameters();
		} else if (node instanceof ICASTKnRFunctionDeclarator) {
			parameters = ((ICASTKnRFunctionDeclarator) node).getParameterDeclarations();
		}
		if (parameters != null)
			process(info, parameters);
		process(info, node.getInitializer());
		return PROCESS_SKIP;
	}

	public int leave(IASTExpressionList node) {
		IASTExpression[] expressions = node.getExpressions();
		processSequential(node, expressions);
		return PROCESS_SKIP;
	}

	public int leave(IASTTypeIdExpression node) {
		assignFlowInfo(node, node.getTypeId());
		return PROCESS_SKIP;
	}

	public int leave(IASTBinaryExpression node) {
		int operator = node.getOperator();
		switch (operator) {
		case IASTBinaryExpression.op_assign:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_moduloAssign:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_shiftLeftAssign:
		case IASTBinaryExpression.op_shiftRightAssign:
			FlowInfo lhs= getFlowInfo(node.getOperand1());
			FlowInfo rhs= getFlowInfo(node.getOperand2());
			if (lhs instanceof LocalFlowInfo) {
				LocalFlowInfo llhs= (LocalFlowInfo) lhs;
				llhs.setWriteAccess(fFlowContext);
				if (operator != IASTBinaryExpression.op_assign) {
					GenericSequentialFlowInfo tmp= createSequential();
					tmp.merge(new LocalFlowInfo(llhs, FlowInfo.READ, fFlowContext), fFlowContext);
					tmp.merge(rhs, fFlowContext);
					rhs= tmp;
				}
			}
			GenericSequentialFlowInfo info= createSequential(node);
			// First process right and side and then left hand side.
			info.merge(rhs, fFlowContext);
			info.merge(lhs, fFlowContext);
			break;

		default:
			IASTExpression[] operands = CPPVisitor.getOperandsOfMultiExpression(node);
			processSequential(node, operands);
			break;
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTLiteralExpression node) {
		// Leaf node.
		return PROCESS_SKIP;
	}
	
	public int leave(IASTIdExpression node) {
		assignFlowInfo(node, node.getName());
		return PROCESS_SKIP;
	}

	public int leave(IASTCastExpression node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		processSequential(node, node.getTypeId(), node.getOperand());
		return PROCESS_SKIP;
	}

	public int leave(IASTUnaryExpression node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		int operator = node.getOperator();

		switch (operator) {
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_postFixIncr:
		case IASTUnaryExpression.op_postFixDecr: {
			FlowInfo info= getFlowInfo(node.getOperand());
			if (info instanceof LocalFlowInfo) {
				// Normally this should be done in the parent node since the write access takes
				// place later. But there seems to be no case where this influences the flow
				// analysis. So it is kept here to simplify the code.
				GenericSequentialFlowInfo result= createSequential(node);
				result.merge(info, fFlowContext);
				result.merge(new LocalFlowInfo((LocalFlowInfo)info, FlowInfo.WRITE, fFlowContext),
						fFlowContext);
			} else {
				setFlowInfo(node, info);
			}
			break;
		}
		case IASTUnaryExpression.op_throw: {
			ThrowFlowInfo info= createThrow();
			setFlowInfo(node, info);
			IASTExpression expression= node.getOperand();
			info.merge(getFlowInfo(expression), fFlowContext);
			break;
		}
		case IASTUnaryExpression.op_alignOf:
		case IASTUnaryExpression.op_sizeof:
		case IASTUnaryExpression.op_sizeofParameterPack:
		case IASTUnaryExpression.op_typeid:
			break;

		default:
			assignFlowInfo(node, node.getOperand());
			break;
		}
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTName node) {
		if (skipNode(node) || node.isDeclaration() || node instanceof ICPPASTQualifiedName)
			return PROCESS_SKIP;
		IBinding binding= node.resolveBinding();
		if (binding instanceof IVariable) {
			IVariable variable= (IVariable) binding;
			if (!(variable instanceof IField)) {
				int accessMode = CPPVariableReadWriteFlags.getReadWriteFlags(node);
				setFlowInfo(node, new LocalFlowInfo(variable, accessMode, fFlowContext));
			}
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTFieldReference node) {
		processSequential(node, node.getFieldOwner(), node.getFieldName());
		return PROCESS_SKIP;
	}

	public int leave(IASTTypeIdInitializerExpression node) {
		processSequential(node, node.getInitializer());
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTNewExpression node) {
		GenericSequentialFlowInfo info= processSequential(node, node.getTypeId());
		process(info, node.getPlacementArguments());
		process(info, node.getInitializer());
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTDeleteExpression node) {
		assignFlowInfo(node, node.getOperand());
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTSimpleTypeConstructorExpression node) {
		processSequential(node, node.getDeclSpecifier(), node.getInitializer());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTInitializer node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		if (node instanceof IASTEqualsInitializer) {
			return leave((IASTEqualsInitializer) node);
		} else if (node instanceof IASTInitializerList) {
			return leave((IASTInitializerList) node);
		} else if (node instanceof ICASTDesignatedInitializer) {
			return leave((ICASTDesignatedInitializer) node);
		} else if (node instanceof IASTInitializerList) {
			return leave((ICPPASTConstructorChainInitializer) node);
		} else if (node instanceof IASTInitializerList) {
			return leave((ICPPASTConstructorInitializer) node);
		}
		return PROCESS_SKIP;
	}

	public int leave(IASTEqualsInitializer node) {
		assignFlowInfo(node, node.getInitializerClause());
		return PROCESS_SKIP;
	}

	public int leave(IASTInitializerList node) {
		processSequential(node, node.getClauses());
		return PROCESS_SKIP;
	}

	public int leave(ICASTDesignatedInitializer node) {
		processSequential(node, node.getDesignators());
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTConstructorChainInitializer node) {
		processSequential(node, node.getMemberInitializerId(), node.getInitializer());
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTConstructorInitializer node) {
		processSequential(node, node.getArguments());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTTranslationUnit node) {
		if (skipNode(node))
			return PROCESS_SKIP;
		processSequential(node, node.getDeclarations());
		return PROCESS_SKIP;
	}

	private void processFunctionCall(IASTFunctionCallExpression node,
			IASTExpression functionNameExpression, IASTInitializerClause[] arguments) {
		if (skipNode(node))
			return;
		FunctionCallFlowInfo info= createFunctionCallFlowInfo();
		setFlowInfo(node, info);
		for (IASTInitializerClause arg : arguments) {
			info.mergeArgument(getFlowInfo(arg), fFlowContext);
		}
		info.mergeReceiver(getFlowInfo(functionNameExpression), fFlowContext);
	}
}
