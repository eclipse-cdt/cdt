/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.datamodel;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Interface for DSF services that provide model data to clients.  
 * <p>
 * For completeness this service interface derives from <code>IDMData</data> 
 * and has a method which allows clients to retrieve the DM Context that 
 * represents the service data. 
 * 
 * @deprecated Without getModelData method this service has no function.
 * There's also no need for it as a marker interface so we may as well
 * get rid of it.
 * 
 * @since 1.0
 */
public interface IDMService extends IDsfService {
    /**
     * Retrieves model data object for given context.  This method makes it
     * un-necessary for every model service to declare a separate method 
     * for retrieving model data of specific type.
     * 
     * @param <V> The Data Model Data type that is to be retrieved.
     * @param dmc Data Model Context for the data model data object to be retrieved.
     * @param rm Request completion monitor to be filled in with the Data Model Data.
     * 
     * @deprecated This method is now deprecated as there is no compile-time linking
     * between IDMContext and IDMData objects (see bug 205132)
     */
    @Deprecated
    void getModelData(IDMContext dmc, DataRequestMonitor<?> rm);
}
