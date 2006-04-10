/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;
/**
 * This interface allows listeners to be kept informed when a long
 *  running request starts and stops. 
 */
public interface ISystemLongRunningRequestListener 
{
	
    /**
     * A long running request is starting
     */
    public void startingLongRunningRequest(SystemLongRunningRequestEvent event);
    /**
     * A long running request is finishing
     */
    public void endingLongRunningRequest(SystemLongRunningRequestEvent event);
}