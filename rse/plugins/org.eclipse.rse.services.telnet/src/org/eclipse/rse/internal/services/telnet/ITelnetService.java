/*******************************************************************************
 * Copyright (c) 2006, 2007 Celunite, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sheldon D'souza (Celunite) - initial API and implementation 
 *******************************************************************************/
package org.eclipse.rse.internal.services.telnet;

/** 
 * Markup Interface for services using the TelnetConnectorService.
 *
 * By implementing this interface, services can be recognized
 * as operating against an TelnetConnectorService. The interface 
 * is used as the key in a table for looking up the connector
 * service when needed.
 */
public interface ITelnetService {

}
