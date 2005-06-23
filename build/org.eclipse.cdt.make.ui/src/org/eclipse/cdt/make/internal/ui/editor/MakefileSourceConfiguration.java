/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileAnnotationHover;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileCodeScanner;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileCompletionProcessor;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefilePartitionScanner;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileReconcilingStrategy;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileTextHover;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

public class MakefileSourceConfiguration extends SourceViewerConfiguration {

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
	public MakefileSourceConfiguration(MakefileEditor editor) {
		super();
		fEditor = editor;
		colorManager = ColorManager.getDefault();
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer v) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION,
			MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION,
			MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION,
			MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION,
			MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION,
		};

	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);

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

	/**
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			MonoReconciler reconciler= new MonoReconciler(new MakefileReconcilingStrategy(fEditor), false);
			reconciler.setDelay(1000);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			return reconciler;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[]{"#"}; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new MakefileTextHover(fEditor);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 */
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
