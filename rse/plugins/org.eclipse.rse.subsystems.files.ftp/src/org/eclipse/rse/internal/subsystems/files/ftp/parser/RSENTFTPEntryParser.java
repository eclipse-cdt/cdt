/********************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - [198272] Classification for executable in WinNT parser
 *   Javier Montalvo Orus (Symbian) - [198635] On Windows servers, only lowercase *.exe files are treated as executable
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.NTFTPEntryParser;

public class RSENTFTPEntryParser extends NTFTPEntryParser {

	public FTPFile parseFTPEntry(String entry)
    {
		FTPFile f = super.parseFTPEntry(entry);

		if(f != null)
		{
		    // only USER permission is shown in RSE
	   	 	f.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
	   	 	f.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);

	   	 	if(f.getName().toLowerCase().endsWith(".exe")) //$NON-NLS-1$
	   	 	{
	   	 		f.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, true);
	   	 	}
		}
   	    return (f);
    }
}
