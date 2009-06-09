/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kushal Munir IBM - Initial creation of this file.
 * David McKnight  (IBM)             [209660] delete encoding mapping when null is specified
 * David McKnight   (IBM)          - [244041] [files] Renaming a file looses Encoding property
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * This singleton class manages encodings of remote files.
 */
public class RemoteFileEncodingManager {

	private static RemoteFileEncodingManager instance;
	private static final String ENCODINGS_DIR = "encodings"; //$NON-NLS-1$
	private static final String ENCODINGS_PROPERTIES_FILE = "encodings.properties"; //$NON-NLS-1$
	private boolean isLoaded;
	private HashMap hostMap;
	
	/**
	 * Constructor for the manager.
	 */
	private RemoteFileEncodingManager() {
		init();
	}
	
	/**
	 * Initializes the manager.
	 */
	private void init() {
		isLoaded = false;
		hostMap = new HashMap();
	}

	/**
	 * Returns the singleton instance.
	 * @return the singleton instance.
	 */
	public static final RemoteFileEncodingManager getInstance() {
		
		if (instance == null) {
			instance = new RemoteFileEncodingManager();
		}
		
		return instance;
	}
	
	/**
	 * Returns the encoding for a file with the given path on a system with the given hostname.
	 * @param hostname the hostname of the system.
	 * @param remotePath the remote path of the file on the system.
	 * @return the encoding.
	 */
	public String getEncoding(String hostname, String remotePath) {
		
		// if the encodings have not been loaded from disk, load them now
		if (!isLoaded) {
			load();
		}
		
		// check to see load was successful
		if (isLoaded) {
			
			Properties props = (Properties)(hostMap.get(hostname));
			
			// if no value exists for the hostname key, then return null
			if (props == null) {
				return null;
			}
			// otherwise, check the properties
			// will return null if the remote path is not a property key
			else {
				String encoding = props.getProperty(remotePath);
				return encoding;
			}
		}
		// if loading was not successful, return null
		else {
			return null;
		}
	}
	
	/**
	 * Sets the encoding for a file with the given path on a system with the given hostname.
	 * @param hostname the hostname of the system.
	 * @param remotePath the remote path of the file on the system.
	 * @param encoding the encoding to set.
	 */
	public void setEncoding(String hostname, String remotePath, String encoding) {
		
		// if the encodings have not been loaded from disk, load them now
		if (!isLoaded) {
			load();
		}
		
		// check to see whether load was successful
		if (isLoaded) {
			
			Properties props = null;
			
			if (hostMap.containsKey(hostname)) {
				props = (Properties)(hostMap.get(hostname));
			}
			else if (encoding != null)
			{
				props = new Properties();
			}

			if (props != null)
			{
				if (encoding == null)
				{
					props.remove(remotePath);
				}
				else
				{
					props.setProperty(remotePath, encoding);
					hostMap.put(hostname, props);
				}
				save(); // this wasn't being saved persistently before
			}
		}
	}
	
	/**
	 * Loads the encoding data from disk.
	 */
	public void load() {
		
		if (!isLoaded) {
			
			IPath encodingsLocation = getEncodingLocation();
			File file = encodingsLocation.toFile();
			
			// if the encodings settings location exists
			if (file.exists() && file.isDirectory()) {
				
				// get a list of all the directory hostnames
				File[] hosts = file.listFiles();
				
				// go through each hostname dir
				for (int i = 0; i < hosts.length; i++) {
					
					// do a check to ensure it indeed is a dir
					if (hosts[i].exists() && hosts[i].isDirectory()) {
						
						// check if encodings.properties file exists in the dir
						IPath encodingsFilePath = new Path(hosts[i].getAbsolutePath());
						
						if (!encodingsFilePath.hasTrailingSeparator()) {
							encodingsFilePath.addTrailingSeparator();
						}
						
						encodingsFilePath = encodingsFilePath.append(ENCODINGS_PROPERTIES_FILE);
						encodingsFilePath = encodingsFilePath.makeAbsolute();
						
						File encodingsFile = encodingsFilePath.toFile();
						
						if (!encodingsFile.exists()) {
							continue;
						}
						else {
							Properties props = new Properties();
							InputStream inputStream = null;
							
							try {
								inputStream = new FileInputStream(encodingsFile);
								props.load(inputStream);
								hostMap.put(hosts[i].getName(), props);
							}
							catch (FileNotFoundException e) {
								RSEUIPlugin.logError("File " + encodingsFilePath.toOSString() + " could not be found", e); //$NON-NLS-1$ //$NON-NLS-2$
								continue;
							}
							catch (IOException e) {
								RSEUIPlugin.logError("I/O problems reading file " + encodingsFilePath.toOSString(), e); //$NON-NLS-1$
								continue;
							}
						}
					}
				}
			}
			
			isLoaded = true;
		}
	}
	
