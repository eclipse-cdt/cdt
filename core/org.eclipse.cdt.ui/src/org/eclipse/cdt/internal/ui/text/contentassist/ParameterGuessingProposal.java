/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Andrew McCullough - initial API and implementation
 *	   IBM Corporation  - general improvement and bug fixes, partial reimplementation
 *     Mohamed Azab (Mentor Graphics) - Bug 438549. Add mechanism for parameter guessing.
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.EditorHighlightingSynchronizer;

/**
 * This class is based on org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal
 * 
 * Extents the basic Function Compilation Proposal to add a linked mode for each of the function parameters
 * with a list of suggestions for each parameter.
 */
public class ParameterGuessingProposal extends FunctionCompletionProposal {
	private ICompletionProposal[][] fChoices; // initialized by guessParameters()
	private Position[] fPositions; // initialized by guessParameters()
	private boolean fReplacementStringComputed;
	private IRegion fSelectedRegion; // initialized by apply()
	private IPositionUpdater fUpdater;
	private String fFullPrefix; // The string from the start of the statement to the invocation offset.
	private char[][] fParametersNames;
	private IType[] fParametersTypes;
	private ArrayList<IBinding> fAssignableElemebts;

	public static ParameterGuessingProposal createProposal(CContentAssistInvocationContext context,
			ArrayList<IBinding> availableElements, CCompletionProposal proposal, IFunction function,
			String prefix) {
		String replacement = getParametersList(function);
		String fullPrefix = function.getName() + "("; //$NON-NLS-1$
		int replacementOffset = proposal.getReplacementOffset();
		int replacementLength = 0;
		int invocationOffset = context.getInvocationOffset();
		int parseOffset = context.getParseOffset();
		IASTTranslationUnit translationUnit = context.getCompletionNode().getTranslationUnit();
		IDocument document = context.getDocument();
		/*
		 * Adjust the replacement offset, the replacement string and the replacement length for the case of
		 * invoking after '('. - The replacement offset will be calculated to point to the start of the
		 * function call statement, as in that case the proposal.getReplacementOffset() doesn't point to that.
		 * - The replacement string will contain the in-editor prefix instead of the function name only, to
		 * handle the case of C++ function templates. - The length will be updated after changing the
		 * replacement string.
		 */
		if (isInsideBracket(invocationOffset, parseOffset)) {
			replacementOffset = parseOffset - prefix.length();
			try {
				fullPrefix = document.get(replacementOffset, invocationOffset - replacementOffset);
				replacement = fullPrefix + replacement + ")"; //$NON-NLS-1$
			} catch (BadLocationException e1) {
			}
			try {
				// remove ')' from the replacement string if it is auto appended.
				if (document.getChar(invocationOffset) == ')')
					replacement = replacement.substring(0, replacement.length() - 1);
			} catch (BadLocationException e) {
			}
		} else {
			replacement = fullPrefix + replacement + ")"; //$NON-NLS-1$
			replacementOffset = proposal.getReplacementOffset();
		}
		replacementLength = replacement.length();
		ParameterGuessingProposal ret = new ParameterGuessingProposal(replacement, replacementOffset,
				replacementLength, proposal.getImage(), proposal.getDisplayString(), proposal.getIdString(),
				proposal.getRelevance(), context.getViewer(), function, invocationOffset, parseOffset,
				translationUnit, document);
		ret.setContextInformation(proposal.getContextInformation());
		ret.fFullPrefix = fullPrefix;
		/*
		 * Get all defined elements before the start of the statement. ex: int a = foo( ^ --> We don't want
		 * 'a' as a suggestion. ex2: char* foo(int a, int b) {return NULL;} void bar(char* name){} ...
		 * bar(foo( ^ --> If this offset is used, the only defined name will be "bar(char*)".
		 */
		ret.fAssignableElemebts = availableElements;
		return ret;
	}

	/**
	 * Returns a comma-separated list of parameters
	 */
	private static String getParametersList(IFunction method) {
		StringBuilder params = new StringBuilder();
		for (IParameter param : method.getParameters()) {
			if (params.length() != 0)
				params.append(", "); //$NON-NLS-1$
			params.append(param.getName());
		}
		return params.toString();
	}

