/********************************************************************************
 * Copyright (c) 2007, 2008  IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * Kevin Doyle (IBM) - [191548] Deleting Read-Only directory removes it from view and displays no error
 * Xuan Chen (IBM) -   [200417] [regression][dstore] Rename an expanded folder in an Archive displays no children
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 ********************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;

public class ArchiveQueryThread extends QueryThread {

	private boolean _foldersOnly;


	public ArchiveQueryThread(DataElement subject, DataElement attributes,
			boolean caseSensitive, boolean foldersOnly, boolean showHidden,
			boolean isWindows, DataElement status) {
		super(subject, status);
		_foldersOnly = foldersOnly;
	}

	public void run() {
		super.run();
		
		doQueryAll();

		if (!isCancelled()) {

			_isDone = true;
			// refresh data store
			_dataStore.refresh(_subject);

			// refresh status
			statusDone(_status);

		}
	}

	protected void doQueryAll() {
		{
			File fileobj = null;
			String queryType = _subject.getType();
			boolean isTypeFilter = queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
			boolean isArchiveFile = false;
			String pathValue = _subject.getValue();
			String path = pathValue;
			if (isTypeFilter)
			{
				if (ArchiveHandlerManager.getInstance().isArchive(new File(pathValue.toString())))
				{
					isArchiveFile = true;
				}
			}
			else
			{
				if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) 
				{
					isArchiveFile = true;
				}
			}
			
			try {
				ArchiveHandlerManager mgr = ArchiveHandlerManager.getInstance();
				
				String rootPath = path;
				String virtualPath = ""; //$NON-NLS-1$

				VirtualChild[] children = null;

				if (isArchiveFile) {
					// it's an archive file (i.e. file.zip)
					if (!isTypeFilter)
					{
						char separatorChar = File.separatorChar;
						path = pathValue + separatorChar + _subject.getName();
						rootPath = path;
					}
					fileobj = new File(rootPath);
					_subject.setAttribute(DE.A_SOURCE, setProperties(fileobj,
							true));

					if (_foldersOnly) {
						children = mgr.getFolderContents(fileobj, ""); //$NON-NLS-1$
					} else {
						children = mgr.getContents(fileobj, ""); //$NON-NLS-1$
					}
					if (isCancelled())
						return;

				} 
				else  //This method could only be called because the subject is a filter (which can be interpreted as archive file or virtual folder,
					  //a virtual file/folder object.
				{
					if (!isTypeFilter)
					{
						//if it is not a filter, then must be a virtual file or folder.
						char separatorChar = File.separatorChar;
						if (ArchiveHandlerManager.isVirtual(_subject
								.getAttribute(DE.A_VALUE))) {
							separatorChar = '/';
						}
	
						path = pathValue + separatorChar
								+ _subject.getName();
					}
					
					// it's a virtual folder (i.e. a folder within zip)
					// need to determine the associate File object
					AbsoluteVirtualPath avp = new AbsoluteVirtualPath(path);
					rootPath = avp.getContainingArchiveString();
					virtualPath = avp.getVirtualPart();
					fileobj = new File(rootPath);

					if (fileobj.exists() && mgr.getVirtualObject(path).exists()) {

						if (_foldersOnly) {
							children = mgr.getFolderContents(fileobj,
									virtualPath);
						} else {
							children = mgr.getContents(fileobj, virtualPath);
						}

						_subject.setAttribute(DE.A_SOURCE, setProperties(mgr
								.getVirtualObject(path)));
						if (children == null || children.length == 0) {
							_dataStore
									.trace("problem with virtual:" + virtualPath); //$NON-NLS-1$
						}
						if (isCancelled())
							return;
					} else {
						// Update the properties so the file's exists() will return false
						_subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
						_subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
						_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);	
						
						// Update all the children showing that they are deleted.
						if (_subject.getNestedSize() > 0)
						{
							List nestedChildren = _subject.getNestedData();
							for (int i = nestedChildren.size() - 1; i >= 0; i--)
							{
								_dataStore.deleteObject(_subject, (DataElement) nestedChildren.get(i));
							}					
						}
						_dataStore.trace("problem with File:" + rootPath); //$NON-NLS-1$
					}
				}
				createDataElement(_dataStore, _subject, children,
						"*", rootPath, virtualPath); //$NON-NLS-1$

				if (!isCancelled())
				{
					_dataStore.refresh(_subject);
			
					FileClassifier clsfy = getFileClassifier(_subject);
					clsfy.start();
				}
				return;
			} catch (Exception e) {
				if (!(fileobj == null)) {
					try {
						(new FileReader(fileobj)).read();
					} catch (IOException ex) {
						_status.setAttribute(DE.A_VALUE,
								IClientServerConstants.FILEMSG_NO_PERMISSION);
						_status.setAttribute(DE.A_SOURCE,
								IServiceConstants.FAILED);
						_dataStore.refresh(_subject);
						statusDone(_status);
					}
				}
				_status.setAttribute(DE.A_VALUE,
						IClientServerConstants.FILEMSG_ARCHIVE_CORRUPTED);
				_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				statusDone(_status);
			}
		}
	}

	protected FileClassifier getFileClassifier(DataElement subject) {
		return new FileClassifier(subject);
	}

	/**
	 * Complete status.
	 */
	public DataElement statusDone(DataElement status) {
		status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
		_dataStore.refresh(status);
		return status;
	}

	public void cancel() {
		_isCancelled = true;
	}

	public boolean isCancelled() {
		return _isCancelled;
	}

	public boolean isDone() {
		return _isDone;
	}

	/**
	 * Method to create the DataElement object in the datastore out of a list of
	 * VirtualChildren
	 */

	protected void createDataElement(DataStore ds, DataElement subject,
			VirtualChild[] list, String filter, String rootPath,
			String virtualPath) {

		HashMap filteredChildren = new HashMap();
		List children = subject.getNestedData();
		if (children != null) {
			for (int f = 0; f < children.size(); f++) {
				if (isCancelled())
					return;
				
				DataElement child = (DataElement) children.get(f);
				String type = child.getType();
				if (type
						.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
						|| type
								.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
					if (StringCompare.compare(filter, child.getName(), false)) {
						filteredChildren.put(child.getName(), child);
					}
				} else {
					filteredChildren.put(child.getName(), child);
				}
			}
		}

		// Check if the current Objects in the DataStore are valid... exist
		// on the remote host
		try {
			if (list != null) {
				boolean found = false;
				for (int j = 0; j < list.length; ++j) {
					if (isCancelled())
						return;
					
					found = false;
					DataElement previousElement = (DataElement) filteredChildren
							.get(list[j].name);
					if (previousElement != null && !previousElement.isDeleted()) {
						// Type have to be equal as well
						String type = previousElement.getType();
						boolean isfile = !list[j].isDirectory;
						if (type
								.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
								|| (type
										.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR) && !isfile)) {
							filteredChildren.remove(list[j].name);
							found = true;
						}
					}
					DataElement deObj = null;
					VirtualChild child = list[j];
	
					if (found) {
						deObj = previousElement;
					}
					if (deObj == null) {
						if (child.isDirectory) {
							deObj = _dataStore
									.createObject(
											subject,
											IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR,
											child.name);
						} else // file
						{
							deObj = _dataStore
									.createObject(
											subject,
											IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR,
											child.name);
						}
	
					}
					String oldValue = deObj.getAttribute(DE.A_VALUE);
					String newValue = rootPath
							+ ArchiveHandlerManager.VIRTUAL_SEPARATOR + virtualPath;
					if (!oldValue.equals(newValue)) {
						deObj.setAttribute(DE.A_VALUE, newValue);
					}
					String oldSource = deObj.getAttribute(DE.A_SOURCE);
					String newSource = setProperties(child);
					if (!oldSource.startsWith(newSource)) {
						deObj.setAttribute(DE.A_SOURCE, newSource);
					}
	
				} // end for j
			}
			// Object left over in the filteredChildren is no longer in the
			// system any more. Need to remove.
			Iterator myIterator = filteredChildren.keySet().iterator();
			while (myIterator.hasNext()) {
				ds.deleteObject(subject, (DataElement) (filteredChildren
						.get(myIterator.next())));
			}
		} catch (Exception e) {
			e.printStackTrace();
			UniversalServerUtilities.logError(
					UniversalFileSystemMiner.CLASSNAME,
					"createDataElement failed with exception - isFile ", e, _dataStore); //$NON-NLS-1$
		}
	} // end currentObj not 0



	public String setProperties(VirtualChild fileObj) {
		String version = IServiceConstants.VERSION_1;
		StringBuffer buffer = new StringBuffer(500);
		long date = fileObj.getTimeStamp();
		long size = fileObj.getSize();
		boolean hidden = false;
		boolean canWrite = fileObj.getContainingArchive().canWrite();
		boolean canRead = fileObj.getContainingArchive().canRead();

		// These extra properties here might cause problems for older clients,
		// ie: a IndexOutOfBounds in UniversalFileImpl.
		String comment = fileObj.getComment();
		if (comment.equals("")) //$NON-NLS-1$
			comment = " "; // make sure this is still a //$NON-NLS-1$
		// token
		long compressedSize = fileObj.getCompressedSize();
		String compressionMethod = fileObj.getCompressionMethod();
		if (compressionMethod.equals("")) //$NON-NLS-1$
			compressionMethod = " "; //$NON-NLS-1$
		double compressionRatio = fileObj.getCompressionRatio();
		long expandedSize = size;

		buffer.append(version).append(IServiceConstants.TOKEN_SEPARATOR)
				.append(date).append(IServiceConstants.TOKEN_SEPARATOR).append(
						size).append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(hidden).append(IServiceConstants.TOKEN_SEPARATOR).append(
				canWrite).append(IServiceConstants.TOKEN_SEPARATOR).append(
				canRead);

		buffer.append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(comment).append(IServiceConstants.TOKEN_SEPARATOR)
				.append(compressedSize).append(
						IServiceConstants.TOKEN_SEPARATOR).append(
						compressionMethod).append(
						IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(
				IServiceConstants.TOKEN_SEPARATOR).append(expandedSize);

		return buffer.toString();
	}

}
