/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import org.eclipse.cdt.ui.text.ICPartitions;


public class AsmSourceViewerConfiguration extends TextSourceViewerConfiguration {

	private AsmTextTools fAsmTextTools;
	
	/**
	 * Constructor for AsmSourceViewerConfiguration
	 */
	public AsmSourceViewerConfiguration(AsmTextTools tools) {
		super();
		fAsmTextTools = tools;
	}

	/**
	 * Returns the ASM multiline comment scanner for this configuration.
	 *
	 * @return the ASM multiline comment scanner
	 */
	protected RuleBasedScanner getMultilineCommentScanner() {
		return fAsmTextTools.getMultilineCommentScanner();
	}
	
	/**
	 * Returns the ASM singleline comment scanner for this configuration.
	 *
	 * @return the ASM singleline comment scanner
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fAsmTextTools.getSinglelineCommentScanner();
	}
	
	/**
	 * Returns the ASM string scanner for this configuration.
	 *
	 * @return the ASM string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fAsmTextTools.getStringScanner();
	}

	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		// the ASM editor also uses the CDocumentPartitioner
		return ICPartitions.C_PARTITIONING;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler= new PresentationReconciler();

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(fAsmTextTools.getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getStringScanner());		
		reconciler.setDamager(dr, ICPartitions.C_STRING);
		reconciler.setRepairer(dr, ICPartitions.C_STRING);

		dr= new DefaultDamagerRepairer(getStringScanner());		
		reconciler.setDamager(dr, ICPartitions.C_CHARACTER);
		reconciler.setRepairer(dr, ICPartitions.C_CHARACTER);

		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		return reconciler;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	
				IDocument.DEFAULT_CONTENT_TYPE, 
				ICPartitions.C_MULTI_LINE_COMMENT,
				ICPartitions.C_SINGLE_LINE_COMMENT,
				ICPartitions.C_STRING,
				ICPartitions.C_CHARACTER };
	}


}


