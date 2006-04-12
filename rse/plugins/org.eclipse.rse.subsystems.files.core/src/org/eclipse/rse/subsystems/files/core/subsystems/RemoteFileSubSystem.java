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

package org.eclipse.rse.subsystems.files.core.subsystems;
// copy all the following imports to your child class...
// remote system framework packages...
//import com.ibm.etools.systems.subsystems.impl.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IRemoteContainer;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.SystemChildrenContentsType;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultConfigurationFactory;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.propertypages.SystemSubSystemPropertyPageCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * Specialization for file subsystem factories.
 * It is subclassed via use of a Rose model and MOF/EMF, or better yet 
 *  by subclassing {@link org.eclipse.rse.core.servicesubsystem.impl.FileServiceSubSystem}.
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
	/**
	 * The default value of the '{@link #getHomeFolder() <em>Home Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHomeFolder()
	 * @generated
	 * @ordered
	 */
	protected static final String HOME_FOLDER_EDEFAULT = null;

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
	
	// all created IRemoteFiles mapped in cache to quick retreival
	protected HashMap _cachedRemoteFiles = new HashMap();
	
	protected IProgressMonitor monitor;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String homeFolder = HOME_FOLDER_EDEFAULT;
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
	 * @return true if this subsystem's properties should take precedence over other subsystems that share the same ISystem
	 */
	public boolean isPrimarySubSystem()
	{
		return true;
	}
	
	/**
	 * Return parent subsystem factory, cast to a RemoteFileSubSystemFactory
	 * Assumes {@link #setSubSystemConfiguration(ISubSystemConfiguration)} has already been called.
	 */
	public IRemoteFileSubSystemConfiguration getParentRemoteFileSubSystemFactory()
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

	/**
	 * Long running list processing calls this method to check for a user-cancel event.
	 * If user did cancel, an exception is thrown.
	 * @return true if caller wants to cancel
	 */
	public boolean checkForCancel()
	{
		if ((monitor != null) && monitor.isCanceled())
			throw new OperationCanceledException(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_CANCELLED).getLevelOneText());
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
			FILTERSTRING_LISTROOTS = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory());
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
	 * Shortcut to {@link #getParentRemoteFileSubSystemFactory()}.getSeparator()
	 */
	public String getSeparator()
	{
		return getParentRemoteFileSubSystemFactory().getSeparator();
	}
	/**
	 * Return in character format the character used to separate folders. Eg, "\" or "/"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemFactory()}.getSeparatorChar()
	 */    
	public char getSeparatorChar()
	{
		return getParentRemoteFileSubSystemFactory().getSeparatorChar();
	}
	/**
	 * Return in string format the character used to separate paths. Eg, ";" or ":"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemFactory()}.getPathSeparator()
	 */    
	public String getPathSeparator()
	{
		return getParentRemoteFileSubSystemFactory().getPathSeparator();
	}
	/**
	 * Return in char format the character used to separate paths. Eg, ";" or ":"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemFactory()}.getPathSeparatorChar()
	 */    
	public char getPathSeparatorChar()
	{
		return getParentRemoteFileSubSystemFactory().getPathSeparatorChar();
	}
	/**
	 * Return as a string the line separator.
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemFactory()}.getLineSeparator()
	 */
	public String getLineSeparator()
	{
		return getParentRemoteFileSubSystemFactory().getLineSeparator();
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
		RemoteFileFilterString rffs = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), filterString);
		// ok, this is a tweak: if the absolute name has " -folder" at the end, that means it is a folder...
		if (remoteObjectAbsoluteName.endsWith(" -folder"))
		{
			if (!rffs.getShowSubDirs())
				return false;
			remoteObjectAbsoluteName = remoteObjectAbsoluteName.substring(0, remoteObjectAbsoluteName.indexOf(" -folder"));
		}
		// problem 1: we don't know if the given remote object name represents a file or folder. We have to assume a file,
		//  since we don't support filtering by folder names.
		if (!rffs.getShowFiles())
			return false;

		// step 1: verify the path of the remote object matches the path of the filter string
		String container = rffs.getPath();
		if (container == null)
			return false;
		
		if (container.equals("."))
		{
		    try 
		    {
		    container = getRemoteFileObject(container).getAbsolutePath();
		    }
		    catch (Exception e)
		    {
		        
		    }
		    //return true; 
		}
		
		// trick: use filter string code to parse remote absolute name
		RemoteFileFilterString rmtName = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), remoteObjectAbsoluteName);
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
		if (!nameFilter.equals("*"))
		{
			IMatcher matcher = null;
			if (nameFilter.endsWith(","))
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
		RemoteFileFilterString rffs = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), filterString.getString());
		String container = rffs.getPath();
		if (container == null)
			return false;
		boolean affected = false;
		if (filterString.getParentSystemFilter().isStringsCaseSensitive())
			affected = container.equals(remoteObjectAbsoluteName);
		else
			affected = container.equalsIgnoreCase(remoteObjectAbsoluteName);

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
     * The default implementation of this simply calls {@link #internalResolveFilterString(IProgressMontior, String)}.
     * If the result for each filter string is a SystemMessage (e.g. an error), then the messages are returned.
     * If the result for any filter string is not a message (i.e. an array of children), then the children are returned,
     * and the messages are not. This avoids mixing chuldren as a result of successful resolution of a filter string with
     * messages that might result for other filter strings.
     * So the returned results are always the successful results, or messages (never a mix of the two).
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterStrings(org.eclipse.core.runtime.IProgressMonitor, java.lang.String[])
	 * @param monitor the progress monitor we are running under
	 * @param filterStrings array of filter patterns for objects to return.
	 * @return Array of objects that are the result of resolving all the filter strings
	 */
	public Object[] internalResolveFilterStrings(IProgressMonitor monitor, String[] filterStrings)
		 throws java.lang.reflect.InvocationTargetException,
				java.lang.InterruptedException
	{
		Object[] children = null;
		Vector vChildren = new Vector();
		Vector vMessages = new Vector();
		
		boolean oneSuccess = false;
		boolean success = false;
		if (filterStrings == null)
		{
		    System.out.println("filterStrings==null!");
		    System.out.println("connection == "+getHostAliasName());
		    return null;
		}
		
		for (int idx=0; idx<filterStrings.length; idx++)
		{		     	
			if (monitor != null)
			{
				monitor.setTaskName(getResolvingMessage(filterStrings[idx]));
			}
		   
		   children = internalResolveFilterString(monitor, filterStrings[idx]);
		   
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
			RemoteFileFilterString currFS = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), allFilterStrings[currFilterStringIndex]);
			String currPath = currFS.getPath();
			if (currPath == null)
				currPath = "";
			boolean matchingPaths = false;
			boolean caseSensitive = getParentRemoteFileSubSystemFactory().isCaseSensitive();
			// test if we are listing in the same folder as any previous filter string...
			// ... if we are not, we can save time and skip the redundancy checking..
			for (int idx = 0; idx < currFilterStringIndex; idx++)
			{
				RemoteFileFilterString prevFS = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), allFilterStrings[idx]);
				String prevPath = prevFS.getPath();
				if (prevPath == null)
					prevPath = "";

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
							path1 = "";
						IRemoteFile match = null;
						for (int idx = 0;(match == null) && (idx < allChildrenSoFar.size()); idx++)
						{
							Object prevChild = allChildrenSoFar.elementAt(idx);
							if (prevChild instanceof IRemoteFile)
							{
								String path2 = ((IRemoteFile) prevChild).getAbsolutePath();
								if (path2 == null)
									path2 = "";
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

	/**
	 * Actually resolve an absolute filter string. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by resolveFilterString.
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(IProgressMonitor,String)
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, String filterString) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException
	{
		this.monitor = monitor;
		boolean debugMode = false;
		IRemoteFileSubSystemConfiguration rfssf = getParentRemoteFileSubSystemFactory();
		boolean windows = !rfssf.isUnixStyle();
		if (debugMode)
			SystemBasePlugin.logInfo("INTERNALRESOLVEFILTERSTRING: INPUT FILTERSTRING: " + filterString);
		if (filterString.startsWith("/") && windows) // request to list root files?
		{ // convert to request to list drives on Windows
			int len = filterString.length();
			if (len == 1)
				filterString = "*"; // hmm, should never happen
			else
				filterString = filterString.substring(1);
			if (debugMode)
				SystemBasePlugin.logInfo("...FINAL FILTERSTRING: " + filterString);
		}
		RemoteFileFilterString fs = new RemoteFileFilterString(rfssf, filterString);
		currFilterString = fs;
		if (debugMode)
			SystemBasePlugin.logInfo("...LISTROOTS = " + fs.listRoots());
		if (fs.listRoots())
			return listRoots(new RemoteFileContext(this, null, fs));
		else
		{
			boolean showDirs = fs.getShowSubDirs();
			boolean showFiles = fs.getShowFiles();
			String path = fs.getPath();
			if (windows && (path != null) && !path.endsWith(rfssf.getSeparator()))
				path = path + rfssf.getSeparatorChar();
			String filter = fs.getFileOrTypes();
			if (debugMode)
				SystemBasePlugin.logInfo("...path='" + path + "', filter='" + filter + "', showDirs=" + showDirs + ", showFiles=" + showFiles);
			
			IRemoteFile parent = null;
			try
			{
				parent = getRemoteFileObject(path);
				
				/* DKM - now filters should get invalidated via SystemRegistry event firing so this should not be needed
				 * 
				// DKM: this may be a filter refresh - to be safe I'm marking it stale
				if (parent != null)
					parent.markStale(true);
					*/
				
			}
			catch (SystemMessageException e)
			{
				SystemBasePlugin.logError("RemoteFileSubSystemImpl.logError()", e);
				parent = null;
			}
			
			boolean parentExists = true;
			
			if (parent != null) {
				parentExists = parent.exists();
			}

			Object[] children = null;
			
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
					}
					else if (hasFileContents)
					{
						// already have the files, now add the folders
						listFolders(parent, filter);						
					}
					else if (hasFolderContents)
					{
						// already have the folders, now add the files
						listFiles(parent, filter);				
					}
					else
					{
						// don't have anything - query both
						listFoldersAndFiles(parent, filter);
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
						children = listFolders(parent, filter);
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
						children = listFiles(parent, filter);
					}
				}
			}
			// otherwise return message saying parent could not be found
			else if (parent != null && !parentExists) {
				children = new SystemMessageObject[1];
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_FOLDER_NOTFOUND);
				msg.makeSubstitution(parent.getAbsolutePath());
				children[0] = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR, null);
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
			return "*";
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
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, Object parent, String filterString) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException
	{

		this.monitor = monitor;
		RemoteFileFilterString fs = null;
		//System.out.println("Inside internalResolveFilterString for parent '"+parent+"' for filterstring '" + filterString+"'");
		if (filterString == null) // this will be the case when we want to support merging of filter strings
		{
			// this is all for defect 42095. Phil
			RemoteFileFilterString[] allFilterStrings = ((IRemoteFile) parent).getAllFilterStrings();
			if (allFilterStrings == null)
				fs = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), "*");
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
					children = internalResolveOneFilterString(monitor, parent, fs, onlyOne);
					if (!onlyOne && (children != null))
					{
						addResolvedFilterStringObjects(vChildren, children, allStrings, idx);
					}
				}
				if (!onlyOne)
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
			fs = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), filterString);
		}
		if (fs != null)
		{
			return internalResolveOneFilterString(monitor, parent, fs, true);
		}
		else
			return new Object[] {
		};
	}
	/**
	 * Do one filter string relative resolve
	 */
	protected Object[] internalResolveOneFilterString(IProgressMonitor monitor, Object parent, RemoteFileFilterString fs, boolean sort)
		throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException
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
				children = listFoldersAndFiles((IRemoteFile) parent, filterString);
			else if (showDirs)
				children = listFolders((IRemoteFile) parent, filterString);
			else
				children = listFiles((IRemoteFile) parent, filterString);
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
	public IRemoteFile[] listRoots() throws InterruptedException
	{
		return listRoots(getDefaultContext());
	}


	/**
	 * Return a list of all remote folders in the given parent folder on the remote system
	 * @param parent The parent folder to list folders in
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent)
	{
		return listFolders(parent, null);
	}

	/**
	 * Return a full list of remote folders in the given parent folder on the remote system.
	 * @param parent The parent folder to list folders in
	 * @param fileNameFilter The name pattern for subsetting the file list when this folder is subsequently expanded
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent, String fileNameFilter)
	{
		RemoteFileFilterString filterString = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory());
		filterString.setPath(parent.getAbsolutePath());
		filterString.setFile((fileNameFilter == null) ? "*" : fileNameFilter);
		filterString.setShowFiles(false);
		filterString.setShowSubDirs(true);
		RemoteFileContext context = new RemoteFileContext(this, parent, filterString);
		//return listFolders(parent, fileNameFilter, context);		
		return listFolders(parent, null, context);
	}

	
	/**
	 * Return a list of all remote files in the given parent folder on the remote system
	 * @param parent The parent folder to list files in
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent)
	{
		return listFiles(parent, null);
	}

	/**
	 * Return a list of remote files in the given folder, which match the given name pattern.
	 * @param parent The parent folder to list files in
	 * @param fileNameFilter The name pattern to subset the list by, or null to return all files.
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent, String fileNameFilter)
	{
		RemoteFileFilterString filterString = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory());
		filterString.setPath(parent.getAbsolutePath());
		filterString.setFile((fileNameFilter == null) ? "*" : fileNameFilter);
		filterString.setShowFiles(true);
		filterString.setShowSubDirs(false);
		RemoteFileContext context = new RemoteFileContext(this, parent, filterString);
		return listFiles(parent, fileNameFilter, context);
	}


	/**
	 * Return a list of all remote folders and files in the given folder. The list is not subsetted.
	 * @param parent The parent folder to list folders and files in
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent)
	{
		return listFoldersAndFiles(parent, (String) null);
	}

	/**
	 * Return a list of remote folders and files in the given folder. 
	 * <p>
	 * The files part of the list is subsetted by the given file name filter. It can be null for no subsetting.
	 * 
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent, String fileNameFilter)
	{
		
		
		RemoteFileFilterString filterString = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory());
		filterString.setPath(parent.getAbsolutePath());
		if (fileNameFilter == null)
			fileNameFilter = "*";
		filterString.setFile(fileNameFilter);
		filterString.setShowFiles(true);
		filterString.setShowSubDirs(true);
		RemoteFileContext context = new RemoteFileContext(this, parent, filterString);
		return listFoldersAndFiles(parent, fileNameFilter, context);
	}




	/**
	 * Given a folder or file, return its parent folder name, fully qualified
	 * @param folderOrFile folder or file to return parent of.
	 */
	public String getParentFolderName(IRemoteFile folderOrFile)
	{
		return folderOrFile.getParentPath();
	}


      /**
     * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#getRemoteSearchResultObject(java.lang.String)
     */
    public IRemoteSearchResult getRemoteSearchResultObject(String key) throws SystemMessageException {
        
        int idx = key.indexOf(IHostSearchResult.SEARCH_RESULT_DELIMITER);
        
        if (idx != -1) {
            String remoteFilePath = key.substring(0, idx);
            IRemoteFile remoteFile = getRemoteFileObject(remoteFilePath);
            
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
	 * <b>Overrideable</b> Override this method to provide optimized implementation
	 * Given a set of fully qualified file or folder names, return an ISystemResourceSet object for it.
	 * @param folderOrFileNames Fully qualified folder or file names
	 */
	public SystemRemoteResourceSet getRemoteFileObjects(List folderOrFileNames) throws SystemMessageException
	{
		SystemRemoteResourceSet results = new SystemRemoteResourceSet(this);
		for (int i = 0; i < folderOrFileNames.size(); i++)
		{
			String path = (String)folderOrFileNames.get(i);
			results.addResource(getRemoteFileObject(path));
		}
		return results;
	}
    
	/**
	 * Given a un-qualified file or folder name, and its parent folder object, 
	 *  return an IRemoteFile object for the file.
	 * <b>note</b>This method should be abstract but MOF doesn't allow abstract impl classes at this point
	 * @param parent Folder containing the folder or file
	 * @param folderOrFileName Un-qualified folder or file name
	 */
	public IRemoteFile getRemoteFileObject(IRemoteFile parent, String folderOrFileName) throws SystemMessageException
	{
		// child subclasses must override
		return null;
	}

	/**
	 * Return the object within the subsystem that corresponds to
	 * the specified unique ID.  For remote files, assuming the key 
	 * is the absolute path of a file, this is simply a wrapper to
	 * getRemoteFileObject(). 
	 */
	public Object getObjectWithAbsoluteName(String key) throws Exception
	{
		Object filterRef = getFilterReferenceWithAbsoluteName(key);
		
		if (filterRef != null) {
			return filterRef;
		}

		// look to see if there is a search result delimiter
		// if not, the key must be for a file
		if (key.lastIndexOf(IHostSearchResult.SEARCH_RESULT_DELIMITER) == -1) {
		    
		    IRemoteFile remoteFile = getRemoteFileObject(key);
		
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







	/**
	 * Delete the given remote file or folder. 
	 * <ul>
	 *   <li>If the input is a folder, that folder must be empty for this to succeed.
	 * </ul>
	 * 
	 * <b>note</b>This method should be abstract but MOF doesn't allow abstract impl classes at this point
	 * 
	 * @param folderOrFile represents the object to be deleted.
	 * @return false if the given folder/file didn't exist to begin with, else true. Throws an exception if anything fails.
	 * @deprecated
	 */
	public boolean delete(IRemoteFile folderOrFile) throws RemoteFolderNotEmptyException, RemoteFileSecurityException, RemoteFileIOException
	{
		// child subclasses must override
		return delete(folderOrFile, null);
	}
	



	/**
	 * Move a file or folder to a new target parent folder.
	 * 
	 * @param sourceFolderOrFile The file or folder to move
	 * @param targetFolder The folder to move to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
	 * @param newName The new name for the moved file or folder
	 * @return true iff the move succeeded
	 * @deprecated
	 */
	public boolean move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName) throws RemoteFileSecurityException, RemoteFileIOException
	{
		return move(sourceFolderOrFile, targetFolder, newName, null);
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
		if ((nameFilter != null) && !nameFilter.equals("*"))
		{
			if (nameFilter.endsWith(","))
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
	 * @param nameFilter The pattern to filter the file names by. Can be null to include all files
	 */
	protected void setListValues(int includeFilesOrFolders, String folderNameFilter, String fileNameFilter)
	{
		setListValues(includeFilesOrFolders, fileNameFilter);
		if ((folderNameFilter != null) && !folderNameFilter.equals("*"))
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
		checkForCancel();
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
		 * Copy a file or folder to a new target parent folder.
		 * 
		 * @param sourceFolderOrFile The file or folder to copy
		 * @param targetFolder The folder to copy to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
		 * @param newName The new name for the copied file or folder
		 * @return false true iff the copy succeeded
		 */
//		public boolean copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
//		{
//			return false;
//		}



	/**
		 * Get the remote file and save it locally. The file is saved in the encoding
		 * of the operating system.
		 * @param source remote file that represents the file to be obtained
		 * @param destination the absolute path of the local file
		 * @param monitor the progress monitor
		 */
	public void download(IRemoteFile source, String destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		download(source, destination, System.getProperty("file.encoding"), monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 */
//	public void copy(IRemoteFile source, String destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
//	{
//		return;
//	}

	/**
	 * Get the remote file and save it locally. The file is saved in UTF-8 encoding.
	 * This is a recommended method to use for file transfer
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param monitor the progress monitor
	 */
	public void downloadUTF8(IRemoteFile source, String destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		download(source, destination, SystemEncodingUtil.ENCODING_UTF_8, monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * of the operating system.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, File destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		download(source, destination.getAbsolutePath(), monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, File destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		download(source, destination.getAbsolutePath(), encoding, monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in UTF-8 encoding.
	 * This is a recommended method to use for file transfer
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void downloadUTF8(IRemoteFile source, File destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		downloadUTF8(source, destination.getAbsolutePath(), monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * of the operating system.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, IFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		download(source, destination.getLocation().makeAbsolute().toOSString(), monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, IFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		download(source, destination.getLocation().makeAbsolute().toOSString(), encoding, monitor);
	}

	/**
	 * Get the remote file and save it locally. The file is saved in UTF-8 encoding.
	 * This is a recommended method to use for file transfer
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void downloadUTF8(IRemoteFile source, IFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		downloadUTF8(source, destination.getLocation().makeAbsolute().toOSString(), monitor);
	}

	/**
		 * Put the local copy of the remote file back to the remote location. The file
		 * is assumed to be in the encoding of the local operating system
		 * @param source the absolute path of the local copy
		 * @param destination remote file that represents the file on the server
		 * @param monitor the progress monitor
		 */
	public void upload(String source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source, destination, System.getProperty("file.encoding"), monitor);
		destination.markStale(true);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding specified
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 */
	
//	public void copy(String source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
//	{
//		return;
//	}

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(String source, String destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source, SystemEncodingUtil.ENCODING_UTF_8, destination, System.getProperty("file.encoding"), monitor);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The local file
	 * must be in UTF-8 encoding.
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void uploadUTF8(String source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source, destination, SystemEncodingUtil.ENCODING_UTF_8, monitor);
		destination.markStale(true);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(File source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source.getAbsolutePath(), destination, monitor);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding specified
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 */
	public void upload(File source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source.getAbsolutePath(), destination, encoding, monitor);
		destination.markStale(true);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The local file
	 * must be in UTF-8 encoding.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void uploadUTF8(File source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		uploadUTF8(source.getAbsolutePath(), destination, monitor);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(IFile source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source.getLocation().makeAbsolute().toOSString(), destination, monitor);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding specified
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 */
	public void upload(IFile source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		upload(source.getLocation().makeAbsolute().toOSString(), destination, encoding, monitor);
	}

	/**
	 * Put the local copy of the remote file back to the remote location. The local file
	 * must be in UTF-8 encoding.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void uploadUTF8(IFile source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException
	{
		uploadUTF8(source.getLocation().makeAbsolute().toOSString(), destination, monitor);
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
				SystemBasePlugin.logInfo("Running local command: " + cmd);
			process = Runtime.getRuntime().exec(cmd);
		}
		catch (IOException exc)
		{
			if (debug)
			{
				String msg = exc.getMessage();
				if (msg == null)
					msg = exc.getClass().getName();
				SystemBasePlugin.logInfo("...Unexpected error running command '" + cmd + "'. Error msg: " + msg);
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
					SystemBasePlugin.logInfo("...System.err: " + line);
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
					SystemBasePlugin.logInfo("...System out: " + line);
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

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getHomeFolder()
	{
		return homeFolder;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setHomeFolder(String newHomeFolder)
	{
		homeFolder = newHomeFolder;
	}

	public void initializeSubSystem(IProgressMonitor monitor)
	{
		getConnectorService().addCommunicationsListener(this);
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
			if (oldFile != null && oldFile.getParentRemoteFile() != null)
			{
				oldFile.getParentRemoteFile().replaceContent(oldFile, file);
			}
			
			// preserve persistent information from old file to new
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
	     path = path.replaceAll("//", "/");	    
	     if (path.endsWith("\\") || (path.endsWith("/") && path.length() > 1))
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
		if (file instanceof IRemoteContainer)
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
		}
		_cachedRemoteFiles.remove(file.getAbsolutePath());
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
				//SystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();	
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
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toString()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (homeFolder: ");
		result.append(homeFolder);
		result.append(')');
		return result.toString();
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
			SystemBasePlugin.logError("Error occured trying to get local host address", e);
			addr = null;
		}
		
		// if the address is the loopback address 
		if (addr != null && addr.isLoopbackAddress()) {
			return null;
		}
		
		return addr;
	}
	
	
    public Object getTargetForFilter(ISystemFilterReference filterRef)
    {
        String firstFilterString = ((ISystemFilterReference)filterRef).getReferencedFilter().getFilterStrings()[0];	
        RemoteFileFilterString fs = new RemoteFileFilterString(getParentRemoteFileSubSystemFactory(), firstFilterString);				    
	    try
	    {
	        // change target to be referenced remote folder
	       return getRemoteFileObject(fs.getPath());
	    }
	    catch (Exception e)
	    {	        
	    }
	    return null;
    }
    
 

	/**
	 * @deprecated
	 */
	public void cancelSearch(IHostSearchResultConfiguration searchConfig)
	{
		// TODO Auto-generated method stub
		
	}
}