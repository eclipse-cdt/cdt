/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.projectconverter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationV2;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class UpdateManagedProject12 {
	
	private static final String ID_CYGWIN = "cygwin";	//$NON-NLS-1$
	private static final String ID_DEBUG = "debug";	//$NON-NLS-1$
	private static final String ID_DIRS = "dirs";	//$NON-NLS-1$
	private static final String ID_EXE = "exe";	//$NON-NLS-1$
	private static final String ID_EXEC = "exec";	//$NON-NLS-1$
	private static final String ID_GENERAL = "general";	//$NON-NLS-1$
	private static final String ID_GNU = "gnu";	//$NON-NLS-1$
	private static final String ID_INCPATHS = "incpaths";	//$NON-NLS-1$
	private static final String ID_INCLUDE = "include";	//$NON-NLS-1$
	private static final String ID_LIBS = "libs";	//$NON-NLS-1$
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
	private static Map configIdMap;
	
	/* (non-Javadoc)
	 * Generates a valid 2.0.x eqivalent ID for an old 1.2 format
	 * configuration.
	 * 
	 * @param oldId
	 * @return
	 */
	protected static String getNewConfigurationId(String oldId){
		boolean cygwin = false;
		boolean debug = false;
		int type = -1;
		
		StringTokenizer idTokens = new StringTokenizer(oldId, ID_SEPARATOR);
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
		return defId;
	}
	
	protected static void convertConfiguration(IManagedProject newProject, IProjectType newParent, Element oldConfig, IProgressMonitor monitor) 
								throws CoreException {
		IConfiguration newParentConfig = null;
		IConfiguration newConfig = null;

		
		// Figure out what the original parent of the config is
		String parentId = oldConfig.getAttribute(IConfigurationV2.PARENT);
		parentId = getNewConfigurationId(parentId);
		
		newParentConfig = newParent.getConfiguration(parentId);
		if (newParentConfig == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject12.2",parentId), null)); //$NON-NLS-1$
		}		

		// Generate a random number for the new config id
		int randomElement = ManagedBuildManager.getRandomNumber();
		String newConfigId = parentId + ID_SEPARATOR + randomElement;

		// Create the new configuration
		newConfig = newProject.createConfiguration(newParentConfig, newConfigId);
		
		Element oldTarget = (Element)oldConfig.getParentNode();
		if(oldTarget.hasAttribute(ITarget.ARTIFACT_NAME)){
			String buildGoal = oldTarget.getAttribute(ITarget.ARTIFACT_NAME);
			// The name may be in the form <name>[.ext1[.ext2[...]]]
			String[] nameElements = buildGoal.split("\\.");	//$NON-NLS-1$
			// There had better be at least a name
			String name = null;
			try {
				name = nameElements[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				name = "default";	//$NON-NLS-1$
			}
			// Reconstruct the extension
			String extension = new String();
			for (int index = 1; index < nameElements.length; ++index) {
				extension += nameElements[index];
				if (index < nameElements.length - 1) {
					extension += ".";	//$NON-NLS-1$
				}
			}
			newConfig.setArtifactName(name);
			if (extension.length() != 0) {
				newConfig.setArtifactExtension(extension);
			}
		}
		
		// Convert the tool references
		IToolChain toolChain = newConfig.getToolChain();

		NodeList toolRefNodes = oldConfig.getElementsByTagName(IConfigurationV2.TOOLREF_ELEMENT_NAME);
		for (int refIndex = 0; refIndex < toolRefNodes.getLength(); ++refIndex) {
			try{
				convertToolRef(toolChain, (Element) toolRefNodes.item(refIndex), monitor);
			}
			catch(CoreException e){
				newProject.removeConfiguration(newConfigId);
				throw e;
			}
		}

		getConfigIdMap().put(oldConfig.getAttribute(IConfigurationV2.ID), newConfig);
		monitor.worked(1);
	}
	
	protected static String getNewOptionId(IToolChain toolChain, ITool tool, String oldId)
								throws CoreException{
		String[] idTokens = oldId.split("\\.");	//$NON-NLS-1$
		
		// New ID will be in for gnu.[c|c++|both].[compiler|link|lib].option.{1.2_component}
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
			// In the case of the librarian the language must be set to both
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
			newIdVector.remove(newIdVector.lastElement());
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
		
		// Another boundary condition to check is the case where the linker option
		// has gnu.[c|cpp].link.option.libs.paths or gnu.[c|cpp].link.option.libs.paths 
		// because the new option format does away with the libs in the second last element
		try {
			if ((newIdVector.lastElement().equals(ID_PATHS) || newIdVector.lastElement().equals(ID_LIBS)) 
					&& newIdVector.get(newIdVector.size() - 2).equals(ID_LIBS)) {
				newIdVector.remove(newIdVector.size() - 2);
			}
		} catch (NoSuchElementException e) {
			// ignore this exception
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore this exception too
		}
		
		
		// Construct the new ID
		String optId = new String();
		for (int rebuildIndex = 0; rebuildIndex < newIdVector.size(); ++ rebuildIndex) {
			String token = (String) newIdVector.get(rebuildIndex);
			optId += token;
			if (rebuildIndex < newIdVector.size() - 1) {
				optId += ID_SEPARATOR;
			}
		}
		
		IConfiguration configuration = toolChain.getParent();
		
		IOption options[] = tool.getOptions();		
		for(int i = 0; i < options.length; i++){
			IOption curOption = options[i];
			// This can be null
			IOption parent = curOption.getSuperClass();
			String curOptionId = curOption.getId();
			
			if(parent == null) {
				if (!curOptionId.equals(optId))
					continue;
			} else {
				String parentId = parent.getId();
				if(!parentId.equals(optId))
					continue;
				
			}
				
			return curOption.getId();
		}
		throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
				ConverterMessages.getFormattedString("UpdateManagedProject12.3",optId), null)); //$NON-NLS-1$
	}
	
	protected static void convertOptionRef(IToolChain toolChain, ITool tool, Element optRef) 
							throws CoreException {
		String optId = optRef.getAttribute(IOption.ID);
		if (optId == null) return;
		
		optId = getNewOptionId(toolChain, tool, optId);
		// Get the option from the new tool
		IOption newOpt = tool.getOptionById(optId);
		if (newOpt == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject12.4",optId), null)); //$NON-NLS-1$
		}

		IConfiguration configuration = toolChain.getParent();

		try {
			switch (newOpt.getValueType()) {
				case IOption.BOOLEAN:
					Boolean bool = new Boolean(optRef.getAttribute(IOption.DEFAULT_VALUE));
				configuration.setOption(tool, newOpt, bool.booleanValue());
					break;
				case IOption.STRING:
				case IOption.ENUMERATED:
					// This is going to be the human readable form of the enumerated value
					String name = (String) optRef.getAttribute(IOption.DEFAULT_VALUE);
					// Convert it to the ID
					String idValue = newOpt.getEnumeratedId(name);
					configuration.setOption(tool, newOpt, idValue != null ? idValue : name);
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
					configuration.setOption(tool, newOpt, (String[])values.toArray(new String[values.size()]));
					break;
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject12.5",e.getMessage()), e)); //$NON-NLS-1$
		}
		
	}
	
	protected static String getNewProjectId(String oldId){
		// The type of target we are converting from/to
		int type = -1;
		// Use the Cygwin or generic target form
		boolean posix = false;

		StringTokenizer idTokens = new StringTokenizer(oldId, ID_SEPARATOR);
		while (idTokens.hasMoreTokens()) {
			String token = idTokens.nextToken();
			if (token.equals(ID_LINUX) || token.equals(ID_SOLARIS)) {
				posix = true;
			} else if (token.equalsIgnoreCase(ID_EXEC)){
				type = TYPE_EXE;
			} else if (token.equalsIgnoreCase(ID_SHARED)){
				type = TYPE_SHARED;
			} else if (token.equalsIgnoreCase(ID_STATIC)){
				type = TYPE_STATIC;
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
		return defID;
	}

	protected static IManagedProject convertTarget(IProject project, Element oldTarget, IProgressMonitor monitor) throws CoreException{
		// What we want to create
		IManagedProject newProject = null;
		IProjectType newParent = null;
	
		// Get the parent
		String id = oldTarget.getAttribute(ITarget.PARENT);
		String parentID = getNewProjectId(id);

		// Get the new target definitions we need for the conversion
		newParent = ManagedBuildManager.getProjectType(parentID);

		if (newParent == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject12.6",parentID), null)); //$NON-NLS-1$
		}

		try {
			// Create a new target based on the new parent
			newProject = ManagedBuildManager.createManagedProject(project, newParent);
			
			// Create new configurations
			NodeList configNodes = oldTarget.getElementsByTagName(IConfigurationV2.CONFIGURATION_ELEMENT_NAME);
			for (int configIndex = 0; configIndex < configNodes.getLength(); ++configIndex) {
				try{
					convertConfiguration(newProject, newParent, (Element) configNodes.item(configIndex), monitor);
				}
				catch(CoreException e){
					// Keep trying
					continue;
				}
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject12.8",new String[]{newProject.getName(),e.getMessage()}), null)); //$NON-NLS-1$
		}
		
		monitor.worked(1);
		return newProject;
	}
	
	protected static String getNewToolId(IToolChain toolChain, String oldId)
								throws CoreException {
		// All known tools have id NEW_TOOL_ROOT.[c|cpp].[compiler|linker|archiver]
		String toolId = NEW_TOOL_ROOT;
		boolean cppFlag = true;
		int toolType = -1;
		
		// Figure out what kind of tool the ref pointed to
		StringTokenizer idTokens = new StringTokenizer(oldId, ID_SEPARATOR);
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
		toolId += ID_SEPARATOR + (cppFlag ? "cpp" : "c") + ID_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
		switch (toolType) {
			case TOOL_TYPE_COMPILER:
				toolId += TOOL_NAME_COMPILER;
				break;
			case TOOL_TYPE_LINKER:
				toolId  += TOOL_NAME_LINKER;
				break;
			case TOOL_TYPE_ARCHIVER:
				toolId += TOOL_NAME_ARCHIVER;
				break;
		}
		
		IConfiguration configuration = toolChain.getParent();
		ITool tools[] = configuration.getTools();
		if(tools == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getResourceString("UpdateManagedProject12.9"), null)); //$NON-NLS-1$
		}
		
		for(int i = 0; i < tools.length; i++){
			ITool curTool = tools[i]; 
			ITool parent = curTool.getSuperClass();
			String curToolId = curTool.getId();
			
			if(parent == null)
				continue;
			
			parent = parent.getSuperClass();
			if(parent == null)
				continue;

			String parentId = parent.getId();
			if(!parentId.equals(toolId))
				continue;
				
			try{
				Integer.decode(curToolId.substring(curToolId.lastIndexOf('.')+1)); //$NON-NLS-1$
			}
			catch(IndexOutOfBoundsException e){
				continue;
			}
			catch(NumberFormatException e){
				continue;
			}
			return curTool.getId();
		}		
		throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
				ConverterMessages.getFormattedString("UpdateManagedProject12.10",toolId), null)); //$NON-NLS-1$
	}

	protected static void convertToolRef(IToolChain toolChain, Element oldToolRef, IProgressMonitor monitor) 
								throws CoreException {
		String toolId = oldToolRef.getAttribute(IToolReference.ID);

		toolId = getNewToolId(toolChain, toolId);

		IConfiguration configuration = toolChain.getParent();

		// Get the new tool out of the configuration
		ITool newTool = configuration.getTool(toolId);
		// Check that this is not null
		if(newTool == null)
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject12.11",toolId), null)); //$NON-NLS-1$
		
		// The ref may or may not contain overridden options
		NodeList optRefs = oldToolRef.getElementsByTagName(ITool.OPTION_REF);
		for (int refIndex = optRefs.getLength() - 1; refIndex >= 0; --refIndex) {
				convertOptionRef(toolChain, newTool, (Element) optRefs.item(refIndex));
		}
		monitor.worked(1);
	}
	
	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 * @throws CoreException
	 */
	public static void doProjectUpdate(IProgressMonitor monitor, IProject project) throws CoreException {
		String[] projectName = new String[]{project.getName()};
		IFile settingsFile = project.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		if (!settingsFile.exists()) {
			monitor.done();
			return;
		}
		
		// Backup the file
		monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject12.0", projectName), 1); //$NON-NLS-1$
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		UpdateManagedProjectManager.backupFile(settingsFile, "_12backup", monitor, project); ;	//$NON-NLS-1$

		IManagedProject newProject = null;
		
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
			monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject12.1", projectName), listSize * 9);	 //$NON-NLS-1$
			for (int targIndex = 0; targIndex < listSize; ++targIndex) {
				Element oldTarget = (Element) targetNodes.item(targIndex);
				String oldTargetId = oldTarget.getAttribute(ITarget.ID);
				newProject = convertTarget(project, oldTarget, monitor);
			
				// Remove the old target
				if (newProject != null) {
					info.removeTarget(oldTargetId);
					monitor.worked(9);
				}
			}
			
			// Set the default configuration
			NodeList defaultConfiguration = document.getElementsByTagName(IManagedBuildInfo.DEFAULT_CONFIGURATION);
			try {
				Element defaultConfig = (Element) defaultConfiguration.item(0);
				String oldDefaultConfigId = defaultConfig.getAttribute(IBuildObject.ID);
				IConfiguration newDefaultConfig = (IConfiguration) getConfigIdMap().get(oldDefaultConfigId);
				if (newDefaultConfig != null) {
					info.setDefaultConfiguration(newDefaultConfig);
					info.setSelectedConfiguration(newDefaultConfig);
				} else {
					IConfiguration[] newConfigs = newProject.getConfigurations();
					if (newConfigs.length > 0) {
						info.setDefaultConfiguration(newConfigs[0]);
						info.setSelectedConfiguration(newConfigs[0]);
					}
				}
			} catch (IndexOutOfBoundsException e) {
					throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
							ConverterMessages.getFormattedString("UpdateManagedProject12.7",newProject.getName()), null)); //$NON-NLS-1$
			}
			
			// Upgrade the version
			((ManagedBuildInfo)info).setVersion(ManagedBuildManager.getBuildInfoVersion().toString());
			info.setValid(true);
		}catch (CoreException e){
			throw e;
		}catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					e.getMessage(), e));
		} finally {
			ManagedBuildManager.saveBuildInfo(project, false);
			monitor.done();
		}
	}
	
	
	/* (non-Javadoc)
	 * @return Returns the configIdMap.
	 */
	protected static Map getConfigIdMap() {
		if (configIdMap == null) {
			configIdMap = new HashMap();
		}
		return configIdMap;
	}

}
