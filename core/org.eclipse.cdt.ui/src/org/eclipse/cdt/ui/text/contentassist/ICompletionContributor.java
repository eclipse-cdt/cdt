/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.ui.text.contentassist;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public interface ICompletionContributor {

	ICompletionProposal[] contributeCompletionProposals(ITextViewer viewer, int offset, ASTCompletionNode node);
	
}
