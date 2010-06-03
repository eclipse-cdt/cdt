/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service.command;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Service which acts as a command control. 
 * 
 * @since 1.1
 */
public interface ICommandControlService extends ICommandControl, IDsfService {
    
    /**
     * Context representing a command control service.  All contexts which
     * originate from a given command control service, should have that 
     * control's context in their hierarchy.
     * 
     * @see ICommandControlService#getContext()
     */
    public interface ICommandControlDMContext extends IDMContext {
        /**
         * Returns the ID of the command control that this context
         * represents.
         */
        public String getCommandControlId();
    } 
    
    /**
     * Event indicating that the back end process has started.
     */
    public interface ICommandControlInitializedDMEvent extends IDMEvent<ICommandControlDMContext> {};
    
    /**
     * Event indicating that the back end process has terminated.
     */
    public interface ICommandControlShutdownDMEvent extends IDMEvent<ICommandControlDMContext> {};

    /**
     * Returns the identifier of this command control service.  It can be used 
     * to distinguish between multiple instances of command control services.   
     */
    public String getId();

    /**
     * returns the context representing this command control.
     */
    public ICommandControlDMContext getContext();

    /**
     * Returns whether this command control is currently active.  A command 
     * control service is active if it has been initialized and has not yet
     * shut down.
     * @return
     */
    public boolean isActive();
}
