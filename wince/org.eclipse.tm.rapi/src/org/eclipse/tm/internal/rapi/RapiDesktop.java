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

import org.eclipse.tm.rapi.IRapiDesktop;
import org.eclipse.tm.rapi.IRapiEnumDevices;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.RapiException;

/**
 * Implementation of <code>IRapiDesktop</code>.
 * 
 * @author Radoslav Gerganov
 */
public class RapiDesktop extends IRapiDesktop {
  
  public RapiDesktop(int addr) {
    super(addr);
  }

  public IRapiEnumDevices enumDevices() throws RapiException {
    int[] ppIEnum = new int[1];
    int rc = EnumDevices(addr, ppIEnum);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("EnumDevices failed", rc); //$NON-NLS-1$
    }
    return new RapiEnumDevices(ppIEnum[0]);
  }
  
  public String toString() {
    return "[RapiDesktop] addr: " + Integer.toHexString(addr); //$NON-NLS-1$
  }

  private final native int EnumDevices(int addr, int[] ppIEnum);
}
