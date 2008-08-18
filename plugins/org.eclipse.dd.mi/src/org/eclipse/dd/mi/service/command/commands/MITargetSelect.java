/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * This command connects to a remote target.
 */
public class MITargetSelect extends MICommand<MIInfo> {

	public MITargetSelect(IDMContext ctx, String host, String port) {
		super(ctx, "-target-select extended-remote", new String[] {host + ":" + port}); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public MITargetSelect(IDMContext ctx, String serialDevice) {
		super(ctx, "-target-select extended-remote", new String[] {serialDevice}); //$NON-NLS-1$
	}

}
