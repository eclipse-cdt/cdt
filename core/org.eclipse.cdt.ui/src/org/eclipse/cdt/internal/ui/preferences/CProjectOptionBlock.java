/*
 * Created on 7-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;

public class CProjectOptionBlock extends TabFolderOptionBlock {

	public CProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
	}

	protected void addTabs() {
		addTab(new IndexerBlock());
	}

}
