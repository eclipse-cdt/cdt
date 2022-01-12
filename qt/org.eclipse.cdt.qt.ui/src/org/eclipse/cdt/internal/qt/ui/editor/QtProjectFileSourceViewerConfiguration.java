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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class QtProjectFileSourceViewerConfiguration extends TextSourceViewerConfiguration {
	private static int TOKEN_DEFAULT = 0;
	private static int TOKEN_FUNCTION_KEYWORD = 1;
	private static int TOKEN_VARIABLE_KEYWORD = 2;
	private static int TOKEN_COMMENT = 3;

	// TODO: Add preference page for syntax highlighting
	private static IToken[] allTokens = new IToken[] { new Token(null),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(140, 140, 0)))),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(140, 0, 100)))),
			new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 140, 0)))) };

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer viewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);

		DefaultDamagerRepairer dr;
		dr = new DefaultDamagerRepairer(createDefaultTokenScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}

	private ITokenScanner createDefaultTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();

		WordRule wordRule = new WordRule(new IWordDetector() {
			@Override
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c) && c != '$';
			}

			@Override
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c) && c != '$';
			}
		}, allTokens[TOKEN_DEFAULT]);

		// QMake function keywords
		for (QtProjectFileKeyword keyword : QtProjectFileKeyword.getFunctionKeywords()) {
			wordRule.addWord(keyword.getKeyword(), allTokens[TOKEN_FUNCTION_KEYWORD]);
		}

		// QMake variable keywords
		for (QtProjectFileKeyword keyword : QtProjectFileKeyword.getVariableKeywords()) {
			wordRule.addWord(keyword.getKeyword(), allTokens[TOKEN_VARIABLE_KEYWORD]);
		}

		scanner.setRules(new IRule[] { wordRule, new EndOfLineRule("#", allTokens[TOKEN_COMMENT]) //$NON-NLS-1$
		});
		return scanner;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant contentAssistant = new ContentAssistant();
		IContentAssistProcessor processor = new QtProjectFileContentAssistProcessor();
		contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return contentAssistant;
	}
}
