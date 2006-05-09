/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.services.ssh;

/** 
 * Markup Interface for services using the SshConnectorService.
 *
 * By implementing this interface, services can be recognized
 * as operating against an SshConnectorService. The interface 
 * is used as the key in a table for looking up the connector
 * service when needed.
 */
public interface ISshService {

}
