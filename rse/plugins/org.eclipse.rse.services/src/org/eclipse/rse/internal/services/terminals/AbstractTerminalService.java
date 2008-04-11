/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.services.terminals;

import org.eclipse.rse.services.AbstractService;

/**
 * Abstract base class for clients to create an ITerminalService.
 *
 * Currently the main value of this class is to protect extenders from API
 * breakage as the ITerminalService is evolved in the future. We might, for
 * instance, be adding state of the terminal service or capabilities for getting
 * environment variables.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a>
 * team.
 * </p>
 *
 * @see ITerminalService
 * @since org.eclipse.rse.services 3.0
 */
public abstract class AbstractTerminalService extends AbstractService implements ITerminalService {

	// empty for now, extenders need to implement the launchTerminal() method
	// themselves

}
