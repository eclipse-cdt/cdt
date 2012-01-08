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

import org.eclipse.core.runtime.Assert;

import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;

/**
 * Internal implementation of IDocCommentOwner
 */
public class DocCommentOwner implements IDocCommentOwner {
	private final String id;
	private final String name;
	private final IDocCommentViewerConfiguration multi, single;
	
	public DocCommentOwner(String id, String name, IDocCommentViewerConfiguration multi, IDocCommentViewerConfiguration single) {
		Assert.isNotNull(id); Assert.isNotNull(name); Assert.isNotNull(multi); Assert.isNotNull(single);
		this.id= id;
		this.name= name;
		this.multi= multi;
		this.single= single;
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
