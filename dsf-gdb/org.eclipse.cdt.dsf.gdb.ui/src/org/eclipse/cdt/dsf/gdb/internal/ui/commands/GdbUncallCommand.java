/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Instruction-level step-return does not make sense (bug 399123)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IUncallHandler;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Command performing an uncall operation (part of Reverse Debugging).
 * @since 2.1
 */
public class GdbUncallCommand extends GdbAbstractReverseStepCommand implements IUncallHandler {
    public GdbUncallCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		super(session, steppingMode);
    }
    
    @Override
    protected final StepType getStepType() {
    	return StepType.STEP_RETURN;
    }
}
