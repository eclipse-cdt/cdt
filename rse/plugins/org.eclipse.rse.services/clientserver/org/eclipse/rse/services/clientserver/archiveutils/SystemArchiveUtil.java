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

package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.File;
import java.util.HashSet;

public class SystemArchiveUtil {

	/**
	 * Helper method to delete a directory. Deletes the children of the directory before
	 * deleting the directory itself. This method is required because a directory can not be deleted if it
	 * is not empty.
	 * @return <code>true</code> if the deletion was successful, <code>false</code> otherwise.
	 */
	public static boolean delete(File file) {
		HashSet set = new HashSet();
		return recursiveDelete(file, set);
	}
	
	/**
	 * Recursively delete a file.
	 * @param file the file or directory.
	 * @param found a set of files that have been considered.
	 * @return <code>true</code> if deletion successful, <code>false</code> otherwise.
	 */
	protected static boolean recursiveDelete(File file, HashSet found) {
		boolean success = true;
		found.add(file);
		File[] children = file.listFiles();
		
		if (children != null) {
			
			for (int i = 0; i < children.length; i++) {
				
				if (!found.contains(children[i])) {
					
					success = recursiveDelete(children[i], found);
					
					if (!success) {
						return false;
					} 
				}
			}
		}
		
		if (file.exists()) {
			success = file.delete();
		}
		
		return success;
	}
}