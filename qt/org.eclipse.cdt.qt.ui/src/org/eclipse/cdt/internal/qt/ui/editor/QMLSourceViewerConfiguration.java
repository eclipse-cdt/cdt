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

import java.util.Map;

import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
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
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * Performs syntax highlighting for the {@link QMLEditor}.
 */
public class QMLSourceViewerConfiguration extends TextSourceViewerConfiguration {
	private Token defaultToken;
	private Token multiLineCommentToken;
	private Token singleLineCommentToken;
	private Token keywordToken;
	private Token stringToken;
	private Token taskTagToken;

	private final QMLEditor editor;

	public QMLSourceViewerConfiguration(QMLEditor editor, IPreferenceStore prefs) {
		super(prefs);
		this.editor = editor;
		initTokens(prefs);
	}

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
		scanner.setDefaultReturnToken(multiLineCommentToken);
		scanner.setRules(new IRule[] { createTaskTagRule(multiLineCommentToken) });
		return scanner;
	}

	private ITokenScanner createSingleLineCommentTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(singleLineCommentToken);
		scanner.setRules(new IRule[] { createTaskTagRule(singleLineCommentToken) });
		return scanner;
	}

	private ITokenScanner createStringTokenScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(stringToken);
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
		}, defaultToken);

		// Works decently well for now. However, some keywords like 'color' can
		// also be used as identifiers. Can only fix this with
		// semantic highlighting after the parser is completed.
		for (String keyword : QMLKeywords.getKeywords(true)) {
			wordRule.addWord(keyword, keywordToken);
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
		wordRule.addWord("TODO", taskTagToken); //$NON-NLS-1$
		wordRule.addWord("FIXME", taskTagToken); //$NON-NLS-1$
		wordRule.addWord("XXX", taskTagToken); //$NON-NLS-1$

		return wordRule;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant contentAssistant = new ContentAssistant();
		IContentAssistProcessor processor = new QMLContentAssistProcessor(editor);
		contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return contentAssistant;
	}

	@Override
	protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("org.eclipse.cdt.qt.ui.qml", editor); //$NON-NLS-1$
		return targets;
	}

	private void initTokens(IPreferenceStore prefStore) {
		IColorManager colorManager = CDTUITools.getColorManager();
		defaultToken = new Token(null);
		multiLineCommentToken = new Token(createTextAttribute(colorManager, ICColorConstants.C_MULTI_LINE_COMMENT));
		singleLineCommentToken = new Token(createTextAttribute(colorManager, ICColorConstants.C_SINGLE_LINE_COMMENT));
		keywordToken = new Token(createTextAttribute(colorManager, ICColorConstants.C_KEYWORD));
		stringToken = new Token(createTextAttribute(colorManager, ICColorConstants.C_STRING));
		taskTagToken = new Token(createTextAttribute(colorManager, ICColorConstants.TASK_TAG));
	}

	private TextAttribute createTextAttribute(IColorManager colorManager, String colorKey) {
		Color color = colorManager.getColor(colorKey);
		if (color == null) {
			RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
			colorManager.unbindColor(colorKey);
			colorManager.bindColor(colorKey, rgb);
			color = colorManager.getColor(colorKey);
		}

		String boldKey = colorKey + PreferenceConstants.EDITOR_BOLD_SUFFIX;
		String italicKey = colorKey + PreferenceConstants.EDITOR_ITALIC_SUFFIX;
		String strikethroughKey = colorKey + PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
		String underlineKey = colorKey + PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;

		int style = fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;
		if (fPreferenceStore.getBoolean(italicKey))
			style |= SWT.ITALIC;

		if (fPreferenceStore.getBoolean(strikethroughKey))
			style |= TextAttribute.STRIKETHROUGH;

		if (fPreferenceStore.getBoolean(underlineKey))
			style |= TextAttribute.UNDERLINE;

		return new TextAttribute(color, null, style);
	}

	public void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		IColorManager colorManager = CDTUITools.getColorManager();
		String property = event.getProperty();
		if (property.startsWith(ICColorConstants.C_MULTI_LINE_COMMENT)) {
			multiLineCommentToken.setData(createTextAttribute(colorManager, ICColorConstants.C_MULTI_LINE_COMMENT));
		}
		if (property.startsWith(ICColorConstants.C_SINGLE_LINE_COMMENT)) {
			singleLineCommentToken.setData(createTextAttribute(colorManager, ICColorConstants.C_SINGLE_LINE_COMMENT));
		}
		if (property.startsWith(ICColorConstants.C_KEYWORD)) {
			keywordToken.setData(createTextAttribute(colorManager, ICColorConstants.C_KEYWORD));
		}
		if (property.startsWith(ICColorConstants.C_STRING)) {
			stringToken.setData(createTextAttribute(colorManager, ICColorConstants.C_STRING));
		}
		if (property.startsWith(ICColorConstants.TASK_TAG)) {
			taskTagToken.setData(createTextAttribute(colorManager, ICColorConstants.TASK_TAG));
		}
	}

	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		String property = event.getProperty();
		return property.startsWith(ICColorConstants.C_MULTI_LINE_COMMENT)
				|| property.startsWith(ICColorConstants.C_SINGLE_LINE_COMMENT)
				|| property.startsWith(ICColorConstants.C_KEYWORD) || property.startsWith(ICColorConstants.C_STRING)
				|| property.startsWith(ICColorConstants.TASK_TAG);
	}

}
