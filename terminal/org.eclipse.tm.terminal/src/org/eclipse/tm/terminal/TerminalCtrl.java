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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.text.MessageFormat;

import javax.comm.CommPortIdentifier;
import javax.comm.CommPortOwnershipListener;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

/**
 * UNDER CONSTRUCTION
 *
 * This class was originally written to use nested classes, which unfortunately makes
 * this source file larger and more complex than it needs to be.  In particular, the
 * methods in the nested classes directly access the fields of the enclosing class.
 * One day we should pull the nested classes out into their own source files (but still
 * in this package).
 *
 * @author Chris Thew <chris.thew@windriver.com>
 */
class TerminalCtrl implements TerminalTarget, TerminalConsts
{
    /**
     * UNDER CONSTRUCTION
     */
    protected final static String[] LINE_DELIMITERS = { "\n" }; //$NON-NLS-1$

    /**
     * This field holds a reference to a TerminalText object that performs all ANSI
     * text processing on data received from the remote host and controls how text is
     * displayed using the view's StyledText widget.
     */
    protected TerminalText              m_TerminalText;

    protected Display                   m_Display;
    protected StyledText                m_ctlText;
    protected TextViewer                m_Viewer;
    protected Composite                 m_wndParent;
    protected CommPortIdentifier        m_SerialPortIdentifier;
    protected SerialPort                m_SerialPort;
    protected Socket                    m_Socket;
    protected InputStream               m_InputStream;
    protected OutputStream              m_OutputStream;
    protected Clipboard                 m_Clipboard;
    protected TerminalSerialPortHandler m_SerialPortHandler;
    protected VerifyListener            m_VerifyHandler;
    protected TerminalModifyListener    m_ModifyListener;
    protected KeyListener               m_KeyHandler;
    protected TerminalSettings          m_TerminalSettings;
    protected TerminalTarget            m_Target;
    protected ViewPart                  m_ViewPart;
    protected String                    m_strMsg = ""; //$NON-NLS-1$
    protected boolean                   m_bConnecting = false;
    protected boolean                   m_bConnected = false;
    protected boolean                   m_bOpened = false;
    protected boolean                   m_bPortInUse = false;
    protected boolean                   m_bCaretReset = false;
    protected TelnetConnection          m_telnetConnection;
    protected VerifyKeyListener         m_VerifyKeyListener;
    protected FocusListener             m_FocusListener;

