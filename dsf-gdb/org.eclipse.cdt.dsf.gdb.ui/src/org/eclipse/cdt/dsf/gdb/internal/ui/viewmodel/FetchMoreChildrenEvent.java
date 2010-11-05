/*******************************************************************************
 * Copyright (c) 2010 Verigy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Event to fetch additional children for and expression context.
 * 
 * @since 3.0
 */
public class FetchMoreChildrenEvent extends AbstractDMEvent<IExpressionDMContext> {
    
	private TreePath path;
	
    public FetchMoreChildrenEvent(IExpressionDMContext exprCtx, TreePath path) {
        super(exprCtx);
        this.path = path;
    }
    
    public TreePath getPath() {
    	return path;
    }
}
