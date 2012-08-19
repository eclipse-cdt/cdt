/*******************************************************************************
 * Copyright (c) 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation (Fix for bug 330060)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * @since 4.2
 */
public class ProcessMIInterpreterExecConsole extends MIInterpreterExecConsole<MIInfo> implements IProcessMIInterpreterExecConsole {
    public ProcessMIInterpreterExecConsole(IDMContext ctx, String cmd) {
        super(ctx, cmd);
    }
}