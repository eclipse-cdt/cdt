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

package org.eclipse.dd.dsf.debug.ui.viewmodel.dm;

import org.eclipse.dd.dsf.debug.ui.viewmodel.update.VMCacheRefreshAlways;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProviderWithCache;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;



/*
 * The purpose of this class is to satisfy package structure requirements while enabling VM update modes.
 * The non-debug centric VMCacheManager and VMCache live under org.eclipse.dd.dsf.*. The debug data view
 * caches (VMCacheRefreshAlways/Manual/OnBreak) live under org.eclipse.dd.dsf.debug.* because of their 
 * awareness of debug specific events. There is a need to instantiate a default (always) cache on view
 * startup. AbstractDMVMProviderWithCache would be a good place to accomplish this task, but like the 
 * VMCacheManager, this class cannot access the *dsf.debug* VMCacheRefreshAlways. AbstractDebugDMVMProviderWithCache
 * is meant to solve this problem.
 */

@SuppressWarnings("restriction")
public class AbstractDebugDMVMProviderWithCache extends
		AbstractDMVMProviderWithCache 
{

	public AbstractDebugDMVMProviderWithCache(AbstractVMAdapter adapter,  IPresentationContext presentationContext, DsfSession session) {
		super(adapter, presentationContext, session);
		
		VMCacheManager.getVMCacheManager().registerCache(presentationContext, new VMCacheRefreshAlways());
	}
	
}
