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

package org.eclipse.rse.dstore.universal.miners.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.SystemFileClassifier;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.search.SystemSearchFileNameMatcher;
import org.eclipse.rse.services.clientserver.search.SystemSearchLineMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatchLocator;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatcher;

public class UniversalSearchHandler extends Thread implements ICancellableHandler
{
	protected HashSet _alreadySearched;

	private File _rootFile;

	protected boolean _isCancelled;
	protected boolean _isDone;
	protected int _depth = -1;
	
	protected DataStore _dataStore;
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

	public UniversalSearchHandler(DataStore dataStore, UniversalFileSystemMiner miner, SystemSearchString searchString, boolean fsCaseSensitive, File theFile, DataElement status) {
		_dataStore = dataStore;
		_miner = miner;
		_searchString = searchString;
		_fsCaseSensitive = fsCaseSensitive;
		_alreadySearched = new HashSet();
		
		_deGrep = _dataStore.findObjectDescriptor("grep");
		_deFile = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
		_deFolder = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
		_deArchiveFile = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
		_deVirtualFile = _dataStore.findObjectDescriptor(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		boolean includeSubfolders = searchString.isIncludeSubfolders();
		
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
		
		_fileNameMatcher = new SystemSearchFileNameMatcher(_searchString.getFileNamesString(), fsCaseSensitive, _searchString.isFileNamesRegex());
		
		// classification of files to restrict the search to
		_classificationString = _searchString.getClassificationString();
	}

	public void run() {
		
		try {
			internalSearch(_rootFile, _depth);
		}
		catch (Exception e) {
			UniversalServerUtilities.logError(_miner.getName(), "Error occured when calling internal search", e);
		}
		
		_isDone = true;
		
		if (_isCancelled) {
			_miner.statusCancelled(_status);
		}
		else {
			// NOTE: do not call miner statusDone() method since we want to
			// update the status immediately.
			// Otherwise we don't get an event on the client corresponding
			// to status refresh. As a result client thinks
			// search isn't finished.
			// _miner.statusDone(_status); 
	        _status.setAttribute(DE.A_NAME, "done");
	        _dataStore.refresh(_status, true);	// true indicates refresh immediately
		}
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

	protected boolean hasSearchedDirectory(File file) {
		return _alreadySearched.contains(file);
	}

	protected void internalSearch(File theFile, int depth) {
		
		// is it a directory?
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
					if (vc.path.equals("")) {
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
						results = vc.getHandler().search(vc.fullName, _stringMatcher);
					
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
			_dataStore.refresh(_status, true);
		}

		// if the depth is not 0, then we need to recursively search
		if (depth != 0) {
			
			// if it is a directory, or an archive, or a virtual directory, then we need to get the
			// children and search those
			if (isDirectory || ((isArchive || isVirtualDirectory) && _searchString.isIncludeArchives()))
			{
				
				if (!hasSearchedDirectory(theFile)) {
					
					_alreadySearched.add(theFile);

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
						catch (IOException e) {
							UniversalServerUtilities.logError(_miner.getName(), "Error occured trying to get the canonical file", e);				
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
		
		FileInputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(theFile);
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader bufReader = new BufferedReader(reader);

			SystemSearchStringMatchLocator locator = new SystemSearchStringMatchLocator(bufReader, _stringMatcher);
		
			SystemSearchLineMatch[] matches = locator.locateMatches();
			
			boolean foundMatches = ((matches != null) && (matches.length > 0));
			
			if (foundMatches) {
				convert(remoteFile, absPath, matches);
			}
			
			return foundMatches;
		}
		catch (Exception e) {
			UniversalServerUtilities.logError(_miner.getName(), "Error occured when trying to locate matches", e);
			remoteFile.setAttribute(DE.A_VALUE, e.getMessage());
			return false;
		}
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
		
		if (_classificationString == null || _classificationString.equals("")) {
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
			obj.setAttribute(DE.A_SOURCE, obj.getSource() + ':'+ match.getLineNumber());
		}
	}
}