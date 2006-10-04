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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class TerminalSettingsDlg extends org.eclipse.jface.dialogs.Dialog
    implements  TerminalTarget,
                TerminalConsts
{
    protected static final String m_strHelpID = "hid_db_terminal"; //$NON-NLS-1$

    protected Combo             m_ctlConnTypeCombo;
    protected Combo             m_ctlSerialPortCombo;
    protected Combo             m_ctlBaudRateCombo;
    protected Combo             m_ctlDataBitsCombo;
    protected Combo             m_ctlStopBitsCombo;
    protected Combo             m_ctlParityCombo;
    protected Combo             m_ctlFlowControlCombo;
    protected Text              m_ctlHostText;
    protected Combo             m_ctlNetworkPortCombo;
    protected Group             m_wndSettingsGroup;
    protected Composite         m_wndSettingsPanel;
    protected TerminalSettings  m_TerminalSettings;
    protected String            m_strConnType;
    protected int               m_nStatus;

    /**
     *
     */
    public TerminalSettingsDlg(Shell wndParent)
    {
        super(wndParent);

        m_nStatus = TERMINAL_ID_CANCEL;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TerminalTarget interface
    //
    
    /**
     *
     */
    public void execute(String strMsg,Object data)
	{
		if (strMsg.equals(ON_CONNTYPE_SELECTED))
		{
			onConnTypeSelected(data);
		}
        else if (strMsg.equals(ON_OK))
		{
			onOk(data);
		}
		else if (strMsg.equals(ON_CANCEL))
		{
			onCancel(data);
		}
		else if (strMsg.equals(ON_HELP))
		{
			onHelp(data);
		}
		else
		{
		}
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Message handlers
    //
    
    /**
     *
     */
    protected void onConnTypeSelected(Object data)
	{
        String strConnType;
        
        strConnType = m_ctlConnTypeCombo.getText();
        if (m_strConnType.equals(strConnType))
            return;
        
        m_strConnType = strConnType;

        if (strConnType.equals(TERMINAL_CONNTYPE_SERIAL))
        {
            setupSerialPanel();
        }
        else if (strConnType.equals(TERMINAL_CONNTYPE_NETWORK))
        {
            setupNetworkPanel();
        }
        else
        {
            setupConnTypeNotSupportedPanel();
        }
    }
	
    /**
     *
     */
    protected void onOk(Object data)
	{
		if (!validateSettings())
			return;

        saveSettings();
        m_nStatus = TERMINAL_ID_CONNECT;
    }
	
    /**
     *
     */
	protected void onCancel(Object data)
	{
        m_nStatus = TERMINAL_ID_CANCEL;
	}

    /**
     *
     */
	protected void onHelp(Object data)
	{
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Dialog interface
    //
    
    /**
     * 
     */
    protected void okPressed()
    {
        execute(ON_OK,null);
        super.okPressed();
    }

    /**
     * 
     */
    protected void cancelPressed()
    {
        execute(ON_CANCEL,null);
        super.cancelPressed();
        
    }

    /**
     * 
     */
    public int open() 
    {
        int nShellStyle;
        
        nShellStyle = getShellStyle();
        nShellStyle = nShellStyle|SWT.RESIZE;
        setShellStyle(nShellStyle);

        return super.open();
    }
    
    /**
     * 
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        
        newShell.setText(TERMINAL_TEXT_TERMINALSETTINGS);
    }
    
    /**
     * 
     */
    protected Control createDialogArea(Composite parent) 
    {
        Composite ctlComposite;
        
        ctlComposite = (Composite)super.createDialogArea(parent);
        createDialog(ctlComposite);

        return ctlComposite;
    }

    /**
     * 
     */
    protected void initializeBounds() 
    {
        setConnType(TERMINAL_CONNTYPE_SERIAL);
        execute(ON_CONNTYPE_SELECTED,null);
        super.initializeBounds();
        setConnType(m_TerminalSettings.getConnType());
        execute(ON_CONNTYPE_SELECTED,null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Operations
    //

    /**
     *
     */
    public void loadSettings(TerminalSettings terminalSettings)
    {
        m_TerminalSettings = terminalSettings; 
    }
    
    /**
     *
     */
    public void saveSettings()
    {
        String strConnType;
        
        strConnType = getConnType();
        
        if (strConnType.equals(TERMINAL_CONNTYPE_SERIAL))
        {
            m_TerminalSettings.setConnType(getConnType());
            m_TerminalSettings.setSerialPort(getSerialPort());
            m_TerminalSettings.setBaudRate(getBaudRate());
            m_TerminalSettings.setDataBits(getDataBits());
            m_TerminalSettings.setStopBits(getStopBits());
            m_TerminalSettings.setParity(getParity());
            m_TerminalSettings.setFlowControl(getFlowControl());
        }
        else if (strConnType.equals(TERMINAL_CONNTYPE_NETWORK))
        {
            m_TerminalSettings.setConnType(getConnType());
            m_TerminalSettings.setHost(getHost());
            m_TerminalSettings.setNetworkPort(getNetworkPort());
        }
    }

    /**
     *
     */
    public int getStatus()
    {
        return m_nStatus;
    }

    /**
     *
     */
    protected void setConnType(String strConnType)
    {
        int nIndex;
        
        nIndex = m_ctlConnTypeCombo.indexOf(strConnType);
        if (nIndex == -1)
            return;
            
        m_ctlConnTypeCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setSerialPort(String strSerialPort)
    {
        int nIndex;
        
        nIndex = m_ctlSerialPortCombo.indexOf(strSerialPort);
        if (nIndex == -1)
            return;
            
        m_ctlSerialPortCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setBaudRate(String strBaudRate)
    {
        int nIndex;
        
        nIndex = m_ctlBaudRateCombo.indexOf(strBaudRate);
        if (nIndex == -1)
            return;
            
        m_ctlBaudRateCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setDataBits(String strDataBits)
    {
        int nIndex;
        
        nIndex = m_ctlDataBitsCombo.indexOf(strDataBits);
        if (nIndex == -1)
            return;
            
        m_ctlDataBitsCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setStopBits(String strStopBits)
    {
        int nIndex;
        
        nIndex = m_ctlStopBitsCombo.indexOf(strStopBits);
        if (nIndex == -1)
            return;
            
        m_ctlStopBitsCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setParity(String strParity)
    {
        int nIndex;
        
        nIndex = m_ctlParityCombo.indexOf(strParity);
        if (nIndex == -1)
            return;
            
        m_ctlParityCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setFlowIn(String strFlowIn)
    {
        int nIndex;
        
        nIndex = m_ctlFlowControlCombo.indexOf(strFlowIn);
        if (nIndex == -1)
            return;
            
        m_ctlFlowControlCombo.select(nIndex);
    }
    
    /**
     *
     */
    protected void setHost(String strHost)
    {
        m_ctlHostText.setText(strHost);
    }
    
    /**
     *
     */
    protected void setNetworkPort(String strNetworkPort)
    {
        TerminalPlugin          plugin;
        TerminalProperties      properties;
        TerminalNetworkPortMap  networkPortMap;
        String                  strPortName;
        int                     nIndex;

        plugin          = TerminalPlugin.getDefault();
        properties      = plugin.getTerminalProperties();
        networkPortMap  = properties.getNetworkPortMap();
        strPortName     = networkPortMap.findPortName(strNetworkPort);
        nIndex          = m_ctlNetworkPortCombo.indexOf(strPortName);
        
        if (nIndex == -1)
        {
            m_ctlNetworkPortCombo.setText(strNetworkPort);
        }
        else
        {
            m_ctlNetworkPortCombo.select(nIndex);
        }
    }
    
    /**
     *
     */
    protected String getConnType()
    {
        int nIndex;
        
        nIndex = m_ctlConnTypeCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlConnTypeCombo.getItem(nIndex);
    }
    
    /**
     *
     */
    protected String getSerialPort()
    {
        int nIndex;
        
        nIndex = m_ctlSerialPortCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlSerialPortCombo.getItem(nIndex);
    }
    
    /**
     *
     */
    protected String getBaudRate()
    {
        int nIndex;
        
        nIndex = m_ctlBaudRateCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlBaudRateCombo.getItem(nIndex);
    }
    
    /**
     *
     */
    protected String getDataBits()
    {
        int nIndex;
        
        nIndex = m_ctlDataBitsCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlDataBitsCombo.getItem(nIndex);
    }
    
    /**
     *
     */
    protected String getStopBits()
    {
        int nIndex;
        
        nIndex = m_ctlStopBitsCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlStopBitsCombo.getItem(nIndex);
    }
    
    /**
     *
     */
    protected String getParity()
    {
        int nIndex;
        
        nIndex = m_ctlParityCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlParityCombo.getItem(nIndex);
    }
    
    /**
     *
     */
    protected String getFlowControl()
    {
        int nIndex;
        
        nIndex = m_ctlFlowControlCombo.getSelectionIndex();
        if (nIndex == -1)
            return ""; //$NON-NLS-1$
            
        return m_ctlFlowControlCombo.getItem(nIndex);
    }
   
    /**
     *
     */
    protected String getHost()
    {
        return m_ctlHostText.getText();
    }
    
    /**
     *
     */
    protected String getNetworkPort()
    {
        TerminalPlugin          plugin;
        TerminalProperties      properties;
        TerminalNetworkPortMap  networkPortMap;
        String                  strPortName;
        String                  strPort;

        plugin          = TerminalPlugin.getDefault();
        properties      = plugin.getTerminalProperties();
        networkPortMap  = properties.getNetworkPortMap();
        strPortName     = m_ctlNetworkPortCombo.getText();
        strPort         = networkPortMap.findPort(strPortName);

        return strPort;
    }
    
    /**
     *
     */
    protected boolean validateSettings()
    {
        return true;
    }
	
    /**
     *
     */
    protected void createDialog(Composite ctlComposite)
    {
        setupData();
        setupPanel(ctlComposite);
		setupListeners();
    }
    
    /**
     *
     */
    protected void setupData()
    {
        m_strConnType = ""; //$NON-NLS-1$
    }
    
    /**
     *
     */
	protected void setupPanel(Composite wndParent)
	{
        setupConnTypePanel(wndParent);
		setupSettingsGroup(wndParent);
	}

    /**
     *
     */
    protected void setupConnTypePanel(Composite wndParent)
    {
        Group       wndGroup;
        GridLayout  gridLayout;
        GridData    gridData;
        
        wndGroup    = new Group(wndParent,SWT.NONE);
        gridLayout  = new GridLayout(1,true);
        gridData    = new GridData(GridData.FILL_HORIZONTAL);

        wndGroup.setLayout(gridLayout);
        wndGroup.setLayoutData(gridData);
        wndGroup.setText(TERMINAL_TEXT_CONNECTIONTYPE + ":"); //$NON-NLS-1$
        
        m_ctlConnTypeCombo  = new Combo(wndGroup,SWT.DROP_DOWN|SWT.READ_ONLY);
        gridData            = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint  = 200;
        m_ctlConnTypeCombo.setLayoutData(gridData);

		// Load controls
		m_ctlConnTypeCombo.add(TERMINAL_CONNTYPE_SERIAL);
		m_ctlConnTypeCombo.add(TERMINAL_CONNTYPE_NETWORK);
    }
    
    /**
     * 
     */
    protected void setupSettingsGroup(Composite parent)
    {
        m_wndSettingsGroup  = new Group(parent,SWT.NONE);
        GridLayout  gridLayout          = new GridLayout();
        GridData    gridData            = new GridData(GridData.FILL_BOTH);

        m_wndSettingsGroup.setText(TERMINAL_TEXT_SETTINGS + ":"); //$NON-NLS-1$
        m_wndSettingsGroup.setLayout(gridLayout);
        m_wndSettingsGroup.setLayoutData(gridData);
    }

    /**
     *
     */
    protected void setupSerialPanel()
    {
        TerminalPlugin      plugin;
        TerminalProperties  properties;
        Label               ctlLabel;
        GridLayout          gridLayout;
        GridData            gridData;
		Vector              table;

        if (m_wndSettingsPanel != null)
        {
            m_wndSettingsPanel.setVisible(false);
            m_wndSettingsPanel.dispose();
        }
        
        plugin              = TerminalPlugin.getDefault();
        properties          = plugin.getTerminalProperties();
        m_wndSettingsPanel  = new Composite(m_wndSettingsGroup,SWT.NONE);
        gridLayout          = new GridLayout(2,false);
        gridData            = new GridData(GridData.FILL_HORIZONTAL);

        m_wndSettingsPanel.setLayout(gridLayout);
        m_wndSettingsPanel.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_PORT + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlSerialPortCombo    = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN|SWT.READ_ONLY);
        m_ctlSerialPortCombo.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_BAUDRATE + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlBaudRateCombo      = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN|SWT.READ_ONLY);
        m_ctlBaudRateCombo.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_DATABITS + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlDataBitsCombo      = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN|SWT.READ_ONLY);
        m_ctlDataBitsCombo.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_STOPBITS + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlStopBitsCombo      = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN|SWT.READ_ONLY);
        m_ctlStopBitsCombo.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_PARITY + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlParityCombo        = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN|SWT.READ_ONLY);
        m_ctlParityCombo.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_FLOWCONTROL + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlFlowControlCombo   = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN|SWT.READ_ONLY);
        m_ctlFlowControlCombo.setLayoutData(gridData);
        
		// Load controls
        table = properties.getSerialPortTable();
        loadCombo(m_ctlSerialPortCombo,table);

        table = properties.getBaudRateTable();    
        loadCombo(m_ctlBaudRateCombo,table);
        
        table = properties.getDataBitsTable();    
        loadCombo(m_ctlDataBitsCombo,table);
        
        table = properties.getStopBitsTable();    
        loadCombo(m_ctlStopBitsCombo,table);
        
        table = properties.getParityTable();    
        loadCombo(m_ctlParityCombo,table);
        
        table = properties.getFlowControlTable();    
        loadCombo(m_ctlFlowControlCombo,table);
        
        setSerialPort(m_TerminalSettings.getSerialPort());
        setBaudRate(m_TerminalSettings.getBaudRate());
        setDataBits(m_TerminalSettings.getDataBits());
        setStopBits(m_TerminalSettings.getStopBits());
        setParity(m_TerminalSettings.getParity());
        setFlowIn(m_TerminalSettings.getFlowControl());

        m_wndSettingsGroup.layout(true);
    }

    /**
     *
     */
    protected void setupNetworkPanel()
    {
        TerminalPlugin          plugin;
        TerminalProperties      properties;
        TerminalNetworkPortMap  networkPortMap;
        Label                   ctlLabel;
        GridLayout              gridLayout;
        GridData                gridData;
        Vector                  table;

        if (m_wndSettingsPanel != null)
        {
            m_wndSettingsPanel.setVisible(false);
            m_wndSettingsPanel.dispose();
        }
        
        plugin              = TerminalPlugin.getDefault();
        properties          = plugin.getTerminalProperties();
        m_wndSettingsPanel  = new Composite(m_wndSettingsGroup,SWT.NONE);
        gridLayout          = new GridLayout(2,false);
        gridData            = new GridData(GridData.FILL_HORIZONTAL);

        m_wndSettingsPanel.setLayout(gridLayout);
        m_wndSettingsPanel.setLayoutData(gridData);

        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_HOST + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlHostText           = new Text(m_wndSettingsPanel,SWT.BORDER);
        m_ctlHostText.setLayoutData(gridData);
        
        // Add label
        ctlLabel = new Label(m_wndSettingsPanel,SWT.RIGHT);
        ctlLabel.setText(TERMINAL_TEXT_PORT + ":"); //$NON-NLS-1$
        
        // Add control
        gridData                = new GridData(GridData.FILL_HORIZONTAL);
        m_ctlNetworkPortCombo   = new Combo(m_wndSettingsPanel,SWT.DROP_DOWN);

        m_ctlNetworkPortCombo.setLayoutData(gridData);

        networkPortMap  = properties.getNetworkPortMap();
        table           = networkPortMap.getNameTable();
        Collections.sort(table);
        loadCombo(m_ctlNetworkPortCombo,table);

        setHost(m_TerminalSettings.getHost());
        setNetworkPort(m_TerminalSettings.getNetworkPort());
        
        m_wndSettingsGroup.layout(true);
    }

    /**
     * 
     */
    protected void setupConnTypeNotSupportedPanel()
    {
        MessageDialog   dlgError;
        Shell           parentShell; 
        Image           imgTitle;
        String          labels[];
        String          strTitle; 
        String          strMessage; 
        int             nImage;
        int             nIndex; 

        if (m_wndSettingsPanel != null)
        {
            m_wndSettingsPanel.setVisible(false);
            m_wndSettingsPanel.dispose();
        }
        
        m_wndSettingsPanel  = new Composite(m_wndSettingsGroup,SWT.NONE);
        parentShell         = getShell();
        strTitle            = TERMINAL_MSG_ERROR_5;
        imgTitle            = null;
        strMessage          = TERMINAL_MSG_ERROR_6;
        nImage              = SWT.ICON_ERROR;
        labels              = new String[]{"OK"}; //$NON-NLS-1$
        nIndex              = 0;
        dlgError            = new MessageDialog(parentShell,
                                                strTitle,
                                                imgTitle,
                                                strMessage,
                                                nImage,
                                                labels,
                                                nIndex);
                                                   
        m_wndSettingsGroup.layout(true);
        dlgError.open();
    }
    
    /**
     *
     */
    protected void setupListeners()
    {
        TerminalSettingsSelectionHandler selectionHandler;

        selectionHandler = new TerminalSettingsSelectionHandler();
        m_ctlConnTypeCombo.addSelectionListener(selectionHandler);
    }
    
    /**
     * 
     */
    protected void loadCombo(Combo ctlCombo,Vector table)
    {
        String  strData;
        
        for(int i=0;i<table.size();i++)
        {
            strData = (String)table.get(i);
            ctlCombo.add(strData);
        }
    }

    /**
     *
     */
    protected Vector getPorts()
    {
        Enumeration         portIdEnum;
        Vector              ports;
        CommPortIdentifier  identifier;
        String              strName;
        int                 nPortType;
        
        portIdEnum    = CommPortIdentifier.getPortIdentifiers();
        ports   = new Vector();

        while(portIdEnum.hasMoreElements())
        {
            identifier  = (CommPortIdentifier)portIdEnum.nextElement();
            strName     = identifier.getName();
            nPortType   = identifier.getPortType();
            
            if (nPortType == CommPortIdentifier.PORT_SERIAL)
                ports.addElement(strName);
        }

        Collections.sort(ports);

        return ports;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    //
    
    /**
     *
     */
    protected class TerminalSettingsSelectionHandler extends SelectionAdapter
    {
        /**
         * 
         */
        protected TerminalSettingsSelectionHandler()
        {
            super();
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // SelectionAdapter interface
        //
        
        /**
         *
         */
        public void widgetSelected(SelectionEvent event)
        {
            Object source;
                
            source = event.getSource();
            if (source == m_ctlConnTypeCombo)
            {
                execute(ON_CONNTYPE_SELECTED,null);
            }
            else
            {
            }
        }
    }
}

