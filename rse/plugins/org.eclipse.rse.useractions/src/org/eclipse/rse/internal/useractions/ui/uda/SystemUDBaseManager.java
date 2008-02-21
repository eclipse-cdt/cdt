/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement ISystemUDAConstants
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceHelpers;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Instances of this class hold the UDA definitions unique to
 * <ol>
 *   <li> The SystemProfile  - according to the subsystem
 *   <li> the SubSystem type - according to the SubSystemFactory
 * </ol>
 * Instances of this class will be linked to a SubSystem instance
 * Eventually, would hope to create a factory method for this class which will
 *  return existing instances common to the subsystems of different connections
 *  within the same profile.
 * <p>
 * When used for IMPORT:  NO SubSystem  instance available.  Will be NULL!
 * <p>
 * This is the base class for both action managers and types managers.
 * Only action managers are actually scoped by profile.
 * <p>
 * Architecturally, this class and the SystemXMLElementWrapper class 
 *  encapsulate all knowledge of the fact the underlying store is a xml document.
 */
public abstract class SystemUDBaseManager implements ErrorHandler, IResourceChangeListener, ISystemXMLElementWrapperFactory, ITreeContentProvider {
	// state
	protected SystemUDActionSubsystem _udas;
	protected IFolder importCaseFolder; // Only set during Import processing
	private SystemUDTreeViewNewItem[] newItemsPerDomain;
	private boolean ignoreMyResourceChange = false; // avoid recursion
	// profile-indexed state. 
	// Access to these is carefully guarded, for the case when
	//  a subclass does not support profiles.		
	private Hashtable udocsByProfile;
	private Hashtable hasChangedByProfile;
	private Hashtable dirPathByProfile;
	// used by subclasses that are not profile-indexed
	private Document udocNoProfile;
	private boolean hasChangedNoProfile = false;
	private Object[] dirPathNoProfile;
	// Profile for which we are working for actions for...
	private ISystemProfile currentlyActiveProfile; // set in UDA GUI
	// Clipboard copy/paste support
	private Element currentNodeClone = null;
	private String currentNodeCloneID = ""; //$NON-NLS-1$
	//	private String  currentNodeCloneName = ""; //$NON-NLS-1$
	private int currentNodeCloneDomain = -1;
	// constants
	protected static final Object[] EMPTY_ARRAY = new Object[0];
	/**
	 * Current release as a string. Eg "5.0"
	 */
	private static final String CURRENT_RELEASE_NAME = RSECorePlugin.CURRENT_RELEASE_NAME; //"5.1.2"; // Historical from when part of iSeries.

	/**
	 * Constructor
	 */
	public SystemUDBaseManager(SystemUDActionSubsystem udas) {
		super();
		_udas = udas;
		if (supportsProfiles()) {
			udocsByProfile = new Hashtable();
			hasChangedByProfile = new Hashtable();
			dirPathByProfile = new Hashtable();
		}
		addListener();
	}

	/** 
	 * Return the action subsystem object
	 */
	public SystemUDActionSubsystem getActionSubSystem() {
		return _udas;
	}

