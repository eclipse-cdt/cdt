/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;

/**
 * This interface provides methods for RunControl that are not
 * part of the standard DSF IRunControl
 * 
 * @since 2.0
 */
public interface IMIRunControl extends IRunControl2
{
	/**
	 * Request that the specified steps be executed by first ensuring the target is available
	 * to receive commands.  Once the specified steps are executed, the target should be
	 * returned to its original availability.
	 * 
	 * This is of value for breakpoints commands; e.g., breakpoints need to be inserted
	 * even when the target is running, so this call would suspend the target, insert the
	 * breakpoint, and resume the target again.
	 * 
	 * @since 3.0
	 */
	public void executeWithTargetAvailable(IDMContext ctx, Sequence.Step[] stepsToExecute, RequestMonitor rm);

}

