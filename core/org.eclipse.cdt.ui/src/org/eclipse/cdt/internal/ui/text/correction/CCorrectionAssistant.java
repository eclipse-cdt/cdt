/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.IColorManager;

import org.eclipse.cdt.internal.core.model.ASTCache;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.text.CTextTools;

public class CCorrectionAssistant extends QuickAssistAssistant {
	private ITextViewer fViewer;
	private ITextEditor fEditor;
	private Position fPosition;
	private Annotation[] fCurrentAnnotations;

	private QuickAssistLightBulbUpdater fLightBulbUpdater;

	/**
	 * Constructor for CCorrectionAssistant.
	 */
	public CCorrectionAssistant(ITextEditor editor) {
		super();
		Assert.isNotNull(editor);
		fEditor= editor;

		CCorrectionProcessor processor= new CCorrectionProcessor(this);

		setQuickAssistProcessor(processor);
		enableColoredLabels(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));

		setInformationControlCreator(getInformationControlCreator());

		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		IColorManager manager= textTools.getColorManager();

		IPreferenceStore store=  CUIPlugin.getDefault().getPreferenceStore();

		Color c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, manager);
		setProposalSelectorForeground(c);

		c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, manager);
		setProposalSelectorBackground(c);
	}

	public IEditorPart getEditor() {
		return fEditor;
	}


	private IInformationControlCreator getInformationControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, false);
			}
		};
	}

	private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistant#install(org.eclipse.jface.text.ITextViewer)
	 */
	@Override
	public void install(ISourceViewer sourceViewer) {
		super.install(sourceViewer);
		fViewer= sourceViewer;

		fLightBulbUpdater= new QuickAssistLightBulbUpdater(fEditor, sourceViewer);
		fLightBulbUpdater.install();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ContentAssistant#uninstall()
	 */
	@Override
	public void uninstall() {
		if (fLightBulbUpdater != null) {
			fLightBulbUpdater.uninstall();
			fLightBulbUpdater= null;
		}
		super.uninstall();
	}

	/**
	 * Show completions at caret position. If current
	 * position does not contain quick fixes look for
	 * next quick fix on same line by moving from left
	 * to right and restarting at end of line if the
	 * beginning of the line is reached.
	 *
	 * @see IQuickAssistAssistant#showPossibleQuickAssists()
	 */
	@Override
	public String showPossibleQuickAssists() {
		fPosition= null;
		fCurrentAnnotations= null;
		
		if (fViewer == null || fViewer.getDocument() == null)
			// Let superclass deal with this
			return super.showPossibleQuickAssists();


		ArrayList<Annotation> resultingAnnotations= new ArrayList<Annotation>(20);
		try {
			Point selectedRange= fViewer.getSelectedRange();
			int currOffset= selectedRange.x;
			int currLength= selectedRange.y;
			boolean goToClosest= (currLength == 0);
			
			int newOffset= collectQuickFixableAnnotations(fEditor, currOffset, goToClosest, resultingAnnotations);
			if (newOffset != currOffset) {
				storePosition(currOffset, currLength);
				fViewer.setSelectedRange(newOffset, 0);
				fViewer.revealRange(newOffset, 0);
			}
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
		fCurrentAnnotations= resultingAnnotations.toArray(new Annotation[resultingAnnotations.size()]);

		return super.showPossibleQuickAssists();
	}
	
	
	private static IRegion getRegionOfInterest(ITextEditor editor, int invocationLocation) throws BadLocationException {
		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider == null) {
			return null;
		}
		IDocument document= documentProvider.getDocument(editor.getEditorInput());
		if (document == null) {
			return null;
		}
		return document.getLineInformationOfOffset(invocationLocation);
	}
	
	public static int collectQuickFixableAnnotations(ITextEditor editor, int invocationLocation, boolean goToClosest, ArrayList<Annotation> resultingAnnotations) throws BadLocationException {
		IAnnotationModel model= CUIPlugin.getDefault().getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		if (model == null) {
			return invocationLocation;
		}
		
		ensureUpdatedAnnotations(editor);
		
		Iterator<?> iter= model.getAnnotationIterator();
		if (goToClosest) {
			IRegion lineInfo= getRegionOfInterest(editor, invocationLocation);
			if (lineInfo == null) {
				return invocationLocation;
			}
			int rangeStart= lineInfo.getOffset();
			int rangeEnd= rangeStart + lineInfo.getLength();
			
			ArrayList<Annotation> allAnnotations= new ArrayList<Annotation>();
			ArrayList<Position> allPositions= new ArrayList<Position>();
			int bestOffset= Integer.MAX_VALUE;
			while (iter.hasNext()) {
				Annotation annot= (Annotation) iter.next();
				if (CCorrectionProcessor.isQuickFixableType(annot)) {
					Position pos= model.getPosition(annot);
					if (pos != null && isInside(pos.offset, rangeStart, rangeEnd)) { // inside our range?
						allAnnotations.add(annot);
						allPositions.add(pos);
						bestOffset= processAnnotation(annot, pos, invocationLocation, bestOffset);
					}
				}
			}
			if (bestOffset == Integer.MAX_VALUE) {
				return invocationLocation;
			}
			for (int i= 0; i < allPositions.size(); i++) {
				Position pos= allPositions.get(i);
				if (isInside(bestOffset, pos.offset, pos.offset + pos.length)) {
					resultingAnnotations.add(allAnnotations.get(i));
				}
			}
			return bestOffset;
		}
		while (iter.hasNext()) {
			Annotation annot= (Annotation) iter.next();
			if (CCorrectionProcessor.isQuickFixableType(annot)) {
				Position pos= model.getPosition(annot);
				if (pos != null && isInside(invocationLocation, pos.offset, pos.offset + pos.length)) {
					resultingAnnotations.add(annot);
				}
			}
		}
		return invocationLocation;
	}

	private static void ensureUpdatedAnnotations(ITextEditor editor) {
		Object inputElement= editor.getEditorInput().getAdapter(ICElement.class);
		if (inputElement instanceof ITranslationUnit) {
			final ASTProvider astProvider= CUIPlugin.getDefault().getASTProvider();
			astProvider.runOnAST((ITranslationUnit) inputElement, ASTProvider.WAIT_ACTIVE_ONLY, null, new ASTCache.ASTRunnable() {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
					return Status.OK_STATUS;
				}
			});
		}
	}

	private static int processAnnotation(Annotation annot, Position pos, int invocationLocation, int bestOffset) {
		int posBegin= pos.offset;
		int posEnd= posBegin + pos.length;
		if (isInside(invocationLocation, posBegin, posEnd)) { // covers invocation location?
			return invocationLocation;
		} else if (bestOffset != invocationLocation) {
			int newClosestPosition= computeBestOffset(posBegin, invocationLocation, bestOffset);
			if (newClosestPosition != -1) { 
				if (newClosestPosition != bestOffset) { // new best
					if (CCorrectionProcessor.hasCorrections(annot)) { // only jump to it if there are proposals
						return newClosestPosition;
					}
				}
			}
		}
		return bestOffset;
	}

	private static boolean isInside(int offset, int start, int end) {
		return offset == start || offset == end || (offset > start && offset < end); // make sure to handle 0-length ranges
	}

	/**
	 * Computes and returns the invocation offset given a new
	 * position, the initial offset and the best invocation offset
	 * found so far.
	 * <p>
	 * The closest offset to the left of the initial offset is the
	 * best. If there is no offset on the left, the closest on the
	 * right is the best.</p>
	 * @return -1 is returned if the given offset is not closer or the new best offset
	 */
	private static int computeBestOffset(int newOffset, int invocationLocation, int bestOffset) {
		if (newOffset <= invocationLocation) {
			if (bestOffset > invocationLocation) {
				return newOffset; // closest was on the right, prefer on the left
			} else if (bestOffset <= newOffset) {
				return newOffset; // we are closer or equal
			}
			return -1; // further away
		}

		if (newOffset <= bestOffset)
			return newOffset; // we are closer or equal

		return -1; // further away
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ContentAssistant#possibleCompletionsClosed()
	 */
	@Override
	protected void possibleCompletionsClosed() {
		super.possibleCompletionsClosed();
		restorePosition();
	}

	private void storePosition(int currOffset, int currLength) {
		fPosition= new Position(currOffset, currLength);
	}

	private void restorePosition() {
		if (fPosition != null && !fPosition.isDeleted() && fViewer.getDocument() != null) {
			fViewer.setSelectedRange(fPosition.offset, fPosition.length);
			fViewer.revealRange(fPosition.offset, fPosition.length);
		}
		fPosition= null;
	}

	/**
	 * Returns true if the last invoked completion was called with an updated offset.
	 */
	public boolean isUpdatedOffset() {
		return fPosition != null;
	}

	/**
	 * Returns the annotations at the current offset
	 */
	public Annotation[] getAnnotationsAtOffset() {
		return fCurrentAnnotations;
	}
}
