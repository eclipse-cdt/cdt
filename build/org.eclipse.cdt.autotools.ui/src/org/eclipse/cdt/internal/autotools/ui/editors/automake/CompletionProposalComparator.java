/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;


import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class CompletionProposalComparator implements Comparator<ICompletionProposal>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(ICompletionProposal o1, ICompletionProposal o2) {
		return o1.getDisplayString().compareToIgnoreCase(o2.getDisplayString());
	}	
	
}
