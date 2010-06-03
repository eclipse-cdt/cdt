/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.Bundle;

/**
 * This supplier is used to suply MBS-predefined macros
 * 
 * @since 3.0
 */
public class CdtMacroSupplier extends CoreMacroSupplierBase {
	private static CdtMacroSupplier fInstance;
	public final static String DOT = ".";	//$NON-NLS-1$
	public final static String EMPTY_STRING = ""; //$NON-NLS-1$
	
	private static final String fConfigurationMacros[] = new String[]{
		"ConfigName",	//$NON-NLS-1$
		"ConfigDescription",	//$NON-NLS-1$
		"ProjName",	//$NON-NLS-1$
		"ProjDirPath",	//$NON-NLS-1$
//		"BuildArtifactFileName",	//$NON-NLS-1$
//		"BuildArtifactFileExt",	//$NON-NLS-1$
//		"BuildArtifactFileBaseName",	//$NON-NLS-1$
//		"BuildArtifactFilePrefix",	//$NON-NLS-1$
//		"TargetOsList",	//$NON-NLS-1$
//		"TargetArchList",	//$NON-NLS-1$
	};

	private static final String fWorkspaceMacros[] = new String[]{
		"WorkspaceDirPath",	//$NON-NLS-1$
		"DirectoryDelimiter",	//$NON-NLS-1$
		"PathDelimiter",	//$NON-NLS-1$
	};

	private static final String fCDTEclipseMacros[] = new String[]{
		"EclipseVersion",	//$NON-NLS-1$
		"CDTVersion",	//$NON-NLS-1$
//		"MBSVersion",	//$NON-NLS-1$
		"HostOsName",	//$NON-NLS-1$
		"HostArchName",	//$NON-NLS-1$
		"OsType",	//$NON-NLS-1$
		"ArchType",	//$NON-NLS-1$
	};
	
	
/*	private String getExplicitFileMacroValue(String name, IPath inputFileLocation, IPath outputFileLocation, IConfiguration cfg){
		String value = null;
		if("InputFileName".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0)
				value = inputFileLocation.lastSegment();
		}else if("InputFileExt".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0)
				value = getExtension(inputFileLocation.lastSegment());
		}else if("InputFileBaseName".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0)
				value = getBaseName(inputFileLocation.lastSegment());
		}else if("InputFileRelPath".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, inputFileLocation);
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		else if("InputDirRelPath".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, inputFileLocation.removeLastSegments(1).addTrailingSeparator());
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		else if("OutputFileName".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0)
				value = outputFileLocation.lastSegment();
		}else if("OutputFileExt".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0)
				value = getExtension(outputFileLocation.lastSegment());
		}else if("OutputFileBaseName".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0)
				value = getBaseName(outputFileLocation.lastSegment());
		}else if("OutputFileRelPath".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, outputFileLocation);
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}else if("OutputDirRelPath".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, outputFileLocation.removeLastSegments(1).addTrailingSeparator());
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		
		return value;
	}*/

	public String[] getMacroNames(int contextType){
		return getMacroNames(contextType,true);
	}

	private String[] getMacroNames(int contextType, boolean clone){
		String names[] = null;
		switch(contextType){
		case ICoreVariableContextInfo.CONTEXT_CONFIGURATION:
			names = fConfigurationMacros; 
			break;
		case ICoreVariableContextInfo.CONTEXT_WORKSPACE:
			names = fWorkspaceMacros; 
			break;
		case ICoreVariableContextInfo.CONTEXT_INSTALLATIONS:
			names = fCDTEclipseMacros; 
			break;
		case ICoreVariableContextInfo.CONTEXT_ECLIPSEENV:
			break;
		}
		if(names != null)
			return clone ? (String[])names.clone() : names;
		return null;
	}

	private CdtMacroSupplier(){
		
	}

	public static CdtMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new CdtMacroSupplier();
		return fInstance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public ICdtVariable getMacro(String macroName, int contextType,
			Object contextData) {
		ICdtVariable macro = null; 
		switch(contextType){
		case ICoreVariableContextInfo.CONTEXT_CONFIGURATION:
			if(contextData instanceof ICConfigurationDescription){
				macro = getMacro(macroName, (ICConfigurationDescription)contextData);
			}
			break;
		case ICoreVariableContextInfo.CONTEXT_WORKSPACE:
			if(contextData == null || contextData instanceof IWorkspace){
				macro = getMacro(macroName, (IWorkspace)contextData);
			}
			break;
		case ICoreVariableContextInfo.CONTEXT_INSTALLATIONS:
			if(contextData == null){
				macro = getMacro(macroName);
			}
			break;
		case ICoreVariableContextInfo.CONTEXT_ECLIPSEENV:
			break;
		}
		
		return macro;
	}

	private IProject getProject(ICConfigurationDescription cfgDes){
		ICProjectDescription des = cfgDes.getProjectDescription();
		return des != null ? des.getProject() : null;
	}
	
