/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Marc Khouzam (Ericsson) - Added IReverseModeChangedDMEvent (Bug 399163)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * @since 2.0
 */
public interface IReverseRunControl {

    /** @since 4.2 */
    public interface IReverseModeChangedDMEvent extends IDMEvent<ICommandControlDMContext> {
    	boolean isReverseModeEnabled();
    };

	void canReverseResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm);
    void reverseResume(IExecutionDMContext context, RequestMonitor requestMonitor);
    boolean isReverseStepping(IExecutionDMContext context);
    void canReverseStep(IExecutionDMContext context, StepType stepType, DataRequestMonitor<Boolean> rm);
    void reverseStep(IExecutionDMContext context, StepType stepType, RequestMonitor requestMonitor);
    
    void canEnableReverseMode(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm);
    void isReverseModeEnabled(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm);
    void enableReverseMode(ICommandControlDMContext context, boolean enable, RequestMonitor rm);
}
