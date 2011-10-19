/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight (IBM) - [192884] Should not use filter to determine previous query results
 * David McKnight (IBM) - [209387] Should not delete elements for files that still exist (but are filtered out)
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight  (IBM)  - [251650] [dstore] Multiple copies of symbolic link file show in Table view
 * David McKnight  (IBM)  - [251729][dstore] problems querying symbolic link folder
 * David McKnight  (IBM)  - [358301] [DSTORE] Hang during debug source look up
 *******************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;

public class FileQueryThread extends QueryThread
{

	private File _fileobj;
	private String _queryType;
	private String _filter;
	private boolean _caseSensitive;
	private int _inclusion;
	private boolean _showHidden;
	private boolean _isWindows;

	
	public FileQueryThread(
			DataElement subject, File fileobj, 
			String queryType, String filter, boolean caseSensitive,
			int inclusion, 
			boolean showHidden, boolean isWindows,
			DataElement status)
	{
		super(subject, status);
		_fileobj = fileobj;
		_queryType = queryType;
		_filter = filter;
		_caseSensitive = caseSensitive;
		_inclusion = inclusion;
		_showHidden = showHidden;
		_isWindows = isWindows;
	}

	
	public void run()
	{
		super.run();
		
		doQueryAll();
	
		if (!isCancelled())
		{
			
			_isDone = true;	
			// refresh data store
			_dataStore.refresh(_subject);
			
			// refresh status
			statusDone(_status);
				
		}		
	}
	
	protected void doQueryAll() {
		if (_fileobj.exists()) 
		{			
			boolean filterFiles = (_inclusion == IClientServerConstants.INCLUDE_ALL) || (_inclusion == IClientServerConstants.INCLUDE_FILES_ONLY);
			boolean filterFolders = (_inclusion == IClientServerConstants.INCLUDE_ALL) || (_inclusion == IClientServerConstants.INCLUDE_FOLDERS_ONLY);
			
			UniversalFileSystemFilter filefilter = new UniversalFileSystemFilter(_filter,filterFiles, filterFolders, _caseSensitive);
			String theOS = System.getProperty("os.name"); //$NON-NLS-1$
			File[] list = null;
			if (theOS.equals("z/OS")) //$NON-NLS-1$ 
			{
				// filters not supported with z/OS jvm
				File[] tempList = _fileobj.listFiles();
				List acceptedList = new ArrayList(tempList.length);
	
				for (int i = 0; i < tempList.length; i++) {
					File afile = tempList[i];
					if (filefilter.accept(_fileobj, afile.getName())) {
						acceptedList.add(afile);
					}
				}
				list = new File[acceptedList.size()];
				for (int l = 0; l < acceptedList.size(); l++)
					list[l] = (File) acceptedList.get(l);
			} 
			else 
			{
				list = _fileobj.listFiles(filefilter);
			}
	
			if (!_isCancelled)
			{
				if (list != null)
				{
					createDataElement(_dataStore, _subject, list, _queryType, _filter,_inclusion);

					if (_subject.getSource() == null || _subject.getSource().equals("")){ //$NON-NLS-1$
						String folderProperties = setProperties(_fileobj);
						_subject.setAttribute(DE.A_SOURCE, folderProperties);
					}
					
					if (!_isCancelled)
					{
						FileClassifier clsfy = getFileClassifier(_subject);
						clsfy.start();
					}
				}
			}
		}
		else {
		}
		
	}
	

	protected FileClassifier getFileClassifier(DataElement subject)
	{
	    return new FileClassifier(subject);
	}

	protected void createDataElement(DataStore ds, DataElement subject,
			File[] list, String queryType, String filter, int include)
	{
		createDataElement(ds, subject, list, queryType, filter, include, null);
	}
	
