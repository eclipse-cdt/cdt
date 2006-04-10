/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
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

package org.eclipse.rse.subsystems.files.core.model;


import org.eclipse.core.resources.IFile;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;



public interface ISystemFileTransferModeRegistry extends ISystemFileTypes
{
	
	// Default text file names
	public static final String[] DEFAULT_TEXT_FILE_NAMES = {"application.xml", "build.properties", "fragment.xml",
															 "ibm-application-bnd.xmi", "ibm-application-ext.xmi",
															 "install.xml", "plugin.xml", "server-cfg.xml", "web.xml"};
															 
	// Default text file types
	public static final String[] DEFAULT_TEXT_FILE_EXTENSIONS =	{"conxmi", "css", "dad", "dadx", "dbxmi", "dtd",
																	 "ent", "htm", "html", "html-ss", "jardesc",
																	 "java", "jhtml", "jpage", "jsp", "nst",
																	 "properties", "schxmi", "shtm", "shtml", "tblxmi",
																	 "tld", "txt", "wsdl", "xhtml", "xmi", "xml",
																	 "xsd", "xsl"};
																	 
	// Default text file types that only we recognize through LPEX
	public static final String[] DEFAULT_LPEX_TEXT_FILE_EXTENSIONS =	{"app", "asm", "c", "c++", "cbl", "cc", "ccs",
																		 "cmd", "cpp", "cpy", "cxx", "f", "f90", "f95",
																		 "h", "h++", "hh", "hla", "hpp", "htm", "html",
																		 "hxx", "inc", "inl", "jav", "java", "jcl",
																		 "jj", "lx", "lxl", "lxu", "mac", "pli", "pro",
																		 "properties", "rc", "rex", "rexx", "s", "sqc",
																		 "sql", "sqlj", "xml", "xsl"};
	
	// the following need to match what is in plugin.xml! */
	/* scrubbed by phil 10/16/2002... removed old code/400 extensions not used anymore, and added .cpy for cobol on ifs
	public static final String[] DEFAULT_ISERIES_LPEX_TEXT_FILE_EXTENSIONS = 	{"c", "cbl", "cblle", "cicsc",
																				 "cicscbl", "cicscblle", "cicssqlcbl",
																				  "cl", "cle", "clle", "clp", "cmd",
																				  "cpp", "cpple", "dsp", "dspf",
																				  "dspf400", "icf", "icff", "icff400",
																				  "irp", "lf", "lf4", "lf400", "mnudds",
																				  "pf", "pf4", "pf400", "pnlgrp", "prt",
																				  "prtf", "prtf400", "rpg", "rpg36",
																				  "rpg38", "rpgle", "rpgleinc", "rpt",
																				  "sir", "sqlc", "sqlcbl", "sqlcblle",
																				  "sqlcpple", "sqlrpg", "sqlrpgle",
																				  "srg"}; */
	public static final String[] DEFAULT_ISERIES_LPEX_TEXT_FILE_EXTENSIONS =    {"c", "cbl", "cblle", "cicsc",
																				  "cicscbl", "cicscblle", "cicssqlcbl",
																				  "cl", "cle", "clle", "clp", "cmd",
																				  "cob",
																				  "cpp", "cpple", "cpy", "dspf",
																				  "icff", 
																				  "lf", "mbr", "mnudds",
																				  "pcml",
																				  "pf", "pnlgrp", 
																				  "prtf", "rpg", "rpg36",
																				  "rpg38", "rpgle", "rpgleinc", "rpt",
																				  "sqlc", "sqlcbl", "sqlcblle",
																				  "sqlcpple", "sqlrpg", "sqlrpgle",
																				  };
	/* added by phil 10/16/2002... */
	public static final String[] DEFAULT_UNIX_LPEX_TEXT_FILE_EXTENSIONS =      {"csh", "ksh", "mak", "pl", "profile", "py",
																				  };
	
	public static final String[] DEFAULT_BINARY_FILE_EXTENSIONS         =      {"xml", "jar", "zip", "tar", "exe", "gz", "z",
																					"gif","jpg"};
	
	
	// Get all file transfer mode mappings
	public ISystemFileTransferModeMapping[] getModeMappings();
	
	
	// Query whether a file should be treated as binary
	public boolean isBinary(IFile file);
	public boolean isBinary(IRemoteFile remoteFile);
	
	
	// Query whether a file should be treated as text
	public boolean isText(IFile file);
	public boolean isText(IRemoteFile remoteFile);
}