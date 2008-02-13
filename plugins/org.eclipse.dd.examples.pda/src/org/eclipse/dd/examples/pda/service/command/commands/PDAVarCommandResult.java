/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.command.commands;


/**
 * @see PDAVarCommand
 */
public class PDAVarCommandResult extends PDACommandBaseResult {
    
    final public int fValue;
    
    PDAVarCommandResult(String response) {
        super(response);
        fValue = Integer.parseInt(response);
    }
}
