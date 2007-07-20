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

package org.eclipse.dd.dsf.ui.viewmodel.dm;

import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;

@SuppressWarnings("restriction")
public abstract class AbstractDMVMProviderWithCache extends AbstractDMVMProvider 
	implements VMCacheManager.CacheListener
{
	@DsfServiceEventHandler
	@Override
	public void eventDispatched(IDMEvent<?> event) {
		VMCacheManager.getVMCacheManager().getCache(getPresentationContext()).handleEvent(event);
		super.eventDispatched(event);
	}

	@SuppressWarnings("restriction")
	public void cacheFlushed(Object context) {
		if(getPresentationContext().equals(context))
			getModelProxy().fireModelChanged(new ModelDelta(getRootElement(),IModelDelta.CONTENT));
	}

    public AbstractDMVMProviderWithCache(AbstractVMAdapter adapter,  IPresentationContext presentationContext, DsfSession session) {
		super(adapter, presentationContext, session);
		
		VMCacheManager.getVMCacheManager().addCacheListener(getPresentationContext(), this);
	}
	
	@Override
	public void update(IHasChildrenUpdate[] updates) {
		super.update(VMCacheManager.getVMCacheManager().getCache(getPresentationContext()).update(updates));
    }
    
	@Override
    public void update(IChildrenCountUpdate[] updates) {
    	super.update(VMCacheManager.getVMCacheManager().getCache(getPresentationContext()).update(updates));
    }
    
    @Override
    public void update(final IChildrenUpdate[] updates) {
    	super.update(VMCacheManager.getVMCacheManager().getCache(getPresentationContext()).update(updates));
    }
    
    @Override
    public void dispose()
    {
    	VMCacheManager.getVMCacheManager().removeCacheListener(getPresentationContext(), this);
    	super.dispose();
    }
}
