/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
 
/**
 * The modules viewer used by the Modules view.
 */
public class ModulesViewer extends TreeViewer {

	/** 
	 * Constructor for ModulesViewer. 
	 */
	public ModulesViewer( Composite parent ) {
		super( parent );
	}

	/** 
	 * Constructor for ModulesViewer. 
	 */
	public ModulesViewer( Composite parent, int style ) {
		super( parent, style );
	}

	/** 
	 * Constructor for ModulesViewer. 
	 */
	public ModulesViewer( Tree tree ) {
		super( tree );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {
		super.refresh();
		ISelection selection = getSelection();
        if ( !selection.isEmpty() ) {
			setSelection( selection );
		}
	}
}
