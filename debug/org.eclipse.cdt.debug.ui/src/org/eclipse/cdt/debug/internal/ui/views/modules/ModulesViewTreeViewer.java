/*******************************************************************************
 * Copyright (c) 2007 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ARM Limited - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules;

import org.eclipse.debug.internal.ui.viewers.model.TreeModelContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * org.eclipse.cdt.debug.internal.ui.views.modules.ModulesViewTreeViewer: 
 * //TODO Add description.
 */
public class ModulesViewTreeViewer extends TreeModelViewer {

	public ModulesViewTreeViewer( Composite parent, int style, IPresentationContext context ) {
		super( parent, style, context );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer#createContentProvider()
	 */
	protected TreeModelContentProvider createContentProvider() {
		return new ModulesViewTreeContentProvider();
	}
}
