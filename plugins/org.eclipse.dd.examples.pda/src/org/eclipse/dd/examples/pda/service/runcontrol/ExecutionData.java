/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.runcontrol;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;

@Immutable 
class ExecutionData implements IExecutionDMData {
    private final StateChangeReason fReason;
    ExecutionData(StateChangeReason reason) {
        fReason = reason;
    }
    public StateChangeReason getStateChangeReason() { return fReason; }
}