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

package org.eclipse.rse.services.files.ftp;

/**
 * Implementers of this interface provide a way to get
 * information about file properties from an FTP directory
 * listing in a way
 * that might be specific to a certain system type.
 * @author mjberger
 *
 */
public interface IFTPDirectoryListingParser 
{
	/**
	 * Return an FTPHostFile representing a line from an FTP directory listing
	 * @param line The line of text from the directory listing
	 * @param parentPath The directory that this is a listing of
	 * @return null if the line is not well formed
	 */
	public FTPHostFile getFTPHostFile(String line, String parentPath);
}