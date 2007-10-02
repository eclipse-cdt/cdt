/**
 * Copyright 2007 ARM Limited. All rights reserved.
 *
 *    $ Rev: $
 * $ Author: $
 *   $ Date: $
 *    $ URL: $
 */
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
