/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB   		- Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;


/**
 * 
 *    -thread-select THREADNUM
 *
 * Make THREADNUM the current thread.  It prints the number of the new
 * current thread, and the topmost frame for that thread.
 * 
 */

public class MIThreadSelect extends MICommand<MIInfo>
{
	public MIThreadSelect(IDMContext ctx, int threadNum) {
		this(ctx, Integer.toString(threadNum));
	}
	
	/**
     * @since 1.1
     */
	public MIThreadSelect(IDMContext ctx, String threadNum) {
		super(ctx, "-thread-select", new String[]{threadNum}); //$NON-NLS-1$
	}
}
