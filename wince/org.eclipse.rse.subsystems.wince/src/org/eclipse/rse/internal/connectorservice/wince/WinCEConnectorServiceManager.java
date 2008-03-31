/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SshConnectorServiceManager
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.wince;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class WinCEConnectorServiceManager extends AbstractConnectorServiceManager {

  private static WinCEConnectorServiceManager inst = null;
  
  private WinCEConnectorServiceManager() {
    super();
  }
  
  public static WinCEConnectorServiceManager getInstance() {
    if (inst == null) {
      inst = new WinCEConnectorServiceManager();
    }
    return inst;
  }
  
  public IConnectorService createConnectorService(IHost host) {
    IConnectorService connectorService = new WinCEConnectorService(host);
    return connectorService;
  }

  public Class getSubSystemCommonInterface(ISubSystem subsystem) {
    return IWinCESubSystem.class;
  }

  public boolean sharesSystem(ISubSystem otherSubSystem) {
    return (otherSubSystem instanceof IWinCESubSystem);
  }

}
