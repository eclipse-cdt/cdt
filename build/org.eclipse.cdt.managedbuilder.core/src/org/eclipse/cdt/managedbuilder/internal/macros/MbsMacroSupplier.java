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
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.BuildSettingsUtil;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceInfo;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IFileContextData;
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * This supplier is used to suply MBS-predefined macros
 *
 * @since 3.0
 */
public class MbsMacroSupplier extends BuildCdtVariablesSupplierBase {
	private static MbsMacroSupplier fInstance;
	public final static String DOT = ".";	//$NON-NLS-1$
	public final static String EMPTY_STRING = ""; //$NON-NLS-1$


	private static final String fFileMacros[] = new String[]{
		"InputFileName",	//$NON-NLS-1$
		"InputFileExt",	//$NON-NLS-1$
		"InputFileBaseName",	//$NON-NLS-1$
		"InputFileRelPath",	//$NON-NLS-1$
		"InputDirRelPath",	//$NON-NLS-1$
		"OutputFileName",	//$NON-NLS-1$
		"OutputFileExt",	//$NON-NLS-1$
		"OutputFileBaseName",	//$NON-NLS-1$
		"OutputFileRelPath",	//$NON-NLS-1$
		"OutputDirRelPath",	//$NON-NLS-1$
	};

	private static final String fOptionMacros[] = new String[]{
		"IncludeDefaults",	//$NON-NLS-1$
		"ParentVersion",	//$NON-NLS-1$
	};

	private static final String fToolMacros[] = new String[]{
		"ToolVersion",	//$NON-NLS-1$
	};

	private static final String fConfigurationMacros[] = new String[]{
//		"ConfigName",	//$NON-NLS-1$
//		"ConfigDescription",	//$NON-NLS-1$
		"BuildArtifactFileName",	//$NON-NLS-1$
		"BuildArtifactFileExt",	//$NON-NLS-1$
		"BuildArtifactFileBaseName",	//$NON-NLS-1$
		"BuildArtifactFilePrefix",	//$NON-NLS-1$
		"TargetOsList",	//$NON-NLS-1$
		"TargetArchList",	//$NON-NLS-1$
		"ToolChainVersion",	//$NON-NLS-1$
		"BuilderVersion",	//$NON-NLS-1$
	};

	private static final String fProjectMacros[] = new String[]{
//		"ProjName",	//$NON-NLS-1$
//		"ProjDirPath",	//$NON-NLS-1$
	};

	private static final String fWorkspaceMacros[] = new String[]{
//		"WorkspaceDirPath",	//$NON-NLS-1$
//		"DirectoryDelimiter",	//$NON-NLS-1$
//		"PathDelimiter",	//$NON-NLS-1$
	};

	private static final String fCDTEclipseMacros[] = new String[]{
//		"EclipseVersion",	//$NON-NLS-1$
//		"CDTVersion",	//$NON-NLS-1$
		"MBSVersion",	//$NON-NLS-1$
//		"HostOsName",	//$NON-NLS-1$
//		"HostArchName",	//$NON-NLS-1$
//		"OsType",	//$NON-NLS-1$
//		"ArchType",	//$NON-NLS-1$
	};

	private class OptionData extends OptionContextData {
		private IBuildObject fOptionContainer;
		public OptionData(IOption option, IBuildObject parent) {
			this(option, parent, parent);
		}

		public OptionData(IOption option, IBuildObject parent,  IBuildObject optionContainer) {
			super(option, parent);
			fOptionContainer = optionContainer;
		}

		public IBuildObject getOptionContainer(){
			return fOptionContainer;
		}
	}

	public class FileContextMacro extends BuildMacro{
//		private IFileContextData fContextData;
//		private IConfiguration fConfiguration;
		//TODO: initialize builder
//		private IBuilder fBuilder;
		private boolean fIsExplicit = true;
		private boolean fIsInitialized;
		private String fExplicitValue;
		private boolean fIsExplicitResolved;
		private IPath fInputFileLocation;
		private IPath fOutputFileLocation;

		private FileContextMacro(String name, IFileContextData contextData){
			fName = name;
			fType = VALUE_TEXT;

			loadValue(contextData);

//			fContextData = contextData;
		}

