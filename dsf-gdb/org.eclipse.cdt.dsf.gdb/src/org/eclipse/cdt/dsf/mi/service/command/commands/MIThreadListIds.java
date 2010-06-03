/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson				- Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadListIdsInfo;

/**
 * 
 *    -thread-list-ids
 *
 * Produces a list of the currently known GDB thread ids.  At the end
 * of the list it also prints the total number of such threads.
 * 
 */
public class MIThreadListIds extends MICommand<MIThreadListIdsInfo> {
	
	public MIThreadListIds(IContainerDMContext contDmc) {
		super(contDmc, "-thread-list-ids"); //$NON-NLS-1$
	}

    @Override
    public MIThreadListIdsInfo getResult(MIOutput out) {
        return new MIThreadListIdsInfo(out);
    }
}