    /**
     * UNDER CONSTRUCTION
     */
    public TerminalCtrl(TerminalTarget target, Composite wndParent)
        throws Exception
    {
        super();

        m_Target = target;
        m_ViewPart = (ViewPart)target;
        m_wndParent = wndParent;

        try
        {
            m_TerminalText = new TerminalText(this);
        }
        catch (Exception ex)
        {
            Logger.logException(ex);

            throw ex;
        }

        setupTerminal();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void execute(String strMsg, Object data)
    {
        if (strMsg.equals(ON_TERMINAL_CONNECT))
        {
            onTerminalConnect(data);
        }
        else if (strMsg.equals(ON_TERMINAL_CONNECTING))
        {
            onTerminalConnecting(data);
        }
        else if (strMsg.equals(ON_TERMINAL_DISCONNECT))
        {
            onTerminalDisconnect(data);
        }
        else if (strMsg.equals(ON_TERMINAL_STATUS))
        {
            onTerminalStatus(data);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void onTerminalConnect(Object data)
    {
        m_Target.execute(ON_TERMINAL_CONNECT, data);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void onTerminalConnecting(Object data)
    {
        m_Target.execute(ON_TERMINAL_CONNECTING, data);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void onTerminalDisconnect(Object data)
    {
        m_Target.execute(ON_TERMINAL_DISCONNECT, data);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void onTerminalStatus(Object data)
    {
        m_Target.execute(ON_TERMINAL_STATUS, data);
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void copy()
    {
        m_ctlText.copy();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void paste()
    {
        TextTransfer    textTransfer;
        String          strText;

        textTransfer    = TextTransfer.getInstance();
        strText         = (String)m_Clipboard.getContents(textTransfer);

        if (strText == null)
            return;

        for (int i=0;i<strText.length();i++)
        {
            sendChar(strText.charAt(i), false);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void selectAll()
    {
        m_ctlText.selectAll();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void sendKey(char character)
    {
        Event       event;
        KeyEvent    keyEvent;

        event           = new Event();
        event.widget    = m_ctlText;
        event.character = character;
        event.keyCode   = 0;
        event.stateMask = 0;
        event.doit      = true;
        keyEvent        = new KeyEvent(event);

        m_KeyHandler.keyPressed(keyEvent);
    }

    /**
     * This method erases all text from the Terminal view.
     */
    public void clearTerminal()
    {
        // The TerminalText object does all text manipulation.

        m_TerminalText.clearTerminal();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public Clipboard getClipboard()
    {
        return m_Clipboard;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public ITextSelection getSelection()
    {
        return (ITextSelection)m_Viewer.getSelection();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public StyledText getTextWidget()
    {
        return m_ctlText;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public OutputStream getOutputStream()
    {
        return m_OutputStream;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void setFocus()
    {
        m_ctlText.setFocus();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public boolean isEmpty()
    {
        int nCharCnt;

        nCharCnt = m_ctlText.getCharCount();
        return (nCharCnt == 0);
    }

    /**
     * UNDER CONSTRUCTION
     */
    public boolean isDisposed()
    {
        return m_ctlText.isDisposed();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public boolean isOpened()
    {
        return m_bOpened;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void setOpened(boolean newValue)
    {
        m_bOpened = newValue;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public boolean isConnecting()
    {
        return m_bConnecting;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public boolean isConnected()
    {
        return m_bConnected;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void setConnected(boolean newValue)
    {
        m_bConnected = newValue;
    }

    /**
     * UNDER CONSTRUCTION 
     */
    public TelnetConnection getTelnetConnection()
    {
        return m_telnetConnection;
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void disposeTerminal()
    {
        Logger.log("entered."); //$NON-NLS-1$
        disconnectTerminal();
        m_Clipboard.dispose();
        m_TerminalText.dispose();
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void connectTerminal(TerminalSettings terminalSettings)
    {
        Logger.log("entered."); //$NON-NLS-1$

        String  strConnType;

        m_TerminalSettings = terminalSettings;
        strConnType        = m_TerminalSettings.getConnType();

        if (strConnType.equals(TERMINAL_CONNTYPE_SERIAL))
        {
            connectSerial();
        }
        else if (strConnType.equals(TERMINAL_CONNTYPE_NETWORK))
        {
            connectNetwork();
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void disconnectTerminal()
    {
        Logger.log("entered."); //$NON-NLS-1$

        String strConnType;

        if (!m_bConnecting && !m_bConnected)
        {
            execute(ON_TERMINAL_STATUS, null);
            return;
        }

        strConnType = m_TerminalSettings.getConnType();

        if (strConnType.equals(TERMINAL_CONNTYPE_SERIAL))
        {
            disconnectSerial();
        }
        else if (strConnType.equals(TERMINAL_CONNTYPE_NETWORK))
        {
            disconnectNetwork();
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void connectSerial()
    {
        Logger.log("entered."); //$NON-NLS-1$

        TerminalSerialConnectWorker     worker;
        Shell                           shell;
        String                          strTitle;

        worker = new TerminalSerialConnectWorker();
        worker.start();
        waitForConnect();

        if (!m_strMsg.equals("")) //$NON-NLS-1$
        {
            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_MSG_ERROR_1;
            MessageDialog.openError(shell, strTitle, m_strMsg);

            disconnectTerminal();
            return;
        }

        execute(ON_TERMINAL_STATUS, null);
        m_ctlText.setFocus();
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void connectNetwork()
    {
        Logger.log("entered."); //$NON-NLS-1$

        TerminalNetworkConnectWorker    worker;
        Shell                           shell;
        String                          strTitle;

        worker = new TerminalNetworkConnectWorker(this);
        worker.start();
        waitForConnect();

        if (!m_strMsg.equals("")) //$NON-NLS-1$
        {
            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_MSG_ERROR_1;
            MessageDialog.openError(shell, strTitle, m_strMsg);

            disconnectTerminal();
            return;
        }

        execute(ON_TERMINAL_STATUS, null);
        m_ctlText.setFocus();
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void disconnectSerial()
    {
        Logger.log("entered."); //$NON-NLS-1$

        // Fix for SPR 112422.  When output is being received from the serial port, the
        // below call to removePortOwnershipListener() attempts to lock the serial port
        // object, but that object is already locked by another Terminal view thread
        // waiting for the SWT display thread to process a syncExec() call.  Since this
        // method is called on the display thread, the display thread is waiting to
        // lock the serial port object and the thread holding the serial port object
        // lock is waiting for the display thread to process a syncExec() call, so the
        // two threads end up deadlocked, which hangs the Workbench GUI.
        //
        // The solution is to spawn a short-lived worker thread that calls
        // removePortOwnershipListener(), thus preventing the display thread from
        // deadlocking with the other Terminal view thread.

        new Thread("Terminal View Serial Port Disconnect Worker") //$NON-NLS-1$
        {
            public void run()
            {

                if (m_SerialPortIdentifier != null)
                {
                    m_SerialPortIdentifier.removePortOwnershipListener(m_SerialPortHandler);
                }

                if (m_SerialPort != null)
                {
                    m_SerialPort.removeEventListener();
                    Logger.log("Calling close() on serial port ..."); //$NON-NLS-1$
                    m_SerialPort.close();
                }

                if (m_InputStream != null)
                {
                    try
                    {
                        m_InputStream.close();
                    }
                    catch(Exception exception)
                    {
                        Logger.logException(exception);
                    }
                }

                if (m_OutputStream != null)
                {
                    try
                    {
                        m_OutputStream.close();
                    }
                    catch(Exception exception)
                    {
                        Logger.logException(exception);
                    }
                }

                m_SerialPortIdentifier  = null;
                m_SerialPort            = null;
                m_InputStream           = null;
                m_OutputStream          = null;
                m_SerialPortHandler     = null;
            }
        }.start();

        m_bConnecting           = false;
        setConnected(false);
        setOpened(false);

        execute(ON_TERMINAL_STATUS, null);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void disconnectNetwork()
    {
        Logger.log("entered."); //$NON-NLS-1$

        if (m_Socket != null)
        {
            try
            {
                m_Socket.close();
            }
            catch(Exception exception)
            {
                Logger.logException(exception);
            }
        }

        if (m_InputStream != null)
        {
            try
            {
                m_InputStream.close();
            }
            catch(Exception exception)
            {
                Logger.logException(exception);
            }
        }

        if (m_OutputStream != null)
        {
            try
            {
                m_OutputStream.close();
            }
            catch(Exception exception)
            {
                Logger.logException(exception);
            }
        }

        m_Socket        = null;
        m_InputStream   = null;
        m_OutputStream  = null;
        m_bConnecting   = false;
        setConnected(false);
        setOpened(false);

        execute(ON_TERMINAL_STATUS, null);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void waitForConnect()
    {
        Logger.log("entered."); //$NON-NLS-1$

        while (m_bConnecting)
        {
            if (m_Display.readAndDispatch())
                continue;

            m_Display.sleep();
        }
    }

    /**
     * UNDER CONSTRUCTION 
     */
    protected void sendString(String string)
    {
        Shell   shell;
        String  strTitle;
        String  strMsg;

        try
        {
            // Send the string after converting it to an array of bytes using the
            // platform's default character encoding.
            //
            // TODO: Find a way to force this to use the ISO Latin-1 encoding.

            m_OutputStream.write(string.getBytes());
            m_OutputStream.flush();
        }
        catch (SocketException socketException)
        {
            displayTextInTerminal(socketException.getMessage());

            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_MSG_ERROR_1;
            strMsg      = TERMINAL_MSG_ERROR_2  + "!\n" + socketException.getMessage(); //$NON-NLS-1$

            MessageDialog.openError(shell, strTitle, strMsg);
            Logger.logException(socketException);

            disconnectTerminal();
            execute(ON_TERMINAL_STATUS, null);
        }
        catch (IOException ioException)
        {
            displayTextInTerminal(ioException.getMessage());

            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_MSG_ERROR_1;
            strMsg      = TERMINAL_MSG_ERROR_3 + "!\n" + ioException.getMessage(); //$NON-NLS-1$

            MessageDialog.openError(shell, strTitle, strMsg);
            Logger.logException(ioException);

            disconnectTerminal();
            execute(ON_TERMINAL_STATUS, null);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void sendChar(char chKey, boolean altKeyPressed)
    {
        Shell   shell;
        String  strTitle;
        String  strMsg;

        try
        {
            int byteToSend = chKey;

            if (altKeyPressed)
            {
                // When the ALT key is pressed at the same time that a character is
                // typed, translate it into an ESCAPE followed by the character.  The
                // alternative in this case is to set the high bit of the character
                // being transmitted, but that will cause input such as ALT-f to be
                // seen as the ISO Latin-1 character '�', which can be confusing to
                // European users running Emacs, for whom Alt-f should move forward a
                // word instead of inserting the '�' character.
                //
                // TODO: Make the ESCAPE-vs-highbit behavior user configurable.

                Logger.log("sending ESC + '" + byteToSend + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                m_OutputStream.write('\u001b');
                m_OutputStream.write(byteToSend);
            }
            else
            {
                Logger.log("sending '" + byteToSend + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                m_OutputStream.write(byteToSend);
            }

            m_OutputStream.flush();
        }
        catch (SocketException socketException)
        {
            Logger.logException(socketException);

            displayTextInTerminal(socketException.getMessage());

            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_MSG_ERROR_1;
            strMsg      = TERMINAL_MSG_ERROR_2  + "!\n" + socketException.getMessage(); //$NON-NLS-1$

            MessageDialog.openError(shell, strTitle, strMsg);
            Logger.logException(socketException);

            disconnectTerminal();
            execute(ON_TERMINAL_STATUS, null);
        }
        catch (IOException ioException)
        {
            Logger.logException(ioException);

            displayTextInTerminal(ioException.getMessage());

            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_MSG_ERROR_1;
            strMsg      = TERMINAL_MSG_ERROR_3 + "!\n" + ioException.getMessage(); //$NON-NLS-1$

            MessageDialog.openError(shell, strTitle, strMsg);
            Logger.logException(ioException);

            disconnectTerminal();
            execute(ON_TERMINAL_STATUS, null);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    public void setupTerminal()
    {
        setupControls();
        setupListeners();
        setupHelp(m_wndParent, HELP_VIEW);
    }

    /**
     * This method is called when the user changes the Terminal view font.
     */
    public void onFontChanged()
    {
        m_TerminalText.fontChanged();
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void setupControls()
    {
        TerminalDocument    doc;
        Font                font;

        doc = new TerminalDocument();

        // The Terminal view now aims to be an ANSI-conforming terminal emulator, so it
        // can't have a horizontal scroll bar (but a vertical one is ok).  Also, do
        // _not_ make the TextViewer read-only, because that prevents it from seeing a
        // TAB character when the user presses TAB (instead, the TAB causes focus to
        // switch to another Workbench control).  We prevent local keyboard input from
        // modifying the text in method TerminalVerifyKeyListener.verifyKey().

        m_Viewer    = new TextViewer(m_wndParent, SWT.V_SCROLL);
        m_ctlText   = m_Viewer.getTextWidget();

        m_Display   = m_ctlText.getDisplay();
        m_Clipboard = new Clipboard(m_Display);
        font        = JFaceResources.getTextFont();

        m_Viewer.setDocument(doc);
        m_ctlText.setFont(font);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void setupListeners()
    {
        m_KeyHandler        = new TerminalKeyHandler();
        m_ModifyListener    = new TerminalModifyListener();
        m_SerialPortHandler = null;
        m_VerifyKeyListener = new TerminalVerifyKeyListener();
        m_FocusListener     = new TerminalFocusListener();

        m_ctlText.addVerifyKeyListener(m_VerifyKeyListener);
        m_ctlText.addKeyListener(m_KeyHandler);
        m_ctlText.addModifyListener(m_ModifyListener);
        m_ctlText.addVerifyKeyListener(m_VerifyKeyListener);
        m_ctlText.addFocusListener(m_FocusListener);
    }

    /**
     * Setup all the help contexts for the controls.
     */
    protected void setupHelp(Composite parent, String id)
    {
        Control[] children = parent.getChildren();

        for (int nIndex = 0; nIndex < children.length; nIndex++)
        {
            if (children[nIndex] instanceof Composite)
            {
                setupHelp((Composite)children[nIndex], id);
            }
        }

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, id);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected void displayTextInTerminal(String text)
    {
        StringBuffer textToDisplay = new StringBuffer(text);
        textToDisplay.append("\r\n"); //$NON-NLS-1$

        m_TerminalText.setNewText(textToDisplay);
        m_Display.syncExec(m_TerminalText);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalSerialConnectWorker extends Thread
    {
        /**
         * UNDER CONSTRUCTION
         */
        protected TerminalSerialConnectWorker()
        {
            super();

            setupWorker();
        }

        // Thread interface

        public void run()
        {
            TerminalPlugin  plugin;
            Preferences     preferences;
            Package         pkg;
            String          strID;
            int             nFlow;
            int             nTimeout;

            try
            {
                plugin                  = TerminalPlugin.getDefault();
                preferences             = plugin.getPluginPreferences();
                nTimeout                = preferences.getInt(TERMINAL_PREF_TIMEOUT_SERIAL) * 1000;
                pkg                     = TerminalCtrl.class.getPackage();
                strID                   = pkg.getName();

                setOpened(true);
                setConnected(true);

                m_SerialPortHandler     = new TerminalSerialPortHandler();
                m_SerialPortIdentifier  = CommPortIdentifier.getPortIdentifier(m_TerminalSettings.getSerialPort());
                m_SerialPort            = (SerialPort) m_SerialPortIdentifier.open(strID, nTimeout);
                m_InputStream           = m_SerialPort.getInputStream();
                m_OutputStream          = m_SerialPort.getOutputStream();
                nFlow                   = m_TerminalSettings.getFlowControlValue();

                m_SerialPort.setSerialPortParams(m_TerminalSettings.getBaudRateValue(),
                                                 m_TerminalSettings.getDataBitsValue(),
                                                 m_TerminalSettings.getStopBitsValue(),
                                                 m_TerminalSettings.getParityValue());
                m_SerialPort.setFlowControlMode(nFlow);
                m_SerialPort.addEventListener(m_SerialPortHandler);
                m_SerialPort.notifyOnDataAvailable(true);
                m_SerialPortIdentifier.addPortOwnershipListener(m_SerialPortHandler);

                m_bConnecting = false;
            }
            catch(PortInUseException portInUseException)
            {
                m_strMsg        = "Connection Error!\n" + portInUseException.getMessage(); //$NON-NLS-1$
                m_bConnecting   = false;
                m_bPortInUse    = true;
                setConnected(false);
                setOpened(false);
            }
            catch(Exception exception)
            {
                m_strMsg        = ""; //$NON-NLS-1$
                m_bConnecting   = false;
                m_bPortInUse    = false;
                setConnected(false);
                setOpened(false);
            }
        }

        // Operations

        protected void setupWorker()
        {
            m_bConnecting = true;

            execute(ON_TERMINAL_CONNECTING, null);
            execute(ON_TERMINAL_STATUS, null);

            m_strMsg = ""; //$NON-NLS-1$
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalNetworkConnectWorker extends Thread
    {
        protected TerminalCtrl terminalController;

        /**
         * UNDER CONSTRUCTION
         */
        protected TerminalNetworkConnectWorker(TerminalCtrl terminalController)
        {
            super();

            this.terminalController = terminalController;

            setupWorker();
        }

        // Thread interface

        public void run()
        {
            TerminalPlugin      plugin;
            Preferences         preferences;
            InetSocketAddress   address;
            String              strHost;
            int                 nPort;
            int                 nTimeout;

            try
            {
                plugin          = TerminalPlugin.getDefault();
                preferences     = plugin.getPluginPreferences();
                nTimeout        = preferences.getInt(TERMINAL_PREF_TIMEOUT_NETWORK) * 1000;
                strHost         = m_TerminalSettings.getHost();
                nPort           = m_TerminalSettings.getNetworkPortValue();
                address         = new InetSocketAddress(strHost, nPort);
                m_Socket        = new Socket();

                m_Socket.connect(address, nTimeout);

                // This next call causes reads on the socket to see TCP urgent data
                // inline with the rest of the non-urgent data.  Without this call, TCP
                // urgent data is silently dropped by Java.  This is required for
                // TELNET support, because when the TELNET server sends "IAC DM", the
                // IAC byte is TCP urgent data.  If urgent data is silently dropped, we
                // only see the DM, which looks like an ISO Latin-1 '�' character.

                m_Socket.setOOBInline(true);

                m_InputStream      = m_Socket.getInputStream();
                m_OutputStream     = m_Socket.getOutputStream();
                m_telnetConnection = new TelnetConnection(terminalController,
                                                          m_Socket,
                                                          m_TerminalText);
                m_bConnecting   = false;
                setConnected(true);
                setOpened(true);

                m_Socket.setKeepAlive(true);

                m_telnetConnection.start();
            }
            catch (UnknownHostException ex)
            {
                displayTextInTerminal("Unknown host: " + ex.getMessage()); //$NON-NLS-1$

                Logger.log("Unknown host: " + ex.getMessage()); //$NON-NLS-1$
                m_strMsg      = "Unknown host: " + ex.getMessage() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                m_bConnecting = false;
                m_Socket      = null;
                setConnected(false);
                setOpened(false);
            }
            catch (SocketTimeoutException socketTimeoutException)
            {
                displayTextInTerminal(socketTimeoutException.getMessage());

                Logger.log("Socket timeout!"); //$NON-NLS-1$
                m_strMsg      = "Connection Error!\n" + socketTimeoutException.getMessage(); //$NON-NLS-1$
                m_bConnecting = false;
                m_Socket      = null;
                setConnected(false);
                setOpened(false);
            }
            catch (ConnectException connectException)
            {
                displayTextInTerminal(connectException.getMessage());

                Logger.log("Connection refused."); //$NON-NLS-1$
                m_strMsg = "Connection refused!"; //$NON-NLS-1$

                m_bConnecting = false;
                m_Socket      = null;
                setConnected(false);
                setOpened(false);
            }
            catch (Exception exception)
            {
                displayTextInTerminal(exception.getMessage());

                Logger.logException(exception);
                m_strMsg      = ""; //$NON-NLS-1$

                m_bConnecting = false;
                m_Socket      = null;
                setConnected(false);
                setOpened(false);
            }
        }

        // Operations

        protected void setupWorker()
        {
            m_bConnecting = true;
            execute(ON_TERMINAL_CONNECTING, null);
            execute(ON_TERMINAL_STATUS, null);

            m_strMsg = ""; //$NON-NLS-1$
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected boolean isLogCharEnabled()
    {
        return TerminalPlugin.isOptionEnabled(TERMINAL_TRACE_DEBUG_LOG_CHAR);
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected boolean isLogBufferSizeEnabled()
    {
        return TerminalPlugin.isOptionEnabled(TERMINAL_TRACE_DEBUG_LOG_BUFFER_SIZE);
    }

    protected class TerminalModifyListener implements ModifyListener
    {
        public void modifyText(ModifyEvent e)
        {
            if (e.getSource() instanceof StyledText)
            {
                StyledText text = (StyledText)e.getSource();
                text.setSelection(text.getText().length());
            }
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalSerialOwnershipRequestedWorker extends Thread
    {
        /**
         * UNDER CONSTRUCTION
         */
        protected TerminalSerialOwnershipRequestedWorker()
        {
            super();
        }

        // Thread interface

        /**
         * UNDER CONSTRUCTION
         */
        public void run()
        {
            Shell shell;
            String[] args;
            String strPort;
            String strTitle;
            String strMsg;
            boolean bConfirm;

            strPort     = m_TerminalSettings.getSerialPort();
            shell       = m_ctlText.getShell();
            strTitle    = TERMINAL_PROP_TITLE;
            args        = new String[] { strPort };
            strMsg      = MessageFormat.format(TERMINAL_MSG_ERROR_4, args);
            bConfirm    = MessageDialog.openQuestion(shell, strTitle, strMsg);

            if (!bConfirm)
                return;

            execute(ON_TERMINAL_DISCONNECT, null);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalFocusListener implements FocusListener
    {
        private IContextActivation contextActivation = null;

        protected TerminalFocusListener()
        {
            super();
        }

        public void focusGained(FocusEvent event)
        {
            // Disable all keyboard accelerators (e.g., Ctrl-B) so the Terminal view
            // can see every keystroke.  Without this, Emacs, vi, and Bash are unusable
            // in the Terminal view.

            IBindingService bindingService =
                (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
            bindingService.setKeyFilterEnabled(false);

            // The above code fails to cause Eclipse to disable menu-activation
            // accelerators (e.g., Alt-F for the File menu), so we set the command
            // context to be the Terminal view's command context.  This enables us to
            // override menu-activation accelerators with no-op commands in our
            // plugin.xml file, which enables the Terminal view to see absolutly _all_
            // key-presses.

            IContextService contextService =
                (IContextService)PlatformUI.getWorkbench().getAdapter(IContextService.class);
            contextActivation =
                contextService.activateContext("org.eclipse.tm.terminal.TerminalContext"); //$NON-NLS-1$
        }

        public void focusLost(FocusEvent event)
        {
            // Enable all keybindings.

            IBindingService bindingService =
                (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
            bindingService.setKeyFilterEnabled(true);
            
            // Restore the command context to its previous value.
            
            IContextService contextService =
                (IContextService)PlatformUI.getWorkbench().getAdapter(IContextService.class);
            contextService.deactivateContext(contextActivation);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalVerifyKeyListener implements VerifyKeyListener
    {
        protected TerminalVerifyKeyListener()
        {
            super();
        }

        public void verifyKey(VerifyEvent event)
        {
            // We set event.doit to false to prevent keyboard input from locally
            // modifying the contents of the StyledText widget.  The only text we
            // display is text received from the remote endpoint.  This also prevents
            // the caret from moving locally when the user presses an arrow key or the
            // PageUp or PageDown keys.  For some reason, doing this in
            // TerminalKeyHandler.keyPressed() does not work, hence the need for this
            // class.

            event.doit = false;
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalKeyHandler extends KeyAdapter
    {
        /**
         * UNDER CONSTRUCTION
         */
        protected TerminalKeyHandler()
        {
            super();
        }

        // KeyAdapter interface

        /**
         * UNDER CONSTRUCTION
         */
        public void keyPressed(KeyEvent event)
        {
            if (m_bConnecting)
                return;

            // We set the event.doit to false to prevent any further processing of this
            // key event.  The only reason this is here is because I was seeing the F10
            // key both send an escape sequence (due to this method) and switch focus
            // to the Workbench File menu (forcing the user to click in the Terminal
            // view again to continue entering text).  This fixes that.

            event.doit = false;

            char character = event.character;

            if (!m_bConnected)
            {
                // Pressing ENTER while not connected causes us to connect.
                if (character == '\r')
                {
                    execute(ON_TERMINAL_CONNECT, null);
                    return;
                }

                // Ignore all other keyboard input when not connected.
                return;
            }

            // If the event character is NUL ('\u0000'), then a special key was pressed
            // (e.g., PageUp, PageDown, an arrow key, a function key, Shift, Alt,
            // Control, etc.).  The one exception is when the user presses Control-@,
            // which sends a NUL character, in which case we must send the NUL to the
            // remote endpoint.  This is necessary so that Emacs will work correctly,
            // because Control-@ (i.e., NUL) invokes Emacs' set-mark-command when Emacs
            // is running on a terminal.  When the user presses Control-@, the keyCode
            // is 50.

            if (character == '\u0000' && event.keyCode != 50)
            {
                // A special key was pressed.  Figure out which one it was and send the
                // appropriate ANSI escape sequence.
                //
                // IMPORTANT: Control will not enter this method for these special keys
                // unless certain <keybinding> tags are present in the plugin.xml file
                // for the Terminal view.  Do not delete those tags.

                switch (event.keyCode)
                {
                case 0x1000001:         // Up arrow.
                    sendString("\u001b[A"); //$NON-NLS-1$
                    break;

                case 0x1000002:         // Down arrow.
                    sendString("\u001b[B"); //$NON-NLS-1$
                    break;

                case 0x1000003:         // Left arrow.
                    sendString("\u001b[D"); //$NON-NLS-1$
                    break;

                case 0x1000004:         // Right arrow.
                    sendString("\u001b[C"); //$NON-NLS-1$
                    break;

                case 0x1000005:         // PgUp key.
                    sendString("\u001b[I"); //$NON-NLS-1$
                    break;

                case 0x1000006:         // PgDn key.
                    sendString("\u001b[G"); //$NON-NLS-1$
                    break;

                case 0x1000007:         // Home key.
                    sendString("\u001b[H"); //$NON-NLS-1$
                    break;

                case 0x1000008:         // End key.
                    sendString("\u001b[F"); //$NON-NLS-1$
                    break;

                case 0x100000a:         // F1 key.
                    sendString("\u001b[M"); //$NON-NLS-1$
                    break;

                case 0x100000b:         // F2 key.
                    sendString("\u001b[N"); //$NON-NLS-1$
                    break;

                case 0x100000c:         // F3 key.
                    sendString("\u001b[O"); //$NON-NLS-1$
                    break;

                case 0x100000d:         // F4 key.
                    sendString("\u001b[P"); //$NON-NLS-1$
                    break;

                case 0x100000e:         // F5 key.
                    sendString("\u001b[Q"); //$NON-NLS-1$
                    break;

                case 0x100000f:         // F6 key.
                    sendString("\u001b[R"); //$NON-NLS-1$
                    break;

                case 0x1000010:         // F7 key.
                    sendString("\u001b[S"); //$NON-NLS-1$
                    break;

                case 0x1000011:         // F8 key.
                    sendString("\u001b[T"); //$NON-NLS-1$
                    break;

                case 0x1000012:         // F9 key.
                    sendString("\u001b[U"); //$NON-NLS-1$
                    break;

                case 0x1000013:         // F10 key.
                    sendString("\u001b[V"); //$NON-NLS-1$
                    break;

                case 0x1000014:         // F11 key.
                    sendString("\u001b[W"); //$NON-NLS-1$
                    break;

                case 0x1000015:         // F12 key.
                    sendString("\u001b[X"); //$NON-NLS-1$
                    break;

                default:
                    // Ignore other special keys.  Control flows through this case when
                    // the user presses SHIFT, CONTROL, ALT, and any other key not
                    // handled by the above cases.
                    break;
                }

                // It's ok to return here, because we never locally echo special keys.

                return;
            }

            // To fix SPR 110341, we consider the Alt key to be pressed only when the
            // Ctrl key is _not_ also pressed.  This works around a bug in SWT where,
            // on European keyboards, the AltGr key being pressed appears to us as Ctrl
            // + Alt being pressed simultaneously.

            Logger.log("stateMask = " + event.stateMask); //$NON-NLS-1$

            boolean altKeyPressed = (((event.stateMask & SWT.ALT) != 0) &&
                                     ((event.stateMask & SWT.CTRL) == 0));

            if (!altKeyPressed && (event.stateMask & SWT.CTRL) != 0 && character == ' ')
            {
                // Send a NUL character -- many terminal emulators send NUL when
                // Ctrl-Space is pressed.  This is used to set the mark in Emacs.

                character='\u0000';
            }
            
            sendChar(character, altKeyPressed);

            // Special case: When we are in a TCP connection and echoing characters
            // locally, send a LF after sending a CR.
            // ISSUE: Is this absolutely required?

            if (character == '\r' &&
                m_telnetConnection != null &&
                m_telnetConnection.isConnected() &&
                m_telnetConnection.localEcho())
            {
                sendChar('\n', false);
            }

            // Now decide if we should locally echo the character we just sent.  We do
            // _not_ locally echo the character if any of these conditions are true:
            //
            // o This is a serial connection.
            //
            // o This is a TCP connection (i.e., m_telnetConnection is not null) and
            //   the remote endpoint is not a TELNET server.
            //
            // o The ALT (or META) key is pressed.
            //
            // o The character is any of the first 32 ISO Latin-1 characters except
            //   Control-I or Control-M.
            //
            // o The character is the DELETE character.

            if (m_telnetConnection == null ||
                m_telnetConnection.isConnected() == false ||
                m_telnetConnection.localEcho() == false ||
                altKeyPressed ||
                (character >= '\u0001' && character < '\t') ||
                (character > '\t' && character < '\r') ||
                (character > '\r' && character <= '\u001f') ||
                character == '\u007f')
            {
                // No local echoing.
                return;
            }

            // Locally echo the character.

            StringBuffer charBuffer = new StringBuffer();
            charBuffer.append(character);

            // If the character is a carriage return, we locally echo it as a CR + LF
            // combination.

            if (character == '\r')
                charBuffer.append('\n');

            m_TerminalText.setNewText(charBuffer);
            m_Display.syncExec(m_TerminalText);
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalSerialPortHandler implements SerialPortEventListener,
                                                         CommPortOwnershipListener
    {
        protected byte[] bytes = new byte[2048];

        /**
         * UNDER CONSTRUCTION
         */
        protected TerminalSerialPortHandler()
        {
            super();
        }

        // Message handlers

        /**
         * UNDER CONSTRUCTION
         */
        protected void onSerialDataAvailable(Object data)
        {
            Display             display;
            StringBuffer        buffer;
            String              strBuffer;
            int                 nBytes;

            display = m_ctlText.getDisplay();

            try
            {
                while (m_InputStream != null && m_InputStream.available() > 0)
                {
                    nBytes          = m_InputStream.read(bytes);
                    strBuffer       = new String(bytes, 0, nBytes);
                    buffer          = new StringBuffer(strBuffer);

                    m_TerminalText.setNewText(buffer);

                    // Do _not_ use asyncExec() here.  Class TerminalText requires that
                    // its run() and setNewText() methods be called in strictly
                    // alternating order.  If we were to call asyncExec() here, this
                    // loop might race around and call setNewText() twice in a row,
                    // which would lose data.

                    display.syncExec(m_TerminalText);
                }
            }
            catch (IOException ex)
            {
                displayTextInTerminal(ex.getMessage());
            }
            catch (Exception exception)
            {
                Logger.logException(exception);
            }
        }

        /**
         * UNDER CONSTRUCTION
         */
        protected void onSerialOwnershipRequested(Object data)
        {
            TerminalSerialOwnershipRequestedWorker  ownershipRequestedWorker;
            Display                                 display;

            if (m_bPortInUse)
            {
                m_bPortInUse = false;
                return;
            }

            display                     = m_ctlText.getDisplay();
            ownershipRequestedWorker    = new TerminalSerialOwnershipRequestedWorker();
            display.asyncExec(ownershipRequestedWorker);
        }

        // SerialPortEventListener interface

        /**
         * UNDER CONSTRUCTION
         */
        public void serialEvent(SerialPortEvent event)
        {
            switch (event.getEventType())
            {
            case SerialPortEvent.DATA_AVAILABLE:
                onSerialDataAvailable(null);
                break;
            }
        }

        // CommPortOwnershipListener interface

        /**
         * UNDER CONSTRUCTION
         */
        public void ownershipChange(int nType)
        {
            switch (nType)
            {
            case CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED:
                onSerialOwnershipRequested(null);
                break;
            }
        }
    }

    /**
     * UNDER CONSTRUCTION
     */
    protected class TerminalDocument extends Document
    {
        /**
         * UNDER CONSTRUCTION
         */
        protected TerminalDocument()
        {
            super();

            setupDocument();
        }

        // Operations

        /**
         * UNDER CONSTRUCTION
         */
        protected void setupDocument()
        {
            ConfigurableLineTracker lineTracker;

            lineTracker = new ConfigurableLineTracker(LINE_DELIMITERS);
            setLineTracker(lineTracker);
        }
    }
}
