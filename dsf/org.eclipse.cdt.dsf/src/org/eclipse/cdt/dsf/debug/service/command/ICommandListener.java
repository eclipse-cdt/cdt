/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service.command;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;

/**
 * Synchronous listener to commands being sent and received.
 * All the registered listeners will be called in the same 
 * dispatch cycle as when the result of the command is submitted. 
 * 
 * @since 1.0
 */

@ConfinedToDsfExecutor("")
public interface ICommandListener {
	/**
	 * Notifies that the specified command has been added to the Command Queue.
	 * It has not yet been sent. In this state the command can be examined  and
	 * possibly withdrawn because it has been coalesced with another command.
	 * 
	 * @return None
	 * @param command Command which has been added to the Queue
 	 */
	public void commandQueued(ICommandToken token);
	
	/**
     * Notification that the given command was sent to the debugger. At this 
     * point the command is no longer in the Command Queue and should not be
     * examined. The only thing which can be done is to try and cancel the
     * command.
     * 
     * @return None
     * @param command
     */
    public void commandSent(ICommandToken token);

	/**
	 * Notifies that the specified command has been removed from the 
	 * Command Queue. This notification means that the command has
	 * been removed from the queue and not sent to the backend. The
	 * user has specifically removed it, perhaps because it has been
	 * combined with another. Or some state change has occured and 
	 * there is no longer a need to get this particular set of data.
	 * 
	 * @return None
	 * @param Command which has been sent to the backend
	 */
	public void commandRemoved(ICommandToken token);
    
	/**
	 * Notifies that the specified command has been completed.
	 * 
	 * @return None
	 * @param Command which has been sent to the backend
	 */
	public void commandDone(ICommandToken token, ICommandResult result);
}
