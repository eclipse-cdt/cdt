/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Fix 162962 - recursive removeCachedRemoteFile()
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [196664] prevent unnecessary query on the parent
 * Rupen Mardirossian (IBM)  	 - [204307] listFolders now deals with a null parameter for fileNameFilter preventing NPE
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [210109] store constants in IFileService rather than IFileServiceConstants
 * David McKnight   (IBM)        - [211472] [api][breaking] IRemoteObjectResolver.getObjectWithAbsoluteName() needs a progress monitor
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * Martin Oberhuber (Wind River) - [226574][api] Add ISubSystemConfiguration#supportsEncoding()
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;
// copy all the following imports to your child class...
// remote system framework packages...

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemChildrenContentsType;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IRemoteContainer;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.internal.subsystems.files.core.Activator;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFileMessageIds;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.propertypages.SystemSubSystemPropertyPageCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * Specialization for file subsystem factories.
 * It is subclassed via use of a Rose model and MOF/EMF, or better yet
 *  by subclassing {@link FileServiceSubSystem}.
 * <p>
 * For your convenience, there is built-in name filtering support. To use it,
 * call:
 * <ul>
 *   <li>{@link #setListValues(int, String)} or {@link #setListValues(int, String, String)} to set the filter criteria
 *   <li>{@link #accept(String, boolean)} to test a given name for a match
 * </ul>
 *
 * <p>This class returns instances of {@link RemoteFile} objects.
 */

public abstract class RemoteFileSubSystem extends SubSystem implements IRemoteFileSubSystem, ICommunicationsListener
{
	public boolean osVarsSet, osWindows, osWindows95, osWindowsNT;
	public String osName, osCmdShell;
	// variables to affect the list method for subsetting folder contents
	private int includeFilesOrFolders = IClientServerConstants.INCLUDE_ALL;
	//private NamePatternMatcher matcher = null;
	protected IMatcher matcher = null;
	protected NamePatternMatcher folderNameMatcher = null;
	protected RemoteFileFilterString currFilterString = null;
	protected RemoteFileFilterString FILTERSTRING_LISTROOTS = null;
	protected RemoteFileContext DEFAULT_CONTEXT = null;
	protected RemoteFileContext DEFAULT_CONTEXT_NOFILTERSTRING = null;

	protected ArrayList _searchHistory;

	// all created IRemoteFiles mapped in cache to quick retrieval
	protected HashMap _cachedRemoteFiles = new HashMap();

	/**
	 * Default constructor. Do not call directly! Rather, use the mof generated factory method to create.
	 * After instantiation, be sure to call {@link #setSubSystemConfiguration(ISubSystemConfiguration)}.
	 */
	public RemoteFileSubSystem(IHost host, IConnectorService connectorService)
	{
		super(host, connectorService);
		_searchHistory = new ArrayList();
	}
	/**
	 * @return true if this subsystem's properties should take precedence
	 *  over other subsystems that share the same {@link IConnectorService}
	 */
	public boolean isPrimarySubSystem()
	{
		return true;
	}

	/**
	 * Return parent subsystem factory, cast to a RemoteFileSubSystemConfiguration
	 * Assumes {@link #setSubSystemConfiguration(ISubSystemConfiguration)} has already been called.
	 */
	public IRemoteFileSubSystemConfiguration getParentRemoteFileSubSystemConfiguration()
	{
		return (IRemoteFileSubSystemConfiguration) super.getSubSystemConfiguration();
	}

	/**
	 * Return true if file names are case-sensitive. Used when doing name or type filtering
	 * Default is false, but can be overridden.
	 */
	public boolean isCaseSensitive()
	{
		return false;
	}


	protected RemoteFileFilterString getCurrentFilterString()
	{
		RemoteFileFilterString crffs = currFilterString;
		currFilterString = null;
		return crffs;
	}

	protected RemoteFileFilterString getFilterStringListRoots()
	{
		if (FILTERSTRING_LISTROOTS == null)
			FILTERSTRING_LISTROOTS = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration());
		return FILTERSTRING_LISTROOTS;
	}
	protected RemoteFileContext getDefaultContext()
	{
		if (DEFAULT_CONTEXT == null)
			DEFAULT_CONTEXT = new RemoteFileContext(this, null, getFilterStringListRoots());
		return DEFAULT_CONTEXT;
	}
	protected RemoteFileContext getDefaultContextNoFilterString()
	{
		if (DEFAULT_CONTEXT_NOFILTERSTRING == null)
			DEFAULT_CONTEXT_NOFILTERSTRING = new RemoteFileContext(this, null, null);
		return DEFAULT_CONTEXT_NOFILTERSTRING;
	}
	protected RemoteFileContext getContext(IRemoteFile parent)
	{
		return new RemoteFileContext(this, parent, null);
	}
	protected RemoteFileContext getContext(IRemoteFile parent, RemoteFileFilterString rffs)
	{
		return new RemoteFileContext(this, parent, rffs);
	}

	// --------------------------------
	// FILE SYSTEM ATTRIBUTE METHODS...
	// --------------------------------
	/**
	 * Return in string format the character used to separate folders. Eg, "\" or "/".
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getSeparator()
	 */
	public String getSeparator()
	{
		return getParentRemoteFileSubSystemConfiguration().getSeparator();
	}
	/**
	 * Return in character format the character used to separate folders. Eg, "\" or "/"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getSeparatorChar()
	 */
	public char getSeparatorChar()
	{
		return getParentRemoteFileSubSystemConfiguration().getSeparatorChar();
	}
	/**
	 * Return in string format the character used to separate paths. Eg, ";" or ":"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getPathSeparator()
	 */
	public String getPathSeparator()
	{
		return getParentRemoteFileSubSystemConfiguration().getPathSeparator();
	}
	/**
	 * Return in char format the character used to separate paths. Eg, ";" or ":"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getPathSeparatorChar()
	 */
	public char getPathSeparatorChar()
	{
		return getParentRemoteFileSubSystemConfiguration().getPathSeparatorChar();
	}
	/**
	 * Return as a string the line separator.
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getLineSeparator()
	 */
	public String getLineSeparator()
	{
		return getParentRemoteFileSubSystemConfiguration().getLineSeparator();
	}


	// -------------------------------------
	// GUI methods
	// -------------------------------------
	/**
	 * Return the single property page to show in the tabbed notebook for the
	 *  for SubSystem property of the parent Connection. Return null if no
	 *  page is to be contributed for this. You are limited to a single page,
	 *  so you may have to compress. It is recommended you prompt for the port
	 *  if applicable since the common base subsystem property page is not shown
	 *  To help with this you can use the SubSystemPortPrompt widget.
	 */
	public PropertyPage getPropertyPage(Composite parent)
	{
		return new SystemSubSystemPropertyPageCore();
	}

	// -------------------------
	// Filter Testing Methods...
	// -------------------------

	/**
	 * @see org.eclipse.rse.core.subsystems.SubSystem#doesFilterMatch(org.eclipse.rse.core.filters.ISystemFilter, java.lang.String)
	 */
	public boolean doesFilterMatch(ISystemFilter filter, String remoteObjectAbsoluteName) {

    	if (filter.isPromptable() || !doesFilterTypeMatch(filter, remoteObjectAbsoluteName)) {
    		return false;
    	}

    	boolean would = false;

      	String[] strings = filter.getFilterStrings();

      	if (strings != null) {

      		for (int idx = 0; !would && (idx < strings.length); idx++) {

      			// for "Drives" filter on Windows, only return match if the absolute path is a drive letter
      			if (strings[idx].equals("*")) { //$NON-NLS-1$
      				IPath path = new Path(remoteObjectAbsoluteName);

      				if (path.segmentCount() == 0) {
      					return true;
      				}
      				else {
      					return false;
      				}
      			}
      			//else if (strings[idx].equals("./*")) {
      			//	would = true;
      			//}
      			else {
      				would = doesFilterStringMatch(strings[idx], remoteObjectAbsoluteName, filter.areStringsCaseSensitive());
      			}
      		}
      	}

      	return would;
	}

	/**
	 * Return true if the given remote object name will pass the filtering criteria for
	 *  the given filter string.
	 * <p>
	 * Subclasses need to override this.
	 * If in doubt, return true.
	 * <p>
	 * There is a hack here if you want to tell us the absolute name is that of a folder: append " -folder" to the name!
	 */
	public boolean doesFilterStringMatch(String filterString, String remoteObjectAbsoluteName, boolean caseSensitive)
	{
		RemoteFileFilterString rffs = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), filterString);
		// ok, this is a tweak: if the absolute name has " -folder" at the end, that means it is a folder...
		if (remoteObjectAbsoluteName.endsWith(" -folder")) //$NON-NLS-1$
		{
			if (!rffs.getShowSubDirs())
				return false;
			remoteObjectAbsoluteName = remoteObjectAbsoluteName.substring(0, remoteObjectAbsoluteName.indexOf(" -folder")); //$NON-NLS-1$
		}
		// problem 1: we don't know if the given remote object name represents a file or folder. We have to assume a file,
		//  since we don't support filtering by folder names.
		if (!rffs.getShowFiles())
			return false;

		// step 1: verify the path of the remote object matches the path of the filter string
		String container = rffs.getPath();
		if (container == null)
			return false;

	if (container.equals(".") && !isOffline()) //$NON-NLS-1$
		{
		    try
		    {
		    container = getRemoteFileObject(container, new NullProgressMonitor()).getAbsolutePath();
		    }
		    catch (Exception e)
		    {

		    }
		    //return true;
		}

		// DKM - if the filter and the remote object are the same
		if (container.equals(remoteObjectAbsoluteName))
			return true;

		// trick: use filter string code to parse remote absolute name
		RemoteFileFilterString rmtName = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), remoteObjectAbsoluteName);
		boolean pathMatch = false;
		if (caseSensitive)
			pathMatch = container.equals(rmtName.getPath());
		else
			pathMatch = container.equalsIgnoreCase(rmtName.getPath());
		if (!pathMatch)
			return false;

		// step 2: test if the given file name matches the filter criteria
		String nameFilter = rffs.getFileOrTypes();
		if (nameFilter == null)
			return false;
		if (!nameFilter.equals("*")) //$NON-NLS-1$
		{
			IMatcher matcher = null;
			if (nameFilter.endsWith(",")) //$NON-NLS-1$
				matcher = new FileTypeMatcher(FileTypeMatcher.parseTypes(nameFilter), true);
			else
				matcher = new NamePatternMatcher(nameFilter, true, caseSensitive);
			return matcher.matches(rmtName.getFile());
		}
		else
			return true;
	}

	/**
	 * Return true if the given filter string lists the contents of the given remote object.
	 *  For example, if given a folder, return true if the given filter string
	 *  lists the contents of that folder. Used in impact analysis when a remote object is
	 *  created, deleted, renamed, copied or moved, so as to establish which filters need to be
	 *  refreshed or collapsed (if the folder is deleted, say).
	 * <p>
	 * This should only return true if the filter string directly lists the contents of the given
	 *  object, versus indirectly.
	 * <p>
	 * Subclasses should override this.
	 */
	public boolean doesFilterStringListContentsOf(ISystemFilterString filterString, String remoteObjectAbsoluteName)
	{
		RemoteFileFilterString rffs = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), filterString.getString());
		String container = rffs.getPath();

		if (container == null)
			return false;
		boolean affected = false;

		String remoteObjectContainer = remoteObjectAbsoluteName;
		int lastSep = remoteObjectAbsoluteName.lastIndexOf(getSeparator());
		if (lastSep != -1)
		{
			remoteObjectContainer = remoteObjectAbsoluteName.substring(0, lastSep);
		}
		if (filterString.getParentSystemFilter().isStringsCaseSensitive())
			affected = container.equals(remoteObjectContainer);
		else
			affected = container.equalsIgnoreCase(remoteObjectContainer);

		//UniversalSystemPlugin.logDebugMessage(
		//    "UniversalFileSubSystemImpl::doesFilterStringListContentsOf",
		//    "Univ Filter String Testing '" + container + "' versus '" + remoteObjectAbsoluteName + "' => " + affected);
		return affected;
	}



	// -------------------------------
	// SubSystem METHODS ...
	// -------------------------------

	/**
	 * Resolves filter strings.
     * The default implementation of this simply calls {@link #internalResolveFilterString(String, IProgressMonitor)}.
     * If the result for each filter string is a SystemMessage (e.g. an error), then the messages are returned.
     * If the result for any filter string is not a message (i.e. an array of children), then the children are returned,
     * and the messages are not. This avoids mixing chuldren as a result of successful resolution of a filter string with
     * messages that might result for other filter strings.
     * So the returned results are always the successful results, or messages (never a mix of the two).
	 * @param filterStrings array of filter patterns for objects to return.
	 * @param monitor the progress monitor we are running under
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterStrings(java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 * @return Array of objects that are the result of resolving all the filter strings
	 */
	public Object[] internalResolveFilterStrings(String[] filterStrings, IProgressMonitor monitor)
		 throws java.lang.reflect.InvocationTargetException,
				java.lang.InterruptedException
	{

		if (!isConnected()) {
			return null;
		}

		Object[] children = null;
		Vector vChildren = new Vector();
		Vector vMessages = new Vector();

		boolean oneSuccess = false;
		boolean success = false;
		if (filterStrings == null)
		{
		    System.out.println("filterStrings==null!"); //$NON-NLS-1$
		    System.out.println("connection == "+getHostAliasName()); //$NON-NLS-1$
		    return null;
		}

		// TODO - change this to use listMulti to be more efficient
		for (int idx=0; idx<filterStrings.length; idx++)
		{
			if (monitor != null)
			{
				monitor.setTaskName(getResolvingMessage(filterStrings[idx]));
			}

		   children = internalResolveFilterString(filterStrings[idx], monitor);

		   if (!(children != null && children.length == 1 && children[0] instanceof SystemMessageObject)) {
		   		success = true;

		   		// one has been successful
		   		oneSuccess = true;
		   }
		   else {
		   		success = false;
		   }

		   // if successful, then add to list
		   if (children != null && success) {
		   	addResolvedFilterStringObjects(vChildren, children, filterStrings, idx);
		   }
		   // otherwise add to messages list
		   else if (children != null){
		   	super.addResolvedFilterStringObjects(vMessages, children, filterStrings, idx);
		   }
		}

		if (oneSuccess) {
			int nbrChildren = vChildren.size();
			children = new Object[nbrChildren];

			for (int idx=0; idx<nbrChildren; idx++)
		   		children[idx] = vChildren.elementAt(idx);
		}
		else {
			int nbrMessages = vMessages.size();
			children = new Object[nbrMessages];

			for (int idx=0; idx<nbrMessages; idx++)
				children[idx] = vMessages.elementAt(idx);
		}

		return children;
	}

	/**
	 * Overridable parent extension point for adding the results of a filter string
	 *  to the overall list of results.
	 * <p>
	 * Can be used to filter out redundant entries in the concatenated list, if this
	 *  is desired.
	 */
	protected void addResolvedFilterStringObjects(Vector allChildrenSoFar, Object[] childrenForThisFilterString, String[] allFilterStrings, int currFilterStringIndex)
	{
		if (currFilterStringIndex == 0)
			super.addResolvedFilterStringObjects(allChildrenSoFar, childrenForThisFilterString, allFilterStrings, currFilterStringIndex);
		else
		{
			// for defect 42095, we filter out redundancies...
			RemoteFileFilterString currFS = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), allFilterStrings[currFilterStringIndex]);
			String currPath = currFS.getPath();
			if (currPath == null)
				currPath = ""; //$NON-NLS-1$
			boolean matchingPaths = false;
			boolean caseSensitive = getParentRemoteFileSubSystemConfiguration().isCaseSensitive();
			// test if we are listing in the same folder as any previous filter string...
			// ... if we are not, we can save time and skip the redundancy checking..
			for (int idx = 0; idx < currFilterStringIndex; idx++)
			{
				RemoteFileFilterString prevFS = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), allFilterStrings[idx]);
				String prevPath = prevFS.getPath();
				if (prevPath == null)
					prevPath = ""; //$NON-NLS-1$

				if ((caseSensitive && prevPath.equals(currPath)) || (!caseSensitive && prevPath.equalsIgnoreCase(currPath)))
				{
					matchingPaths = true;
				}
			}

			// ok, what through the current resolved list, and if appropriate strip out
			// any entries that are already in the list. That is, any entries with the same
			// absolute path name...
			// ... for performance reasons, we don't want to waste time checking items in our
			// our own list, so we do this in two passes, first marking redundant entries by
			// nulling them out...
			if (matchingPaths)
			{
				for (int jdx = 0; jdx < childrenForThisFilterString.length; jdx++)
				{
					if (childrenForThisFilterString[jdx] instanceof IRemoteFile)
					{
						IRemoteFile child = (IRemoteFile) childrenForThisFilterString[jdx];
						String path1 = child.getAbsolutePath();
						if (path1 == null)
							path1 = ""; //$NON-NLS-1$
						IRemoteFile match = null;
						for (int idx = 0;(match == null) && (idx < allChildrenSoFar.size()); idx++)
						{
							Object prevChild = allChildrenSoFar.elementAt(idx);
							if (prevChild instanceof IRemoteFile)
							{
								String path2 = ((IRemoteFile) prevChild).getAbsolutePath();
								if (path2 == null)
									path2 = ""; //$NON-NLS-1$
								if (path1.equals(path2))
									match = (IRemoteFile) prevChild;
							}
						}
						if (match != null)
						{
							childrenForThisFilterString[jdx] = null; // mark as redundant
							// if this redundant entry is a folder, that implies there are multiple
							// filter strings that would have resulted in seeing this folder. We need
							// to record all such filter strings in the folder's context, so that when
							// subsequently expanded, all applicable filter strings will be used to get
							// the children of this subdirectory...
							if (match.isDirectory() && (match instanceof RemoteFile))
							{
								IRemoteFileContext context = ((RemoteFile) match).getContext();
								if (context != null)
								{
									context.addFilterString(currFS);
								}
							}
						}
					}
				} // end for loop
			}
			// second pass...
			for (int jdx = 0; jdx < childrenForThisFilterString.length; jdx++)
				if (childrenForThisFilterString[jdx] != null)
					allChildrenSoFar.addElement(childrenForThisFilterString[jdx]);
		}
	}


	private String fixFilterString(IRemoteFileSubSystemConfiguration rfssf, String filterString)
	{
		boolean windows = !rfssf.isUnixStyle();

		if (filterString.startsWith("/") && windows) // request to list root files? //$NON-NLS-1$
		{ // convert to request to list drives on Windows
			int len = filterString.length();
			if (len == 1)
				filterString = "*"; // hmm, should never happen //$NON-NLS-1$
			else
				filterString = filterString.substring(1);
		}

		return filterString;
	}

	/**
	 * Actually resolve an absolute filter string. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by resolveFilterString.
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(String,IProgressMonitor)
	 */
	protected Object[] internalResolveFilterString(String filterString, IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException
	{
		IRemoteFileSubSystemConfiguration rfssf = getParentRemoteFileSubSystemConfiguration();
		filterString = fixFilterString(rfssf, filterString);

		RemoteFileFilterString fs = new RemoteFileFilterString(rfssf, filterString);
		currFilterString = fs;

		if (fs.listRoots())
			return listRoots(new RemoteFileContext(this, null, fs), monitor);
		else
		{
			boolean showDirs = fs.getShowSubDirs();
			boolean showFiles = fs.getShowFiles();
			String path = fs.getPath();
			boolean windows = !rfssf.isUnixStyle();

			if (windows && (path != null) && !path.endsWith(rfssf.getSeparator()))
				path = path + rfssf.getSeparatorChar();

			String filter = fs.getFileOrTypes();
			IRemoteFile parent = null;
			try
			{
				parent = getRemoteFileObject(path, monitor);
			}
			catch (SystemMessageException e)
			{
				SystemBasePlugin.logError("RemoteFileSubSystemImpl.logError()", e); //$NON-NLS-1$
			}

			boolean parentExists = true;

			if (parent != null) {
				parentExists = parent.exists();
			}

			Object[] children = null;
			try
			{
				// if parent exists, get its children according to the filter
				if (parent != null && parentExists)
				{
					boolean hasFileContents = !parent.isStale() && parent.hasContents(RemoteFileChildrenContentsType.getInstance(), filter);
					boolean hasFolderContents = !parent.isStale() && parent.hasContents(RemoteFolderChildrenContentsType.getInstance(), filter);
					boolean hasFileAndFolderContents = !parent.isStale() && parent.hasContents(RemoteChildrenContentsType.getInstance(), filter);
					if (showDirs && showFiles)
					{
	 					if (hasFileAndFolderContents)
						{
	 						// has everything
						}
						else if (hasFileContents)
						{
							// already have the files, now add the folders
							list(parent, filter, IFileService.FILE_TYPE_FOLDERS, monitor);
						}
						else if (hasFolderContents)
						{
							// already have the folders, now add the files
							list(parent, filter, IFileService.FILE_TYPE_FILES, monitor);
						}
						else
						{
							// don't have anything - query both
							list(parent, filter, IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
						}
						children = parent.getContents(RemoteChildrenContentsType.getInstance(), filter);
					}
					else if (showDirs)
					{
						if (hasFolderContents)
						{
							children = parent.getContents(RemoteFolderChildrenContentsType.getInstance(), filter);
						}
						else
						{
							children = list(parent, filter, IFileService.FILE_TYPE_FOLDERS, monitor);
						}
					}
					else
					{
						if (hasFileContents)
						{
							children = parent.getContents(RemoteFileChildrenContentsType.getInstance(), filter);
						}
						else
						{
							children = list(parent, filter, IFileService.FILE_TYPE_FILES, monitor);
						}
					}
				}
				// otherwise return message saying parent could not be found
				else if (parent != null && !parentExists) {
					children = new SystemMessageObject[1];
					String msgTxt = NLS.bind(SystemFileResources.FILEMSG_FILE_NOTFOUND, parent.getAbsolutePath());
					SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ISystemFileMessageIds.FILEMSG_FILE_NOTFOUND,
							IStatus.ERROR, msgTxt);
					children[0] = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR, null);
				}
			}
			catch (SystemMessageException e)
			{
				children = new SystemMessageObject[1];
				children[0] = new SystemMessageObject(e.getSystemMessage(), ISystemMessageObject.MSGTYPE_ERROR, null);
			}

			return children;
		}
		//return null;
	}
	/**
	 * Sort the concatenated list of all objects returned by resolving one or more
	 *  filter strings.
	 * The default implementation does nothing. Child classes can override if they wish
	 *  to show their resulting objects sorted.
	 */
	public Object[] sortResolvedFilterStringObjects(Object[] children)
	{
		if ((children != null) && (children.length > 1))
			Arrays.sort(children);
		return children;
	}

	/**
	 * Called by parent when we defer getting a filter string until later, where we query it from
	 *  the parent. In this case we need the first filter string for the progress monitor msg.
	 */
	protected String getFirstParentFilterString(Object parent)
	{
		RemoteFileFilterString[] allFilterStrings = ((IRemoteFile) parent).getAllFilterStrings();
		if ((allFilterStrings == null) || (allFilterStrings.length == 0))
			return "*"; //$NON-NLS-1$
		else
		{
			return allFilterStrings[0].getFileOrTypes();
		}
	}
	/**
	 * Actually resolve a relative filter string. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by resolveFilterString.
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT FILTERS!
	 */
	protected Object[] internalResolveFilterString(Object parent, String filterString, IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException
	{
		RemoteFileFilterString fs = null;
		try
		{
		//System.out.println("Inside internalResolveFilterString for parent '"+parent+"' for filterstring '" + filterString+"'");
		if (filterString == null) // this will be the case when we want to support merging of filter strings
		{
			// this is all for defect 42095. Phil
			RemoteFileFilterString[] allFilterStrings = ((IRemoteFile) parent).getAllFilterStrings();
			if (allFilterStrings == null)
				fs = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), "*"); //$NON-NLS-1$
			else
			{
				boolean onlyOne = (allFilterStrings.length == 1);
				Object[] children = null;
				Vector vChildren = null;
				if (!onlyOne)
					vChildren = new Vector();
				String[] allStrings = new String[allFilterStrings.length];
				for (int idx = 0; idx < allFilterStrings.length; idx++)
					allStrings[idx] = allFilterStrings[idx].toString();
				for (int idx = 0; idx < allFilterStrings.length; idx++)
				{
					fs = (RemoteFileFilterString) allFilterStrings[idx].clone();
					fs.setPath(null);
					children = internalResolveOneFilterString(parent, fs, onlyOne, monitor);
					if (!onlyOne && (children != null))
					{
						addResolvedFilterStringObjects(vChildren, children, allStrings, idx);
					}
				}
				if (!onlyOne && vChildren != null)
				{
					children = new Object[vChildren.size()];
					for (int cdx = 0; cdx < children.length; cdx++)
						children[cdx] = vChildren.elementAt(cdx);
					Arrays.sort(children);
				}
				else
				{
					/*
					System.out.println("Returning " + children.length + " children " );
					System.out.println();
					for (int idx=0;idx<children.length;idx++)
					 System.out.print(children[idx] + "; ");
					System.out.println();
					*/
				}
				return children;
			}
		}
		else
		{
			fs = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), filterString);
		}
		return internalResolveOneFilterString(parent, fs, true, monitor);
		}

		catch (SystemMessageException e)
		{
				SystemMessageObject[] children = new SystemMessageObject[1];
				children[0] = new SystemMessageObject(e.getSystemMessage(), ISystemMessageObject.MSGTYPE_ERROR, null);
				return children;
		}
	}
	/**
	 * Do one filter string relative resolve
	 */
	protected Object[] internalResolveOneFilterString(Object parent, RemoteFileFilterString fs, boolean sort, IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		currFilterString = fs;
		String filterString = fs.toStringNoSwitches();
		boolean showDirs = fs.getShowSubDirs();
		boolean showFiles = fs.getShowFiles();
		//String path = fs.getPath();
		//String filter = fs.getFile();
		//System.out.println("...path='"+path+"', filter='"+filter+"', showDirs="+showDirs+", showFiles="+showFiles);
		//System.out.println("...toStringNoSwitches='"+filterString+"'");
		Object[] children = null;
		if (parent != null)
		{
			if (showDirs && showFiles)
				//children = listFoldersAndFiles((IRemoteFile)parent, filterString);
				children = list((IRemoteFile) parent, filterString, IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
			else if (showDirs)
				children = list((IRemoteFile) parent, filterString, IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
			else
				children = list((IRemoteFile) parent, filterString, IFileService.FILE_TYPE_FILES, monitor);
			if (sort && (children != null) && (children.length > 1))
				Arrays.sort(children);
		}
		return children;
	}
	// ---------------------------------------
	// RemoteFileSubSystem WRAPPER METHODS ...
	//  ... we set the filterstring objects
	// ---------------------------------------

	// -------------------------------
	// RemoteFileSubSystem METHODS ...
	// -------------------------------
	/**
	 * Return a list of roots/drives on the remote system
	 */
	public IRemoteFile[] listRoots(IProgressMonitor monitor) throws InterruptedException
	{
		return listRoots(getDefaultContext(), monitor);
	}


		/**
	 * Return a list of all remote folders and files in the given folder. The
	 * list is not subsetted.
	 *
	 * @param parents The parent folders to list folders and files in
	 * @param fileTypes - indicates whether to query files, folders, both or
	 *            some other type
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, int[] fileTypes, IProgressMonitor monitor) throws SystemMessageException
	{
		String[] fileNameFilters = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			fileNameFilters[i] = "*"; // default filter //$NON-NLS-1$
		}

		return listMultiple(parents, fileNameFilters, fileTypes, monitor);
	}

	/**
	 * Return a list of all remote folders and files in the given folder. The
	 * list is not subsetted.
	 *
	 * @param parents The parent folders to list folders and files in
	 * @param fileType - indicates whether to query files, folders, both or some
	 *            other type
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		String[] fileNameFilters = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			fileNameFilters[i] = "*"; // default filter //$NON-NLS-1$
		}

		return listMultiple(parents, fileNameFilters, fileType, monitor);
	}

	/**
	 * Return a list of all remote folders and files in the given folder. The
	 * list is not subsetted.
	 *
	 * @param parent The parent folder to list folders and files in
	 * @param monitor the progress monitor
	 * @since 3.0 renamed from listFoldersAndFiles()
	 */
	public IRemoteFile[] list(IRemoteFile parent, IProgressMonitor monitor) throws SystemMessageException
	{
		return list(parent, IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
	}

	/**
	 * Return a list of all remote folders and files in the given folder. The
	 * list is not subsetted.
	 *
	 * @param parent The parent folder to list folders and files in
	 * @param fileType the type of file
	 * @param monitor the monitor
	 * @since 3.0 using int fileType parameter
	 */
	public IRemoteFile[] list(IRemoteFile parent, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		return list(parent, (String) null, fileType, monitor);
	}

	/**
	 * Return a list of remote folders and files in the given folder.
	 * <p>
	 * The files part of the list is subsetted by the given file name filter. It
	 * can be null for no subsetting.
	 *
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or
	 *            null to return all files.
	 * @param fileType the type of file
	 * @param monitor the monitor
	 * @since 3.0 using int fileType parameter
	 */
	public IRemoteFile[] list(IRemoteFile parent, String fileNameFilter, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		String path = parent.getAbsolutePath();
		fileNameFilter = (fileNameFilter == null) ? "*" : fileNameFilter; //$NON-NLS-1$
		IRemoteFileSubSystemConfiguration config = getParentRemoteFileSubSystemConfiguration();
		RemoteFileFilterString filterString = new RemoteFileFilterString(config, path, fileNameFilter);
		filterString.setShowFiles(true);
		filterString.setShowSubDirs(true);
		RemoteFileContext context = new RemoteFileContext(this, parent, filterString);
		return list(parent, fileNameFilter, context, fileType, monitor);
	}



	/**
	 * Given a folder or file, return its parent folder name, fully qualified
	 * @param folderOrFile folder or file to return parent of.
	 */
	public String getParentFolderName(IRemoteFile folderOrFile)
	{
		return folderOrFile.getParentPath();
	}

    /*
     * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#getRemoteSearchResultObject(java.lang.String)
     */
    public IRemoteSearchResult getRemoteSearchResultObject(String key) throws SystemMessageException {

        int idx = key.indexOf(IHostSearchResult.SEARCH_RESULT_DELIMITER);

        if (idx != -1) {
            String remoteFilePath = key.substring(0, idx);
            IRemoteFile remoteFile = getRemoteFileObject(remoteFilePath, new NullProgressMonitor());

            if (remoteFile != null) {

                int jdx = idx + IHostSearchResult.SEARCH_RESULT_DELIMITER.length() + IHostSearchResult.SEARCH_RESULT_OPEN_DELIMITER.length();

                int kdx = key.indexOf(IHostSearchResult.SEARCH_RESULT_INDEX_DELIMITER, jdx);

                String searchString = key.substring(jdx, kdx);

                Object[] children = remoteFile.getContents(RemoteSearchResultsContentsType.getInstance(), searchString);

                if (children != null) {

                    int ldx = key.indexOf(IHostSearchResult.SEARCH_RESULT_CLOSE_DELIMITER, kdx+1);

                    int index = Integer.valueOf(key.substring(kdx+1, ldx)).intValue();

                    if (children.length > index) {
                        IRemoteSearchResult result = (IRemoteSearchResult)(children[index]);
                        return result;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

	/**
	 * Given a set of fully qualified file or folder names, return an
	 * ISystemResourceSet object for it. <b>Overrideable</b> Override this
	 * method to provide optimized implementation
	 *
	 * @param folderOrFileNames Fully qualified folder or file names
	 * @since 3.0
	 */
	public IRemoteFile[] getRemoteFileObjects(String[] folderOrFileNames, IProgressMonitor monitor) throws SystemMessageException
	{
		IRemoteFile[] results = new IRemoteFile[folderOrFileNames.length];
		for (int i = 0; i < folderOrFileNames.length; i++)
		{
			String path = folderOrFileNames[i];
			results[i] = getRemoteFileObject(path, monitor);
		}
		return results;
	}



	/**
	 * Return the object within the subsystem that corresponds to the specified
	 * unique ID.
	 *
	 * For remote files, assuming the key is the absolute path of a file, this
	 * is simply a wrapper to getRemoteFileObject().
	 *
	 * @see SubSystem#getObjectWithAbsoluteName(String, IProgressMonitor)
	 */
	public Object getObjectWithAbsoluteName(String key, IProgressMonitor monitor) throws Exception
	{
		Object filterRef = super.getObjectWithAbsoluteName(key, monitor);
		if (filterRef != null) {
			return filterRef;
		}

		// look to see if there is a search result delimiter
		// if not, the key must be for a file
		if (key.lastIndexOf(IHostSearchResult.SEARCH_RESULT_DELIMITER) < 0) {

		    IRemoteFile remoteFile = getRemoteFileObject(key, monitor);

		    if (remoteFile != null) {
		        return remoteFile;
		    }
		    else {
		        return null;
		    }
		}
		// otherwise, it's a search result
		else {
		    return getRemoteSearchResultObject(key);
		}
	}

















	// -----------------------------------------------------------------
	// CONVENIENCE METHODS FOR CHILD CLASSES FOR DOING NAME FILTERING...
	// -----------------------------------------------------------------
	/**
	 * Method to set variables to affect the folder content subsetting.
	 * Use this when only listing <i>either</i> files <i>or</i> folders, but not both.
	 * @param includeFilesOrFolders A constant from {@link org.eclipse.rse.core.subsystems.IFileConstants}
	 * @param nameFilter The pattern to filter the file or folder names by. Can be null to include all.
	 */
	protected void setListValues(int includeFilesOrFolders, String nameFilter)
	{
		this.includeFilesOrFolders = includeFilesOrFolders;
		if ((nameFilter != null) && !nameFilter.equals("*")) //$NON-NLS-1$
		{
			if (nameFilter.endsWith(",")) //$NON-NLS-1$
				matcher = new FileTypeMatcher(FileTypeMatcher.parseTypes(nameFilter), isCaseSensitive());
			else
				matcher = new NamePatternMatcher(nameFilter, true, isCaseSensitive());
		}
		else
			matcher = null;
		folderNameMatcher = null;
	}
	/**
	 * Overloaded method to set variables to affect the folder content subsetting,
	 * when there is separate filters for both folder names and filter names.
	 * @param includeFilesOrFolders A constant from {@link org.eclipse.rse.core.subsystems.IFileConstants}
	 * @param folderNameFilter The pattern to filter the folder names by. Can be null to include all folders
	 * @param fileNameFilter The pattern to filter the file names by. Can be null to include all files
	 */
	protected void setListValues(int includeFilesOrFolders, String folderNameFilter, String fileNameFilter)
	{
		setListValues(includeFilesOrFolders, fileNameFilter);
		if ((folderNameFilter != null) && !folderNameFilter.equals("*")) //$NON-NLS-1$
			folderNameMatcher = new NamePatternMatcher(folderNameFilter, true, isCaseSensitive());
	}

	/**
	 * Method to decide if a given folder or file name matches the present criteria.
	 * You must have previously called {@link #setListValues(int, String)} or {@link #setListValues(int, String, String)}
	 * @param name The file or folder name to test
	 * @param isFile true if this is a file name, false if it is a folder name.
	 * @return true if the name matches the previously specified criteria
	 */
	protected boolean accept(String name, boolean isFile)
	{
		boolean match = true;
		if (includeFilesOrFolders == IClientServerConstants.INCLUDE_FILES_ONLY)
		{
			if (!isFile)
				return false;
		}
		else if (includeFilesOrFolders == IClientServerConstants.INCLUDE_FOLDERS_ONLY)
		{
			if (isFile)
				return false;
		}
		if ((matcher == null) && (folderNameMatcher == null))
			return true;
		if (includeFilesOrFolders != IClientServerConstants.INCLUDE_ALL)
			match = matcher.matches(name);
		else
		{
			if (isFile)
			{
				if (matcher != null)
					match = matcher.matches(name);
			}
			else
			{
				if (folderNameMatcher != null)
					match = folderNameMatcher.matches(name);
			}
		}
		return match;
	}

	/**
	  * helper method to run an external command
	 */
	public static int runLocalCommand(String cmd, Vector lines) throws Exception
	{

		boolean debug = true;
		int rc = -99;
		Process process;
		try
		{
			if (debug)
				SystemBasePlugin.logInfo("Running local command: " + cmd); //$NON-NLS-1$
			process = Runtime.getRuntime().exec(cmd);
		}
		catch (IOException exc)
		{
			if (debug)
			{
				String msg = exc.getMessage();
				if (msg == null)
					msg = exc.getClass().getName();
				SystemBasePlugin.logInfo("...Unexpected error running command '" + cmd + "'. Error msg: " + msg); //$NON-NLS-1$ //$NON-NLS-2$
			}
			throw exc;
		}
		String line;
		DataInputStream err = new DataInputStream(process.getErrorStream()); // capture standard err
		BufferedReader berr = new BufferedReader(new InputStreamReader(err));
		try
		{
			while ((line = berr.readLine()) != null)
			{
				if (debug)
					SystemBasePlugin.logInfo("...System.err: " + line); //$NON-NLS-1$
				if ((lines != null) && (line.trim().length() > 0))
					lines.addElement(line);
			}
		}
		catch (IOException exc)
		{
		}
		DataInputStream in = new DataInputStream(process.getInputStream()); // capture standard out
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		try
		{
			while ((line = bin.readLine()) != null)
			{
				if (debug)
					SystemBasePlugin.logInfo("...System out: " + line); //$NON-NLS-1$
				if ((lines != null) && (line.trim().length() > 0))
					lines.addElement(line);
			}
		}
		catch (IOException exc)
		{
		}
		try
		{
			rc = process.waitFor();
		}
		catch (InterruptedException exc)
		{
		}
		return rc;
	} // end runCmd method

	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException
	{
		super.initializeSubSystem(monitor);
		// load UI plugin for adapters right after successful connect
		Platform.getAdapterManager().loadAdapter(new RemoteFileEmpty(), "org.eclipse.rse.ui.view.ISystemViewElementAdapter"); //$NON-NLS-1$
		getConnectorService().addCommunicationsListener(this);
	}

	public void uninitializeSubSystem(IProgressMonitor monitor)
	{
		getConnectorService().removeCommunicationsListener(this);
		super.uninitializeSubSystem(monitor);
	}

	/**
	 * Store the IRemoteFile in a hashmap to quick subsequent retrieval
	 * @param file the file
	 */
	public void cacheRemoteFile(IRemoteFile file, String path)
	{

		if (_cachedRemoteFiles.containsKey(path))
		{
			IRemoteFile oldFile = (IRemoteFile)_cachedRemoteFiles.remove(path);
			if (oldFile == file)
			{
				// already cached - recache
				_cachedRemoteFiles.put(path, file);
				return;
			}

			// replace file under parent
			if (oldFile instanceof RemoteFile) {
				RemoteFile roldFile = (RemoteFile)oldFile;
				if (roldFile._parentFile != null) // prevent parent query from bug #196664
				{
					roldFile._parentFile.replaceContent(oldFile, file);
				}
			}
			else if (oldFile != null && oldFile.getParentRemoteFile() != null) {
				oldFile.getParentRemoteFile().replaceContent(oldFile, file);
			}

			// preserve persistent information from old file to new
			if (oldFile != null)
				oldFile.copyContentsTo(file);

		}
		_cachedRemoteFiles.put(path, file);
	}

	/**
	 * Store the IRemoteFile in a hashmap to quick subsequent retrieval
	 * @param file the file
	 */
	public void cacheRemoteFile(IRemoteFile file)
	{
		if (file.exists())
		{
			cacheRemoteFile(file, file.getAbsolutePath());
		}
	}
	/**
	 * Returns the cached remote file with the specified path.  If no such file
	 * is found, returns null
	 * @param path
	 * @return the cached file, if found in the cache, else null
	 */
	public IRemoteFile getCachedRemoteFile(String path)
	{
		if (_cachedRemoteFiles.size() > 0)
		{
	     path = path.replaceAll("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	     if (path.endsWith("\\") || (path.endsWith("/") && path.length() > 1)) //$NON-NLS-1$ //$NON-NLS-2$
	     {
	         path = path.substring(0, path.length() - 1);
	     }
		  if (_cachedRemoteFiles.containsKey(path))
		  {
		      {return (IRemoteFile)_cachedRemoteFiles.get(path);}
		  }

		}
		return null;
	}

	protected void removeCachedRemoteFile(IRemoteFile file)
	{
		if (file != null)
		{
			Object[] children = ((IRemoteContainer)file).getContents(SystemChildrenContentsType.getInstance());
			if (children != null)
			{
				for (int i = 0; i < children.length; i++)
				{
					if (children[i] instanceof IRemoteFile)
					{
						removeCachedRemoteFile((IRemoteFile)children[i]);
					}
				}
			}
			//Workaround for bug 162962: getContents() incomplete, children not deleted
			//If getContents() is implemented correctly, no matches should be removed
			String prefix = file.getAbsolutePath() + file.getSeparator();
			//Clone the hashMap in order to avoid ConcurrentModificationException in the iterator
			HashMap tmpMap = (HashMap)_cachedRemoteFiles.clone();
			Iterator it = tmpMap.keySet().iterator();
			while (it.hasNext()) {
				String remotePath = (String)it.next();
				if (remotePath.startsWith(prefix)) {
					//FIXME this should never be called if getContents() is implemented correctly
					//such that children are already removed in the code above.
					removeCachedRemoteFile(remotePath);
				}
			}

			_cachedRemoteFiles.remove(file.getAbsolutePath());
		}
	}

	protected void removeCachedRemoteFile(String path)
	{
		_cachedRemoteFiles.remove(path);
	}


	public void communicationsStateChange(CommunicationsEvent e)
	{
		switch (e.getState())
		{
			case CommunicationsEvent.AFTER_DISCONNECT :
				_cachedRemoteFiles.clear();
				// DKM - taking this out because it causes an exception when the event occurs in Modal Context
				//ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
				//sr.connectedStatusChange(this, false, true, true);
				getConnectorService().removeCommunicationsListener(this);

				break;

			case CommunicationsEvent.BEFORE_DISCONNECT :
			case CommunicationsEvent.CONNECTION_ERROR :
				break;
			default :
				break;
		}
	}

	/**
	 * @see ICommunicationsListener#isPassiveCommunicationsListener()
	 */
	public boolean isPassiveCommunicationsListener()
	{
		return true;
	}

	/**
	 * Returns -1 by default. Subclasses should override if necessary.
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#getUnusedPort()
	 */
	public int getUnusedPort()
	{
		return -1;
	}

	/**
	 * Returns the address found by calling <code>InetAddress.getLocalHost()</code>. If that
	 * call returns the local loopback address, it returns <code>null</code>.
	 * Subclasses should reimplement to handle cases where systems have multiple IP addresses due
	 * to multiple network cards or VPN. This method should return an address
	 * that is usable from the remote system to connect back to the local system.
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#getLocalAddress()
	 */
	public InetAddress getLocalAddress() {

		InetAddress addr = null;

		try {
			addr = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			SystemBasePlugin.logError("Error occured trying to get local host address", e); //$NON-NLS-1$
		}

		// if the address is the loopback address
		if (addr != null && addr.isLoopbackAddress()) {
			return null;
		}

		return addr;
	}


    public Object getTargetForFilter(ISystemFilterReference filterRef)
    {
        String firstFilterString = filterRef.getReferencedFilter().getFilterStrings()[0];
        RemoteFileFilterString fs = new RemoteFileFilterString(getParentRemoteFileSubSystemConfiguration(), firstFilterString);
	    try
	    {
	        // change target to be referenced remote folder
	       return getRemoteFileObject(fs.getPath(), new NullProgressMonitor());
	    }
	    catch (Exception e)
	    {
	    }
	    return null;
    }

	/**
	 * {@inheritDoc}
	 * @deprecated Do not call this method directly since it is not implemented.
	 */
	public void cancelSearch(IHostSearchResultConfiguration searchConfig)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the local platform encoding if the default encoding of the host was not set.
	 * Subclasses should override to return the actual remote encoding.
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#getRemoteEncoding()
	 */
	public String getRemoteEncoding() {
		IHost host = getHost();

		// get the encoding from the host that was not by the remote system
		String encoding = host.getDefaultEncoding(false);

		// get the encoding from the host that was set by querying a remote system
		// this allows us to pick up the host encoding that may have been set by another subsystem
		if (encoding == null) {
			encoding = host.getDefaultEncoding(true);
		}

		if (encoding != null) {
			return encoding;
		}
		else {
			return SystemEncodingUtil.getInstance().getLocalDefaultEncoding();
		}
	}
}
