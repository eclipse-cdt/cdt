/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 ********************************************************************************/

package org.eclipse.rse.core.events;
import java.util.EventListener;

/**
 * Interface that listeners interesting in changes to remote
 * system connections and subsystems can listen implement and
 * subsequently register their interest in via SystemRegistry.
 */
public interface ISystemResourceChangeListener extends EventListener
{
	/**
	 * This is the method in your class that will be called when a
	 * system resource changes.
	 * @see ISystemResourceChangeEvent
	 */
    public void systemResourceChanged(ISystemResourceChangeEvent event);

}