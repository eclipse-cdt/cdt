/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mohamed Azab (Mentor Graphics) - Bug 438549. Add mechanism for parameter guessing.
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * An enhanced implementation of the <code>ICompletionProposal</code> interface implementing all the extension interfaces.
 * It uses a position to track its replacement offset and length. The position must be set up externally.
 */
public class PositionBasedCompletionProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2 {
	/** The string to be displayed in the completion proposal popup */
	private String fDisplayString;
	/** The replacement string */
	private String fReplacementString;
	/** The replacement position. */
	private Position fReplacementPosition;
	/** The cursor position after this proposal has been applied */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup */
	private Image fImage;
	/** The context information of this proposal */
	private IContextInformation fContextInformation;
	/** The additional info of this proposal */
	private String fAdditionalProposalInfo;
	/** The trigger characters */
	private char[] fTriggerCharacters;

	/**
	 * Creates a new completion proposal based on the provided information.  The replacement string is
	 * considered being the display string too. All remaining fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementPosition the position of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 */
	public PositionBasedCompletionProposal(String replacementString, Position replacementPosition, int cursorPosition) {
		this(replacementString, replacementPosition, cursorPosition, null, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementPosition the position of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * @param contextInformation the context information associated with this proposal
	 * @param additionalProposalInfo the additional information associated with this proposal
	 * @param triggers the trigger characters
	 */
	public PositionBasedCompletionProposal(String replacementString, Position replacementPosition, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo, char[] triggers) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementPosition != null);

		fReplacementString= replacementString;
		fReplacementPosition= replacementPosition;
		fCursorPosition= cursorPosition;
		fImage= image;
		fDisplayString= displayString;
		fContextInformation= contextInformation;
		fAdditionalProposalInfo= additionalProposalInfo;
		fTriggerCharacters= triggers;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementPosition.getOffset(), fReplacementPosition.getLength(), fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementPosition.getOffset() + fCursorPosition, 0);
	}

	@Override
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public String getDisplayString() {
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}

	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		apply(viewer.getDocument());
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {
	}

	@Override
	public void unselected(ITextViewer viewer) {
	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		try {
			String content= document.get(fReplacementPosition.getOffset(), offset - fReplacementPosition.getOffset());
			if (fReplacementString.startsWith(content))
				return true;
		} catch (BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		// not called any more
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		// not called any more
		return false;
	}

	@Override
	public char[] getTriggerCharacters() {
		return fTriggerCharacters;
	}

	@Override
	public int getContextInformationPosition() {
		return fReplacementPosition.getOffset();
	}
}

