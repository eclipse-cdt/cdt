/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.actions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @since 2.0
 */
public class UpdateManagedProjectAction implements IWorkbenchWindowActionDelegate {
	
	
	private static final String ID_CYGWIN = "cygwin";	//$NON-NLS-1$
	private static final String ID_DEBUG = "debug";	//$NON-NLS-1$
	private static final String ID_DIRS = "dirs";	//$NON-NLS-1$
	private static final String ID_EXE = "exe";	//$NON-NLS-1$
	private static final String ID_EXEC = "exec";	//$NON-NLS-1$
	private static final String ID_GENERAL = "general";	//$NON-NLS-1$
	private static final String ID_GNU = "gnu";	//$NON-NLS-1$
	private static final String ID_INCPATHS = "incpaths";	//$NON-NLS-1$
	private static final String ID_INCLUDE = "include";	//$NON-NLS-1$
	private static final String ID_LINUX = "linux";	//$NON-NLS-1$
	private static final String ID_OPTION = "option";	//$NON-NLS-1$
	private static final String ID_OPTIONS = "options";	//$NON-NLS-1$
	private static final String ID_PATHS = "paths";	//$NON-NLS-1$
	private static final String ID_PREPROC = "preprocessor";	//$NON-NLS-1$
	private static final String ID_RELEASE = "release";	//$NON-NLS-1$
	private static final String ID_SEPARATOR = ".";	//$NON-NLS-1$
	private static final String ID_SHARED = "so";	//$NON-NLS-1$
	private static final String ID_SOLARIS = "solaris";	//$NON-NLS-1$
	private static final String ID_STATIC = "lib";	//$NON-NLS-1$
	private static final String NEW_CONFIG_ROOT = "cdt.managedbuild.config.gnu";	//$NON-NLS-1$
	private static final String NEW_CYGWIN_TARGET_ROOT = "cdt.managedbuild.target.gnu.cygwin";	//$NON-NLS-1$
	private static final String NEW_POSIX_TARGET_ROOT = "cdt.managedbuild.target.gnu";	//$NON-NLS-1$
	private static final String NEW_TOOL_ROOT = "cdt.managedbuild.tool.gnu";	//$NON-NLS-1$
	private static final String TOOL_LANG_BOTH = "both";	//$NON-NLS-1$
	private static final String TOOL_LANG_C = "c";	//$NON-NLS-1$
	private static final String TOOL_LANG_CPP = "cpp";	//$NON-NLS-1$
	private static final String TOOL_NAME_AR = "ar";	//$NON-NLS-1$	
	private static final String TOOL_NAME_ARCHIVER = "archiver";	//$NON-NLS-1$
	private static final String TOOL_NAME_COMPILER = "compiler";	//$NON-NLS-1$
	private static final String TOOL_NAME_LIB = "lib";	//$NON-NLS-1$
	private static final String TOOL_NAME_LINK = "link";	//$NON-NLS-1$
	private static final String TOOL_NAME_LINKER = "linker";	//$NON-NLS-1$
	private static final String TOOL_NAME_SOLINK = "solink";	//$NON-NLS-1$
	private static final int TOOL_TYPE_COMPILER = 0;
	private static final int TOOL_TYPE_LINKER = 1;
	private static final int TOOL_TYPE_ARCHIVER = 2;
	private static final int TYPE_EXE = 0;
	private static final int TYPE_SHARED = 1;
	private static final int TYPE_STATIC = 2;
	
	/* (non-Javadoc)
	 * Create a back-up file containing the pre-2.0 project settings. 
	 * 
	 * @param settingsFile
	 * @param monitor
	 * @param project
	 * @throws CoreException
	 */
	protected static void backupFile(IFile settingsFile, IProgressMonitor monitor, IProject project) throws CoreException {
		// Make a back-up of the settings file
		String newName = settingsFile.getName() + "_12backup";	//$NON-NLS-1$
		IContainer destFolder = (IContainer)project;
		IFile backupFile = destFolder.getFile(new Path(newName)); 
		if (backupFile.exists()) {
			Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
			boolean shouldUpdate = MessageDialog.openQuestion(shell,
					ManagedBuilderUIMessages.getResourceString("ManagedBuildConvert.12x.warning.title"), //$NON-NLS-1$
					ManagedBuilderUIMessages.getFormattedString("ManagedBuildConvert.12x.warning.message", project.getName())); //$NON-NLS-1$
			if (shouldUpdate) {
				backupFile.delete(true, monitor);
			} else {
				monitor.setCanceled(true);
				throw new OperationCanceledException(ManagedBuilderUIMessages.getFormattedString("ManagedBuildConvert.12x.cancelled.message", project.getName())); //$NON-NLS-1$
			}
		}
		settingsFile.copy(backupFile.getFullPath(), true, monitor);
	}
	
