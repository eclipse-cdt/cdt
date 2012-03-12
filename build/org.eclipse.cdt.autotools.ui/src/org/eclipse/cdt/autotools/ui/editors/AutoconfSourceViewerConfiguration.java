/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.cdt.internal.autotools.ui.text.hover.AutoconfTextHover;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;


public class AutoconfSourceViewerConfiguration extends
		TextSourceViewerConfiguration {

	private ITextHover acHover;
	private IAnnotationHover aaHover;
	private AutoconfEditor fEditor;
	
	public AutoconfSourceViewerConfiguration(IPreferenceStore prefs, AutoconfEditor editor) {
		super(prefs);
		fEditor = editor;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return AutoconfEditor.AUTOCONF_PARTITIONING;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		
		IContentAssistProcessor macroContentAssistProcessor =
			new AutoconfMacroContentAssistProcessor(new AutoconfMacroCodeScanner(), fEditor);
		assistant.setContentAssistProcessor(macroContentAssistProcessor, AutoconfPartitionScanner.AUTOCONF_MACRO);
		assistant.setContentAssistProcessor(macroContentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(AutoconfTextHover.getInformationControlCreator());
		
		return assistant;
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
							  AutoconfPartitionScanner.AUTOCONF_MACRO,
							  AutoconfPartitionScanner.AUTOCONF_COMMENT};
	}
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (acHover == null)
			acHover = new AutoconfTextHover(fEditor);
		return acHover;
	}
	
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		if (aaHover == null)
			aaHover = new AutoconfAnnotationHover();
		return aaHover;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		MonoReconciler reconciler= new MonoReconciler(new AutoconfReconcilingStrategy(fEditor), false);
		reconciler.setDelay(1000);
		reconciler.setProgressMonitor(new NullProgressMonitor());
		return reconciler;
	}
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr= new AutoconfMacroDamagerRepairer(new AutoconfMacroCodeScanner());
		reconciler.setDamager(dr, AutoconfPartitionScanner.AUTOCONF_MACRO);
		reconciler.setRepairer(dr, AutoconfPartitionScanner.AUTOCONF_MACRO);
		
		dr= new DefaultDamagerRepairer(new AutoconfCodeScanner());
		reconciler.setDamager(dr, AutoconfPartitionScanner.AUTOCONF_COMMENT);
		reconciler.setRepairer(dr, AutoconfPartitionScanner.AUTOCONF_COMMENT);
		
		dr= new MultilineRuleDamagerRepairer(new AutoconfCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		return reconciler;
	}
}
