/*******************************************************************************
 * Copyright (c) 2013  AdaCore and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Philippe Gil (AdaCore) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *     -gdb-show
 *     
 * @since 4.3
 * 
 */
public class MIGDBShow<V extends MIInfo> extends MICommand<V> 
{
    public MIGDBShow(IDMContext ctx, String[] params) {
        super(ctx, "-gdb-show", null, params); //$NON-NLS-1$
    }
}