/*******************************************************************************
 * Copyright (c) 2008  Ericsson and others.
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
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;


/**	
 *   -inferior-tty-set TTY
 *   
 * Set terminal for future runs of the program being debugged.
 */
public class MIInferiorTTYSet extends MICommand<MIInfo>
{
    /**
     * @since 1.1
     */
    public MIInferiorTTYSet(ICommandControlDMContext dmc, String tty) {
        super(dmc, "-inferior-tty-set", null, new String[] {tty}); //$NON-NLS-1$
    }
    
    @Deprecated
    public MIInferiorTTYSet(MIControlDMContext dmc, String tty) {
        this ((ICommandControlDMContext)dmc, tty);
    }
}  