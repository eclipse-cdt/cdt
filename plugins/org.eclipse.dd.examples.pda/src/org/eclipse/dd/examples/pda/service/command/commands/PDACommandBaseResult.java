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

import org.eclipse.dd.dsf.debug.service.command.ICommand;
import org.eclipse.dd.dsf.debug.service.command.ICommandResult;

/**
 * 
 */
public class PDACommandBaseResult implements ICommandResult {

    final public String fResponseText;
    
    protected PDACommandBaseResult(String response) {
        fResponseText = response;
    }
    
    public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PDACommandBaseResult) {
            PDACommandBaseResult result = (PDACommandBaseResult)obj;
            return fResponseText.equals(result.fResponseText);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return fResponseText.hashCode();
    }
}
