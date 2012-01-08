/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

/**
 * This is style checker for function name code style. Pattern parameter is
 * regular expression defining the style.
 * 
 */
public class NamingConventionFunctionChecker extends AbstractIndexAstChecker implements ICheckerWithPreferences {
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
						String name = getSearchibleName(astName);
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

				public boolean shouldReport(IASTName astName, IProblem pt) {
					if (astName instanceof ICPPASTQualifiedName) {
						return shouldReportCppMethods(pt);
					}
					return true;
				}

				/**
				 * @param astName
				 * @return
				 */
				public String getSearchibleName(IASTName astName) {
					String name = astName.toString();
					if (astName instanceof ICPPASTQualifiedName) {
						ICPPASTQualifiedName cppAstName = (ICPPASTQualifiedName) astName;
						if (cppAstName.isConversionOrOperator())
							return null;
						name = cppAstName.getLastName().toString();
						if (name.startsWith("~")) // destructor //$NON-NLS-1$
							return null;
						IASTName[] names = cppAstName.getNames();
						if (names.length >= 2) {
							if (names[names.length - 1].toString().equals(names[names.length - 2].toString())) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.ICheckerWithPreferences#initParameters
	 * (org.eclipse.cdt.codan.core.model.IProblemWorkingCopy)
	 */
	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_KEY, CheckersMessages.NamingConventionFunctionChecker_LabelNamePattern, "^[a-z]"); //$NON-NLS-1$
		addPreference(problem, PARAM_METHODS, CheckersMessages.NamingConventionFunctionChecker_ParameterMethods, Boolean.TRUE);
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
