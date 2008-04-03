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
