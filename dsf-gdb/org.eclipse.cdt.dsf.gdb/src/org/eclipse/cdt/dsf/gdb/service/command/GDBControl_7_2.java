/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Turn on the use of the --thread-group option for GDB 7.2
 * @since 4.0
 */
public class GDBControl_7_2 extends GDBControl_7_0 implements IGDBControl {
    public GDBControl_7_2(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, config, factory);
    	setUseThreadGroupOptions(true);
    }
}
