/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.internal.ui.text.contentassist.CompletionProposalCategory;
import org.eclipse.cdt.internal.ui.text.contentassist.CompletionProposalComputerRegistry;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A content assist executor can invoke content assist for a specific proposal category on an editor.
 *
 * @since 4.0
 */
public final class SpecificContentAssistExecutor {

	private final CompletionProposalComputerRegistry fRegistry;

	/**
	 * Creates a new executor.
	 *
	 * @param registry the computer registry to use for the enablement of proposal categories
	 */
	public SpecificContentAssistExecutor(CompletionProposalComputerRegistry registry) {
		Assert.isNotNull(registry);
		fRegistry = registry;
	}

	/**
	 * Invokes content assist on <code>editor</code>, showing only proposals computed by the
	 * <code>CompletionProposalCategory</code> with the given <code>categoryId</code>.
	 *
	 * @param editor the editor to invoke code assist on
	 * @param categoryId the id of the proposal category to show proposals for
	 */
	public void invokeContentAssist(final ITextEditor editor, String categoryId) {
		Collection<CompletionProposalCategory> categories = fRegistry.getProposalCategories();
		boolean[] inclusionState = new boolean[categories.size()];
		boolean[] separateState = new boolean[categories.size()];
		int i = 0;
		for (Iterator<CompletionProposalCategory> it = categories.iterator(); it.hasNext(); i++) {
			CompletionProposalCategory cat = it.next();
			inclusionState[i] = cat.isIncluded();
			cat.setIncluded(cat.getId().equals(categoryId));
			separateState[i] = cat.isSeparateCommand();
			cat.setSeparateCommand(false);
		}

		try {
			ITextOperationTarget target = editor.getAdapter(ITextOperationTarget.class);
			if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS))
				target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		} finally {
			i = 0;
			for (Iterator<CompletionProposalCategory> it = categories.iterator(); it.hasNext(); i++) {
				CompletionProposalCategory cat = it.next();
				cat.setIncluded(inclusionState[i]);
				cat.setSeparateCommand(separateState[i]);
			}
		}
	}
}
