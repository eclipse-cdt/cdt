package org.eclipse.rse.internal.useractions.files.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Xuan Chen        (IBM)    - [222263] Need to provide a PropertySet Adapter for System Team View (cleanup some use action stuff)
 * Kevin Doyle		(IBM)	 - [222830] ArrayIndexOutOfBoundsException on Restore Defaults on Folder User Actions
 * Xuan Chen        (IBM)    - [246807] [useractions] - Command Command property set from different os and subsystem are all shown as "Compile Commands" in Team view
 *******************************************************************************/
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.uda.ISystemUDAConstants;
import org.eclipse.rse.internal.useractions.ui.uda.ISystemUDAEditPaneHoster;
import org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTreeView;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionManager;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDBaseManager;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeEditPane;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeManager;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/** 
 * User action subsystem for universal files
 */
public class UDActionSubsystemFiles extends SystemUDActionSubsystem {
	public static final String NO_EXTENSION_PLACEHOLDER = ".null"; //$NON-NLS-1$
	private static DateFormat dateFormatter;
	// INSTANCE VARIABLES...
	private FileTypeMatcher fileTypeMatcher;
	// CONSTANTS...
	private static final String DOMAINS[] = { "Folder", "File" }; //$NON-NLS-1$ //$NON-NLS-2$
	// Matching name string in the plugin resources (translated)
	//private String DOMAIN_NEWTYPENAME_STRING[] = { RESID_UDT_FILES_DOMAIN_NEWFOLDER, RESID_UDT_FILES_DOMAIN_NEWFILE };
	// index values must match above 2 variables
	public static final int DOMAIN_FOLDER = 0;
	public static final int DOMAIN_FILE = 1;
	protected static final String FILE_ACTIONS[][] = // todo!
	//      name,    refresh, singleSel, collect, types,         cmd
	{ { "java", "false", "true", "false", "CLASS", "java ${resource_name_root}" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			{ "javac", "true", "false", "true", "JAVA", "javac -deprecation -classpath . ${resource_name}" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			{ "jar", "true", "false", "true", "ALL", "jar -cvf classes.jar ${resource_name}" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			{ "unjar", "false", "true", "false", "JAR ZIP", "jar -xf ${resource_name}" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			{ "gmake", "true", "false", "false", "GNU_MAKEFILE", "gmake -f ${resource_name}" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			{ "make", "true", "false", "false", "MAKEFILE", "make -f ${resource_name}" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	// I have decided against the following in favor of allowing these to be expanded in the tree view. Phil
	//{"list",   "false", "true",    "false", "JAR ZIP",     "jar tf ${resource_name}"}, 
	};
	protected static final String FOLDER_ACTIONS[][] =
	//      name,    refresh, singleSel, collect, cmd
	{ { "javac", "true", "false", "true", "javac -deprecation -classpath . *.java" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			{ "jar", "true", "false", "true", "jar cvf classes.jar *.class" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	};
	protected static final String IBM_DEFAULT_FOLDERTYPES[][] = { // name, types
	{ "ALL", "*" }, //$NON-NLS-1$ //$NON-NLS-2$
	};
	protected static final String IBM_DEFAULT_FILETYPES[][] = { // name,   types      
	{ "ALL", "*" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "C", "c,h,i" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "C_COMPILABLE", "c" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "CPP", "cpp,cxx,hpp,ipp" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "CPP_COMPILABLE", "cpp,cxx" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "CLASS", "class" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "CSS", "css" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "EAR", "ear" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "EXE", "exe,bat,cmd" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "GRAPHIC", "bmp,gif,jpg" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "GNU_MAKEFILE", "GNUMakefile" + NO_EXTENSION_PLACEHOLDER }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "HTML", "htm, html" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "JAVA", "java" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "JAVASCRIPT", "js" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "JAR", "jar" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "JARZIP", "jar,zip" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "JSP", "jsp" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "MAKEFILE", "makefile" + NO_EXTENSION_PLACEHOLDER + ", mak" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{ "NONE", "null" }, // our own invention! //$NON-NLS-1$ //$NON-NLS-2$
			{ "PERL", "pl" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "PROFILE", "profile" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "PYTHON", "py" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "SHELL ", "csh, ksh, sh" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "SQLJ", "sqlj" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "WAR", "war" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "WEB", "css, htm, html, js, jsp" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "TAR", "tar" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "ZIP", "zip" }, //$NON-NLS-1$ //$NON-NLS-2$
	};
	protected String[] DOMAIN_NAME_STRING = new String[2];
	protected String[] DOMAIN_NEWNAME_STRING = new String[2];

	/**
	 * Constructor
	 */
	public UDActionSubsystemFiles() {
		super();
		DOMAIN_NAME_STRING[0] = SystemUDAResources.RESID_UDA_FILES_DOMAIN_FOLDER;
		DOMAIN_NAME_STRING[1] = SystemUDAResources.RESID_UDA_FILES_DOMAIN_FILE;
		DOMAIN_NEWNAME_STRING[0] = SystemUDAResources.RESID_UDA_FILES_DOMAIN_NEWFOLDER;
		DOMAIN_NEWNAME_STRING[1] = SystemUDAResources.RESID_UDA_FILES_DOMAIN_NEWFILE;
	}

	/**
	 * Overridable extension point for child classes to do migration of their actions.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done... we return false
	 */
	protected boolean doActionsMigration(ISystemProfile profile, String oldRelease) {
		return false;
	}

	/**
	 * Overridable extension point for child classes to do migration of their types.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done... we return false
	 */
	protected boolean doTypesMigration(ISystemProfile profile, String oldRelease) {
		return false;
	}

	/**
	 * Required override of parent for doing substitutions for our unique variables.
	 */
	public String internalGetSubstitutionValue(SystemUDActionElement currentAction, String subvar, Object selectedObject) {
		return getUniversalSubstitutionValue(currentAction, subvar, selectedObject);
	}

	/**
	 * Required override of parent for doing substitutions for our unique variables.
	 */
	public static String getUniversalSubstitutionValue(SystemUDActionElement currentAction, String subvar, Object selectedObject) {
		/* from resource property file...
		 Common to files and folders:
		 ...uda.files.subvar.resource_date = Last modified date of selected resource
		 ...uda.files.subvar.resource_name = Name of selected resource, unqualified
		 ...uda.files.subvar.resource_path = Path of selected resource, including name
		 ...uda.files.subvar.resource_path_root=Root of selected file's path. "c:\\" on Windows, or "/" on others
		 ...uda.files.subvar.resource_path_drive=Drive letter on Windows, empty string on others
		 ...uda.files.subvar.container_name=Name of folder containing selected resource, unqualified
		 ...uda.files.subvar.container_path=Path of folder containing selected resource, including name
		 // note: resource_name and resource_path handled for us in parent class!       
		 File specific:
		 ...uda.files.subvar.resource_name_root=Name of selected resource without the extension
		 ...uda.files.subvar.resource_name_ext=Extension part of the name of the selected resource
		 */
		IRemoteFile selectedFile = (IRemoteFile) selectedObject;
		if (subvar.equals("${resource_date}")) //$NON-NLS-1$
		{
			if (dateFormatter == null) dateFormatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss z"); //new SimpleDateFormat(); //$NON-NLS-1$
			Date lmd = selectedFile.getLastModifiedDate();
			if (lmd != null)
				return dateFormatter.format(lmd);
			else
				return "not available"; //$NON-NLS-1$
		} else if (subvar.equals("${resource_name_root}")) //$NON-NLS-1$
		{
			String name = selectedFile.getName();
			int dotIdx = name.lastIndexOf('.');
			if (dotIdx == 0)
				return ""; //$NON-NLS-1$
			else if (dotIdx > 0)
				return name.substring(0, dotIdx);
			else
				return name;
		} else if (subvar.equals("${resource_name_ext}")) //$NON-NLS-1$
		{
			String name = selectedFile.getName();
			int dotIdx = name.lastIndexOf('.');
			if (dotIdx == 0) {
				if (name.length() == 1)
					return ""; //$NON-NLS-1$
				else
					return name.substring(1);
			} else if (dotIdx > 0)
				return name.substring(dotIdx + 1);
			else
				return ""; //$NON-NLS-1$
		}
		/*
		 else if (subvar.equals("${path}"))
		 {
		 String p = selectedFile.getParentNoRoot();
		 if (p != null)
		 return p;
		 else
		 return "";
		 }
		 */
		else if (subvar.equals("${container_name}")) //$NON-NLS-1$
		{
			// another example where the info can't be trusted!
			/*
			 String fn = selectedFile.getParentName();
			 if (fn != null)
			 return fn;
			 else
			 return "";
			 */
			String fullpath = selectedFile.getParentPath();
			if ((fullpath == null) || (fullpath.length() == 0)) return ""; //$NON-NLS-1$
			IRemoteFileSubSystem rfss = getFileSubSystem(selectedObject);
			if (rfss == null) return ""; //$NON-NLS-1$
			char sep = rfss.getParentRemoteFileSubSystemConfiguration().getSeparatorChar();
			int idx = fullpath.lastIndexOf(sep);
			if (idx >= 0)
				return fullpath.substring(idx + 1);
			else
				return ""; //$NON-NLS-1$
		} else if (subvar.equals("${container_path}")) //$NON-NLS-1$
			return selectedFile.getParentPath();
		else if (subvar.equals("${resource_path_root}")) //$NON-NLS-1$
		{
			String name = selectedFile.getAbsolutePath();
			if (name != null) {
				if (name.startsWith("/") || name.startsWith("\\")) //$NON-NLS-1$ //$NON-NLS-2$
					return name.substring(0, 1);
				else {
					int idx = name.indexOf(":\\"); //$NON-NLS-1$
					if (idx > 0) return name.substring(0, idx + 2);
				}
			}
			return ""; //$NON-NLS-1$
		} else if (subvar.equals("${resource_path_drive}")) //$NON-NLS-1$
		{
			//String root = selectedFile.getRoot();
			String name = selectedFile.getAbsolutePath();
			if ((name != null) && (name.length() > 1)) {
				int idx = name.indexOf(':');
				if (idx > 0) return name.substring(0, idx);
			}
			return ""; //$NON-NLS-1$
		}
		/* now handled in common base class
		 else if (subvar.equals("${system_pathsep}"))
		 return selectedFile.getSeparator();
		 */
		return null; // return null to tell parent we didn't do any substitutions!
	}

	/**
	 * Get the delimiter used to delimiter the types in a type string.
	 * Default is " "
	 */
	protected String getTypesDelimiter() {
		return ","; //$NON-NLS-1$
	}

	/**
	 * After an action's command has been resolved (vars substituted) this method
	 * is called to actually do the remote command execution
	 * <p>
	 * Run the user action's command in the default shell, and 
	 *  log result in the command view.
	 * 
	 * @param shell - the shell to use if need to prompt for password or show msg dialog
	 * @param action - the action being processed, in case attributes of it need to be queried
	 * @param cmdString - the resolved command
	 * @param cmdSubSystem - this connection's command subsystem, which will run the command
	 * @param context - the selected IRemoteFile object
	 * @param viewer the viewer that originated the compile action
	 * @return true if we should continue, false if something went wrong
	 */
	protected boolean runCommand(Shell shell, SystemUDActionElement action, String cmdString, IRemoteCmdSubSystem cmdSubSystem, Object context, Viewer viewer) {
		return runUniversalCommand(shell, cmdString, cmdSubSystem, context);
	}

	/**
	 * Encapsulation of code needed to run a universal subsystem command.
	 * 
	 * @param shell - the shell to use if need to prompt for password or show msg dialog
	 * @param cmdString - the resolved command
	 * @param cmdSubSystem - this connection's command subsystem, which will run the command
	 * @param context - the selected IRemoteFile object
	 * @return true if we should continue, false if something went wrong
	 */
	public static boolean runUniversalCommand(Shell shell, String cmdString, IRemoteCmdSubSystem cmdSubSystem, Object context) {
		String path = RemoteCommandHelpers.getWorkingDirectory((IRemoteFile) context);
		boolean ok = RemoteCommandHelpers.runUniversalCommand(shell, cmdString, path, cmdSubSystem);
		return ok;
	} // end method	 

	// -----------------------------------------------
	// OVERRIDDEN METHODS FOR CAPABILITY DEFINITION
	// -----------------------------------------------
	/**
	 * Return true if actions can be scoped by file types
	 * The iSeries native file system does support types
	 */
	public boolean supportsTypes() {
		return true;
	}

	/**
	 * Return true if actions can be scoped by file types for the given domain.
	 * Default is supportsTypes()
	 */
	public boolean supportsTypes(int domain) {
		if (domain == DOMAIN_FOLDER)
			return false;
		else
			return true;
	}

	/**
	 * Return true if the action/type manager supports domains.
	 * The iSeries native file system does support domains
	 */
	public boolean supportsDomains() {
		return true;
	}

	/**
	 * In some cases, we supports domains in general, but only want to expose
	 *  one of those domains to the user. For example, for file subsystems,
	 *  we support folder and file domains, but for named types we really only
	 *  support the file domain.
	 * <p>
	 * We return DOMAIN_FILE if the docManager is the type manager
	 */
	public int getSingleDomain(SystemUDBaseManager docManager) {
		if (docManager != getUDTypeManager())
			return -1;
		else
			return DOMAIN_FILE;
	}

	// --------------------
	// VARIOUS OVERRIDES...
	// --------------------
	/** 
	 * Subclasses may override to provide a custom type edit pane subclass 
	 */
	public SystemUDTypeEditPane getCustomUDTypeEditPane(ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		return new UDTypesEditPaneFiles(this, parent, tv);
	}

	/**
	 * Prime new document with default types. This adds all the types that common across all system types,
	 *  but also allows unique types to be added per system by calling primeAdditionalDefaultUniversalTypes,
	 *  which is a method that subclasses can override.
	 * <p>
	 * Do no override this method, but instead override primeAdditionalDefaultUniversalTypes.
	 */
	public SystemUDTypeElement[] primeDefaultTypes(SystemUDTypeManager typeMgr) {
		Vector v = new Vector();
		primeDefaultUniversalTypes(typeMgr, v);
		// give subclasses a chance...
		primeAdditionalDefaultUniversalTypes(typeMgr, v);
		// convert vector to array
		SystemUDTypeElement[] typesArray = new SystemUDTypeElement[v.size()];
		for (int idx = 0; idx < typesArray.length; idx++)
			typesArray[idx] = (SystemUDTypeElement) v.elementAt(idx);
		return typesArray;
	}

	/**
	 * Static version of primeDefaultTypes that is called by the iSeries IFS action subsystem.
	 * @param typeMgr - the manager of the types document to be primed
	 * @param vectorOfTypes - the vector to populate with types. Can be null, in which case the results are returned as an array
	 * @return null if given a vector (it is populated), else the array of default types
	 */
	public static SystemUDTypeElement[] primeDefaultUniversalTypes(SystemUDTypeManager typeMgr, Vector vectorOfTypes) {
		Vector v = vectorOfTypes;
		if (v == null) v = new Vector();
		for (int i = 0; i < IBM_DEFAULT_FOLDERTYPES.length; i++) {
			SystemUDTypeElement ft = typeMgr.addType(DOMAIN_FOLDER, IBM_DEFAULT_FOLDERTYPES[i][0]);
			if (null == ft) continue;
			v.addElement(ft);
			ft.setTypes(IBM_DEFAULT_FOLDERTYPES[i][1]);
		}
		for (int i = 0; i < IBM_DEFAULT_FILETYPES.length; i++) {
			SystemUDTypeElement ft = typeMgr.addType(DOMAIN_FILE, IBM_DEFAULT_FILETYPES[i][0]);
			if (null == ft) continue;
			v.addElement(ft);
			ft.setTypes(IBM_DEFAULT_FILETYPES[i][1]);
		}
		if (vectorOfTypes != null) return null;
		// convert vector to array
		SystemUDTypeElement[] typesArray = new SystemUDTypeElement[v.size()];
		for (int idx = 0; idx < typesArray.length; idx++)
			typesArray[idx] = (SystemUDTypeElement) v.elementAt(idx);
		return typesArray;
	}

	/**
	 * This is an override point for subclasses to add system-specify default types.
	 * <p>
	 * To simplify processing, subclasses should add their additional SystemUDTypeElement 
	 *  objects to the given vector.
	 */
	protected void primeAdditionalDefaultUniversalTypes(SystemUDTypeManager typeMgr, Vector vectorOfTypes) {
	}

	/**
	 * Prime new document with default action. This adds all the actions that common across all system types,
	 *  but also allows unique actions to be added per system by calling primeAdditionalDefaultUniversalActions,
	 *  which is a method that subclasses can override.
	 * <p>
	 * Do no override this method, but instead override primeAdditionalDefaultUniversalActions.
	 */
	public SystemUDActionElement[] primeDefaultActions(SystemUDActionManager actionMgr, ISystemProfile profile) {
		Vector v = new Vector();
		primeDefaultUniversalActions(actionMgr, profile, v);
		// give subclasses a chance...
		primeAdditionalDefaultUniversalActions(actionMgr, profile, v);
		// convert vector to array
		SystemUDActionElement[] actionArray = new SystemUDActionElement[v.size()];
		for (int idx = 0; idx < actionArray.length; idx++)
			actionArray[idx] = (SystemUDActionElement) v.elementAt(idx);
		return actionArray;
	}

	/**
	 * Static version of primeDefaultActions that is called by the iSeries IFS action subsystem.
	 * @param actionMgr - the manager of the actions document to be primed
	 * @param vectorOfActions - the vector to populate with actions. Can be null, in which case the results are returned as an array
	 * @return null if given a vector (it is populated), else the array of default actions
	 */
	public static SystemUDActionElement[] primeDefaultUniversalActions(SystemUDActionManager actionMgr, ISystemProfile profile, Vector vectorOfActions) {
		Vector v = vectorOfActions;
		if (v == null) v = new Vector();
		String osType = actionMgr.getActionSubSystem().getOSType();
		String userDefinedActionPropertySetName = ISystemUDAConstants.USER_DEFINED_ACTION_PROPRERTY_SET_PREFIX + osType + "." + actionMgr.getDocumentRootTagName(); //$NON-NLS-1$
		IPropertySet userDefinedActionPropertySet = profile.getPropertySet(userDefinedActionPropertySetName);
		if (null == userDefinedActionPropertySet)
		{
			userDefinedActionPropertySet = profile.createPropertySet(userDefinedActionPropertySetName);
			userDefinedActionPropertySet.addProperty(ISystemUDAConstants.NAME_ATTR, ISystemUDAConstants.USER_DEFINED_ACTION_PROPRERTY_SET_Name + " - " + osType); //$NON-NLS-1$
			userDefinedActionPropertySet.addProperty(ISystemUDAConstants.RELEASE_ATTR, ISystemUDAConstants.RELEASE_VALUE);
			userDefinedActionPropertySet.addProperty(ISystemUDAConstants.UDA_ROOT_ATTR, actionMgr.getDocumentRootTagName());
		}
		// add file actions
		int domain = DOMAIN_FILE;
		SystemUDActionElement newAction;
		//IPropertySet domainFilePropertySet = userDefinedActionPropertySet.createPropertySet(DOMAINS[1]);
		for (int idx = 0; idx < FILE_ACTIONS.length; idx++) {
			newAction = actionMgr.addAction(profile, FILE_ACTIONS[idx][0], domain);
			v.addElement(newAction);
			newAction.setCommand(FILE_ACTIONS[idx][5]);
			newAction.setPrompt(true); // may as well always allow users chance to change command as its submitted
			newAction.setRefresh(FILE_ACTIONS[idx][1].equals("true")); //$NON-NLS-1$
			newAction.setShow(true);
			newAction.setSingleSelection(FILE_ACTIONS[idx][2].equals("true")); //$NON-NLS-1$
			newAction.setCollect(FILE_ACTIONS[idx][3].equals("true")); //$NON-NLS-1$
			newAction.setFileTypes(convertStringToArray(FILE_ACTIONS[idx][4]));
		}
		// add folder actions
		domain = DOMAIN_FOLDER;
		for (int idx = 0; idx < FOLDER_ACTIONS.length; idx++) {
			newAction = actionMgr.addAction(profile, FOLDER_ACTIONS[idx][0], domain);
			v.addElement(newAction);
			newAction.setCommand(FOLDER_ACTIONS[idx][4]);
			newAction.setPrompt(true); // may as well always allow users chance to change command as its submitted
			newAction.setRefresh(FOLDER_ACTIONS[idx][1].equals("true")); //$NON-NLS-1$
			newAction.setShow(true);
			newAction.setSingleSelection(FOLDER_ACTIONS[idx][2].equals("true")); //$NON-NLS-1$
			newAction.setCollect(FOLDER_ACTIONS[idx][3].equals("true")); //$NON-NLS-1$
			newAction.setFileTypes(new String[] { "ALL" }); //$NON-NLS-1$
		}
		if (vectorOfActions != null) return null;
		// convert vector to array...
		SystemUDActionElement[] actionArray = new SystemUDActionElement[v.size()];
		for (int idx = 0; idx < actionArray.length; idx++)
			actionArray[idx] = (SystemUDActionElement) v.elementAt(idx);
		return actionArray;
	}

	/**
	 * This is an override point for subclasses to add system-specify default actions.
	 * <p>
	 * To simplify processing, subclasses should add their additional SystemUDActionElement 
	 *  objects to the given vector.
	 */
	protected void primeAdditionalDefaultUniversalActions(SystemUDActionManager actionMgr, ISystemProfile profile, Vector vectorOfActions) {
	}

	/**
	 * Given this IBM-supplied named type, restore it to its IBM-supplied state
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	public boolean restoreDefaultType(SystemUDTypeElement element, int domain, String typeName) {
		boolean ok = restoreUniversalDefaultType(element, domain, typeName);
		if (!ok) ok = restoreAdditionalDefaultType(element, domain, typeName);
		return ok;
	}

	/**
	 * Given this IBM-supplied named type, restore it to its IBM-supplied state.
	 * This concrete method tests the commonly supplied universal types.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	public static boolean restoreUniversalDefaultType(SystemUDTypeElement element, int domain, String typeName) {
		boolean ok = false;
		String[][] typesArray = IBM_DEFAULT_FILETYPES;
		if (domain == DOMAIN_FOLDER) // no IBM types for folder.
			typesArray = IBM_DEFAULT_FOLDERTYPES;
		// first test the universal types...
		int match = -1;
		for (int idx = 0; (match == -1) && (idx < typesArray.length); idx++) {
			if (typeName.equals(typesArray[idx][0])) match = idx;
		}
		if (match != -1) {
			element.setName(typesArray[match][0]);
			element.setTypes(typesArray[match][1]);
			ok = true;
		}
		return ok;
	}

	/**
	 * Given this IBM-supplied named type, restore it to its IBM-supplied state.
	 * This abstract method is for the subclasses.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	protected boolean restoreAdditionalDefaultType(SystemUDTypeElement element, int domain, String typeName) {
		return false;
	}

	/**
	 * Given this IBM-supplied named action, restore it to its IBM-supplied state
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	public boolean restoreDefaultAction(SystemUDActionElement element, int domain, String typeName) {
		boolean ok = restoreUniversalDefaultAction(element, domain, typeName);
		if (!ok) ok = restoreAdditionalDefaultAction(element, domain, typeName);
		return ok;
	}

	/**
	 * Given this IBM-supplied named action, restore it to its IBM-supplied state.
	 * This concrete method tests the commonly supplied universal actions.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	public static boolean restoreUniversalDefaultAction(SystemUDActionElement element, int domain, String actionName) {
		boolean ok = false;
		String[][] actionsArray = FILE_ACTIONS;
		if (domain == DOMAIN_FOLDER) // no IBM types for folder.
			actionsArray = FOLDER_ACTIONS;
		// first test the universal types...
		int match = -1;
		for (int idx = 0; (match == -1) && (idx < actionsArray.length); idx++) {
			if (actionName.equals(actionsArray[idx][0])) match = idx;
		}
		if (match != -1) {
			element.setName(actionsArray[match][0]);
			element.setPrompt(true); // may as well always allow users chance to change command as its submitted
			element.setRefresh(actionsArray[match][1].equals("true")); //$NON-NLS-1$
			element.setShow(true);
			element.setSingleSelection(actionsArray[match][2].equals("true")); //$NON-NLS-1$
			element.setCollect(actionsArray[match][3].equals("true")); //$NON-NLS-1$
			if (domain == DOMAIN_FOLDER) {
				element.setFileTypes(new String[] { "ALL" }); //$NON-NLS-1$
				element.setCommand(actionsArray[match][4]);
			} else {
				element.setFileTypes(convertStringToArray(actionsArray[match][4]));
				element.setCommand(actionsArray[match][5]);
			}
			ok = true;
		}
		return ok;
	}

	/**
	 * Given this IBM-supplied named action, restore it to its IBM-supplied state.
	 * This abstract method is for the subclasses.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	protected boolean restoreAdditionalDefaultAction(SystemUDActionElement element, int domain, String actionName) {
		return false;
	}

	/**
	 * Convert space-delimited string into array of strings
	 */
	protected static String[] convertStringToArray(String string) {
		StringTokenizer tokens = new StringTokenizer(string);
		Vector v = new Vector();
		while (tokens.hasMoreTokens()) {
			v.addElement(tokens.nextToken());
		}
		String[] strings = new String[v.size()];
		for (int idx = 0; idx < strings.length; idx++)
			strings[idx] = (String) v.elementAt(idx);
		return strings;
	}

	/**
	 * We disable user defined actions if we are in work-offline mode.
	 * Currently, how we determine this is dependent on the subsystem factory.
	 */
	public boolean getWorkingOfflineMode() {
		return false; // todo: change this when offline mode is supported for universal!
	}

	/**
	 * Return the list of substitution variables for the given domain type.
	 * Called from edit pane in work with dialog.
	 * This must be overridden!
	 */
	public SystemCmdSubstVarList getActionSubstVarList(int actionDomainType) {
		if (actionDomainType == DOMAIN_FOLDER)
			return UDSubstListFolders.getInstance();
		else if (actionDomainType == DOMAIN_FILE)
			return UDSubstListFiles.getInstance();
		else
			return null;
	}

	/**
	 * Given an action, and the currently selected remote objects, and the domain of those,
	 *  return true if ALL of the selected remote objects matches any of the type criteria 
	 *  for this action
	 * <p>
	 * Override of parent
	 */
	protected boolean meetsSelection(SystemUDActionElement action, IStructuredSelection selection, int domainType) {
		if (domainType == DOMAIN_FOLDER) return true; // no point in further testing because our getDomainFromSelection method already did this   	 
		String unresolvedActionTypes[] = action.getFileTypes();
		// fastpath for "ALL"!
		if ((unresolvedActionTypes == null) || (unresolvedActionTypes.length == 0))
			return true; // what else to do?
		else if (unresolvedActionTypes[0].equals("ALL")) //$NON-NLS-1$
			return true;
		// flatten types
		String[] actionTypes = resolveTypes(unresolvedActionTypes, domainType);
		// create file type matcher
		fileTypeMatcher = null;
		if (domainType == DOMAIN_FILE) {
			if (fileTypeMatcher == null) fileTypeMatcher = new FileTypeMatcher(null, getSubsystem().getSubSystemConfiguration().isCaseSensitive());
			fileTypeMatcher.setTypesAndNames(actionTypes);
		}
		Iterator elements = selection.iterator();
		Object element = null;
		while (elements.hasNext()) {
			element = elements.next();
			IRemoteFile file = (IRemoteFile) element;
			// OK if matches any one of the file types for an action
			boolean foundMatch = false;
			if (domainType == DOMAIN_FOLDER) {
				if (file.isDirectory()) foundMatch = true;
			} else {
				if (fileTypeMatcher.matches(file.getName())) foundMatch = true;
			}
			if (!foundMatch) return false;
		}
		return true;
	}

	/**
	 * Parent override.
	 * <p>
	 * Compares a particular file type (not named, but actual scalar/generic type)
	 *  to a specific user-selected remote object.
	 * Returns true if the object's information matches that of the given type
	 * <p>
	 * BECAUSE WE OVERRRIDE meetsSelection, THIS METHOD IS NEVER CALLED FOR US!
	 * 
	 * @param actionType - an unnamed file type, as in "*.cpp"
	 * @param selectedObject - one of the currently selected remote objects
	 * @param domainType - integer representation of current domain
	 */
	protected boolean isMatch(Object actionType, Object selectedObject, int domainType) {
		return true;
	}

	// -----------------------------------------------
	// OVERRIDDEN METHODS RELATED TO DOMAIN SUPPORT...
	// -----------------------------------------------
	/**
	 * Parent override.
	 * Determine domain, given the selection.
	 * Eg subsystem that supports domains has to do this via overriding this method.
	 * If domains not supported, return -1.
	 */
	protected int getDomainFromSelection(IStructuredSelection selection) {
		int domain = -1;
		Iterator elements = selection.iterator();
		if (elements.hasNext()) {
			IRemoteFile currFile = (IRemoteFile) elements.next();
			if (currFile.isDirectory())
				domain = DOMAIN_FOLDER;
			else
				domain = DOMAIN_FILE;
		}
		return domain;
	}

	/**
	 * Parent override.
	 * For efficiency reasons, internally we use an integer to represent a domain.
	 * However, that has to be mapped to a name which is actually what is stored as the
	 *  "type" attribute for the xml domain node.
	 * This returns the maximum integer number supported by this action/type manager.
	 * Returns 1 for us.
	 */
	public int getMaximumDomain() {
		return 1;
	}

	/**
	 * Get the list of untranslated domain names
	 */
	public String[] getDomainNames() {
		return DOMAINS;
	}

	/**
	 * Get the list of translated domain names
	 */
	public String[] getXlatedDomainNames() {
		return DOMAIN_NAME_STRING;
	}

	/**
	 * Get the list of translated domain names for the tree view's "New" nodes
	 */
	public String[] getXlatedDomainNewNames() {
		return DOMAIN_NEWNAME_STRING;
	}

	/**
	 * Get the list of translated domain names for "new" nodes in tree view, for the WW Named Types dialog
	 */
	public String[] getXlatedDomainNewTypeNames() {
		return DOMAIN_NEWNAME_STRING;
		//return DOMAIN_NEWTYPENAME_STRING; // SHOULD NEVER BE CALLED
	}

	/**
	 * Get the domain icon to show in the tree views
	 */
	public Image getDomainImage(int domain) {
		if (domain == DOMAIN_FOLDER)
			return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_FOLDER_ID);
		//return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		else if (domain == DOMAIN_FILE)
		//return RSEUIPlugin.getDefault().getImage(ISystemConstants.ICON_SYSTEM_FILE_ID);
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		return null;
	}

	/**
	 * Get the domain icon to show in the tree views, for the new item for this domain
	 */
	public Image getDomainNewImage(int domain) {
		if (domain == DOMAIN_FOLDER)
			return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_NEWFOLDER_ID);
		//return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		else if (domain == DOMAIN_FILE) return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_NEWFILE_ID);
		//return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		return null;
	}

	/**
	 * Get the domain icon to show in the named type tree view, for the new item for this domain
	 */
	public Image getDomainNewTypeImage(int domain) {
		return UserActionsIcon.USERTYPE_NEW.getImage();
	}

	/**
	 * Overridable method for child classes to supply the label to display in the 
	 *  "New" node for types. Typically only overriden if domains are not supported, as otherwise
	 *  the child nodes of "New" have the specific labels.   
	 * @return translated label for "New named type..."
	 */
	protected String getNewNodeTypeLabel() {
		return SystemUDAResources.RESID_UDA_FILES_NEWNODE_LABEL;
	}
}
