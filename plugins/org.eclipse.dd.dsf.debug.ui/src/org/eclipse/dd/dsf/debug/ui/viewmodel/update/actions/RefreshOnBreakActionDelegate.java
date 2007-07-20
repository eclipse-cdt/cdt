/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.debug.ui.viewmodel.update.actions;

import org.eclipse.dd.dsf.debug.ui.viewmodel.update.VMCacheRefreshOnBreak;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCache;
import org.eclipse.dd.dsf.ui.viewmodel.update.actions.AbstractRefreshActionDelegate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;

@SuppressWarnings("restriction")
public class RefreshOnBreakActionDelegate extends AbstractRefreshActionDelegate 
{
	@Override
    public Object getContext()
	{
		return ((TreeModelViewer) ((AbstractDebugView) fView).getViewer()).getPresentationContext();
	}
	
	@Override
    public VMCache createCache()
	{
		return new VMCacheRefreshOnBreak();
	}
}
