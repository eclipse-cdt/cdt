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
 * This class is used to enumerate the set of connected WinCE-based
 * remote devices which are represented by <code>IRapiDevice</code>
 * objects. 
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @author Radoslav Gerganov
 */
public abstract class IRapiEnumDevices extends IUnknown {

  public IRapiEnumDevices(int addr) {
    super(addr);
  }

  /**
   * Returns an instance of <code>IRapiDevice</code>.
   * Use {@link IRapiDevice#release()} to release this instance when it is
   * no longer needed.
   * @return an instance of <code>IRapiDevice</code>
   * @throws RapiException if an error occurs.
   */
  public abstract IRapiDevice next() throws RapiException;

}