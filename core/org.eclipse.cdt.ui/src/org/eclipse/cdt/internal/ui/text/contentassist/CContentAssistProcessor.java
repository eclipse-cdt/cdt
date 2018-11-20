/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Kirk Beitz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.HeuristicResolver;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.ui.preferences.ProposalFilterPreferencesUtil;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;
import org.eclipse.cdt.internal.ui.text.Symbols;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.IProposalFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;

/**
 * C/C++ content assist processor.
 */
public class CContentAssistProcessor extends ContentAssistProcessor {

	private static class ActivationSet {
		private final String set;

		ActivationSet(String s) {
			set = s;
		}

		boolean contains(char c) {
			return -1 != set.indexOf(c);
		}
	}

	/**
	 * A wrapper for {@link ICompetionProposal}s.
	 */
	private static class CCompletionProposalWrapper implements ICCompletionProposal {
		private ICompletionProposal fWrappedProposal;

		public CCompletionProposalWrapper(ICompletionProposal proposal) {
			fWrappedProposal = proposal;
		}

		@Override
		public String getIdString() {
			return fWrappedProposal.getDisplayString();
		}

		@Override
		public int getRelevance() {
			return RelevanceConstants.CASE_MATCH_RELEVANCE + RelevanceConstants.KEYWORD_TYPE_RELEVANCE;
		}

		@Override
		public void apply(IDocument document) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getAdditionalProposalInfo() {
			return fWrappedProposal.getAdditionalProposalInfo();
		}

		@Override
		public IContextInformation getContextInformation() {
			return fWrappedProposal.getContextInformation();
		}

		@Override
		public String getDisplayString() {
			return fWrappedProposal.getDisplayString();
		}

		@Override
		public Image getImage() {
			return fWrappedProposal.getImage();
		}

		@Override
		public Point getSelection(IDocument document) {
			return fWrappedProposal.getSelection(document);
		}

		/**
		 * @return the original proposal
		 */
		public ICompletionProposal unwrap() {
			return fWrappedProposal;
		}
	}

	private ActivationSet fReplacementAutoActivationCharacters;
	private ActivationSet fCContentAutoActivationCharacters;
	private IContextInformationValidator fValidator;
	private final IEditorPart fEditor;

	public CContentAssistProcessor(IEditorPart editor, ContentAssistant assistant, String partition) {
		super(assistant, partition);
		fEditor = editor;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		if (fValidator == null) {
			fValidator = new CParameterListValidator();
		}
		return fValidator;
	}

	@Override
	protected List<ICompletionProposal> filterAndSortProposals(List<ICompletionProposal> proposals,
			IProgressMonitor monitor, ContentAssistInvocationContext context) {
		IProposalFilter filter = getCompletionFilter();
		ICCompletionProposal[] proposalsInput = new ICCompletionProposal[proposals.size()];
		// Wrap proposals which are no ICCompletionProposals.
		boolean wrapped = false;
		int i = 0;
		for (ICompletionProposal proposal : proposals) {
			if (proposal instanceof ICCompletionProposal) {
				proposalsInput[i++] = (ICCompletionProposal) proposal;
			} else {
				wrapped = true;
				proposalsInput[i++] = new CCompletionProposalWrapper(proposal);
			}
		}
		// Filter.
		ICCompletionProposal[] proposalsFiltered = filter.filterProposals(proposalsInput);

		// Sort.
		boolean sortByAlphabet = CUIPlugin.getDefault().getPreferenceStore()
				.getBoolean(ContentAssistPreference.ORDER_PROPOSALS);
		if (sortByAlphabet) {
			// Already sorted alphabetically by DefaultProposalFilter
			// in case of custom proposal filter, keep ordering applied by filter.
		} else {
			// Sort by relevance.
			CCompletionProposalComparator propsComp = new CCompletionProposalComparator();
			propsComp.setOrderAlphabetically(sortByAlphabet);
			Arrays.sort(proposalsFiltered, propsComp);
		}
		List<ICompletionProposal> filteredList;
		if (wrapped) {
			// Unwrap again.
			filteredList = new ArrayList<>(proposalsFiltered.length);
			for (ICCompletionProposal proposal : proposalsFiltered) {
				if (proposal instanceof CCompletionProposalWrapper) {
					filteredList.add(((CCompletionProposalWrapper) proposal).unwrap());
				} else {
					filteredList.add(proposal);
				}
			}
		} else {
			final ICompletionProposal[] tmp = proposalsFiltered;
			filteredList = Arrays.asList(tmp);
		}
		return filteredList;
	}

	private IProposalFilter getCompletionFilter() {
		IProposalFilter filter = null;
		try {
			IConfigurationElement filterElement = ProposalFilterPreferencesUtil.getPreferredFilterElement();
			if (null != filterElement) {
				Object contribObject = filterElement.createExecutableExtension("class"); //$NON-NLS-1$
				if ((contribObject instanceof IProposalFilter)) {
					filter = (IProposalFilter) contribObject;
				}
			}
		} catch (InvalidRegistryObjectException e) {
			// No action required since we will be using the fail-safe default filter.
			CUIPlugin.log(e);
		} catch (CoreException e) {
			// No action required since we will be using the fail-safe default filter.
			CUIPlugin.log(e);
		}

		if (filter == null) {
			// Fail-safe default implementation.
			filter = new DefaultProposalFilter();
		}
		return filter;
	}

