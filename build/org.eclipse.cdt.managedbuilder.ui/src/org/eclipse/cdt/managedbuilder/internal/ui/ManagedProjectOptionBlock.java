package org.eclipse.cdt.managedbuilder.internal.ui;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;

public class ManagedProjectOptionBlock extends TabFolderOptionBlock {

	ErrorParserBlock errParserBlock;
	
	/**
	 * @param parent
	 */
	public ManagedProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
	 */
	protected void addTabs() {
		errParserBlock = new ErrorParserBlock();
		addTab(errParserBlock);
	}

	public ErrorParserBlock getErrorParserBlock() {
		return errParserBlock;
	}
	
}
