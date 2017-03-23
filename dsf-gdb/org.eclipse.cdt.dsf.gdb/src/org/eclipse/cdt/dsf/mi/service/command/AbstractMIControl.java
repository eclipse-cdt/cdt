/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for handling of multiple stacks and threads
 *     Nokia - create and use backend service.
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Marc Khouzam (Ericsson) - New method to properly created ErrorThread (Bug 350837)
 *     Jason Litton (Sage Electronic Engineering, LLC) - Use Dynamic Tracing option (Bug 379169)
 *     Jonah Graham (Kichwa Coders) - Bug 317173 - cleanup warnings
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbDebugOptions;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.RawCommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIList;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIParser;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.icu.text.MessageFormat;

/**
 * Base implementation of an MI control service.  It provides basic handling 
 * of input/output channels, and processing of the commands.
 * <p>
 * Extending classes need to implement the initialize() and shutdown() methods.
 */
public abstract class AbstractMIControl extends AbstractDsfService
    implements IMICommandControl
{
	private static final String MI_TRACE_IDENTIFIER = "[MI]"; //$NON-NLS-1$
	private static final int NUMBER_CONCURRENT_COMMANDS = 3;
	private static final int DEVELOPMENT_TRACE_LIMIT_CHARS = 5000;
	
    /*
	 *  Thread control variables for the transmit and receive threads.
	 */

	private TxThread fTxThread;
    private RxThread fRxThread;
    private ErrorThread fErrorThread;
    
    // MI did not always support the --thread/--frame options
    // This boolean is used to know if we should use -thread-select and -stack-select-frame instead
    private boolean fUseThreadAndFrameOptions;
    // currentStackLevel and currentThreadId are only necessary when
    // we must use -thread-select and -stack-select-frame
    private int fCurrentStackLevel  = -1;
    private String fCurrentThreadId = null;
    
    // boolean for --thread-group option which helps to handle multiple inferior behavior.
    // Since GDB.7.1
    private boolean fUseThreadGroupOption;
    
    private final BlockingQueue<CommandHandle> fTxCommands = new LinkedBlockingQueue<CommandHandle>();
    private final Map<Integer, CommandHandle>  fRxCommands = Collections.synchronizedMap(new HashMap<Integer, CommandHandle>());

    /**
     * Handle that's inserted into the TX commands queue to signal 
     * that the TX thread should shut down.
     */
    private final CommandHandle fTerminatorHandle = new CommandHandle(null, null);

    /*
     *   Various listener control variables used to keep track of listeners who want to monitor
     *   what the control object is doing.
     */

    private final List<ICommandListener> fCommandProcessors = new ArrayList<ICommandListener>();
    private final List<IEventListener>   fEventProcessors = new ArrayList<IEventListener>();
    
    /**
     *   Current command which have not been handed off to the backend yet.
     */
    
    private final List<CommandHandle> fCommandQueue = new ArrayList<CommandHandle>();

    /**
     * Flag indicating that the command control has stopped processing commands.
     */
    private boolean fStoppedCommandProcessing = false;

    /**
     * An output stream that MI communication should be output to.
     * It serves for debugging. Can be <code>null</code> to disable tracing.
     */
    private OutputStream fTracingStream = null;

    private CommandFactory fCommandFactory;
    
    public AbstractMIControl(DsfSession session) {
    	this(session, false, false, new CommandFactory());
    }

    /**
     * @since 3.0
     */
    public AbstractMIControl(DsfSession session, boolean useThreadAndFrameOptions, CommandFactory factory) {
    	this(session, false, useThreadAndFrameOptions, factory);
    }

    /**
	 * @since 4.0
	 */
    public AbstractMIControl(DsfSession session, boolean useThreadGroupOption, boolean useThreadAndFrameOptions, CommandFactory factory) {
        super(session);
        
        // If we use the --thread-group option, we should automatically use the --thread/--frame option
        // since the --thread-group was added to GDB later than the --thread/--frame option
        assert useThreadGroupOption ? useThreadAndFrameOptions : true;
        
        fUseThreadGroupOption     = useThreadGroupOption;
        fUseThreadAndFrameOptions = useThreadAndFrameOptions;
        if (fUseThreadGroupOption) {
        	// If we use --thread-group option, we should automatically use the --thread option
        	fUseThreadAndFrameOptions = true;
        }
        fCommandFactory = factory;
    }

    /**
     * Set the tracing stream for the MI communication.  If this method is never
     * called, tracing will be off, by default.
     * 
     * @param tracingStream The stream to use.  Can be <code>null</code>
     * to disable tracing.
     * @since 2.0
     */
    protected synchronized void setMITracingStream(OutputStream tracingStream) {
    	fTracingStream = tracingStream;
    }

    /**
     * Returns the MI tracing stream.
     */
    private synchronized OutputStream getMITracingStream() {
    	return fTracingStream;
    }
    
    /**
	 * @since 3.0
	 */
    protected void setUseThreadAndFrameOptions(boolean shouldUse) {
    	fUseThreadAndFrameOptions = shouldUse;
    }
    
    /**
	 * @since 4.0
	 */
    protected void setUseThreadGroupOptions(boolean shouldUse) {
    	fUseThreadGroupOption = shouldUse;
    	if (shouldUse) {
    		fUseThreadAndFrameOptions = true;
    	}
    }
    
    /**
     * @since 3.0
     */
	@Override
    public CommandFactory getCommandFactory() {
    	return fCommandFactory;
    }
    
    /**
     * Starts the threads that process the debugger input/output channels.
     * To be invoked by the initialization routine of the extending class.
     * 
     * This version of the method will not start a thread for the error stream.
     * 
     * @param inStream
     * @param outStream
     */
    protected void startCommandProcessing(InputStream inStream, OutputStream outStream) {
    	startCommandProcessing(inStream, outStream, null);
    }
    
    /**
     * Starts the threads that process the debugger input/output/error channels.
     * To be invoked by the initialization routine of the extending class.
     * 
     * 
     * @param inStream
     * @param outStream
     * @param errorStream
     * @since 4.1
     */
    protected void startCommandProcessing(InputStream inStream, OutputStream outStream, InputStream errorStream) {
    	
        fTxThread = new TxThread(outStream);
        fRxThread = new RxThread(inStream);
        
        if (errorStream != null) {
        	fErrorThread = new ErrorThread(errorStream);
        }
        
        fTxThread.start();
        fRxThread.start();
        if (fErrorThread != null) {
        	fErrorThread.start();
        }
    }
    
    /**
     *  Stops the threads that process the debugger input/output channels, and notifies the
     *  results of the outstanding commands. To be invoked by the shutdown routine of the 
     *  extending class.
     * 
     * @param inStream
     * @param outStream
     */

    private Status genStatus(String str) {
    	return new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, str, null);
    }
    
    protected void stopCommandProcessing() {
        // Guard against calling this multiple times (e.g. as a result of a 
        // user request and an event from the back end).
        if (fStoppedCommandProcessing) return;
        fStoppedCommandProcessing = true;
        
    	/*
    	 *  First go through the commands which have been queueud and not yet sent to the backend.
    	 */
    	for (CommandHandle commandHandle : fCommandQueue) {
            if (commandHandle.getRequestMonitor() == null) continue;
            commandHandle.getRequestMonitor().setStatus(genStatus("Connection is shut down")); //$NON-NLS-1$
            commandHandle.getRequestMonitor().done();
        }
    	fCommandQueue.clear();   
    	
    	/*
    	 *  Now go through the commands which are outstanding in that they have been sent to the backend.
    	 */
    	cancelRxCommands();
    	
        /*
         *  Now handle any requests which have not been transmitted, but weconsider them handed off.
         */
        List<CommandHandle> txCommands = new ArrayList<CommandHandle>();
        fTxCommands.drainTo(txCommands);
        for (CommandHandle commandHandle : txCommands) {
            if (commandHandle.getRequestMonitor() == null) continue;
            commandHandle.getRequestMonitor().setStatus(genStatus("Connection is shut down")); //$NON-NLS-1$
            commandHandle.getRequestMonitor().done();
        }
        
        // Queue a null value to tell the send thread to shut down.
        fTxCommands.add(fTerminatorHandle);
    }
    
    private synchronized void cancelRxCommands() {
        for (CommandHandle commandHandle : fRxCommands.values()) {
            if (commandHandle.getRequestMonitor() == null) continue;
            commandHandle.getRequestMonitor().setStatus(genStatus( "Connection is shut down")); //$NON-NLS-1$
            commandHandle.getRequestMonitor().done();
        }
        fRxCommands.clear();
    }
    
    /**
     * Queues the given MI command to be sent to the debugger back end.  
     * 
     * @param command Command to be executed.  This parameter must be an 
     * instance of DsfMICommand, otherwise a ClassCastException will be 
     * thrown. 
     * @param rm Request completion monitor
     * 
     * @see org.eclipse.cdt.dsf.debug.service.command.ICommandControl#addCommand(org.eclipse.cdt.dsf.debug.service.command.ICommand, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    
	@Override
    public <V extends ICommandResult> ICommandToken queueCommand(final ICommand<V> command, DataRequestMonitor<V> rm) {

        // Cast the command to MI Command type.  This will cause a cast exception to be 
        // thrown if the client did not give an MI command as an argument.
        @SuppressWarnings("unchecked")
              MICommand<MIInfo> miCommand = (MICommand<MIInfo>) command;

        // Cast the return token to match the result type of MI Command.  This is checking
        // against an erased type so it should never throw any exceptions.
        @SuppressWarnings("unchecked")
        DataRequestMonitor<MIInfo> miDone = (DataRequestMonitor<MIInfo>) rm;

        final CommandHandle handle = new CommandHandle(miCommand, miDone);

        // If the command control stopped processing commands, just return an error immediately. 
        if (fStoppedCommandProcessing) {
            rm.setStatus(genStatus("Connection is shut down")); //$NON-NLS-1$
            rm.done();
        } else {
        	/*
        	 *  We only allow three outstanding commands to be on the wire to the backend
        	 *  at any one time. This allows for coalescing as well as canceling
        	 *  existing commands on a state change. So we add it to the waiting list and let
        	 *  the user know they can now work with this item if need be.
        	 */
        	fCommandQueue.add(handle);
            processCommandQueued(handle);
            
            if (fRxCommands.size() < NUMBER_CONCURRENT_COMMANDS) {
                // In a separate dispatch cycle.  This allows command listeners 
            	// to respond to the command queued event.  
                getExecutor().execute(new DsfRunnable() {
                	@Override
                    public void run() {
                        processNextQueuedCommand();
                    }
                });
            }
        }
        
        return handle;
    }

    private void processNextQueuedCommand() {
		if (!fCommandQueue.isEmpty()) {
			final CommandHandle handle = fCommandQueue.remove(0);
			if (handle != null) {
				processCommandSent(handle);

				// Older debuggers didn't support the --thread/--frame options
				// Also, not all commands support those options (e.g., CLI commands)
				if (!fUseThreadAndFrameOptions || !handle.getCommand().supportsThreadAndFrameOptions()) {
					// Without the --thread/--frame, we need to send the proper 
					// -thread-select and -stack-frame-select before sending the command
					
					final IDMContext targetContext = handle.fCommand.getContext();
					final String targetThread = handle.getThreadId();
					final int targetFrame = handle.getStackFrameId();

					// The thread-select and frame-select make sense only if the thread is stopped.
					IRunControl runControl = getServicesTracker().getService(IRunControl.class);
					IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(targetContext, IMIExecutionDMContext.class);
					if (runControl != null && execDmc != null && runControl.isSuspended(execDmc)) {
						// Before the command is sent, Check the Thread Id and send it to 
						// the queue only if the id has been changed. Also, don't send a threadId of 0,
						// because that id is only used internally for single-threaded programs
						if (targetThread != null && !targetThread.equals("0") && !targetThread.equals(fCurrentThreadId)) { //$NON-NLS-1$
							fCurrentThreadId = targetThread;
							resetCurrentStackLevel();
							CommandHandle cmdHandle = new CommandHandle(
									(MICommand<MIInfo>)getCommandFactory().createMIThreadSelect(targetContext, targetThread), null);
							cmdHandle.generateTokenId();
							fTxCommands.add(cmdHandle);
						}

						// Before the command is sent, Check the Stack level and send it to 
						// the queue only if the level has been changed. 
						if (targetFrame >= 0 && targetFrame != fCurrentStackLevel) {
							fCurrentStackLevel = targetFrame;
							CommandHandle cmdHandle = new CommandHandle(
									(MICommand<MIInfo>)getCommandFactory().createMIStackSelectFrame(targetContext, targetFrame), null);
							cmdHandle.generateTokenId();
							fTxCommands.add(cmdHandle);
						}
					}
				}

				if (!(handle.getCommand() instanceof RawCommand)) {
					// Only generate a token id if the command is not a RawCommand
					// RawCommands are sent to GDB without an answer expected, so we don't
					// need a token id.  In fact, GDB will fail if we send one in this case.
					handle.generateTokenId();
				}
		    	fTxCommands.add(handle);
			}
		}
    }

    /*
     *   This is the command which allows the user to retract a previously issued command. The
     *   state of the command  is that it is in the waiting queue  and has not yet been handed 
     *   to the backend yet.
     *   
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#removeCommand(org.eclipse.cdt.dsf.mi.service.command.commands.ICommand)
     */
	@Override
    public void removeCommand(ICommandToken token) {
    	
    	synchronized(fCommandQueue) {
    		
    		for ( CommandHandle handle : fCommandQueue ) {
    			if ( handle.equals(token)) {
    				fCommandQueue.remove(handle);
    				
    				final CommandHandle finalHandle = handle;
                    getExecutor().execute(new DsfRunnable() {
                    	@Override
                        public void run() {
                        	processCommandRemoved(finalHandle);
                        }
                    });
    				break;
    			}
    		}
    	}
    }
   
    /*
     *  Allows a user ( typically a cache manager ) to sign up a listener to monitor command queue
     *  activity.
     *  
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#addCommandListener(org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl.ICommandListener)
     */
	@Override
    public void addCommandListener(ICommandListener processor) { fCommandProcessors.add(processor); }
    
    /*
     *  Allows a user ( typically a cache manager ) to remove a monitoring listener.
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#removeCommandListener(org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl.ICommandListener)
     */
	@Override
    public void removeCommandListener(ICommandListener processor) { fCommandProcessors.remove(processor); }
    
    /*
     *  Allows a user to sign up to a listener which handles out of band messages ( events ).
     *  
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#addEventListener(org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl.IEventListener)
     */
	@Override
    public void addEventListener(IEventListener processor) { fEventProcessors.add(processor); }
    
    /*
     *  Allows a user to remove a event monitoring listener.
     *  
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#removeEventListener(org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl.IEventListener)
     */
	@Override
    public void removeEventListener(IEventListener processor) { fEventProcessors.remove(processor); }
    
	/** @deprecated Replaced with {@link ICommandControlService#getContext()} */
	@Deprecated
    abstract public MIControlDMContext getControlDMContext();
    
    /**
     * @since 1.1
     */
	@Override
    public boolean isActive() {
        return !fStoppedCommandProcessing;
    }
    
    /*
     *  These are the service routines which perform the various callouts back to the listeners.
     */

    private void processCommandQueued(CommandHandle commandHandle) {
        for (ICommandListener processor : fCommandProcessors) {
            processor.commandQueued(commandHandle);
        }
    }
    private void processCommandRemoved(CommandHandle commandHandle) {
        for (ICommandListener processor : fCommandProcessors) {
            processor.commandRemoved(commandHandle);
        }
    }
    
    private void processCommandSent(CommandHandle commandHandle) {
        for (ICommandListener processor : fCommandProcessors) {
            processor.commandSent(commandHandle);
        }
    }

    private void processCommandDone(CommandHandle commandHandle, ICommandResult result) {
        /*
         *  Tell the listeners we have completed this one.
         */
        for (ICommandListener processor : fCommandProcessors) {
            processor.commandDone(commandHandle, result);
        }
    }
    
    private void processEvent(MIOutput output) {
        for (IEventListener processor : fEventProcessors) {
            processor.eventReceived(output);
        }
    }

	/*
	 * A global counter for all command, the token will be use to identify uniquely a command.
	 * Unless the value wraps around which is unlikely.
	 */
    private int fTokenIdCounter = 0 ;
    
	private int getNewTokenId() {
		int count = ++fTokenIdCounter;
		// If we ever wrap around.
		if (count <= 0) {
			count = fTokenIdCounter = 1;
		}
		return count;
	}
	
	/*
	 *  Support class which creates a convenient wrapper for holding all information about an
	 *  individual request.
	 */
    
    private class CommandHandle implements ICommandToken {

        private MICommand<MIInfo> fCommand;
        private DataRequestMonitor<MIInfo> fRequestMonitor;
        private int fTokenId ;
        
        CommandHandle(MICommand<MIInfo> c, DataRequestMonitor<MIInfo> d) {
            fCommand = c; 
            fRequestMonitor = d;
            fTokenId = -1; // Only initialize to a real value when needed
        }
        
    	@Override
        public MICommand<MIInfo> getCommand() { return fCommand; }
        public DataRequestMonitor<MIInfo> getRequestMonitor() { return fRequestMonitor; }
        // This method allows us to generate the token Id when we area actually going to use
        // it.  It is meant to help order the token ids based on when commands will actually
        // be sent
        public void generateTokenId() { fTokenId = getNewTokenId(); }
        public Integer getTokenId() { return fTokenId; }
        
        public int getStackFrameId() {
        	IFrameDMContext frameCtx = DMContexts.getAncestorOfType(fCommand.getContext(), IFrameDMContext.class);
        	if(frameCtx != null)
        		return frameCtx.getLevel();
        	return -1;
        } 

        public String getThreadId() {
        	IMIExecutionDMContext execCtx = DMContexts.getAncestorOfType(fCommand.getContext(), IMIExecutionDMContext.class);
        	if(execCtx != null)
        		return execCtx.getThreadId();
        	return null;
        } 
        
        public String getGroupId() {
        	IMIContainerDMContext containerCtx = DMContexts.getAncestorOfType(fCommand.getContext(), IMIContainerDMContext.class);
        	if(containerCtx != null)
        		return containerCtx.getGroupId();
        	return null;
        } 
        
        @Override
        public String toString() {
            return Integer.toString(fTokenId) + fCommand;
        }
    }

    /*
     *  This is the transmitter thread. When a command is given to this thread it has been
     *  considered to be sent, even if it has not actually been sent yet.  This assumption
     *  makes it easier from state management.  Whomever fill this pipeline handles all of
     *  the required state notification ( callbacks ). This thread simply physically gives
     *  the message to the backend. 
     */
    
    private class TxThread extends Thread {

        final private OutputStream fOutputStream; 
        
        public TxThread(OutputStream outStream) {
            super("MI TX Thread"); //$NON-NLS-1$
            fOutputStream = outStream;
        }

        @Override
        public void run () {
            while (true) {
                CommandHandle commandHandle = null;
                
                    try {
                        commandHandle = fTxCommands.take();
                    } catch (InterruptedException e) {
                        break;  // Shutting down.
                    }
        
                    if (commandHandle == fTerminatorHandle) {
                    	// There is a small possibility that a new command was inserted
                    	// in the fRxCommands map after we cleared that map.
                    	// Just to be safe, clear it again.
                    	// We do this to avoid synchronizing the handling of fRxCommands
                    	// because this is more efficient, as it happens only once at shutdown.
                    	cancelRxCommands();
                        break; // Null command is an indicator that we're shutting down. 
                    }
                    
                    /*
                     *  We note that this is an outstanding request at this point.
                     */
                    if (!(commandHandle.getCommand() instanceof RawCommand)) {
                    	// RawCommands will not get an answer, so we cannot put them in the receive queue.
                    	fRxCommands.put(commandHandle.getTokenId(), commandHandle);
                    }
                
                /*
                 *   Construct the new command and push this command out the pipeline.
                 */

                final String str;
                if (commandHandle.getCommand() instanceof RawCommand) {
                	// RawCommands CANNOT have a token id: GDB would read it as part of the RawCommand!
                	str = commandHandle.getCommand().constructCommand();
                } else if (fUseThreadGroupOption) {
                	// Implies that fUseThreadAndFrameOptions == true
                	str = commandHandle.getTokenId() + commandHandle.getCommand().constructCommand(commandHandle.getGroupId(),
							   																	   commandHandle.getThreadId(),
							   																	   commandHandle.getStackFrameId());                	
                } else if (fUseThreadAndFrameOptions) {
                	str = commandHandle.getTokenId() + commandHandle.getCommand().constructCommand(commandHandle.getThreadId(),
							   																	   commandHandle.getStackFrameId());
                } else {
                	str = commandHandle.getTokenId() + commandHandle.getCommand().constructCommand();
                }
                
                try {
                    if (fOutputStream != null) {
                        
                        if (GdbDebugOptions.DEBUG) {
                        	GdbDebugOptions.trace(String.format( "%s %s  %s", GdbPlugin.getDebugTime(), MI_TRACE_IDENTIFIER, str)); //$NON-NLS-1$
                        }
                        if (getMITracingStream() != null) {
                        	try {
                        		String message = GdbPlugin.getDebugTime() + " " + str; //$NON-NLS-1$
                        		while (message.length() > 100) {
                        			String partial = message.substring(0, 100) + "\\\n"; //$NON-NLS-1$
                        			message = message.substring(100);
                        			getMITracingStream().write(partial.getBytes());
                        		}
                        		getMITracingStream().write(message.getBytes());
                        	} catch (IOException e) {
                        		// The tracing stream could be closed at any time
                        		// since the user can set a preference to turn off
                        		// this tracing.
                        		setMITracingStream(null);
                        	}
                        }

                        fOutputStream.write(str.getBytes());
                        fOutputStream.flush();
                    }
                } catch (IOException e) {
                    // Shutdown thread in case of IO error.
                    break;
                }
            }
            // Must close the stream here to avoid leaking
            // Bug 345164 and Bug 339379
            try {
                if (fOutputStream != null) fOutputStream.close();
			} catch (IOException e) {
			}
        }
    }

    private class RxThread extends Thread {
        private final InputStream fInputStream;
        private final MIParser fMiParser = new MIParser();

		/**
		 * List of out of band records since the last result record. Out of band
		 * records are required for processing the results of CLI commands.
		 */
        private final List<MIOOBRecord> fAccumulatedOOBRecords = new LinkedList<MIOOBRecord>();

		/**
		 * List of stream records since the last result record, not including
		 * the record currently being processed (if it's a stream one). This is
		 * a subset of {@link #fAccumulatedOOBRecords}, as a stream record is a
		 * particular type of OOB record.
		 */
        private final List<MIStreamRecord> fAccumulatedStreamRecords = new LinkedList<MIStreamRecord>();

        public RxThread(InputStream inputStream) {
            super("MI RX Thread"); //$NON-NLS-1$
            fInputStream = inputStream;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() != 0) {
                    	// Write Gdb response to sysout or file
                    	if(GdbDebugOptions.DEBUG) {
                    		if (line.length() < DEVELOPMENT_TRACE_LIMIT_CHARS) {
                    			GdbDebugOptions.trace(String.format( "%s %s  %s\n", GdbPlugin.getDebugTime(), MI_TRACE_IDENTIFIER, line)); //$NON-NLS-1$
                    		} else {
                    			// "-list-thread-groups --available" give a very large output that is not very useful but that makes
                    			// looking at the traces much more difficult.  Don't show the full output in the traces.
                    			// If we really need to see that output, it will still be in the 'gdb traces'.
                    			GdbDebugOptions.trace(String.format( "%s %s  %s\n", GdbPlugin.getDebugTime(), MI_TRACE_IDENTIFIER,  //$NON-NLS-1$
                    					line.substring(0, DEVELOPMENT_TRACE_LIMIT_CHARS) + 
                    					" [remaining output truncated. Refer to 'gdb traces' if full output needed.]")); //$NON-NLS-1$
                    		}
                    	}
                        
                        final String finalLine = line;
                        if (getMITracingStream() != null) {
                        	try {
                        		String message = GdbPlugin.getDebugTime() + " " + finalLine + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        		while (message.length() > 100) {
                        			String partial = message.substring(0, 100) + "\\\n"; //$NON-NLS-1$
                        			message = message.substring(100);
                        			getMITracingStream().write(partial.getBytes());
                        		}
                        		getMITracingStream().write(message.getBytes());
                        	} catch (IOException e) {
                        		// The tracing stream could be closed at any time
                        		// since the user can set a preference to turn off
                        		// this tracing.
                        		setMITracingStream(null);
                        	}
                        }
                        
                    	processMIOutput(line);
                    }
                }
            } catch (IOException e) {
                // Socket is shut down.
            } catch (RejectedExecutionException e) {
                // Dispatch thread is down.
            }
            // Must close the stream here to avoid leaking and
            // to give enough time to read all the data
            // Bug 345164 and Bug 339379
            try {
				fInputStream.close();
			} catch (IOException e) {
			}
        }
        
        private MIResult findResultRecord(MIResult[] results, String variable) {
            for (int i = 0; i < results.length; i++) {
                if (variable.equals(results[i].getVariable())) {
                    return results[i];
                }
            }
            return null;
        }
        
        private String getStatusString(MICommand<MIInfo> origCommand, MIOutput response ) {
            
        	// Attempt to extract a message from the result record:
        	String message = null;
        	String[] parameters = null;
        	if (response != null && response.getMIResultRecord() != null) {
        		MIResult[] results = response.getMIResultRecord().getMIResults();

        		// Extract the parameters
        		MIResult paramsRes = findResultRecord(results, "parameters"); //$NON-NLS-1$
        		if (paramsRes != null && paramsRes.getMIValue() instanceof MIList) {
        			MIValue[] paramValues = ((MIList)paramsRes.getMIValue()).getMIValues();
        			parameters = new String[paramValues.length];
        			for (int i = 0; i < paramValues.length; i++) {
        				if (paramValues[i] instanceof MIConst) {
        					parameters[i] = ((MIConst)paramValues[i]).getString();
        				} else {
        					parameters[i] = ""; //$NON-NLS-1$
        				}
        			}
        		}
        		MIResult messageRes = findResultRecord(results, "message"); //$NON-NLS-1$
        		if (messageRes != null && messageRes.getMIValue() instanceof MIConst) {
        			message = ((MIConst)messageRes.getMIValue()).getString();
        		}
        		// FRCH: I believe that the actual string is "msg" ...
        		// FRCH: (at least for the version of gdb I'm using)
        		else {
        			messageRes = findResultRecord(results, "msg"); //$NON-NLS-1$
        			if (messageRes != null && messageRes.getMIValue() instanceof MIConst) {
        				message = ((MIConst)messageRes.getMIValue()).getString();
        			}
        		}
        	}
        	StringBuilder clientMsg = new StringBuilder();
        	clientMsg.append("Failed to execute MI command:\n"); //$NON-NLS-1$
        	clientMsg.append(origCommand.toString());
        	if (message != null) {
        		clientMsg.append("Error message from debugger back end:\n"); //$NON-NLS-1$
        		if (parameters != null) {
        			try {
        				clientMsg.append(MessageFormat.format(message, (Object[])parameters));
        			} catch(IllegalArgumentException e2) {
        				// Message format string invalid.  Fallback to just appending the strings. 
        				clientMsg.append(message);
        				clientMsg.append(Arrays.toString(parameters));
        			}
        		} else {
        			clientMsg.append(message);
        		}
        	}
        	return clientMsg.toString();
        }

       private String getBackendMessage(MIOutput response) {
            
        	// Attempt to extract a message from the result record:
        	String message = null;
        	String[] parameters = null;
        	if (response != null && response.getMIResultRecord() != null) {
        		MIResult[] results = response.getMIResultRecord().getMIResults();

        		// Extract the parameters
        		MIResult paramsRes = findResultRecord(results, "parameters"); //$NON-NLS-1$
        		if (paramsRes != null && paramsRes.getMIValue() instanceof MIList) {
        			MIValue[] paramValues = ((MIList)paramsRes.getMIValue()).getMIValues();
        			parameters = new String[paramValues.length];
        			for (int i = 0; i < paramValues.length; i++) {
        				if (paramValues[i] instanceof MIConst) {
        					parameters[i] = ((MIConst)paramValues[i]).getString();
        				} else {
        					parameters[i] = ""; //$NON-NLS-1$
        				}
        			}
        		}
        		MIResult messageRes = findResultRecord(results, "message"); //$NON-NLS-1$
        		if (messageRes != null && messageRes.getMIValue() instanceof MIConst) {
        			message = ((MIConst)messageRes.getMIValue()).getString();
        		}
        		// FRCH: I believe that the actual string is "msg" ...
        		// FRCH: (at least for the version of gdb I'm using)
        		else {
        			messageRes = findResultRecord(results, "msg"); //$NON-NLS-1$
        			if (messageRes != null && messageRes.getMIValue() instanceof MIConst) {
        				message = ((MIConst)messageRes.getMIValue()).getString();
        			}
        		}
        	}
        	StringBuilder clientMsg = new StringBuilder();
        	if (message != null) {
        		if (parameters != null) {
        			try {
        				clientMsg.append(MessageFormat.format(message, (Object[])parameters));
        			} catch(IllegalArgumentException e2) {
        				// Message format string invalid.  Fallback to just appending the strings. 
        				clientMsg.append(message);
        				clientMsg.append(Arrays.toString(parameters));
        			}
        		} else {
        			clientMsg.append(message);
        		}
        	}
        	return clientMsg.toString();
        }

        void processMIOutput(String line) {

            MIParser.RecordType recordType = fMiParser.getRecordType(line);
            
            if (recordType == MIParser.RecordType.ResultRecord) {
                final MIResultRecord rr = fMiParser.parseMIResultRecord(line);
           
            	
            	/*
            	 *  Find the command in the current output list. If we cannot then this is
            	 *  some form of asynchronous notification. Or perhaps general IO.
            	 */
                int id = rr.getToken();
                
                final CommandHandle commandHandle = fRxCommands.remove(id);

                if (commandHandle != null) {
                    final MIOutput response = new MIOutput(
                        rr, fAccumulatedOOBRecords.toArray(new MIOOBRecord[fAccumulatedOOBRecords.size()]) );
                    fAccumulatedOOBRecords.clear();
                    fAccumulatedStreamRecords.clear();
                	
                	MIInfo result = commandHandle.getCommand().getResult(response);
					DataRequestMonitor<MIInfo> rm = commandHandle.getRequestMonitor();
					
					/*
					 *  Not all users want to get there results. They indicate so by not having
					 *  a completion object. 
					 */
					if ( rm != null ) {
						rm.setData(result);
						
						/*
						 * We need to indicate if this request had an error or not.
						 */
						String errorResult =  rr.getResultClass();
						
						if ( errorResult.equals(MIResultRecord.ERROR) ) {
							String status = getStatusString(commandHandle.getCommand(),response);
							String message = getBackendMessage(response);
							Exception exception = new Exception(message);
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, status, exception)); 
						}
						
						/*
						 *  We need to complete the command on the DSF thread for data security.
						 */
						final ICommandResult finalResult = result;
						getExecutor().execute(new DsfRunnable() {
							@Override
	                        public void run() {
	                        	/*
	                        	 *  Complete the specific command.
	                        	 */
	                            if (commandHandle.getRequestMonitor() != null) {
	                                commandHandle.getRequestMonitor().done();
	                            }
	                            
	                            /*
	                             *  Now tell the generic listeners about it.
	                             */
	                            processCommandDone(commandHandle, finalResult);
	                        }
	                        @Override
                            public String toString() {
	                            return "MI command output received for: " + commandHandle.getCommand(); //$NON-NLS-1$
	                        }
	                    });
					} else {
						/*
						 *  While the specific requestor did not care about the completion  we
						 *  need to call any listeners. This could have been a CLI command for
						 *  example and  the CommandDone listeners there handle the IO as part
						 *  of the work.
						 */
						final ICommandResult finalResult = result;
						getExecutor().execute(new DsfRunnable() {
							@Override
	                        public void run() {
	                            processCommandDone(commandHandle, finalResult);
	                        }
	                        @Override
                            public String toString() {
	                            return "MI command output received for: " + commandHandle.getCommand(); //$NON-NLS-1$
	                        }
	                    });
					}
                } else {
                    /*
                     *  GDB apparently can sometimes send multiple responses to the same command.  In those cases, 
                     *  the command handle is gone, so post the result as an event.  To avoid processing OOB records
                     *  as events multiple times, do not include the accumulated OOB record list in the response 
                     *  MIOutput object.  
                     */
                    final MIOutput response = new MIOutput(rr, new MIOOBRecord[0]);

                    getExecutor().execute(new DsfRunnable() {
                    	@Override
                        public void run() {
                            processEvent(response);
                        }
                        @Override
                        public String toString() {
                            return "MI asynchronous output received: " + response; //$NON-NLS-1$
                        }
                    });
                }
        	} else if (recordType == MIParser.RecordType.OOBRecord) {
				// Process OOBs
        		final MIOOBRecord oob = fMiParser.parseMIOOBRecord(line);

        		fAccumulatedOOBRecords.add(oob);
        		// limit growth, but only if these are not responses to CLI commands
        		// Bug 302927 & 330608
        		if (fRxCommands.isEmpty() && fAccumulatedOOBRecords.size() > 20) {
        			fAccumulatedOOBRecords.remove(0);
        		}

				// The handling of this OOB record may need the stream records
				// that preceded it. One such case is a stopped event caused by a
				// catchpoint in gdb < 7.0. The stopped event provides no
				// reason, but we can determine it was caused by a catchpoint by
				// looking at the target stream.
        		
       	        final MIOutput response = new MIOutput(oob, fAccumulatedStreamRecords.toArray(new MIStreamRecord[fAccumulatedStreamRecords.size()]));

				// If this is a stream record, add it to the accumulated bucket
				// for possible use in handling a future OOB (see comment above)
    			if (oob instanceof MIStreamRecord) {
    				fAccumulatedStreamRecords.add((MIStreamRecord)oob);
            		if (fAccumulatedStreamRecords.size() > 20) { // limit growth; see bug 302927
            			fAccumulatedStreamRecords.remove(0);
            		}
    			}

            	/*
            	 *   OOBS are events. So we pass them to any event listeners who want to see them. Again this must
            	 *   be done on the DSF thread for integrity.
            	 */
                getExecutor().execute(new DsfRunnable() {
                	@Override
                    public void run() {
                        processEvent(response);
                    }
                    @Override
                    public String toString() {
                        return "MI asynchronous output received: " + response; //$NON-NLS-1$
                    }
                });
            }
            
            getExecutor().execute(new DsfRunnable() {
            	@Override
            	public void run() {
        			processNextQueuedCommand();
            	}
            });
        }
    }

    /**
     * A thread that will read GDB's stderr stream.
     * When a PTY is not being used for the inferior, everything
     * the inferior writes to stderr will be output on GDB's stderr.
     * If we don't read it, gdb eventually blocks, when the sream is
     * full.
     * 
     * Although we could write this error output to the inferior
     * console, we actually write it to the GDB console.  This is
     * because we cannot differentiate between inferior errors printouts
     * and GDB error printouts.
     * 
     * See bug 327617 for details.
     */
    private class ErrorThread extends Thread {
        private final InputStream fErrorStream;
        private final MIParser fMiParser = new MIParser();

        public ErrorThread(InputStream errorStream) {
            super("MI Error Thread"); //$NON-NLS-1$
            fErrorStream = errorStream;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fErrorStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                	// Create an error MI out-of-band record so that our gdb console prints it.
            		final MIOOBRecord oob = fMiParser.parseMIOOBRecord("&"+line+"\n");  //$NON-NLS-1$//$NON-NLS-2$
                    final MIOutput response = new MIOutput(oob, new MIStreamRecord[0]);
                    getExecutor().execute(new DsfRunnable() {
                    	@Override
                        public void run() {
                            processEvent(response);
                        }
                        @Override
                        public String toString() {
                            return "MI error output received: " + response; //$NON-NLS-1$
                        }
                    });
                }
            } catch (IOException e) {
                // Socket is shut down.
            } catch (RejectedExecutionException e) {
                // Dispatch thread is down.
            }
            // Must close the stream here to avoid leaking
            // Bug 345164 and Bug 339379
            try {
            	fErrorStream.close();
			} catch (IOException e) {
			}
        }
    }
    
    // we keep track of currentStackLevel and currentThreadId because in
    // some cases we must use -thread-select and -stack-select-frame
    public void resetCurrentThreadLevel(){
    	fCurrentThreadId = null; 
    }
    
    public void resetCurrentStackLevel(){
    	fCurrentStackLevel = -1; 
    }

    /**
	 * @since 4.1
	 */
    @ConfinedToDsfExecutor("this.getExecutor()")
    protected void commandFailed(ICommandToken token, int statusCode, String errorMessage) {
    	if ( !(token instanceof CommandHandle && token.getCommand() instanceof MICommand<?>) )
    		return;
		final CommandHandle commandHandle = (CommandHandle)token;
		Integer tokenId = commandHandle.getTokenId();

		// If the timeout value is too small a command can be timed out but still processed by RxThread.
		// To avoid processing it twice we need to remove it from the command list.
		CommandHandle h = fRxCommands.remove(tokenId);
		if (h == null)
			// Command has already been processed by RxThread.
			return;
		
		MIConst value = new MIConst();
		value.setCString(errorMessage);
		MIResult result = new MIResult();
		result.setVariable("msg"); //$NON-NLS-1$
		result.setMIValue(value);
		MIResultRecord resultRecord = new MIResultRecord();
		resultRecord.setToken(tokenId.intValue());
		resultRecord.setResultClass(MIResultRecord.ERROR);
		resultRecord.setMIResults(new MIResult[] { result });
		MIOutput miOutput = new MIOutput(resultRecord, new MIOOBRecord[0]);

		final MIInfo info = commandHandle.getCommand().getResult(miOutput);
		DataRequestMonitor<MIInfo> rm = commandHandle.getRequestMonitor();
		
		if ( rm != null ) {
			rm.setData(info);			
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, statusCode, errorMessage, null)); 
			rm.done();
            
            /*
             *  Now tell the generic listeners about it.
             */
            processCommandDone(commandHandle, info);
		}
    }
}
