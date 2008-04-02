/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.rapi;

/**
 * This class contains information that describes the connection between 
 * a WinCE device and a host computer.
 * 
 * @author Radoslav Gerganov
 */
public class RapiConnectionInfo {
  
  public static final int RAPI_CONNECTION_USB = 0;
  public static final int RAPI_CONNECTION_IR = 1;
  public static final int RAPI_CONNECTION_SERIAL = 2;
  public static final int RAPI_CONNECTION_NETWORK = 3;

  //FIXME
	//SOCKADDR_STORAGE ipaddr;
	//SOCKADDR_STORAGE hostIpaddr;
    
  public int connectionType;

  public String toString() {
    switch (connectionType) {
      case RAPI_CONNECTION_USB: return "USB"; //$NON-NLS-1$
      case RAPI_CONNECTION_IR: return "IR"; //$NON-NLS-1$
      case RAPI_CONNECTION_SERIAL: return "Serial"; //$NON-NLS-1$
      case RAPI_CONNECTION_NETWORK: return "Network"; //$NON-NLS-1$
    }
    return "Unknown"; //$NON-NLS-1$
  }
  
}
