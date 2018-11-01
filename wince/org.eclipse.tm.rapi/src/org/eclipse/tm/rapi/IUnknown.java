/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.rapi;

/**
 * Java wrapper for the native IUnknown interface.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @author Radoslav Gerganov
 */
public abstract class IUnknown {
  
  /**
   * Pointer to the underlying <code>IUnknown</code> object.
   */
  protected int addr;
  
  public IUnknown(int addr) {
    this.addr = addr;
  }
  
  /**
   * Releases the underlying <code>IUnknown<code> object.
   */
  public void release() {
    Rapi.ReleaseIUnknown(addr);
  }
  
}
