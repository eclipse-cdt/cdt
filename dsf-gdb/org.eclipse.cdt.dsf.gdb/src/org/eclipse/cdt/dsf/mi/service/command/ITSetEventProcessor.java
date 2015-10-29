/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation, using 
 *     							MIRunControlEventProcessor_7_0 as model
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MINamedITSetCreated;
import org.eclipse.cdt.dsf.mi.service.command.events.MINamedITSetDeleted;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * Event processor for events related to ITSets
 * 
 * Expected events examples:
 * - ITSet creation: 
 *   =named-itset-created,name="Group-1",spec="tt1.t,tt1.2"
 * - ITSet deletion  
 *   =named-itset-deleted,name="Group-1"
 *   
 * @since 5.0
 */
public class ITSetEventProcessor implements IEventProcessor {

	/**
     * The connection service that this event processor is registered with.
     */
    private final AbstractMIControl fCommandControl;
    /**
     * Container context used as the context for the run control events generated
     * by this processor.
     */
    private final ICommandControlDMContext fControlDmc; 
    
    
	public ITSetEventProcessor(AbstractMIControl connection, ICommandControlDMContext controlDmc) {
		fCommandControl = connection;
		fControlDmc = controlDmc;
		
		// register to be used as an event processor
		connection.addEventListener(this);
	}
	
	@Override
	public void dispose() {
		fCommandControl.removeEventListener(this);	
	}

	@Override
	public void eventReceived(Object output) {
		for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
    		if (oobr instanceof MINotifyAsyncOutput) {
    			// Parse the string and dispatch the corresponding event
    			MINotifyAsyncOutput exec = (MINotifyAsyncOutput) oobr;
    			String miEvent = exec.getAsyncClass();
    			if ("named-itset-created".equals(miEvent) || "named-itset-deleted".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
    				String name = null;
    				String spec = null;

    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("name")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							name = ((MIConst) val).getString();
    						}
    					} else if (var.equals("spec")) { //$NON-NLS-1$
    		    			if (val instanceof MIConst) {
    							spec = ((MIConst) val).getString();
    		    			}
    		    		}
    				}
    				
		    		// create MI event and dispatch
    				MIEvent<?> event = null;
    				if ("named-itset-created".equals(miEvent)) { //$NON-NLS-1$
    					assert name != null && spec != null;
    					event = new MINamedITSetCreated(fControlDmc, exec.getToken(), name, spec);
    				}
    				else if ("named-itset-deleted".equals(miEvent)) { //$NON-NLS-1$
    					assert name != null;
    					event = new MINamedITSetDeleted(fControlDmc, exec.getToken(), name);
    				}
    				else {
		    			assert false;	// earlier check should have guaranteed this isn't possible
		    		}
    				
    		    	fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
    			}
    		}
    	}
	}
	
	@Override
	public void commandQueued(ICommandToken token) {
		// Do nothing.
	}

	@Override
	public void commandSent(ICommandToken token) {
		// Do nothing.
	}

	@Override
	public void commandRemoved(ICommandToken token) {
		// Do nothing.
	}

	@Override
	public void commandDone(ICommandToken token, ICommandResult result) {
		// Do nothing.
	}

}
