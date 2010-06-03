/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;

/**
 * Base class for PDA commands.  The PDA commands consist of a text request and 
 * a context.  Since the PDA debugger protocol is stateless, the context is only 
 * needed to satisfy the ICommand interface.   
 */
@Immutable
abstract public class AbstractPDACommand<V extends PDACommandResult> implements ICommand<V> {

    final private IDMContext fContext;
    final private String fRequest;
    
    public AbstractPDACommand(IDMContext context, String request) {
        fContext = context;
        fRequest = request;
    }
    
    public IDMContext getContext() {
        return fContext;
    }
    
    public ICommand<? extends ICommandResult> coalesceWith(ICommand<? extends ICommandResult> command) {
        return null;
    }

    /**
     * Returns the request to be sent to PDA. 
     */
    public String getRequest() {
        return fRequest;
    }

    /**
     * Returns the command result based on the given PDA response.  This command 
     * uses the class type parameter as the return type to allow the compiler to 
     * enforce the correct command result.  This class must be implemented by 
     * each command to create the concrete result type. 
     */
    abstract public V createResult(String resultText);
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractPDACommand) {
            AbstractPDACommand<?> cmd = (AbstractPDACommand<?>)obj;
            return fContext.equals(cmd.fContext) && fRequest.equals(cmd.fRequest);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return fContext.hashCode() + fRequest.hashCode();
    }
    
}
