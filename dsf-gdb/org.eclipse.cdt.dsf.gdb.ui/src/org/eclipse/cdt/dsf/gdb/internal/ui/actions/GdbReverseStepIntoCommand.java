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
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.gdb.actions.IReverseStepIntoHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.jface.viewers.ISelection;

/**
 * @since 2.0
 */
@Immutable
public class GdbReverseStepIntoCommand extends GdbAbstractReverseStepCommand implements IReverseStepIntoHandler {

	public GdbReverseStepIntoCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		super(session, steppingMode);
	}       

	public boolean canReverseStepInto(ISelection debugContext) {
		return canReverseStep(debugContext);
	}

	public void reverseStepInto(ISelection debugContext) {
		reverseStep(debugContext);
	}

	/**
	 * @return the currently active step type
	 */
	 @Override
	 protected final StepType getStepType() {
		 boolean instructionSteppingEnabled = getSteppingMode() != null && getSteppingMode().isInstructionSteppingEnabled();
		 return instructionSteppingEnabled ? StepType.INSTRUCTION_STEP_INTO : StepType.STEP_INTO;
	 }
}

