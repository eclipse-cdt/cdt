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
 * Michael Berger (IBM) - Bug 147791 - symbolic links can cause circular search.
 ********************************************************************************/

package org.eclipse.rse.services.local.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.local.search.LocalSearchResult;
import org.eclipse.rse.services.clientserver.StringCompare;
import org.eclipse.rse.services.clientserver.SystemFileClassifier;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.search.SystemSearchFileNameMatcher;
import org.eclipse.rse.services.clientserver.search.SystemSearchLineMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatchLocator;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatcher;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.local.files.LocalHostFile;
import org.eclipse.rse.services.local.files.LocalVirtualHostFile;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.ISearchHandler;

/**
 * Class used to collect local search results.
 */
public class LocalSearchHandler implements ISearchHandler
{

	protected HashSet _alreadySearched;

	protected IHostSearchResultConfiguration _searchConfig;

	protected File _theFile;

	protected int _depth = -1;

	protected IFileService _fs;

	protected boolean _isDone;

	protected boolean _isCancelled;

	protected LocalHostFile _theRmtFile;

	protected SystemSearchString _searchString;

	protected SystemSearchStringMatcher _stringMatcher;

	protected boolean _isFileSearch;

	protected SystemSearchFileNameMatcher _fileNameMatcher;

	protected String _classificationString;

	/**
	 * Constructor for local search handler.
	 * 
	 * @param shell
	 *            a shell.
	 * @param searchConfig
	 *            a search configuration.
	 */
	public LocalSearchHandler(IHostSearchResultConfiguration searchConfig, IFileService fileService)
	{

		_searchConfig = searchConfig;

		_theRmtFile = (LocalHostFile) (searchConfig.getSearchTarget());

		// if the remote file is not virtual, then simply get the file
		if (!(_theRmtFile instanceof LocalVirtualHostFile))
		{
			_theFile = (File) (_theRmtFile.getFile());
		}
		// for virtual file, fake it with a non-existent file representing the
		// actual path (note this file won't actually exist on the filesystem)
		else
		{
			String absPath = _theRmtFile.getAbsolutePath();
			AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absPath);
			_theFile = new File(avp.toString());
		}

		_fs = fileService;

		_searchString = searchConfig.getSearchString();
		

		boolean includeSubfolders = _searchString.isIncludeSubfolders();

		if (includeSubfolders)
		{
			_depth = -1;
		}
		else
		{
			_depth = 1;
		}

		_isCancelled = false;
		_isDone = false;
		_alreadySearched = new HashSet();

		_stringMatcher = new SystemSearchStringMatcher(_searchString.getTextString(), _searchString.isCaseSensitive(),
				_searchString.isTextStringRegex());

		// if the search string is empty or if it is an asterisk, then we are
		// doing a file search
		// i.e. we do not want to look inside files
		_isFileSearch = _stringMatcher.isSearchStringEmpty() || _stringMatcher.isSearchStringAsterisk();

		boolean fsCaseSensitive = _fs.isCaseSensitive();
		_fileNameMatcher = new SystemSearchFileNameMatcher(_searchString.getFileNamesString(), fsCaseSensitive,
				_searchString.isFileNamesRegex());

