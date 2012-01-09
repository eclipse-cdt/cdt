/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;
import org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

/**
 * The checker suppose to find issue related to mismatched return value/function
 * declaration<br>
 * <li>Function declared as returning non-void returns void
 * <li>Function declared as returning void has non-void return
 * <li>Function declared as returning non-void has no return (requires control
 * flow graph)
 */
public class ReturnChecker extends AbstractAstFunctionChecker {
	public static final String PARAM_IMPLICIT = "implicit"; //$NON-NLS-1$
	public static final String RET_NO_VALUE_ID = "org.eclipse.cdt.codan.checkers.noreturn"; //$NON-NLS-1$
	public static final String RET_ERR_VALUE_ID = "org.eclipse.cdt.codan.checkers.errreturnvalue"; //$NON-NLS-1$
	public static final String RET_NORET_ID = "org.eclipse.cdt.codan.checkers.errnoreturn"; //$NON-NLS-1$

	class ReturnStmpVisitor extends ASTVisitor {
		private final IASTFunctionDefinition func;
		boolean hasret;

		ReturnStmpVisitor(IASTFunctionDefinition func) {
			shouldVisitStatements = true;
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			this.func = func;
			this.hasret = false;
		}

		@Override
		public int visit(IASTDeclaration element) {
			if (element != func)
				return PROCESS_SKIP; // skip inner functions
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expr) {
			if (expr instanceof ICPPASTLambdaExpression) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTReturnStatement) {
				IASTReturnStatement ret = (IASTReturnStatement) stmt;
				boolean hasValue = ret.getReturnArgument() != null;
				if (hasret == false && hasValue) {
					hasret = true;
				}
				if (!isVoid(func) && !isConstructorDestructor(func)) {
					if (checkImplicitReturn(RET_NO_VALUE_ID) || isExplicitReturn(func)) {
						if (!hasValue)
							reportProblem(RET_NO_VALUE_ID, ret);
					}
				} else {
					if (hasValue) {
						IType type = ret.getReturnValue().getExpressionType();
						if (isVoid(type))
							return PROCESS_SKIP;
						reportProblem(RET_ERR_VALUE_ID, ret.getReturnValue());
					}
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * @param func
	 * @return
	 * 
	 */
	public boolean isConstructorDestructor(IASTFunctionDefinition func) {
		if (func instanceof ICPPASTFunctionDefinition) {
			IBinding method = func.getDeclarator().getName().resolveBinding();
			if (method instanceof ICPPConstructor || method instanceof ICPPMethod && ((ICPPMethod) method).isDestructor()) {
				return true;
			}
		}
		return false;
	}

	public boolean isMain(IASTFunctionDefinition func) {
		try {
			String functionName = func.getDeclarator().getName().getRawSignature();
			if (functionName.equals("main")) { //$NON-NLS-1$
				return true;
			}
		} catch (Exception e) {
			// well, not main
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker#
	 * processFunction(org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition)
	 */
	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		if (func.getParent() instanceof ICPPASTTemplateDeclaration)
			return; // if it is template get out of here
		ReturnStmpVisitor visitor = new ReturnStmpVisitor(func);
		func.accept(visitor);
		boolean nonVoid = !isVoid(func);
		if (nonVoid && !isMain(func)) {
			// there a return but maybe it is only on one branch
			IASTStatement body = func.getBody();
			if (body instanceof IASTCompoundStatement) {
				IASTStatement[] statements = ((IASTCompoundStatement) body).getStatements();
				if (statements.length > 0) {
					IASTStatement last = statements[statements.length - 1];
					// get nested statement if this is a label
					while (last instanceof IASTLabelStatement) {
						last = ((IASTLabelStatement) last).getNestedStatement();
					}
					// now check if last statement if complex (for optimization reasons, building CFG is expensive)
					if (isCompoundStatement(last)) {
						if (endsWithNoExitNode(func))
							reportNoRet(func, visitor.hasret);
					} else if (!isFuncExitStatement(last)) {
						reportNoRet(func, visitor.hasret);
					}
				} else {
					reportNoRet(func, false);
				}
			}
		}
	}

	/**
	 * @param func
	 */
	protected void reportNoRet(IASTFunctionDefinition func, boolean hasRet) {
		if (!hasRet) {
			// no return at all
			if (checkImplicitReturn(RET_NORET_ID) == false && isExplicitReturn(func) == false) {
				return;
			}
			if (isConstructorDestructor(func)) {
				return;
			}
		}
		reportProblem(RET_NORET_ID, func.getDeclSpecifier());
	}

	/**
	 * @param last
	 * @return
	 */
	private boolean isCompoundStatement(IASTStatement last) {
		return last instanceof IASTIfStatement || last instanceof IASTWhileStatement ||
				last instanceof IASTDoStatement	|| last instanceof IASTForStatement ||
				last instanceof IASTSwitchStatement || last instanceof IASTCompoundStatement;
	}

	protected boolean isFuncExitStatement(IASTStatement statement) {
		return statement instanceof IASTReturnStatement || CxxAstUtils.isThrowStatement(statement) ||
				CxxAstUtils.isExitStatement(statement);
	}

	/**
	 * @param if - problem id
	 * @return true if need to check inside functions with implicit return
	 */
	protected boolean checkImplicitReturn(String id) {
		final IProblem pt = getProblemById(id, getFile());
		return (Boolean) getPreference(pt, PARAM_IMPLICIT);
	}

	/**
	 * @param func
	 * @return
	 */
	protected boolean endsWithNoExitNode(IASTFunctionDefinition func) {
		IControlFlowGraph graph = getModelCache().getControlFlowGraph(func);
		Iterator<IExitNode> exitNodeIterator = graph.getExitNodeIterator();
		boolean noexitop = false;
		for (; exitNodeIterator.hasNext();) {
			IExitNode node = exitNodeIterator.next();
			if (((ICfgData) node).getData() == null) {
				// if it real exit node such as return, exit or throw data
				// will be an ast node, if it is null it is a fake node added by the
				// graph builder
				noexitop = true;
				break;
			}
		}
		return noexitop;
	}

	/**
	 * @param func
	 * @return
	 */
	protected boolean isExplicitReturn(IASTFunctionDefinition func) {
		return getDeclSpecType(func) != IASTSimpleDeclSpecifier.t_unspecified;
	}

	/**
	 * @param func
	 * @return
	 */
	public boolean isVoid(IASTFunctionDefinition func) {
		int type = getDeclSpecType(func);
		if (type == IASTSimpleDeclSpecifier.t_void) {
			IASTFunctionDeclarator declarator = func.getDeclarator();
			if (declarator.getPointerOperators().length == 0)
				return true;
		} else if (type == IASTSimpleDeclSpecifier.t_auto) {
			if (isAutoVoid(func)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check if type if void
	 * (uses deprecated API for compatibility with 6.0)
	 * 
	 * @param type
	 * @throws DOMException
	 */
	@SuppressWarnings("deprecation")
	public boolean isVoid(IType type) {
		if (type instanceof IBasicType) {
			try {
				if (((IBasicType) type).getType() == IBasicType.t_void)
					return true;
			} catch (DOMException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * @param func
	 * @return
	 */
	protected int getDeclSpecType(IASTFunctionDefinition func) {
		IASTDeclSpecifier declSpecifier = func.getDeclSpecifier();
		int type = -1;
		if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			type = ((IASTSimpleDeclSpecifier) declSpecifier).getType();
		} else if (declSpecifier instanceof IASTNamedTypeSpecifier) {
			IBinding binding = ((IASTNamedTypeSpecifier) declSpecifier).getName().resolveBinding();
			IType utype = CxxAstUtils.unwindTypedef((IType) binding);
			if (isVoid(utype))
				return IASTSimpleDeclSpecifier.t_void;
		}
		return type;
	}

	/* checker must implement @link ICheckerWithPreferences */
	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		if (problem.getId().equals(RET_NO_VALUE_ID) || problem.getId().equals(RET_NORET_ID)) {
			addPreference(problem, PARAM_IMPLICIT, CheckersMessages.ReturnChecker_Param0, Boolean.FALSE);
		}
	}

	/**
	 * Checks if a "void" return type is specified using the C++0x
	 * late-specified return type
	 * for a given function definition
	 * 
	 * For example, auto f() -> void { } would return true
	 * 
	 * @param functionDefinition
	 * @return True if the function has a void (late-specified) return type,
	 *         False otherwise
	 */
	private boolean isAutoVoid(IASTFunctionDefinition functionDefinition) {
		IASTFunctionDeclarator declarator = functionDefinition.getDeclarator();
		if (declarator instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;
			IASTTypeId trailingReturnType = functionDeclarator.getTrailingReturnType();
			if (trailingReturnType != null) {
				IASTDeclarator abstractDeclarator = trailingReturnType.getAbstractDeclarator();
				if (abstractDeclarator != null) {
					if (abstractDeclarator.getPointerOperators().length > 0) {
						return false;
					}
					IASTDeclSpecifier declSpecifier = trailingReturnType.getDeclSpecifier();
					if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier) {
						ICPPASTSimpleDeclSpecifier simpleDeclSpecifier = (ICPPASTSimpleDeclSpecifier) declSpecifier;
						if (simpleDeclSpecifier.getType() == IASTSimpleDeclSpecifier.t_void) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
