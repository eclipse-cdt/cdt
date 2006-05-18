/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Doug Schaefer
 *
 */
public class RebuildIndexAction extends IndexAction {
	
	public RebuildIndexAction(TreeViewer viewer) {
		super(viewer, CUIPlugin.getResourceString("IndexView.rebuildIndex.name")); //$NON-NLS-1$
	}
	
	public void run() {
		CCorePlugin.getPDOMManager().reindex();
	}
	
	public boolean valid() {
		return true;
	}

}
