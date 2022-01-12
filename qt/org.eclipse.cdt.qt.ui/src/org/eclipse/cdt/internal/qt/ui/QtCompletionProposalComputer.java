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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.internal.ui.text.contentassist.ParsingBasedProposalComputer;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

@SuppressWarnings("restriction")
public class QtCompletionProposalComputer extends ParsingBasedProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		// this is overridden in order to find proposals when the completion node is null
		try {
			if (context instanceof CContentAssistInvocationContext) {
				CContentAssistInvocationContext cContext = (CContentAssistInvocationContext) context;

				String prefix = null;
				IASTCompletionNode completionNode = cContext.getCompletionNode();
				// the parent implementation gives up when this condition is false
				if (completionNode != null)
					prefix = completionNode.getPrefix();

				if (prefix == null)
					prefix = cContext.computeIdentifierPrefix().toString();

				return computeCompletionProposals(cContext, completionNode, prefix);
			}
		} catch (Exception e) {
			Activator.log(e);
		}

		return Collections.emptyList();
	}

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

		return QtNature.hasNature(project);
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) throws CoreException {

		// make sure this is a Qt project
		if (!isApplicable(context))
			return Collections.emptyList();

		List<ICompletionProposal> proposals = null;

		if (completionNode != null) {
			IASTName[] names = completionNode.getNames();
			for (IASTName name : names) {
				// the node isn't properly hooked up, must have backtracked out of this node
				if (name.getTranslationUnit() == null)
					continue;

				IASTCompletionContext astContext = name.getCompletionContext();
				if (astContext == null || !(astContext instanceof IASTNode))
					continue;
				IASTNode astNode = (IASTNode) astContext;

				proposals = addAll(proposals,
						QObjectConnectCompletion.getProposals(context, name, astContext, astNode));
				proposals = addAll(proposals, QObjectDeclarationCompletion.getProposals(context, name));
				proposals = addAll(proposals, QPropertyCompletion.getProposals(context, name, astContext, astNode));
			}
		}

		// Attributes within Q_PROPERTY declarations
		proposals = addAll(proposals, QPropertyCompletion.getAttributeProposals(context));

		return proposals == null ? Collections.<ICompletionProposal>emptyList() : proposals;
	}

	private static <T> List<T> addAll(List<T> list, Collection<T> toAdd) {
		if (toAdd == null || toAdd.isEmpty())
			return list;

		if (list == null)
			return new ArrayList<>(toAdd);

		list.addAll(toAdd);
		return list;
	}
}
