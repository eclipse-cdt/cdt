/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.codan;

import java.util.Collection;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.qt.core.ASTUtil;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.QtFunctionCall;
import org.eclipse.cdt.internal.qt.core.QtMethodReference;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;

/**
 * A Codan checker for QObject::connect and QObject::disconnect function calls.  The checker
 * confirms that SIGNAL and SLOT macro expansions reference a valid Qt signal or slot.
 */
public class QtSyntaxChecker extends AbstractIndexAstChecker {
	private final Checker checker = new Checker();

	@Override
	public boolean runInEditor() {
		return true;
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		// don't run on full or incremental builds
		getTopLevelPreference(problem);
		getLaunchModePreference(problem).enableInLaunchModes(CheckerLaunchMode.RUN_ON_FILE_OPEN,
				CheckerLaunchMode.RUN_AS_YOU_TYPE, CheckerLaunchMode.RUN_ON_DEMAND);
	}

	@Override
	public synchronized boolean processResource(IResource resource) throws OperationCanceledException {
		if (QtNature.hasNature(resource.getProject()))
			return super.processResource(resource);
		return false;
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		// Run the checker only on Qt-enabled projects.
		if (QtNature.hasNature(ASTUtil.getProject(ast)))
			ast.accept(checker);
	}

	private class Checker extends ASTVisitor {
		public Checker() {
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expr) {
			if (!(expr instanceof IASTFunctionCallExpression))
				return PROCESS_CONTINUE;
			IASTFunctionCallExpression fncall = (IASTFunctionCallExpression) expr;

			Collection<QtMethodReference> refs = QtFunctionCall.getReferences(fncall);
			if (refs != null)
				for (QtMethodReference ref : refs) {
					IQMethod method = ref.getMethod();
					if (method != null)
						continue;

					// Either the macro expansion didn't have an argument, or the argument was not a valid method.
					if (ref.getRawSignature().isEmpty())
						report(ref, Messages.QtConnect_macro_without_method_1, ref.getType().macroName);
					else
						report(ref, Messages.QtConnect_macro_method_not_found_3, ref.getType().paramName,
								ref.getContainingType().getName(), ref.getRawSignature());
				}

			return PROCESS_CONTINUE;
		}

		private void report(IASTNode node, String message, Object... args) {
			if (args.length <= 0)
				reportProblem(Activator.QT_SYNTAX_ERR_ID, node, message);
			else
				reportProblem(Activator.QT_SYNTAX_ERR_ID, node, NLS.bind(message, args));
		}
	}
}
