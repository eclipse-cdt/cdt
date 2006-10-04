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

import javax.comm.SerialPort;

import org.eclipse.jface.dialogs.IDialogSettings;

public class TerminalSettings
    implements TerminalConsts
{
    protected String m_strConnType;
    protected String m_strSerialPort;
    protected String m_strBaudRate;
    protected String m_strDataBits;
    protected String m_strStopBits;
    protected String m_strParity;
    protected String m_strFlowControl;
    protected String m_strHost;
    protected String m_strNetworkPort;

    /**
     *
     */
    public TerminalSettings(String terminalPartName)
    {
        importSettings(terminalPartName);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Operations
    //
    
    /**
     *
     */
    public String getConnType()
    {
        return m_strConnType;
    }

    /**
     *
     */
    public void setConnType(String strConnType) 
    {
        m_strConnType = strConnType;
    }

    /**
     *
     */
    public String getSerialPort()
    {
        return m_strSerialPort;
    }

    /**
     *
     */
    public void setSerialPort(String strSerialPort) 
    {
        m_strSerialPort = strSerialPort;
    }

    /**
     *
     */
    public String getBaudRate()
    {
        return m_strBaudRate;
    }

    /**
     *
     */
    public int getBaudRateValue()
    {
        int nBaudRate;
        
        try
        {
            nBaudRate = Integer.parseInt(m_strBaudRate);
        }
        catch(NumberFormatException numberFormatException)
        {
            nBaudRate = 9600;
        }
        
        return nBaudRate;
    }

    /**
     *
     */
    public void setBaudRate(String strBaudRate) 
    {
        m_strBaudRate = strBaudRate;
    }

    /**
     *
     */
    public String getDataBits()
    {
        return m_strDataBits;
    }

    /**
     *
     */
    public int getDataBitsValue()
    {
        if (m_strDataBits.equals("5")) //$NON-NLS-1$
        {
            return SerialPort.DATABITS_5;
        }
        else if (m_strDataBits.equals("6")) //$NON-NLS-1$
        {
            return SerialPort.DATABITS_6;
        }
        else if (m_strDataBits.equals("7")) //$NON-NLS-1$
        {
            return SerialPort.DATABITS_7;
        }
        else // 8
        {
            return SerialPort.DATABITS_8;
        }
    }

    /**
     *
     */
    public void setDataBits(String strDataBits) 
    {
        m_strDataBits = strDataBits;
    }

    /**
     *
     */
    public String getStopBits()
    {
        return m_strStopBits;
    }

    /**
     *
     */
    public int getStopBitsValue()
    {
        if (m_strStopBits.equals("1_5")) //$NON-NLS-1$
        {
            return SerialPort.STOPBITS_1_5;
        }
        else if (m_strStopBits.equals("2")) //$NON-NLS-1$
        {
            return SerialPort.STOPBITS_2;
        }
        else // 1
        {
            return SerialPort.STOPBITS_1;
        }
    }

    /**
     *
     */
    public void setStopBits(String strStopBits) 
    {
        m_strStopBits = strStopBits;
    }

    /**
     *
     */
    public String getParity()
    {
        return m_strParity;
    }

    /**
     *
     */
    public int getParityValue()
    {
        if (m_strParity.equals("Even")) //$NON-NLS-1$
        {
            return SerialPort.PARITY_EVEN;
        }
        else if (m_strParity.equals("Odd")) //$NON-NLS-1$
        {
            return SerialPort.PARITY_ODD;
        }
        else if (m_strParity.equals("Mark")) //$NON-NLS-1$
        {
            return SerialPort.PARITY_MARK;
        }
        else if (m_strParity.equals("Space")) //$NON-NLS-1$
        {
            return SerialPort.PARITY_SPACE;
        }
        else // None
        {
            return SerialPort.PARITY_NONE;
        }
    }

    /**
     *
     */
    public void setParity(String strParity) 
    {
        m_strParity = strParity;
    }

    /**
     *
     */
    public String getFlowControl()
    {
        return m_strFlowControl;
    }

    /**
     *
     */
    public int getFlowControlValue()
    {
        if (m_strFlowControl.equals("RTS/CTS")) //$NON-NLS-1$
        {
            return SerialPort.FLOWCONTROL_RTSCTS_IN;
        }
        else if (m_strFlowControl.equals("Xon/Xoff")) //$NON-NLS-1$
        {
            return SerialPort.FLOWCONTROL_XONXOFF_IN;
        }
        else // None
        {
            return SerialPort.FLOWCONTROL_NONE;
        }
    }

    /**
     *
     */
    public void setFlowControl(String strFlow) 
    {
        m_strFlowControl = strFlow;
    }

     /**
     *
     */
    public String getHost()
    {
        return m_strHost;
    }

    /**
     *
     */
    public void setHost(String strHost) 
    {
        m_strHost = strHost;
    }

    /**
     *
     */
    public String getNetworkPort()
    {
        return m_strNetworkPort;
    }

    /**
     *
     */
    public int getNetworkPortValue()
    {
        int nNetworkPort;
        
        try
        {
            nNetworkPort = Integer.parseInt(m_strNetworkPort);
        }
        catch(NumberFormatException numberFormatException)
        {
            nNetworkPort = 1313;
        }
        
        return nNetworkPort;
    }

    /**
     *
     */
    public void setNetworkPort(String strNetworkPort) 
    {
        m_strNetworkPort = strNetworkPort;
    }

    /**
     * 
     */
    public void importSettings(String terminalPartName)
    {
        TerminalPlugin      plugin;
        TerminalProperties  properties;
        
        plugin              = TerminalPlugin.getDefault();
        properties          = plugin.getTerminalProperties();
        m_strConnType       = importSetting(terminalPartName, "ConnType",       //$NON-NLS-1$
                                            properties.getDefaultConnType());
        m_strSerialPort     = importSetting(terminalPartName, "SerialPort",     //$NON-NLS-1$
                                            properties.getDefaultSerialPort());
        m_strBaudRate       = importSetting(terminalPartName, "BaudRate",       //$NON-NLS-1$
                                            properties.getDefaultBaudRate());
        m_strDataBits       = importSetting(terminalPartName, "DataBits",       //$NON-NLS-1$
                                            properties.getDefaultDataBits());
        m_strStopBits       = importSetting(terminalPartName, "StopBits",       //$NON-NLS-1$
                                            properties.getDefaultStopBits());
        m_strParity         = importSetting(terminalPartName, "Parity",         //$NON-NLS-1$
                                            properties.getDefaultParity());
        m_strFlowControl    = importSetting(terminalPartName, "FlowControl",    //$NON-NLS-1$
                                            properties.getDefaultFlowControl());
        m_strHost           = importSetting(terminalPartName, "Host",           //$NON-NLS-1$
                                            properties.getDefaultHost());
        m_strNetworkPort    = importSetting(terminalPartName, "NetworkPort",    //$NON-NLS-1$
                                            properties.getDefaultNetworkPort());
    }

    /**
     * 
     */
    public void exportSettings(String terminalPartName)
    {
        TerminalPlugin      plugin;
        TerminalProperties  properties;
        
        plugin      = TerminalPlugin.getDefault();
        properties  = plugin.getTerminalProperties();

        exportSetting(terminalPartName, "ConnType", m_strConnType,              //$NON-NLS-1$
                      properties.getDefaultConnType());
        exportSetting(terminalPartName, "SerialPort", m_strSerialPort,          //$NON-NLS-1$
                      properties.getDefaultSerialPort());
        exportSetting(terminalPartName, "BaudRate", m_strBaudRate,              //$NON-NLS-1$
                      properties.getDefaultBaudRate());
        exportSetting(terminalPartName, "DataBits", m_strDataBits,              //$NON-NLS-1$
                      properties.getDefaultDataBits());
        exportSetting(terminalPartName, "StopBits", m_strStopBits,              //$NON-NLS-1$
                      properties.getDefaultStopBits());
        exportSetting(terminalPartName, "Parity", m_strParity,                  //$NON-NLS-1$
                      properties.getDefaultParity());
        exportSetting(terminalPartName, "FlowControl", m_strFlowControl,        //$NON-NLS-1$
                      properties.getDefaultFlowControl());
        exportSetting(terminalPartName, "Host", m_strHost,                      //$NON-NLS-1$
                      properties.getDefaultHost());
        exportSetting(terminalPartName, "NetworkPort", m_strNetworkPort,        //$NON-NLS-1$
                      properties.getDefaultNetworkPort());
    }

    /**
     * 
     */
    protected String importSetting(String terminalPartName, String strName, String strDefault)
    {
        TerminalPlugin  plugin;
        IDialogSettings settings;
        String          strPrefix;
        String          strKey;
        String          strValue;
        
        plugin      = TerminalPlugin.getDefault();
        settings    = plugin.getDialogSettings();
        strPrefix   = TerminalSettings.class.getName() + ".";           //$NON-NLS-1$
        strKey      = strPrefix + terminalPartName + "." + strName;     //$NON-NLS-1$
        strValue    = settings.get(strKey);

        if ((strValue == null)  ||
            (strValue.equals("")))                                      //$NON-NLS-1$
            return strDefault;
            
        return strValue;
    }

    /**
     * 
     */
    protected void exportSetting(String terminalPartName, String strName, String strValue,
                                 String strDefault)
    {
        TerminalPlugin  plugin;
        IDialogSettings settings;
        String          strPrefix;
        String          strKey;
        
        plugin      = TerminalPlugin.getDefault();
        settings    = plugin.getDialogSettings();
        strPrefix   = TerminalSettings.class.getName() + ".";           //$NON-NLS-1$
        strKey      = strPrefix + terminalPartName + "." + strName;     //$NON-NLS-1$
        
        if ((strValue == null)  ||
            (strValue.equals("")))                                      //$NON-NLS-1$
        {
            settings.put(strKey,strDefault);
        }
        else
        {
            settings.put(strKey,strValue);
        }
    }
}
