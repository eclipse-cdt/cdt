/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IAutomaticVariable;
import org.eclipse.cdt.make.core.makefile.IBuiltinFunction;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.text.WordPartDetector;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

/**
 * MakefileCompletionProcessor
 */
public class MakefileCompletionProcessor implements IContentAssistProcessor {
	/**
	 * Simple content assist tip closer. The tip is valid in a range
	 * of 5 characters around its pop-up location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {
		protected int fInstallOffset;

		@Override
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		@Override
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset = offset;
		}

		@Override
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected IContextInformationValidator fValidator = new Validator();
	protected Image imageFunction = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_FUNCTION);
	protected Image imageVariable = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_VARIABLE);
	protected Image imageAutomaticVariable = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_AUTO_VARIABLE);
	protected Image imageTarget = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_TARGET);

	protected IEditorPart fEditor;
	protected IWorkingCopyManager fManager;

	private Comparator<IDirective> directivesComparator = new Comparator<IDirective>() {
		@Override
		public int compare(IDirective o1, IDirective o2) {
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	};

	private class BracketHandler {
		private char open;
		private char closed;
		private boolean found;
		private String followingText;

		public BracketHandler(String input) {
			char firstChar = input.length() > 0 ? input.charAt(0) : 0;
			open = firstChar == '{' ? '{' : '(';
			closed = firstChar == '{' ? '}' : ')';
			found = firstChar == open;
			followingText = found ? input.substring(1) : input;
		}
	}

	public MakefileCompletionProcessor(IEditorPart editor) {
		fEditor = editor;
		fManager = MakeUIPlugin.getDefault().getWorkingCopyManager();
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		List<ICompletionProposal> proposalList = new ArrayList<>();
		IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
		WordPartDetector wordPart = new WordPartDetector(viewer.getDocument(), documentOffset);
		if (wordPart.isMacro()) {
			IAutomaticVariable[] automaticVariables = makefile.getAutomaticVariables();
			proposalList.addAll(createCompletionProposals(wordPart, automaticVariables));

			IMacroDefinition[] macroDefinitions = makefile.getMacroDefinitions();
			Arrays.sort(macroDefinitions, directivesComparator);
			proposalList.addAll(createCompletionProposals(wordPart, macroDefinitions));

			IBuiltinFunction[] builtinFunctions = makefile.getBuiltinFunctions();
			Arrays.sort(builtinFunctions, directivesComparator);
			proposalList.addAll(createCompletionProposals(wordPart, builtinFunctions));
		} else {
			ITargetRule[] targetRules = makefile.getTargetRules();
			Arrays.sort(targetRules, directivesComparator);
			proposalList.addAll(createCompletionProposals(wordPart, targetRules));
		}

		return proposalList.toArray(new ICompletionProposal[proposalList.size()]);
	}

	private String macro(String name, BracketHandler bracket) {
		if (bracket.found) {
			name = bracket.open + name + bracket.closed;
		}
		return '$' + name;
	}

	private ArrayList<ICompletionProposal> createCompletionProposals(WordPartDetector wordPart,
			IAutomaticVariable[] autoVars) {
		ArrayList<ICompletionProposal> proposalList = new ArrayList<>(autoVars.length);
		String wordPartName = wordPart.getName();
		BracketHandler bracket = new BracketHandler(wordPartName);
		String partialName = bracket.followingText;

		for (IMacroDefinition autoVar : autoVars) {
			String name = autoVar.getName();
			if (name.startsWith(partialName)) {
				String replacement;
				if (bracket.found) {
					replacement = name + bracket.closed;
				} else {
					replacement = name;
				}
				CompletionProposal proposal = new CompletionProposal(replacement, wordPart.getOffset(),
						partialName.length(), replacement.length(), imageAutomaticVariable,
						macro(name, bracket) + " - " + autoVar.getValue().toString(), //$NON-NLS-1$
						null, autoVar.getValue().toString());
				proposalList.add(proposal);
			}
		}
		return proposalList;
	}

	private ArrayList<ICompletionProposal> createCompletionProposals(WordPartDetector wordPart,
			IMacroDefinition[] macros) {
		ArrayList<ICompletionProposal> proposalList = new ArrayList<>(macros.length);

		String wordPartName = wordPart.getName();
		BracketHandler bracket = new BracketHandler(wordPartName);
		String partialName = bracket.followingText;

		for (IMacroDefinition macro : macros) {
			String name = macro.getName();
			if (name.startsWith(partialName)) {
				String replacement;
				if (bracket.found) {
					replacement = name + bracket.closed;
				} else {
					replacement = bracket.open + name + bracket.closed;
				}
				String displayString = name;
				ICompletionProposal proposal = new CompletionProposal(replacement, wordPart.getOffset(),
						partialName.length(), replacement.length(), imageVariable, displayString, null,
						macro.getValue().toString());
				proposalList.add(proposal);
			}
		}
		return proposalList;
	}

	private ArrayList<ICompletionProposal> createCompletionProposals(WordPartDetector wordPart,
			IBuiltinFunction[] builtinFuns) {
		ArrayList<ICompletionProposal> proposalList = new ArrayList<>(builtinFuns.length);

		String wordPartName = wordPart.getName();
		BracketHandler bracket = new BracketHandler(wordPartName);
		String partialName = bracket.followingText;

		for (IBuiltinFunction builtinFun : builtinFuns) {
			String name = builtinFun.getName();
			String replacement;
			if (bracket.found) {
				replacement = name + bracket.closed;
			} else {
				replacement = bracket.open + name + bracket.closed;
			}
			int indexComma = replacement.indexOf(',');
			int cursorPosition = indexComma >= 0 ? indexComma : replacement.length() - 1;
			if (name.startsWith(partialName)) {
				ICompletionProposal proposal = new CompletionProposal(replacement, wordPart.getOffset(),
						partialName.length(), cursorPosition, imageFunction, "$" + bracket.open + name + bracket.closed, //$NON-NLS-1$
						null, builtinFun.getValue().toString());
				proposalList.add(proposal);
			}
		}
		return proposalList;
	}

	private ArrayList<ICompletionProposal> createCompletionProposals(WordPartDetector wordPart, ITargetRule[] targets) {
		ArrayList<ICompletionProposal> proposalList = new ArrayList<>(targets.length);

		String partialName = wordPart.getName();
		for (ITargetRule target : targets) {
			String name = target.getTarget().toString();
			if (name.startsWith(partialName)) {
				String replacement = name;
				ICompletionProposal proposal = new CompletionProposal(replacement, wordPart.getOffset(),
						partialName.length(), replacement.length(), imageTarget, name, null, null);
				proposalList.add(proposal);
			}
		}
		return proposalList;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		WordPartDetector wordPart = new WordPartDetector(viewer.getDocument(), documentOffset);
		IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
		ArrayList<String> contextList = new ArrayList<>();
		ArrayList<IContextInformation> contextInformationList = new ArrayList<>();
		if (wordPart.isMacro()) {
			IDirective[] statements = makefile.getMacroDefinitions();
			for (IDirective statement : statements) {
				if (statement instanceof IMacroDefinition) {
					String name = ((IMacroDefinition) statement).getName();
					if (name != null && name.equals(wordPart.getName())) {
						String value = ((IMacroDefinition) statement).getValue().toString();
						if (value != null && value.length() > 0) {
							contextList.add(value);
							contextInformationList
									.add(new ContextInformation(imageVariable, wordPart.getName(), value));
						}
					}
				}
			}
			statements = makefile.getBuiltinMacroDefinitions();
			for (IDirective statement : statements) {
				if (statement instanceof IMacroDefinition) {
					String name = ((IMacroDefinition) statement).getName();
					if (name != null && name.equals(wordPart.getName())) {
						String value = ((IMacroDefinition) statement).getValue().toString();
						if (value != null && value.length() > 0) {
							contextList.add(value);
							contextInformationList
									.add(new ContextInformation(imageAutomaticVariable, wordPart.getName(), value));
						}
					}
				}
			}
		}

		IContextInformation[] result = contextInformationList
				.toArray(new IContextInformation[contextInformationList.size()]);
		return result;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

}
