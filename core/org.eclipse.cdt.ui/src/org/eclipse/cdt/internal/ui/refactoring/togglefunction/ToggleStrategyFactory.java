/*******************************************************************************
 * Copyright (c) 2011, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

public class ToggleStrategyFactory {
	private ToggleRefactoringContext context;

	public ToggleStrategyFactory(ToggleRefactoringContext context) {
		this.context = context;
	}

	public IToggleRefactoringStrategy getAppropriateStategy() {
		if (context.getDefinition() == null)
			throw new NotSupportedException(Messages.ToggleStrategyFactory_NoDefinitionFound);
		if (!context.getDefinitionAST().isHeaderUnit())
			return new ToggleFromImplementationToHeaderOrClassStrategy(context);
		if (isInClassSituation())
			return new ToggleFromClassToInHeaderStrategy(context);
		if (isTemplateSituation())
			return new ToggleFromInHeaderToClassStrategy(context);
		if (isinHeaderSituation())
			return new ToggleFromInHeaderToImplementationStrategy(context);
		throw new NotSupportedException(Messages.ToggleStrategyFactory_UnsupportedSituation);
	}

	private boolean isinHeaderSituation() {
		return (context.getDefinition() != null) && (context.getDefinitionAST().isHeaderUnit());
	}

	private boolean isInClassSituation() {
		return (context.getDeclaration() == null)
				&& (ASTQueries.findAncestorWithType(context.getDefinition(), IASTCompositeTypeSpecifier.class) != null);
	}

	private boolean isTemplateSituation() {
		return (ASTQueries.findAncestorWithType(context.getDefinition(), ICPPASTTemplateDeclaration.class) != null);
	}
}
