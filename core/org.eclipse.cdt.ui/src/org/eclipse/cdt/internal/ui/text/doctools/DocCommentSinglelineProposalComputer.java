/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;

public class DocCommentSinglelineProposalComputer extends AbstractDocCommentProposalComputer {
	@Override
	protected IDocCommentViewerConfiguration getConfiguration(IDocCommentOwner owner) {
		return owner.getSinglelineConfiguration();
	}
}
