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
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;

/**
 * Catching by reference is recommended by C++ experts, for example Herb
 * Sutter/Andrei Alexandresscu "C++ Coding Standards", Rule 73
 * "Throw by value, catch by reference".
 * For one thing, this avoids copying and potentially slicing the exception.
 * 
 */
public class CatchByReference extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.CatchByReference"; //$NON-NLS-1$
	public static final String PARAM_EXCEPT_ARG_LIST = "exceptions"; //$NON-NLS-1$
	public static final String PARAM_UNKNOWN_TYPE = "unknown"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new OnCatch());
	}

	class OnCatch extends ASTVisitor {
		OnCatch() {
			shouldVisitStatements = true;
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof ICPPASTTryBlockStatement) {
				try {
					ICPPASTTryBlockStatement tblock = (ICPPASTTryBlockStatement) stmt;
					ICPPASTCatchHandler[] catchHandlers = tblock
							.getCatchHandlers();
					for (int i = 0; i < catchHandlers.length; i++) {
						ICPPASTCatchHandler catchHandler = catchHandlers[i];
						IASTDeclaration decl = catchHandler.getDeclaration();
						if (decl instanceof IASTSimpleDeclaration) {
							IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
							IASTDeclSpecifier spec = sdecl.getDeclSpecifier();
							if (!usesReference(catchHandler)) {
								if (spec instanceof IASTNamedTypeSpecifier) {
									IASTName tname = ((IASTNamedTypeSpecifier) spec)
											.getName();
									IType typeName = (IType) tname
											.resolveBinding();
									typeName = CxxAstUtils.getInstance()
											.unwindTypedef(typeName);
									if (typeName instanceof IBasicType
											|| typeName instanceof IPointerType
											|| typeName == null)
										continue;
									if (typeName instanceof IProblemBinding && !shouldReportForUnknownType())
										continue;
									String arg = spec.getRawSignature();
									if (!isFilteredArg(arg)) {
										reportProblem(ER_ID, decl, arg);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					CodanCheckersActivator.log(e);
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * If it uses reference or ponter
		 * 
		 * @param catchHandler
		 * @return
		 */
		private boolean usesReference(ICPPASTCatchHandler catchHandler) {
			IASTDeclaration declaration = catchHandler.getDeclaration();
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration)
						.getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator d = declarators[i];
					IASTPointerOperator[] pointerOperators = d
							.getPointerOperators();
					for (int j = 0; j < pointerOperators.length; j++) {
						IASTPointerOperator po = pointerOperators[j];
						if (po instanceof ICPPASTReferenceOperator) {
							return true;
						}
						if (po instanceof IASTPointer) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_UNKNOWN_TYPE,
				CheckersMessages.CatchByReference_ReportForUnknownType, Boolean.FALSE);
		addListPreference(problem, PARAM_EXCEPT_ARG_LIST,
				CheckersMessages.GenericParameter_ParameterExceptions,
				CheckersMessages.GenericParameter_ParameterExceptionsItem);
	}

	public boolean isFilteredArg(String arg) {
		Object[] arr = (Object[]) getPreference(
				getProblemById(ER_ID, getFile()), PARAM_EXCEPT_ARG_LIST);
		for (int i = 0; i < arr.length; i++) {
			String str = (String) arr[i];
			if (arg.equals(str))
				return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	public boolean shouldReportForUnknownType() {
		return (Boolean) getPreference(getProblemById(ER_ID, getFile()),
				PARAM_UNKNOWN_TYPE);
	}
}
