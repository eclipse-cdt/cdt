/*******************************************************************************
 * Copyright (c) 2008, 2009  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *     -gdb-set
 * 
 */
public class MIGDBSet extends MICommand<MIInfo> 
{
    public MIGDBSet(IDMContext ctx, String[] params) {
        super(ctx, "-gdb-set", null, params); //$NON-NLS-1$
    }
}