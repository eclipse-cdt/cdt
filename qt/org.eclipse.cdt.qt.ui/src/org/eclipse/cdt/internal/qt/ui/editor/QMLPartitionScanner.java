/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * Partitions a QML document into four distinct sections:
 * <ol>
 * <li>multi-line comments</li>
 * <li>single-line comments</li>
 * <li>strings</li>
 * <li>anything else that does not fall under the aforementioned categories</li>
 * </ol>
 */
public class QMLPartitionScanner extends RuleBasedPartitionScanner implements IQMLPartitions {

	public QMLPartitionScanner() {
		super();

		IToken multiLineComment = new Token(QML_MULTI_LINE_COMMENT);
		IToken singleLineComment = new Token(QML_SINGLE_LINE_COMMENT);
		IToken string = new Token(QML_STRING);

		setPredicateRules(new IPredicateRule[] { new MultiLineRule("/*", "*/", multiLineComment, (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
				new EndOfLineRule("//", singleLineComment), //$NON-NLS-1$
				new SingleLineRule("\"", "\"", string, '\\', true) //$NON-NLS-2$ //$NON-NLS-1$
		});
	}
}
