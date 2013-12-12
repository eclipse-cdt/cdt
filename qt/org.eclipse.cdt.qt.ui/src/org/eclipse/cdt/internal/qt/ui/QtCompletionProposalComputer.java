/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.qt.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITagReader;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.internal.ui.text.contentassist.ParsingBasedProposalComputer;
import org.eclipse.cdt.internal.ui.text.contentassist.RelevanceConstants;
import org.eclipse.cdt.qt.core.QtKeywords;
import org.eclipse.cdt.qt.core.QtNature;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.cdt.qt.ui.QtUIPlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

@SuppressWarnings("restriction")
public class QtCompletionProposalComputer extends ParsingBasedProposalComputer {
	private boolean isApplicable(ICEditorContentAssistInvocationContext context) {
		ITranslationUnit tu = context.getTranslationUnit();
		if (tu == null)
			return false;

		ICProject cProject = tu.getCProject();
		if (cProject == null)
			return false;

		IProject project = cProject.getProject();
		if (project == null)
			return false;

		try {
			return project.hasNature(QtNature.ID);
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		}
	}

	private static boolean is_QObject_connect(
			ICEditorContentAssistInvocationContext context,
			IASTCompletionContext astContext, IASTName name) {
		IASTName connectName = name.getLastName();
		if (!QtKeywords.CONNECT.equals(new String(connectName.getSimpleID())))
			return false;

		IBinding[] funcBindings = astContext.findBindings(connectName,
				!context.isContextInformationStyle());
		for (IBinding funcBinding : funcBindings)
			if (funcBinding instanceof ICPPFunction) {
				IBinding ownerBinding = ((ICPPFunction) funcBinding).getOwner();
				if (ownerBinding != null
						&& QtKeywords.QOBJECT.equals(ownerBinding.getName()))
					return true;
			}

		return false;
	}

	private static class Completion {
		private final String replacement;
		private final String display;
		private final int cursorOffset;

		public static final Completion SIGNAL = new Completion("SIGNAL()",
				"SIGNAL(a)", -1);
		public static final Completion SLOT = new Completion("SLOT()",
				"SLOT(a)", -1);

		public Completion(String replacement) {
			this(replacement, replacement, 0);
		}

		public Completion(String replacement, String display, int cursorOffset) {
			this.replacement = replacement;
			this.display = display;
			this.cursorOffset = cursorOffset;
		}

		public ICompletionProposal createProposal(
				ICEditorContentAssistInvocationContext context) {
			int repLength = replacement.length();
			int repOffset = context.getInvocationOffset();
			CCompletionProposal p = new CCompletionProposal(replacement,
					repOffset, repLength, null, display,
					RelevanceConstants.DEFAULT_TYPE_RELEVANCE,
					context.getViewer());
			p.setCursorPosition(repLength + cursorOffset);
			return p;
		}

		@Override
		public String toString() {
			if (replacement == null)
				return super.toString();
			return replacement + '@' + cursorOffset;
		}
	}

	private static interface MethodFilter {
		public boolean keep(ICPPMethod method);

		public static class Qt {
			public static final MethodFilter Signal = new MethodFilter() {
				@Override
				public boolean keep(ICPPMethod method) {
					ITagReader tagReader = CCorePlugin.getTagService()
							.findTagReader(method);
					if (tagReader == null)
						return false;

					ITag tag = tagReader.getTag(QtPlugin.SIGNAL_SLOT_TAGGER_ID);
					if (tag == null)
						return false;

					int result = tag.getByte(0);
					return result != ITag.FAIL
							&& ((result & QtPlugin.SignalSlot_Mask_signal) == QtPlugin.SignalSlot_Mask_signal);
				}
			};

			public static final MethodFilter Slot = new MethodFilter() {
				@Override
				public boolean keep(ICPPMethod method) {
					ITagReader tagReader = CCorePlugin.getTagService()
							.findTagReader(method);
					if (tagReader == null)
						return false;

					ITag tag = tagReader.getTag(QtPlugin.SIGNAL_SLOT_TAGGER_ID);
					if (tag == null)
						return false;

					int result = tag.getByte(0);
					return result != ITag.FAIL
							&& ((result & QtPlugin.SignalSlot_Mask_slot) == QtPlugin.SignalSlot_Mask_slot);
				}
			};
		}
	}

