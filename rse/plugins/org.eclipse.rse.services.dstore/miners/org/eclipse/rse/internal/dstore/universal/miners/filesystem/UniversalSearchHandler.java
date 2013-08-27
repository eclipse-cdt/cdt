/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Michael Berger   (IBM) - Bug 147791 - symbolic links can cause circular search.
 * David McKnight   (IBM)  - [190010] cancelling search
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)  - [214378] canonical path not required - problem is in the client
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 * David McKnight  (IBM)  - [250168] handle malformed binary and always resolve canonical paths
 * David McKnight  (IBM)  - [250168] update to just search file of canonical paths (not symbolic links)
 * David McKnight  (IBM)  - [255390] memory checking
 * David McKnight  (IBM)  - [261644] [dstore] remote search improvements
 * David McKnight  (IBM)  - [243495] [api] New: Allow file name search in Remote Search to not be case sensitive
 * David McKnight  (IBM)  - [299568] Remote search only shows result in the symbolic linked file
 * David McKnight  (IBM]  - [330989] [dstore] OutOfMemoryError occurs when searching for a text in a large remote file
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 * David McKnight  (IBM)  - [358301] [DSTORE] Hang during debug source look up
 * Noriaki Takatsu  (IBM) - [362025] [dstore] Search for text hung in encountering a device definition
 * David McKnight   (IBM) - [371401] [dstore][multithread] avoid use of static variables - causes memory leak after disconnect
 * Noriaki Takatsu  (IBM) - [380562] [multithread][dstore] File Search is not canceled by the client UI on disconnect
 * David McKnight   (IBM)        - [396783] [dstore] fix issues with the spiriting mechanism and other memory improvements (phase 2)
 * David McKnight   (IBM) - [414016] [dstore] new server audit log requirements
 ********************************************************************************/

package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.server.SecuredThread;
import org.eclipse.dstore.core.server.SystemServiceManager;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;
import org.eclipse.dstore.internal.core.util.MemoryManager;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.SystemFileClassifier;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.search.SystemSearchFileNameMatcher;
import org.eclipse.rse.services.clientserver.search.SystemSearchLineMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatchLocator;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatcher;

public class UniversalSearchHandler extends SecuredThread implements ICancellableHandler
{
	protected HashSet _alreadySearched;

	private File _rootFile;

	protected boolean _isCancelled;
	protected boolean _isDone;
	protected int _depth = -1;

	protected UniversalFileSystemMiner _miner;
	protected DataElement _status;

	protected SystemSearchString _searchString;
	protected SystemSearchStringMatcher _stringMatcher;
	protected boolean _isFileSearch;
	protected SystemSearchFileNameMatcher _fileNameMatcher;
	protected String _classificationString;

	protected DataElement _deGrep;
	protected DataElement _deFile;
	protected DataElement _deFolder;
	protected DataElement _deArchiveFile;
	protected DataElement _deVirtualFile;

	protected boolean _fsCaseSensitive;
	private MemoryManager _memoryManager;
	private boolean _searchOnlyUniqueFolders = true;

	public UniversalSearchHandler(DataStore dataStore, UniversalFileSystemMiner miner, SystemSearchString searchString, boolean fsCaseSensitive, File theFile, DataElement status) {
		super(dataStore);
		
		_memoryManager = new MemoryManager(dataStore);
		_miner = miner;
		_searchString = searchString;
		_fsCaseSensitive = fsCaseSensitive;
		_alreadySearched = new HashSet();

		_deGrep = _dataStore.findObjectDescriptor("grep"); //$NON-NLS-1$
		_deFile = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
		_deFolder = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
		_deArchiveFile = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
		_deVirtualFile = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		boolean includeSubfolders = searchString.isIncludeSubfolders();

		String searchUnique = System.getProperty(IDataStoreSystemProperties.DSTORE_SEARCH_ONLY_UNIQUE_FOLDERS);
		if (searchUnique != null && searchUnique.equals("false")) //$NON-NLS-1$
		{
			_searchOnlyUniqueFolders = false;
		}
		
		if (includeSubfolders) {
			_depth = -1;
		}
		else {
			_depth = 1;
		}

		_rootFile = theFile;
		_status = status;

		_isCancelled = false;
		_isDone = false;

		_stringMatcher = new SystemSearchStringMatcher(_searchString.getTextString(), _searchString.isCaseSensitive(), _searchString.isTextStringRegex());

		// if the search string is empty or if it is an asterisk, then we are doing a file search
		// i.e. we do not want to look inside files
		_isFileSearch = _stringMatcher.isSearchStringEmpty() || _stringMatcher.isSearchStringAsterisk();

		boolean fileNamesCaseSensitive = fsCaseSensitive;
		if  (fileNamesCaseSensitive){ // even though it may be a case sensitive system we may want to search case-insensitive
			fileNamesCaseSensitive = searchString.isFileNamesCaseSensitive();
		}
		_fileNameMatcher = new SystemSearchFileNameMatcher(_searchString.getFileNamesString(), fileNamesCaseSensitive, _searchString.isFileNamesRegex());

		// classification of files to restrict the search to
		_classificationString = _searchString.getClassificationString();
	}

