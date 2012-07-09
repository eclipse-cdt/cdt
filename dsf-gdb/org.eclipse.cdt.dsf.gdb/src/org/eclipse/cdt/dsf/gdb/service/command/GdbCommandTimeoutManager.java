/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Jason Litton (Sage Electronic Engineering, LLC) - Use Dynamic Tracing option (Bug 379169)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service.command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbDebugOptions;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * The command timeout manager registers itself as a command listener and monitors 
 * the command execution time. The goal of this implementation is to gracefully 
 * handle disruptions in the communication between Eclipse and GDB. 
 * 
 * The algorithm used by this class is based on the assumption that the command 
 * execution in GDB is sequential even though DSF can send up to 3 commands at 
 * a time to GDB (see {@link AbstractMIControl}).
 *  
 * @since 4.1
 */
public class GdbCommandTimeoutManager implements ICommandListener, IPreferenceChangeListener {

	public interface ICommandTimeoutListener {
		
		void commandTimedOut( ICommandToken token );
	}
	
	/**
	 * @deprecated The DEBUG flag is replaced with the GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS
	 */
	@Deprecated
	public final static boolean DEBUG = "true".equals( Platform.getDebugOption( "org.eclipse.cdt.dsf.gdb/debug/timeouts" ) ); //$NON-NLS-1$//$NON-NLS-2$
	
	private class QueueEntry {
		private long fTimestamp;
		private ICommandToken fCommandToken;
		
		private QueueEntry( long timestamp, ICommandToken commandToken ) {
			super();
			fTimestamp = timestamp;
			fCommandToken = commandToken;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals( Object obj ) {
			if ( obj instanceof QueueEntry ) {
				return fCommandToken.equals( ((QueueEntry)obj).fCommandToken );
			}
			return false;
		}
	}

	private enum TimerThreadState {
		INITIALIZING,
		RUNNING,
		HALTED,
		SHUTDOWN
	}

	private class TimerThread extends Thread {

		private BlockingQueue<QueueEntry> fQueue;
		private int fWaitTimeout = IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT;
		private TimerThreadState fState = TimerThreadState.INITIALIZING;

		TimerThread( BlockingQueue<QueueEntry> queue, int timeout ) {
			super();
			setName( "GDB Command Timer Thread" ); //$NON-NLS-1$
			fQueue = queue;
			setWaitTimout( timeout );
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			setTimerThreadState( ( getWaitTimeout() > 0 ) ? 
					TimerThreadState.RUNNING : TimerThreadState.HALTED );
			doRun();
		}

		private void doRun() {
			while ( getTimerThreadState() != TimerThreadState.SHUTDOWN ) {
				if ( getTimerThreadState() == TimerThreadState.HALTED ) {
					halted();
				}
				else {
					running();
				}
			}
		}

		private void halted() {
			fQueue.clear();
			try {
				synchronized( TimerThread.this ) {
					wait();
				}
			}
			catch( InterruptedException e ) {
			}
		}

		private void running() {
			try {
				while( getTimerThreadState() == TimerThreadState.RUNNING ) {
					// Use the minimum of all timeout values > 0 as the wait timeout.
					long timeout = getWaitTimeout();
					QueueEntry entry = fQueue.peek();
					if ( entry != null ) {
						// Calculate the time elapsed since the execution of this command started 
						// and compare it with the command's timeout value.
						// If the elapsed time is greater or equal than the timeout value the command 
						// is marked as timed out. Otherwise, schedule the next check when the timeout 
						// expires.
						long commandTimeout = getTimeoutForCommand( entry.fCommandToken.getCommand() );
						
						if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS ) {
							String commandText = entry.fCommandToken.getCommand().toString();
							if ( commandText.endsWith( "\n" ) ) //$NON-NLS-1$
								commandText = commandText.substring( 0, commandText.length() - 1 );

							printDebugMessage( String.format( "Processing command '%s', command timeout is %d", //$NON-NLS-1$
	commandText, Long.valueOf( commandTimeout ) ) );

						}

						long currentTime = System.currentTimeMillis();
						long elapsedTime = currentTime - entry.fTimestamp;
						if ( commandTimeout <= elapsedTime ) {
							processTimedOutCommand( entry.fCommandToken );
							fQueue.remove( entry );
							// Reset the timestamp of the next command in the queue because 
							// regardless how long the command has been in the queue GDB will 
							// start executing it only when the execution of the previous command 
							// is completed. 
							QueueEntry nextEntry = fQueue.peek();
							if ( nextEntry != null ) {
								setTimeStamp( currentTime, nextEntry );
							}
						}
						else {
							// Adjust the wait timeout because the time remaining for 
							// the current command to expire may be less than the current wait timeout. 
							timeout = Math.min( timeout, commandTimeout - elapsedTime );

							if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS ) {
								String commandText = entry.fCommandToken.getCommand().toString();
								if ( commandText.endsWith( "\n" ) ) //$NON-NLS-1$
									commandText = commandText.substring( 0, commandText.length() - 1 );
								
								printDebugMessage( String.format( "Setting timeout %d for command '%s'", Long.valueOf( timeout ), commandText ) ); //$NON-NLS-1$

							}
						}
					}
					synchronized( TimerThread.this ) {
						wait( timeout );
					}
				}
			}
			catch( InterruptedException e ) {
			}
		}