	protected static void convertConfiguration(ITarget newTarget, ITarget newParent, Element oldConfig, IProgressMonitor monitor) {
		IConfiguration newParentConfig = null;
		IConfiguration newConfig = null;
		boolean cygwin = false;
		boolean debug = false;
		int type = -1;
		
		// Figure out what the original parent of the config is
		String parentId = oldConfig.getAttribute(IConfiguration.PARENT);
		StringTokenizer idTokens = new StringTokenizer(parentId, ID_SEPARATOR);
		while (idTokens.hasMoreTokens()) {
			String id = idTokens.nextToken();
			if (id.equalsIgnoreCase(ID_CYGWIN)) {
				cygwin = true;
			} else if(id.equalsIgnoreCase(ID_EXEC)) {
				type = TYPE_EXE;
			} else if(id.equalsIgnoreCase(ID_SHARED)) {
				type = TYPE_SHARED;
			} else if (id.equalsIgnoreCase(ID_STATIC)) {
				type = TYPE_STATIC;
			} else if (id.equalsIgnoreCase(ID_DEBUG)) {
				debug = true;
			}
		}
		String defId = NEW_CONFIG_ROOT + ID_SEPARATOR;
		if (cygwin) defId += ID_CYGWIN + ID_SEPARATOR; 
		switch (type) {
			case TYPE_EXE:
				defId += ID_EXE;
				break;
			case TYPE_SHARED : 
				defId += ID_SHARED;
				break;
			case TYPE_STATIC :
				defId += ID_STATIC;
				break;
		}
		defId += ID_SEPARATOR + (debug ? "debug" : "release"); //$NON-NLS-1$ //$NON-NLS-2$
		newParentConfig = newParent.getConfiguration(defId);
		if (newParentConfig == null) {
			// Create a default gnu exe release or debug
		}		
		// Generate a random number for the new config id
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		int randomElement = rand.nextInt();
		if (randomElement < 0) {
			randomElement *= -1;
		}
		// Create the new configuration
		newConfig = newTarget.createConfiguration(newParentConfig, defId + ID_SEPARATOR + randomElement);
		
		// Convert the tool references
		NodeList toolRefNodes = oldConfig.getElementsByTagName(IConfiguration.TOOLREF_ELEMENT_NAME);
		for (int refIndex = 0; refIndex < toolRefNodes.getLength(); ++refIndex) {
			convertToolRef(newConfig, (Element) toolRefNodes.item(refIndex), monitor);
		}
		monitor.worked(1);
	}
	
