/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.ui.text.contentassist;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.jface.text.ITextViewer;

public interface ICompletionContributor {

	/**
	 * This method allows the contributor to add to the list of proposals
	 * 
	 * @param viewer the text viewer where completion is occuring
	 * @param offset the offset into the text where the completion is occuring
	 * @param completionNode the completion node produced by the parser for the offset
	 * @param proposals the current list of proposals. This method should any additional
	 * proposals to this list.
	 */
	void contributeCompletionProposals(ITextViewer viewer,
									   int offset,
									   ASTCompletionNode completionNode,
									   List proposals);
	
}
