/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for caching commands corresponding to multiple execution contexts 
 *******************************************************************************/
 
package org.eclipse.cdt.dsf.debug.service.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * This is a utility class for caching results of commands--typically commands
 * sent to an external engine via some protocol (GDB/MI, e.g.). Commands are
 * sent to their intended target through the supplied {@link ICommandControl},
 * and their results are cached so as to quickly service future identical
 * commands.
 * 
 * The cache has to be reset by the client that owns and uses the cache. A reset
 * should be done when an event occurs that alters the state of a context such
 * that commands on that context may no longer yield the same outcome as they
 * did before the event. A reset can be done on the entire cache or on a per
 * context basis.
 * 
 * @since 1.0
 */
 
public class CommandCache implements ICommandListener
{
    static enum CommandStyle { COALESCED, NONCOALESCED }

    /**
     * Holds cache information for a given command.
     * @param <V> Type matches the result type associated with the command.
     */
    class CommandInfo {
        
        /*
         *  Control variables.
         */
        
        /** List of the request monitors associated with this command */
        private final List<DataRequestMonitor<ICommandResult>> fCurrentRequestMonitors ; 
        
        /** Original command. Need for reference from Queue completion notification */
        private final ICommand<ICommandResult> fCommand;                   

        /** Style of this command ( internal coalesced or not) */
        private final CommandStyle fCmdStyle;
        
        /** Command being processed for this command */
        private CommandInfo fCoalescedCmd;           

        private ICommandToken fToken;
        
        public CommandInfo(CommandStyle cmdstyle, ICommand<ICommandResult> cmd, DataRequestMonitor<ICommandResult> rm ) {
            fCmdStyle = cmdstyle;
            fCommand = cmd;
            fCurrentRequestMonitors = new LinkedList<DataRequestMonitor<ICommandResult>>();
            fCurrentRequestMonitors.add(rm);
            fCoalescedCmd = null;
        }
        
