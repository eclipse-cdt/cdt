/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * DisassemblyViewerConfiguration
 */
public class DisassemblyViewerConfiguration extends TextSourceViewerConfiguration {

	private DisassemblyPart fPart;

	/**
	 * SimpleDamagerRepairer
	 */
	public class SimpleDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

		/*
		 * @see org.eclipse.jface.text.presentation.IPresentationDamager#setDocument(org.eclipse.jface.text.IDocument)
		 */
		@Override
		public void setDocument(IDocument document) {
		}

		/*
		 * @see org.eclipse.jface.text.presentation.IPresentationDamager#getDamageRegion(org.eclipse.jface.text.ITypedRegion, org.eclipse.jface.text.DocumentEvent, boolean)
		 */
		@Override
		public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
			int start= e.fOffset;
			int end= e.getOffset() + (e.getText() == null ? 0 : e.getText().length());
			return new Region(start, end - start);
		}

		/*
		 * @see org.eclipse.jface.text.presentation.IPresentationRepairer#createPresentation(org.eclipse.jface.text.TextPresentation, org.eclipse.jface.text.ITypedRegion)
		 */
		@Override
		public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
			// do nothing
		}

	}

	/**
	 * 
	 */
	public DisassemblyViewerConfiguration(DisassemblyPart part) {
		super(EditorsUI.getPreferenceStore());
		fPart = part;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		SimpleDamagerRepairer dr = new SimpleDamagerRepairer();
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getUndoManager(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
		// no undo/redo
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new DisassemblyTextHover(fPart);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		IHyperlinkDetector[] inheritedDetectors= super.getHyperlinkDetectors(sourceViewer);
		
		if (fPart == null || inheritedDetectors == null)
			return inheritedDetectors;
		
		int inheritedDetectorsLength= inheritedDetectors.length;
		IHyperlinkDetector[] detectors= new IHyperlinkDetector[inheritedDetectorsLength + 1];
		detectors[0]= new DisassemblyHyperlinkDetector(fPart);
		for (int i= 0; i < inheritedDetectorsLength; i++) {
			detectors[i+1]= inheritedDetectors[i];
		}
		
		return detectors;
	}
	
	/*
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		// disable spell checking
		return null;
	}
}
