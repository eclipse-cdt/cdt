/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class MakefilePartitionScanner extends RuleBasedPartitionScanner {
	// Partition types
	public final static String MAKEFILE_COMMENT_PARTITION = "makefile_comment"; //$NON-NLS-1$
	public final static String MAKEFILE_OTHER_PARTITION = IDocument.DEFAULT_CONTENT_TYPE;

	public final static String[] MAKE_PARTITIONS = new String[] { MAKEFILE_COMMENT_PARTITION,
			MAKEFILE_OTHER_PARTITION, };

	/** The predefined delimiters of this tracker */
	private char[][] fModDelimiters = { { '\r', '\n' }, { '\r' }, { '\n' } };

	/**
	 * Constructor for MakefilePartitionScanner
	 */
	public MakefilePartitionScanner() {
		super();

		IToken tComment = new Token(MAKEFILE_COMMENT_PARTITION);

		List<EndOfLineRule> rules = new ArrayList<>();

		// Add rule for single line comments.

		EndOfLineRule commentRule = new EndOfLineRule("#", tComment, '\\', true) { //$NON-NLS-1$
			@Override
			protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) {
				int c = scanner.read();
				if (c == fEscapeCharacter) {
					c = scanner.read();
					if (c == fStartSequence[0]) {
						return Token.UNDEFINED;
					}
					scanner.unread();
				}
				scanner.unread();
				return super.doEvaluate(scanner, resume);
			}
		};
		rules.add(commentRule);

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);

	}

	/*
	 * @see ICharacterScanner#getLegalLineDelimiters
	 */
	@Override
	public char[][] getLegalLineDelimiters() {
		return fModDelimiters;
	}

}
