/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service;

import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;

/**
 * An execution group context object.  In the GDB/MI protocol, thread groups
 * are represented by a string identifier, which is the basis for this context.
 * @since 1.1
 */
public interface IMIExecutionGroupDMContext extends IContainerDMContext 
{
    /**
     * Returns the GDB/MI thread group identifier of this context.
     */
    public String getGroupId();
}
