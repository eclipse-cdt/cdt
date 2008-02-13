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

import org.eclipse.dd.dsf.datamodel.IDMContext;

/**
 * Sets what events cause the execution to stop.
 * 
 * <pre>
 *    C: eventstop {event_name} {0|1}
 *    R: ok
 *    ...
 *    E: suspended event {event_name}
 * </pre>
 * 
 * Where event_name could be <code>unimpinstr</code> or <code>nosuchlabel</code>.  
 */
public class PDAEventStopCommand extends PDACommandBase<PDACommandBaseResult> {

    public enum Event { UNIMPINSTR, NOSUCHLABEL };
    
    public PDAEventStopCommand(IDMContext context, Event event, boolean enable) {
        super(context, 
              "eventstop " + 
              (event == Event.UNIMPINSTR ? "unimpinstr " : "nosuchlabel ") + 
              (enable ? "1" : "0"));
    }
    
    @Override
    public PDACommandBaseResult createResult(String resultText) {
        return new PDACommandBaseResult(resultText);
    }
}
