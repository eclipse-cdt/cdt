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

package org.eclipse.rse.filters;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * An interface required if you wish to be called back by the 
 *  system filter wizard, when the user selects a target filter pool.
 */
public interface ISystemFilterPoolSelectionValidator 
{
		
    /**
     * Delimiter used to qualify filter names by filter pool name, when calling
     *  filter pool selection validator in new filter wizard.
     */
    public static final String DELIMITER_FILTERPOOL_FILTER = "_____";

    /**
     * Validate the given selection.
     * @param filterPool the user-selected filter pool
     * @return null if no error, else a SystemMessage
     */
    public SystemMessage validate(ISystemFilterPool filterPool);
    
}