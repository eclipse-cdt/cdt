/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/
package org.eclipse.rse.core.subsystems;

/**
 * A delegating connector service forwards all requests for information to
 * another connector service.
 */
public interface IDelegatingConnectorService extends IConnectorService
{
	/**
	 * @return the connector service that this connector service will
	 * forward requests to.
	 */
	public IConnectorService getRealConnectorService();
}
