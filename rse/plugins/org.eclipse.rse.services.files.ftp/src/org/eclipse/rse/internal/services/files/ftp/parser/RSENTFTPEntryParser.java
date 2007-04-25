/********************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.services.files.ftp.parser;

import java.text.ParseException;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.NTFTPEntryParser;

public class RSENTFTPEntryParser extends NTFTPEntryParser {

	/**
     * Parses a line of an NT FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned. This extension enables the R/W permission
     * for NT parsing, setting it as true by default. 
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
	public FTPFile parseFTPEntry(String entry)
    {
        FTPFile f = new FTPFile();
        f.setRawListing(entry);

        if (matches(entry))
        {
        	String datestr = group(1)+" "+group(2); //$NON-NLS-1$
            String dirString = group(3);
            String size = group(4);
            String name = group(5);
            try
            {
                f.setTimestamp(super.parseTimestamp(datestr));
            }
            catch (ParseException e)
            {
            	return null;  // this is a parsing failure too.
            }

            if (null == name || name.equals(".") || name.equals(".."))  //$NON-NLS-1$//$NON-NLS-2$
            {
                return (null);
            }
            f.setName(name);


            if ("<DIR>".equals(dirString)) //$NON-NLS-1$
            {
                f.setType(FTPFile.DIRECTORY_TYPE);
                f.setSize(0);
            }
            else
            {
                f.setType(FTPFile.FILE_TYPE);
                if (null != size)
                {
                  f.setSize(Long.parseLong(size));
                }
            }
            
            // only USER permission is shown in RSE
       	 	f.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
       	 	f.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
       	 	f.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, true);
            
            return (f);
        }
        return null;
    }
	
}
