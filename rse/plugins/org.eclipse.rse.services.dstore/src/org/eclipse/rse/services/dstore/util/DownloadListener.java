/********************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * David McKnight   (IBM)        - [162195] new APIs for upload multi and download multi
 * David McKnight   (IBM)        - [197480] eliminating UI dependencies
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 * Martin Oberhuber (Wind River) - [219952] Use MessageFormat for download progress message
 * David McKnight   (IBM)        - [222448] [dstore] update DownloadListener to handle timeouts and nudge
 * David McKnight   (IBM)        - [225902] [dstore] use C_NOTIFICATION command to wake up the server
 * David McKnight   (IBM)        - [231126] [dstore] status monitor needs to reset WaitThreshold on nudge
 * David McKnight   (IBM)        - [267478] [dstore] Invalid thread access thrown calling the DStoreFileService.download method
 ********************************************************************************/

package org.eclipse.rse.services.dstore.util;



import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.rse.internal.services.dstore.ServiceResources;

public class DownloadListener implements IDomainListener
{

	private DataElement _status;
	private IProgressMonitor _monitor;
	private DataStore _dataStore;
	private File _localFile;

	private boolean _networkDown = false;
	private boolean _isDone = false;
	private boolean _isCancelled = false;
	private long _totalBytesNotified = 0;
	private long _totalLength;

	public DownloadListener(DataElement status, File localFile, String remotePath, long totalLength, IProgressMonitor monitor)
	{
		_monitor = monitor;
		_status = status;
		_totalLength = totalLength;

		if (_status == null)
		{
		    System.out.println("Status is null!"); //$NON-NLS-1$
		}

		_dataStore = _status.getDataStore();
		_dataStore.getDomainNotifier().addDomainListener(this);

		_localFile = localFile;

		if (monitor != null)
		{
			/* DKM - DO WE NEED THIS?!!
			while (_display!=null && _display.readAndDispatch()) {
				//Process everything on event queue
			}
			*/
		}
		if (_status.getValue().equals("done")) //$NON-NLS-1$
		{
			updateDownloadState();
			setDone(true);
		}
	}

	/** @since 3.0 */
	public long getTotalLength()
	{
		return _totalLength;
	}

	public boolean isCancelled()
	{
		return _isCancelled;
	}

	/** @since 3.0 */
	public boolean isDone()
	{
		return _isDone;
	}

	public DataElement getStatus()
	{
		return _status;
	}

	/**
	 * @see IDomainListener#listeningTo(DomainEvent)
	 */
	public boolean listeningTo(DomainEvent event)
	{
		if (_status == null)
		{
			return false;
		}

		if (_status == event.getParent())
		{
			return true;
		}

		return false;
	}

	/**
	 * @see IDomainListener#domainChanged(DomainEvent)
	 */
	public void domainChanged(DomainEvent event)
	{
		if (_status.getValue().equals("done")) //$NON-NLS-1$
		{
			if (_status == event.getParent())
			{
				setDone(true);
			}
		}
		else
		{
			updateDownloadState();
		}
	}

	private void updateDownloadState()
	{

		if (_monitor != null)
		{
			long currentLength =  _localFile.length();
			long delta = currentLength - _totalBytesNotified;
			if (delta > 0)
			{
				try { // certain progress monitors can't do work when not on main thread
					_monitor.worked((int)delta);
				}
				catch (Exception e){					
				}

				try
				{
					double percent = (currentLength * 1.0) / _totalLength;
					String str = MessageFormat.format(
						ServiceResources.DStore_Service_Percent_Complete_Message,
						new Object[] {
							new Long(currentLength/1024),
							new Long(_totalLength/1024),
							new Double(percent)
						});

					_monitor.subTask(str);

					/* DKM - DO WE NEED THIS?!!
					while (_display != null && _display.readAndDispatch()) {
						//Process everything on event queue
					}
					*/
				}
				catch (Exception e)
				{
				}
				_totalBytesNotified = currentLength;
			}
		}

		if (!_status.getDataStore().getStatus().getName().equals("okay")) //$NON-NLS-1$
		{
			_networkDown = true;
		}
	}

	/**
	 * setDone(boolean)
	 */
	public void setDone(boolean done)
	{
		this._isDone = done;
		if (done)
		{
			updateDownloadState();
			_status.getDataStore().getDomainNotifier().removeDomainListener(this);

		}
	}



	/**
	 *
	 */
	public boolean wasCancelled()
	{
		return _isCancelled;
	}



	/**
     * Wait for the the status DataElement to be refreshed
     *
     * @return The status DataElement after it has been updated, or the user
     *         has pressed cancel
     *
     * @throws InterruptedException if the thread was interrupted.
     */
	public DataElement waitForUpdate() throws InterruptedException
	{
		return waitForUpdate(0); //No diagnostic
	}

	/**
	 * Wait for the the status DataElement to be refreshed
	 *
	 * @param wait threshold for starting diagnostic. Default is 60 seconds; a zero means to use the default.
	 *             -1 means to force a timeout; mainly for testing purpose.
	 *
	 * @return The status DataElement after it has been updated, or the user
	 *         has pressed cancel
	 *
	 * @throws InterruptedException if the thread was interrupted.
	 */
	public DataElement waitForUpdate(int wait) throws InterruptedException
	{
		// Prevent infinite looping by introducing a threshold for wait

		int WaitThreshold = 50;

		if (wait > 0)
			WaitThreshold = wait * 10; // 1 second means 10 sleep(100ms)
		else if (wait == -1) // force a diagnostic
			WaitThreshold = -1;

		int initialWaitTheshold = WaitThreshold;
		{
			// Current thread is not UI thread
			while (!_isDone && !_isCancelled && !_networkDown)
			{
				if ((_monitor != null) && (_monitor.isCanceled()))
				{
					cancelDownload();
					_isCancelled = true;
					setDone(true);
				}
				else if (_networkDown)
				{
					_isCancelled = true;
					setDone(true);
					throw new InterruptedException();
				}
				if (getStatus().getAttribute(DE.A_NAME).equals("done")) //$NON-NLS-1$
				{
					setDone(true);
				}
				else
				{
				    Thread.sleep(100);
					updateDownloadState();

					if (WaitThreshold > 0) // update timer count if
					 {
	                        // threshold not reached
	                        --WaitThreshold; // decrement the timer count
					 }
					 else if (WaitThreshold == 0)
					 {
						 // try to wake up the server
						 wakeupServer(_status);
						 WaitThreshold = initialWaitTheshold;
					 }
				}
			}
		}
		return _status;
	}


	private void wakeupServer(DataElement status)
	{
		if (status != null)
		{
			// token command to wake up update handler
			DataElement cmdDescriptor = _dataStore.findCommandDescriptor(DataStoreSchema.C_NOTIFICATION);
			DataElement subject = status.getParent().get(0);
			if (cmdDescriptor != null)
			{
				_dataStore.command(cmdDescriptor, subject);
			}
		}
	}

	public void cancelDownload()
		{
			DataElement status = _status;
			if (status != null)
			{
				DataElement command = status.getParent();
				DataStore dataStore = command.getDataStore();
				DataElement cmdDescriptor = command.getDescriptor();
				DataElement cancelDescriptor = dataStore.localDescriptorQuery(cmdDescriptor, "C_CANCEL"); //$NON-NLS-1$
				if (cancelDescriptor != null)
				{
					dataStore.command(cancelDescriptor, command);
				}
				_localFile.delete();
			}
			if (_monitor != null)
			{
				_monitor.done();
			}
		}
}