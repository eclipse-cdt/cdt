/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AutoconfSubstRule implements IPredicateRule {

	private IToken token;
	private char[][] fLineDelimiters;
	private char[][] fSortedLineDelimiters;

	private static class DecreasingCharArrayLengthComparator implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			return ((char[]) o2).length - ((char[]) o1).length;
		}
	}

	private Comparator<Object> fLineDelimiterComparator = new DecreasingCharArrayLengthComparator();

	public AutoconfSubstRule(IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		char[][] originalDelimiters = scanner.getLegalLineDelimiters();
		int count = originalDelimiters.length;
		if (fLineDelimiters == null || originalDelimiters.length != count) {
			fSortedLineDelimiters = new char[count][];
		} else {
			while (count > 0 && fLineDelimiters[count - 1] == originalDelimiters[count - 1])
				count--;
		}
		if (count != 0) {
			fLineDelimiters = originalDelimiters;
			System.arraycopy(fLineDelimiters, 0, fSortedLineDelimiters, 0, fLineDelimiters.length);
			Arrays.sort(fSortedLineDelimiters, fLineDelimiterComparator);
		}

		int c;
		boolean okToScan = resume;
		int charCount = 0;

		if (!resume) {
			// Not resuming.  Verify first char is '@'.
			c = scanner.read();
			++charCount;
			if (c == '@') {
				okToScan = true;
			}
		}

		if (okToScan) {
			// We want to make sure we have a valid id (not @@) or (@_@).  When
			// we resume, we have no choice but to assume it is valid so far.
			boolean isId = resume;
			++charCount;
			while ((c = scanner.read()) != ICharacterScanner.EOF) {
				if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
					// A valid id has some alphabetic character in it.
					isId = true;
				} else if (c >= '0' && c <= '9' || c == '_') {
					// continue
				} else if (c == '@' && isId)
					return getSuccessToken();
				else
					break;
				++charCount;
			}
		}

		for (int i = 0; i < charCount; ++i)
			scanner.unread();

		return Token.UNDEFINED;
	}

	@Override
	public IToken getSuccessToken() {
		return token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}

}
