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

	@Override
	public void processAst(IASTTranslationUnit ast) {
		Object[] list = (Object[]) getPreference(getProblemById(ERR_ID, getFile()), PARAM_BLACKLIST);
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
				String completeName = getBindingQualifiedName(binding);
				if (completeName != null && Arrays.binarySearch(list, completeName) >= 0)
					reportProblem(ERR_ID, name, completeName);
				return PROCESS_CONTINUE;
			}
		});
	}

	private String getBindingQualifiedName(IBinding binding) {
		if (binding instanceof ICPPFunction) {
			ICPPFunction method = (ICPPFunction) binding;
			try {
				return String.join("::", method.getQualifiedName()); //$NON-NLS-1$
			} catch (DOMException e) {
				CodanCheckersActivator.log(e);
			}
		} else if (binding instanceof IFunction) {
			return binding.getName();
		}
		return null;
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addListPreference(problem, PARAM_BLACKLIST, CheckersMessages.BlacklistChecker_list,
				CheckersMessages.BlacklistChecker_list_item);
	}
}
