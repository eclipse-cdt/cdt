/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoOsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 4.1
 */
public class MIInfoOs extends MICommand<MIInfoOsInfo> {
	
	public MIInfoOs(IDMContext ctx, String resourceClass)	
	{
		super(ctx, "-info-os", new String[]{resourceClass}); //$NON-NLS-1$
	}
	
	@Override
	public MIInfoOsInfo getResult(MIOutput out) {
		return new MIInfoOsInfo(out);
	}

}
