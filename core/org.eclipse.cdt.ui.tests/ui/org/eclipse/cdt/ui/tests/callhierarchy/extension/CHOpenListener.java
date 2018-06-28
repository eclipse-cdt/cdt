/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lidia Popescu - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeSelection;

public class CHOpenListener implements IOpenListener {

	/** On Node click open corresponding file */
	@Override
	public void open(OpenEvent event) {
		if (event !=null ) {
			ISelection selection = event.getSelection();
			if (selection !=null && selection instanceof TreeSelection ) {
				TreeSelection treeSelection = (TreeSelection)selection;
				Object element = treeSelection.getFirstElement();
				if (element !=null && element instanceof DslNode) {
					DslNode node = (DslNode)element;
//					Suppose to open the file
				}
			}
		}
	}

}
