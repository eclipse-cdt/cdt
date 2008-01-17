/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer.IMacroExpansionStep;

import org.eclipse.cdt.internal.ui.compare.CMergeViewer;

/**
 * A viewer for comparison of macro expansions.
 *
 * @since 5.0
 */
class CMacroCompareViewer extends CMergeViewer {

	private static final RGB CHANGE_COLOR= new RGB(212,212,212);

	private class ReplaceEditsHighlighter implements ITextPresentationListener {
		private boolean fBefore;
		private int[] fStarts;
		private int[] fLengths;
		private Color fBackground;

		public ReplaceEditsHighlighter(Color background, boolean before) {
			fBackground= background;
			fBefore= before;
		}

		public void setReplaceEdits(ReplaceEdit[] edits) {
			int[] deltas= new int[edits.length];
			if (fBefore) {
				for (int i= 1; i < edits.length; i++) {
					ReplaceEdit edit= edits[i-1];
					deltas[i]= deltas[i-1] + (edit.getText().length() - edit.getLength());
				}
			}
			fStarts= new int[edits.length];
			fLengths= new int[edits.length];
			for (int i= 0; i < edits.length; i++) {
				ReplaceEdit edit= edits[i];
				fStarts[i]= edit.getOffset() + deltas[i];
				fLengths[i]= fBefore ? edit.getLength() : edit.getText().length();
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.ITextPresentationListener#applyTextPresentation(org.eclipse.jface.text.TextPresentation)
		 */
		public void applyTextPresentation(TextPresentation textPresentation) {
			for (int i = 0; i < fStarts.length; i++) {
				textPresentation.mergeStyleRange(new StyleRange(fStarts[i], fLengths[i], null, fBackground));
			}
		}

	}

//	private class MacroExpansionComparator implements ITokenComparator {
//
//		private final int[] fStarts;
//		private final int[] fLengths;
//		private int fCount;
//
//		public MacroExpansionComparator(String text, ReplaceEdit[] edits, boolean before) {
//			int[] deltas= new int[edits.length];
//			if (before) {
//				for (int i= 1; i < edits.length; i++) {
//					ReplaceEdit edit= edits[i-1];
//					deltas[i]= deltas[i-1] + (edit.getText().length() - edit.getLength());
//				}
//			}
//			fStarts= new int[edits.length * 2 + 1];
//			fLengths= new int[edits.length * 2 + 1];
//			int offset= 0;
//			int i= 0;
//			for (; i < edits.length; i++) {
//				if (offset >= text.length()) {
//					break;
//				}
//				fStarts[2*i]= offset;
//				ReplaceEdit edit= edits[i];
//				fLengths[2*i]= edit.getOffset() + deltas[i] - offset;
//				fStarts[2*i+1]= edit.getOffset() + deltas[i];
//				fLengths[2*i+1]= before ? edit.getLength() : edit.getText().length();
//				offset= fStarts[2*i+1] + fLengths[2*i+1];
//			}
//			fCount= 2*i;
//			
//			if (offset < text.length()) {
//				fStarts[fCount]= offset;
//				fLengths[fCount]= text.length() - offset;
//				fCount++;
//			}
//		}
//
//		/*
//		 * @see org.eclipse.compare.contentmergeviewer.ITokenComparator#getTokenLength(int)
//		 */
//		public int getTokenLength(int index) {
//			return fLengths[index];
//		}
//
//		/*
//		 * @see org.eclipse.compare.contentmergeviewer.ITokenComparator#getTokenStart(int)
//		 */
//		public int getTokenStart(int index) {
//			return fStarts[index];
//		}
//
//		/*
//		 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
//		 */
//		public int getRangeCount() {
//			return fCount;
//		}
//
//		/*
//		 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int, org.eclipse.compare.rangedifferencer.IRangeComparator, int)
//		 */
//		public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
//			return thisIndex == otherIndex && thisIndex % 2 == 0;
//		}
//
//		/*
//		 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int, int, org.eclipse.compare.rangedifferencer.IRangeComparator)
//		 */
//		public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
//			return false;
//		}
//
//	}

	/**
	 * A dummy {@link ITokenComparator}.
	 */
	private static class NullTokenComparator implements ITokenComparator {
		public int getTokenLength(int index) {
			return 0;
		}
		public int getTokenStart(int index) {
			return 0;
		}
		public int getRangeCount() {
			return 0;
		}
		public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
			return true;
		}
		public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
			return true;
		}
	}