		private void shutdown() {
			setTimerThreadState( TimerThreadState.SHUTDOWN );
		}
		
		private synchronized void setWaitTimout( int waitTimeout ) {
			fWaitTimeout = waitTimeout;
			if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS )
				printDebugMessage( String.format( "Wait timeout is set to %d", Integer.valueOf( fWaitTimeout ) ) ); //$NON-NLS-1$
		}
		
		private synchronized int getWaitTimeout() {
			return fWaitTimeout;
		}
		
		private synchronized void setTimerThreadState( TimerThreadState state ) {
			fState = state;
			interrupt();
		}
		
		private synchronized TimerThreadState getTimerThreadState() {
			return fState;
		}
	}

	private static final String TIMEOUT_TRACE_IDENTIFIER = "[TMO]"; //$NON-NLS-1$

	private ICommandControl fCommandControl;
	private boolean fTimeoutEnabled = false;
	private int fTimeout = 0;
	private TimerThread fTimerThread;
	private BlockingQueue<QueueEntry> fCommandQueue = new LinkedBlockingQueue<QueueEntry>();
	private CustomTimeoutsMap fCustomTimeouts = new CustomTimeoutsMap();

	private ListenerList fListeners;
	
	public GdbCommandTimeoutManager( ICommandControl commandControl ) {
		fCommandControl = commandControl;
		fListeners = new ListenerList();
	}

	public void initialize() {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode( GdbPlugin.PLUGIN_ID );

		fTimeoutEnabled = Platform.getPreferencesService().getBoolean( 
				GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, 
				false,
				null );

		fTimeout = Platform.getPreferencesService().getInt( 
				GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, 
				IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT,
				null );
		
		fCustomTimeouts.initializeFromMemento( Platform.getPreferencesService().getString( 
				GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_COMMAND_CUSTOM_TIMEOUTS, 
				"", //$NON-NLS-1$
				null ) );
		
		node.addPreferenceChangeListener( this );
		
		fCommandControl.addCommandListener( this );
		
		fTimerThread = new TimerThread( fCommandQueue, calculateWaitTimeout() );
		fTimerThread.start();
	}

	public void dispose() {
		fTimerThread.shutdown();
		fListeners.clear();
		InstanceScope.INSTANCE.getNode( GdbPlugin.PLUGIN_ID ).removePreferenceChangeListener( this );
		fCommandControl.removeCommandListener( this );
		fCommandQueue.clear();
		fCustomTimeouts.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandQueued(org.eclipse.cdt.dsf.debug.service.command.ICommandToken)
	 */
	@Override
	public void commandQueued( ICommandToken token ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandSent(org.eclipse.cdt.dsf.debug.service.command.ICommandToken)
	 */
	@Override
	public void commandSent( ICommandToken token ) {
		if ( !isTimeoutEnabled() )
			return;
		int commandTimeout = getTimeoutForCommand( token.getCommand() );
		if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS ) {
			String commandText = token.getCommand().toString();
			if ( commandText.endsWith( "\n" ) ) //$NON-NLS-1$
				commandText = commandText.substring( 0, commandText.length() - 1 );
			printDebugMessage( String.format( "Command '%s' sent, timeout = %d", commandText, Integer.valueOf( commandTimeout ) ) ); //$NON-NLS-1$
		}
		if ( commandTimeout == 0 )
			// Skip commands with no timeout 
			return;
		try {
			fCommandQueue.put( new QueueEntry( System.currentTimeMillis(), token ) );
		}
		catch( InterruptedException e ) {
			// ignore
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandRemoved(org.eclipse.cdt.dsf.debug.service.command.ICommandToken)
	 */
	@Override
	public void commandRemoved( ICommandToken token ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandDone(org.eclipse.cdt.dsf.debug.service.command.ICommandToken, org.eclipse.cdt.dsf.debug.service.command.ICommandResult)
	 */
	@Override
	public void commandDone( ICommandToken token, ICommandResult result ) {
		if ( !isTimeoutEnabled() )
			return;
		fCommandQueue.remove( new QueueEntry( 0, token ) );
		if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS ) {
			String commandText = token.getCommand().toString();
			if ( commandText.endsWith( "\n" ) ) //$NON-NLS-1$
				commandText = commandText.substring( 0, commandText.length() - 1 );
			printDebugMessage( String.format( "Command '%s' is done", commandText ) ); //$NON-NLS-1$
		}
		// Reset the timestamp of the next command in the queue because 
		// regardless how long it has been in the queue GDB will start 
		// executing it only when the execution of the previous command 
		// is completed.
		QueueEntry nextEntry = fCommandQueue.peek();
		if ( nextEntry != null ) {
			setTimeStamp( System.currentTimeMillis(), nextEntry );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	@Override
	public void preferenceChange( PreferenceChangeEvent event ) {
		String property = event.getKey();
		if ( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT.equals( property ) ) {
			// The new value is null when the timeout support is disabled.
			if ( event.getNewValue() == null || !event.getNewValue().equals( event.getOldValue() ) ) {
				fTimeoutEnabled = ( event.getNewValue() != null ) ? 
					Boolean.parseBoolean( event.getNewValue().toString() ) : Boolean.FALSE;
				updateWaitTimeout();
				fTimerThread.setTimerThreadState( ( fTimerThread.getWaitTimeout() > 0 ) ? 
						TimerThreadState.RUNNING : TimerThreadState.HALTED );
			}
		}
		else if ( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE.equals( property ) ) {
			if ( event.getNewValue() == null || !event.getNewValue().equals( event.getOldValue() ) ) {
				try {
					fTimeout = ( event.getNewValue() != null ) ? 
						Integer.parseInt( event.getNewValue().toString() ) : 
						IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT;
					updateWaitTimeout();
					fTimerThread.setTimerThreadState( ( fTimerThread.getWaitTimeout() > 0 ) ? 
							TimerThreadState.RUNNING : TimerThreadState.HALTED );
				}
				catch( NumberFormatException e ) {
					GdbPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invlaid timeout value" ) ); //$NON-NLS-1$
				}
			}
		}
		else if ( IGdbDebugPreferenceConstants.PREF_COMMAND_CUSTOM_TIMEOUTS.equals( property ) ) {
			if ( event.getNewValue() instanceof String ) {
				fCustomTimeouts.initializeFromMemento( (String)event.getNewValue() );
			}
			else if ( event.getNewValue() == null ) {
				fCustomTimeouts.clear();
			}
			updateWaitTimeout();
			fTimerThread.setTimerThreadState( ( fTimerThread.getWaitTimeout() > 0 ) ? 
					TimerThreadState.RUNNING : TimerThreadState.HALTED );
		}
	}

	protected int getTimeoutForCommand( ICommand<? extends ICommandResult> command ) {
		if ( !(command instanceof MICommand<?>) )
			return 0;
		@SuppressWarnings( "unchecked" )
		Integer timeout = fCustomTimeouts.get( ((MICommand<? extends MIInfo>)command).getOperation() );
		return ( timeout != null ) ? timeout.intValue() : fTimeout;
	}

	protected void processTimedOutCommand( ICommandToken token ) {
		if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS ) {
			String commandText = token.getCommand().toString();
			if ( commandText.endsWith( "\n" ) ) //$NON-NLS-1$
				commandText = commandText.substring( 0, commandText.length() - 1 );
			printDebugMessage( String.format( "Command '%s' is timed out", commandText ) ); //$NON-NLS-1$
		}
		for ( Object l : fListeners.getListeners() ) {
			((ICommandTimeoutListener)l).commandTimedOut( token );
		}
	}

	public void addCommandTimeoutListener( ICommandTimeoutListener listener ) {
		fListeners.add( listener );
	}

	public void removeCommandTimeoutListener( ICommandTimeoutListener listener ) {
		fListeners.remove( listener );
	}
	
	private void updateWaitTimeout() {
		fTimerThread.setWaitTimout( calculateWaitTimeout() );
	}

	private boolean isTimeoutEnabled() {
		return fTimeoutEnabled;
	}

	private void printDebugMessage( String message ) {
		if(GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS) {
			GdbDebugOptions.trace(String.format( "%s %s  %s\n", GdbPlugin.getDebugTime(), TIMEOUT_TRACE_IDENTIFIER, message)); //$NON-NLS-1$
		}
		
	}

	private int calculateWaitTimeout() {
		int waitTimeout = 0;
		if ( isTimeoutEnabled() ) {
			waitTimeout = fTimeout;
			int minCustomTimeout = Integer.MAX_VALUE;
			for ( Integer t : fCustomTimeouts.values() ) {
				if ( t.intValue() > 0 ) {
					minCustomTimeout = Math.min( minCustomTimeout, t.intValue() );
				}
			}
			if ( minCustomTimeout > 0 ) {
				waitTimeout = ( waitTimeout == 0 ) ? 
					minCustomTimeout : Math.min( waitTimeout, minCustomTimeout );
			}
		}
		return waitTimeout;
	}

	private void setTimeStamp( long currentTime, QueueEntry nextEntry ) {
		if ( nextEntry != null ) {
			nextEntry.fTimestamp = currentTime;
			
			if ( GdbDebugOptions.DEBUG_COMMAND_TIMEOUTS ) {
				String commandText = nextEntry.fCommandToken.getCommand().toString();
				if ( commandText.endsWith( "\n" ) ) //$NON-NLS-1$
					commandText = commandText.substring( 0, commandText.length() - 1 );
				printDebugMessage( String.format( "Setting the timestamp for command '%s' to %d", commandText, Long.valueOf( currentTime ) ) ); //$NON-NLS-1$
			}
		}
	}
}
