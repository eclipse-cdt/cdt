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
 * Signals that an error has occurred during execution of RAPI2 call. 
 * 
 * @author Radoslav Gerganov
 */
public class RapiException extends Exception {

  private static final long serialVersionUID = -1833456445593343458L;

  private int errorCode;
  
  public RapiException(String msg) {
    super(msg);
  }
  
  public RapiException(String msg, int errCode) {
    super(msg + " errorCode: 0x" + Integer.toHexString(errCode)); //$NON-NLS-1$
    this.errorCode = errCode;
  }
  
  /**
   * Returns the error code associated with this exception.
   */
  public int getErrorCode() {
    return errorCode;
  }
}
