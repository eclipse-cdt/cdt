/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.cdt.ui.text.IProblemLocation;
import org.eclipse.cdt.ui.text.IQuickFixProcessor;

import org.eclipse.cdt.internal.ui.text.IHtmlTagConstants;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.RankedWordProposal;

/**
 * Quick fix processor for incorrectly spelled words.
 */
public class WordQuickFixProcessor implements IQuickFixProcessor {
	/**
	 * Creates a new word quick fix processor.
	 */
	public WordQuickFixProcessor() {
	}

	/*
	 * @see org.eclipse.cdt.ui.text.java.IQuickFixProcessor#getCorrections(org.eclipse.cdt.ui.text.java.ContentAssistInvocationContext,org.eclipse.cdt.ui.text.java.IProblemLocation[])
	 */
	@Override
	public ICCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		final int threshold= SpellingPreferences.spellingProposalThreshold();

		int size= 0;
		List<RankedWordProposal> proposals= null;
		String[] arguments= null;

		IProblemLocation location= null;
		RankedWordProposal proposal= null;
		ICCompletionProposal[] result= null;

		boolean fixed= false;
		boolean match= false;
		boolean sentence= false;

		final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
		final ISpellChecker checker= engine.getSpellChecker();

		if (checker != null) {
			for (int index= 0; index < locations.length; index++) {
				location= locations[index];
				if (location.getProblemId() == CSpellingReconcileStrategy.SPELLING_PROBLEM_ID) {
					arguments= location.getProblemArguments();
					if (arguments != null && arguments.length > 4) {
						sentence= Boolean.valueOf(arguments[3]).booleanValue();
						match= Boolean.valueOf(arguments[4]).booleanValue();
						fixed= arguments[0].charAt(0) == IHtmlTagConstants.HTML_TAG_PREFIX;

						if ((sentence && match) && !fixed) {
							result= new ICCompletionProposal[] { new ChangeCaseProposal(arguments, location.getOffset(), location.getLength(), context, engine.getLocale())};
						} else {
							proposals= new ArrayList<RankedWordProposal>(checker.getProposals(arguments[0], sentence));
							size= proposals.size();

							if (threshold > 0 && size > threshold) {
								Collections.sort(proposals);
								proposals= proposals.subList(size - threshold - 1, size - 1);
								size= proposals.size();
							}

							boolean extendable= !fixed ? (checker.acceptsWords() || AddWordProposal.canAskToConfigure()) : false;
							result= new ICCompletionProposal[size + (extendable ? 3 : 2)];

							for (index= 0; index < size; index++) {
								proposal= proposals.get(index);
								result[index]= new WordCorrectionProposal(proposal.getText(), arguments, location.getOffset(), location.getLength(), context, proposal.getRank());
							}

							if (extendable)
								result[index++]= new AddWordProposal(arguments[0], context);

							result[index++]= new WordIgnoreProposal(arguments[0], context);
							result[index++]= new DisableSpellCheckingProposal(context);
						}
						break;
					}
				}
			}
		}
		return result;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.java.IQuickFixProcessor#hasCorrections(org.eclipse.cdt.core.ICompilationUnit,int)
	 */
	@Override
	public final boolean hasCorrections(ITranslationUnit unit, int id) {
		return id == CSpellingReconcileStrategy.SPELLING_PROBLEM_ID;
	}
}
