/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalSettings 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import gnu.io.SerialPort;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class SerialSettings implements ISerialSettings {
    protected String fSerialPort;
    protected String fBaudRate;
    protected String fDataBits;
    protected String fStopBits;
    protected String fParity;
    protected String fFlowControl;
    protected String fTimeout;
    final private SerialProperties fProperties=new SerialProperties();

	public String getSerialPort() {
		return fSerialPort;
	}

	public void setSerialPort(String strSerialPort) {
		fSerialPort = strSerialPort;
	}

	public String getBaudRateString() {
		return fBaudRate;
	}

	public int getBaudRate() {
		int nBaudRate;

		try {
			nBaudRate = Integer.parseInt(fBaudRate);
		} catch (NumberFormatException numberFormatException) {
			nBaudRate = 9600;
		}

		return nBaudRate;
	}

	public void setBaudRate(String strBaudRate) {
		fBaudRate = strBaudRate;
	}

	public String getDataBitsString() {
		return fDataBits;
	}

	public int getDataBits() {
		if (fDataBits.equals("5")) { //$NON-NLS-1$
			return SerialPort.DATABITS_5;
		} else if (fDataBits.equals("6")) { //$NON-NLS-1$
			return SerialPort.DATABITS_6;
		} else if (fDataBits.equals("7")) { //$NON-NLS-1$
			return SerialPort.DATABITS_7;
		} else {
			return SerialPort.DATABITS_8;
		}
	}

	public void setDataBits(String strDataBits) {
		fDataBits = strDataBits;
	}

	public String getStopBitsString() {
		return fStopBits;
	}

	public int getStopBits() {
		if (fStopBits.equals("1_5")) { //$NON-NLS-1$
			return SerialPort.STOPBITS_1_5;
		} else if (fStopBits.equals("2")) { //$NON-NLS-1$
			return SerialPort.STOPBITS_2;
		} else { // 1
			return SerialPort.STOPBITS_1;
		}
	}

	public void setStopBits(String strStopBits) {
		fStopBits = strStopBits;
	}

	public String getParityString() {
		return fParity;
	}

	public int getParity() {
		if (fParity.equals("Even")) //$NON-NLS-1$
		{
			return SerialPort.PARITY_EVEN;
		} else if (fParity.equals("Odd")) //$NON-NLS-1$
		{
			return SerialPort.PARITY_ODD;
		} else if (fParity.equals("Mark")) //$NON-NLS-1$
		{
			return SerialPort.PARITY_MARK;
		} else if (fParity.equals("Space")) //$NON-NLS-1$
		{
			return SerialPort.PARITY_SPACE;
		} else // None
		{
			return SerialPort.PARITY_NONE;
		}
	}

	public void setParity(String strParity) {
		fParity = strParity;
	}

	public String getFlowControlString() {
		return fFlowControl;
	}

	public int getFlowControl() {
		if (fFlowControl.equals("RTS/CTS")) //$NON-NLS-1$
		{
			return SerialPort.FLOWCONTROL_RTSCTS_IN;
		} else if (fFlowControl.equals("Xon/Xoff")) //$NON-NLS-1$
		{
			return SerialPort.FLOWCONTROL_XONXOFF_IN;
		} else // None
		{
			return SerialPort.FLOWCONTROL_NONE;
		}
	}

	public void setFlowControl(String strFlow) {
		fFlowControl = strFlow;
	}

	public String getSummary() {
		return getSerialPort() + ", " + //$NON-NLS-1$
			getBaudRateString() + ", " + //$NON-NLS-1$
			getDataBitsString() + ", " + //$NON-NLS-1$
			getStopBitsString() + ", " + //$NON-NLS-1$
			getParityString() + ", " + //$NON-NLS-1$
			getFlowControlString();
	}

	public void load(ISettingsStore store) {
		fSerialPort = store.get("SerialPort", fProperties.getDefaultSerialPort());//$NON-NLS-1$
		fBaudRate = store.get("BaudRate", fProperties.getDefaultBaudRate());//$NON-NLS-1$
		fDataBits = store.get("DataBits", fProperties.getDefaultDataBits());//$NON-NLS-1$
		fStopBits = store.get("StopBits", fProperties.getDefaultStopBits());//$NON-NLS-1$
		fParity = store.get("Parity", fProperties.getDefaultParity());//$NON-NLS-1$
		fFlowControl = store.get("FlowControl", fProperties.getDefaultFlowControl());//$NON-NLS-1$
		fTimeout = store.get("Timeout",fProperties.getDefaultTimeout()); //$NON-NLS-1$
	}

	public void save(ISettingsStore store) {
		store.put("SerialPort", fSerialPort); //$NON-NLS-1$
		store.put("BaudRate", fBaudRate); //$NON-NLS-1$
		store.put("DataBits", fDataBits); //$NON-NLS-1$
		store.put("StopBits", fStopBits); //$NON-NLS-1$
		store.put("Parity", fParity); //$NON-NLS-1$
		store.put("FlowControl", fFlowControl); //$NON-NLS-1$
	}

	public SerialProperties getProperties() {
		return fProperties;
	}

	public int getTimeout() {
		try {
			return Integer.parseInt(fTimeout);
		} catch (NumberFormatException numberFormatException) {
			return 10;
		}
	}
	public String getTimeoutString() {
		return fTimeout;
	}

	public void setTimeout(String timeout) {
		fTimeout = timeout;
	}
}
