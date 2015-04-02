/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * @since 4.7
 */
public class MIVarSetFrozen extends MICommand<MIInfo>
{
    public MIVarSetFrozen(ICommandControlDMContext ctx, String name, boolean frozen) {
        super(ctx, "-var-set-frozen"); //$NON-NLS-1$
        setParameters(new String[]{name, frozen ? "1" : "0"}); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
