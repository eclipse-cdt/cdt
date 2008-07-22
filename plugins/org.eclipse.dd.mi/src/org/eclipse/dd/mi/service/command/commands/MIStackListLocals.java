/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.command.output.MIOutput;
import org.eclipse.dd.mi.service.command.output.MIStackListLocalsInfo;

/**
 * 
 *     -stack-list-locals PRINT-VALUES
 *
 *  Display the local variable names for the current frame.  With an
 * argument of 0 prints only the names of the variables, with argument of 1
 * prints also their values.
 * 
 */
public class MIStackListLocals extends MICommand<MIStackListLocalsInfo> 
{
	
    public MIStackListLocals(IFrameDMContext frameCtx, boolean printValues) {
        this(frameCtx, false, printValues);
    }

    public MIStackListLocals(IFrameDMContext frameCtx, boolean setThread, boolean printValues) {
      super(frameCtx, "-stack-list-locals"); //$NON-NLS-1$
      IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameCtx, IMIExecutionDMContext.class);
      if (setThread && execDmc != null) {
      	 setParameters(new String[] { "--thread", Integer.toString(execDmc.getThreadId()), printValues ? "1" : "0" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else {
      	 setParameters(new String[] { printValues ? "1" : "0" } );  //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    
    @Override
    public MIStackListLocalsInfo getResult(MIOutput out) {
        return new MIStackListLocalsInfo(out);
    }
}
