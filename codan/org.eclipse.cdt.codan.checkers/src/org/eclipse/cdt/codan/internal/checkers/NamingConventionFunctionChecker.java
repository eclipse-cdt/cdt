/*******************************************************************************
 * Copyright (c) 2009, 2013 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

/**
 * This is style checker for function name code style. Pattern parameter is
 * regular expression defining the style.
 */
public class NamingConventionFunctionChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.NamingConventionFunctionChecker"; //$NON-NLS-1$
	public static final String PARAM_KEY = "pattern"; //$NON-NLS-1$
	public static final String PARAM_METHODS = "macro"; //$NON-NLS-1$
	public static final String PARAM_EXCEPT_ARG_LIST = "exceptions"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		final List<IProblem> pts = getProblemsByMainId(ER_ID, getFile());
		try {
			ast.accept(new ASTVisitor() {
				{
					shouldVisitDeclarations = true;
				}

				@Override
				public int visit(IASTDeclaration element) {
					if (element instanceof IASTFunctionDefinition) {
						IASTName astName = ((IASTFunctionDefinition) element).getDeclarator().getName();
						String name = getSearchableName(astName);
						if (name != null) {
							for (Iterator<IProblem> iterator = pts.iterator(); iterator.hasNext();) {
								IProblem pt = iterator.next();
								if (!shouldReport(astName, pt))
									return PROCESS_SKIP;
								String parameter = (String) getPreference(pt, PARAM_KEY);
								Pattern pattern = Pattern.compile(parameter);
								if (!pattern.matcher(name).find() && !isFilteredArg(name, pt)) {
									reportProblem(pt, astName, name, parameter);
								}
							}

						}
					}
					return PROCESS_SKIP;
				}

				private boolean shouldReport(IASTName astName, IProblem pt) {
					if (astName instanceof ICPPASTQualifiedName) {
						return shouldReportCppMethods(pt);
					}
					return true;
				}

				private String getSearchableName(IASTName astName) {
					String name = astName.toString();
					if (astName instanceof ICPPASTQualifiedName) {
						ICPPASTQualifiedName cppAstName = (ICPPASTQualifiedName) astName;
						if (cppAstName.isConversionOrOperator())
							return null;
						name = cppAstName.getLastName().toString();
						if (name.startsWith("~")) // destructor //$NON-NLS-1$
							return null;
						ICPPASTNameSpecifier[] qualifier = cppAstName.getQualifier();
						if (qualifier.length > 0) {
							if (cppAstName.getLastName().toString()
									.equals(qualifier[qualifier.length - 1].toString())) {
								// constructor
								return null;
							}
						}
					}
					return name;
				}
			});
		} catch (Exception e) {
			CodanCheckersActivator.log(e);
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_KEY, CheckersMessages.NamingConventionFunctionChecker_LabelNamePattern, "^[a-z]"); //$NON-NLS-1$
		addPreference(problem, PARAM_METHODS, CheckersMessages.NamingConventionFunctionChecker_ParameterMethods,
				Boolean.TRUE);
		addListPreference(problem, PARAM_EXCEPT_ARG_LIST, CheckersMessages.GenericParameter_ParameterExceptions,
				CheckersMessages.GenericParameter_ParameterExceptionsItem);
	}

	public boolean shouldReportCppMethods(IProblem pt) {
		return (Boolean) getPreference(pt, PARAM_METHODS);
	}

	public boolean isFilteredArg(String arg, IProblem pt) {
		return isFilteredArg(arg, pt, PARAM_EXCEPT_ARG_LIST);
	}

	@Override
	public boolean runInEditor() {
		return true;
	}
}