	/**
	 * Get the icon to show in the tree views, for the "new" expandable item
	 */
	public Image getNewImage() {
		return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_NEW_ID);
	}

	// -----------------------------------------------------------	
	// ISystemXMLElementWrapperFactory
	// -----------------------------------------------------------	
	/**
	 * Return the tag name for our managed elements.
	 * Eg: will be "Action" for user actions, and "Type" for file types.
	 */
	public abstract String getTagName();

	/**
	 * Given an xml element node, create an instance of the appropriate
	 * subclass of SystemXMLElementWrapper to represent it.
	 */
	public abstract SystemXMLElementWrapper createElementWrapper(Element xmlElementToWrap, ISystemProfile profile, int domain);

	// -----------------------------------------------------------	
	// ITREECONTENTPROVIDER METHODS...
	// -----------------------------------------------------------
	/**
	 * Returns the implementation of IWorkbenchAdapter for the given
	 * object.  Returns null if the adapter is not defined or the
	 * object is not adaptable.
	 */
	protected IWorkbenchAdapter getAdapter(Object o) {
		if (!(o instanceof IAdaptable)) return null;
		return (IWorkbenchAdapter) ((IAdaptable) o).getAdapter(IWorkbenchAdapter.class);
	}

	public void dispose() {
		// To be safe, we clear the parent profile name when the tree is disposed,
		//  so it doesn't linger. This will happen because we only instantiate ourselves
		//  once and then always re-use that instance.
		setCurrentProfile(null);
	}

	/**
	 * Method declared on ITreeContentProvider.
	 * We return null.
	 */
	public Object getParent(Object element) {
		return null;
	}

	/**
	 * Method declared on ITreeContentProvider.
	 */
	public boolean hasChildren(Object element) {
		//return getChildren(element).length > 0;
		if (element instanceof SystemUDTreeViewNewItem) {
			if (getActionSubSystem().supportsDomains())
				return !((SystemUDTreeViewNewItem) element).isExecutable();
			else
				return false;
		} else if (element instanceof SystemXMLElementWrapper) {
			if (getActionSubSystem().supportsDomains()) {
				SystemXMLElementWrapper wrapper = (SystemXMLElementWrapper) element;
				if (!wrapper.isDomain())
					return false;
				else
					return (wrapper.getElement().getFirstChild() != null);
			} else
				return false;
		} else if (element == null) {
			SystemBasePlugin.logError("Unexpected null input to hasChildren!"); //$NON-NLS-1$
		} else {
			String message = "Unexpected input to hasChildren: " + element.getClass().getName(); //$NON-NLS-1$
			SystemBasePlugin.logError(message);
		}
		return false;
	}

	/**
	 * Method declared on IContentProvider.
	 * Callen when input changed. We do nothing.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	// -----------------------------------------------------------	
	// THE FOLLOWING ABSTRACT OUT THE DIFFERENCES BETWEEN ACTIONS
	//  AND TYPES
	// -----------------------------------------------------------
	/**
	 * Get the document root tag name. 
	 * Will be "FileTypes" for types, and "Actions" for actions
	 */
	public abstract String getDocumentRootTagName();

	/**
	 * Do we uppercase the value of the "Name" attribute?
	 * Yes, we do for types, and No, we don't for actions
	 */
	protected abstract boolean uppercaseName();

	/**
	 * Retrieve the action/type tags for the given profile and domain,
	 *  wrapped in appropriate xml wrapper objects
	 * @param children - existing vector to populate. Pass null to create and return new vector
	 * @param domain - the integer representation of the domain, or -1 iff domains not support
	 * @param profile - profile to determine the document to query. If profiles not supported, pass null.
	 * @return a vector of the action/type tags
	 */
	public Vector getXMLWrappers(Vector children, int domain, ISystemProfile profile) {
		SystemXMLElementWrapper domainElement = null;
		if (getActionSubSystem().supportsDomains()) {
			domainElement = getDomainWrapper(profile, domain);
			if (domainElement == null) // if parent domain not found, don't continue!
				return children;
		}
		return getXMLWrappers(children, domainElement, profile);
	}

	/**
	 * Retrieve the action/type tags for the given profile and domain,
	 *  wrapped in appropriate xml wrapper objects
	 * @param children - existing vector to populate. Pass null to create and return new vector
	 * @param parentOrDomain - if domains supported, this must be the parent domain whose kids are being queried, or an Integer for single-domains, else null
	 * @param profile - profile to determine the document to query. If profiles not supported, pass null.
	 * @return a vector of the action/type tags
	 */
	public Vector getXMLWrappers(Vector children, Object parentOrDomain, ISystemProfile profile) {
		int domain = -1;
		Element parentElement = null;
		if (parentOrDomain instanceof SystemXMLElementWrapper) {
			parentElement = ((SystemXMLElementWrapper) parentOrDomain).getElement();
			domain = ((SystemXMLElementWrapper) parentOrDomain).getDomain();
		} else if (parentOrDomain instanceof Element) parentElement = (Element) parentOrDomain;
		children = SystemXMLElementWrapper.getChildren(children, parentElement, getDocument(profile), profile, this, domain);
		return children;
	}

	/**
	 * Return true if the elements managed by this class are scoped by
	 *  profile. Usually true for actions, false for types
	 */
	public boolean supportsProfiles() {
		return true;
	}

	// -------------------------------------
	//
	// -------------------------------------
	/**
	 * 
	 */
	private void addListener() {
		// Team support
		// Register a listener for resource change events on objects
		// in our remote system project.
		// Dont register multiple times, if already done once.
		// (Since this load method may be repeated)
		/* ADDED BY JOHN, BUT COMMENTED OUT BY PHIL. OUR MODEL FOR RECOVERING FROM A TEAM SYNCH
		 *  IS TO REQUIRE THE USER TO RUN RELOADRSE. 
		 if (!listening) 
		 {
		 listening = true;
		 SystemResourceManager.addResourceChangeListener(this);
		 }
		 */
	}

	/**
	 * Return name of the xml file used to persist the actions
	 */
	public abstract String getFileName();

	/**
	 * Initialize a new document
	 */
	public Document initializeDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		}
		if (builder != null) {
			doc = builder.newDocument();
			//       Document doc= new DocumentImpl();
			// create root element. Eg <Actions> or <Types>
			Element root = doc.createElement(getDocumentRootTagName());
			// set current release as an attribute
			root.setAttribute(ISystemUDAConstants.RELEASE_ATTR, CURRENT_RELEASE_NAME);
			// assign root
			doc.appendChild(root); // Add Root to Document
		}
		return doc;
	}

	/**
	 * Get the current subsystem. Will be null for import, or working in team view when
	 *  no subsystems exist yet for a particular subsystem factory.
	 */
	protected ISubSystem getSubSystem() {
		if (_udas != null)
			return _udas.getSubsystem();
		else
			return null;
	}

	/**
	 * Get the current subsystem. Will be set in SystemProfileImpl's getUserActions method
	 *   for cases when there are no subsystems created for a subsystemconfiguration yet.
	 */
	protected ISubSystemConfiguration getSubSystemFactory() {
		if (_udas != null)
			return _udas.getSubSystemFactory();
		else
			return null;
	}

	/**
	 * Get the path of the XML document containing the user actions,
	 *  for the given profile (and current subsystem)
	 */
	protected String getFilePath(ISystemProfile profile) {
		IFolder folder = getFolder(profile);
		if (folder == null) return null;
		String fn = folder.getLocation().toOSString() + File.separator + getFileName();
		// System.out.println("UD file: " + fn);
		return fn;
	}

	/**
	 * Build a vector of the folder names, in order, from workspace
	 * to our data file.  Do once only.
	 * (To be used when resolving resource change events)
	 */
	private void resolveDirPath(ISystemProfile profile) {
		//     System.out.println("UD file:"  );
		Vector dirFolder = new Vector();
		dirFolder.add(getFileName());
		IContainer folder = getFolder(profile);
		while (folder != null) {
			// Insert at start of vector
			String s = folder.getName();
			// Workspace is empty string, dont add it
			if (!"".equals(s)) //$NON-NLS-1$
			{
				dirFolder.add(0, s);
				//  System.out.println("+" + s );
			}
			folder = folder.getParent();
		}
		Object[] dirPath = dirFolder.toArray();
		setProfileIndexedInstanceVariable_dirPath(profile, dirPath);
	}

	/**
	 * loadAndParseXMLFile:
	 * tries to load and parse the specified XML file.
	 * @param fileName the name of xml file which will contain the messages
	 * @param profile the profile in which the user defined actions are kept
	 * @return the document containing the user defined actions
	 */
	protected Document loadAndParseXMLFile(String fileName, ISystemProfile profile) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = null;
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			SystemBasePlugin.logError("SystemUDBaseManager: loadAndParseXMLFile, configuration not valid " + e.toString(), e); //$NON-NLS-1$
			return null;
		}
		//		DOMParser parser = new DOMParser();
		parser.setErrorHandler(this);
		try {
			Document doc = parser.parse(fileName);
			// verify the document is what we expect...
			Element docroot = doc.getDocumentElement(); // get Root Element
			// ?? Confirm root is XE_ROOT ??
			// ok, I took on the job of handling this. I also decided to put it 
			// here so we don't have this code scattered everywhere after calls to getDocument()
			// Phil. 08/2002
			if ((null == docroot) || !docroot.getTagName().equals(getDocumentRootTagName())) {
				Shell activeShell = getActiveShell();
				

				String oldFileName = getFilePath(profile);
				String newFileName = getFileName() + ".bad"; //$NON-NLS-1$
				IFile file = getFolder(profile).getFile(getFileName());
				try {
					SystemResourceHelpers.getResourceHelpers().renameFile(file, newFileName);
				} catch (Exception exc) {
				}

				
				String msgTxt = NLS.bind(UserActionsResources.MSG_UDA_ROOTTAG_ERROR, getFilePath(profile));
				String msgDetails = NLS.bind(UserActionsResources.MSG_UDA_ROOTTAG_ERROR_DETAILS, getDocumentRootTagName(), newFileName);
				
				SystemMessage docRootMsg = new SimpleSystemMessage(Activator.PLUGIN_ID, IStatus.ERROR, msgTxt, msgDetails);
		
				doc = createAndPrimeDocument(profile);
				docRootMsg.makeSubstitution(oldFileName, getDocumentRootTagName(), newFileName);
				SystemBasePlugin.logWarning(docRootMsg.getLevelOneText());
				if (activeShell != null) {
					SystemMessageDialog.displayErrorMessage(activeShell, docRootMsg);
				}
			}
			return doc;
		} catch (Exception exc) {
			// Provide a non-null value.  Might as well prime
			// with a "proper" doc structure.
			Document doc = initializeDocument();
			
			String msgTxt = NLS.bind(UserActionsResources.MSG_UDA_LOAD_ERROR, fileName);
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, IStatus.ERROR, msgTxt, exc);
			SystemMessageDialog msgdlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), msg);
			msgdlg.open();
			return doc;
		}
	}

	// **********************************************************
	//  ErrorHandler Interface:    (XML SAX parsing)
	// **********************************************************
	/** Warning. */
	public void warning(SAXParseException ex) {
		SystemBasePlugin.logWarning("SystemAbstractUDdata: XML Warning: " + ex.toString()); //$NON-NLS-1$
	}

	/** Error. */
	public void error(SAXParseException ex) {
		SystemBasePlugin.logError("SystemAbstractUDdata: XML Error: " + ex.toString(), ex); //$NON-NLS-1$
	}

	/** Fatal error. */
	public void fatalError(SAXParseException ex) throws SAXException {
		SystemBasePlugin.logError("SystemAbstractUDdata: Fatal XML error: " + ex.toString(), ex); //$NON-NLS-1$
		throw (ex);
	}

	// **********************************************************
	//
	// **********************************************************
	/**
	 * Interface   org.eclipse.core.resources. IResourceChangeListener
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		//System.out.println( "SystemUDBaseManager.resourceChanged:  flag=" + String.valueOf(ignoreMyResourceChange) );
		if (ignoreMyResourceChange) return;
		if (IResourceChangeEvent.POST_CHANGE != event.getType()) return;
		Object source = event.getSource();
		//   if (ignoreEvents || !(event.getSource() instanceof IWorkspace))
		if (!(source instanceof IWorkspace)) return;
		//		int type = event.getType();
		//		String sType = SystemResourceListener.getTypeString(type);
		//   System.out.println("RESOURCE CHANGED EVENT: eventType="+sType+", eventSource=" + source.toString() );
		//	  System.out.println("RESOURCE DELTA:"); //$NON-NLS-1$
		//		IResource resource = event.getResource();
		IResourceDelta delta = event.getDelta();
		if (null == delta) return;
		/*		
		 if ((resource == null) && (delta != null))
		 resource = delta.getResource();		
		 
		 if ( null != resource)
		 {
		 String rname =  resource.getLocation().toOSString();
		 
		 System.out.println("RESOURCE  " + rname);
		 System.out.println("RESOURCE Name: " + resource.getName() );
		 System.out.println("RESOURCE type: " + SystemResourceListener.getTypeString( resource.getType()) );
		 
		 if ( null != resource.getProject() )
		 System.out.println("RESOURCE Proj: " + resource.getProject().getName() );
		 
		 }
		 
		 resource = delta.getResource();		
		 if ( null != resource)
		 {
		 String rname =  resource.getLocation().toOSString();
		 
		 System.out.println("RESOURCE DELTA: " + rname);
		 System.out.println("RESOURCE Name: " + resource.getName() );
		 System.out.println("RESOURCE type: " + SystemResourceListener.getTypeString( resource.getType()) );
		 
		 if ( null != resource.getProject() )
		 System.out.println("RESOURCE Proj: " + resource.getProject().getName() );		
		 }		
		 */
		//System.out.println("res UD file:"  );
		if (supportsProfiles()) {
			ISystemProfile[] activeProfiles = getActiveSystemProfiles();
			for (int idx = 0; idx < activeProfiles.length; idx++) {
				ISystemProfile profile = activeProfiles[idx];
				ensureDirPathResolved(profile);
				searchDelta(profile, delta, 0);
			}
		} else {
			ensureDirPathResolved(null);
			searchDelta(null, delta, 0);
		}
	}

	private void ensureDirPathResolved(ISystemProfile profile) {
		Object[] dirPath = getProfileIndexedInstanceVariable_dirPath(profile);
		if (dirPath == null) resolveDirPath(profile);
	}

	private void searchDelta(ISystemProfile profile, IResourceDelta parent, int nestLevel) {
		Object[] dirPath = getProfileIndexedInstanceVariable_dirPath(profile);
		String target = (String) dirPath[nestLevel];
		// System.out.println("search for: "+String.valueOf( nestLevel)+ ": "+target);
		IResourceDelta resdel[] = parent.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.ADDED | IResourceDelta.REMOVED);
		for (int i = 0; i < resdel.length; i++) {
			IResource resource = resdel[i].getResource();
			// System.out.println("  ..  " + String.valueOf(i) + ": " + resource.getName());
			if ((null != resource) && target.equals(resource.getName())) {
				// End of the search chain?
				nestLevel++;
				if (nestLevel < dirPath.length)
					// Recurse
					searchDelta(profile, resdel[i], nestLevel);
				else {
					// Matches !!
					//System.out.println("Matches! " );
					processResourceChangeHit(profile);
				}
				// Stop further searching at this level
				return;
			}
		}
	}

	/**
	 * Get the active system profiles
	 */
	private ISystemProfile[] getActiveSystemProfiles() {
		return RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
	}

	// ---------------------------------------------------------------------------
	// The GUI for working with user-defined actions sets the current profile
	// as the user selects it. This becomes the default profile to work with until
	// the user changes it again
	// ---------------------------------------------------------------------------
	/**
	 * Set the profile we are working with the actions for, until reset.
	 * Note, to reduce chance for errors, we do not implicitly use this profile
	 * anywhere! Rather, this is simply a convenient holding place for it,
	 * and the caller must explicitly call getCurrentProfile to retrieve it
	 * when it is needed as input to any of the other methods in this class
	 * or a child class.
	 */
	public void setCurrentProfile(ISystemProfile profile) {
		this.currentlyActiveProfile = profile;
	}

	/**
	 * Get the profile we are currently working with, as set by a call to setCurrentProfile
	 */
	public ISystemProfile getCurrentProfile() {
		return currentlyActiveProfile;
	}

	// -----------------------------------------------------------	
	// THE FOLLOWING METHODS ARE ALL INDEXED BY SYSTEM PROFILE...
	// -----------------------------------------------------------
	/**
	 * Prime the given document with any default actions/types
	 * Should be overridden!
	 */
	public SystemXMLElementWrapper[] primeDocument(ISystemProfile profile) {
		return null;
	}

	/**
	 * Get the release of the document. Eg, value of the "release"attribute of the root.
	 * If not set then we assume it is release "4.0"
	 */
	public String getDocumentRelease(ISystemProfile profile) {
		Document doc = getDocument(profile);
		Element root = doc.getDocumentElement();
		String rel = root.getAttribute(ISystemUDAConstants.RELEASE_ATTR);
		if (rel == null)
			return "4.0"; //$NON-NLS-1$
		else
			return rel;
	}

	/**
	 * Load document for given SystemProfile only if not already done.
	 */
	public Document getDocument(ISystemProfile profile) {
		Document doc = getProfileIndexedInstanceVariable_Document(profile);
		if (doc == null) {
			doc = loadUserData(profile);
			setProfileIndexedInstanceVariable_Document(profile, doc);
			// document is good. Now, check the release date stamped on it.
			// if not the current release, then we must consider migration...	
			Element docroot = doc.getDocumentElement();
			String docRelease = docroot.getAttribute(ISystemUDAConstants.RELEASE_ATTR);
			if ((docRelease == null) || (docRelease.length() == 0)) docRelease = "4.0"; //$NON-NLS-1$
			if (!docRelease.equals(CURRENT_RELEASE_NAME)) {
				//System.out.println("Doing migration from "+docRelease+" to " + ISystemConstants.CURRENT_RELEASE_NAME + "...");
				boolean migrationDone = doMigration(profile, docRelease);
				docroot.setAttribute(ISystemUDAConstants.RELEASE_ATTR, RSECorePlugin.CURRENT_RELEASE_NAME);
				if (migrationDone) {
					setChanged(profile); // is this the right thing to do?
					saveUserData(profile);
				}
			}
		} else {
		}
		return doc;
	}

	/**
	 * Overridable extension point for child classes to do migration of their document.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done
	 */
	protected abstract boolean doMigration(ISystemProfile profile, String oldRelease);

	/**
	 * Get the active shell so we can show an error message
	 */
	private Shell getActiveShell() {
		return Display.getCurrent().getActiveShell();
	}

	/**
	 * Indicate the data has changed for the document for the given system profile
	 */
	public void setChanged(ISystemProfile profile) {
		dataChanged(profile);
		setProfileIndexedInstanceVariable_hasChanged(profile, true);
	}

	/**
	 * Reload the User data for the given profile, from disk if it has been changed in memory
	 */
	public void resetUserData(ISystemProfile profile) {
		//System.out.println("UD reset: " + getFileName() );
		if (!getProfileIndexedInstanceVariable_hasChanged(profile)) setProfileIndexedInstanceVariable_Document(profile, loadUserData(profile));
	}

	/**
	 * Force a re-load
	 */
	public void processResourceChangeHit(ISystemProfile profile) {
		setProfileIndexedInstanceVariable_Document(profile, null);
		dataChanged(profile);
	}

	/**
	 * Get the folder containing the xml file used to persist the actions,
	 *  for the given profile
	 */
	private IFolder getFolder(ISystemProfile profile) {
		ISubSystem subsystem = getSubSystem();
		// Import action:  no subsystem
		if ((subsystem == null) && (getSubSystemFactory() == null))
			return importCaseFolder;
		else {
			if ((profile == null) && (subsystem != null)) profile = subsystem.getSystemProfile();
			return getDocumentFolder(getSubSystemFactory(), profile);
		}
	}

	/**
	 * Get the folder containing the xml file used to persist the actions,
	 *  for the given profile
	 */
	protected abstract IFolder getDocumentFolder(ISubSystemConfiguration subsystemFactory, ISystemProfile profile);

	/**
	 * Intended for IMPORT actions only, where no Subsystem instance available:
	 */
	public abstract void setFolder(String profileName, String factoryId);

	/**
	 * Indicate data has changed for the given profile
	 */
	protected void dataChanged(ISystemProfile profile) {
	}

	/**
	 * Load the user actions from the XML document, for the given profile
	 */
	protected Document loadUserData(ISystemProfile profile) {
		//System.out.println("UD load: " + getFileName() );
		dataChanged(profile); // not sure why we call this, at this time!! Phil
		setProfileIndexedInstanceVariable_hasChanged(profile, false);
		String fn = getFilePath(profile);
		Document doc = null;
		if (!(new File(fn)).canRead())
			doc = createAndPrimeDocument(profile);
		else
			doc = loadAndParseXMLFile(fn, profile);
		//addListener();
		return doc;
	}

	/**
	 * Create and prime the XML document
	 */
	protected Document createAndPrimeDocument(ISystemProfile profile) {
		Document doc = initializeDocument();
		setProfileIndexedInstanceVariable_Document(profile, doc);
		SystemXMLElementWrapper[] primedElements = primeDocument(profile);
		if (primedElements != null) {
			for (int idx = 0; idx < primedElements.length; idx++) {
				SystemXMLElementWrapper newElement = primedElements[idx];
				newElement.setIBM(true);
				newElement.setUserChanged(false);
			}
		}
		saveUserData(profile);
		return doc;
	}

	/**
	 * Save user data for the given system profile
	 */
	public void saveUserData(ISystemProfile profile) {
		//System.out.println("UD save: " + getFileName() );
		if (!getProfileIndexedInstanceVariable_hasChanged(profile)) {
			//System.out.println("UD save: No changes. " + getFileName() );
			return;
		}
		/* Old way of toggling off/on resource change monitoring wasnt working
		 for case of two connections with same subsystems with UDA data loaded,
		 and user changes UDA data in one.  Change wasn't propagated to other,
		 which might later update the UDA file with old data.
		 
		 Better appraoch is to toggle my (ignoreMyResourceChange) flag around
		 my update, and check that flag in my resource change listener so that I
		 dont reload the data I just saved.  (But other connection subsystem
		 instances do.)
		 */
		String fn = getFilePath(profile);
		// Disable our resource change monitoring temporarily while we
		// update the XML file
		ignoreMyResourceChange = true;
		SystemResourceManager.turnOffResourceEventListening();
		File xf = new File(fn);
		if (!xf.getParentFile().exists()) // test added by Phil
		{
			xf.getParentFile().mkdirs(); // this is bad! Who refreshes from local? 
			try { // ok, now we do. I added this. Phil
				getFolder(profile).getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Exception exc) {
			}
		}
		Document udoc = getProfileIndexedInstanceVariable_Document(profile);
		try // address various file I/O exceptions
		{
			FileOutputStream fo = new FileOutputStream(xf);
			try {
				Source source = new DOMSource(udoc);
				Result result = new StreamResult(fo);
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
				t.transform(source, result);
			} catch (TransformerConfigurationException e) {
			} catch (TransformerFactoryConfigurationError e) {
			} catch (TransformerException e) {
			}
			//			XMLSerializer xs = new XMLSerializer(fo, null);
			//			// Select "readable" format.  (multiple lines)
			//			OutputFormat fmt = new OutputFormat(udoc);
			//			xs.setOutputFormat(fmt);
			//			fmt.setIndenting(true);
			//			fmt.setIndent(1); // 0 turns off indenting
			//			// Line width 300, so that we don't cut long comments/commands/actions at a certain length.
			//			fmt.setLineWidth(300);
			//			fmt.setPreserveSpace(true);
			//			xs.serialize(udoc);
			//			fo.close();
			//      fo = null;
			setProfileIndexedInstanceVariable_hasChanged(profile, false);
			// Always refresh-from-local:
			// If this saves, don't want next refresh-from-local to trigger
			// a change event when we were the ones to change it.  (and have the
			// current data already loaded.)
			// Refresh the workspace to recognise (new) file.
			//    getFolder().refreshLocal( IResource.DEPTH_ONE, null);
			// A more specific refresh:
			IFile file = getFolder(profile).getFile(getFileName());
			//		file.touch(null);
			file.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (Exception exc) {
			//        if (null != fo)
			//          fo.close();
			SystemMessageDialog msgdlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_SAVE_FAILED).makeSubstitution(exc));
			msgdlg.open();
		}
		ignoreMyResourceChange = false;
		SystemResourceManager.turnOnResourceEventListening();
	}

	/**
	 * Refresh the xml file from disk. Eg equivalent to use selecting Refresh.
	 */
	public void refreshLocal(ISystemProfile profile) {
		try {
			IFile file = getFolder(profile).getFile(getFileName());
			file.touch(null);
			file.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (Exception e) {
			SystemBasePlugin.logError("Error refreshing in SystemUDBaseManager.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Move given element down one in document, save document
	 * @return true if move successful
	 */
	public boolean moveElementDown(SystemXMLElementWrapper elementWrapper, SystemXMLElementWrapper nextNextElementWrapper) {
		getDocument(elementWrapper.getProfile());
		Element element = elementWrapper.getElement();
		try {
			Node parentElement = element.getParentNode();
			//Node nextElement = element.getNextSibling();
			//Node nextNextElement = nextElement.getNextSibling();
			if (nextNextElementWrapper != null)
				parentElement.insertBefore(element, nextNextElementWrapper.getElement());
			else
				parentElement.insertBefore(element, null);
		} catch (Exception exc) {
			SystemBasePlugin.logError("Error moving user action/type down", exc); //$NON-NLS-1$
			return false;
		}
		saveUserData(elementWrapper.getProfile());
		return true;
	}

	/**
	 * Move given element up one in document, save document
	 * @return true if move successful
	 */
	public boolean moveElementUp(SystemXMLElementWrapper elementWrapper, SystemXMLElementWrapper previousElementWrapper) {
		getDocument(elementWrapper.getProfile());
		Element element = elementWrapper.getElement();
		try {
			Node parentElement = element.getParentNode();
			//Node previousElement = element.getPreviousSibling();
			parentElement.insertBefore(element, previousElementWrapper.getElement());
		} catch (Exception exc) {
			SystemBasePlugin.logError("Error moving user action/type up", exc); //$NON-NLS-1$
			return false;
		}
		saveUserData(elementWrapper.getProfile());
		return true;
	}

	/**
	 * Prepares a given element for the clipboard.
	 * This clones the element in transient memory, and returns a reference to the clone
	 * that can be placed in a local clipboard instance. Subsequently, paste is only enabled
	 * if the reference in the clipboard corresponds to a node clone in this object.
	 * @return an id that uniquely identifies the cloned node, or null if it failed.
	 */
	public String prepareClipboardCopy(SystemXMLElementWrapper elementWrapper) {
		getDocument(elementWrapper.getProfile());
		Element element = elementWrapper.getElement();
		currentNodeClone = null;
		try {
			currentNodeClone = (Element) element.cloneNode(true); // true=>deep clone, including text vs just attributes
		} catch (Exception exc) {
			SystemBasePlugin.logError("Error cloning user action/type element for clipboard", exc); //$NON-NLS-1$
			return null;
		}
		currentNodeCloneID = getActionSubSystem().getClass().getName() + "." + //$NON-NLS-1$
				getFileName() + "." + //$NON-NLS-1$
				elementWrapper.getName();
		currentNodeCloneDomain = elementWrapper.getDomain();
		//		currentNodeCloneName = elementWrapper.getName();
		return currentNodeCloneID;
	}

	/**
	 * Test if the given ID, read from the clipboard, matches a node we prepared for
	 * the clipboard. It also ensure that domains match.
	 * <p>
	 * This decides if the paste action will be enabled or not
	 */
	public boolean enablePaste(SystemXMLElementWrapper selectedElementWrapper, String id) {
		if (id == null) return false;
		int selectedElementDomain = selectedElementWrapper.getDomain();
		return (id.equals(currentNodeCloneID) && (selectedElementDomain == currentNodeCloneDomain));
	}

	/**
	 * After a successful call to enablePaste, this is called to do the paste operation.
	 * The new object is inserted before the current selection if appropriate, else to the end of the domain
	 * @return SystemXMLElementWrapper wrapper object of pasted element, or null if it failed
	 */
	public SystemXMLElementWrapper pasteClipboardCopy(SystemXMLElementWrapper selectedElementWrapper, String id) {
		getDocument(selectedElementWrapper.getProfile());
		Element selectedElement = selectedElementWrapper.getElement();
		SystemXMLElementWrapper pastedElementWrapper = null;
		try {
			Node parentElement = null;
			Element pastedElement = null;
			if (selectedElementWrapper.isDomain()) {
				parentElement = selectedElement;
				pastedElement = (Element) parentElement.appendChild(currentNodeClone);
			} else {
				parentElement = selectedElement.getParentNode();
				pastedElement = (Element) parentElement.insertBefore(currentNodeClone, selectedElement);
			}
			pastedElementWrapper = createElementWrapper(pastedElement, selectedElementWrapper.getProfile(), selectedElementWrapper.getDomain());
			pastedElementWrapper.setName(getUniqueCloneName(pastedElementWrapper));
			pastedElementWrapper.setIBM(false); // not an IBM action, even if source was
		} catch (Exception exc) {
			SystemBasePlugin.logError("Error pasting user action/type", exc); //$NON-NLS-1$
			return null;
		}
		saveUserData(selectedElementWrapper.getProfile());
		return pastedElementWrapper;
	}

	/**
	 * Return a new unique name to assign to a pastable element node clone
	 */
	private String getUniqueCloneName(SystemXMLElementWrapper elementWrapper) {
		String newName = SystemUDAResources.RESID_UDA_COPY_NAME_1;
		newName = SystemMessage.sub(newName, "%1", elementWrapper.getName()); //$NON-NLS-1$
		Vector existingNames = getExistingNames(elementWrapper.getProfile(), elementWrapper.getDomain());
		boolean nameInUse = (existingNames.indexOf(newName) >= 0);
		int nbr = 2;
		while (nameInUse) {
			newName = SystemUDAResources.RESID_UDA_COPY_NAME_N;
			newName = SystemMessage.sub(newName, "%1", elementWrapper.getName()); //$NON-NLS-1$
			newName = SystemMessage.sub(newName, "%2", Integer.toString(nbr)); //$NON-NLS-1$
			nameInUse = (existingNames.indexOf(newName) >= 0);
			++nbr;
		}
		return newName;
	}

	// ----------------------------------------------------------------------
	// THE FOLLOWING WERE PULLED DOWN FROM VARIOUS SUBCLASSES, AND ABSTRACTED
	// TO BE USABLE AS IS FOR ALL SCENARIOS:
	//   ACTIONS VS TYPES
	//   PROFILE-SCOPED VS NOT-PROFILE-SCOPED
	//   SUPPORTS-DOMAINS VS DOESN'T-SUPPORT DOMAINS
	// ----------------------------------------------------------------------
	/**
	 * Method declared on IStructuredContentProvider.
	 * Returns root elements for the currently set profile (see setCurrentProfile).
	 * If this is null, returns root elements for all active profiles
	 */
	public Object[] getElements(Object element) {
		if (!supportsProfiles())
			return getElements((ISystemProfile) null, element);
		else {
			ISystemProfile currProfile = getCurrentProfile();
			if (currProfile != null)
				return getElements(currProfile, element);
			else
				return getElements(getActiveSystemProfiles(), element);
		}
	}

	/**
	 * Return root elements for given profile.
	 */
	public Object[] getElements(ISystemProfile profile, Object element) {
		return getElements(new ISystemProfile[] { profile }, element);
	}

	/**
	 * Return the root elements.
	 * <p>
	 * If domains are supported, returns a root "New" item plus element wrappers for 
	 *  any existing domain tags in the xml
	 * If domains are not supported, returns a non-root "New" item plus element
	 *  wrappers for all action/type xml tags found under the root of the xml document
	 */
	public Object[] getElements(ISystemProfile[] profiles, Object input) {
		/*
		 if (input == null)
		 System.out.println("Inside getElements. input is null");
		 else
		 System.out.println("Inside getElements. input is of type " + input.getClass().getName());
		 */
		if ((input != null) && !(input instanceof String)) return EMPTY_ARRAY;
		Vector v = new Vector();
		// if domains supported, return "New" root item, plus wrappers of 
		//  any domain xml elements found...
		int onlyDomain = getActionSubSystem().getSingleDomain(this);
		if (getActionSubSystem().supportsDomains() && (onlyDomain == -1)) {
			v.add(SystemUDTreeViewNewItem.getRootNewItem(isUserActionsManager(), getNewNodeLabel()));
			if (supportsProfiles()) {
				// get domain elements per given profile
				for (int idx = 0; idx < profiles.length; idx++) {
					ISystemProfile profile = profiles[idx];
					v = createExistingDomainElementWrappers(v, profile);
				}
			} else {
				// get domain elements 
				v = createExistingDomainElementWrappers(v, null);
			}
		}
		// if domains not supported, return singleton New item, plus wrappers
		//  of any action/type elements found
		else {
			Element parentDomainElement = null;
			if (onlyDomain == -1)
				v.add(SystemUDTreeViewNewItem.getOnlyNewItem(isUserActionsManager(), getNewNodeLabel()));
			else
				v.add(SystemUDTreeViewNewItem.getOnlyNewItem(onlyDomain, isUserActionsManager(), getNewNodeLabel()));
			if (supportsProfiles()) {
				// get actual elements (actions/types) per given profile
				for (int idx = 0; idx < profiles.length; idx++) {
					ISystemProfile profile = profiles[idx];
					if (onlyDomain != -1) parentDomainElement = findDomainElement(getDocument(profile), onlyDomain);
					v = getXMLWrappers(v, parentDomainElement, profile);
				}
			} else {
				// get actual elements (actions/types)
				if (onlyDomain != -1) parentDomainElement = findDomainElement(getDocument(null), onlyDomain);
				v = getXMLWrappers(v, parentDomainElement, null);
			}
		}
		return v.toArray();
	}

	/**
	 * Overridable method for returning the label for the "New" nodes in the tree view.
	 * Will usually be different for actions versus types.
	 * @return translated value for "New" in new icon. Default is "New"
	 */
	protected String getNewNodeLabel() {
		return SystemResources.ACTION_CASCADING_NEW_LABEL;
	}

	/**
	 * Return true if this is user actions, false if this is named types.
	 */
	protected abstract boolean isUserActionsManager();

	/**
	 * Return all the user actions/types under the given node.
	 * If input is a New item, return New items per domain
	 * If input is a Domain element wrapper, return wrappers of all child actions/types under that domain,
	 *   for that domain's profile.
	 */
	public Object[] getChildren(Object element) {
		/*
		 if (element == null)
		 System.out.println("Inside getElements. input is null");
		 else
		 System.out.println("Inside getElements. input is of type " + element.getClass().getName());
		 */
		if (element instanceof SystemUDTreeViewNewItem) {
			// Only on the (parent) cascade item.
			// Will only happen if we support domains
			if (!((SystemUDTreeViewNewItem) element).isExecutable()) {
				boolean isUserActionDialog = ((SystemUDTreeViewNewItem) element).isWorkWithActionsDialog();
				if (newItemsPerDomain == null) {
					int nbrDomains = getActionSubSystem().getMaximumDomain() + 1;
					newItemsPerDomain = new SystemUDTreeViewNewItem[nbrDomains];
					for (int idx = 0; idx < newItemsPerDomain.length; idx++) {
						if (isUserActionDialog)
							newItemsPerDomain[idx] = new SystemUDTreeViewNewItem(true, getActionSubSystem().mapDomainXlatedNewName(idx), idx, isUserActionDialog);
						else
							newItemsPerDomain[idx] = new SystemUDTreeViewNewItem(true, getActionSubSystem().mapDomainXlatedNewTypeName(idx), idx, isUserActionDialog);
					}
				}
				return newItemsPerDomain;
			}
			return EMPTY_ARRAY;
		}
		// getElements() is called to get roots, so we should never be 
		// called here unless we have been given a domain element wrapper
		if (!(element instanceof SystemXMLElementWrapper) || !((SystemXMLElementWrapper) element).isDomain()) return EMPTY_ARRAY;
		SystemXMLElementWrapper parent = (SystemXMLElementWrapper) element;
		Vector v = new Vector();
		ISystemProfile profile = parent.getProfile();
		getXMLWrappers(v, parent, profile);
		return v.toArray();
	}

	/**
	 * Find a child element of a given name.
	 * Returns the xml node element or null
	 */
	public Element findChildByName(ISystemProfile profile, String name, int domain) {
		Document xdoc = getDocument(profile);
		if (getActionSubSystem().supportsDomains() && (domain >= 0)) {
			Element domainElement = findDomainElement(xdoc, domain);
			return SystemXMLElementWrapper.findChildByName(domainElement, xdoc, getTagName(), name);
		} else {
			return SystemXMLElementWrapper.findChildByName(null, xdoc, getTagName(), name);
		}
	}

	/**
	 * Find a child element of a given name.
	 * Returns the wrapper of the xml node element or null
	 */
	public SystemXMLElementWrapper findByName(ISystemProfile profile, String name, int domain) {
		Element element = findChildByName(profile, name, domain);
		if (element == null)
			return null;
		else
			return createElementWrapper(element, profile, domain);
	}

	/**
	 * Get a list of existing names, for unique-name checking.
	 */
	public Vector getExistingNames(ISystemProfile profile, int domain) {
		Document xdoc = getDocument(profile);
		if (getActionSubSystem().supportsDomains() && (domain >= 0)) {
			Element domainElement = findDomainElement(xdoc, domain);
			if (domainElement == null) return new Vector(); // defect 46147
			return SystemXMLElementWrapper.getExistingNames(domainElement, xdoc, getTagName());
		} else {
			return SystemXMLElementWrapper.getExistingNames(null, xdoc, getTagName());
		}
	}

	/**
	 * Add a new user action or type.
	 * Creates the new XML node in the document,
	 *  and creates and returns a wrapper object for it.
	 */
	public SystemXMLElementWrapper addElement(ISystemProfile profile, int domain, String name) {
		Document xdoc = getDocument(profile);
		SystemXMLElementWrapper newElementWrapper = null;
		Element newtag = xdoc.createElement(getTagName());
		newtag.setAttribute("Name", uppercaseName() ? name.toUpperCase() : name); //$NON-NLS-1$
		// Get domain element, create if necessary
		if (getActionSubSystem().supportsDomains()) {
			Element se = findOrCreateDomainElement(xdoc, domain);
			se.appendChild(newtag);
		} else
			xdoc.getDocumentElement().appendChild(newtag);
		newElementWrapper = createElementWrapper(newtag, profile, domain);
		setChanged(profile);
		return newElementWrapper;
	}

	/**
	 * Delete a give user action or type, given its wrapper.
	 * Deletes the xml node from the document.
	 */
	public void delete(ISystemProfile profile, SystemXMLElementWrapper elementWrapper) {
		elementWrapper.deleteElement();
		setChanged(profile);
	}

	// -----------------------------------------------------------    
	// ISOLATE READING AND WRITING OF PROFILE-INDEXED VARIABLES...
	// -----------------------------------------------------------
	/**
	 * Set the profile-indexed document instance variable
	 */
	private void setProfileIndexedInstanceVariable_Document(ISystemProfile profile, Document doc) {
		if (!supportsProfiles())
			udocNoProfile = doc;
		else
			udocsByProfile.put(profile, doc);
	}

	/**
	 * Get the profile-indexed document instance variable
	 */
	private Document getProfileIndexedInstanceVariable_Document(ISystemProfile profile) {
		if (!supportsProfiles())
			return udocNoProfile;
		else
			return (Document) udocsByProfile.get(profile);
	}

	/**
	 * Set the profile-indexed has-changed instance variable
	 */
	private void setProfileIndexedInstanceVariable_hasChanged(ISystemProfile profile, boolean hasChanged) {
		if (!supportsProfiles())
			hasChangedNoProfile = hasChanged;
		else {
			if (hasChanged)
				hasChangedByProfile.put(profile, Boolean.TRUE);
			else
				hasChangedByProfile.put(profile, Boolean.FALSE);
		}
	}

	/**
	 * Get the profile-indexed has-changed instance variable
	 */
	private boolean getProfileIndexedInstanceVariable_hasChanged(ISystemProfile profile) {
		if (!supportsProfiles())
			return hasChangedNoProfile;
		else {
			Boolean b = (Boolean) hasChangedByProfile.get(profile);
			if (b == null)
				return false;
			else
				return (b == Boolean.TRUE);
		}
	}

	/**
	 * Set the profile-indexed dir-path instance variable
	 */
	private void setProfileIndexedInstanceVariable_dirPath(ISystemProfile profile, Object[] dirPath) {
		if (!supportsProfiles())
			dirPathNoProfile = dirPath;
		else
			dirPathByProfile.put(profile, dirPath);
	}

	/**
	 * Get the dir-path has-changed instance variable
	 */
	private Object[] getProfileIndexedInstanceVariable_dirPath(ISystemProfile profile) {
		if (!supportsProfiles())
			return dirPathNoProfile;
		else
			return (Object[]) dirPathByProfile.get(profile);
	}

	// -------------------------------------
	// METHODS RELATED TO DOMAIN ELEMENTS...
	// -------------------------------------
	/**
	 * Given a domain's integer representation, find its element in 
	 * xml document and return the wrapper for it. If not found,
	 * returns null
	 */
	protected SystemXMLElementWrapper getDomainWrapper(ISystemProfile profile, int domain) {
		Element element = findDomainElement(getDocument(profile), domain);
		if (element != null)
			return createDomainElementWrapper(element, profile, domain);
		else
			return null;
	}

	/**
	 * Find all existing domain XML elements that are children of the root,
	 *   and create wrapper objects for them, and add them to the given vector.
	 * <p>
	 * It is important to note these are returned in the pre-determined order,
	 *  not the order they are found in the document!
	 */
	protected Vector createExistingDomainElementWrappers(Vector v, ISystemProfile profile) {
		Document xdoc = getDocument(profile);
		Element docroot = xdoc.getDocumentElement();
		// get the "domain" children of the root, in the pre-determined order of domains
		NodeList subList = docroot.getChildNodes();
		if ((subList == null) || (subList.getLength() == 0)) return v;
		String[] domains = getActionSubSystem().getDomainNames();
		int subListLen = subList.getLength();
		for (int idx = 0; idx < domains.length; idx++) {
			Element match = null;
			for (int jdx = 0; (match == null) && (jdx < subListLen); jdx++) {
				Node currNode = subList.item(jdx);
				if ((currNode instanceof Element) &&
				// is "Domain" tag, and "Type" attr value matches domains[idx]?
						isDomainElement((Element) currNode, domains[idx])) {
					//Element currElement = (Element)currNode;
					//if (currElement.getAttribute(XE_DOMTYPE).equals(domains[idx]))
					match = (Element) currNode;
				}
			}
			if (match != null) v.add(createDomainElementWrapper(match, profile, idx));
		}
		return v;
	}

	/**
	 * Create a domain element wrapper
	 */
	protected SystemXMLElementWrapper createDomainElementWrapper(Element xmlDomainElementToWrap, ISystemProfile profile, int domain) {
		return createElementWrapper(xmlDomainElementToWrap, profile, domain);
	}

	/**
	 * Given an xml action/type document, try to find a domain element ("Domain" tag)
	 *  of the given domain type. If not found, do NOT create it.
	 */
	protected Element findDomainElement(Document xdoc, int domain) {
		return findOrCreateDomainElement(xdoc, domain, false);
	}

	/**
	 * Given an xml action/type document, try to find a domain element ("Domain" tag)
	 *  of the given untranslated name ("Type" attribute). If not found, create it.
	 */
	protected Element findOrCreateDomainElement(Document xdoc, int domain) {
		return findOrCreateDomainElement(xdoc, domain, true);
	}

	/**
	 * Given an xml action/type document, try to find a domain element ("Domain" tag)
	 *  of the given untranslated name ("Type" attribute). If not found, optionally create it.
	 */
	protected Element findOrCreateDomainElement(Document xdoc, int domain, boolean create) {
		NodeList subList = xdoc.getDocumentElement().getChildNodes();
		String domainName = getActionSubSystem().mapDomainName(domain); // unxlated name. Eg "Type" parm
		Element domainElement = null;
		if (subList != null) {
			for (int idx = 0; (domainElement == null) && (idx < subList.getLength()); idx++) {
				Node sn = subList.item(idx);
				if (sn instanceof Element) {
					if (isDomainElement((Element) sn, domainName)) domainElement = (Element) sn;
				}
			}
		}
		if (create && (domainElement == null)) domainElement = createDomainElement(xdoc, domain);
		return domainElement;
	}

	/**
	 * Create a new xml domain element. That, an element of tag name "Domain".
	 * @param xdoc - the document to add it to. Will be added as child of root
	 * @param domain - the integer representation of the domain, used to get its name and translated name
	 */
	protected Element createDomainElement(Document xdoc, int domain) {
		Element element = xdoc.createElement(ISystemUDAConstants.XE_DOMAIN);
		xdoc.getDocumentElement().appendChild(element);
		element.setAttribute(ISystemUDAConstants.XE_DOMTYPE, getActionSubSystem().mapDomainName(domain));
		element.setAttribute(ISystemUDAConstants.XE_DOMNAME, getActionSubSystem().mapDomainXlatedName(domain));
		return element;
	}

	// -------------------------------------------
	// STATIC HELPER METHODS RELATED TO DOMAINS...
	// -------------------------------------------
	/**
	 * Given an xml Element object, return true if it is a Domain
	 *  element. That is, if its tag name is "Domain"
	 */
	public static boolean isDomainElement(Element element) {
		return (element.getTagName().equals(ISystemUDAConstants.XE_DOMAIN));
	}

	/**
	 * Given an xml Element object, return true if it is a Domain
	 *  element and its "Type" attribute matches the given name.
	 */
	public static boolean isDomainElement(Element element, String domainName) {
		return isDomainElement(element) && domainTypeEquals(element, domainName);
	}

	/**
	 * Given an xml Domain element, return true if it's "type" attribute matches 
	 *  the given untranslated domain name
	 */
	public static boolean domainTypeEquals(Element element, String domainName) {
		return (element.getAttribute(ISystemUDAConstants.XE_DOMTYPE).equals(domainName));
	}

	/**
	 * Checking not deleted.  Still in document tree?
	 * (for Actions and Types).
	 * Needed by tree view/ Edit pane selection change processing.
	 * If current selection has validation errors, & user tries to
	 * change selection, want to set view back to the old selection,
	 * but have to confirm it hasn't been deleted, first.
	 * 
	 * Do so by traversing the tree backwards, back to the Document root,
	 * then forwards again to verify the child links are in place.
	 */
	public static boolean inCurrentTree(Node n) {
		if (n instanceof Document) return true;
		Node parent = n.getParentNode();
		if (null == parent) return false;
		// Recursive, walk tree back to root, then finally Document.
		if (!inCurrentTree(parent)) return false;
		// Finally, check this is still a child of the parent
		Node sibling = parent.getFirstChild();
		while (null != sibling) {
			if (n == sibling) return true;
			sibling = sibling.getNextSibling();
		}
		return false;
	}
}
