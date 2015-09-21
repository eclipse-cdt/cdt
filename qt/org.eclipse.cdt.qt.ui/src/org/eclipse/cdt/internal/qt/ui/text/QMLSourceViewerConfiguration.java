/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.text;

import org.eclipse.cdt.internal.qt.ui.editor.QMLEditor;
import org.eclipse.cdt.internal.qt.ui.editor.QMLKeywords;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * Performs syntax highlighting for the {@link QMLEditor}.
 */
public class QMLSourceViewerConfiguration extends TextSourceViewerConfiguration {
	private static final int TOKEN_DEFAULT = 0;
	private static final int TOKEN_MULTI_LINE_COMMENT = 1;
	private static final int TOKEN_SINGLE_LINE_COMMENT = 2;
	private static final int TOKEN_KEYWORD = 3;
	private static final int TOKEN_STRING = 4;
	private static final int TOKEN_TASK_TAG = 5;

	// Just using Qt Creator defaults-ish for now
	// TODO: Add preference page for syntax highlighting
	private static final IToken[] allTokens = new IToken[] {
			new Token(null),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 155, 200)))),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 155, 200)))),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(155, 155, 0)), null, SWT.BOLD)),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 155, 0)), null, SWT.ITALIC)),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 100, 155)), null, SWT.BOLD))
	};

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer viewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(IQMLPartitions.QML_PARTITIONING);

		DefaultDamagerRepairer dr;
		dr = new DefaultDamagerRepairer(createMultiLineCommentTokenScanner());
		reconciler.setDamager(dr, IQMLPartitions.QML_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, IQMLPartitions.QML_MULTI_LINE_COMMENT);

		dr = new DefaultDamagerRepairer(createSingleLineCommentTokenScanner());
		reconciler.setDamager(dr, IQMLPartitions.QML_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, IQMLPartitions.QML_SINGLE_LINE_COMMENT);

		dr = new DefaultDamagerRepairer(createStringTokenScanner());
		reconciler.setDamager(dr, IQMLPartitions.QML_STRING);
		reconciler.setRepairer(dr, IQMLPartitions.QML_STRING);

		dr = new DefaultDamagerRepairer(createDefaultTokenScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}

	private ITokenScanner createMultiLineCommentTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(allTokens[TOKEN_MULTI_LINE_COMMENT]);
		scanner.setRules(new IRule[] { createTaskTagRule(allTokens[TOKEN_MULTI_LINE_COMMENT]) });
		return scanner;
	}

	private ITokenScanner createSingleLineCommentTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(allTokens[TOKEN_SINGLE_LINE_COMMENT]);
		scanner.setRules(new IRule[] { createTaskTagRule(allTokens[TOKEN_SINGLE_LINE_COMMENT]) });
		return scanner;
	}

	private ITokenScanner createStringTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(allTokens[TOKEN_STRING]);
		return scanner;
	}

	private ITokenScanner createDefaultTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();

		WordRule wordRule = new WordRule(new IWordDetector() {
			@Override
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c);
			}

			@Override
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		}, allTokens[TOKEN_DEFAULT]);

		// Works decently well for now. However, some keywords like 'color' can also be used as identifiers. Can only fix this with
		// semantic highlighting after the parser is completed.
		for (String keyword : QMLKeywords.getKeywords(true)) {
			wordRule.addWord(keyword, allTokens[TOKEN_KEYWORD]);
		}

		scanner.setRules(new IRule[] { wordRule });
		return scanner;
	}

	private IRule createTaskTagRule(IToken defaultToken) {
		WordRule wordRule = new WordRule(new IWordDetector() {
			@Override
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c);
			}

			@Override
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		}, defaultToken);

		// TODO: Add preference page for task tags
		wordRule.addWord("TODO", allTokens[TOKEN_TASK_TAG]); //$NON-NLS-1$
		wordRule.addWord("FIXME", allTokens[TOKEN_TASK_TAG]); //$NON-NLS-1$
		wordRule.addWord("XXX", allTokens[TOKEN_TASK_TAG]); //$NON-NLS-1$

		return wordRule;
	}
}
