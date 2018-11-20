/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.ui;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.ui.assist.QPropertyExpansion;
import org.eclipse.cdt.internal.qt.ui.assist.QtProposalContext;
import org.eclipse.cdt.internal.qt.ui.assist.QtTemplateProposal;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

@SuppressWarnings("restriction")
public class QPropertyCompletion {

	private static final String CONTEXT_ID = Activator.PLUGIN_ID + ".proposal.Q_PROPERTY";

	private static final Template QPropertyTemplate = new Template("Q_PROPERTY", "Q_PROPERTY declaration", CONTEXT_ID,
			"Q_PROPERTY( ${type} ${name} READ ${accessor} ${cursor} )", true);

	public static Collection<ICompletionProposal> getAttributeProposals(
			ICEditorContentAssistInvocationContext context) {
		QPropertyExpansion expansion = QPropertyExpansion.create(context);
		return expansion == null ? Collections.<ICompletionProposal>emptyList()
				: expansion.getProposals(CONTEXT_ID, context);
	}

	public static Collection<ICompletionProposal> getProposals(ICEditorContentAssistInvocationContext context,
			IASTName name, IASTCompletionContext astContext, IASTNode astNode) {

		String token = name.getLastName().toString();
		if (token.isEmpty() || !QtKeywords.Q_PROPERTY.startsWith(token))
			return Collections.emptyList();

		TemplateContextType ctxType = new CContextType();
		ctxType.setId(CONTEXT_ID);

		QtProposalContext templateCtx = new QtProposalContext(context, ctxType);
		Region region = new Region(templateCtx.getCompletionOffset(), templateCtx.getCompletionLength());

		return Collections
				.<ICompletionProposal>singletonList(new QtTemplateProposal(QPropertyTemplate, templateCtx, region));
	}
}