	public void run() {
		super.run();
		try {
			internalSearch(_rootFile, _depth);
		}
		catch (Exception e) {
			UniversalServerUtilities.logError(_miner.getName(), "Error occured when calling internal search", e, _dataStore); //$NON-NLS-1$
		}

		_isDone = true;

		if (_isCancelled) {

			_miner.statusCancelled(_status);
		}
		else {
			// previously, the status would be set to done immediately because search results were sent
			// back to the client as they arrived.  Now, the search handler wait until the search has
			// completed before setting the status to done
			_status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
	        _dataStore.refresh(_status);	// true indicates refresh immediately
	        _miner.updateCancellableThreads(_status.getParent(), this);
		}
		
		_alreadySearched.clear();
		_dataStore.disconnectObjects(_status);
	}

	public boolean isCancelled() {
		return _isCancelled;
	}

	public boolean isDone() {
		return _isDone;
	}

	public void cancel() {
		_isCancelled = true;
	}


	protected boolean hasSearched(File file)
	{       
        boolean result = false;
        String path = null;
        if (!_searchOnlyUniqueFolders){
        	path = file.getAbsolutePath();
        	result = _alreadySearched.contains(path);
        }
        else {
	        try {
	        	        	
	        	path = file.getCanonicalPath();
	        	
	        	// check whether it's already been searched
	        	result = _alreadySearched.contains(path);
	        }
	        catch (Exception e){	
	        	result = _alreadySearched.contains(file.getAbsolutePath());
	        	_dataStore.trace(e);
	        }
        }

		return result;
	}

