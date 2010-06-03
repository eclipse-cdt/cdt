/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * Indicates the end of a sequence of steps. Should be handled like a suspended
 * event to trigger a full refresh of stack frames.
 * 
 * @since 1.1
 */
public class FullStackRefreshEvent extends AbstractDMEvent<IExecutionDMContext> {
    
    public FullStackRefreshEvent(IExecutionDMContext execCtx) {
        super(execCtx);
    }

}
