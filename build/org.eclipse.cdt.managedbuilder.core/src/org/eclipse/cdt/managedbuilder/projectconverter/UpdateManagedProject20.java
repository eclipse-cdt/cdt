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
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationV2;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
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

class UpdateManagedProject20 {
	private static final String ID_SEPARATOR = ".";	//$NON-NLS-1$
	
	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 * @throws CoreException
	 */
	static void doProjectUpdate(IProgressMonitor monitor, IProject project) throws CoreException {
		String[] projectName = new String[]{project.getName()};
		IFile settingsFile = project.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		if (!settingsFile.exists()) {
			monitor.done();
			return;
		}
		
		// Backup the file
		monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject20.0", projectName), 1); //$NON-NLS-1$
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		UpdateManagedProjectManager.backupFile(settingsFile, "_20backup", monitor, project); //$NON-NLS-1$
		
		try {
			// Load the old build file
			InputStream stream = settingsFile.getContents();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(stream);
			
			// Clone the target based on the proper target definition
			NodeList targetNodes = document.getElementsByTagName(ITarget.TARGET_ELEMENT_NAME);
			// This is a guess, but typically the project has 1 target, 2 configs, and 6 tool defs
			int listSize = targetNodes.getLength();
			monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject20.1", projectName), listSize * 9); //$NON-NLS-1$
			for (int targIndex = 0; targIndex < listSize; ++targIndex) {
				Element oldTarget = (Element) targetNodes.item(targIndex);
				String oldTargetId = oldTarget.getAttribute(ITarget.ID);
				IManagedProject newProject = convertTarget(project, oldTarget, monitor);
			
				// Remove the old target
				if (newProject != null) {
					info.removeTarget(oldTargetId);
					monitor.worked(9);
				}
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
	
	protected static IManagedProject convertTarget(IProject project, Element oldTarget, IProgressMonitor monitor)
						throws CoreException{
		// What we want to create
		IManagedProject newProject = null;
		IProjectType newParent = null;
		
		// Get the parent
		String parentID = oldTarget.getAttribute(ITarget.PARENT);
		
		String targetID = oldTarget.getAttribute(ITarget.ID);
	
		// Get the new target definitions we need for the conversion
		newParent = ManagedBuildManager.getProjectType(parentID);
		
		if (newParent == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject20.9",parentID), null)); //$NON-NLS-1$
		}

		try {
			// Create a new ManagedProject based on the new parent
			newProject = ManagedBuildManager.createManagedProject(project, newParent);
			
			// Create new configurations
			NodeList configNodes = oldTarget.getElementsByTagName(IConfigurationV2.CONFIGURATION_ELEMENT_NAME);
			for (int configIndex = 0; configIndex < configNodes.getLength(); ++configIndex) {
				try{
					convertConfiguration(newProject, newParent, (Element) configNodes.item(configIndex), monitor);
				}
				catch(CoreException e){
					//TODO: implement logging					
					//should we continue or fail ??
				}
			}
			
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] newConfigs = newProject.getConfigurations();
			if (newConfigs.length > 0) {
				info.setDefaultConfiguration(newConfigs[0]);
				info.setSelectedConfiguration(newConfigs[0]);
			}
			else{
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
						ConverterMessages.getFormattedString("UpdateManagedProject20.10",newProject.getName()), null)); //$NON-NLS-1$
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject20.11",new String[]{newProject.getName(),e.getMessage()}), null)); //$NON-NLS-1$
		}
		
		monitor.worked(1);
		return newProject;
	}

	protected static void convertConfiguration(IManagedProject newProject, IProjectType newParent, Element oldConfig, IProgressMonitor monitor) 
								throws CoreException {
		IConfiguration newParentConfig = null;
		IConfiguration newConfig = null;

		// Figure out what the original parent of the config is
		String parentId = oldConfig.getAttribute(IConfigurationV2.PARENT);

		newParentConfig = newParent.getConfiguration(parentId);
		if (newParentConfig == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject20.2", parentId), null)); //$NON-NLS-1$
		}		
		// Generate a random number for the new config id
		int randomElement = ManagedBuildManager.getRandomNumber();
		String newConfigId = parentId + ID_SEPARATOR + randomElement;
		// Create the new configuration
		newConfig = newProject.createConfiguration(newParentConfig, newConfigId);

		if(oldConfig.hasAttribute(IConfigurationV2.NAME))
			newConfig.setName(oldConfig.getAttribute(IConfigurationV2.NAME));

		Element targetEl = (Element)oldConfig.getParentNode();

		if(targetEl.hasAttribute(ITarget.ARTIFACT_NAME))
			newConfig.setArtifactName(targetEl.getAttribute(ITarget.ARTIFACT_NAME));

		if(targetEl.hasAttribute(ITarget.ERROR_PARSERS))
			newConfig.setErrorParserIds(targetEl.getAttribute(ITarget.ERROR_PARSERS));

		if(targetEl.hasAttribute(ITarget.CLEAN_COMMAND))
			newConfig.setCleanCommand(targetEl.getAttribute(ITarget.CLEAN_COMMAND));

		if(targetEl.hasAttribute(ITarget.EXTENSION))
			newConfig.setArtifactExtension(targetEl.getAttribute(ITarget.EXTENSION));
		
		// Convert the tool references
		
		IToolChain toolChain = newConfig.getToolChain();
		
		if(targetEl.hasAttribute(ITarget.OS_LIST)){
			String oses = targetEl.getAttribute(ITarget.OS_LIST);
			String osList[] = oses.split(","); //$NON-NLS-1$
			for (int i = 0; i < osList.length; ++i) {
				osList[i]=osList[i].trim();
			}
			toolChain.setOSList(osList);
		}
		
		if(targetEl.hasAttribute(ITarget.ARCH_LIST)){
			String archs = targetEl.getAttribute(ITarget.ARCH_LIST);
			String archList[] = archs.split(","); //$NON-NLS-1$
			for (int i = 0; i < archList.length; ++i) {
				archList[i]=archList[i].trim();
			}
			toolChain.setArchList(archList);
		}
		
		if(targetEl.hasAttribute(ITarget.BINARY_PARSER)){
			String binaryParser = targetEl.getAttribute(ITarget.BINARY_PARSER);
			ITargetPlatform targetPlatform = toolChain.getTargetPlatform();
			if(targetPlatform.isExtensionElement()){
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId = targetPlatform.getId() + "." + nnn;		//$NON-NLS-1$
				String builderName = targetPlatform.getName() + "." + newConfig.getName(); 	//$NON-NLS-1$				
				toolChain.createTargetPlatform(targetPlatform,subId,builderName,false);
			}
			targetPlatform.setBinaryParserId(binaryParser);
		}
		
		if(targetEl.hasAttribute(ITarget.MAKE_COMMAND)){
			String makeCommand =  targetEl.getAttribute(ITarget.MAKE_COMMAND);
			IBuilder builder = toolChain.getBuilder();
			if (builder.isExtensionElement()) {
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId = builder.getId() + "." + nnn;		//$NON-NLS-1$
				String builderName = builder.getName() + "." + newConfig.getName(); 	//$NON-NLS-1$
				builder = toolChain.createBuilder(builder, subId, builderName, false);
			}
			builder.setCommand(makeCommand);
		}

		if(targetEl.hasAttribute(ITarget.MAKE_ARGS)){
			String makeArguments =  targetEl.getAttribute(ITarget.MAKE_ARGS);
			IBuilder builder = toolChain.getBuilder();
			if (builder.isExtensionElement()) {
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId = builder.getId() + "." + nnn;		//$NON-NLS-1$
				String builderName = builder.getName() + "." + newConfig.getName(); 	//$NON-NLS-1$
				builder = toolChain.createBuilder(builder, subId, builderName, false);
			}
			builder.setArguments(makeArguments);
		}

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

		monitor.worked(1);
	}
	
	protected static void convertToolRef(IToolChain toolChain, Element oldToolRef, IProgressMonitor monitor) 
							throws CoreException {
		if(!oldToolRef.hasAttribute(IToolReference.ID)) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getResourceString("UpdateManagedProject20.3"), null)); //$NON-NLS-1$
		}

		String toolId = oldToolRef.getAttribute(IToolReference.ID);
		IConfiguration configuration = toolChain.getParent();
		
		ITool tools[] = configuration.getTools();
		if(tools == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getResourceString("UpdateManagedProject20.4"), null)); //$NON-NLS-1$
		}
		
		ITool tool = null;
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
			tool = curTool;
			break;
		}

		if(tool == null){
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject20.5",toolId), null)); //$NON-NLS-1$
		}
			
		//the tool found, proceed with conversion ...

		if(oldToolRef.hasAttribute(IToolReference.COMMAND))
			tool.setToolCommand(oldToolRef.getAttribute(IToolReference.COMMAND));

		if(oldToolRef.hasAttribute(IToolReference.OUTPUT_FLAG))
			tool.setOutputFlag(oldToolRef.getAttribute(IToolReference.OUTPUT_FLAG));

		if(oldToolRef.hasAttribute(IToolReference.OUTPUT_PREFIX))
			tool.setOutputPrefix(oldToolRef.getAttribute(IToolReference.OUTPUT_PREFIX));
			

		if(oldToolRef.hasAttribute(IToolReference.OUTPUTS)){
			String outputs = oldToolRef.getAttribute(IToolReference.OUTPUTS);
			tool.setOutputExtensions(outputs);
		}

		NodeList optRefs = oldToolRef.getElementsByTagName(ITool.OPTION_REF);
		for (int refIndex = optRefs.getLength() - 1; refIndex >= 0; --refIndex) {
				convertOptionRef(toolChain, tool, (Element) optRefs.item(refIndex), monitor);
		}

		monitor.worked(1);
	}
	
	protected static void convertOptionRef(IToolChain toolChain, ITool tool, Element optRef, IProgressMonitor monitor) 
							throws CoreException {

		if(!optRef.hasAttribute(IOption.ID)){
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getResourceString("UpdateManagedProject20.6"), null)); //$NON-NLS-1$
		}
		
		String optId = optRef.getAttribute(IOption.ID);
		
		IConfiguration configuration = toolChain.getParent();
		
		IOption options[] = tool.getOptions();		
		IOption option = null;

		for(int i = 0; i < options.length; i++){
			IOption curOption = options[i]; 
			IOption parent = curOption.getSuperClass();
			String curOptionId = curOption.getId();
			
			if(parent == null)
				continue;
			
			String parentId = parent.getId();
			if(!parentId.equals(optId))
				continue;
				
			option = curOption;
			break;
		}
		
		if(option == null)
			option = tool.getOptionById(optId);
		
		if(option == null){
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject20.7",optId), null)); //$NON-NLS-1$
		}

		try{
			int type = option.getValueType();
				
			switch(type){
				case IOption.BOOLEAN:{
					if(optRef.hasAttribute(IOption.DEFAULT_VALUE)){
						Boolean bool = new Boolean(optRef.getAttribute(IOption.DEFAULT_VALUE));
						configuration.setOption(tool,option,bool.booleanValue());
					}
					break;
				}
			case IOption.ENUMERATED:
			case IOption.STRING:{
					if(optRef.hasAttribute(IOption.DEFAULT_VALUE))
						configuration.setOption(tool,option,optRef.getAttribute(IOption.DEFAULT_VALUE));
					break;
				}
			case IOption.STRING_LIST:
			case IOption.INCLUDE_PATH:
			case IOption.PREPROCESSOR_SYMBOLS:
			case IOption.LIBRARIES:
			case IOption.OBJECTS:{
					Vector values = new Vector();
					NodeList nodes = optRef.getElementsByTagName(IOption.LIST_VALUE);
					for (int j = 0; j < nodes.getLength(); ++j) {
						Node node = nodes.item(j);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Boolean isBuiltIn = new Boolean(((Element)node).getAttribute(IOption.LIST_ITEM_BUILTIN));
							if (!isBuiltIn.booleanValue()) {
								values.add(((Element)node).getAttribute(IOption.LIST_ITEM_VALUE));
							}
						}
					}
					configuration.setOption(tool,option,(String[])values.toArray(new String[values.size()]));
					break;
				}
			default:
				break;
			}
		}
		catch(BuildException e){
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getFormattedString("UpdateManagedProject20.8",e.getMessage()), e)); //$NON-NLS-1$
		}

	}
}