	protected void internalSearch(File theFile, int depth) throws SystemMessageException {
		
		if (!hasSearched(theFile)) {
			if (!theFile.isDirectory() && !theFile.isFile()) {
				return;
			}
			if (!_searchOnlyUniqueFolders){
				_alreadySearched.add(theFile.getAbsolutePath());
			}
			else {
				try {
					_alreadySearched.add(theFile.getCanonicalPath());
				}
				catch (Exception e){
					_alreadySearched.add(theFile.getAbsolutePath());
					_dataStore.trace(e);				
				}			
			}
	
			boolean isDirectory = theFile.isDirectory();	
	
			// is it an archive?
			boolean isArchive = ArchiveHandlerManager.getInstance().isArchive(theFile);
	
			String absPath = theFile.getAbsolutePath();
	
			String compareStr = theFile.getName();
	
			// is it a virtual file?
			boolean isVirtual = ArchiveHandlerManager.isVirtual(absPath);
	
			// is it a virtual directory?
			boolean isVirtualDirectory = false;
	
			// if it is a virtual object, then get a reference to it
			if (isVirtual) {
				VirtualChild vc = ArchiveHandlerManager.getInstance().getVirtualObject(absPath);
				isVirtualDirectory = isVirtual && vc.isDirectory;
			}
	
			// base case for the recursive method call
			// if the file is not a directory, an archive or a virtual directory,
			// and we get a match with the file name, then we can search for match within the file
			if (!isDirectory &&
					(!isArchive || _isFileSearch) &&
					!isVirtualDirectory &&
					doesFilePatternMatch(compareStr) &&
					doesClassificationMatch(absPath))
			{
				DataElement deObj = null;
	
				// if the file is a virtual file, then get matches from the archive handler
				if (ArchiveHandlerManager.isVirtual(absPath)) {
					VirtualChild vc = ArchiveHandlerManager.getInstance().getVirtualObject(absPath);
	
					if (!vc.isDirectory) {
						deObj = _dataStore.createObject(null, _deVirtualFile, compareStr);
	
						// if parent of virtual child is archive, then create it this way
						if (vc.path.equals("")) { //$NON-NLS-1$
							deObj.setAttribute(DE.A_VALUE, vc.getContainingArchive().getAbsolutePath());
						}
						else {
							deObj.setAttribute(DE.A_VALUE, vc.getContainingArchive().getAbsolutePath() +
									ArchiveHandlerManager.VIRTUAL_SEPARATOR + vc.path);
						}
	
						deObj.setAttribute(DE.A_SOURCE, _miner.setProperties(vc));
	
						SystemSearchLineMatch[] results = null;
	
						// if it's not a file search, call the handler method to search
						if (!_isFileSearch) {
							results = vc.getHandler().search(vc.fullName, _stringMatcher, null);
	
							// if at least one match found, then send back the remote file with matches
							if (results != null && results.length > 0) {
								convert(deObj, absPath, results);
								deObj.setParent(_status);
								_status.addNestedData(deObj, false);
							}
						}
						// otherwise if it is a file search, return the remote file back with no children
						else {
							deObj.setParent(_status);
							_status.addNestedData(deObj, false);
						}
					}
				}
				// otherwise, search the file
				else {
					if (!isArchive) {
						deObj = _dataStore.createObject(null, _deFile, compareStr);
					}
					else {
						deObj = _dataStore.createObject(null, _deArchiveFile, compareStr);
					}
	
					deObj.setAttribute(DE.A_VALUE, theFile.getParentFile().getAbsolutePath());
					deObj.setAttribute(DE.A_SOURCE, _miner.setProperties(theFile));
	
					// if it is a file search, we send the remote file back
					// otherwise search within the file and see if there is at least one match
					if (_isFileSearch || internalSearchWithinFile(deObj, absPath, theFile)) {
						deObj.setParent(_status);
						_status.addNestedData(deObj, false);
					}
				}
	
				// do a refresh
				_dataStore.refresh(_status);
				_dataStore.disconnectObjects(_status);
			}
	
			// if the depth is not 0, then we need to recursively search
			if (depth != 0) {
	
				// if it is a directory, or an archive, or a virtual directory, then we need to get the
				// children and search those
				if (isDirectory || ((isArchive || isVirtualDirectory) && _searchString.isIncludeArchives()))
				{			
					File[] children = null;

					// if the file is an archive or a virtual directory, then get the children from
					// the archive handler
					if (isArchive || isVirtualDirectory) {

						AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absPath);
						File archive = new File(avp.getContainingArchiveString());
						String virtualPath = avp.getVirtualPart();

						VirtualChild[] virtualchildren = null;

						try {
							virtualchildren = ArchiveHandlerManager.getInstance().getContents(archive, virtualPath);
						}
						catch (Exception e) {
							UniversalServerUtilities.logError(_miner.getName(), "Error occured trying to get the canonical file", e, _dataStore);				 //$NON-NLS-1$
						}

						if (virtualchildren != null) {

							children = new File[virtualchildren.length];

							for (int i = 0; i < virtualchildren.length; i++) {
								AbsoluteVirtualPath newAvp = new AbsoluteVirtualPath(absPath);
								newAvp.setVirtualPart(virtualchildren[i].fullName);
								children[i] = new File(newAvp.toString());
							}

							if (virtualchildren.length == 0) {
								children = null;
							}
						}
					}
					// otherwise, get the list of children
					else {
						children = theFile.listFiles();
					}

					if (children != null) {

						for (int i = 0; i < children.length && !_isCancelled; i++) {		
							
							checkAndClearupMemory();
							File child = children[i];
							internalSearch(child, depth - 1);
							
						}
					}
				}
			}
		}
	}

	protected boolean internalSearchWithinFile(DataElement remoteFile, String absPath, File theFile) {
		// if search string is empty, no need to look for matches within file
		if (_isFileSearch) {
			return true;
		}

        String[] auditData = new String[] {"READ", theFile.getAbsolutePath(), null, null}; //$NON-NLS-1$
     	UniversalServerUtilities.logAudit(auditData, _dataStore);
		FileInputStream inputStream = null;

		try {
			long MAX_FILE = Runtime.getRuntime().freeMemory() / 4;
			long fileLength = theFile.length();
			
			inputStream = new FileInputStream(theFile);
			InputStreamReader reader = new InputStreamReader(inputStream);		
			BufferedReader bufReader = new BufferedReader(reader);

			// test for unreadable binary
			if (isUnreadableBinary(bufReader) || fileLength > MAX_FILE){
				boolean matched = false;
				try {
					long MAX_READ = MAX_FILE / 10; // read no more than a tenth of max file at a time
					int offset = 0;
					
					while (offset < fileLength && !matched && !_isCancelled){
						long readSize = MAX_READ;
						if (offset +  MAX_READ > fileLength){
							readSize = fileLength - offset;
						}
						matched = simpleSearch(inputStream, offset,readSize, _stringMatcher);
						offset+=readSize;
					}				
				}
				catch (Exception e){					
					return false;
				}
				finally {
					bufReader.close();
					reader.close();
				}
				return matched;
			}
			else 
			{
				SystemSearchStringMatchLocator locator = new SystemSearchStringMatchLocator(bufReader, _stringMatcher);						
				
				SystemSearchLineMatch[] matches = locator.locateMatches();									
				boolean foundMatches = ((matches != null) && (matches.length > 0));

				if (foundMatches) {
					if (matches.length * 500 < MAX_FILE){ // only creating match objects if we have enough memory																			
						convert(remoteFile, absPath, matches);
					}
				}
				return foundMatches;
			} 

		}
		catch (OutOfMemoryError e){
			if (SystemServiceManager.getInstance().getSystemService() == null)
				System.exit(-1);
			return false;
		}
		catch (Exception e) {
			UniversalServerUtilities.logError(_miner.getName(), "Error occured when trying to locate matches", e, _dataStore); //$NON-NLS-1$
			remoteFile.setAttribute(DE.A_VALUE, e.getMessage());
			return false;
		}
	}
	
	private boolean simpleSearch(FileInputStream stream, int offset, long size, SystemSearchStringMatcher matcher)
	{
		byte[] bytes = new byte[(int)size];
		try {
			stream.read(bytes, offset, (int)size);
		}
		catch (Exception e){			
		}
		
		String str = new String(bytes);
		return _stringMatcher.matches(str);
		
	}
	
	private boolean isUnreadableBinary(BufferedReader reader){
		try {
			reader.mark(1);
			reader.read();
			reader.reset();
		}
		catch (Exception e){
			return true;
		}
		
		return false;
	}

	protected boolean doesFilePatternMatch(String compareStr) {
		return _fileNameMatcher.matches(compareStr);
	}

	/**
	 * Returns whether classification matches.
	 * @param absolutePath the absolute path of the file for which we want to check classification.
	 * @return <code>true</code> if the classification matches, <code>false</code> otherwise.
	 */
	protected boolean doesClassificationMatch(String absolutePath) {

		if (_classificationString == null || _classificationString.equals("")) { //$NON-NLS-1$
			return true;
		}
		else {
			String classification = SystemFileClassifier.getInstance().classifyFile(absolutePath);
			return StringCompare.compare(_classificationString, classification, true);
		}
	}

	/**
	 * Converts from system line matches to data elements that will be sent back.
	 * @param deObj the element representing the file for which matches have been found.
	 * @param absPath the absolute path of the file.
	 * @param lineMatches an array of line matches, or empty if no matches.
	 */
	protected void convert(DataElement deObj, String absPath, SystemSearchLineMatch[] lineMatches) {

		SystemSearchLineMatch match = null;
		
		for (int i = 0; i < lineMatches.length; i++) {
			match = lineMatches[i];

			DataElement obj = _dataStore.createObject(deObj, _deGrep, match.getLine(), absPath);
			String sourceString = obj.getSource() + ':'+ match.getLineNumber();
						
			Iterator iter = match.getMatches();
			StringBuffer offsets = new StringBuffer();
			while (iter.hasNext()){
				SystemSearchMatch m = (SystemSearchMatch)iter.next();
				int start = m.getStartOffset();
				int end = m.getEndOffset();
				offsets.append("(" + start + "," + end + ")");				
			}
			obj.setAttribute(DE.A_SOURCE, sourceString + offsets.toString());
		}
		_dataStore.disconnectObjects(deObj);	
		_dataStore.refresh(deObj);
	}
	
	public void checkAndClearupMemory()
	{
		_memoryManager.checkAndClearupMemory();			
	}

	
		
}
