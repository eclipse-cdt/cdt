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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.comm.CommPortIdentifier;

public class TerminalProperties implements TerminalConsts
{
    protected TerminalNetworkPortMap    m_NetworkPortMap;
    protected Vector                    m_ConnTypeTable;
    protected Vector                    m_SerialPortTable;
    protected Vector                    m_BaudRateTable;
    protected Vector                    m_DataBitsTable;
    protected Vector                    m_StopBitsTable;
    protected Vector                    m_ParityTable;
    protected Vector                    m_FlowControlTable;
    protected String                    m_strDefaultConnType;
    protected String                    m_strDefaultSerialPort;
    protected String                    m_strDefaultBaudRate;
    protected String                    m_strDefaultDataBits;
    protected String                    m_strDefaultStopBits;
    protected String                    m_strDefaultParity;
    protected String                    m_strDefaultFlowControl;
    protected String                    m_strDefaultHost;
    protected String                    m_strDefaultNetworkPort;

    /**
     * 
     */
    public TerminalProperties()
    {
        super();
        
        setupProperties();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Operations
    //
    
    /**
     *
     */
    public Vector getConnTypeTable()
    {
        return m_ConnTypeTable;
    }

    /**
     *
     */
    public Vector getSerialPortTable()
    {
        return m_SerialPortTable; 
    }

    /**
     *
     */
    public Vector getBaudRateTable()
    {
        return m_BaudRateTable;
    }

    /**
     *
     */
    public Vector getDataBitsTable()
    {
        return m_DataBitsTable;
    }

    /**
     *
     */
    public Vector getStopBitsTable()
    {
        return m_StopBitsTable;
    }

    /**
     *
     */
    public Vector getParityTable()
    {
        return m_ParityTable;
    }

    /**
     *
     */
    public Vector getFlowControlTable()
    {
        return m_FlowControlTable;
    }

    /**
     *
     */
    public TerminalNetworkPortMap getNetworkPortMap()
    {
        return m_NetworkPortMap;
    }

    /**
     *
     */
    public String getDefaultConnType()
    {
        return m_strDefaultConnType;
    }

    /**
     *
     */
    public String getDefaultSerialPort()
    {
        return m_strDefaultSerialPort; 
    }

    /**
     *
     */
    public String getDefaultBaudRate()
    {
        return m_strDefaultBaudRate;
    }

    /**
     *
     */
    public String getDefaultDataBits()
    {
        return m_strDefaultDataBits;
    }

    /**
     *
     */
    public String getDefaultStopBits()
    {
        return m_strDefaultStopBits;
    }

    /**
     *
     */
    public String getDefaultParity()
    {
        return m_strDefaultParity;
    }

    /**
     *
     */
    public String getDefaultFlowControl()
    {
        return m_strDefaultFlowControl;
    }

    /**
     *
     */
    public String getDefaultHost()
    {
        return m_strDefaultHost;
    }

    /**
     *
     */
    public String getDefaultNetworkPort()
    {
        return m_strDefaultNetworkPort;
    }

    /**
     *
     */
    protected void setupProperties()
    {
        Enumeration         portIdEnum;
        CommPortIdentifier  identifier;
        String              strName;
        int                 nPortType;
        
        portIdEnum                    = CommPortIdentifier.getPortIdentifiers();
        m_NetworkPortMap        = new TerminalNetworkPortMap();
        m_ConnTypeTable         = new Vector();
        m_SerialPortTable       = new Vector();
        m_BaudRateTable         = new Vector();
        m_DataBitsTable         = new Vector();
        m_StopBitsTable         = new Vector();
        m_ParityTable           = new Vector();
        m_FlowControlTable      = new Vector();
        m_strDefaultConnType    = "";    //$NON-NLS-1$
        m_strDefaultSerialPort  = ""; //$NON-NLS-1$
        m_strDefaultBaudRate    = ""; //$NON-NLS-1$
        m_strDefaultDataBits    = ""; //$NON-NLS-1$
        m_strDefaultStopBits    = ""; //$NON-NLS-1$
        m_strDefaultParity      = ""; //$NON-NLS-1$
        m_strDefaultFlowControl = ""; //$NON-NLS-1$
        m_strDefaultHost        = ""; //$NON-NLS-1$
        m_strDefaultNetworkPort = ""; //$NON-NLS-1$
        
        m_ConnTypeTable.add(TERMINAL_CONNTYPE_SERIAL);
        m_ConnTypeTable.add(TERMINAL_CONNTYPE_NETWORK);

        m_BaudRateTable.add("300"); //$NON-NLS-1$
        m_BaudRateTable.add("1200"); //$NON-NLS-1$
        m_BaudRateTable.add("2400"); //$NON-NLS-1$
        m_BaudRateTable.add("4800"); //$NON-NLS-1$
        m_BaudRateTable.add("9600"); //$NON-NLS-1$
        m_BaudRateTable.add("19200"); //$NON-NLS-1$
        m_BaudRateTable.add("38400"); //$NON-NLS-1$
        m_BaudRateTable.add("57600"); //$NON-NLS-1$
        m_BaudRateTable.add("115200"); //$NON-NLS-1$
        
        m_DataBitsTable.add("5"); //$NON-NLS-1$
        m_DataBitsTable.add("6"); //$NON-NLS-1$
        m_DataBitsTable.add("7"); //$NON-NLS-1$
        m_DataBitsTable.add("8"); //$NON-NLS-1$
        
        m_StopBitsTable.add("1"); //$NON-NLS-1$
        m_StopBitsTable.add("1_5"); //$NON-NLS-1$
        m_StopBitsTable.add("2"); //$NON-NLS-1$
        
        m_ParityTable.add("None"); //$NON-NLS-1$
        m_ParityTable.add("Even"); //$NON-NLS-1$
        m_ParityTable.add("Odd"); //$NON-NLS-1$
        m_ParityTable.add("Mark"); //$NON-NLS-1$
        m_ParityTable.add("Space"); //$NON-NLS-1$
        
        m_FlowControlTable.add("None"); //$NON-NLS-1$
        m_FlowControlTable.add("RTS/CTS"); //$NON-NLS-1$
        m_FlowControlTable.add("Xon/Xoff"); //$NON-NLS-1$

        m_strDefaultNetworkPort = m_NetworkPortMap.getDefaultNetworkPort();
        m_strDefaultConnType    = (String) m_ConnTypeTable.get(0);
        m_strDefaultBaudRate    = (String) m_BaudRateTable.get(4);
        m_strDefaultDataBits    = (String) m_DataBitsTable.get(3);
        m_strDefaultStopBits    = (String) m_StopBitsTable.get(0);
        m_strDefaultParity      = (String) m_ParityTable.get(0);
        m_strDefaultFlowControl = (String) m_FlowControlTable.get(0);
        m_strDefaultHost        = ""; //$NON-NLS-1$
        
        while(portIdEnum.hasMoreElements())
        {
            identifier  = (CommPortIdentifier)portIdEnum.nextElement();
            strName     = identifier.getName();
            nPortType   = identifier.getPortType();
            
            if (nPortType == CommPortIdentifier.PORT_SERIAL)
                m_SerialPortTable.addElement(strName);
        }

        Collections.sort(m_SerialPortTable);

        if (!m_SerialPortTable.isEmpty())
        {
            m_strDefaultSerialPort = (String) m_SerialPortTable.get(0);
        }
    }
}
