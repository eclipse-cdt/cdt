/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.ui.text.contentassist;


import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class CCompletionProposal implements ICCompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3 {
	
	private String fDisplayString;
	private String fReplacementString;
	private int fReplacementOffset;
	private int fReplacementLength;
	private int fCursorPosition;
	private Image fImage;
	private IContextInformation fContextInformation;
	private int fContextInformationPosition;
	private String fProposalInfo;
	private char[] fTriggerCharacters;
	protected boolean fToggleEating;
	protected ITextViewer fTextViewer;	
	
	private int fRelevance;
	private StyleRange fRememberedStyleRange;

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * If set to <code>null</code>, the replacement string will be taken as display string.
	 */
	public CCompletionProposal(String replacementString, int replacementOffset, int replacementLength, Image image, String displayString, int relevance) {
		this(replacementString, replacementOffset, replacementLength, image, displayString, relevance, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * @param viewer the text viewer for which this proposal is computed, may be <code>null</code>
	 * If set to <code>null</code>, the replacement string will be taken as display string.
	 */
	public CCompletionProposal(String replacementString, int replacementOffset, int replacementLength, Image image, String displayString, int relevance, ITextViewer viewer) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
				
		fReplacementString= replacementString;
		fReplacementOffset= replacementOffset;
		fReplacementLength= replacementLength;
		fImage= image;
		fDisplayString= displayString != null ? displayString : replacementString;
		fRelevance= relevance;
		fTextViewer= viewer;

		fCursorPosition= replacementString.length();
	
		fContextInformation= null;
		fContextInformationPosition= -1;
		fTriggerCharacters= null;
		fProposalInfo= null;
	}
	
	/**
	 * Sets the context information.
	 * @param contextInformation The context information associated with this proposal
	 */
	public void setContextInformation(IContextInformation contextInformation) {
		fContextInformation= contextInformation;
		fContextInformationPosition= (fContextInformation != null ? fCursorPosition : -1);
	}
	
	/**
	 * Sets the trigger characters.
	 * @param triggerCharacters The set of characters which can trigger the application of this completion proposal
	 */
	public void setTriggerCharacters(char[] triggerCharacters) {
		fTriggerCharacters= triggerCharacters;
	}
	
	/**
	 * Sets the proposal info.
	 * @param proposalInfo The additional information associated with this proposal or <code>null</code>
	 */
	public void setAdditionalProposalInfo(String proposalInfo) {
		fProposalInfo= proposalInfo;
	}
	
	/**
	 * Sets the cursor position relative to the insertion offset. By default this is the length of the completion string
	 * (Cursor positioned after the completion)
	 * @param cursorPosition The cursorPosition to set
	 */
	public void setCursorPosition(int cursorPosition) {
		Assert.isTrue(cursorPosition >= 0);
		fCursorPosition= cursorPosition;
		fContextInformationPosition= (fContextInformation != null ? fCursorPosition : -1);
	}	
	
	/*
	 * @see ICompletionProposalExtension#apply(IDocument, char, int)
	 */
	public void apply(IDocument document, char trigger, int offset) {
		try {
			// patch replacement length
			int delta= offset - (fReplacementOffset + fReplacementLength);
			if (delta > 0)
				fReplacementLength += delta;
			
			String string;
			if (trigger == (char) 0) {
				string= fReplacementString;
			} else {
				StringBuffer buffer= new StringBuffer(fReplacementString);

				// fix for PR #5533. Assumes that no eating takes place.
				if ((fCursorPosition > 0 && fCursorPosition <= buffer.length() && buffer.charAt(fCursorPosition - 1) != trigger)) {
					buffer.insert(fCursorPosition, trigger);
					++fCursorPosition;
				}
				
				string= buffer.toString();
			}

			// reference position just at the end of the document change.
			int referenceOffset= fReplacementOffset + fReplacementLength;
			final ReferenceTracker referenceTracker= new ReferenceTracker();
			referenceTracker.preReplace(document, referenceOffset);

			replace(document, fReplacementOffset, fReplacementLength, string);

			referenceOffset= referenceTracker.postReplace(document);			
			fReplacementOffset= referenceOffset - (string == null ? 0 : string.length());

			if (fTextViewer != null && string != null) {
				int index= string.indexOf("()"); //$NON-NLS-1$
				if (index != -1 && index + 1 == fCursorPosition) {
					IPreferenceStore preferenceStore= CUIPlugin.getDefault().getPreferenceStore();
//					if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACKETS)) {
					if(true){
						int newOffset= fReplacementOffset + fCursorPosition;
						
						LinkedPositionGroup group= new LinkedPositionGroup();
						group.addPosition(new LinkedPosition(document, newOffset, 0, LinkedPositionGroup.NO_STOP));
						
						LinkedModeModel model= new LinkedModeModel();
						model.addGroup(group);
						model.forceInstall();
						
						LinkedModeUI ui= new EditorLinkedModeUI(model, fTextViewer);
						ui.setSimpleMode(true);
						ui.setExitPolicy(new ExitPolicy(')'));
						ui.setExitPosition(fTextViewer, newOffset + 1, 0, Integer.MAX_VALUE);
						ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
						ui.enter();
					}
				}
			}

		} catch (BadLocationException x) {
			// ignore
		}		
	}

	/**
	 * A class to simplify tracking a reference position in a document. 
	 */
	private static final class ReferenceTracker {

		/** The reference position category name. */
		private static final String CATEGORY= "reference_position"; //$NON-NLS-1$
		/** The position updater of the reference position. */
		private final IPositionUpdater fPositionUpdater= new DefaultPositionUpdater(CATEGORY);
		/** The reference position. */
		private final Position fPosition= new Position(0);
		
		/**
		 * Called before document changes occur. It must be followed by a call to postReplace().
		 * 
		 * @param document the document on which to track the reference position.
		 *	
		 */
		public void preReplace(IDocument document, int offset) throws BadLocationException {
			fPosition.setOffset(offset);
			try {
				document.addPositionCategory(CATEGORY);
				document.addPositionUpdater(fPositionUpdater);
				document.addPosition(CATEGORY, fPosition);

			} catch (BadPositionCategoryException e) {
				// should not happen
				CUIPlugin.getDefault().log(e);
			}
		}
	
		/**
		 * Called after the document changed occured. It must be preceded by a call to preReplace().
		 * 
		 * @param document the document on which to track the reference position.
		 */
		public int postReplace(IDocument document) {
			try {
				document.removePosition(CATEGORY, fPosition);
				document.removePositionUpdater(fPositionUpdater);
				document.removePositionCategory(CATEGORY);
				 
			} catch (BadPositionCategoryException e) {
				// should not happen
				CUIPlugin.getDefault().log(e);
			}
			return fPosition.getOffset();
		}
	}	
	
	protected static class ExitPolicy implements IExitPolicy {
		
		final char fExitCharacter;
		
		public ExitPolicy(char exitCharacter) {
			fExitCharacter= exitCharacter;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.eclipse.jdt.internal.ui.text.link.LinkedPositionManager, org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		public ExitFlags doExit(LinkedModeModel environment, VerifyEvent event, int offset, int length) {
			
			if (event.character == fExitCharacter) {
				if (environment.anyPositionContains(offset))
					return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
				
				return new ExitFlags(ILinkedModeListener.UPDATE_CARET, true);
			}	
			
			switch (event.character) {			
			case ';':
				return new ExitFlags(ILinkedModeListener.NONE, true);
								
			default:
				return null;
			}						
		}

	}	
	
	// #6410 - File unchanged but dirtied by code assist
	private void replace(IDocument document, int offset, int length, String string) throws BadLocationException {
		if (!document.get(offset, length).equals(string))
			document.replace(offset, length, string);
	}

	/*
	 * @see ICompletionProposal#apply
	 */
	public void apply(IDocument document) {
		apply(document, (char) 0, fReplacementOffset + fReplacementLength);
	}
	
	/*
	 * @see ICompletionProposal#getSelection
	 */
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return fDisplayString;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		if (fProposalInfo != null) {
			return fProposalInfo;
		}
		return null;
	}
	
	/*
	 * @see ICompletionProposalExtension#getTriggerCharacters()
	 */
	public char[] getTriggerCharacters() {
		return fTriggerCharacters;
	}

	/*
	 * @see ICompletionProposalExtension#getContextInformationPosition()
	 */
	public int getContextInformationPosition() {
		return fReplacementOffset + fContextInformationPosition;
	}
	
	/**
	 * Gets the replacement offset.
	 * @return Returns a int
	 */
	public int getReplacementOffset() {
		return fReplacementOffset;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getCompletionOffset()
	 */
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return getReplacementOffset();
	}

	/**
	 * Sets the replacement offset.
	 * @param replacementOffset The replacement offset to set
	 */
	public void setReplacementOffset(int replacementOffset) {
		Assert.isTrue(replacementOffset >= 0);
		fReplacementOffset= replacementOffset;
	}	

	/**
	 * Gets the replacement length.
	 * @return Returns a int
	 */
	public int getReplacementLength() {
		return fReplacementLength;
	}

	/**
	 * Sets the replacement length.
	 * @param replacementLength The replacementLength to set
	 */
	public void setReplacementLength(int replacementLength) {
		Assert.isTrue(replacementLength >= 0);
		fReplacementLength= replacementLength;
	}

	/**
	 * Gets the replacement string.
	 * @return Returns a String
	 */
	public String getReplacementString() {
		return fReplacementString;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementText()
	 */
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		String string= getReplacementString();
		int pos= string.indexOf('(');
		if (pos > 0)
			return string.subSequence(0, pos);
		return string;
	}

	/**
	 * Sets the replacement string.
	 * @param replacementString The replacement string to set
	 */
	public void setReplacementString(String replacementString) {
		fReplacementString= replacementString;
	}

	/**
	 * Sets the image.
	 * @param image The image to set
	 */
	public void setImage(Image image) {
		fImage= image;
	}

	/*
	 * @see ICompletionProposalExtension#isValidFor(IDocument, int)
	 */
	public boolean isValidFor(IDocument document, int offset) {
		return validate(document, offset, null);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
	 */
	public boolean validate(IDocument document, int offset, DocumentEvent event) {

		if (offset < fReplacementOffset)
			return false;
				
		/* 
		 * See http://dev.eclipse.org/bugs/show_bug.cgi?id=17667
		String word= fReplacementString;
		 */ 
		boolean validated= startsWith(document, offset, fDisplayString);	

		if (validated && event != null) {
			// adapt replacement range to document change
			int delta= (event.fText == null ? 0 : event.fText.length()) - event.fLength;
			fReplacementLength += delta;	
		}

		return validated;
	}
	
	/**
	 * Gets the proposal's relevance.
	 * @return Returns a int
	 */
	public int getRelevance() {
		return fRelevance;
	}

	/**
	 * Sets the proposal's relevance.
	 * @param relevance The relevance to set
	 */
	public void setRelevance(int relevance) {
		fRelevance= relevance;
	}

	/**
	 * Returns <code>true</code> if a words starts with the code completion prefix in the document,
	 * <code>false</code> otherwise.
	 */	
	protected boolean startsWith(IDocument document, int offset, String word) {
		int wordLength= word == null ? 0 : word.length();
		if (offset >  fReplacementOffset + wordLength)
			return false;
		
		try {
			int length= offset - fReplacementOffset;
			String start= document.get(fReplacementOffset, length);
			return word.substring(0, length).equalsIgnoreCase(start);
		} catch (BadLocationException x) {
		}
		
		return false;	
	}	

	private static boolean insertCompletion() {
		IPreferenceStore preference= CUIPlugin.getDefault().getPreferenceStore();
		return preference.getBoolean(ContentAssistPreference.AUTOINSERT);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension1#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
	 */
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

		IDocument document= viewer.getDocument();

		// don't eat if not in preferences, XOR with modifier key 1 (Ctrl)
		// but: if there is a selection, replace it!
		Point selection= viewer.getSelectedRange();
		fToggleEating= (stateMask & SWT.MOD1) != 0;
		if (insertCompletion() ^ fToggleEating)
			fReplacementLength= selection.x + selection.y - fReplacementOffset;
		
		apply(document, trigger, offset);
		fToggleEating= false;
	}

	private static Color getForegroundColor(StyledText text) {

		IPreferenceStore preference= CUIPlugin.getDefault().getPreferenceStore();
		RGB rgb= PreferenceConverter.getColor(preference, ContentAssistPreference.PROPOSALS_FOREGROUND);
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		return textTools.getColorManager().getColor(rgb);
	}

	private static Color getBackgroundColor(StyledText text) {

		IPreferenceStore preference= CUIPlugin.getDefault().getPreferenceStore();
		RGB rgb= PreferenceConverter.getColor(preference, ContentAssistPreference.PROPOSALS_BACKGROUND);
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		return textTools.getColorManager().getColor(rgb);
	}
	
	private void repairPresentation(ITextViewer viewer) {
		if (fRememberedStyleRange != null) {
			 if (viewer instanceof ITextViewerExtension2) {
			 	// attempts to reduce the redraw area
			 	ITextViewerExtension2 viewer2= (ITextViewerExtension2) viewer;
			 	
			 	if (viewer instanceof ITextViewerExtension5) {
			 		
			 		ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			 		IRegion widgetRange= extension.modelRange2WidgetRange(new Region(fRememberedStyleRange.start, fRememberedStyleRange.length));
			 		if (widgetRange != null)
			 			viewer2.invalidateTextPresentation(widgetRange.getOffset(), widgetRange.getLength());
			 			
			 	} else {
					viewer2.invalidateTextPresentation(fRememberedStyleRange.start + viewer.getVisibleRegion().getOffset(), fRememberedStyleRange.length);
			 	}
			 	
			} else
				viewer.invalidateTextPresentation();
		}
	}

	private void updateStyle(ITextViewer viewer) {

		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		int widgetCaret= text.getCaretOffset();
		
		int modelCaret= 0;
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			modelCaret= extension.widgetOffset2ModelOffset(widgetCaret);
		} else {
			IRegion visibleRegion= viewer.getVisibleRegion();
			modelCaret= widgetCaret + visibleRegion.getOffset();			
		}
		
		if (modelCaret >= fReplacementOffset + fReplacementLength) {
			repairPresentation(viewer);
			return;
		}
					
		int offset= widgetCaret;
		int length= fReplacementOffset + fReplacementLength - modelCaret;
	
		Color foreground= getForegroundColor(text);
		Color background= getBackgroundColor(text);

		StyleRange range= text.getStyleRangeAtOffset(offset);
		int fontStyle= range != null ? range.fontStyle : SWT.NORMAL;

		repairPresentation(viewer);
		fRememberedStyleRange= new StyleRange(offset, length, foreground, background, fontStyle);
		
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=34754
		try {
			text.setStyleRange(fRememberedStyleRange);
		} catch (IllegalArgumentException x) {
			// catching exception as offset + length might be outside of the text widget
			fRememberedStyleRange= null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(ITextViewer, boolean)
	 */
	public void selected(ITextViewer viewer, boolean smartToggle) {
		if (!insertCompletion() ^ smartToggle)
			updateStyle(viewer);
		else {
			repairPresentation(viewer);
			fRememberedStyleRange= null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(ITextViewer)
	 */
	public void unselected(ITextViewer viewer) {
		repairPresentation(viewer);
		fRememberedStyleRange= null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getInformationControlCreator()
	 */
	public IInformationControlCreator getInformationControlCreator() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateReplacementOffset(int newOffset) {
		setReplacementOffset(newOffset);
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateReplacementLength(int length) {
		setReplacementLength(length);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fDisplayString.hashCode() 
		+ fReplacementString.hashCode() 
		+ ((fContextInformation == null) ? 0 : fContextInformation.hashCode());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(!(other instanceof CCompletionProposal))
			return false;
		if(!(fDisplayString.equals(((CCompletionProposal)other).fDisplayString)))
			return false;
		if(!(fReplacementString.equals(((CCompletionProposal)other).fReplacementString)))
			return false;
		if((fContextInformation != null) && (((CCompletionProposal)other).fContextInformation != null) && (!(fContextInformation.equals(((CCompletionProposal)other).fContextInformation))))
			return false;
		
		return true;
	}


}
