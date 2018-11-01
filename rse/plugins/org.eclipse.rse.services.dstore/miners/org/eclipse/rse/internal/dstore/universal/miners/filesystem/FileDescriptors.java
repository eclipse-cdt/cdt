/********************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 * David McKnight   (IBM) - [371401] [dstore][multithread] avoid use of static variables - causes memory leak after disconnect
 ********************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import org.eclipse.dstore.core.model.DataElement;

public class FileDescriptors 
{
	public DataElement _deUniversalFileObject;
	public DataElement _deUniversalFolderObject;
	public DataElement _deUniversalVirtualFileObject;
	public DataElement _deUniversalVirtualFolderObject;
	public DataElement _deUniversalArchiveFileObject;
	
}
