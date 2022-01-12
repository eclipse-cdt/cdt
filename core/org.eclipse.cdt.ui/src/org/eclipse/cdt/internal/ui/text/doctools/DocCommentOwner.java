/*******************************************************************************
 * Copyright (c) 2008, 2012 Symbian Software Systems and others.
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
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;
import org.eclipse.core.runtime.Assert;

/**
 * Internal implementation of IDocCommentOwner
 */
public class DocCommentOwner implements IDocCommentOwner {
	private final String id;
	private final String name;
	private final IDocCommentViewerConfiguration multi, single;

	public DocCommentOwner(String id, String name, IDocCommentViewerConfiguration multi,
			IDocCommentViewerConfiguration single) {
		Assert.isNotNull(id);
		Assert.isNotNull(name);
		Assert.isNotNull(multi);
		Assert.isNotNull(single);
		this.id = id;
		this.name = name;
		this.multi = multi;
		this.single = single;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.ICCommentOwner#getID()
	 */
	@Override
	public String getID() {
		return id;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.ICCommentOwner#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.ICCommentOwner#getMultilineConfiguration()
	 */
	@Override
	public IDocCommentViewerConfiguration getMultilineConfiguration() {
		return multi;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.ICCommentOwner#getSinglelineConfiguration()
	 */
	@Override
	public IDocCommentViewerConfiguration getSinglelineConfiguration() {
		return single;
	}
}
