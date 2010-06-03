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
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.concurrent.Immutable;


/**
 * @see PDARegistersCommand
 */
@Immutable
public class PDARegistersCommandResult extends PDACommandResult {
    
    /**
     * Array of registers returned by the registers commands.  
     */
    final public PDARegister[] fRegisters;
    
    PDARegistersCommandResult(String response) {
        super(response);
        StringTokenizer st = new StringTokenizer(response, "#");
        List<PDARegister> regList = new ArrayList<PDARegister>();
        
        while (st.hasMoreTokens()) {
            regList.add(new PDARegister(st.nextToken()));
        }
        fRegisters = regList.toArray(new PDARegister[regList.size()]);
    }
}
