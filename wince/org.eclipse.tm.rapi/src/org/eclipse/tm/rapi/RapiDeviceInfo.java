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
 * This class contains information that identifies a particular WinCE device.
 * 
 * @author Radoslav Gerganov
 */
public class RapiDeviceInfo {

  public String id;
  public int versionMajor;               
  public int versionMinor;               
  public String name;       
  public String platform;
}
