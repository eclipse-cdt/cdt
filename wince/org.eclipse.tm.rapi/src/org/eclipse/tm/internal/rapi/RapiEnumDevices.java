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
package org.eclipse.tm.internal.rapi;

import org.eclipse.tm.rapi.IRapiDevice;
import org.eclipse.tm.rapi.IRapiEnumDevices;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.RapiException;

/**
 * Implementation of <code>IRapiEnumDevices</code>.
 * 
 * @author Radoslav Gerganov
 */
public class RapiEnumDevices extends IRapiEnumDevices {

  public RapiEnumDevices(int addr) {
    super(addr);
  }
  
  public IRapiDevice next() throws RapiException {
    int[] ppIDevice = new int[1];
    int rc = Next(addr, ppIDevice);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("Next failed", rc); //$NON-NLS-1$
    }
    return new RapiDevice(ppIDevice[0]);
  }
  
  public String toString() {
    return "[RapiEnumDevices] addr: " + Integer.toHexString(addr); //$NON-NLS-1$
  }
  
  private final native int Next(int addr, int[] ppIDevice);
}
