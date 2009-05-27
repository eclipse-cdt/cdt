/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [197848] Fix shell terminated state when remote dies
 * Martin Oberhuber (Wind River) - [217429] Make registering multiple output listeners thread-safe
 * David McKnight   (IBM)        - [272421] AbstractHostShellOutputReader does not generate notification when error stream is closed
 *******************************************************************************/

package org.eclipse.rse.services.shells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractHostShellOutputReader  extends Thread implements IHostShellOutputReader
{
	protected List _listeners;
	protected int _waitIncrement = 2;
	protected boolean _keepRunning = true;

	protected List _linesOfOutput;
	protected int  _consumerOffset;
	protected IHostShell _hostShell;
	protected boolean _isErrorReader = false;


	protected long _timeOfLastEvent = 0;
	protected int _sizeAtLastEvent = 0;

	public AbstractHostShellOutputReader(IHostShell hostShell, boolean isErrorReader)
	{
		_hostShell = hostShell;
		_listeners = Collections.synchronizedList(new ArrayList());
		_linesOfOutput = new ArrayList();
		_consumerOffset = 0;
		_isErrorReader = isErrorReader;
		_timeOfLastEvent = System.currentTimeMillis();
	}

	public boolean isErrorReader()
	{
		return _isErrorReader;
	}

	public IHostShell getHostShell()
	{
		return _hostShell;
	}

	public void setWaitTime(int value)
	{
		_waitIncrement = value;
	}

	public int getWaitTime()
	{
		return _waitIncrement;
	}


	public void handle()
	{
		IHostOutput line = internalReadLine();

		if (line != null)
		{
			addLine(line);
		}

		else
		{
			finish();
			_keepRunning = false;
		}

	}

	protected void addLine(IHostOutput line)
	{
		_linesOfOutput.add(line);
		int sizenow = _linesOfOutput.size();
		int deltaSize = sizenow - _sizeAtLastEvent;

		long timenow = System.currentTimeMillis();
		//if ((timenow - _timeOfLastEvent) > 10 || deltaSize > 2)
		{


			// notify listeners
			HostShellChangeEvent event = new HostShellChangeEvent(_hostShell, this, _sizeAtLastEvent, deltaSize);
			fireOutputChanged(event);
			_timeOfLastEvent = timenow;
			_sizeAtLastEvent = sizenow;
		}
	}

	/** @since 3.0 */
	protected final synchronized void startIfNotAlive() {
		if (!isAlive()) {
			start();
		}
	}

	public IHostOutput readLine()
	{
		if (!isAlive())
		{
			internalReadLine();
			startIfNotAlive();
		}
		return (IHostOutput)_linesOfOutput.get(_consumerOffset++);
	}

	public IHostOutput readLine(int lineNumber)
	{
		return (IHostOutput)_linesOfOutput.get(lineNumber);
	}



	public void setLineOffset(int lineNumber)
	{
		_consumerOffset = lineNumber;
	}
	public void addOutputListener(IHostShellOutputListener listener)
	{
		_listeners.add(listener);
		startIfNotAlive();
	}

	public void fireOutputChanged(IHostShellChangeEvent event)
	{
		for (int i = 0; i < _listeners.size(); i++)
		{
			IHostShellOutputListener listener = (IHostShellOutputListener)_listeners.get(i);
			listener.shellOutputChanged(event);
		}
		if (!_keepRunning)
			dispose();
	}

	public void dispose()
	{
		_listeners.clear();
	}

	public boolean isFinished()
	{
		return !_keepRunning;
	}

	public void finish()
	{
		if (_keepRunning)
		{
			_waitIncrement = 0;
			//dispose();
		}
	}

	public void run()
	{
		while (_keepRunning)
		{
			try
			{
				Thread.sleep(_waitIncrement);
				Thread.yield();
				handle();
			}
			catch (InterruptedException e)
			{
				finish();
				_keepRunning = false;
			}
		}

		//Bug 197848: Fire empty event as notification that we are done
		HostShellChangeEvent event = new HostShellChangeEvent(_hostShell, this, 0, 0);
		fireOutputChanged(event);
	}

	protected abstract IHostOutput internalReadLine();

}
