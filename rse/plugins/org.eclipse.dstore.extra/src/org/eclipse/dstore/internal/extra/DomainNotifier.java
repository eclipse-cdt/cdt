/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM) - [282634] [dstore] IndexOutOfBoundsException on Disconnect
 *******************************************************************************/

package org.eclipse.dstore.internal.extra;

import java.util.ArrayList;

import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.dstore.extra.IDomainNotifier;

public class DomainNotifier implements IDomainNotifier
{


	private ArrayList _listeners;

	private boolean _enabled;

	public DomainNotifier()
	{
		_listeners = new ArrayList();
		_enabled = false;
	}

	

	public void enable(boolean on)
	{
		_enabled = on;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public void addDomainListener(IDomainListener listener)
	{
		synchronized (_listeners){
			if (!_listeners.contains(listener))
			{
				_listeners.add(listener);
			}
		}
	}

	
	public void fireDomainChanged(DomainEvent event)
	{
		if (_enabled)
		{
			Object[] listeners = null;
			
			synchronized (_listeners){
				listeners = _listeners.toArray();
			}
			
			for (int i = 0; i < listeners.length; i++)
			{
				IDomainListener listener = (IDomainListener)listeners[i];
				if ((listener != null) && listener.listeningTo(event))
				{
					listener.domainChanged(event);
				}
			}	
		}
	}

	public boolean hasDomainListener(IDomainListener listener)
	{
		synchronized (_listeners){
			return _listeners.contains(listener);
		}
	}

	public void removeDomainListener(IDomainListener listener)
	{
		synchronized (_listeners){
			_listeners.remove(listener);
		}
	}
}
