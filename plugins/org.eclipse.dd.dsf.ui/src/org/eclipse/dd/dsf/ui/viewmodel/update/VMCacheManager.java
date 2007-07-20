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

package org.eclipse.dd.dsf.ui.viewmodel.update;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.dd.dsf.datamodel.IDMEvent;


public class VMCacheManager
{
	private static VMCacheManager fInstance = null;
	
	private HashMap<Object, VMCache> fAssociations = new HashMap<Object, VMCache>();
	
	public interface CacheListener
	{
		public void cacheFlushed(Object context);
	}
	
	private HashMap<Object, Vector<CacheListener>> fListeners = new HashMap<Object, Vector<CacheListener>>();

	public VMCacheManager()
	{
	}
	
	public static VMCacheManager getVMCacheManager()
	{
		if(fInstance == null)
			fInstance = new VMCacheManager();
		
		return fInstance;
	}

	public VMCache getCache(Object context)
	{
		if(!fAssociations.containsKey(context))
			fAssociations.put(context, new VMCache()
			{
				@SuppressWarnings("unchecked")
                @Override
				public void handleEvent(IDMEvent event) {
				}
				
				@Override
				public boolean isCacheReadEnabled()
				{
					return false;
				}
				
				@Override
				public boolean isCacheWriteEnabled()
				{
					return false;
				}
			}); 
			
		return fAssociations.get(context);
	}
	
	public void registerCache(Object context, VMCache cache)
	{
		fAssociations.put(context, cache);
	}
	
	public void addCacheListener(Object context, CacheListener listener)
	{
		if(!fListeners.containsKey(context))
			fListeners.put(context, new Vector<CacheListener>());
		
		fListeners.get(context).addElement(listener);
	}
	
	public void removeCacheListener(Object context, CacheListener listener)
	{
		if(!fListeners.containsKey(context))
		{
			fListeners.get(context).removeElement(listener);
			if(fListeners.get(context).isEmpty())
				fListeners.remove(context);
		}
	}
	
	private void fireCacheFlushed(Object context)
	{
		if(fListeners.containsKey(context))
		{
			for(CacheListener listener : fListeners.get(context))
				listener.cacheFlushed(context);
		}
	}

	public void flush(Object context)
	{
		getCache(context).flush(false);
		fireCacheFlushed(context);
	}
	
}



