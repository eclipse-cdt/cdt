/*******************************************************************************
 * Copyright (c) 2005-2015 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Justin You
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.c.CExternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitFunction;

public class ExternalBindingChecker extends AbstractIndexAstChecker {
	public final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.ExternalBindingProblem"; //$NON-NLS-1$

	@Override
	public boolean runInEditor() {
		return true;
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
				shouldVisitImplicitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				try {
					IBinding binding = name.resolveBinding();
					//Function declarations in C programming language are NOT mandatory for legacy and backwards compatibility reasons.
					//But Function declarations in C++ programming language are mandatory.
					//
					//In CDT parser, The interface ICExternalBinding represents a binding for a function or variable that is 
					//assumed to exist in another compilation unit and that would be found at link time.
					//Class CExternalFunction implement the interface ICExternalBinding and models functions used without declarations.
					//Class CImplicitFunction is subclass of CExternalFunction and models built-in functions and intrinsic functions.
					//
					//this checker check all CExternalFunction except CImplicitFunction
					if (binding instanceof ICExternalBinding)
						if ( !(binding instanceof CImplicitFunction) && (binding instanceof CExternalFunction)) {
							reportProblem(ERR_ID, name.getLastName(), name.getRawSignature());
							return PROCESS_CONTINUE;
						}
				} catch (Exception e) {
					CodanCheckersActivator.log(e);
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
