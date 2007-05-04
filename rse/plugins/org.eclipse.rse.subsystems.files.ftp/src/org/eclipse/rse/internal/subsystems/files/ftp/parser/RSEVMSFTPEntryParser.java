/********************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.VMSFTPEntryParser;

public class RSEVMSFTPEntryParser extends VMSFTPEntryParser {

	private static final Pattern PERMISSIONS_PATTERN= Pattern.compile(".*(\\([a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*\\))");	 //$NON-NLS-1$

	public FTPFile parseFTPEntry(String entry)
	{
		FTPFile f = super.parseFTPEntry(entry);

		if(f != null)
		{
			 if (!isVersioning())
	         {
	             if(f.getName().lastIndexOf(".DIR")!=-1) //$NON-NLS-1$
	             {
	            	 f.setName(f.getName().substring(0, f.getName().lastIndexOf(".DIR"))); //$NON-NLS-1$
	             }
	         }

			 Matcher m = PERMISSIONS_PATTERN.matcher(entry.trim());

			 if(m.matches())
			 {
			 	 //Set file permission.
		         //VMS has (SYSTEM,OWNER,GROUP,WORLD) users that can contain
		         //R (read) W (write) E (execute) D (delete)

				 StringTokenizer t = new StringTokenizer(m.group(1), ","); //$NON-NLS-1$

		         //discard SYSTEM permission
		         t.nextElement();

		         //iterate for OWNER GROUP WORLD permissions
		         for (int access = 0; access < 3; access++)
		         {
		        	String token = t.nextToken();

		        	f.setPermission(access, FTPFile.READ_PERMISSION, token.indexOf('R') >= 0);
					f.setPermission(access, FTPFile.WRITE_PERMISSION, token.indexOf('W') >= 0);
					f.setPermission(access, FTPFile.EXECUTE_PERMISSION, token.indexOf('E') >= 0);
		         }
			 }
		}

		return f;
	}
}
