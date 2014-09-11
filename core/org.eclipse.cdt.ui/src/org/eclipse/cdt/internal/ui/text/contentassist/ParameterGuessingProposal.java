/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Andrew McCullough - initial API and implementation
 *		IBM Corporation  - general improvement and bug fixes, partial reimplementation
 *		Mentor Graphics (Mohamed Azab) - added the API to CDT and made the necessary changes
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

import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.AccessContext;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.EditorHighlightingSynchronizer;

/**
 * This API layout is copied from org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal
 * 
 * Extents the basic Function Compilation Proposal to add a linked mode for each of
 * the function parameters with a list of suggestions for each parameter.
 */
public class ParameterGuessingProposal extends FunctionCompletionProposal {
	private ICompletionProposal[][] fChoices; // initialized by guessParameters()
	private Position[] fPositions; // initialized by guessParameters()
	private boolean fReplacementStringComputed = false;
	private IRegion fSelectedRegion; // initialized by apply()
	private IPositionUpdater fUpdater;
	private String fPrefix; // The string from the start of the statement to the parse offset.
	private String fFullPrefix; // The string from the start of the statement to the invocation offset.

	public static ParameterGuessingProposal createProposal(CContentAssistInvocationContext context, CCompletionProposal proposal, IFunction function, String prefix) {
		String replacement = getParametersList(function);
		String fullPrefix = function.getName() + "("; //$NON-NLS-1$
		int replacementOffset = proposal.getReplacementOffset();
		int replacementLength = 0;
		/*
		 * Adjust the replacement offset, the replacement string and the replacement length for the case of invoking
		 * after '('.
		 *   - The replacement offset will be calculated to point to the start of the function call statement, as
		 *     in that case the proposal.getReplacementOffset() doesn't point to that.
		 *   - The replacement string will contain the in-editor prefix instead of the function name only, to
		 *     handle the case of C++ function templates.
		 *   - The length will be updated after changing the replacement string.
		 */
		if (isInsideBracket(context)) {
			replacementOffset = context.getParseOffset() - prefix.length();
			try {
				fullPrefix = context.getDocument().get(replacementOffset, context.getInvocationOffset() - replacementOffset);
				replacement =  fullPrefix + replacement + ")"; //$NON-NLS-1$
			} catch (BadLocationException e1) {
			}
			try {
				// remove ')' from the replacement string if it is auto appended.
				if (context.getDocument().getChar(context.getInvocationOffset()) == ')')
					replacement = replacement.substring(0, replacement.length() - 1);
			} catch (BadLocationException e) {
			}
		} else {
			replacement = fullPrefix + replacement + ")"; //$NON-NLS-1$
			replacementOffset = proposal.getReplacementOffset();
		}
		replacementLength = replacement.length();
		
		ParameterGuessingProposal ret = new ParameterGuessingProposal(replacement, replacementOffset, replacementLength, proposal.getImage(), proposal.getDisplayString(), proposal.getIdString(), proposal.getRelevance(), context.getViewer(), function, context);
		ret.setContextInformation(proposal.getContextInformation());
		ret.fPrefix = prefix;
		ret.fFullPrefix = fullPrefix;
		return ret;
	}
	
	/**
	 * @return a comma-separated list of parameters
	 */
	private static String getParametersList(IFunction method) {
		StringBuilder params = new StringBuilder();
		boolean first = true;
		for (IParameter param : method.getParameters()) {
			if (first)
				first = false;
			else
				params.append(", "); //$NON-NLS-1$
			params.append(param.getName());
		}
		return params.toString();
	}