	/**
	 * Method to create the DataElement object in the datastore.
	 */
	protected void createDataElement(DataStore ds, DataElement subject,
			File[] list, String queryType, String filter, int include, String types[]) 
	{
		HashMap filteredChildren = new HashMap();
		List children = subject.getNestedData();
		if (children != null)
		{
			//Use a HashMap instead of array list to improve performance
			for (int f = 0; f < children.size(); f++)
			{
				if (_isCancelled) {
					return;
				}
				
				DataElement child = (DataElement)children.get(f);
				if (!child.isDeleted())
				{
						filteredChildren.put(child.getName(), child);
				}
			}
		}
			
		
			
		boolean found = false;
				
		// Check if the current Objects in the DataStore are valid... exist
		// on the remote host
		try {
				for (int j = 0; j < list.length; ++j) 
				{
					if (_isCancelled) {
						return;
					}
					
					found = false;
					File file = list[j];
					String fileName = file.getName();
					boolean isHidden = file.isHidden() || fileName.charAt(0) == '.';

					DataElement previousElement = (DataElement)filteredChildren.get(fileName);
					if (previousElement != null && !previousElement.isDeleted()) 
					{
						// Type have to be equal as well
						//String type = ((DataElement) currentObjList[i]).getType();
						String type = previousElement.getType();
						boolean isfile = !list[j].isDirectory();
						if (((type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || type.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) && isfile)
								|| 
							(type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR) && !isfile))
						{
							if (types !=null)
							{
								String attributes = previousElement.getAttribute(DE.A_SOURCE);
								String thisType = types[j];
								if (attributes.indexOf(thisType) != -1)
								{
								    filteredChildren.remove(list[j].getName()); //remove it from the filterChildren list
									found = true;
								}
							}
							else
							{
							    filteredChildren.remove(list[j].getName());
								found = true;
							}
						}
					}
					
					DataElement deObj = null;
					if (!isHidden || _showHidden)
					{
						if (found)
						{
							//this object already exists in the DStore
							deObj = previousElement;
						}
						else
						{
							//We need to create a new data element for this object.
							if (include == IClientServerConstants.INCLUDE_ALL) 
							{
								if (file.isDirectory())
								{
									deObj = ds.createObject(subject,FileDescriptors._deUniversalFolderObject,fileName);
								}
								else
								// file
								{
									if (ArchiveHandlerManager.getInstance().isArchive(file)) 
									{
										deObj = ds
												.createObject(
														subject,
														FileDescriptors._deUniversalArchiveFileObject,
														fileName);
									} 
									else 
									{
										deObj = ds.createObject(subject,
												FileDescriptors._deUniversalFileObject,
												fileName);
									}
								}
							} 
							else if (include == IClientServerConstants.INCLUDE_FOLDERS_ONLY) 
							{
								if (ArchiveHandlerManager.getInstance().isArchive(file)) 
								{
									deObj = ds.createObject(subject,
											FileDescriptors._deUniversalArchiveFileObject,
											fileName);
								} 
								else 
								{
									deObj = ds.createObject(subject,
											FileDescriptors._deUniversalFolderObject,
											fileName);
								}
							} 
							else if (include == IClientServerConstants.INCLUDE_FILES_ONLY) 
							{
								if (ArchiveHandlerManager.getInstance().isArchive(file)) 
								{
									deObj = ds.createObject(subject,
											FileDescriptors._deUniversalArchiveFileObject,
											fileName);
								} 
								else 
								{
									deObj = ds
											.createObject(subject,
													FileDescriptors._deUniversalFileObject,
													fileName);
								}
							}
							if (deObj != null)
							{
								if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
								{
									deObj.setAttribute(DE.A_VALUE, subject.getAttribute(DE.A_VALUE));
								}
								else 
								{
								
									if (subject.getName().length() > 0) 
									{
										String valueStr = subject.getAttribute(DE.A_VALUE);
										//String valueStr = list[i].getParentFile().getAbsolutePath();
										StringBuffer valueBuffer = new StringBuffer(valueStr);
										if ((_isWindows && valueStr.endsWith("\\"))|| valueStr.endsWith("/") || subject.getName().startsWith("/"))  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										{
											valueBuffer.append(subject.getName());
											deObj.setAttribute(DE.A_VALUE,valueBuffer.toString());
										} 
										else 
										{
											valueBuffer.append(File.separatorChar);
											valueBuffer.append(subject.getName());
											deObj.setAttribute(DE.A_VALUE,valueBuffer.toString());
										}
									} 
									else 
									{
										String valueStr = list[j].getParentFile().getAbsolutePath();
										deObj.setAttribute(DE.A_VALUE, valueStr);
									}
								}
							}
						}
						
						String properties = setProperties(file);
						if (deObj != null)
						{
							if (types != null)
							{
								String oldSource = deObj.getAttribute(DE.A_SOURCE);
								String newSource = properties + "|" + types[j]; //$NON-NLS-1$
								if (!oldSource.startsWith(newSource))
								                                            
							    {
							        deObj.setAttribute(DE.A_SOURCE, newSource); 
							    }
							}
							else
							{
								String oldSource = deObj.getAttribute(DE.A_SOURCE);
								String newSource = properties;
								if (!oldSource.startsWith(newSource))
									deObj.setAttribute(DE.A_SOURCE, properties);
							}
						}
					}
				} // end for j
				
				//Object left over in the filteredChildren is no longer in the system any more.  Need to remove.
				if (!filteredChildren.isEmpty())
				{
					// get the complete list of files (because we're only working with filtered right now
					String[] completeList = _fileobj.list();
					
					Iterator myIterator = filteredChildren.keySet().iterator();
					while(myIterator.hasNext()) 
					{
						DataElement oldChild = (DataElement)filteredChildren.get(myIterator.next());
						String oldName = oldChild.getName();
						boolean foundOnSystem = false;
						for (int c = 0; c < completeList.length && !foundOnSystem; c++){
							if (completeList[c].equals(oldName)){
								foundOnSystem = true;
							}
						}
						if (!foundOnSystem){
							ds.deleteObject(subject, oldChild);
						}
					}
				}

		} 
		catch (OutOfMemoryError e){
			System.exit(-1);
		}
		catch (Exception e) {
			e.printStackTrace();
			UniversalServerUtilities.logError(UniversalFileSystemMiner.CLASSNAME,
					"createDataElement failed with exception - isFile ", e, _dataStore); //$NON-NLS-1$
		}

	}
	

	





}
