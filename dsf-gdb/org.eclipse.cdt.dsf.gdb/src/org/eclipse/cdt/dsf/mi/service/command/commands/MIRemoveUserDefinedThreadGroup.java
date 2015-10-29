/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * @since 5.1
 */
public class MIRemoveUserDefinedThreadGroup extends MICommand<MIInfo> {

	public MIRemoveUserDefinedThreadGroup(IDMContext ctx, String groupId) {
		super(ctx, "-remove-user-defined-thread-group", new String[] { groupId }); //$NON-NLS-1$
	}

}
