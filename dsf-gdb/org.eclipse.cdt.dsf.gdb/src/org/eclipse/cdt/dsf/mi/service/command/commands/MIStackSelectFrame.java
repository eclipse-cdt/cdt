/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *     -stack-select-frame FRAMENUM
 *
 *  Change the current frame.  Select a different frame FRAMENUM on the
 * stack.
 * 
 */
public class MIStackSelectFrame extends MICommand<MIInfo> {
	
	public MIStackSelectFrame(IDMContext ctx, int frameNum) {
		super(ctx, "-stack-select-frame", new String[]{Integer.toString(frameNum)}); //$NON-NLS-1$
	}
}
