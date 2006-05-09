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

package org.eclipse.rse.connectorservice.ssh;

/** 
 * Markup Interface for subsystems using the SshConnectorService.
 *
 * By implementing this interface, subsystems can be recognized
 * as being able to share a single ssh connector service between
 * multiple different subsystems.
 */
public interface ISshSubSystem {

}