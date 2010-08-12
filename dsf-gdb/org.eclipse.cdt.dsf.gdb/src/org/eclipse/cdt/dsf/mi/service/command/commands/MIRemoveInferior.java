/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**	
 *   -remove-inferior GROUPID
 *   ^done
 *   
 *   Remove the specified inferior.
 *   
 *   @since 4.0
 */
public class MIRemoveInferior extends MICommand<MIInfo>
{
    public MIRemoveInferior(ICommandControlDMContext dmc, String groupId) {
        super(dmc, "-remove-inferior", new String[] { groupId }); //$NON-NLS-1$
    }
}
