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

		public void setReplaceEdits(int prefixLength, ReplaceEdit[] edits) {
			fStarts= new int[edits.length];
			fLengths= new int[edits.length];
			int delta= 0;
			for (int i= 0; i < edits.length; i++) {
				ReplaceEdit edit= edits[i];
				fStarts[i]= prefixLength + edit.getOffset() + delta;
				fLengths[i]= fBefore ? edit.getLength() : edit.getText().length();
				if (!fBefore) {
					delta += edit.getText().length() - edit.getLength();
				}
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

	private final ReplaceEditsHighlighter fLeftHighlighter;
	private final ReplaceEditsHighlighter fRightHighlighter;
	private Color fChangeBackground;

	private TextViewer fLeftViewer;
	private TextViewer fRightViewer;
	private TextViewer fTopViewer;
	
	private int fViewerIndex;

	private CMacroExpansionInput fInput;
	private int fStepIndex;
	private int fPrefixLength;
	
	public CMacroCompareViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
		Font font= JFaceResources.getFont(CMergeViewer.class.getName());
		fLeftViewer.getTextWidget().setFont(font);
		fRightViewer.getTextWidget().setFont(font);
		fTopViewer.getTextWidget().setFont(font);
		
		fChangeBackground= new Color(parent.getDisplay(), CHANGE_COLOR);
		fLeftViewer.addTextPresentationListener(fLeftHighlighter= new ReplaceEditsHighlighter(fChangeBackground, true));
		fRightViewer.addTextPresentationListener(fRightHighlighter= new ReplaceEditsHighlighter(fChangeBackground, false));
		fViewerIndex= 0;
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
		switch (fViewerIndex++) {
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
		return new NullTokenComparator();
	}

	/**
	 * Set the macro expansion input.
	 * 
	 * @param input
	 */
	public void setMacroExpansionInput(CMacroExpansionInput input) {
		fInput= input;
		fPrefixLength= fInput.getPrefix().length();
	}

	/*
	 * @see org.eclipse.jface.viewers.ContentViewer#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		fLeftViewer.setRedraw(false);
		fRightViewer.setRedraw(false);

		final ReplaceEdit[] edits;
		
		try {
			final IMacroExpansionStep step;
			if (fStepIndex < fInput.fExplorer.getExpansionStepCount()) {
				step= fInput.fExplorer.getExpansionStep(fStepIndex);
			} else {
				step= fInput.fExplorer.getFullExpansion();
			}
			edits= step.getReplacements();

			fLeftHighlighter.setReplaceEdits(fPrefixLength, edits);
			fRightHighlighter.setReplaceEdits(fPrefixLength, edits);
	
			super.setInput(input);
			
		} finally {
			fLeftViewer.setRedraw(true);
			fRightViewer.setRedraw(true);
		}
		if (edits.length > 0) {
			final int firstDiffOffset= fPrefixLength + edits[0].getOffset();
			fLeftViewer.revealRange(firstDiffOffset, edits[0].getLength());
			fRightViewer.revealRange(firstDiffOffset, edits[0].getText().length());
		}
	}

	public void setMacroExpansionStep(int index) {
		fStepIndex= index;
	}

}
