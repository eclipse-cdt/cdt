/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.dstore.core.model;

import java.io.File;

/**
 * Interface for converting between host bytes and client bytes
 */
public interface IByteConverter 
{
	public void setContext(File file);
	public byte[] convertHostBytesToClientBytes(byte[] buffer, int offset, int length);
	public byte[] convertClientBytesToHostBytes(byte[] buffer, int offset, int length);
}