	@Override
	protected List<IContextInformation> filterAndSortContextInformation(List<IContextInformation> contexts,
			IProgressMonitor monitor) {
		return contexts;
	}

	/**
	 * Establishes this processor's set of characters checked after
	 * auto-activation to determine if auto-replacement correction
	 * is to occur.
	 * <p>
	 * This set is a (possibly complete) subset of the set established by
	 * {@link ContentAssistProcessor#setCompletionProposalAutoActivationCharacters},
	 * which is the set of characters used to initially trigger auto-activation
	 * for any content-assist operations, including this.  (<i>And while the
	 * name setCompletionProposalAutoActivationCharacters may now be a bit
	 * misleading, it is part of an API implementation called by jface.</i>)
	 *
	 * @param activationSet the activation set
	 */
	public void setReplacementAutoActivationCharacters(String activationSet) {
		fReplacementAutoActivationCharacters = new ActivationSet(activationSet);
	}

	/**
	 * Establishes this processor's set of characters checked after
	 * auto-activation and any auto-correction to determine if completion
	 * proposal computation is to proceed.
	 * <p>
	 * This set is a (possibly complete) subset of the set established by
	 * {@link ContentAssistProcessor#setCompletionProposalAutoActivationCharacters},
	 * which is the set of characters used to initially trigger auto-activation
	 * for any content-assist operations, including this.  (<i>And while the
	 * name setCompletionProposalAutoActivationCharacters may now be a bit
	 * misleading, it is part of an API implementation called by jface.</i>)
	 *
	 * @param activationSet the activation set
	 */
	public void setCContentAutoActivationCharacters(String activationSet) {
		fCContentAutoActivationCharacters = new ActivationSet(activationSet);
	}

	@Override
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset, boolean isCompletion) {
		char activationChar = getActivationChar(viewer, offset);
		CContentAssistInvocationContext context = new CContentAssistInvocationContext(viewer, offset, fEditor,
				isCompletion, isAutoActivated());
		try {
			if (isCompletion && activationChar == '.' && fReplacementAutoActivationCharacters != null
					&& fReplacementAutoActivationCharacters.contains('.')) {
				IASTCompletionNode node = context.getCompletionNode();
				if (node != null) {
					IASTName[] names = node.getNames();
					if (names.length > 0 && names[0].getParent() instanceof IASTFieldReference) {
						IASTFieldReference ref = (IASTFieldReference) names[0].getParent();
						IASTExpression ownerExpr = ref.getFieldOwner();
						IType ownerExprType = SemanticUtil.getNestedType(ownerExpr.getExpressionType(),
								SemanticUtil.TDEF);
						if (ownerExprType instanceof ICPPUnknownType) {
							try {
								CPPSemantics.pushLookupPoint(names[0]);
								ownerExprType = HeuristicResolver.resolveUnknownType((ICPPUnknownType) ownerExprType);
							} finally {
								CPPSemantics.popLookupPoint();
							}
						}
						if (ownerExprType instanceof IPointerType) {
							context = replaceDotWithArrow(viewer, offset, isCompletion, context, activationChar);
						}
					}
				}
				if (context != null && isAutoActivated()
						&& !fCContentAutoActivationCharacters.contains(activationChar)) {
					// Auto-replace, but not auto-content-assist - bug 344387.
					context.dispose();
					context = null;
				}
			}
		} catch (RuntimeException | Error e) {
			if (context != null)
				context.dispose();
			throw e;
		}

		return context;
	}

	private CContentAssistInvocationContext replaceDotWithArrow(ITextViewer viewer, int offset, boolean isCompletion,
			CContentAssistInvocationContext context, char activationChar) {
		IDocument doc = viewer.getDocument();
		try {
			doc.replace(offset - 1, 1, "->"); //$NON-NLS-1$
			context.dispose();
			context = null;
			// If user turned on activation only for replacement characters,
			// setting the context to null will skip the proposals popup later.
			if (!isAutoActivated() || fCContentAutoActivationCharacters.contains(activationChar)) {
				context = new CContentAssistInvocationContext(viewer, offset + 1, fEditor, isCompletion,
						isAutoActivated());
			}
		} catch (BadLocationException e) {
			// Ignore
		}
		return context;
	}

	/**
	 * Returns the character preceding the content assist activation offset.
	 *
	 * @param viewer
	 * @param offset
	 * @return the activation character
	 */
	private char getActivationChar(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		if (doc == null) {
			return 0;
		}
		if (offset <= 0) {
			return 0;
		}
		try {
			return doc.getChar(offset - 1);
		} catch (BadLocationException e) {
		}
		return 0;
	}

	@Override
	protected boolean verifyAutoActivation(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		if (doc == null) {
			return false;
		}
		if (offset <= 0) {
			return false;
		}
		try {
			char activationChar = doc.getChar(--offset);
			switch (activationChar) {
			case ':':
				return offset > 0 && doc.getChar(--offset) == ':';
			case '>':
				return offset > 0 && doc.getChar(--offset) == '-';
			case '.':
				// Avoid completion of float literals
				CHeuristicScanner scanner = new CHeuristicScanner(doc);
				int token = scanner.previousToken(--offset, Math.max(0, offset - 200));
				// The scanner reports numbers as identifiers
				if (token == Symbols.TokenIDENT
						&& !Character.isJavaIdentifierStart(doc.getChar(scanner.getPosition() + 1))) {
					// Not a valid identifier
					return false;
				}
				return true;
			}
		} catch (BadLocationException e) {
		}
		return false;
	}
}
