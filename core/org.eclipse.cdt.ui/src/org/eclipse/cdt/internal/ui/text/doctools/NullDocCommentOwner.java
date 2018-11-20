/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;

public final class NullDocCommentOwner extends DocCommentOwner {
	private static final String ID = "org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner"; //$NON-NLS-1$
	public static final IDocCommentOwner INSTANCE = new NullDocCommentOwner();

	private NullDocCommentOwner() {
		super(ID, Messages.NullDocCommentOwner_Name, NullDocCommentViewerConfiguration.INSTANCE,
				NullDocCommentViewerConfiguration.INSTANCE);
	}
}
