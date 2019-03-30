/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.ArrayDeque;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;

public class BlacklistChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.BlacklistProblem"; //$NON-NLS-1$
	public static final String PARAM_BLACKLIST = "blacklist"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) binding;
					ArrayDeque<String> textStack = new ArrayDeque<>();
					textStack.push(method.getName());
					ICPPClassType classType = method.getClassOwner();
					if (classType != null) {
						textStack.push("::"); //$NON-NLS-1$
						textStack.push(classType.getName());
						IBinding classOwner = classType.getOwner();
						while (classOwner != null && classOwner instanceof ICPPNamespace) {
							textStack.push("::"); //$NON-NLS-1$
							textStack.push(classOwner.getName());
							classOwner = classOwner.getOwner();
						}
					}
					StringBuilder builder = new StringBuilder();
					while (!textStack.isEmpty()) {
						builder.append(textStack.pop());
					}
					if (isInBlackList(builder.toString()))
						reportProblem(ERR_ID, name);
				} else if (binding instanceof IFunction) {
					if (isInBlackList(binding.getName()))
						reportProblem(ERR_ID, name);
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private boolean isInBlackList(String arg) {
		Object[] arr = (Object[]) getPreference(getProblemById(ERR_ID, getFile()), PARAM_BLACKLIST);
		for (int i = 0; i < arr.length; i++) {
			String str = (String) arr[i];
			if (arg.equals(str))
				return true;
		}
		return false;
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addListPreference(problem, PARAM_BLACKLIST, CheckersMessages.BlacklistChecker_list,
				CheckersMessages.BlacklistChecker_list_item);
	}

	public boolean skipConstructorsWithFCalls() {
		return (Boolean) getPreference(getProblemById(ERR_ID, getFile()), PARAM_BLACKLIST);
	}
}
