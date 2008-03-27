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

import org.eclipse.tm.internal.rapi.RapiDesktop;

/**
 * This class is used to find connected WinCE-based remote devices. 
 * <p> Use <code>IRapiDesktop.getInstance()</code> to obtain an instance.  
 * 
 * @author Radoslav Gerganov
 */
public abstract class IRapiDesktop extends IUnknown {
  
  public IRapiDesktop(int addr) {
    super(addr);
  }

  /**
   * Returns new instance of <code>IRapiDesktop</code>.
   * Use {@link IRapiDesktop#release()} to release this instance when it is
   * no longer needed.
   * @return new instance of <code>IRapiDesktop</code>
   * @throws RapiException if an error occurs.
   */
  public synchronized static IRapiDesktop getInstance() throws RapiException {
    int[] rapiDesktop = new int[1];
    int rc = OS.CreateRapiDesktop(rapiDesktop);
    if (rc != OS.NOERROR) {
      throw new RapiException("CreateRapiDesktop failed", rc);
    }
    return new RapiDesktop(rapiDesktop[0]);
  }  
  
  /**
   * Returns an instance of <code>IRapiEnumDevices</code>.
   * Use {@link IRapiEnumDevices#release()} to release this instance when it is
   * no longer needed.
   * @return an instance of <code>IRapiEnumDevices</code>
   * @throws RapiException if an error occurs.
   */
  public abstract IRapiEnumDevices enumDevices() throws RapiException;

}
