/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

/**
 * A word rule matching assembly labels.
 *
 * @since 5.0
 */
final class AsmLabelRule implements IRule {

	/** The word detector used by this rule. */
	protected IWordDetector fDetector;
	/** The token to be returned when a label has been matched. */
	protected IToken fLabelToken;
	/** The default token to be returned when no label could be matched. */
	protected IToken fDefaultToken;

	/**
	 * @param detector
	 * @param defaultToken
	 */
	AsmLabelRule(IWordDetector detector, IToken labelToken, IToken defaultToken) {
		Assert.isNotNull(detector);
		Assert.isNotNull(labelToken);
		Assert.isNotNull(defaultToken);

		fDetector= detector;
		fLabelToken= labelToken;
		fDefaultToken= defaultToken;
	}

	/*
	 * @see IRule#evaluate
	 */
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c= scanner.read();
		if (fDetector.isWordStart((char) c)) {
			int count= 1;
			do {
				c= scanner.read();
				++count;
			} while (fDetector.isWordPart((char) c));
			if(c == ':') {
				return fLabelToken;
			}
			if (fDefaultToken.isUndefined()) {
				while (count-- > 0) {
					scanner.unread();
				}
			}
			return fDefaultToken;
		}
		
		scanner.unread();
		return Token.UNDEFINED;
	}

}