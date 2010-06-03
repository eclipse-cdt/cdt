/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;

/**
 * 
 * -thread-info [ thread-id ]
 *
 * Reports information about either a specific thread, if [thread-id] is present,
 * or about all threads. When printing information about all threads, also reports
 * the current thread.
 * @since 1.1
 * 
 */
public class MIThreadInfo extends MICommand<MIThreadInfoInfo> {
	
	public MIThreadInfo(ICommandControlDMContext dmc) {
		super(dmc, "-thread-info"); //$NON-NLS-1$
	}

	public MIThreadInfo(ICommandControlDMContext dmc, String threadId) {
		super(dmc, "-thread-info", new String[]{ threadId }); //$NON-NLS-1$
	}

    @Override
    public MIThreadInfoInfo getResult(MIOutput out) {
        return new MIThreadInfoInfo(out);
    }
}
