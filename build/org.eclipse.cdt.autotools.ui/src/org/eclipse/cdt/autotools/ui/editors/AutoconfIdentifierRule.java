/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AutoconfIdentifierRule implements IPredicateRule {

	private IToken fToken;
	private String fExtraChars = "_${@"; //$NON-NLS-1$
	
	public AutoconfIdentifierRule(IToken token) {
		Assert.isNotNull(token);
		fToken = token;
	}

	@Override
	public IToken getSuccessToken() {
		return fToken;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		int c = scanner.read();
		if (Character.isLetterOrDigit((char)c) || fExtraChars.indexOf((char)c) >= 0) {
			do {
				c = scanner.read();
			} while (Character.isLetterOrDigit((char)c) || fExtraChars.indexOf((char)c) >= 0);
			scanner.unread();
			return fToken;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}

}