        public CommandStyle getCommandstyle() { return fCmdStyle; }
        public List<DataRequestMonitor<ICommandResult>> getRequestMonitorList() { return fCurrentRequestMonitors; }
        public ICommand<ICommandResult> getCommand() { return fCommand; }
        public CommandInfo getCoalescedCmd() { return fCoalescedCmd; }
        public void setCoalescedCmd( CommandInfo cmd ) { fCoalescedCmd = cmd; }
        
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof CommandInfo)) return false;
            CommandInfo otherCmd = (CommandInfo)other;
            
            return otherCmd.fCommand.equals(fCommand);
        }
        
        @Override
        public int hashCode() {
            return fCommand.hashCode();
        }
    }

    class CommandResultInfo {
    	private final ICommandResult fData;
    	private final IStatus fStatus;

    	public CommandResultInfo(ICommandResult data, IStatus status) {
    		fData = data;
    		fStatus = status;
    	}

    	public ICommandResult getData() { return fData; }
    	public IStatus getStatus() { return fStatus; }
    }

    private DsfSession fSession;
    
    /**
     * The command control to be used to send commands to the backend.
     * In certain cases, a {@link BufferedCommandControl} should be used
     * to artificially delay the processing of command results; these
     * artificial delays are important to keep events and command results
     * ordered as received by the backend.
     */
    private ICommandControl fCommandControl;    
    
    /*
     *  This class contains 5 significant lists.
     *
     *  Cached Results : 
     *  
     *      Contains a mapping of commands and their completed results. Until the cached
     *      results are cleared by the owner of the cache.
     *                   
     *  Pending Commands Not Queued :
     *  
     *      The Control object has not yet indicated that it has recognized the command
     *      yet. The user is not allowed to interrogate these objects until the Control
     *      object indicates they have been queued ( commandQueued notification ).
     *
     *  Pending Commands Unsent : 
     *  
     *      This is the list of commands  which have been  issued to the Control object but 
     *      have not been actually issued to the backend. These commands represent coalesce 
     *      options.  They may be compared against the Queued list  being maintained by the
     *      Control object until told otherwise - commandSent notification ).
     *                            
     *  Pending Commands Sent :
     *  
     *      This is a list  of commands which have been issued to the Control object and 
     *      have also been sent to the backend. It is not possible use these objects for 
     *      coalescents.
     *
     *  Coalesced Pending Q :
     *  
     *      These represent original commands  for which a new coalesced command has been 
     *      created. When the coalesced commands completes the results will be decomposed
     *      when back into individual results from this command.
     */
    private Set<IDMContext> fAvailableContexts = new HashSet<IDMContext>();

    private Map<IDMContext, HashMap<CommandInfo, CommandResultInfo>> fCachedContexts = new HashMap<IDMContext, HashMap<CommandInfo, CommandResultInfo>>();
    
    private ArrayList<CommandInfo> fPendingQCommandsSent = new ArrayList<CommandInfo>();
    
    private ArrayList<CommandInfo> fPendingQCommandsNotYetSent = new ArrayList<CommandInfo>();
    
    private ArrayList<CommandInfo> fPendingQWaitingForCoalescedCompletion = new ArrayList<CommandInfo>();
    
    private static boolean DEBUG = false;
	private static final String CACHE_TRACE_IDENTIFIER = " [CHE]"; //$NON-NLS-1$
	private static String BLANK_CACHE_TRACE_IDENTIFIER = ""; //$NON-NLS-1$
	static {
        DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf/debugCache"));  //$NON-NLS-1$//$NON-NLS-2$
		for (int i=0; i<CACHE_TRACE_IDENTIFIER.length(); i++) {
			BLANK_CACHE_TRACE_IDENTIFIER += " "; //$NON-NLS-1$
		}
    }  

	private void debug(String message) {
		debug(message, ""); //$NON-NLS-1$
    }
    
    private void debug(String message, String prefix) {
    	if (DEBUG) {
    		// The message can span more than one line
    		String[] multiLine = message.split("\n"); //$NON-NLS-1$
    		
			// Create a blank prefix for proper alignment
    		String blankPrefix = ""; //$NON-NLS-1$
    		for (int i=0; i<prefix.length(); i++) {
    			blankPrefix += " "; //$NON-NLS-1$
    		}

    		for (int i = 0; i < multiLine.length; i++) {
    			String traceIdentifier;
    			if (i == 0) {
    	    		// For the first line we prepend the cache identifier string
    				traceIdentifier = CACHE_TRACE_IDENTIFIER + prefix;
    				
    			} else {
    	    		// For all other lines we prepend a blank prefix for proper alignment
    				traceIdentifier = BLANK_CACHE_TRACE_IDENTIFIER + blankPrefix;
    			}
    				
    			message = DsfPlugin.getDebugTime() + traceIdentifier + 
    						" " + multiLine[i]; //$NON-NLS-1$

    			// Make sure our lines are not too long
    			while (message.length() > 100) {
    				String partial = message.substring(0, 100) + "\\"; //$NON-NLS-1$
    				message = message.substring(100);
    				System.out.println(partial);
    			}
    			System.out.println(message);
    		}
    	}
    }

    public CommandCache(DsfSession session, ICommandControl control) {
        fSession = session;
        fCommandControl = control;

		/*
		 * We listen for the notifications that the commands have been sent to
		 * their intended target via the ICommandControl service.
		 */
        fCommandControl.addCommandListener(this);
    }

    /*
     * Constructs a coalesced command if possible.
     */
    private CommandInfo getCoalescedCommand(CommandInfo cmd) {
        
        for ( CommandInfo currentUnsentEntry : new ArrayList<CommandInfo>(fPendingQCommandsNotYetSent) ) {
            /*
             *  Get the current unsent entry to determine if we can coalesced with it.
             */
            ICommand<?> unsentCommand = currentUnsentEntry.getCommand();
            
            /*
             * Check if we can so construct a new COALESCED command from scratch.
             */
            
            // For sanity's sake, cast the generic ?'s to concrete types in the cache implementation.
            @SuppressWarnings("unchecked")            
            ICommand<ICommandResult> coalescedCmd = 
                (ICommand<ICommandResult>)unsentCommand.coalesceWith( cmd.getCommand() );
            
            if ( coalescedCmd != null )  {
                CommandInfo coalescedCmdInfo = new CommandInfo( CommandStyle.COALESCED, coalescedCmd, null) ;
              
                if ( currentUnsentEntry.getCommandstyle() == CommandStyle.COALESCED ) {
                    /*
                     *  We matched a command which is itself already a COALESCED command.  So
                     *  we need to run through  the reference list  and point all the current
                     *  command which are referencing the command we just subsumed and change
                     *  them to point to the new super command.
                     */
                  
                    for ( CommandInfo waitingEntry : new ArrayList<CommandInfo>(fPendingQWaitingForCoalescedCompletion) ) {
                      
                        if ( waitingEntry.getCoalescedCmd() == currentUnsentEntry ) {
                            /*
                             *  This referenced the old command change it to point to the new one.
                             */
                            waitingEntry.setCoalescedCmd(coalescedCmdInfo);
                        }
                    }
                } else {
                    /*
                     *  This currently unsent entry needs to go into the coalescing list. To
                     *  be completed when the coalesced command comes back with a result.
                     */
                    fPendingQWaitingForCoalescedCompletion.add(currentUnsentEntry);
                    currentUnsentEntry.setCoalescedCmd(coalescedCmdInfo);
                }
              
                /*
                 *  Either way we want to take the command back from the Control object so it
                 *  does not continue to process it.
                 */
                fPendingQCommandsNotYetSent.remove(currentUnsentEntry);
                fCommandControl.removeCommand(currentUnsentEntry.fToken);
              
                return( coalescedCmdInfo );
            }
        }
        
        return null;
    }
    
    /**
     * Executes given ICommand, or retrieves the cached result if known.
     * @param command Command to execute.
     * @param rm Return token, contains the retrieved MIInfo object as
     * well as its cache status.
     */
    public <V extends ICommandResult> void execute(ICommand<V> command, DataRequestMonitor<V> rm) {
        assert fSession.getExecutor().isInExecutorThread();
        
        // Cast the generic ?'s to concrete types in the cache implementation.
        @SuppressWarnings("unchecked")
        final ICommand<ICommandResult> genericCommand = (ICommand<ICommandResult>)command;
        @SuppressWarnings("unchecked")
        final DataRequestMonitor<ICommandResult> genericDone = (DataRequestMonitor<ICommandResult>) rm;

        CommandInfo cachedCmd = new CommandInfo( CommandStyle.NONCOALESCED, genericCommand, genericDone) ;
        
        final IDMContext context = genericCommand.getContext();
        
        /*
         * If command is already cached, just return the cached data.
         */ 
        if(fCachedContexts.get(context) != null && fCachedContexts.get(context).containsKey(cachedCmd)){
        	CommandResultInfo result = fCachedContexts.get(context).get(cachedCmd);
        	debug(command.toString().trim());
            if (result.getStatus().getSeverity() <= IStatus.INFO) {
            	@SuppressWarnings("unchecked") 
            	V v = (V)result.getData();
            	rm.setData(v);
            	debug(v.toString());
            } else {
            	rm.setStatus(result.getStatus());
            	debug(result.getStatus().toString());
            }
            rm.done();
            return;
        } 
        
        /*
         *  Return an error if the target is available anymore.
         */ 
        if (!isTargetAvailable(command.getContext())) {
        	debug(command.toString().trim(), "[N/A]"); //$NON-NLS-1$

            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Target not available.", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
            
        /*
         *  If we are already waiting for this command to complete, 
         *  add this request monitor to list of waiting monitors.
         */ 
        for ( CommandInfo sentCommand : fPendingQCommandsSent ) {
            if ( sentCommand.equals( cachedCmd )) {
                sentCommand.getRequestMonitorList().add(genericDone);
            	debug(command.toString().trim(), "[SNT]"); //$NON-NLS-1$
                return;
            }
        }
        for ( CommandInfo notYetSentCommand : fPendingQCommandsNotYetSent ) {
            if ( notYetSentCommand.equals( cachedCmd )) {
                notYetSentCommand.getRequestMonitorList().add(genericDone);
            	debug(command.toString().trim(), "[SND]"); //$NON-NLS-1$
                return;
            }
        }

        
        /*
         *  We see if this command can be combined into a coalesced one. The
         *  coalesce routine will take care of the already enqueued one which
         *  this command is being coalesced with.
         */
       
        CommandInfo coalescedCmd = getCoalescedCommand(cachedCmd);
        
        if ( coalescedCmd != null  ) {
            /*
             *  The original command we were handed needs to go into the waiting QUEUE.
             *  We also need to point it it to the coalesced command.
             */
            fPendingQWaitingForCoalescedCompletion.add(cachedCmd);
            cachedCmd.setCoalescedCmd(coalescedCmd);
            cachedCmd = coalescedCmd;
        }
 
        /*
         *  Now we have a command to send ( coalesced or not ).  Put it in the cannot touch
         *  it list and give it to the Control object. Our state handlers will move it into
         *  the proper list as the Control object deals with it.
         */
        final CommandInfo finalCachedCmd = cachedCmd;
        fPendingQCommandsNotYetSent.add(finalCachedCmd);
        
        finalCachedCmd.fToken = fCommandControl.queueCommand(
            finalCachedCmd.getCommand(), 
            new DataRequestMonitor<ICommandResult>(ImmediateExecutor.getInstance(), null) {
            	@Override
            	public synchronized void done() {
            		// protect against the cache being called in non-session thread, but at 
            		// the same time avoid adding extra dispatch cycles to command processing. 
            		if (fSession.getExecutor().isInExecutorThread()) {;
            			super.done();
            		} else {
            			fSession.getExecutor().execute(new DsfRunnable() {
            				public void run() {
            					superDone();
            				}
            			});
            		}
            	}
            	private void superDone() {
            		super.done();
            	}
                @Override
                public void handleCompleted() {
                    
                    /*
                     *  Match this up with a command set we know about.
                     */
                    if ( ! fPendingQCommandsSent.remove(finalCachedCmd) ) {
                    	/*
                    	 * This can happen if the call to queueCommand() completes without
                    	 * actually having to send the command.  In that case, we should find
                    	 * our command in the fPendingQCommandsNotYetSent queue.
                    	 * For example, upon termination some queueCommand() implementation
                    	 * may complete immediately with an error status. Bug 309309
                    	 */
                        if ( ! fPendingQCommandsNotYetSent.remove(finalCachedCmd) ) {
                        	assert false : "Missing command"; //$NON-NLS-1$
                        	return;
                        }
                    }

                    ICommandResult result = getData();
                    IStatus status = getStatus();

                    if ( finalCachedCmd.getCommandstyle() == CommandStyle.COALESCED ) {
                        /*
                         *  We matched a command which is itself  already a COALESCED command.  So
                         *  we need to go through the list of unsent commands which were not sent
                         *  because  the coalesced command represented it.  For each match we find
                         *  we create a new result from the coalesced command for it.
                         */
                        
                        for ( CommandInfo waitingEntry : new ArrayList<CommandInfo>(fPendingQWaitingForCoalescedCompletion) ) {
                            
                            if ( waitingEntry.getCoalescedCmd() == finalCachedCmd ) {
                                
                                /*
                                 *  Remove this entry from the list since we can complete it.
                                 */
                                fPendingQWaitingForCoalescedCompletion.remove(waitingEntry);
                                
                                // Cast the calculated result back to the requested type.
                                @SuppressWarnings("unchecked")
                                V subResult = (V)result.getSubsetResult(waitingEntry.getCommand());
                                CommandResultInfo subResultInfo = new CommandResultInfo(subResult, status);
                                
                                if(fCachedContexts.get(context) != null){
                                	fCachedContexts.get(context).put(waitingEntry, subResultInfo);
                                } else {
                                	HashMap<CommandInfo, CommandResultInfo> map = new HashMap<CommandInfo, CommandResultInfo>();
                                	map.put(waitingEntry, subResultInfo);
                                	fCachedContexts.put(context, map);
                                }	

                                if (!isSuccess()) {
                                    
                                    /*
                                     *  We had some form of error with the original command. So notify the 
                                     *  original requesters of the issues.
                                     */
                                    for (DataRequestMonitor<?> pendingRM : waitingEntry.getRequestMonitorList()) {
                                        pendingRM.setStatus(status);
                                        pendingRM.done();
                                    }
                                } else {
                                    assert subResult != null;
                                    
                                    /*
                                     *  Notify the original requesters of the positive results.
                                     */
                                    for (DataRequestMonitor<? extends ICommandResult> pendingRM : waitingEntry.getRequestMonitorList()) {
                                        // Cast the pending return token to match the requested type.
                                        @SuppressWarnings("unchecked")
                                        DataRequestMonitor<V> vPendingRM = (DataRequestMonitor<V>) pendingRM;
                                        
                                        vPendingRM.setData(subResult);
                                        vPendingRM.done();
                                    }
                                }
                            }
                        }
                    } else {
                    	// Save the command result in cache, but only if the command's context 
                    	// is still available.  Otherwise an error may get cached incorrectly.
                    	if (isTargetAvailable(context)) {
                    		CommandResultInfo resultInfo = new CommandResultInfo(result, status);

                    		if (fCachedContexts.get(context) != null){
                    			fCachedContexts.get(context).put(finalCachedCmd, resultInfo);
                    		} else {
                    			HashMap<CommandInfo, CommandResultInfo> map = new HashMap<CommandInfo, CommandResultInfo>();
                    			map.put(finalCachedCmd, resultInfo);
                    			fCachedContexts.put(context, map);
                    		}
                    	}
                    	// This is an original request which completed. Indicate success or
                    	// failure to the original requesters.
                        if (!isSuccess()) {
                            /*
                             *  We had some form of error with the original command. So notify the 
                             *  original requesters of the issues.
                             */
                            for (DataRequestMonitor<?> pendingRM : finalCachedCmd.getRequestMonitorList()) {
                                pendingRM.setStatus(status);
                                pendingRM.done();
                            }
                        } else {
                            // Cast the calculated result back to the requested type.
                            @SuppressWarnings("unchecked")
                            V vResult = (V)result;
                            
                            for (DataRequestMonitor<? extends ICommandResult> pendingRM : finalCachedCmd.getRequestMonitorList()) {
                                // Cast the pending return token to match the requested type.
                                @SuppressWarnings("unchecked")
                                DataRequestMonitor<V> vPendingRM = (DataRequestMonitor<V>) pendingRM;
                                
                                vPendingRM.setData(vResult);
                                vPendingRM.done();
                            }
                        }
                    }                
                }
        });
    }

    /**
     * TODO
     */
    public void setContextAvailable(IDMContext context, boolean isAvailable) {
        if (isAvailable) {
            fAvailableContexts.add(context);
        } else {
            fAvailableContexts.remove(context);
            for (Iterator<IDMContext> itr = fAvailableContexts.iterator(); itr.hasNext();) {
                if (DMContexts.isAncestorOf(context, itr.next())) {
                    itr.remove();
                }
            }
        }
    }
    
    /** 
     * TODO
     * @see #setContextAvailable(IDMContext, boolean)
     */
    public boolean isTargetAvailable(IDMContext context) {
        for (IDMContext availableContext : fAvailableContexts) {
            if (context.equals(availableContext) || DMContexts.isAncestorOf(context, availableContext)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Clears all the cache data. Equivalent to <code>reset(null)</code>.
	 */
    public void reset() {
    	fCachedContexts.clear();
    }

    public void commandRemoved(ICommandToken token) {
        /*
         *  Do nothing. 
         */
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandQueued(org.eclipse.cdt.dsf.debug.service.command.ICommandToken)
     */
    public void commandQueued(ICommandToken token) {
        /*
         *  Do nothing. 
         */
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandDone(org.eclipse.cdt.dsf.debug.service.command.ICommandToken, org.eclipse.cdt.dsf.debug.service.command.ICommandResult)
     */
    public void commandDone(ICommandToken token, ICommandResult result) {
        /*
         *  We handle the done with a runnable where we initiated the command
         *  so there is nothing to do here.
         */
    }
    
    /*
     * Move the command into our internal sent list. This means we can no longer look at
     * this command for possible coalescence since it has been given to the debug engine
     * and is currently being processed.
     * 
     * @see org.eclipse.cdt.dsf.debug.service.command.ICommandListener#commandSent(org.eclipse.cdt.dsf.debug.service.command.ICommandToken)
     */
    public void commandSent(ICommandToken token) {
        
        // Cast the generic ?'s to concrete types in the cache implementation.
        @SuppressWarnings("unchecked")
        ICommand<ICommandResult> genericCommand = (ICommand<ICommandResult>)token.getCommand();
        
        CommandInfo cachedCmd = new CommandInfo( CommandStyle.NONCOALESCED, genericCommand, null) ;
        
        // It is important to actually fetch the content of the fPendingQCommandsNotYetSent map
        // instead of only using 'cachedCmd'.  This is because although cachedCmd can be considered
        // equal to unqueuedCommand, it is not identical and we need the full content of unqueuedCommand.
        // For instance, cachedCmd does not have the list of requestMonitors that unqueuedCommand has.
        for ( CommandInfo unqueuedCommand : new ArrayList<CommandInfo>(fPendingQCommandsNotYetSent) ) {
            if ( unqueuedCommand.equals( cachedCmd )) {
                fPendingQCommandsNotYetSent.remove(unqueuedCommand);
                fPendingQCommandsSent.add(unqueuedCommand);
                break;
            }
        }
    }
    
	/**
	 * Clears the cache entries for given context.  Clears the whole cache if 
	 * context parameter is null.
	 */
	public void reset(IDMContext dmc) {
	    if (dmc == null) {
	        fCachedContexts.clear();
	        return;
	    }
	    for (Iterator<IDMContext> itr = fCachedContexts.keySet().iterator(); itr.hasNext();) {
	        IDMContext keyDmc = itr.next();
	        if (keyDmc != null && (dmc.equals(keyDmc) || DMContexts.isAncestorOf(keyDmc, dmc))) {
	           itr.remove();
	        }
	    }
	}
}
