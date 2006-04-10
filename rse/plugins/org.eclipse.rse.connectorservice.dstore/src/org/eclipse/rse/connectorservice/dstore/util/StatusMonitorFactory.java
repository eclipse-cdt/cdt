/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.connectorservice.dstore.util;

import java.util.HashMap;

import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * Factory for finding and creating the StatusMonitor class for a given system
 */
public class StatusMonitorFactory
{
    protected HashMap _monitorMap;
    protected static StatusMonitorFactory _instance;
    
    public static StatusMonitorFactory getInstance()
    {
        if (_instance == null)
        {
            _instance = new StatusMonitorFactory();
        }
        return _instance;
    }
    
    public StatusMonitorFactory()
    {
        _monitorMap= new HashMap();
    }
    
    public void removeStatusMonitorFor(IConnectorService system)
    {	
    	StatusMonitor monitor = (StatusMonitor)_monitorMap.remove(system);
    	if (monitor != null)
    	{
    		monitor.dispose();
    	}
    }
    
    public StatusMonitor getStatusMonitorFor(IConnectorService system, DataStore dataStore)
    {
       return getStatusMonitorFor(system, dataStore, null);
    }
    
    public StatusMonitor getStatusMonitorFor(IConnectorService system, DataStore dataStore, ICommunicationsDiagnosticFactory diagnosticFactory)
    {
        StatusMonitor monitor = (StatusMonitor)_monitorMap.get(system);
        if (monitor == null)
        {
            monitor = new StatusMonitor(system, dataStore, diagnosticFactory);
            _monitorMap.put(system, monitor);
        }
        DataStore mDataStore = monitor.getDataStore();
        if (mDataStore != dataStore)
        {
            removeStatusMonitorFor(system);
            monitor = new StatusMonitor(system, dataStore, diagnosticFactory);
            _monitorMap.put(system, monitor);
        }
          
        return monitor;
    }
}