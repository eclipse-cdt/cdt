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
 * This class contains information for a new process 
 * created with {@link IRapiSession#createProcess(String, String, int)}
 * 
 * @author Radoslav Gerganov
 */
public class ProcessInformation {
  public int hProcess;
  public int hThread;
  public int dwProcessId;
  public int dwThreadId;
}
