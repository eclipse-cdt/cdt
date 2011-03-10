/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;


/**	
 *   -inferior-tty-set TTY
 *   
 * Set terminal for future runs of the specified program.
 */
public class MIInferiorTTYSet extends MICommand<MIInfo>
{
    /**
     * @since 4.0
     */
    public MIInferiorTTYSet(IMIContainerDMContext dmc, String tty) {
        super(dmc, "-inferior-tty-set", null, new String[] {tty}); //$NON-NLS-1$
    }
}  