/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.ui.text.contentassist;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.jface.text.ITextViewer;

/**
 * This interface must be implemented by clients extending the extension point
 * <tt>org.eclipse.cdt.core.completionContributors</tt>.
 *
 * @deprecated Clients should extend the new extension point
 *             <tt>completionProprosalComputer</tt> and implement interface
 *             {@link ICompletionProposalComputer}
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ICompletionContributor {

	/**
	 * This method allows the contributor to add to the list of proposals
	 *
	 * @param viewer the text viewer where completion is occuring
	 * @param offset the offset into the text where the completion is occuring
	 * @param completionNode the completion node produced by the parser for the offset
	 * @param proposals the current list of proposals. This method should add any additional
	 * proposals to this list.
	 */
	@SuppressWarnings("rawtypes") // no need to change, it's deprecated
	void contributeCompletionProposals(ITextViewer viewer, int offset, IWorkingCopy workingCopy,
			ASTCompletionNode completionNode, String prefix, List proposals);

}
