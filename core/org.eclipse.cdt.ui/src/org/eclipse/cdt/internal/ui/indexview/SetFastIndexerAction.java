/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX software Systems - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.indexer.fast.PDOMFastIndexer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;

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
			IPDOMManager manager = CCorePlugin.getPDOMManager();
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			for (int i = 0; i < projects.length; ++i) {
				try {
					manager.setIndexerId(projects[i], PDOMFastIndexer.ID);
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	public boolean valid() {
		return true;
	}

}
