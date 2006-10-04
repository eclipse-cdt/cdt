/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;


public class TerminalNetworkPortMap extends HashMap
    implements TerminalConsts
{
	static final long serialVersionUID = 0;
	
    public TerminalNetworkPortMap()
    {
        super();
        
        setupMap();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Operations
    //
    
    /**
     * 
     */
    public String getDefaultNetworkPort()
    {
        return (String) get(TERMINAL_PROP_NAMETELNET);
    }

    /**
     * 
     */
    public String findPortName(String strPort)
    {
        Collection  values;
        Vector      portTable;
        Vector      nameTable;
        String      strPortName;
        int         nIndex;
        
        values      = values();
        portTable   = new Vector(values);
        nIndex      = portTable.indexOf(strPort);
        nameTable   = getNameTable();
        
        if (nIndex == -1)
            return strPort;
            
        strPortName = (String) nameTable.get(nIndex);
        return strPortName;
    }
    
    /**
     * 
     */
    public String findPort(String strPortName)
    {   
        String strPort;
         
        strPort = (String) get(strPortName);
        if (strPort == null)
            return strPortName;
            
        return strPort;
    }
    
    /**
     * 
     */
    public Vector getNameTable()
    {
        Set     keySet;
        Vector  nameTable;
        
        keySet      = keySet();
        nameTable   = new Vector(keySet);

        return nameTable; 
    }
    
    /**
     * 
     */
    protected void setupMap()
    {
        put(TERMINAL_PROP_NAMETGTCONST, TERMINAL_PROP_VALUETGTCONST);
        put(TERMINAL_PROP_NAMETELNET, TERMINAL_PROP_VALUETELNET);
    }
}
