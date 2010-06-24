/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;

public class NameResolutionChecker extends AbstractIndexAstChecker {

	static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.NameResolutionChecker"; //$NON-NLS-1$
	
	public void processAst(IASTTranslationUnit ast) {
		try {
			ast.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					IBinding binding = name.resolveBinding();
					if (binding instanceof IProblemBinding) {
						reportProblem(ERR_ID, name, name.getRawSignature());
					}
					return PROCESS_CONTINUE;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean runInEditor() {
		return true;
	}

}
