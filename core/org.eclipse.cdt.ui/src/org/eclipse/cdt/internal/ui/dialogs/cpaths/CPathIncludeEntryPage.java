/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;

public class CPathIncludeEntryPage extends ExtendedCPathBasePage {

	public CPathIncludeEntryPage(ITreeListAdapter adapter) {
		super(adapter, "IncludeEntryPage"); //$NON-NLS-1$
	}

	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_INCLUDE;
	}

	protected void addContributed() {
		// dinglis-TODO Auto-generated method stub
	}

	protected void addFromWorkspace() {
		// dinglis-TODO Auto-generated method stub

	}

	protected void addPath() {
		// dinglis-TODO Auto-generated method stub

	}
}