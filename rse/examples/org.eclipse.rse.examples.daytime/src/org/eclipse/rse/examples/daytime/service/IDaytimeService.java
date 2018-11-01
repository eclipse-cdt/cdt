/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.examples.daytime.service;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.rse.services.IService;

/**
 * IDaytimeService is the interface (API) for retrieving the time of day
 * from a remote system.
 */
public interface IDaytimeService extends IService {

	/**
	 * @return a String of the form "01 MAR 2006 11:25:12 CET"
	 * @throws UnknownHostException when remote address could not be resolved
	 * @throws IOException in case of an error transferring the data
	 */
	public String getTimeOfDay() throws UnknownHostException, IOException;
	
}
