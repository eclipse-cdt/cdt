/********************************************************************************
 * Copyright (c) 2008 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - [225821] [ftp] opening "/home" fails on OS/400 IFS
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.OS400FTPEntryParser;

public class RSEOS400FTPEntryParser extends OS400FTPEntryParser {
	 public FTPFile parseFTPEntry(String entry)
	    {
		 FTPFile f = super.parseFTPEntry(entry);
		 
		 if(f != null)
		 {
		 	 // only USER permission is shown in RSE
	    	 f.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
	    	 f.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
		 }
		 
		 return f;
	    }
}