	public ParameterGuessingProposal(String replacementString, int replacementOffset, int replacementLength,
			Image image, String displayString, String idString, int relevance, ITextViewer viewer,
			IFunction function, int invocationOffset, int parseOffset, IASTTranslationUnit translationUnit,
			IDocument document) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, idString,
				relevance, viewer, function, invocationOffset, parseOffset, translationUnit, document);
		fParametersNames = getFunctionParametersNames(fFunctionParameters);
		fParametersTypes = getFunctionParametersTypes(fFunctionParameters);
	}

	/**
	 * Checks if the invocation of content assist was after open bracket.
	 */
	private static boolean isInsideBracket(int invocationOffset, int parseOffset) {
		return invocationOffset - parseOffset != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		if (isInsideBracket(fInvocationOffset, fParseOffset)) {
			try {
				return fDocument.get(getReplacementOffset(), fInvocationOffset - getReplacementOffset());
			} catch (BadLocationException e) {
			}
		}
		return super.getPrefixCompletionText(document, completionOffset);
	}

	@Override
	public void apply(final IDocument document, char trigger, int offset) {
		try {
			super.apply(document, trigger, offset);

			int baseOffset = getReplacementOffset();
			String replacement = getReplacementString();

			if (fPositions != null && fTextViewer != null) {
				LinkedModeModel model = new LinkedModeModel();

				for (int i = 0; i < fPositions.length; i++) {
					LinkedPositionGroup group = new LinkedPositionGroup();
					int positionOffset = fPositions[i].getOffset();
					int positionLength = fPositions[i].getLength();

					if (fChoices[i].length < 2) {
						group.addPosition(new LinkedPosition(document, positionOffset, positionLength,
								LinkedPositionGroup.NO_STOP));
					} else {
						ensurePositionCategoryInstalled(document, model);
						document.addPosition(getCategory(), fPositions[i]);
						group.addPosition(new ProposalPosition(document, positionOffset, positionLength,
								LinkedPositionGroup.NO_STOP, fChoices[i]));
					}
					model.addGroup(group);
				}

				model.forceInstall();
				CEditor editor = getCEditor();
				if (editor != null) {
					model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
				}

				LinkedModeUI ui = new EditorLinkedModeUI(model, fTextViewer);
				ui.setExitPosition(fTextViewer, baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
				// exit character can be either ')' or ';'
				final char exitChar = replacement.charAt(replacement.length() - 1);
				ui.setExitPolicy(new ExitPolicy(exitChar) {
					@Override
					public ExitFlags doExit(LinkedModeModel model2, VerifyEvent event, int offset2, int length) {
						if (event.character == ',') {
							for (int i = 0; i < fPositions.length - 1; i++) { // not for the last one
								Position position = fPositions[i];
								if (position.offset <= offset2
										&& offset2 + length <= position.offset + position.length) {
									event.character = '\t';
									event.keyCode = SWT.TAB;
									return null;
								}
							}
						} else if (event.character == ')' && exitChar != ')') {
							// exit from link mode when user is in the last ')' position.
							Position position = fPositions[fPositions.length - 1];
							if (position.offset <= offset2
									&& offset2 + length <= position.offset + position.length) {
								return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
							}
						}
						return super.doExit(model2, event, offset2, length);
					}
				});
				ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
				ui.setDoContextInfo(true);
				ui.enter();
				fSelectedRegion = ui.getSelectedRegion();
			} else {
				fSelectedRegion = new Region(baseOffset + replacement.length(), 0);
			}

		} catch (BadLocationException | BadPositionCategoryException e) {
			ensurePositionCategoryRemoved(document);
			CUIPlugin.log(e);
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		if (fSelectedRegion == null)
			return new Point(getReplacementOffset(), 0);

		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	@Override
	public String getReplacementString() {
		if (!fReplacementStringComputed) {
			String rep = computeReplacementString();
			setReplacementString(rep);
			setReplacementLength(rep.length());
			fReplacementStringComputed = true;
		}
		return super.getReplacementString();
	}

	private String computeReplacementString() {
		if (!hasParameters())
			return super.getReplacementString();

		String replacement;
		try {
			replacement = computeGuessingCompletion();
		} catch (Exception x) {
			fPositions = null;
			fChoices = null;
			CUIPlugin.log(x);
			return super.getReplacementString();
		}

		return replacement;
	}

	private String computeGuessingCompletion() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append(fFullPrefix);
		setCursorPosition(buffer.length());

		for (int i = 0; i < fFunctionParameters.length; i++) {
			fParametersNames[i] = fFunctionParameters[i].getNameCharArray();
		}

		fChoices = guessParameters(fParametersNames);
		int count = fChoices.length;
		int replacementOffset = getReplacementOffset();

		for (int i = 0; i < count; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$

			ICompletionProposal proposal = fChoices[i][0];
			String argument = proposal.getDisplayString();

			Position position = fPositions[i];
			position.setOffset(replacementOffset + buffer.length());
			position.setLength(argument.length());

			if (proposal instanceof CCompletionProposal) // handle the "unknown" case where we only insert a
															// proposal.
				((CCompletionProposal) proposal).setReplacementOffset(replacementOffset + buffer.length());
			buffer.append(argument);
		}

		buffer.append(")"); //$NON-NLS-1$

		return buffer.toString();
	}

	private ICompletionProposal[][] guessParameters(char[][] parameterNames) throws Exception {
		int count = parameterNames.length;
		fPositions = new Position[count];
		fChoices = new ICompletionProposal[count][];

		ParameterGuesser guesser = new ParameterGuesser(fTranslationUnit);

		for (int i = 0; i < count; i++) {
			String paramName = new String(parameterNames[i]);
			Position position = new Position(0, 0);

			boolean isLastParameter = i == count - 1;
			ArrayList<ICompletionProposal> allProposals = new ArrayList<ICompletionProposal>();
			ICompletionProposal[] argumentProposals = guesser.parameterProposals(fParametersTypes[i],
					paramName, position, fAssignableElemebts, true, isLastParameter);
			allProposals.addAll(Arrays.asList(argumentProposals));
			fPositions[i] = position;
			fChoices[i] = argumentProposals;
		}

		return fChoices;
	}

	private static IType[] getFunctionParametersTypes(IParameter[] functionParameters) {
		IType[] ret = new IType[functionParameters.length];
		for (int i = 0; i < functionParameters.length; i++) {
			ret[i] = functionParameters[i].getType();
		}
		return ret;
	}

	private static char[][] getFunctionParametersNames(IParameter[] functionParameters) {
		char[][] parameterNames = new char[functionParameters.length][];
		for (int i = 0; i < functionParameters.length; i++) {
			parameterNames[i] = functionParameters[i].getNameCharArray();
		}
		return parameterNames;
	}

	private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
		if (!document.containsPositionCategory(getCategory())) {
			document.addPositionCategory(getCategory());
			fUpdater = new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(fUpdater);

			model.addLinkingListener(new ILinkedModeListener() {
				@Override
				public void left(LinkedModeModel environment, int flags) {
					ensurePositionCategoryRemoved(document);
				}

				@Override
				public void suspend(LinkedModeModel environment) {
				}

				@Override
				public void resume(LinkedModeModel environment, int flags) {
				}
			});
		}
	}

	private void ensurePositionCategoryRemoved(IDocument document) {
		if (document.containsPositionCategory(getCategory())) {
			try {
				document.removePositionCategory(getCategory());
			} catch (BadPositionCategoryException e) {
				// ignore
			}
			document.removePositionUpdater(fUpdater);
		}
	}

	private String getCategory() {
		return "ParameterGuessingProposal_" + toString(); //$NON-NLS-1$
	}

	/**
	 * Returns the currently active C/C++ editor, or <code>null</code> if it cannot be determined.
	 */
	private static CEditor getCEditor() {
		IEditorPart part = CUIPlugin.getActivePage().getActiveEditor();
		if (part instanceof CEditor) {
			return (CEditor) part;
		} else {
			return null;
		}
	}

	/**
	 * Returns the guesses for each parameter
	 */
	public ICompletionProposal[][] getParametersGuesses() {
		return fChoices;
	}
}
