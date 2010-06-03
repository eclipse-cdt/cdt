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

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * API for sending commands to the debugger and for receiving command results
 * and asynchronous events.  The command control may be implemented by a service
 * or a non-service object.
 * 
 * @see ICommandControlService
 * 
 * @since 1.0
 */
public interface ICommandControl {

    /**
     * Adds the specified command to the queue of commands to be processed. 
     *   
     * @param command Specific command to be processed
     * @param rm Request completion monitor
     * @return None
     */
    <V extends ICommandResult> ICommandToken queueCommand(ICommand<V> command, DataRequestMonitor<V> rm);
    
    /**
     * Removes the specified command from the processor queue.
     *   
     * @param command Specific command to be removed
     * @return None
     */
    void removeCommand(ICommandToken token);
    
    /**
	 * Adds a notification handler for the Command processor.
	 * 
	 * @param command listener to be added
	 * @return None
	 */
    void addCommandListener(ICommandListener listener);
    
    /**
	 * Removes a notification handler for the Command processor.
	 * 
	 * @param command listener to be removed
	 * @return None
	 */
    void removeCommandListener(ICommandListener listener);
    
    /**
	 * Adds a notification handler for the Event processor.
	 * 
	 * @param event listener to be added
	 * @return None
	 */
    void addEventListener(IEventListener listener);
    
    /**
	 * Removes a notification handler for the Event processor.
	 * 
	 * @param event listener to be removed
	 * @return None
	 */
    void removeEventListener(IEventListener listener);
}