	public ParameterGuessingProposal(String replacementString, int replacementOffset, int replacementLength,
			Image image, String displayString, String idString, int relevance, ITextViewer viewer, IFunction function, CContentAssistInvocationContext context) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, idString, relevance,
				viewer, function, context);
	}
	
	// Check if the invocation of content assist was after open bracket.
	private static boolean isInsideBracket(CContentAssistInvocationContext context) {
		return context.getInvocationOffset() - context.getParseOffset() != 0;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementText()
	 * 
	 * Add special handling for the case of invocation after open bracket.
	 */
	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		if (isInsideBracket(fContext)) {
			try {
				return fContext.getDocument().get(getReplacementOffset(), fContext.getInvocationOffset() - getReplacementOffset());
			} catch (BadLocationException e) {
			}
		}
		return super.getPrefixCompletionText(document, completionOffset);
	}

	/**
	 * Copied from JDT
	 */
	@Override
	public void apply(final IDocument document, char trigger, int offset) {
		try {
			super.apply(document, trigger, offset);

			int baseOffset= getReplacementOffset();
			String replacement= getReplacementString();

			if (fPositions != null && fTextViewer != null) {

				LinkedModeModel model= new LinkedModeModel();

				for (int i= 0; i < fPositions.length; i++) {
					LinkedPositionGroup group= new LinkedPositionGroup();
					int positionOffset= fPositions[i].getOffset();
					int positionLength= fPositions[i].getLength();

					if (fChoices[i].length < 2) {
						group.addPosition(new LinkedPosition(document, positionOffset, positionLength, LinkedPositionGroup.NO_STOP));
					} else {
						ensurePositionCategoryInstalled(document, model);
						document.addPosition(getCategory(), fPositions[i]);
						group.addPosition(new ProposalPosition(document, positionOffset, positionLength, LinkedPositionGroup.NO_STOP, fChoices[i]));
					}
					model.addGroup(group);
				}

				model.forceInstall();
				CEditor editor= getCEditor();
				if (editor != null) {
					model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
				}

				LinkedModeUI ui= new EditorLinkedModeUI(model, fTextViewer);
				ui.setExitPosition(fTextViewer, baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
				// exit character can be either ')' or ';'
				final char exitChar= replacement.charAt(replacement.length() - 1);
				ui.setExitPolicy(new ExitPolicy(exitChar) {
					@Override
					public ExitFlags doExit(LinkedModeModel model2, VerifyEvent event, int offset2, int length) {
						if (event.character == ',') {
							for (int i= 0; i < fPositions.length - 1; i++) { // not for the last one
								Position position= fPositions[i];
								if (position.offset <= offset2 && offset2 + length <= position.offset + position.length) {
									event.character= '\t';
									event.keyCode= SWT.TAB;
									return null;
								}
							}
						} else if (event.character == ')' && exitChar != ')') {
							// exit from link mode when user is in the last ')' position.
							Position position= fPositions[fPositions.length - 1];
							if (position.offset <= offset2 && offset2 + length <= position.offset + position.length) {
								return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
							}
						}
						return super.doExit(model2, event, offset2, length);
					}
				});
				ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
				ui.setDoContextInfo(true);
				ui.enter();
				fSelectedRegion= ui.getSelectedRegion();

			} else {
				fSelectedRegion= new Region(baseOffset + replacement.length(), 0);
			}

		} catch (BadLocationException e) {
			ensurePositionCategoryRemoved(document);
			CUIPlugin.log(e);
		} catch (BadPositionCategoryException e) {
			ensurePositionCategoryRemoved(document);
			CUIPlugin.log(e);
		}
	}

	/**
	 * Copied from JDT
	 */
	@Override
	public Point getSelection(IDocument document) {
		if (fSelectedRegion == null)
			return new Point(getReplacementOffset(), 0);

		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	/**
	 * Copied from JDT
	 */
	@Override
	public String getReplacementString() {
		if (!fReplacementStringComputed) {
			String rep = computeReplacementString();
			setReplacementString(rep);
			setReplacementLength(rep.length());
		}
		return super.getReplacementString();
	}

	/**
	 * Copied from JDT
	 */
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

	/**
	 * Copied from JDT with replacing JDT types with CDT types
	 */
	private String computeGuessingCompletion() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append(fFullPrefix); //$NON-NLS-1$
		setCursorPosition(buffer.length());

		IParameter [] params = fFunction.getParameters();
		char[][] parameterNames = new char[params.length][];
		for (int i = 0; i < params.length; i++) {
			parameterNames[i] = params[i].getNameCharArray();
		}

		fChoices= guessParameters(parameterNames);
		int count= fChoices.length;
		int replacementOffset= getReplacementOffset();

		for (int i= 0; i < count; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$

			ICompletionProposal proposal= fChoices[i][0];
			String argument= proposal.getDisplayString();

			Position position= fPositions[i];
			position.setOffset(replacementOffset + buffer.length());
			position.setLength(argument.length());

			if (proposal instanceof CCompletionProposal) // handle the "unknown" case where we only insert a proposal.
				((CCompletionProposal) proposal).setReplacementOffset(replacementOffset + buffer.length());
			buffer.append(argument);
		}

		buffer.append(")"); //$NON-NLS-1$

		return buffer.toString();
	}

	/**
	 * Copied from JDT with replacing JDT types with CDT types
	 */
	private ICompletionProposal[][] guessParameters(char[][] parameterNames) throws Exception {
		int count= parameterNames.length;
		fPositions= new Position[count];
		fChoices= new ICompletionProposal[count][];

		IType[] parameterTypes= getFunctionParametersTypes();
		ParameterGuesser guesser= new ParameterGuesser(fContext.getCompletionNode().getTranslationUnit());
		ArrayList<IBinding> assignableElements = getAssignableElements();

		for (int i= count - 1; i >= 0; i--) {
			String paramName= new String(parameterNames[i]);
			Position position= new Position(0,0);

			boolean isLastParameter= i == count - 1;
			ArrayList<ICompletionProposal> allProposals = new ArrayList<ICompletionProposal>();
			CCompletionProposal proposal= new CCompletionProposal(paramName, 0, paramName.length(), null, paramName, 0);
			if (isLastParameter)
				proposal.setTriggerCharacters(new char[] { ',' });
			allProposals.add(proposal);
			ICompletionProposal[] argumentProposals= guesser.parameterProposals(parameterTypes[i], paramName, position, assignableElements, true, isLastParameter);
			allProposals.addAll(Arrays.asList(argumentProposals));
			fPositions[i]= position;
			fChoices[i]= allProposals.toArray(new ICompletionProposal[allProposals.size()]);
		}

		return fChoices;
	}

	private IType[] getFunctionParametersTypes() {
		IParameter [] params = fFunction.getParameters();
		IType [] ret = new IType[params.length];
		for (int i = 0; i < params.length; i++) {
			ret[i] = params[i].getType();
		}
		return ret;
	}

	/**
	 * @return the start offset of the statement that contains the current position. 
	 */
	private int getStatementStartOffset() {
		return fContext.getParseOffset() - fPrefix.length();
	}

	/**
	 * Returns a list of functions and variables that are defined in current context.
	 * @return a list of assignable elements.
	 */
	private ArrayList<IBinding> getAssignableElements() {
		int i = getStatementStartOffset();
		
		/*
		 * The loop is for handling the case of invoking a function from inside a function call.
		 * The basic idea is to find the start offset of the statement that includes the current function call,
		 * and then use this offset to get all definitions at this point.
		 * Searching at the start offset of a function call from inside a function call will find only the including function.
		 * Example:
		 * 		char* foo(int a, int b) {return NULL;}
		 * 		void bar(char* name){}
		 * 		...
		 * 		bar(foo(
		 * 		Invoking content assist at this point will normally try to get all defined elements at the offset of 'f'
		 *		(the start offset of the function call statement) and then filter them and prepare a list of suggestions for each of the parameters a and b.
		 *      Doing that here will result on resolving only the binding for function "bar(char*)".
		 *      The solution is to  get back to the start offset of the containing call 'b'. 
		 */
		while (i >= 0) {
			CContentAssistInvocationContext c = new CContentAssistInvocationContext(fTextViewer, i, getCEditor(), true, false);
			IASTCompletionNode node = c.getCompletionNode();
			IASTName [] names = node.getNames();
			ArrayList<IBinding> allBindings = new ArrayList<IBinding>();
			for (IASTName name : names) {
				IASTCompletionContext astContext = name.getCompletionContext();
				IBinding[] bindings = astContext.findBindings(name, true);
				if (bindings != null) {
					AccessContext accessibilityContext = new AccessContext(name);
					for (IBinding binding : bindings) {
						if (accessibilityContext.isAccessible(binding))
							allBindings.add(binding);
					}
				}
			}
			if (!isInFunctionCall(allBindings))
				return allBindings;
			// Get the start offset of the call that contains that call
			i = getFunctionCallStartOffset(fContext.getDocument(), allBindings.get(0).getName(), i);
			if (i == -1)
				break;
		}
		// failed to resolve any bindings, return an empty list.
		return new ArrayList<IBinding>();
	}

	/**
	 * If all the resolved bindings are for the same function, then the search operation is performed
	 * inside a function call.
	 * @return true, if all bindings are for the same function or overloaded versions of a function
	 */
	private boolean isInFunctionCall(ArrayList<IBinding> allBindings) {
		if (allBindings == null || allBindings.size() == 0)
			return false;
		String functionName = allBindings.get(0).getName();
		for (IBinding binding : allBindings) {
			if (! (binding instanceof IFunction && binding.getName().equals(functionName)))
				return false;
		}
		return true;
	}
	
	/**
	 * @return the start offset of the function call that contains the given offset.
	 */
	private int getFunctionCallStartOffset(IDocument doc, String funcName, int offset) {
			try {
				return doc.get(0, offset).lastIndexOf(funcName);
			} catch (BadLocationException e) {
			}
			return -1;
	}
	
	/**
	 * Copied from JDT
	 */
	private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
		if (!document.containsPositionCategory(getCategory())) {
			document.addPositionCategory(getCategory());
			fUpdater= new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(fUpdater);

			model.addLinkingListener(new ILinkedModeListener() {
				@Override
				public void left(LinkedModeModel environment, int flags) {
					ensurePositionCategoryRemoved(document);
				}

				@Override
				public void suspend(LinkedModeModel environment) {}

				@Override
				public void resume(LinkedModeModel environment, int flags) {}
			});
		}
	}

	/**
	 * Copied from JDT
	 */
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

	/**
	 * Copied from JDT
	 */
	private String getCategory() {
		return "ParameterGuessingProposal_" + toString(); //$NON-NLS-1$
	}

	/**
	 * Copied from JDT with replacing JDT types with CDT types.
	 * 
	 * Returns the currently active C/C++ editor, or <code>null</code> if it
	 * cannot be determined.
	 *
	 * @return  the currently active C/C++ editor, or <code>null</code>
	 */
	private CEditor getCEditor() {
		IEditorPart part= CUIPlugin.getActivePage().getActiveEditor();
		if (part instanceof CEditor)
			return (CEditor) part;
		else
			return null;
	}
}
