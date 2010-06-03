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

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.examples.dsf.pda.service.PDAVirtualMachineDMContext;

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
@Immutable
public class PDAEventStopCommand extends AbstractPDACommand<PDACommandResult> {

    public enum Event { UNIMPINSTR, NOSUCHLABEL };
    
    public PDAEventStopCommand(PDAVirtualMachineDMContext context, Event event, boolean enable) {
        super(context, 
              "eventstop " + 
              (event == Event.UNIMPINSTR ? "unimpinstr " : "nosuchlabel ") + 
              (enable ? "1" : "0"));
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