	TextViewer fLeftViewer;
	TextViewer fRightViewer;
	TextViewer fTopViewer;
	
	int fIndex;
	private CMacroExpansionInput fInput;
	private int fStepIndex;
	private ReplaceEditsHighlighter fLeftHighlighter;
	private ReplaceEditsHighlighter fRightHighlighter;
	private Color fChangeBackground;
	
	public CMacroCompareViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
		Font font= JFaceResources.getFont(CMergeViewer.class.getName());
		fLeftViewer.getTextWidget().setFont(font);
		fRightViewer.getTextWidget().setFont(font);
		fTopViewer.getTextWidget().setFont(font);
		
		fChangeBackground= new Color(parent.getDisplay(), CHANGE_COLOR);
		fLeftViewer.addTextPresentationListener(fLeftHighlighter= new ReplaceEditsHighlighter(fChangeBackground, true));
		fRightViewer.addTextPresentationListener(fRightHighlighter= new ReplaceEditsHighlighter(fChangeBackground, false));
		fIndex= 0;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.compare.AbstractMergeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
	 */
	protected void handleDispose(DisposeEvent event) {
		fLeftViewer.removeTextPresentationListener(fLeftHighlighter);
		fRightViewer.removeTextPresentationListener(fRightHighlighter);
		fChangeBackground.dispose();
		super.handleDispose(event);
	}
	
	protected IToolBarManager getToolBarManager(Composite parent) {
		// no toolbar
		return null;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.compare.AbstractMergeViewer#configureTextViewer(org.eclipse.jface.text.TextViewer)
	 */
	protected void configureTextViewer(TextViewer textViewer) {
		super.configureTextViewer(textViewer);
		
		// hack: gain access to text viewers
		switch (fIndex++) {
		case 0:
			fTopViewer= textViewer;
			break;
		case 1:
			fLeftViewer= textViewer;
			break;
		case 2:
			fRightViewer= textViewer;
		}
	}
	
	/*
	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#createTokenComparator(java.lang.String)
	 */
	protected ITokenComparator createTokenComparator(String line) {
//		boolean before= fIndex++ % 2 != 0;
//		final IMacroExpansionStep step;
//		if (fStepIndex < fInput.fExplorer.getExpansionStepCount()) {
//			step= fInput.fExplorer.getExpansionStep(fStepIndex);
//		} else {
//			before= !before;
//			step= fInput.fExplorer.getFullExpansion();
//		}
//		return new MacroExpansionComparator(line, step.getReplacements(), before);
		return new NullTokenComparator();
	}

	public void setMacroExpansionInput(CMacroExpansionInput input) {
		fInput= input;
	}

	/*
	 * @see org.eclipse.jface.viewers.ContentViewer#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		fLeftViewer.setRedraw(false);
		fRightViewer.setRedraw(false);

		final ReplaceEdit[] edits;
		try {
			if (fStepIndex < fInput.fExplorer.getExpansionStepCount()) {
				final IMacroExpansionStep step;
				step= fInput.fExplorer.getExpansionStep(fStepIndex);
				edits= step.getReplacements();
			} else {
				edits= new ReplaceEdit[0];
			}
			fLeftHighlighter.setReplaceEdits(edits);
			fRightHighlighter.setReplaceEdits(edits);
	
			super.setInput(input);
		} finally {
			fLeftViewer.setRedraw(true);
			fRightViewer.setRedraw(true);
		}
		if (edits.length > 0) {
			fLeftViewer.revealRange(edits[0].getOffset(), edits[0].getLength());
			fRightViewer.revealRange(edits[0].getOffset(), edits[0].getText().length());
		}
	}

	public void setMacroExpansionStep(int index) {
		fStepIndex= index;
	}

}
