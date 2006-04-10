/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.resources;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class should be used when there's a file already in the
 * destination and we don't want to lose its contents if a
 * failure writing this stream happens.
 * Basically, the new contents are written to a temporary location.
 * If everything goes OK, it is moved to the right place.
 * The user has the option to define the temporary location or 
 * it will be created in the default-temporary directory
 */
public class SystemSafeFileOutputStream extends OutputStream {



	protected File destination = null;
	protected File temp = null;
	protected OutputStream output = null;
	protected boolean failed = false;
	protected static final String BACKUP_EXTENSION = ".bak";
	
	/**
	 * Constructor for SystemSafeFileOutputStream.
	 * @param the destination file.
	 */
	public SystemSafeFileOutputStream(File file) throws IOException {
		this(file.getAbsolutePath(), null);
	}
	
	/**
	 * Constructor for SystemSafeFileOutputStream.
	 * @param the destination file name
	 */
	public SystemSafeFileOutputStream(String destinationName) throws IOException {
		this(destinationName, null);
	}
	
	/**
	 * Constructor for SystemSafeFileOutputStream.
	 * @param the destination file name
	 * @param the temporary file name
	 */
	public SystemSafeFileOutputStream(String destinationPath, String tempPath) throws IOException {
		destination = new File(destinationPath);
		createTempFile(tempPath);
		
		if (!destination.exists()) {
			
			if (!temp.exists()) {
				output = new BufferedOutputStream(new FileOutputStream(destination));
				return;
			}
			
			// If we do not have a file at destination location, but we do have at temp location,
			// it probably means something wrong happened the last time we tried to write it.
			// So, try to recover the backup file. And, if successful, write the new one.
			copy(temp, destination);
		}
		
		output = new BufferedOutputStream(new FileOutputStream(temp));
	}
	
	/**
	 * Close the stream.
	 */
	public void close() throws IOException {
		
		try {
			output.close();
		}
		catch (IOException e) {
			failed = true;
			throw e;
		}
		
		if (failed) {
			temp.delete();
		}
		else {
			commit();
		}
	}
	
	/**
	 * Commit the temporary file to the destination.
	 */
	protected void commit() throws IOException {
		
		if (!temp.exists()) {
			return;
		}
		
		destination.delete();
		copy(temp, destination);
		temp.delete();
	}
	
	/**
	 * Copy contents of one file to another.
	 * @param the source file
	 * @param the destination file
	 */
	protected void copy(File sourceFile, File destinationFile) throws IOException {
		
		if (!sourceFile.exists()) {
			return;
		}
			
		FileInputStream source = new FileInputStream(sourceFile);
		FileOutputStream destination = new FileOutputStream(destinationFile);
		transferData(source, destination);
	}
	
	/**
	 * Create the temporary file at the given path.
	 * @param the path of the temporary file to be created
	 */
	protected void createTempFile(String tempPath) throws IOException {
		
		if (tempPath == null) {
			tempPath = destination.getAbsolutePath() + BACKUP_EXTENSION;
		}
			
		temp = new File(tempPath);
	}
	
	/**
	 * Flush the stream.
	 */
	public void flush() throws IOException {
		try {
			output.flush();
		} catch (IOException e) {
			failed = true;
			throw e; // rethrow
		}
	}
	
	/**
	 * Get the temporary file path.
	 * @return the path of the temporary file
	 */
	public String getTempFilePath() {
		return temp.getAbsolutePath();
	}
	
	/**
	 * Transfers data from one stream to another.
	 * @param source stream
	 * @param destination stream
	 */
	protected void transferData(InputStream source, OutputStream destination) throws IOException {
		
		try {
			byte[] buffer = new byte[8192];
			
			while (true) {
				int bytesRead = source.read(buffer);
				
				if (bytesRead == -1) {
					break;
				}
				
				destination.write(buffer, 0, bytesRead);
			}
		}
		finally {
			try {
				source.close();
			}
			catch (IOException e) {
			}
			
			try {
				destination.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	/**
	 * Write an integer.
	 * @param the integer to write
	 */
	public void write(int b) throws IOException {
		
		try {
			output.write(b);
		}
		catch (IOException e) {
			failed = true;
			throw e;
		}
	}
}