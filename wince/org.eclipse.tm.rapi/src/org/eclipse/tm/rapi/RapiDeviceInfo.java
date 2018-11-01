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
