/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;

public class MacroReferenceRule extends PatternRule {

	int nOfBrackets;
	int fBracket;

//	public MacroReferenceRule(IToken token) {
//		super("$(", ")", token, (char) 0, true); //$NON-NLS-1$ //$NON-NLS-2$
//	}

	public MacroReferenceRule(IToken token, String startSeq, String endSeq) {
		super(startSeq, endSeq, token, (char)0, true);
		if (endSeq.length() > 0 && endSeq.charAt(0) == '}') {
			fBracket = '{';
		} else {
			fBracket = '(';
		}
	}
	
	protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) {
		nOfBrackets = 1;
		return super.doEvaluate(scanner, resume);
	}

	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		int c;
		char[][] delimiters = scanner.getLegalLineDelimiters();
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			if (fBracket == c) {
				++nOfBrackets;
			}
			if (fEndSequence.length > 0 && c == fEndSequence[0]) {
				// Check if the specified end sequence has been found.
				if (sequenceDetected(scanner, fEndSequence, true)) {
					if (0 == --nOfBrackets) {
						return true;
					}
				}
			} else if (fBreaksOnEOL) {
				// Check for end of line since it can be used to terminate the pattern.
				for (int i = 0; i < delimiters.length; i++) {
					if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i], false)) {
						return true;
					}
				}
			}
		}
		scanner.unread();
		return true;
	}

}
