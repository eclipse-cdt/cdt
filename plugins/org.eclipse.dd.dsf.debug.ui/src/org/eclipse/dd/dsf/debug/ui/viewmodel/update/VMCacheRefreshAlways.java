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

package org.eclipse.dd.dsf.debug.ui.viewmodel.update;

import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCache;

public class VMCacheRefreshAlways extends VMCache  
{
	public VMCacheRefreshAlways()
	{
		super();
	}
	
	public VMCacheRefreshAlways(VMCache oldCache)
	{
		super(oldCache);
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public void handleEvent(IDMEvent event) {
		if(event instanceof IRunControl.ISuspendedDMEvent)
			flush(true);
	}
}
