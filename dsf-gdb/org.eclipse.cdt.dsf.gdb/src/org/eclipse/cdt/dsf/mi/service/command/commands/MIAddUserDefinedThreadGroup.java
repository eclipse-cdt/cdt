/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddUserDefinedThreadGroupInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 5.1
 */
public class MIAddUserDefinedThreadGroup extends MICommand<MIAddUserDefinedThreadGroupInfo> {

	public MIAddUserDefinedThreadGroup(IDMContext ctx, String groupName, String spec) {
		super(ctx, "-add-user-defined-thread-group"); //$NON-NLS-1$
		final ArrayList<String> arguments = new ArrayList<String>();
		
		arguments.add(groupName);
		arguments.add(spec);
		
		setParameters(arguments.toArray(new String[0]));
	}

	@Override
    public MIAddUserDefinedThreadGroupInfo getResult(MIOutput out) {
        return new MIAddUserDefinedThreadGroupInfo(out);
    }
}