		private void loadValue(IFileContextData contextData){
			if(fIsInitialized)
				return;
			IBuilder builder = null;
			IConfiguration configuration = null;
			IOptionContextData optionContext = contextData.getOptionContextData();
			if(optionContext != null){
				IBuildObject buildObject = optionContext.getParent();
				if(buildObject instanceof ITool){
					buildObject = ((ITool)buildObject).getParent();
				} else if(buildObject instanceof IConfiguration){
					buildObject = ((IConfiguration)buildObject).getToolChain();
				}
				if(buildObject instanceof IToolChain){
					IToolChain toolChain = (IToolChain)buildObject;
					builder = toolChain.getBuilder();
					configuration = toolChain.getParent();
				} else if (buildObject instanceof IResourceInfo){
					configuration = ((IResourceInfo)buildObject).getParent();
					if(configuration != null){
						IToolChain toolChain = configuration.getToolChain();
						if(toolChain != null)
							builder = toolChain.getBuilder();
					}
				}
			}

			if(builder != null){
				IFileContextBuildMacroValues values = builder.getFileContextBuildMacroValues();
				String value = values.getMacroValue(fName);
				if(value != null){
					fStringValue = value;
					fIsExplicit = false;
				}
			}

			if(fStringValue == null){
				fIsExplicit = true;
				fStringValue = getExplicitFileMacroValue(fName, contextData.getInputFileLocation(), contextData.getOutputFileLocation(), builder, configuration);
				fExplicitValue = fStringValue;
				fIsExplicitResolved = true;
			}

			fInputFileLocation = contextData.getInputFileLocation();
			fOutputFileLocation = contextData.getOutputFileLocation();
			fIsInitialized = true;
		}

		public String getExplicitMacroValue(IConfiguration configuration, IBuilder builder){
//			loadValue();
			if(!fIsExplicitResolved){
				fExplicitValue = getExplicitFileMacroValue(fName, fInputFileLocation, fOutputFileLocation, builder, configuration);
				fIsExplicitResolved = true;
			}
			return fExplicitValue;
		}

		public boolean isExplicit(){
//			loadValue();
			return fIsExplicit;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
		 */
		@Override
		public String getStringValue(){
//			loadValue();
			return fStringValue;
		}
	}

	private String getExplicitFileMacroValue(String name, IPath inputFileLocation, IPath outputFileLocation, IBuilder builder, IConfiguration cfg){
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
				IPath workingDirectory = getBuilderCWD(builder, cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, inputFileLocation);
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		else if("InputDirRelPath".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(builder, cfg);
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
				IPath workingDirectory = getBuilderCWD(builder, cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, outputFileLocation);
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}else if("OutputDirRelPath".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(builder, cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, outputFileLocation.removeLastSegments(1).addTrailingSeparator());
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}

		return value;
	}

	public String[] getMacroNames(int contextType){
		return getMacroNames(contextType,true);
	}

	private String[] getMacroNames(int contextType, boolean clone){
		String names[] = null;
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_FILE:
			names = fFileMacros;
			break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			names = fOptionMacros;
			break;
		case IBuildMacroProvider.CONTEXT_TOOL:
			names = fToolMacros;
			break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			names = fConfigurationMacros;
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			names = fProjectMacros;
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			names = fWorkspaceMacros;
			break;
		case IBuildMacroProvider.CONTEXT_INSTALLATIONS:
			names = fCDTEclipseMacros;
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			break;
		}
		if(names != null)
			return clone ? (String[])names.clone() : names;
		return null;
	}

	private MbsMacroSupplier(){

	}

