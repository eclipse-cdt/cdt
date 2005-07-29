/*******************************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.projectconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
	private static final String OLD_TOOL_ROOT_LONG = "org.eclipse.cdt.build.tool";	//$NON-NLS-1$
	private static final String OLD_TOOL_ROOT_SHORT = "cdt.build.tool";	//$NON-NLS-1$
	private static final String REGEXP_SEPARATOR = "\\.";	//$NON-NLS-1$
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
	 * Generates a valid 2.1 eqivalent ID for an old 1.2 format
	 * configuration. Old built-in configurations had the format
	 * 	cygin.[exec|so|exp|lib].[debug|release]
	 * 	[linux|solaris].gnu.[exec|so|lib].[release|debug]
	 * 
	 * @param oldId
	 * @return
	 */
	protected static String getNewConfigurationId(String oldId){
	    boolean builtIn = false;
		boolean cygwin = false;
		boolean debug = false;
		int type = -1;
		
		Vector idTokens = new Vector(Arrays.asList(oldId.split(REGEXP_SEPARATOR)));
		try {
		    String platform = (String) idTokens.get(0);
		    if (platform.equalsIgnoreCase(ID_CYGWIN)) {
		        cygwin = builtIn = true;
		    } else if ((platform.equalsIgnoreCase(ID_LINUX) || platform.equalsIgnoreCase(ID_SOLARIS))
		            && ((String)idTokens.get(1)).equalsIgnoreCase(ID_GNU)) {
		       builtIn = true; 
		    }
		            
		} catch (ArrayIndexOutOfBoundsException e) {
		    // This just means the ID is not a built-in
		    builtIn = false;
		}
		
		// If this isn't a built-in configuration, don't convert it
		if(!builtIn) {
		    return oldId;
		}
		
		// Otherwise make the conversion
		Iterator iter = idTokens.iterator();
		while (iter.hasNext()) {
			String id = (String) iter.next();
			if(id.equalsIgnoreCase(ID_EXEC)) {
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
		newConfig.setName(oldConfig.getAttribute(IConfigurationV2.NAME));
		
		Element oldTarget = (Element)oldConfig.getParentNode();
		if(oldTarget.hasAttribute(ITarget.ARTIFACT_NAME)){
			String buildGoal = oldTarget.getAttribute(ITarget.ARTIFACT_NAME);
			// The name may be in the form <name>[.ext1[.ext2[...]]]
			String[] nameElements = buildGoal.split(REGEXP_SEPARATOR);
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
					extension += ID_SEPARATOR;	
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
				// TODO:  Need error dialog!
				newProject.removeConfiguration(newConfigId);
				throw e;
			}
		}

		getConfigIdMap().put(oldConfig.getAttribute(IConfigurationV2.ID), newConfig);
		monitor.worked(1);
	}
	
	protected static String getNewOptionId(IToolChain toolChain, ITool tool, String oldId)
								throws CoreException{

	    String optId = null;	    
		String[] idTokens = oldId.split(REGEXP_SEPARATOR);
		Vector oldIdVector = new Vector(Arrays.asList(idTokens));
		if (isBuiltInOption(oldIdVector)) {
			
			// New ID will be in form gnu.[c|c++|both].[compiler|link|lib].option.{1.2_component}
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
			optId = new String();
			for (int rebuildIndex = 0; rebuildIndex < newIdVector.size(); ++ rebuildIndex) {
				String token = (String) newIdVector.get(rebuildIndex);
				optId += token;
				if (rebuildIndex < newIdVector.size() - 1) {
					optId += ID_SEPARATOR;
				}
			}
		} else {
		    optId = oldId;
		}
			
		// Now look it up
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
		return optId;	
	}
	
	protected static void convertOptionRef(IToolChain toolChain, ITool tool, Element optRef) 
							throws CoreException {
		String optId = optRef.getAttribute(IOption.ID);
		if (optId == null) return;
		
		optId = getNewOptionId(toolChain, tool, optId);
		// Get the option from the new tool
		IOption newOpt = tool.getOptionById(optId);
		if (newOpt != null) {		//  Ignore options that don't have a match
			IConfiguration configuration = toolChain.getParent();
	
			try {
				switch (newOpt.getValueType()) {
					case IOption.BOOLEAN:
						Boolean bool = new Boolean(optRef.getAttribute(IOption.DEFAULT_VALUE));
						configuration.setOption(tool, newOpt, bool.booleanValue());
						break;
					case IOption.STRING:
						String strVal = optRef.getAttribute(IOption.DEFAULT_VALUE);
						configuration.setOption(tool, newOpt, strVal);
						break;
					case IOption.ENUMERATED:
						// This is going to be the human readable form of the enumerated value
						String name = optRef.getAttribute(IOption.DEFAULT_VALUE);
						// Convert it to the ID
						String idValue = newOpt.getEnumeratedId(name);
						if (idValue == null) {
							// If the name does not match one of the enumerations values, probably because
							// the list of enumerands has changed, set the name to be the name used for the 
							// enumeration's default value
							name = (String)newOpt.getDefaultValue();
						}
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
	}
	
	/* (non-Javadoc)
	 * Converts an old built-in target ID from 1.2 format to 
	 * 2.1 format. A 1.2 target will have the format :
	 * 	cygyin.[exec|so|lib]
	 * 	[linux|solaris].gnu.[exec|so|lib]
	 * @param oldId
	 * @return
	 */
	protected static String getNewProjectId(String oldId){
		// The type of target we are converting from/to
		int type = -1;
		// Use the Cygwin or generic target form
		boolean posix = false;
		// Is this a built-in target or one we cannot convert
		boolean builtIn = false;

		Vector idTokens = new Vector(Arrays.asList(oldId.split(REGEXP_SEPARATOR)));
		try {
		    String platform = (String) idTokens.get(0);
		    if (platform.equalsIgnoreCase(ID_CYGWIN)) {
		        builtIn = true;
		    } else if ((platform.equalsIgnoreCase(ID_LINUX) || platform.equalsIgnoreCase(ID_SOLARIS))
		            && ((String)idTokens.get(1)).equalsIgnoreCase(ID_GNU)) {
		       posix = builtIn = true; 
		    }
		    
		} catch (ArrayIndexOutOfBoundsException e) {
		    builtIn = false;
		}
		
		// Just answer the original ID if this isn't something we know how to convert
		if (!builtIn) {
		    return oldId;
		}
		
		Iterator iter = idTokens.iterator();
		while (iter.hasNext()) {
			String token = (String) iter.next();
			if (token.equalsIgnoreCase(ID_EXEC)){
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

	/* (non-Javadoc)
	 * 
	 * @param project the project being upgraded
	 * @param oldTarget a document element contain the old target information
	 * @param monitor 
	 * @return new 2.1 managed project 
	 * @throws CoreException if the target is unknown
	 */
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
	    String toolId = null;
	    
	    // Make sure we can convert the ID
	    if (!(oldId.startsWith(OLD_TOOL_ROOT_SHORT) ||  
	            oldId.startsWith(OLD_TOOL_ROOT_LONG))) {
	        toolId= oldId;
	    } else {
			// All known tools have id NEW_TOOL_ROOT.[c|cpp].[compiler|linker|archiver]
			toolId = NEW_TOOL_ROOT;
			boolean cppFlag = true;
			int toolType = -1;
			
			// Figure out what kind of tool the ref pointed to
			Vector idTokens = new Vector(Arrays.asList(oldId.split(REGEXP_SEPARATOR)));
			
			Iterator iter = idTokens.iterator();
			while (iter.hasNext()) {
				String token = (String) iter.next();
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
			
			while (parent != null) {
				String parentId = parent.getId();
				if(parentId.equals(toolId))
					break;
				parent = parent.getSuperClass();
			}
			if(parent == null)
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

	/* (non-Javadoc)
	 * Answers true if the target is one supplied by the CDT which will have the 
	 * format :
	 * 	cygyin.[exec|so|lib]
	 * 	[linux|solaris].gnu.[exec|so|lib]
	 * 
	 * @param id id to test
	 * @return true if the target is recognized as built-in
	 */
//	private static boolean isBuiltInTarget(String id) {
//		// Do the deed
//		if (id == null) return false;
//		String[] idTokens = id.split(REGEXP_SEPARATOR);
//		if (!(idTokens.length == 2 || idTokens.length == 3)) return false;
//		try {
//			String platform = idTokens[0];
//			if (platform.equals(ID_CYGWIN)) {
//				return (idTokens[1].equals(ID_EXEC) 
//						|| idTokens[1].equals(ID_SHARED)
//						|| idTokens[1].equals(ID_STATIC));
//			} else if (platform.equals(ID_LINUX) 
//					|| platform.equals(ID_SOLARIS)) {
//				if (idTokens[1].equals(ID_GNU)) {
//					return (idTokens[2].equals(ID_EXEC) 
//							|| idTokens[2].equals(ID_SHARED)
//							|| idTokens[2].equals(ID_STATIC));					
//				}
//			}
//		} catch (ArrayIndexOutOfBoundsException e) {
//			return false;
//		}
//		return false;
//	}

	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 * @throws CoreException if the update fails
	 */
	public static void doProjectUpdate(IProgressMonitor monitor, final IProject project) throws CoreException {
		String[] projectName = new String[]{project.getName()};
		IFile file = project.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		File settingsFile = file.getLocation().toFile();

		if (!settingsFile.exists()) {
			monitor.done();
			return;
		}
		
		// Backup the file
		monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject12.0", projectName), 1); //$NON-NLS-1$
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		UpdateManagedProjectManager.backupFile(file, "_12backup", monitor, project); ;	//$NON-NLS-1$

		IManagedProject newProject = null;
		
		//Now convert each target to the new format
		try {
			// Load the old build file
			InputStream stream = new FileInputStream(settingsFile);
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
					monitor.worked(1);
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
			        // The only safe thing to do if there wasn't a default configuration for a built-in
			        // target is to set the first defined configuration as the default
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
			((ManagedBuildInfo)info).setVersion("2.1.0");
			info.setValid(true);
		} catch (CoreException e){
			throw e;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					e.getMessage(), e));
		} finally {
			// If the tree is locked spawn a job to this.
			IWorkspace workspace = project.getWorkspace();
			boolean treeLock = workspace.isTreeLocked();
			ISchedulingRule rule = workspace.getRuleFactory().createRule(project);
			if (treeLock) {
				WorkspaceJob job = new WorkspaceJob(ConverterMessages.getResourceString("UpdateManagedProject.notice")) { //$NON-NLS-1$
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						ManagedBuildManager.saveBuildInfo(project, false);
						return Status.OK_STATUS;
					}
				};
				job.setRule(rule);
				job.schedule();
			} else {
				ManagedBuildManager.saveBuildInfo(project, false);
			}
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

	protected static boolean isBuiltInOption(Vector idTokens) {
	    try {
	        String platform = (String) idTokens.firstElement();
	        String secondToken = (String) idTokens.get(1);
	        if (platform.equals(ID_CYGWIN)) {
	            // bit of a mess since was done first
	            // but valid second tokens are 'compiler',
	            // 'preprocessor', 'c', 'gnu', 'link', 
	            // 'solink', or 'ar'
	            if (secondToken.equals(TOOL_NAME_COMPILER) ||
	                    secondToken.equals(ID_PREPROC) || 
	                    secondToken.equals(TOOL_LANG_C) ||
	                    secondToken.equals(ID_GNU) ||
	                    secondToken.equals(TOOL_NAME_LINK) ||
	                    secondToken.equals(TOOL_NAME_SOLINK)||
	                    secondToken.equals(TOOL_NAME_AR)) {
	                return true;
	            }
	        } else if (platform.equals(ID_LINUX)) {
	            // Only going to see 'gnu' or 'c'
	            if (secondToken.equals(ID_GNU) ||
	                    secondToken.equals(TOOL_LANG_C)) {
	                return true;
	            }
	        } else if (platform.equals(ID_SOLARIS)) {
	            // Only going to see 'gnu', 'c', or 'compiler'
	            if ( secondToken.equals(ID_GNU) || 
	                    secondToken.equals(TOOL_LANG_C) ||
	                    secondToken.equals(TOOL_NAME_COMPILER)) {
	                return true;
	            }	            
	        } else {
	            return false;
	        }
	    } catch (NoSuchElementException e) {
	        // If the string is empty, it isn't valid
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        } 
	    return false;
	}
}
