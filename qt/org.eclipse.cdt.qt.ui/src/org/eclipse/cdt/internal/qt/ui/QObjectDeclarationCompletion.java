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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.corext.template.c.TranslationUnitContext;
import org.eclipse.cdt.internal.corext.template.c.TranslationUnitContextType;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine.CTemplateProposal;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class QObjectDeclarationCompletion {

	private static final String TEMPLATE = "class ${name} : public ${QObject}\n{\nQ_OBJECT\n\n${cursor}\n};";

	private final static TranslationUnitContextType context;
	static {
		context = new CContextType();
		context.setId(CContextType.ID);
	}

	public static Collection<ICompletionProposal> getProposals(ICEditorContentAssistInvocationContext ctx,
			IASTName name) {

		String token = name.getLastName().toString();
		if (token.isEmpty() || !"class".startsWith(token))
			return null;

		Position position = getPosition(ctx);
		if (position == null)
			return null;

		TranslationUnitContext tuCtx = context.createContext(ctx.getDocument(), position, ctx.getTranslationUnit());
		IRegion region = new Region(position.getOffset(), position.getLength());

		Template template = new Template("class", "QObject declaration", CContextType.ID, TEMPLATE, true);
		return Collections.<ICompletionProposal>singletonList(
				new CTemplateProposal(template, tuCtx, region, Activator.getQtLogo()));
	}

	private static Position getPosition(ICEditorContentAssistInvocationContext context) {
		ITextEditor textEditor = context.getEditor().getAdapter(ITextEditor.class);
		if (textEditor == null)
			return null;

		ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
		if (selectionProvider == null)
			return null;

		ISelection selection = selectionProvider.getSelection();
		if (!(selection instanceof ITextSelection))
			return null;

		ITextSelection text = (ITextSelection) selection;
		return new Position(text.getOffset(), text.getLength());
	}
}
