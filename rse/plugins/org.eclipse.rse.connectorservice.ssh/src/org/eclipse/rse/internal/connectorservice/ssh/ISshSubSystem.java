/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.internal.connectorservice.ssh;

/** 
 * Markup Interface for subsystems using the SshConnectorService.
 *
 * By implementing this interface, subsystems can be recognized
 * as being able to share a single ssh connector service between
 * multiple different subsystems.
 */
public interface ISshSubSystem {

}