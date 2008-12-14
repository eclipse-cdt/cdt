/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [258631][api] ITerminalService should be public API
 *******************************************************************************/

package org.eclipse.rse.services.terminals;

import org.eclipse.rse.services.AbstractService;

/**
 * Abstract base class for clients to create an ITerminalService.
 *
 * Currently the main value of this class is to protect extenders from API
 * breakage as the ITerminalService is evolved in the future. We might, for
 * instance, be adding state of the terminal service or capabilities for getting
 * environment variables.
 *
 * @see ITerminalService
 * @since org.eclipse.rse.services 3.1
 */
public abstract class AbstractTerminalService extends AbstractService implements ITerminalService {

	// empty for now, extenders need to implement the launchTerminal() method
	// themselves

}
