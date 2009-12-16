/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IReverseStepOverHandler;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Command performing a reverse step over
 * @since 2.1
 */
public class GdbReverseStepOverCommand extends GdbAbstractReverseStepCommand implements IReverseStepOverHandler {
	public GdbReverseStepOverCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		super(session, steppingMode);
	}

	@Override
	protected final StepType getStepType() {
		boolean instructionSteppingEnabled = getSteppingMode() != null && getSteppingMode().isInstructionSteppingEnabled();
		return instructionSteppingEnabled ? StepType.INSTRUCTION_STEP_OVER : StepType.STEP_OVER;
	}
}
