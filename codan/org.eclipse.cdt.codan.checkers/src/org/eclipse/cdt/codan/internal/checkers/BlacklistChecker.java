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

import java.util.Arrays;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

public class BlacklistChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.BlacklistProblem"; //$NON-NLS-1$
	public static final String PARAM_BLACKLIST = "blacklist"; //$NON-NLS-1$

	private Object[] list = null;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		list = (Object[]) getPreference(getProblemById(ERR_ID, getFile()), PARAM_BLACKLIST);
		if (list == null || list.length == 0)
			return;
		Arrays.sort(list);
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof ICPPFunction) {
					ICPPFunction method = (ICPPFunction) binding;
					try {
						if (isInBlackList(String.join("::", method.getQualifiedName()))) //$NON-NLS-1$
							reportProblem(ERR_ID, name);
					} catch (DOMException e) {
						CodanCheckersActivator.log(e);
					}
				} else if (binding instanceof IFunction) {
					if (isInBlackList(binding.getName()))
						reportProblem(ERR_ID, name);
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private boolean isInBlackList(String arg) {
		if (Arrays.binarySearch(list, arg) >= 0)
			return true;
		return false;
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addListPreference(problem, PARAM_BLACKLIST, CheckersMessages.BlacklistChecker_list,
				CheckersMessages.BlacklistChecker_list_item);
	}
}
