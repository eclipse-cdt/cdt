/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoOsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * MIInfoOS
 *
 * -info-os [ type ]
 * If no argument is supplied, the command returns a table of
 * available operating-system-specific information types. If one of these
 * types is supplied as an argument type, then the command returns a
 * table of data of that type.
 *
 * The types of information available depend on the target operating system.
 * @since 4.2
 */
public class MIInfoOs extends MICommand<MIInfoOsInfo> {

	public MIInfoOs(IDMContext ctx) {
		super(ctx, "-info-os"); //$NON-NLS-1$
	}

	public MIInfoOs(IDMContext ctx, String resourceClass) {
		super(ctx, "-info-os", new String[] { resourceClass }); //$NON-NLS-1$
		specificResource = true;
	}

	@Override
	public MIInfoOsInfo getResult(MIOutput out) {

		return new MIInfoOsInfo(out, specificResource);
	}

	private boolean specificResource = false;

}
