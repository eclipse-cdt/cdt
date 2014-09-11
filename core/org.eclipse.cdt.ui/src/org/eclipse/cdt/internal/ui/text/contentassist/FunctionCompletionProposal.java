/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Eicher <eclipse@tom.eicher.name> - [content assist] prefix complete casted method proposals - https://bugs.eclipse.org/bugs/show_bug.cgi?id=247547
 *     Mentor Graphics (Mohamed Azab) - added the API to CDT and made the necessary changes
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * This is a modified version of org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal
 * 
 * This class adds a linked mode function compilation proposal with
 * exit policy.
 */
public class FunctionCompletionProposal extends CCompletionProposal {
	private boolean fHasParametersComputed= false;
	private boolean fHasParameters;
	protected IFunction fFunction;
	protected CContentAssistInvocationContext fContext;

	public FunctionCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			Image image, String displayString, String idString, int relevance, ITextViewer viewer, IFunction function, CContentAssistInvocationContext context) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, idString, relevance,
				viewer);
		fFunction = function;
		fContext = context;
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		if (trigger == ' ' || trigger == '(')
			trigger= '\0';
		super.apply(document, trigger, offset);
		if (hasParameters()) {
			setUpLinkedMode(document, ')');
		} else if (getReplacementString().endsWith(";")) { //$NON-NLS-1$
			setUpLinkedMode(document, ';');
		}
	}

	/**
	 * @return <code>true</code> if the method has any parameters, <code>false</code> if it has
	 *         no parameters
	 */
	protected final boolean hasParameters() {
		if (!fHasParametersComputed) {
			fHasParametersComputed = true;
			fHasParameters = computeHasParameters();
		}
		return fHasParameters;
	}

	private boolean computeHasParameters() {
		return (fFunction.getParameters().length != 0);
	}

	protected static class ExitPolicy implements IExitPolicy {

		final char fExitCharacter;

		public ExitPolicy(char exitCharacter) {
			fExitCharacter = exitCharacter;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.eclipse.jdt.internal.ui.text.link.LinkedPositionManager, org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		@Override
		public ExitFlags doExit(LinkedModeModel environment, VerifyEvent event, int offset, int length) {

			if (event.character == fExitCharacter) {
				if (environment.anyPositionContains(offset))
					return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
				else
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

	protected void setUpLinkedMode(IDocument document, char closingCharacter) {
		if (fTextViewer != null) {
			int offset = fContext.getInvocationOffset();
			int exit= getReplacementOffset() + getReplacementString().length();
			try {
				LinkedPositionGroup group= new LinkedPositionGroup();
				group.addPosition(new LinkedPosition(document, offset, 0, LinkedPositionGroup.NO_STOP));

				LinkedModeModel model= new LinkedModeModel();
				model.addGroup(group);
				model.forceInstall();

				LinkedModeUI ui= new EditorLinkedModeUI(model, fTextViewer);
				ui.setSimpleMode(true);
				ui.setExitPolicy(new ExitPolicy(closingCharacter));
				ui.setExitPosition(fTextViewer, exit, 0, Integer.MAX_VALUE);
				ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
				ui.enter();
			} catch (BadLocationException x) {
				CUIPlugin.log(x);
			}
		}
	}
}
