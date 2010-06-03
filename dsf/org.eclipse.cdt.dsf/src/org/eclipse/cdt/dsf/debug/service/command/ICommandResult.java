/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service.command;

/**
 * @since 1.0
 */
public interface ICommandResult {
    /**
     *  Returns an ICommandResult which is a subset command result. The command
     *  result which is being passed in is from a coalesced command. The result
     *  which is desired is contained within those results. In this instance we
     *  are processing the command result from the coalesced command to get our
     *  command result.
     *  <i>Note:</i> The type of returned command result must match the type 
     *  associated with the subset command that is passed in the argument.   
     * 
     *  @return result for this particular command.
     */
    public <V extends ICommandResult> V getSubsetResult( ICommand<V> command );
}
