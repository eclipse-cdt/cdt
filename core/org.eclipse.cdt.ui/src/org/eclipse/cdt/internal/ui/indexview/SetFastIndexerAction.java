/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Sets all selected actions to use the Fast indexer.
 * 
 * @author dschaefer
 */
public class SetFastIndexerAction extends IndexAction {

	public SetFastIndexerAction(TreeViewer viewer) {
		super(viewer, CUIPlugin.getResourceString("IndexView.setFastIndexer.name")); //$NON-NLS-1$
	}

	public void run() {
		try {
			IIndexManager manager = CCorePlugin.getIndexManager();
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			for (int i = 0; i < projects.length; ++i) {
				manager.setIndexerId(projects[i], IPDOMManager.ID_FAST_INDEXER);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	public boolean valid() {
		return true;
	}

}