	/**
	 * Saves the encoding data to disk. 
	 */
	public void save() {
			
		Set hosts = hostMap.keySet();
		
		// if there are hosts in the hashmap
		if (hosts != null && !hosts.isEmpty()) {
			
			// get the location for encoding settings
			IPath encodingsLocation = getEncodingLocation();
			File file = encodingsLocation.toFile();
			
			// create the location if it does not exist
			if (!file.exists()) {
				file.mkdirs();
			}
			
			// if the location now exists
			if (file.exists() && file.isDirectory()) {
				
				Iterator iter = hosts.iterator();
				
				while (iter.hasNext()) {
					String hostname = (String)(iter.next());
					
					IPath hostDirPath = new Path(encodingsLocation.toOSString());
					
					if (!hostDirPath.hasTrailingSeparator()) {
						hostDirPath.addTrailingSeparator();
					}
					
					hostDirPath = hostDirPath.append(hostname);
					hostDirPath.makeAbsolute();
					
					File hostDir = hostDirPath.toFile();
					
					Properties props = (Properties)(hostMap.get(hostname));
					
					if (props != null && !props.isEmpty()) {
					
						if (!hostDir.exists()) {
							hostDir.mkdirs();
						}
					
						if (hostDir.exists() && hostDir.isDirectory()) {
						
							IPath encodingsFilePath = new Path(hostDirPath.toOSString());
							encodingsFilePath = encodingsFilePath.append(ENCODINGS_PROPERTIES_FILE);
							encodingsFilePath = encodingsFilePath.makeAbsolute();
						
							File encodingsFile = encodingsFilePath.toFile();
						
							if (!encodingsFile.exists()) {
								boolean created = false;
							
								try {
									created = encodingsFile.createNewFile();
								}
								catch (IOException e) {
									RSEUIPlugin.logError("I/O error when trying to create encodings file " + encodingsFilePath.toOSString(), e); //$NON-NLS-1$
									created = false;
								}
							
								if (!created) {
									continue;
								}
							}
						
							OutputStream outStream = null;
						
							try {
								outStream = new FileOutputStream(encodingsFile);
								props.store(outStream, null);
							}
							catch (FileNotFoundException e) {
								RSEUIPlugin.logError("File " + encodingsFilePath + " could not be found", e); //$NON-NLS-1$ //$NON-NLS-2$
								continue;
							}
							catch (IOException e) {
								RSEUIPlugin.logError("I/O problems writing to file " + encodingsFilePath, e); //$NON-NLS-1$
								continue;
							}
						}
					}
					// no properties for hostname, so remove the dir corresponding to the hostname if it exists
					else {
						
						if (hostDir.exists()) {
							hostDir.delete();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the absolute path of the location that contains the encoding settings.
	 * @return the absolute path of the location that contains encoding settings.
	 */
	private IPath getEncodingLocation() {
		IPath location = RSEUIPlugin.getDefault().getStateLocation();
		location = location.append(ENCODINGS_DIR);
		location = location.makeAbsolute();
		return location;
	}
}
