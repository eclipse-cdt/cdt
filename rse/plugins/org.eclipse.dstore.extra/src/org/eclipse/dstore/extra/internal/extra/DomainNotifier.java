/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.dstore.extra.internal.extra;

import java.util.*;

public class DomainNotifier implements IDomainNotifier
{


	private ArrayList _listeners;

	private boolean _enabled;
	
/*
	public class FireMainThread extends Job
	{
		public boolean _isWorking;

		private DomainEvent _event;
		
		public FireMainThread(DomainEvent event)
		{
			super("DStore Events Fired");
			_isWorking = false;
			_event = event;
			setPriority(Job.INTERACTIVE);
		}

		public IStatus run(IProgressMonitor monitor)
		{
			_isWorking = true;

			if (_event.getType() != DomainEvent.FILE_CHANGE)
			{
				for (int i = 0; i < _listeners.size(); i++)
				{
					IDomainListener listener = (IDomainListener) _listeners.get(i);
					if ((listener != null) && listener.listeningTo(_event))
					{
						listener.domainChanged(_event);
					}
				}
			}

			_isWorking = false;
			return Status.OK_STATUS;
		}
	}
*/
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
		if (!_listeners.contains(listener))
		{
			_listeners.add(listener);
		}
	}

	
	public void fireDomainChanged(DomainEvent event)
	{
		if (_enabled)
		{
			for (int i = 0; i < _listeners.size(); i++)
			{
				IDomainListener listener = (IDomainListener) _listeners.get(i);
				if ((listener != null) && listener.listeningTo(event))
				{
					listener.domainChanged(event);
				}
			}
			//FireMainThread fireJob = new FireMainThread(event);
			//fireJob.schedule();
		}
	}

	public boolean hasDomainListener(IDomainListener listener)
	{
		return _listeners.contains(listener);
	}

	public void removeDomainListener(IDomainListener listener)
	{
		_listeners.remove(listener);
	}
}