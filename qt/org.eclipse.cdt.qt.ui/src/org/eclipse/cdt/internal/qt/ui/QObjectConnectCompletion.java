/*
 * Copyright (c) 2013, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.qt.core.ASTUtil;
import org.eclipse.cdt.internal.qt.core.QtFunctionCallUtil;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.contentassist.RelevanceConstants;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

@SuppressWarnings("restriction")
public class QObjectConnectCompletion {
	// These suggestions are populated from the index, so the case is always an exact match.
	// Secondly, these suggestions should appear above generic variable and method matches, since
	// have based the calculation on the exact function that is being called.

	private static final int MACRO_RELEVANCE = RelevanceConstants.CASE_MATCH_RELEVANCE
			+ RelevanceConstants.LOCAL_VARIABLE_TYPE_RELEVANCE + 2;
	private static final int MACRO_PARAM_RELEVANCE = RelevanceConstants.CASE_MATCH_RELEVANCE
			+ RelevanceConstants.METHOD_TYPE_RELEVANCE + 1;

	/**
	 * Different suggestions should be proposed for each parameter of the QObject::connect
	 * function call.  The 'sender' parameter should suggest SIGNAL, but 'member' can be
	 * either SLOT or SIGNAL.
	 */
	public enum Param {
		Signal, Member, Generic
	}

	private final Param param;
	private final Data data;

	public QObjectConnectCompletion(Param param) {
		this.param = param;
		this.data = null;
	}

	public QObjectConnectCompletion(String replacement) {
		this.param = Param.Generic;
		this.data = new Data(replacement);
	}

	/**
	 * The data used to produce the completions varies depending on the role of the
	 * parameter that is being completed.
	 */
	private static class Data {
		public final String replacement;
		public final String display;
		public final int cursorOffset;

		public static final Data SIGNAL = new Data("SIGNAL()", "SIGNAL(a)", -1);
		public static final Data SLOT = new Data("SLOT()", "SLOT(a)", -1);

		public Data(String replacement) {
			this(replacement, replacement, 0);
		}

		public Data(String replacement, String display, int cursorOffset) {
			this.replacement = replacement;
			this.display = display;
			this.cursorOffset = cursorOffset;
		}

		public ICompletionProposal createProposal(ICEditorContentAssistInvocationContext context, int relevance) {
			int repLength = replacement.length();
			int repOffset = context.getInvocationOffset();
			CCompletionProposal p = new CCompletionProposal(replacement, repOffset, repLength, Activator.getQtLogo(),
					display, relevance, context.getViewer());
			p.setCursorPosition(repLength + cursorOffset);
			return p;
		}
	}

	private static void addProposal(Collection<ICompletionProposal> proposals,
			ICEditorContentAssistInvocationContext context, Data data, int relevance) {
		if (data == null)
			return;

		ICompletionProposal proposal = data.createProposal(context, relevance);
		if (proposal != null)
			proposals.add(proposal);
	}

	private void addProposals(Collection<ICompletionProposal> proposals,
			ICEditorContentAssistInvocationContext context) {

		if (data != null)
			addProposal(proposals, context, data, MACRO_PARAM_RELEVANCE);
		else
			switch (param) {
			case Signal:
				addProposal(proposals, context, Data.SIGNAL, MACRO_RELEVANCE);
				break;
			case Member:
				addProposal(proposals, context, Data.SLOT, MACRO_RELEVANCE);
				addProposal(proposals, context, Data.SIGNAL, MACRO_RELEVANCE - 1);
				break;
			default:
				break;
			}
	}

	// Copied from org.eclipse.cdt.internal.ui.text.CParameterListValidator
	private static int indexOfClosingPeer(String code, char left, char right, int pos) {
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
		List<Integer> positions = new ArrayList<>();
		positions.add(-1);
		while (pos < length && pos != -1) {
			char ch = code.charAt(pos);
			switch (ch) {
			case ',':
				positions.add(Integer.valueOf(pos));
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
		positions.add(Integer.valueOf(length));

		int[] fields = new int[positions.size()];
		for (int i = 0; i < fields.length; i++)
			fields[i] = positions.get(i).intValue();
		return fields;
	}

	private static Collection<QObjectConnectCompletion> getCompletionsFor(IType targetType, IASTInitializerClause arg) {

		if (!(targetType instanceof ICPPClassType))
			return null;
		ICPPClassType cls = (ICPPClassType) targetType;

		QtIndex qtIndex = QtIndex.getIndex(ASTUtil.getProject(arg));
		if (qtIndex == null)
			return null;

		IQObject qobj = null;
		try {
			qobj = qtIndex.findQObject(cls.getQualifiedName());
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}

		// QtIndex.findQObject will return null in some cases, e.g., when the parameter is null
		if (qobj == null)
			return null;

		Collection<QObjectConnectCompletion> completions = new ArrayList<>();
		String raw = arg.getRawSignature();
		if (raw.startsWith(QtKeywords.SIGNAL))
			for (IQMethod method : qobj.getSignals().withoutOverrides())
				for (String signature : method.getSignatures())
					completions.add(new QObjectConnectCompletion(signature));
		if (raw.startsWith(QtKeywords.SLOT))
			for (IQMethod method : qobj.getSlots().withoutOverrides())
				for (String signature : method.getSignatures())
					completions.add(new QObjectConnectCompletion(signature));
		return completions;
	}

	public static Collection<QObjectConnectCompletion> getConnectProposals(
			ICEditorContentAssistInvocationContext context, IASTName name, IASTCompletionContext astContext,
			IASTNode astNode) {

		if (QtFunctionCallUtil.isQObjectFunctionCall(astContext, !context.isContextInformationStyle(), name)) {
			int parseOffset = context.getParseOffset();
			int invocationOffset = context.getInvocationOffset();

			String unparsed = "";
			try {
				unparsed = context.getDocument().get(parseOffset, invocationOffset - parseOffset);
			} catch (BadLocationException e) {
				CCorePlugin.log(e);
			}

			if (unparsed.length() > 0 && unparsed.charAt(0) == '(')
				unparsed = unparsed.substring(1);

			int[] commas = computeCommaPositions(unparsed);
			switch (commas.length) {
			case 2:
			case 3:
				// Across all possible connect/disconnect overloads, the first and second arguments
				// can be SIGNAL expansion.
				return Collections.singletonList(new QObjectConnectCompletion(QObjectConnectCompletion.Param.Signal));
			case 4:
			case 5:
				// Across all possible connect/disconnect overloads, the first and second arguments
				// can be SIGNAL or SLOT expansions.
				return Collections.singletonList(new QObjectConnectCompletion(QObjectConnectCompletion.Param.Member));
			}

			return null;
		}

		if (astNode.getPropertyInParent() == IASTFunctionCallExpression.ARGUMENT) {
			IASTNode parent = astNode.getParent();
			if (!(parent instanceof IASTFunctionCallExpression))
				return null;

			// NOTE: QtConnectFunctionCall cannot be used here because that class expects a
			//       valid expression.  During content assist the function is still being
			//       created.

			IASTFunctionCallExpression call = (IASTFunctionCallExpression) parent;
			IASTExpression nameExpr = call.getFunctionNameExpression();
			IASTName funcName = null;
			if (nameExpr instanceof IASTIdExpression)
				funcName = ((IASTIdExpression) nameExpr).getName();
			else if (nameExpr instanceof ICPPASTFieldReference)
				funcName = ((ICPPASTFieldReference) nameExpr).getFieldName();

			// If this isn't a QObject::connect or QObject::disconnect function call then
			// look no further.
			if (!QtFunctionCallUtil.isQObjectFunctionCall(astContext, !context.isContextInformationStyle(), funcName))
				return null;

			// In a content assist context the argument that is currently being entered is
			// last in the function call.
			IASTInitializerClause[] args = call.getArguments();
			if (args == null || args.length < 0)
				return null;
			int argIndex = args.length - 1;

			// Find the type node that is used for this expansion.
			IType targetType = QtFunctionCallUtil.getTargetType(call, args, argIndex);
			if (targetType == null)
				return null;

			// Returns completions for the given expansion using the given type as the
			// source for Qt methods.
			return getCompletionsFor(targetType, args[argIndex]);
		}

		return null;
	}

	public static Collection<ICompletionProposal> getProposals(ICEditorContentAssistInvocationContext context,
			IASTName name, IASTCompletionContext astContext, IASTNode astNode) {

		Collection<QObjectConnectCompletion> qtProposals = getConnectProposals(context, name, astContext, astNode);
		if (qtProposals == null || qtProposals.isEmpty())
			return null;

		Collection<ICompletionProposal> proposals = new ArrayList<>();
		for (QObjectConnectCompletion qtProposal : qtProposals)
			qtProposal.addProposals(proposals, context);
		return proposals;
	}
}