		// classification of files to restrict the search to
		_classificationString = _searchString.getClassificationString();
	}

	public void search(IProgressMonitor monitor)
	{

		// start search
		// pass in the context of the target file
		internalSearch(_theFile, _depth, _theRmtFile);

		_isDone = true;

		if (!_isCancelled)
		{
			_searchConfig.setStatus(IHostSearchConstants.FINISHED);
		}
		else
		{
			_searchConfig.setStatus(IHostSearchConstants.CANCELLED);
		}
	}

	public boolean isDone()
	{
		return _isDone;
	}

	public void cancel(IProgressMonitor monitor)
	{
		_isCancelled = true;
	}

	private boolean hasSearchedDirectory(File file)
	{
		try
		{
			return _alreadySearched.contains(file.getCanonicalFile());
		}
		catch (IOException e)
		{
			return _alreadySearched.contains(file);
		}
	}

	private boolean internalSearch(File theFile, int depth, IHostFile context)
	{

		boolean foundFile = false;

		// is it a directory? (note that for virtual the file won't exist, so it
		// will
		// return false, which is what we want!)
		boolean isDirectory = theFile.isDirectory();

		// is it an archive?
		boolean isArchive = ArchiveHandlerManager.getInstance().isArchive(theFile) && _searchString.isIncludeArchives();

		String absPath = theFile.getAbsolutePath();
		String compareStr = theFile.getName();

		// is it a virtual file?
		boolean isVirtual = ArchiveHandlerManager.isVirtual(absPath);

		// is it a virtual directory?
		boolean isVirtualDirectory = false;

		VirtualChild vc = null;

		// if it is a virtual object, then get a reference to it
		if (isVirtual)
		{
			vc = ArchiveHandlerManager.getInstance().getVirtualObject(absPath);
			isVirtualDirectory = isVirtual && vc.isDirectory;
		}

		// base case for the recursive method call
		// if the file is not a directory, an archive or a virtual directory,
		// and we get a match with the file name, then we can search for match
		// within the file
		if (!isDirectory && !isArchive && !isVirtualDirectory && doesFilePatternMatch(compareStr)
				&& doesClassificationMatch(absPath))
		{

			LocalHostFile file;

			// if the file is a virtual file, then get matches from the archive
			// handler
			if (isVirtual)
			{
				file = new LocalVirtualHostFile(vc);

				SystemSearchLineMatch[] matches = null;

				// if it is not a file search, then call the handler search
				// method
				if (!_isFileSearch)
				{
					matches = vc.getHandler().search(vc.fullName, _stringMatcher);
					IHostSearchResult[] results = convert(file, matches);

					/** TODO - how to store search results related to files
					// if there is at least one match, then add the file to the
					// search results
					if (results != null && results.length > 0)
					{
						file.setContents(IHostSearchResultsContentsType.getInstance(), _searchString.toString(),
								results);
						_searchConfig.addResult(file);
					}
					
					**/
					_searchConfig.addResults(file, results);
				}
				// otherwise add the file to the search results
				else
				{
					_searchConfig.addResult(file);
				}
			}
			// otherwise, search the file
			else
			{

				// note that the file can not be root
				file = new LocalHostFile(theFile, false);

				/* TODO
				if (!isArchive)
				{
					file.setIsContainer(false);
				}
				else
				{
					file.setIsContainer(true);
				}
				*/

				// if file search, add the file to the results
				// otherwise, search within the file and if there is at least
				// one text match, then add it
				if (_isFileSearch || internalSearchWithinFile(file, theFile))
				{
					_searchConfig.addResult(file);
				}
				else
				{
					_searchConfig.addResult(file);
				}
			}

			// indicate that we have found a file
			foundFile = true;

			if (foundFile)
			{
				// TODO refresh(_isDone);
				foundFile = false;
			}
		}

		// if the depth is not 0, then we need to recursively search
		if (depth != 0)
		{

			// if it is a directory, or an archive, or a virtual directory, then
			// we need to get the
			// children and search those
			if (isDirectory || (_searchString.isIncludeArchives() && (isArchive || isVirtualDirectory)))
			{

				if (!hasSearchedDirectory(theFile))
				{

					try
					{
						_alreadySearched.add(theFile.getCanonicalFile());
					}
					catch (IOException e)
					{
						_alreadySearched.add(theFile);
					}

					File[] children = null;

					// if the file is an archive or a virtual directory, then
					// get the children from
					// the archive handler
					if (isArchive || isVirtualDirectory)
					{

						AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absPath);
						File archive = new File(avp.getContainingArchiveString());
						String virtualPath = avp.getVirtualPart();

						VirtualChild[] virtualchildren = null;

						try
						{
							virtualchildren = ArchiveHandlerManager.getInstance().getContents(archive, virtualPath);
						}
						catch (IOException e)
						{
							//SystemPlugin.logError("An erorr occured trying to retrieve virtual file " + virtualPath
							//		+ " for " + archive.getAbsolutePath(), e);
						}

						if (virtualchildren != null)
						{
							children = new File[virtualchildren.length];

							for (int i = 0; i < virtualchildren.length; i++)
							{
								AbsoluteVirtualPath newAvp = new AbsoluteVirtualPath(absPath);
								newAvp.setVirtualPart(virtualchildren[i].fullName);
								children[i] = new File(newAvp.toString());
							}

							if (virtualchildren.length == 0)
							{
								children = null;
							}
						}
					}
					else
					{
						children = theFile.listFiles();
					}

					if (children != null)
					{

						LocalHostFile fileImpl = null;

						// create local file for archive or directory which is
						// not virtual
						if (!isVirtualDirectory)
						{

							// if the context's parent file (which is
							// essentially the parent file of
							// the given file) is null, then it means the given
							// file is root
							boolean isRoot = false; //TODO

							fileImpl = new LocalHostFile(theFile, isRoot);
						}
						// create local file differently for virtual directory
						else
						{
							fileImpl = new LocalVirtualHostFile(vc);
						}

						for (int i = 0; i < children.length && !_isCancelled; i++)
						{

							File child = children[i];

							if (internalSearch(child, depth - 1, fileImpl))
							{
								foundFile = true;
							}
						}
					}
				}
			}
		}

		return foundFile;
	}


	private boolean internalSearchWithinFile(IHostFile remoteFile, File theFile)
	{

		// if file search, no need to look for matches within file
		if (_isFileSearch)
		{
			return true;
		}

		FileInputStream inputStream = null;

		try
		{
			inputStream = new FileInputStream(theFile);
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader bufReader = new BufferedReader(reader);

			SystemSearchStringMatchLocator locator = new SystemSearchStringMatchLocator(bufReader, _stringMatcher);

			SystemSearchLineMatch[] matches = locator.locateMatches();

			if (matches == null || matches.length == 0)
			{
				return false;
			}

			IHostSearchResult[] results = convert(remoteFile, matches);
			_searchConfig.addResults(remoteFile, results);

			// TODO - how to store results related to files
			//remoteFile.setContents(IHostSearchResultsContentsType.getInstance(), _searchString.toString(), results);

			return true;
		}
		catch (IOException e)
		{
			//SystemPlugin.logError("Error occured when trying to locate matches", e);
			return false;
		}
	}

	protected boolean doesFilePatternMatch(String compareStr)
	{
		return _fileNameMatcher.matches(compareStr);
	}

	/**
	 * Returns whether classification matches.
	 * 
	 * @param absolutePath
	 *            the absolute path of the file for which we want to check
	 *            classification.
	 * @return <code>true</code> if the classification matches,
	 *         <code>false</code> otherwise.
	 */
	protected boolean doesClassificationMatch(String absolutePath)
	{

		if (_classificationString == null || _classificationString.equals(""))
		{
			return true;
		}
		else
		{
			String classification = SystemFileClassifier.getInstance().classifyFile(absolutePath);
			return StringCompare.compare(_classificationString, classification, true);
		}
	}

	/**
	 * Converts from system line matches to remote search results that will show
	 * up in the remote search view.
	 * 
	 * @param remoteFile
	 *            the remote file for which line matches have been found.
	 * @param lineMatches
	 *            an array of line matches, or empty if no matches.
	 * @return an array of remote search results, or empty if no matches.
	 */
	private IHostSearchResult[] convert(IHostFile remoteFile, SystemSearchLineMatch[] lineMatches)
	{

		LocalSearchResult[] results = new LocalSearchResult[lineMatches.length];

		for (int i = 0; i < lineMatches.length; i++)
		{
			results[i] = new LocalSearchResult(_searchConfig, remoteFile, _searchString);
			results[i].setText(lineMatches[i].getLine());
			results[i].setLine(lineMatches[i].getLineNumber());
			results[i].setIndex(i);

			Iterator iter = lineMatches[i].getMatches();

			// add matches within the line
			while (iter.hasNext())
			{
				SystemSearchMatch match = (SystemSearchMatch) iter.next();
				int startOffset = match.getStartOffset();
				int endOffset = match.getEndOffset();
				results[i].addMatch(startOffset, endOffset);
			}
		}

		return results;
	}
}