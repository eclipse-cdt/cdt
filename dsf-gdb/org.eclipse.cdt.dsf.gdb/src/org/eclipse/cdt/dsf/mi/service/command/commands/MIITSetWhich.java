/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * @since 4.9
 */
public class MIITSetWhich extends MIITSet {
	public MIITSetWhich(ICommandControlDMContext ctx) {
		super(ctx, WHICH);
	}
	
	public MIITSetWhich(ICommandControlDMContext ctx, String itsetName) {
		super(ctx, WHICH + " " + itsetName); //$NON-NLS-1$
	}
}