	public ICdtVariable getMacro(String macroName, ICConfigurationDescription cfg){
		ICdtVariable macro = null;
		if("ConfigName".equals(macroName)){	//$NON-NLS-1$
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,cfg.getName());
		}
		else if("ConfigDescription".equals(macroName)){	//$NON-NLS-1$
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,cfg.getDescription());
		} else if("ProjName".equals(macroName)){	//$NON-NLS-1$
			IProject project = getProject(cfg);
			if(project != null)
				macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,project.getName());
		} else if("ProjDirPath".equals(macroName)){	//$NON-NLS-1$
			IProject project = getProject(cfg);
			if(project != null && project.getLocation() != null) // in the EFS world getLocation() can return null
				macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,project.getLocation().toString());
		}
/*		else if("BuildArtifactFileName".equals(macroName)){	//$NON-NLS-1$
			String name = cfg.getArtifactName();
			String ext = cfg.getArtifactExtension();
			if(ext != null && !EMPTY_STRING.equals(ext))
				name = name + DOT + ext;
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,name); 
		}*/
/*		else if("BuildArtifactFileExt".equals(macroName)){	//$NON-NLS-1$
			String ext = cfg.getArtifactExtension();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,ext); 
		}*/
/*		else if("BuildArtifactFileBaseName".equals(macroName)){	//$NON-NLS-1$
			String name = cfg.getArtifactName();
			ITool targetTool = cfg.calculateTargetTool();
			if(targetTool != null){
				IOutputType pot = targetTool.getPrimaryOutputType();
				String prefix = pot.getOutputPrefix();
				

				// Resolve any macros in the outputPrefix
				// Note that we cannot use file macros because if we do a clean
				// we need to know the actual
				// name of the file to clean, and cannot use any builder
				// variables such as $@. Hence
				// we use the next best thing, i.e. configuration context.

				// figure out the configuration we're using
				IBuildObject toolParent = targetTool.getParent();
				IConfiguration config = null;
				// if the parent is a config then we're done
				if (toolParent instanceof IConfiguration)
					config = (IConfiguration) toolParent;
				else if (toolParent instanceof IToolChain) {
					// must be a toolchain
					config = (IConfiguration) ((IToolChain) toolParent)
							.getParent();
				}

				else if (toolParent instanceof IResourceConfiguration) {
					config = (IConfiguration) ((IResourceConfiguration) toolParent)
							.getParent();
				}

				else {
					// bad
					throw new AssertionError(
							"tool parent must be one of configuration, toolchain, or resource configuration");
				}

				if (config != null) {

					try {
						prefix = ManagedBuildManager
								.getBuildMacroProvider()
								.resolveValueToMakefileFormat(
										prefix,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_CONFIGURATION,
										config);
					}

					catch (BuildMacroException e) {
					}

				}

				
				if(prefix != null && !EMPTY_STRING.equals(prefix))
					name = prefix + name;
			}
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,name); 
		}*/
/*		else if("BuildArtifactFilePrefix".equals(macroName)){	//$NON-NLS-1$
			ITool targetTool = cfg.calculateTargetTool();
			if(targetTool != null){
				IOutputType pot = targetTool.getPrimaryOutputType();
				String prefix = pot.getOutputPrefix();
				
				// Resolve any macros in the outputPrefix
				// Note that we cannot use file macros because if we do a clean
				// we need to know the actual
				// name of the file to clean, and cannot use any builder
				// variables such as $@. Hence
				// we use the next best thing, i.e. configuration context.

				// figure out the configuration we're using
				IBuildObject toolParent = targetTool.getParent();
				IConfiguration config = null;
				// if the parent is a config then we're done
				if (toolParent instanceof IConfiguration)
					config = (IConfiguration) toolParent;
				else if (toolParent instanceof IToolChain) {
					// must be a toolchain
					config = (IConfiguration) ((IToolChain) toolParent)
							.getParent();
				}

				else if (toolParent instanceof IResourceConfiguration) {
					config = (IConfiguration) ((IResourceConfiguration) toolParent)
							.getParent();
				}

				else {
					// bad
					throw new AssertionError(
							"tool parent must be one of configuration, toolchain, or resource configuration");
				}

				if (config != null) {

					try {
						prefix = ManagedBuildManager
								.getBuildMacroProvider()
								.resolveValueToMakefileFormat(
										prefix,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_CONFIGURATION,
										config);
					}

					catch (BuildMacroException e) {
					}

				}
				
				if(prefix == null)
					prefix = EMPTY_STRING;
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,prefix);
			}
		}*/
/*		else if("TargetOsList".equals(macroName)){	//$NON-NLS-1$
			IToolChain toolChain = cfg.getToolChain();
			String osList[] = toolChain.getOSList();
			if(osList == null)
				osList = new String[0];
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT_LIST,osList);
		}*/
/*		else if("TargetArchList".equals(macroName)){	//$NON-NLS-1$
			IToolChain toolChain = cfg.getToolChain();
			String archList[] = toolChain.getArchList();
			if(archList == null)
				archList = new String[0];
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT_LIST,archList);
			
		}*/