	protected static void convertOptionRef(IConfiguration newConfig, ITool newTool, Element optRef) {
		String optId = optRef.getAttribute(IOption.ID);
		if (optId == null) return;
		String[] idTokens = optId.split("\\.");	//$NON-NLS-1$
		
		// New ID will be in for gnu.[compiler|link|lib].[c|c++|both].option.{1.2_component}
		Vector newIdVector = new Vector(idTokens.length + 2);
		
		// We can ignore the first element of the old IDs since it is just [cygwin|linux|solaris]
		for (int index = 1; index < idTokens.length; ++index) {
			newIdVector.add(idTokens[index]);
		}
 		
		// In the case of some Cygwin C++ tools, the old ID will be missing gnu
		if (!((String)newIdVector.firstElement()).equals(ID_GNU)) {
			newIdVector.add(0, ID_GNU);
		}
		
		// In some old IDs the language specifier is missing for librarian and C++ options
		String langToken = (String)newIdVector.get(1); 
		if(!langToken.equals(TOOL_LANG_C)) {
			// In the case of the librarian the language must b set to both
			if (langToken.equals(TOOL_NAME_LIB) || langToken.equals(TOOL_NAME_AR)) {
				newIdVector.add(1, TOOL_LANG_BOTH);
			} else {
				newIdVector.add(1, TOOL_LANG_CPP);
			}
		}
		
		// Standardize the next token to compiler, link, or lib
		String toolToken = (String)newIdVector.get(2);
		if (toolToken.equals(ID_PREPROC)) {
			// Some compiler preprocessor options are missing this
			newIdVector.add(2, TOOL_NAME_COMPILER);
		} else if (toolToken.equals(TOOL_NAME_LINKER) || toolToken.equals(TOOL_NAME_SOLINK)) {
			// Some linker options have linker or solink as the toolname
			newIdVector.remove(2);
			newIdVector.add(2, TOOL_NAME_LINK);
		} else if (toolToken.equals(TOOL_NAME_AR)) {
			// The cygwin librarian uses ar
			newIdVector.remove(2);
			newIdVector.add(2, TOOL_NAME_LIB);			
		}
		
		// Add in the option tag
		String optionToken = (String)newIdVector.get(3);
		if (optionToken.equals(ID_OPTIONS)) {
			// Some old-style options had "options" in the id
			newIdVector.remove(3);
		}
		newIdVector.add(3, ID_OPTION);
		
		// Convert any lingering "incpaths" to "include.paths"
		String badToken = (String) newIdVector.lastElement();
		if (badToken.equals(ID_INCPATHS)) {
			newIdVector.addElement(ID_INCLUDE);
			newIdVector.addElement(ID_PATHS);
		}

		// Edit out the "general" or "dirs" categories that may be in some older IDs
		int generalIndex = newIdVector.indexOf(ID_GENERAL);
		if (generalIndex != -1) {
			newIdVector.remove(generalIndex);
		}
		int dirIndex = newIdVector.indexOf(ID_DIRS);
		if (dirIndex != -1) {
			newIdVector.remove(dirIndex);
		}
		
		// Construct the new ID
		String newOptionId = new String();
		for (int rebuildIndex = 0; rebuildIndex < newIdVector.size(); ++ rebuildIndex) {
			String token = (String) newIdVector.get(rebuildIndex);
			newOptionId += token;
			if (rebuildIndex < newIdVector.size() - 1) {
				newOptionId += ID_SEPARATOR;
			}
		}
		
		// Get the option from the new tool
		IOption newOpt = newTool.getOptionById(newOptionId);
		if (newOpt == null) {
			// TODO flag warning condition to user
			return;
		}
		try {
			switch (newOpt.getValueType()) {
				case IOption.BOOLEAN:
					Boolean bool = new Boolean(optRef.getAttribute(IOption.DEFAULT_VALUE));
					newConfig.setOption(newOpt, bool.booleanValue());
					break;
				case IOption.STRING:
				case IOption.ENUMERATED:
					// This is going to be the human readable form of the enumerated value
					String name = (String) optRef.getAttribute(IOption.DEFAULT_VALUE);
					// Convert it to the ID
					String idValue = newOpt.getEnumeratedId(name);
					newConfig.setOption(newOpt, idValue != null ? idValue : name);
					break;
				case IOption.STRING_LIST:
				case IOption.INCLUDE_PATH:
				case IOption.PREPROCESSOR_SYMBOLS:
				case IOption.LIBRARIES:
				case IOption.OBJECTS:
					Vector values = new Vector();
					NodeList nodes = optRef.getElementsByTagName(IOption.LIST_VALUE);
					for (int i = 0; i < nodes.getLength(); ++i) {
						Node node = nodes.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Boolean isBuiltIn = new Boolean(((Element)node).getAttribute(IOption.LIST_ITEM_BUILTIN));
							if (!isBuiltIn.booleanValue()) {
								values.add(((Element)node).getAttribute(IOption.LIST_ITEM_VALUE));
							}
						}
					}
					newConfig.setOption(newOpt, (String[])values.toArray(new String[values.size()]));
					break;
			}
		} catch (BuildException e) {
			// TODO flag error to user
			return;
		}
		
	}
	protected static ITarget convertTarget(IProject project, Element oldTarget, IProgressMonitor monitor) {
		// What we want to create
		ITarget newTarget = null;
		ITarget newParent = null;
		// The type of target we are converting from/to
		int type = -1;
		// Use the Cygwin or generic target form
		boolean posix = false;
		
		// Get the parent
		String id = oldTarget.getAttribute(ITarget.PARENT);
		
		// Figure out the new target definition to use for that type
		StringTokenizer idTokens = new StringTokenizer(id, ID_SEPARATOR);
		while (idTokens.hasMoreTokens()) {
			String token = idTokens.nextToken();
			if (token.equals(ID_LINUX) || token.equals(ID_SOLARIS)) {
				posix = true;
			} else if (token.equalsIgnoreCase(ID_EXEC)){
				type = TYPE_EXE;
			} else if (token.equalsIgnoreCase(ID_SHARED)){
				type = TYPE_SHARED;
			} else if (token.equalsIgnoreCase(ID_SHARED)){
				type = TYPE_SHARED;
			}
		}
		// Create a target based on the new target type
		String defID = (posix ? NEW_POSIX_TARGET_ROOT : NEW_CYGWIN_TARGET_ROOT) + ID_SEPARATOR;
		switch (type) { 
			case TYPE_EXE :
				defID += ID_EXE;
				break;
			case TYPE_SHARED : 
				defID += ID_SHARED;
				break;
			case TYPE_STATIC :
				defID += ID_STATIC;
				break;
		}		
		
		// Get the new target definitions we need for the conversion
		newParent = ManagedBuildManager.getTarget(project, defID);
		if (newParent == null) {
			// Return null and let the caller deal with the error reporting
			return null;
		}

		try {
			// Create a new target based on the new parent
			newTarget = ManagedBuildManager.createTarget(project, newParent);
			newTarget.setArtifactName(oldTarget.getAttribute(ITarget.ARTIFACT_NAME));
			
			// Create new configurations
			NodeList configNodes = oldTarget.getElementsByTagName(IConfiguration.CONFIGURATION_ELEMENT_NAME);
			for (int configIndex = 0; configIndex < configNodes.getLength(); ++configIndex) {
				convertConfiguration(newTarget, newParent, (Element) configNodes.item(configIndex), monitor);
			}
		} catch (BuildException e) {
			ManagedBuilderUIPlugin.logException(e);
		}
		
		monitor.worked(1);
		return newTarget;
	}
	
	protected static void convertToolRef(IConfiguration newConfig, Element oldToolRef, IProgressMonitor monitor) {
		String oldToolId = oldToolRef.getAttribute(IToolReference.ID);
		// All known tools have id NEW_TOOL_ROOT.[c|cpp].[compiler|linker|archiver]
		String newToolId = NEW_TOOL_ROOT;
		boolean cppFlag = true;
		int toolType = -1;
		
		// Figure out what kind of tool the ref pointed to
		StringTokenizer idTokens = new StringTokenizer(oldToolId, ID_SEPARATOR);
		while (idTokens.hasMoreTokens()) {
			String token = idTokens.nextToken();
			if(token.equals(TOOL_LANG_C)) {
				cppFlag = false;
			} else if (token.equalsIgnoreCase(TOOL_NAME_COMPILER)) {
				toolType = TOOL_TYPE_COMPILER;
			} else if (token.equalsIgnoreCase(TOOL_NAME_AR)) {
				toolType = TOOL_TYPE_ARCHIVER;
			} else if (token.equalsIgnoreCase(TOOL_NAME_LIB)) {
				toolType = TOOL_TYPE_ARCHIVER;
			} else if (token.equalsIgnoreCase(TOOL_NAME_LINK)) {
				toolType = TOOL_TYPE_LINKER;
			} else if (token.equalsIgnoreCase(TOOL_NAME_SOLINK)) {
				toolType = TOOL_TYPE_LINKER;
			}
		}
		
		// Now complete the new tool id
		newToolId += ID_SEPARATOR + (cppFlag ? "cpp" : "c") + ID_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
		switch (toolType) {
			case TOOL_TYPE_COMPILER:
				newToolId += TOOL_NAME_COMPILER;
				break;
			case TOOL_TYPE_LINKER:
				newToolId  += TOOL_NAME_LINKER;
				break;
			case TOOL_TYPE_ARCHIVER:
				newToolId += TOOL_NAME_ARCHIVER;
				break;
		}
		
		// Get the new tool out of the configuration
		ITool newTool = newConfig.getToolById(newToolId);
		// Check that this is not null
		
		// The ref may or may not contain overridden options
		NodeList optRefs = oldToolRef.getElementsByTagName(ITool.OPTION_REF);
		for (int refIndex = optRefs.getLength() - 1; refIndex >= 0; --refIndex) {
			convertOptionRef(newConfig, newTool, (Element) optRefs.item(refIndex));
		}
		monitor.worked(1);
	}
	
	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 * @throws CoreException
	 */
	protected static void doProjectUpdate(IProgressMonitor monitor, IProject project) throws CoreException {
		String[] projectName = new String[]{project.getName()};
		IFile settingsFile = project.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		if (!settingsFile.exists()) {
			monitor.done();
			return;
		}
		
		// Backup the file
		monitor.beginTask(ManagedBuilderUIMessages.getFormattedString("ManagedBuildConvert.12x.monitor.message.backup", projectName), 1); //$NON-NLS-1$
		backupFile(settingsFile, monitor, project);
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		
		//Now convert each target to the new format
		try {
			// Load the old build file
			InputStream stream = settingsFile.getContents();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(stream);
			
			// Clone the target based on the proper target definition
			NodeList targetNodes = document.getElementsByTagName(ITarget.TARGET_ELEMENT_NAME);
			// This is a guess, but typically the project has 1 target, 2 configs, and 6 tool defs
			int listSize = targetNodes.getLength();
			monitor.beginTask(ManagedBuilderUIMessages.getFormattedString("ManagedBuildConvert.12x.monitor.message.project", projectName), listSize * 9); //$NON-NLS-1$	
			for (int targIndex = 0; targIndex < listSize; ++targIndex) {
				Element oldTarget = (Element) targetNodes.item(targIndex);
				String oldTargetId = oldTarget.getAttribute(ITarget.ID);
				ITarget newTarget = convertTarget(project, oldTarget, monitor);
			
				// Remove the old target
				if (newTarget != null) {
					info.removeTarget(oldTargetId);
					monitor.worked(9);
				}
			}
			// Upgrade the version
			((ManagedBuildInfo)info).setVersion(ManagedBuildManager.getBuildInfoVersion().toString());
		} catch (ParserConfigurationException e) {
			ManagedBuilderUIPlugin.log(e);
		} catch (FactoryConfigurationError e) {
			ManagedBuilderUIPlugin.log(e);
		} catch (SAXException e) {
			ManagedBuilderUIPlugin.log(e);
		} catch (IOException e) {
			ManagedBuilderUIPlugin.log(e);
		} finally {
			ManagedBuildManager.saveBuildInfo(project, false);
			monitor.done();
		}
	}

	/**
	 * Determines which projects in the workspace are still using 
	 * the settings format defined in CDT 1.2.x. 
	 * 
	 * @return an array of <code>IProject</code> that need to have their  
	 * project settings updated to the CDT 2.0 format 
	 */
	public static IProject[] getVersion12Projects() {
		IProject[] projects = ManagedBuilderUIPlugin.getWorkspace().getRoot().getProjects();
		Vector result = new Vector();
		for (int index = projects.length - 1; index >=0 ; --index) {
			IProjectDescription description;
			try {
				description = projects[index].getDescription();
			} catch (CoreException e) {
				continue;
			}
			// Make sure it has a managed nature
			if (description == null || !description.hasNature(ManagedCProjectNature.MNG_NATURE_ID)) {
				continue;
			}
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projects[index]);
			if (info != null && info.getVersion()== null) {
				// This is a pre-2.0 file (no version info)
				result.add(projects[index]);
			}
		}

		return (IProject[]) result.toArray(new IProject[result.size()]);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// TODO Auto-generated method stub
		
	}

	static public void run(boolean fork, IRunnableContext context, final IProject project) {
		try {
			context.run(fork, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								doProjectUpdate(monitor, project);
							}
						};
						ManagedBuilderUIPlugin.getWorkspace().run(runnable, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} catch (OperationCanceledException e) {
						throw new InterruptedException(e.getMessage());
					}
				}
			});
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			ManagedBuilderUIPlugin.logException(e, 
					ManagedBuilderUIMessages.getResourceString("ManagedBuilderStartup.update.exception.error"),	//$NON-NLS-1$
					ManagedBuilderUIMessages.getFormattedString("ManagedBuilderStartup.update.exception.message", project.getName()));	//$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}
