/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.internal.autotools.ui.preferences.ColorManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class MakefileSourceConfiguration extends TextSourceViewerConfiguration {

	private ColorManager colorManager;
	MakefileCodeScanner codeScanner;
	private MakefileEditor fEditor;

	/**
	 * Single token scanner.
	 */
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	}

	/**
	 * Constructor for MakeConfiguration
	 */
	public MakefileSourceConfiguration(IPreferenceStore preferenceStore, MakefileEditor editor) {
		super(preferenceStore);
		fEditor = editor;
		colorManager = ColorManager.getDefault();
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer v) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION,
				MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION,
				MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION,
				MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION,
				MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION, };

	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor),
				MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor),
				MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor),
				MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor),
				MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor),
				MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);

		assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		//Set to Carolina blue
		assistant.setContextInformationPopupBackground(colorManager.getColor(new RGB(0, 191, 255)));

		return assistant;
	}

	protected MakefileCodeScanner getCodeScanner() {
		if (null == codeScanner)
			codeScanner = new MakefileCodeScanner();
		return codeScanner;

	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer v) {

		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_OTHER_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_OTHER_PARTITION);
		return reconciler;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			MonoReconciler reconciler = new MonoReconciler(new MakefileReconcilingStrategy(fEditor), false);
			reconciler.setDelay(1000);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			return reconciler;
		}
		return null;
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "#" }; //$NON-NLS-1$
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new MakefileTextHover(fEditor);
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new MakefileAnnotationHover(fEditor);
	}

	/**
	 * @param event
	 * @return
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		MakefileCodeScanner scanner = getCodeScanner();
		return scanner.affectsBehavior(event);
	}

	/**
	 * @param event
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		MakefileCodeScanner scanner = getCodeScanner();
		scanner.adaptToPreferenceChange(event);
	}

}