/*		else if("ToolChainVersion".equals(macroName)){	//$NON-NLS-1$
			if(cfg.getToolChain().getVersion() != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getToolChain().getVersion().toString());
		}*/
/*		else if("BuilderVersion".equals(macroName)){	//$NON-NLS-1$
			PluginVersionIdentifier version = cfg.getToolChain().getBuilder().getVersion(); 
			if(version != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version.toString());
		}*/
		return macro;
	}
	
//	private String getBaseName(String name){
//		String value = null;
//		int index = name.lastIndexOf('.');
//		if(index == -1)
//			value = name;
//		else
//			value = name.substring(0,index);
//		return value;
//	}
//	
//	private String getExtension(String name){
//		String value = null;
//		int index = name.lastIndexOf('.');
//		if(index != -1)
//			value = name.substring(index+1);
//		return value;
//	}
	
/*	public IBuildMacro getMacro(String macroName, IManagedProject mngProj){
		IBuildMacro macro = null;
		if("ProjName".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,mngProj.getOwner().getName());
		}
		else if("ProjDirPath".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_PATH_DIR,mngProj.getOwner().getLocation().toOSString());
		}
		return macro;
	}
*/	
	public ICdtVariable getMacro(String macroName, IWorkspace wsp){
		if(wsp == null)
			wsp = ResourcesPlugin.getWorkspace();
		ICdtVariable macro = null;
		if("WorkspaceDirPath".equals(macroName)){	//$NON-NLS-1$
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_PATH_DIR,wsp.getRoot().getLocation().toOSString());
		} else if("DirectoryDelimiter".equals(macroName)){	//$NON-NLS-1$
			if(isWin32()){
				macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,"\\");	//$NON-NLS-1$
			} else {
				macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,"/");	//$NON-NLS-1$
			}
		} else if("PathDelimiter".equals(macroName)){	//$NON-NLS-1$
			if(isWin32()){
				macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,";");	//$NON-NLS-1$
			} else {
				macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,":");	//$NON-NLS-1$
			}
		}
		return macro;
	}
	
	private boolean isWin32(){
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (os.startsWith("windows ")) //$NON-NLS-1$
			return true;
		return false;
	}

	public ICdtVariable getMacro(String macroName){
		ICdtVariable macro = null;
		if("EclipseVersion".equals(macroName)){	//$NON-NLS-1$
			Bundle bundle = Platform.getBundle("org.eclipse.platform");	//$NON-NLS-1$
			String version = bundle != null ? 
					(String)bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION) :
						null;
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,version);
		}
		else if("CDTVersion".equals(macroName)){	//$NON-NLS-1$
			String version = (String)CCorePlugin.getDefault().getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,version);
		}
/*		else if("MBSVersion".equals(macroName)){	//$NON-NLS-1$
			String version = ManagedBuildManager.getBuildInfoVersion().toString();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}*/
		else if("HostOsName".equals(macroName)){	//$NON-NLS-1$
			String os = System.getProperty("os.name"); //$NON-NLS-1$
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,os);
		}
		else if("HostArchName".equals(macroName)){	//$NON-NLS-1$
			String arch = System.getProperty("os.arch"); //$NON-NLS-1$
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,arch);
		}
		else if("OsType".equals(macroName)){	//$NON-NLS-1$
			String os = Platform.getOS();
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,os);
		}
		else if("ArchType".equals(macroName)){	//$NON-NLS-1$
			String arch = Platform.getOSArch();
			macro = new CdtVariable(macroName,ICdtVariable.VALUE_TEXT,arch);
		}

		return macro;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	@Override
	public ICdtVariable[] getMacros(int contextType, Object contextData) {
		String names[] = getMacroNames(contextType,false);
		
		if(names != null){
			ICdtVariable macros[] = new ICdtVariable[names.length];
			int num = 0;
			for (String name : names) {
				ICdtVariable macro = getMacro(name,contextType,contextData);
				if(macro != null)
					macros[num++] = macro;
			}
			if(macros.length != num){
				ICdtVariable tmp[] = new ICdtVariable[num];
				if(num > 0)
					System.arraycopy(macros,0,tmp,0,num);
				macros = tmp;
			}
			return macros;
		}
		return null;
	}
	
/*	private IPath getBuilderCWD(IConfiguration cfg){
		IPath workingDirectory = null;
		IResource owner = cfg.getOwner();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(owner);
			
		if(info != null){
			if(info.getDefaultConfiguration().equals(cfg)){
				IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(info.getDefaultConfiguration());
				generator.initialize((IProject)owner,info,null);
				
				IPath topBuildDir = generator.getBuildWorkingDir();
				if(topBuildDir == null)
					topBuildDir = new Path(info.getConfigurationName());

				IPath projectLocation = owner.getLocation();
				workingDirectory = projectLocation.append(topBuildDir);
			}
		}
		return workingDirectory;
	}
*/	
}
