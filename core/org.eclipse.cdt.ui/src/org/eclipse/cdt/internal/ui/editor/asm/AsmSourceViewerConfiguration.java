/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


public class AsmSourceViewerConfiguration extends SourceViewerConfiguration {


	private AsmTextEditor fEditor;
	private AsmTextTools fTextTools;
	
	/**
	 * Returns the ASM multiline comment scanner for this configuration.
	 *
	 * @return the ASM multiline comment scanner
	 */
	protected RuleBasedScanner getMultilineCommentScanner() {
		return fTextTools.getMultilineCommentScanner();
	}
	
	/**
	 * Returns the ASM singleline comment scanner for this configuration.
	 *
	 * @return the ASM singleline comment scanner
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fTextTools.getSinglelineCommentScanner();
	}
	
	/**
	 * Returns the ASM string scanner for this configuration.
	 *
	 * @return the ASM string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fTextTools.getStringScanner();
	}
	
	/**
	 * Constructor for AsmSourceViewerConfiguration
	 */
	public AsmSourceViewerConfiguration(AsmTextTools tools, AsmTextEditor editor) {
		super();
		fEditor = editor;
		fTextTools = tools;
	}
	
	/**
	 * @see ISourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {



		// CColorManager manager= fTextTools.getColorManager();
		PresentationReconciler reconciler= new PresentationReconciler();



		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(fTextTools.getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);



		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, AsmPartitionScanner.ASM_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, AsmPartitionScanner.ASM_MULTILINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, AsmPartitionScanner.ASM_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, AsmPartitionScanner.ASM_SINGLE_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getStringScanner());		
		reconciler.setDamager(dr, AsmPartitionScanner.ASM_STRING);
		reconciler.setRepairer(dr, AsmPartitionScanner.ASM_STRING);


		return reconciler;
	}
	
	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	IDocument.DEFAULT_CONTENT_TYPE, 
								AsmPartitionScanner.ASM_MULTILINE_COMMENT,
								AsmPartitionScanner.ASM_SINGLE_LINE_COMMENT,
								AsmPartitionScanner.ASM_STRING };
	}


}