	private static Iterable<ICPPMethod> filterMethods(final ICPPClassType cls,
			final MethodFilter filter) {
		return new Iterable<ICPPMethod>() {
			@Override
			public Iterator<ICPPMethod> iterator() {
				return new Iterator<ICPPMethod>() {
					private int index = 0;
					private final ICPPMethod[] methods = cls.getMethods();

					@Override
					public boolean hasNext() {
						for (; index < methods.length; ++index)
							if (filter.keep(methods[index]))
								return true;
						return false;
					}

					@Override
					public ICPPMethod next() {
						return methods[index++];
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}

	private static String getSignature(ICPPMethod method) {
		StringBuilder signature = new StringBuilder();

		signature.append(method.getName());
		signature.append('(');
		boolean first = true;
		for (ICPPParameter param : method.getParameters()) {
			if (first)
				first = false;
			else
				signature.append(", ");
			signature.append(ASTTypeUtil.getType(param.getType()));
		}

		signature.append(')');
		return signature.toString();
	}

	private static void addCompletionsFor(Collection<Completion> completions,
			IASTInitializerClause init, MethodFilter filter) {
		if (!(init instanceof ICPPASTInitializerClause))
			return;

		ICPPEvaluation eval = ((ICPPASTInitializerClause) init).getEvaluation();
		if (eval == null)
			return;

		IType type = eval.getTypeOrFunctionSet(init);
		while (type instanceof IPointerType)
			type = ((IPointerType) type).getType();

		if (type instanceof ICPPClassType)
			for (ICPPMethod signal : filterMethods((ICPPClassType) type, filter))
				completions.add(new Completion(getSignature(signal)));
	}

	// Copied from org.eclipse.cdt.internal.ui.text.CParameterListValidator
	private static int indexOfClosingPeer(String code, char left, char right,
			int pos) {
		int level = 0;
		final int length = code.length();
		while (pos < length) {
			char ch = code.charAt(pos);
			if (ch == left) {
				++level;
			} else if (ch == right) {
				if (--level == 0) {
					return pos;
				}
			}
			++pos;
		}
		return -1;
	}

	// Copied from org.eclipse.cdt.internal.ui.text.CParameterListValidator
	private static int[] computeCommaPositions(String code) {
		final int length = code.length();
		int pos = 0;
		List<Integer> positions = new ArrayList<Integer>();
		positions.add(new Integer(-1));
		while (pos < length && pos != -1) {
			char ch = code.charAt(pos);
			switch (ch) {
			case ',':
				positions.add(new Integer(pos));
				break;
			case '(':
				pos = indexOfClosingPeer(code, '(', ')', pos);
				break;
			case '<':
				pos = indexOfClosingPeer(code, '<', '>', pos);
				break;
			case '[':
				pos = indexOfClosingPeer(code, '[', ']', pos);
				break;
			default:
				break;
			}
			if (pos != -1)
				pos++;
		}
		positions.add(new Integer(length));

		int[] fields = new int[positions.size()];
		for (int i = 0; i < fields.length; i++)
			fields[i] = positions.get(i).intValue();
		return fields;
	}

	private void addConnectParameterCompletions(
			List<ICompletionProposal> proposals,
			ICEditorContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) {
		IASTName[] names = completionNode.getNames();
		List<Completion> completions = new LinkedList<Completion>();

		for (IASTName name : names) {
			// The node isn't properly hooked up, must have backtracked out of
			// this node
			if (name.getTranslationUnit() == null)
				continue;

			IASTCompletionContext astContext = name.getCompletionContext();
			if (astContext == null || !(astContext instanceof IASTNode))
				continue;
			IASTNode astNode = (IASTNode) astContext;

			if (is_QObject_connect(context, astContext, name)) {
				int parseOffset = context.getParseOffset();
				int invocationOffset = context.getInvocationOffset();

				String unparsed = "";
				try {
					unparsed = context.getDocument().get(parseOffset,
							invocationOffset - parseOffset);
				} catch (BadLocationException e) {
					QtUIPlugin.log(e);
				}

				if (unparsed.length() > 0 && unparsed.charAt(0) == '(')
					unparsed = unparsed.substring(1);

				int[] commas = computeCommaPositions(unparsed);
				switch (commas.length) {
				case 3:
					completions.add(Completion.SIGNAL);
					break;
				case 5:
					completions.add(Completion.SLOT);
					break;
				}
			} else if (astNode.getPropertyInParent() == IASTFunctionCallExpression.ARGUMENT) {
				IASTNode parent = astNode.getParent();
				if (!(parent instanceof IASTFunctionCallExpression))
					continue;
				IASTFunctionCallExpression call = (IASTFunctionCallExpression) parent;
				IASTExpression nameExpr = call.getFunctionNameExpression();
				if (!(nameExpr instanceof IASTIdExpression))
					continue;
				IASTIdExpression funcNameIdExpr = (IASTIdExpression) nameExpr;
				IASTName funcName = funcNameIdExpr.getName();

				if (!is_QObject_connect(context, astContext, funcName))
					continue;

				IASTInitializerClause[] args = call.getArguments();
				switch (args.length) {
				case 2:
					addCompletionsFor(completions, args[0],
							MethodFilter.Qt.Signal);
					break;
				case 4:
					addCompletionsFor(completions, args[2],
							MethodFilter.Qt.Slot);
					break;
				}
			}
		}

		for (Completion completion : completions) {
			ICompletionProposal proposal = completion.createProposal(context);
			if (proposal != null)
				proposals.add(proposal);
		}
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(
			CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix)
			throws CoreException {
		if (!isApplicable(context))
			return Collections.emptyList();

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		addConnectParameterCompletions(proposals, context, completionNode,
				prefix);
		return proposals;
	}
}
