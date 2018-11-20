/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	protected Optional<IASTDeclSpecifier> getNewDeclSpecifier(IASTSimpleDeclaration declaration) {
		IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
		IASTDeclSpecifier replacement = declSpecifier.copy(CopyStyle.withLocations);
		replacement.setConst(true);
		return Optional.of(replacement);
	}
}