	public static MbsMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new MbsMacroSupplier();
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		IBuildMacro macro = null;
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_FILE:
			if(contextData instanceof IFileContextData){
				for(int i = 0; i < fFileMacros.length; i++){
					if(macroName.equals(fFileMacros[i])){
						macro = new FileContextMacro(macroName,(IFileContextData)contextData);
						break;
					}
				}
			}
			break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			if(contextData instanceof IOptionContextData){
				macro = getMacro(macroName, (IOptionContextData)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_TOOL:
			if(contextData instanceof ITool){
				macro = getMacro(macroName, (ITool)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			IConfiguration cfg = null;
			IBuilder builder = null;
			if(contextData instanceof IConfiguration){
				cfg = (IConfiguration)contextData;
				builder = cfg.getBuilder();
			} else if (contextData instanceof IBuilder){
				builder = (IBuilder)contextData;
				cfg = builder.getParent().getParent();
			}
			if(cfg != null)
				macro = getMacro(macroName, builder, cfg);

			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(contextData instanceof IManagedProject){
				macro = getMacro(macroName, (IManagedProject)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(contextData instanceof IWorkspace){
				macro = getMacro(macroName, (IWorkspace)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_INSTALLATIONS:
			if(contextData == null){
				macro = getMacro(macroName);
			}
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			break;
		}

		return macro;
	}

	public IBuildMacro getMacro(String macroName, IOptionContextData optionContext){
		IBuildMacro macro = null;
		if("IncludeDefaults".equals(macroName)){	//$NON-NLS-1$
			if(!canHandle(optionContext))
				optionContext = null;
			macro = new OptionMacro(macroName,optionContext);
		} else if("ParentVersion".equals(macroName)){ //$NON-NLS-1$
			IHoldsOptions holder = OptionContextData.getHolder(optionContext);
			if(holder != null && holder.getVersion() != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,holder.getVersion().toString());
		}
		return macro;
	}

	public IBuildMacro getMacro(String macroName, ITool tool){
		IBuildMacro macro = null;
		if("ToolVersion".equals(macroName) && tool.getVersion() != null){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,tool.getVersion().toString());
		}
		return macro;
	}

	public IBuildMacro getMacro(String macroName, IBuilder builder, IConfiguration cfg){
		IBuildMacro macro = null;
		if("ConfigName".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getName());
		}
		else if("ConfigDescription".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getDescription());
		}
		else if("BuildArtifactFileName".equals(macroName)){	//$NON-NLS-1$
			String name = cfg.getArtifactName();
			String ext = cfg.getArtifactExtension();
			if(ext != null && !EMPTY_STRING.equals(ext))
				name = name + DOT + ext;
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,name);
		}
		else if("BuildArtifactFileExt".equals(macroName)){	//$NON-NLS-1$
			String ext = cfg.getArtifactExtension();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,ext);
		}
		else if("BuildArtifactFileBaseName".equals(macroName)){	//$NON-NLS-1$
			String name = cfg.getArtifactName();
			ITool targetTool = cfg.calculateTargetTool();
			if(targetTool != null){
				IOutputType pot = targetTool.getPrimaryOutputType();
				String prefix = pot != null ? pot.getOutputPrefix() : "";	//$NON-NLS-1$


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
					config = ((IToolChain) toolParent)
							.getParent();
				}

				else if (toolParent instanceof IResourceConfiguration) {
					config = ((IResourceConfiguration) toolParent)
							.getParent();
				}

				else {
					// bad
					throw new AssertionError(
							ManagedMakeMessages.getResourceString("MbsMacroSupplier.1")); //$NON-NLS-1$
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
		}
		else if("BuildArtifactFilePrefix".equals(macroName)){	//$NON-NLS-1$
			ITool targetTool = cfg.calculateTargetTool();
			if(targetTool != null){
				IOutputType pot = targetTool.getPrimaryOutputType();
				String prefix = pot != null ? pot.getOutputPrefix() : "";	//$NON-NLS-1$

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
					config = ((IToolChain) toolParent)
							.getParent();
				}

				else if (toolParent instanceof IResourceConfiguration) {
					config = ((IResourceConfiguration) toolParent)
							.getParent();
				}

				else {
					// bad
					throw new AssertionError(
							ManagedMakeMessages.getResourceString("MbsMacroSupplier.2")); //$NON-NLS-1$
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
		}
		else if("TargetOsList".equals(macroName)){	//$NON-NLS-1$
			IToolChain toolChain = cfg.getToolChain();
			String osList[] = toolChain.getOSList();
			if(osList == null)
				osList = new String[0];
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT_LIST,osList);
		}
		else if("TargetArchList".equals(macroName)){	//$NON-NLS-1$
			IToolChain toolChain = cfg.getToolChain();
			String archList[] = toolChain.getArchList();
			if(archList == null)
				archList = new String[0];
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT_LIST,archList);

		}
		else if("ToolChainVersion".equals(macroName)){	//$NON-NLS-1$
			if(cfg.getToolChain().getVersion() != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getToolChain().getVersion().toString());
		}
		else if("BuilderVersion".equals(macroName)){	//$NON-NLS-1$
			Version version = cfg.getToolChain().getBuilder().getVersion();
			if(version != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version.toString());
		}
		return macro;
	}

	private String getBaseName(String name){
		String value = null;
		int index = name.lastIndexOf('.');
		if(index == -1)
			value = name;
		else
			value = name.substring(0,index);
		return value;
	}

	private String getExtension(String name){
		String value = null;
		int index = name.lastIndexOf('.');
		if(index != -1)
			value = name.substring(index+1);
		return value;
	}

	public IBuildMacro getMacro(String macroName, IManagedProject mngProj){
		IBuildMacro macro = null;
		if("ProjName".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,mngProj.getOwner().getName());
		}
		else if("ProjDirPath".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_PATH_DIR,mngProj.getOwner().getLocation().toOSString());
		}
		return macro;
	}

	public IBuildMacro getMacro(String macroName, IWorkspace wsp){
		IBuildMacro macro = null;
		if("WorkspaceDirPath".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_PATH_DIR,wsp.getRoot().getLocation().toOSString());
		} else if("DirectoryDelimiter".equals(macroName)){	//$NON-NLS-1$
			if(isWin32()){
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,"\\");	//$NON-NLS-1$
			} else {
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,"/");	//$NON-NLS-1$
			}
		} else if("PathDelimiter".equals(macroName)){	//$NON-NLS-1$
			if(isWin32()){
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,";");	//$NON-NLS-1$
			} else {
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,":");	//$NON-NLS-1$
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

	public IBuildMacro getMacro(String macroName){
		IBuildMacro macro = null;
		if("EclipseVersion".equals(macroName)){	//$NON-NLS-1$
			Bundle bundle = Platform.getBundle("org.eclipse.platform");	//$NON-NLS-1$
			String version = bundle != null ?
					(String)bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION) :
						null;
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}
		else if("CDTVersion".equals(macroName)){	//$NON-NLS-1$
			String version = CCorePlugin.getDefault().getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}
		else if("MBSVersion".equals(macroName)){	//$NON-NLS-1$
			String version = ManagedBuildManager.getBuildInfoVersion().toString();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}
		else if("HostOsName".equals(macroName)){	//$NON-NLS-1$
			String os = System.getProperty("os.name"); //$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,os);
		}
		else if("HostArchName".equals(macroName)){	//$NON-NLS-1$
			String arch = System.getProperty("os.arch"); //$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,arch);
		}
		else if("OsType".equals(macroName)){	//$NON-NLS-1$
			String os = Platform.getOS();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,os);
		}
		else if("ArchType".equals(macroName)){	//$NON-NLS-1$
			String arch = Platform.getOSArch();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,arch);
		}

		return macro;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	@Override
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		String names[] = getMacroNames(contextType,false);

		if(names != null){
			IBuildMacro macros[] = new IBuildMacro[names.length];
			int num = 0;
			for(int i = 0; i < names.length; i++){
				IBuildMacro macro = getMacro(names[i],contextType,contextData);
				if(macro != null)
					macros[num++] = macro;
			}
			if(macros.length != num){
				IBuildMacro tmp[] = new IBuildMacro[num];
				if(num > 0)
					System.arraycopy(macros,0,tmp,0,num);
				macros = tmp;
			}
			return macros;
		}
		return null;
	}

	private IPath getBuilderCWD(IBuilder builder, IConfiguration cfg){
		return ManagedBuildManager.getBuildLocation(cfg, builder);
	}

	private IPath getOutputFilePath(IPath inputPath, IBuilder builder, IConfiguration cfg){
		ITool buildTools[] = null;
		IResourceConfiguration rcCfg = cfg.getResourceConfiguration(inputPath.toString());
		if(rcCfg != null) {
			buildTools = rcCfg.getToolsToInvoke();
		}
		if (buildTools == null || buildTools.length == 0) {
			buildTools = cfg.getFilteredTools();
		}

		String name = null;
		IPath path = null;
		for(int i = 0; i < buildTools.length; i++){
			ITool tool = buildTools[i];
			IInputType inputType = tool.getInputType(inputPath.getFileExtension());
			if(inputType != null){
				IOutputType prymOutType = tool.getPrimaryOutputType();
				String names[] = prymOutType.getOutputNames();
				if(names != null && names.length > 0)
					name = names[0];
			}
		}
		if(name != null){
			IPath namePath = new Path(name);
			if(namePath.isAbsolute()){
				path = namePath;
			}
			else{
				IPath cwd = getBuilderCWD(builder, cfg);
				if(cwd != null)
					path = cwd.append(namePath);
			}
		}
		return path;

	}

	/* (non-Javadoc)
	 * Returns the option that matches the option ID in this tool
	 */
	public IOption getOption(ITool tool, String optionId) {
		if (optionId == null) return null;

		//  Look for an option with this ID, or an option with a superclass with this id
		IOption[] options = tool.getOptions();
		for (int i = 0; i < options.length; i++) {
			IOption targetOption = options[i];
			IOption option = targetOption;
			do {
				if (optionId.equals(option.getId())) {
					return targetOption;
				}
				option = option.getSuperClass();
			} while (option != null);
		}

		return null;
	}

	private class IncludeDefaultsSubstitutor implements IVariableSubstitutor {
		private IOptionContextData fOptionContextData;

		public IncludeDefaultsSubstitutor(IOptionContextData data){
			fOptionContextData = data;
		}

		@Override
		public String resolveToString(String macroName) throws CdtVariableException {
			if(!"IncludeDefaults".equals(macroName)) 	//$NON-NLS-1$
				return CdtVariableResolver.createVariableReference(macroName);
			IOptionContextData parent = getParent(fOptionContextData);
			if(parent == null)
				return EMPTY_STRING;
			IncludeDefaultsSubstitutor sub = new IncludeDefaultsSubstitutor(parent);
			IOption option = parent.getOption();
			if (option==null)
				return null;

			String str = null;
			String strL[] = null;
			try{
				switch(option.getBasicValueType()){
				case IOption.STRING :
					str = option.getStringValue();
					break;
				case IOption.STRING_LIST :
					strL = option.getBasicStringListValue();
					break;
//				case IOption.INCLUDE_PATH :
//					strL = option.getIncludePaths();
//					break;
//				case IOption.PREPROCESSOR_SYMBOLS :
//					strL = option.getDefinedSymbols();
//					break;
//				case IOption.LIBRARIES :
//					strL = option.getLibraries();
//					break;
//				case IOption.OBJECTS :
//					strL = option.getUserObjects();
//					break;
				default :
					break;
				}

				if(str != null)
					return CdtVariableResolver.resolveToString(str,sub);
				else if(strL != null){
					strL = CdtVariableResolver.resolveStringListValues(strL,sub,true);
					return CdtVariableResolver.convertStringListToString(strL," "); 	//$NON-NLS-1$
				}
			} catch (BuildException e){

			} catch (CdtVariableException e){

			}
			return null;
		}

		@Override
		public String[] resolveToStringList(String macroName) throws CdtVariableException {
			if(!"IncludeDefaults".equals(macroName)) 	//$NON-NLS-1$
				return new String[]{CdtVariableResolver.createVariableReference(macroName)};

			IOptionContextData parent = getParent(fOptionContextData);
			if(parent == null)
				return new String[]{EMPTY_STRING};
			IncludeDefaultsSubstitutor sub = new IncludeDefaultsSubstitutor(parent);
			IOption option = parent.getOption();
			if (option==null)
				return null;

			String str = null;
			String strL[] = null;
			try{
				switch(option.getBasicValueType()){
				case IOption.STRING :
					str = option.getStringValue();
					break;
				case IOption.STRING_LIST :
					strL = option.getBasicStringListValue();
					break;
//				case IOption.INCLUDE_PATH :
//					strL = option.getIncludePaths();
//					break;
//				case IOption.PREPROCESSOR_SYMBOLS :
//					strL = option.getDefinedSymbols();
//					break;
//				case IOption.LIBRARIES :
//					strL = option.getLibraries();
//					break;
//				case IOption.OBJECTS :
//					strL = option.getUserObjects();
//					break;
				default :
					break;
				}

				if(str != null)
					return CdtVariableResolver.resolveToStringList(str,sub);
				else if(strL != null)
					return CdtVariableResolver.resolveStringListValues(strL,sub,true);
			} catch (BuildException e){

			} catch (CdtVariableException e){

			}
			return null;
		}

		public void setMacroContextInfo(int contextType, Object contextData) throws BuildMacroException {
		}

		public IMacroContextInfo getMacroContextInfo() {
			return null;
		}
	}

	public class OptionMacro extends BuildMacro{
//		private IOptionContextData fOptionContextData;
//		private IOptionContextData fParentOptionContextData;
//		private IOption fParentOption;
		private OptionMacro(String name, IOptionContextData optionContextData){
			fName = name;
//			fOptionContextData = optionContextData;
			IOptionContextData parentOptionContextData = getParent(optionContextData);
			load(optionContextData, parentOptionContextData);
		}

		private boolean load(IOptionContextData optionContextData, IOptionContextData parentOptionContextData){
			fStringValue = null;
			fStringListValue = null;
			fType = 0;
			if(parentOptionContextData != null){
				IOption option = parentOptionContextData.getOption();
				if (option!=null) {
					try{
						switch (option.getValueType()) {
						case IOption.BOOLEAN:
							break;
						case IOption.STRING:
							fType = IBuildMacro.VALUE_TEXT;
							fStringValue = option.getStringValue();
							break;
						case IOption.ENUMERATED:
							break;
						case IOption.STRING_LIST:
							fType = IBuildMacro.VALUE_TEXT_LIST;
							fStringListValue = option.getStringListValue();
							break;
						case IOption.INCLUDE_PATH:
							fType = IBuildMacro.VALUE_PATH_DIR_LIST;
							fStringListValue = option.getIncludePaths();
							break;
						case IOption.PREPROCESSOR_SYMBOLS:
							fType = IBuildMacro.VALUE_TEXT_LIST;
							fStringListValue = option.getDefinedSymbols();
							break;
						case IOption.LIBRARIES:
							fType = IBuildMacro.VALUE_TEXT_LIST;
							fStringListValue = option.getLibraries();
							break;
						case IOption.OBJECTS:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getUserObjects();
							break;
						case IOption.INCLUDE_FILES:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.LIBRARY_PATHS:
							fType = IBuildMacro.VALUE_PATH_DIR_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.LIBRARY_FILES:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.MACRO_FILES:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.UNDEF_INCLUDE_PATH:
							fType = IBuildMacro.VALUE_PATH_DIR_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
							fType = IBuildMacro.VALUE_TEXT_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.UNDEF_INCLUDE_FILES:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.UNDEF_LIBRARY_PATHS:
							fType = IBuildMacro.VALUE_PATH_DIR_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.UNDEF_LIBRARY_FILES:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						case IOption.UNDEF_MACRO_FILES:
							fType = IBuildMacro.VALUE_PATH_FILE_LIST;
							fStringListValue = option.getBasicStringListValue();
							break;
						}
						if(fStringValue != null)
							fStringValue = CdtVariableResolver.resolveToString(fStringValue,new IncludeDefaultsSubstitutor(parentOptionContextData));
						else if(fStringListValue != null)
							fStringListValue = CdtVariableResolver.resolveStringListValues(fStringListValue,new IncludeDefaultsSubstitutor(parentOptionContextData), true);
					}catch(Exception e){
						fType = 0;
					}
				}
			}

			boolean result = fType != 0;
			if(!result){
				fType = VALUE_TEXT;
				fStringListValue = null;
				fStringValue = null;
			}

			return result;
		}
	}

	private OptionData getParent(IOptionContextData optionContext){
		if(optionContext == null)
			return null;
		IOption option = optionContext.getOption();
		if(option == null)
			return null;

		IBuildObject parent = option.getParent();
		ITool tool = null;
		IToolChain tCh = null;
		IResourceInfo optionRcInfo = null;
		if (parent instanceof ITool) {
			tool = (ITool)parent;
			optionRcInfo = tool.getParentResourceInfo();
		} else if (parent instanceof IToolChain) {
			tCh = (IToolChain)parent;
			optionRcInfo = tCh.getParentFolderInfo();
		}

		if(optionRcInfo != null && optionRcInfo.isExtensionElement())
			optionRcInfo = null;

		IBuildObject parentObj = null;
		IOption parentOption = null;

		if(optionRcInfo != null){
			//only if optionRcInfo is not null
			IBuildObject bObj = (optionContext instanceof OptionData) ?
					((OptionData)optionContext).getOptionContainer() : optionContext.getParent();


			IResourceInfo rcInfo = null;
			IFileInfo fileInfo = null;
			IFolderInfo folderInfo = null;
			ITool holderTool = null;
			IToolChain holderTc = null;
			if(bObj instanceof ITool){
				holderTool = (ITool)bObj;
				rcInfo = holderTool.getParentResourceInfo();
			} else if(bObj instanceof IFileInfo) {
				fileInfo = (IFileInfo)bObj;
				rcInfo = fileInfo;
			} else if (bObj instanceof IFolderInfo) {
				folderInfo = (IFolderInfo)bObj;
				rcInfo = folderInfo;
				holderTc = folderInfo.getToolChain();
			} else if (bObj instanceof IToolChain) {
				holderTc = (IToolChain)bObj;
				folderInfo = holderTc.getParentFolderInfo();
			}

			if(rcInfo != null && rcInfo.isExtensionElement())
				rcInfo = null;

			IResourceInfo parentRcInfo = null;

			if(rcInfo != null){
				IPath optionRcPath = optionRcInfo.getPath();
				IPath rcPath = rcInfo.getPath();
				if(optionRcPath.isPrefixOf(rcPath)){
					parentRcInfo = ((ResourceInfo)optionRcInfo).getParentResourceInfo();
				} else {
					parentRcInfo = ((ResourceInfo)rcInfo).getParentResourceInfo();
				}
			}

			if(parentRcInfo != null){
				if(tool != null){
					ITool tools[] = parentRcInfo.getTools();
					ITool cur = tool;
					ITool found = null;
					do{
						String id = cur.getId();
						ITool []tmp = BuildSettingsUtil.getToolsBySuperClassId(tools, id);
						if(tmp.length != 0){
							found = tmp[0];
							break;
						}
						if(cur.isExtensionElement())
							break;
						cur = cur.getSuperClass();
					} while(cur != null);

					if(found != null){
						parentOption = getParentOption(found, option);
						if(parentOption != null){
							parentObj = found;
						}
					}
				} else if (tCh != null) {
					if(parentRcInfo instanceof IFolderInfo){
						IToolChain parentTc = ((IFolderInfo)parentRcInfo).getToolChain();
						parentOption = getParentOption(parentTc, option);
						if(parentOption != null){
							parentObj = parentTc;
						}
					}
				}
			}
		}

		if(parentObj == null)
			parentOption = null;
		if(parentOption == null)
			parentOption = option.getSuperClass();

		if(parentOption != null)
			return new OptionData(parentOption,optionContext.getParent(),parentObj);

		return null;
	}

	private IOption getParentOption(IHoldsOptions holder, IOption option){
		IOption cur = option;
		IOption found = null;
		do {
			String id = cur.getId();
			found = holder.getOptionBySuperClassId(id);
			if(found != null)
				break;

			if(cur.isExtensionElement())
				break;
			cur = cur.getSuperClass();
		} while (cur != null);
		return found;
	}

	private boolean canHandle(IOptionContextData optionData){
		IOption option = optionData.getOption();
		if(option == null)
			return false;

		boolean can = false;
		try{
			switch (option.getBasicValueType()) {
			case IOption.BOOLEAN:
				break;
			case IOption.STRING:
				can = true;
				break;
			case IOption.ENUMERATED:
				break;
			case IOption.STRING_LIST:
				can = true;
				break;
			case IOption.INCLUDE_PATH:
				can = true;
				break;
			case IOption.PREPROCESSOR_SYMBOLS:
				can = true;
				break;
			case IOption.LIBRARIES:
				can = true;
				break;
			case IOption.OBJECTS:
				can = true;
				break;
			}
		}catch(BuildException e){
			can = false;
		}
		return can;
	}

}
