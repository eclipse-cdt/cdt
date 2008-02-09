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

public final class NullDocCommentOwner extends DocCommentOwner {
	private static final String ID = "org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner"; //$NON-NLS-1$
	public static final IDocCommentOwner INSTANCE= new NullDocCommentOwner();
	private NullDocCommentOwner() {
		super(
			ID,
			Messages.NullDocCommentOwner_Name,
			NullDocCommentViewerConfiguration.INSTANCE,
			NullDocCommentViewerConfiguration.INSTANCE
			);
	}
}
