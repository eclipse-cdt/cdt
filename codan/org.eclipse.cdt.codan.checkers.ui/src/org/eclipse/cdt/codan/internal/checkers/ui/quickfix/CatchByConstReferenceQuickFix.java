/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - original API and implementation in CatchByReferenceQuickFix
 *    Tomasz Wesolowski - modified for const &
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.util.Optional;

import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

/**
 * Quick fix for catch by value
 */
public class CatchByConstReferenceQuickFix extends CatchByReferenceQuickFix {
	@Override
	public String getLabel() {
		return Messages.CatchByConstReferenceQuickFix_Message;
	}

	protected Optional<IASTDeclSpecifier> getNewDeclSpecifier(IASTSimpleDeclaration declaration) {
		IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
		IASTDeclSpecifier replacement = declSpecifier.copy(CopyStyle.withLocations);
		replacement.setConst(true);
		return Optional.of(replacement);
	}	
}
