/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.command.commands;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommand;
import org.eclipse.dd.dsf.debug.service.command.ICommandResult;

/**
 * 
 */
abstract public class PDACommandBase<V extends PDACommandBaseResult> implements ICommand<V> {

    final private IDMContext fContext;
    final private String fRequest;
    
    public PDACommandBase(IDMContext context, String request) {
        fContext = context;
        fRequest = request;
    }
    
    public IDMContext getContext() {
        return fContext;
    }
    
    public ICommand<? extends ICommandResult> coalesceWith(ICommand<? extends ICommandResult> command) {
        return null;
    }
    
    public String getRequest() {
        return fRequest;
    }
    
    abstract public V createResult(String resultText);
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PDACommandBase) {
            PDACommandBase<?> cmd = (PDACommandBase<?>)obj;
            return fContext.equals(cmd.fContext) && fRequest.equals(cmd.fRequest);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return fContext.hashCode() + fRequest.hashCode();
    }
    
}
