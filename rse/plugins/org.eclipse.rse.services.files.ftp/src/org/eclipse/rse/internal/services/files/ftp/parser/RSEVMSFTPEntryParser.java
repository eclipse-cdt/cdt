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

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;

public class RSEVMSFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {

	private static final String DEFAULT_DATE_FORMAT 
	= "d-MMM-yyyy HH:mm:ss"; //9-NOV-2001 12:30:24 //$NON-NLS-1$
	
	private static final String REGEX =
        "(.*;[0-9]+)\\s*" //$NON-NLS-1$
        + "(\\d+)/\\d+\\s*" //$NON-NLS-1$
        +"(\\S+)\\s+(\\S+)\\s+" //$NON-NLS-1$
        + "\\[(([0-9$A-Za-z_]+)|([0-9$A-Za-z_]+),([0-9$a-zA-Z_]+))\\]?\\s*" //$NON-NLS-1$
        + "(\\([a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*\\))";	 //$NON-NLS-1$
	
	 /**
     * Constructor for a VMSFTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public RSEVMSFTPEntryParser()
    {
        this(null);
    }

    /**
     * This constructor allows the creation of a VMSFTPEntryParser object with
     * something other than the default configuration.
     *
     * @param config The {@link FTPClientConfig configuration} object used to 
     * configure this parser.
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     * @since 1.4
     */
    public RSEVMSFTPEntryParser(FTPClientConfig config)
    {
        super(REGEX);
        configure(config);
    }
	
 /**
  * Parses a line of a VMS FTP server file listing and converts it into a
  * usable format in the form of an <code> FTPFile </code> instance.  If the
  * file listing line doesn't describe a file, <code> null </code> is
  * returned, otherwise a <code> FTPFile </code> instance representing the
  * files in the directory is returned.
  * <p>
  * @param entry A line of text from the file listing
  * @return An FTPFile instance corresponding to the supplied entry
  */
 public FTPFile parseFTPEntry(String entry)
 {
     //one block in VMS equals 512 bytes
     long longBlock = 512;

     if (matches(entry))
     {
         FTPFile f = new FTPFile();
         f.setRawListing(entry);
         String name = group(1);
         String size = group(2);
     	 String datestr = group(3)+" "+group(4); //$NON-NLS-1$
         String owner = group(5);
         String permissions = group(9);
         try
         {
             f.setTimestamp(super.parseTimestamp(datestr));
         }
         catch (ParseException e)
         {
         	return null;  // this is a parsing failure too.
         }


         String grp;
         String user;
         StringTokenizer t = new StringTokenizer(owner, ","); //$NON-NLS-1$
         switch (t.countTokens()) {
             case 1:
                 grp  = null;
                 user = t.nextToken();
                 break;
             case 2:
                 grp  = t.nextToken();
                 user = t.nextToken();
                 break;
             default:
                 grp  = null;
                 user = null;
         }

         if (name.lastIndexOf(".DIR") != -1) //$NON-NLS-1$
         {
             f.setType(FTPFile.DIRECTORY_TYPE);
         }
         else
         {
             f.setType(FTPFile.FILE_TYPE); 
         }
         //set FTPFile name
         //Check also for versions to be returned or not
         if (isVersioning())
         {
             f.setName(name);
         }
         else
         {
             name = name.substring(0, name.lastIndexOf(";")); //$NON-NLS-1$
             if(name.lastIndexOf(".DIR")!=-1) //$NON-NLS-1$
             {
            	 name = name.substring(0, name.lastIndexOf(".DIR")); //$NON-NLS-1$
             }
             f.setName(name);
         }
         //size is retreived in blocks and needs to be put in bytes
         //for us humans and added to the FTPFile array
         long sizeInBytes = Long.parseLong(size) * longBlock;
         f.setSize(sizeInBytes);

         f.setGroup(grp);
         f.setUser(user);
         //set group and owner
         
         //Set file permission. 
         //VMS has (SYSTEM,OWNER,GROUP,WORLD) users that can contain
         //R (read) W (write) E (execute) D (delete)
         
         t = new StringTokenizer(permissions, ","); //$NON-NLS-1$
         
         //discard SYSTEM permission
         t.nextElement();
         
         //iterate for OWNER GROUP WORLD permissions 
         for (int access = 0; access < 3; access++)
         {
        	 String token = t.nextToken();
        	 
        	 f.setPermission(access, FTPFile.READ_PERMISSION, token.indexOf('R')!=-1);
        	 f.setPermission(access, FTPFile.WRITE_PERMISSION, token.indexOf('W')!=-1);
        	 f.setPermission(access, FTPFile.EXECUTE_PERMISSION, token.indexOf('E')!=-1);
         }
         
         return f;
     }
     return null;
 }
 
 
 /**
  * Reads the next entry using the supplied BufferedReader object up to
  * whatever delemits one entry from the next.   This parser cannot use
  * the default implementation of simply calling BufferedReader.readLine(),
  * because one entry may span multiple lines.
  *
  * @param reader The BufferedReader object from which entries are to be
  * read.
  *
  * @return A string representing the next ftp entry or null if none found.
  * @exception IOException thrown on any IO Error reading from the reader.
  */
 public String readNextEntry(BufferedReader reader) throws IOException
 {
     String line = reader.readLine();
     StringBuffer entry = new StringBuffer();
     while (line != null)
     {
         if (line.startsWith("Directory") || line.startsWith("Total")) {  //$NON-NLS-1$//$NON-NLS-2$
             line = reader.readLine();
             continue;
         }

         entry.append(line);
         if (line.trim().endsWith(")")) //$NON-NLS-1$
         {
             break;
         }
         line = reader.readLine();
     }
     return (entry.length() == 0 ? null : entry.toString());
 }

 protected boolean isVersioning() {
     return false;
 }
 
 /**
  * Defines a default configuration to be used when this class is
  * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
  * parameter being specified.
  * @return the default configuration for this parser.
  */
protected FTPClientConfig getDefaultConfiguration() {
     return new FTPClientConfig(
             FTPClientConfig.SYST_VMS,
             DEFAULT_DATE_FORMAT,
             null, null, null, null);
 }


}
