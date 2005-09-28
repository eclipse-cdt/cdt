/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This class represents information about a Tool's inputs
 * and outputs while a Gnu makefile is being generated.
 */
public class ManagedBuildGnuToolInfo implements IManagedBuildGnuToolInfo {
	
	//private static final String EMPTY_STRING = new String();
	//private static final String OBJS_MACRO = "OBJS";			//$NON-NLS-1$
	private static final String DEPS_MACRO = "DEPS";			//$NON-NLS-1$

	/*
	 * Members
	 */
	private IProject project;
	private Tool tool;
	private boolean bIsTargetTool;
	private String targetName;
	private String targetExt;
	private boolean inputsCalculated = false;
	private boolean outputsCalculated = false;
	private boolean outputVariablesCalculated = false;
	private boolean dependenciesCalculated = false;
	private Vector commandInputs = new Vector();
	private Vector enumeratedInputs = new Vector();
	private Vector commandOutputs = new Vector();
	private Vector enumeratedPrimaryOutputs = new Vector();
	private Vector enumeratedSecondaryOutputs = new Vector();
	private Vector outputVariables = new Vector();
	private Vector commandDependencies = new Vector();
	//private Vector enumeratedDependencies = new Vector();
	// Map of macro names (String) to values (List)
	
	/*
	 * Constructor
	 */
	public ManagedBuildGnuToolInfo(IProject project, ITool tool, boolean targetTool, String name, String ext) {
		this.project = project;
		this.tool = (Tool)tool;
		bIsTargetTool = targetTool;
		if (bIsTargetTool) {
			targetName = name;
			targetExt = ext;
		}
	}
	
	/*
	 * IManagedBuildGnuToolInfo Methods
	 */
	public boolean areInputsCalculated() {
		return inputsCalculated;
	}

	//  Command inputs are top build directory relative
	public Vector getCommandInputs() {
		return commandInputs;
	}

	//  Enumerated inputs are project relative
	public Vector getEnumeratedInputs() {
		return enumeratedInputs;
	}

	public boolean areOutputsCalculated() {
		return outputsCalculated;
	}

	//  Command outputs are top build directory relative
	public Vector getCommandOutputs() {
		return commandOutputs;
	}

	public Vector getEnumeratedPrimaryOutputs() {
		return enumeratedPrimaryOutputs;
	}

	public Vector getEnumeratedSecondaryOutputs() {
		return enumeratedSecondaryOutputs;
	}

	public Vector getOutputVariables() {
		return outputVariables;
	}

	public boolean areOutputVariablesCalculated() {
		return outputVariablesCalculated;
	}

	public boolean areDependenciesCalculated() {
		return dependenciesCalculated;
	}

	public Vector getCommandDependencies() {
		return commandDependencies;
	}

	//public Vector getEnumeratedDependencies() {
	//	return enumeratedDependencies;
	//}

	public boolean isTargetTool() {
		return bIsTargetTool;
	}
	
	/*
	 * Other Methods
	 */
	
	public boolean calculateInputs(GnuMakefileGenerator makeGen, IConfiguration config, IResource[] projResources, boolean lastChance) {
		// Get the inputs for this tool invocation
		// Note that command inputs that are also dependencies are also added to the command dependencies list
		
		/* The priorities for determining the names of the inputs of a tool are:
		 *  1.  If an option is specified, use the value of the option.
		 *  2.  If a build variable is specified, use the files that have been added to the build variable as
		 *      the output(s) of other build steps.
		 *  3.  Use the file extensions and the resources in the project 
		 */
		boolean done = true;
		Vector myCommandInputs = new Vector();			// Inputs for the tool command line
		Vector myCommandDependencies = new Vector();	// Dependencies for the make rule
		Vector myEnumeratedInputs = new Vector();		// Complete list of individual inputs

		IInputType[] inTypes = tool.getInputTypes();
		if (inTypes != null && inTypes.length > 0) {
			for (int i=0; i<inTypes.length; i++) {
				IInputType type = inTypes[i];
				Vector itCommandInputs = new Vector();			// Inputs for the tool command line for this input-type
				Vector itCommandDependencies = new Vector();	// Dependencies for the make rule for this input-type
				Vector itEnumeratedInputs = new Vector();		// Complete list of individual inputs for this input-type				
				String variable = type.getBuildVariable();
				boolean primaryInput = type.getPrimaryInput();
				boolean useFileExts = false;
				IOption option = tool.getOptionBySuperClassId(type.getOptionId());
				IOption assignToOption = tool.getOptionBySuperClassId(type.getAssignToOptionId());
				
				//  Option?
				if (option != null) {
					try {
						List inputs = new ArrayList();
						int optType = option.getValueType();
						if (optType == IOption.STRING) {
							inputs.add(option.getStringValue());
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS) {
							inputs = (List)option.getValue();
						}
						for (int j=0; j<inputs.size(); j++) {
							String inputName = (String)inputs.get(j); 
							try{
								//try to resolve the build macros in the output names
								String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
										inputName,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_OPTION,
										new OptionContextData(option, config.getToolChain()));
								if((resolved = resolved.trim()).length() > 0)
									inputName = resolved;
							} catch (BuildMacroException e){
							}
	
							if (primaryInput) {
								itCommandDependencies.add(j, inputName);
							} else {
								itCommandDependencies.add(inputName);
							}
							// NO - itCommandInputs.add(inputName);
							// NO - itEnumeratedInputs.add(inputName);
						}
					} catch( BuildException ex ) {
					}
					
				} else {

					//  Build Variable?
					if (variable.length() > 0) {
						String cmdVariable = variable = "$(" + variable + ")";			//$NON-NLS-1$	//$NON-NLS-2$
						itCommandInputs.add(cmdVariable);
						if (primaryInput) {
							itCommandDependencies.add(0, cmdVariable);
						} else {
							itCommandDependencies.add(cmdVariable);
						}
						// If there is an output variable with the same name, get
						// the files associated with it.
						List outMacroList = makeGen.getBuildVariableList(variable, GnuMakefileGenerator.PROJECT_RELATIVE,
								null, true);
						if (outMacroList != null) {
							itEnumeratedInputs.addAll(outMacroList);
						} else {
							// If "last chance", then calculate using file extensions below
							if (lastChance) {
								useFileExts = true;
							} else {
								done = false;
								break;
							}
						}
					}
					
					//  Use file extensions
					if (variable.length() == 0 || useFileExts) {
						//if (type.getMultipleOfType()) {
							// Calculate EnumeratedInputs using the file extensions and the resources in the project
							// Note:  This is only correct for tools with multipleOfType == true, but for other tools
							//        it gives us an input resource for generating default names
							// Determine the set of source input macros to use
					 		HashSet handledInputExtensions = new HashSet();
							String[] exts = type.getSourceExtensions(tool);
							if (projResources != null) {
								for (int j=0; j<projResources.length; j++) {
									if (projResources[j].getType() == IResource.FILE) {
										String fileExt = projResources[j].getFileExtension();
										
										// fix for NPE, bugzilla 99483
										if(fileExt == null)
										{
											fileExt = "";  //$NON-NLS-1$
										}
										
										for (int k=0; k<exts.length; k++) {
											if (fileExt.equals(exts[k])) {
												if (!useFileExts) {
													if(!handledInputExtensions.contains(fileExt)) {
									 					handledInputExtensions.add(fileExt);
									 					String buildMacro = "$(" + makeGen.getSourceMacroName(fileExt).toString() + ")";	//$NON-NLS-1$ //$NON-NLS-2$
														itCommandInputs.add(buildMacro);
														if (primaryInput) {
															itCommandDependencies.add(0, buildMacro);
														} else {
															itCommandDependencies.add(buildMacro);
														}
									 				}
												}
												if (type.getMultipleOfType() || itEnumeratedInputs.size() == 0) {
													//  Add a path that is relative to the project directory
													itEnumeratedInputs.add(projResources[j].getProjectRelativePath().toString());
												}
												break;
											}
										}
									}
								}
							}
						//}
					}
				}
				
				// Get any additional inputs specified in the manifest file or the project file
				IAdditionalInput[] addlInputs = type.getAdditionalInputs();
				if (addlInputs != null) {
					for (int j=0; j<addlInputs.length; j++) {
						IAdditionalInput addlInput = addlInputs[j];
						int kind = addlInput.getKind();
						if (kind == IAdditionalInput.KIND_ADDITIONAL_INPUT ||
							kind == IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY) {
							String[] paths = addlInput.getPaths();
							if (paths != null) {
								for (int k = 0; k < paths.length; k++) {
									String path = paths[k];
									itEnumeratedInputs.add(path);
									// Translate the path from project relative to
									// build directory relative
									if (!(path.startsWith("$("))) {		//$NON-NLS-1$
										IResource addlResource = project.getFile(path);
										if (addlResource != null) {
											IPath addlPath = addlResource.getLocation();
											if (addlPath != null) {
												path = ManagedBuildManager.calculateRelativePath(makeGen.getTopBuildDir(), addlPath).toString();
											}
										}
									}
									itCommandInputs.add(path);
								}
							}
						}
					}
				}
				
				//  If the assignToOption attribute is specified, set the input(s) as the value of that option
				if (assignToOption != null && option == null) {
					try {
						int optType = assignToOption.getValueType();
						if (optType == IOption.STRING) {
							String optVal = "";	   //$NON-NLS-1$
							for (int j=0; j<itCommandInputs.size(); j++) {
								if (j != 0) {
									optVal += " ";	   //$NON-NLS-1$
								}
								optVal += itCommandInputs.get(j);
							}
							ManagedBuildManager.setOption(config, tool, assignToOption, optVal);							
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS) {
							//  Mote that when using the enumerated inputs, the path(s) must be translated from project relative 
							//  to top build directory relative
							String[] paths = new String[itEnumeratedInputs.size()];
							for (int j=0; j<itEnumeratedInputs.size(); j++) {
								paths[j] = (String)itEnumeratedInputs.get(j);
								IResource enumResource = project.getFile(paths[j]);
								if (enumResource != null) {
									IPath enumPath = enumResource.getLocation();
									if (enumPath != null) {
										paths[j] = ManagedBuildManager.calculateRelativePath(makeGen.getTopBuildDir(), enumPath).toString();
									}
								}								
							}
							ManagedBuildManager.setOption(config, tool, assignToOption, paths);
						} else if (optType == IOption.BOOLEAN) {
							if (itEnumeratedInputs.size() > 0) {
								ManagedBuildManager.setOption(config, tool, assignToOption, true);
							} else {
								ManagedBuildManager.setOption(config, tool, assignToOption, false);
							}
						} else if (optType == IOption.ENUMERATED) {
							if (itCommandInputs.size() > 0) {
								ManagedBuildManager.setOption(config, tool, assignToOption, (String)itCommandInputs.firstElement());
							}
						}
						itCommandInputs.removeAllElements();
						//itEnumeratedInputs.removeAllElements();
					} catch( BuildException ex ) {
					}
				}
				
				myCommandInputs.addAll(itCommandInputs);
				myCommandDependencies.addAll(itCommandDependencies);
				myEnumeratedInputs.addAll(itEnumeratedInputs);
			}
		} else {
			// For support of pre-CDT 3.0 integrations.
			if (bIsTargetTool) {
				// NOTE WELL:  This only supports the case of a single "target tool"
				//      with the following characteristics:
				// 1.  The tool consumes exactly all of the object files produced
				//     by other tools in the build and produces a single output
				// 2.  The target name comes from the configuration artifact name
				// The rule looks like:
				//    <targ_prefix><target>.<extension>: $(OBJS) <refd_project_1 ... refd_project_n>
				myCommandInputs.add("$(OBJS)");			 //$NON-NLS-1$		
				myCommandInputs.add("$(USER_OBJS)");	 //$NON-NLS-1$		
				myCommandInputs.add("$(LIBS)");			 //$NON-NLS-1$
			} else {
				// Rule will be generated by addRuleForSource
			}
		}		
		
		if (done) {
			commandInputs.addAll(myCommandInputs);
			commandDependencies.addAll(0, myCommandDependencies);
			enumeratedInputs.addAll(myEnumeratedInputs);
			inputsCalculated = true;
			return true;
		}
				
		return false;		
	}
	
	 /*
	 * The priorities for determining the names of the outputs of a tool are:
	 *  1.  If the tool is the build target and primary output, use artifact name & extension
	 *  2.  If an option is specified, use the value of the option
	 *  3.  If a nameProvider is specified, call it
	 *  4.  If outputNames is specified, use it
	 *  5.  Use the name pattern to generate a transformation macro 
	 *      so that the source names can be transformed into the target names 
	 *      using the built-in string substitution functions of <code>make</code>.
	 *      
	 * NOTE: If an option is not specified and this is not the primary output type, the outputs
	 *       from the type are not added to the command line     
	 */  
	public boolean calculateOutputs(GnuMakefileGenerator makeGen, IConfiguration config, HashSet handledInputExtensions, boolean lastChance) {

		boolean done = true;
		Vector myCommandOutputs = new Vector();
		Vector myEnumeratedPrimaryOutputs = new Vector();
		Vector myEnumeratedSecondaryOutputs = new Vector();
	    HashMap myOutputMacros = new HashMap();
		//  The next two fields are used together
		Vector myBuildVars = new Vector();
		Vector myBuildVarsValues = new Vector();
		
		// Get the outputs for this tool invocation
		IOutputType[] outTypes = tool.getOutputTypes();
		if (outTypes != null && outTypes.length > 0) {
			for (int i=0; i<outTypes.length; i++) {
				Vector typeEnumeratedOutputs = new Vector();
				IOutputType type = outTypes[i];
				String outputPrefix = type.getOutputPrefix();
				String variable = type.getBuildVariable();
				boolean multOfType = type.getMultipleOfType();
				boolean primaryOutput = (type == tool.getPrimaryOutputType());
				IOption option = tool.getOptionBySuperClassId(type.getOptionId());
				IManagedOutputNameProvider nameProvider = type.getNameProvider();
				String[] outputNames = type.getOutputNames();
				
				//  1.  If the tool is the build target and this is the primary output, 
				//      use artifact name & extension
				if (bIsTargetTool && primaryOutput) {
					String outputName = outputPrefix + targetName; 
					if (targetExt.length() > 0) {
						outputName += (DOT + targetExt);
					}
					myCommandOutputs.add(outputName);
					typeEnumeratedOutputs.add(outputName);
					//  But this doesn't use any output macro...
				} else 
				//  2.  If an option is specified, use the value of the option
				if (option != null) {
					try {
						List outputs = new ArrayList();
						int optType = option.getValueType();
						if (optType == IOption.STRING) {
							outputs.add(outputPrefix + option.getStringValue());
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS) {
							outputs = (List)option.getValue();
							// Add outputPrefix to each if necessary
							if (outputPrefix.length() > 0) {
								for (int j=0; j<outputs.size(); j++) {
									outputs.set(j, outputPrefix + outputs.get(j));
								}
							}
						}
						for (int j=0; j<outputs.size(); j++) {
							String outputName = (String)outputs.get(j); 
							try{
								//try to resolve the build macros in the output names
								String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
										outputName,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_OPTION,
										new OptionContextData(option, config.getToolChain()));
								if((resolved = resolved.trim()).length() > 0)
									outputs.set(j, resolved);
							} catch (BuildMacroException e){
							}
						}
						
						// NO - myCommandOutputs.addAll(outputs);
						typeEnumeratedOutputs.addAll(outputs);
						if (variable.length() > 0) {
							List outputPaths = new ArrayList();
							for (int j=0; j<outputs.size(); j++) {
								outputPaths.add(Path.fromOSString((String)outputs.get(j)));
							}
							if (myOutputMacros.containsKey(variable)) {
								List currList = (List)myOutputMacros.get(variable);
								currList.addAll(outputPaths);
								myOutputMacros.put(variable, currList);
							} else {
								myOutputMacros.put(variable, outputPaths);
							}
						}
					} catch( BuildException ex ) {
					}
				} else 
				//  3.  If a nameProvider is specified, call it
				if (nameProvider != null) {
					// The inputs must have been calculated before we can do this
					IPath[] outNames = null;
					if (!inputsCalculated) {
						done = false;
					} else {
						Vector inputs = getEnumeratedInputs();
						IPath[] inputPaths = new IPath[inputs.size()];
						for (int j=0; j<inputPaths.length; j++) {
							inputPaths[j] = Path.fromOSString((String)inputs.get(j));
						}
						outNames = nameProvider.getOutputNames(tool, inputPaths);
						if (outNames != null) {
							for (int j=0; j<outNames.length; j++) {
								String outputName = outNames[j].toString(); 
								try{
									//try to resolve the build macros in the output names
									String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
											outputName,
											"", //$NON-NLS-1$
											" ", //$NON-NLS-1$
											IBuildMacroProvider.CONTEXT_CONFIGURATION,
											config);
									if((resolved = resolved.trim()).length() > 0) {
										outputName = resolved;
										outNames[j] = Path.fromOSString(resolved);
									}
								} catch (BuildMacroException e){
								}

								if (primaryOutput) {
									myCommandOutputs.add(outputName);
								}
								typeEnumeratedOutputs.add(outputName);
							}
						}
					}
					if (variable.length() > 0 && outNames != null) {
						if (myOutputMacros.containsKey(variable)) {
							List currList = (List)myOutputMacros.get(variable);
							currList.addAll(Arrays.asList(outNames));
							myOutputMacros.put(variable, currList);
						} else {
							myOutputMacros.put(variable, Arrays.asList(outNames));
						}
					}
				} else
				//  4.  If outputNames is specified, use it
				if (outputNames != null) {
					if (outputNames.length > 0) {
						for (int j=0; j<outputNames.length; j++) {
							String outputName = outputNames[j]; 
							try{
								//try to resolve the build macros in the output names
								String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
										outputName,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_OPTION,
										new OptionContextData(option, config.getToolChain()));
								if((resolved = resolved.trim()).length() > 0)
									outputNames[j] = resolved;
							} catch (BuildMacroException e){
							}
						}
						List namesList = Arrays.asList(outputNames);
						if (primaryOutput) {
							myCommandOutputs.addAll(namesList);
						}
						typeEnumeratedOutputs.addAll(namesList);
						if (variable.length() > 0) {
							List outputPaths = new ArrayList();
							for (int j=0; j<namesList.size(); j++) {
								outputPaths.add(Path.fromOSString((String)namesList.get(j)));
							}
							if (myOutputMacros.containsKey(variable)) {
								List currList = (List)myOutputMacros.get(variable);
								currList.addAll(outputPaths);
								myOutputMacros.put(variable, currList);
							} else {
								myOutputMacros.put(variable, outputPaths);
							}
						}
					}
				} else {
				//  5.  Use the name pattern to generate a transformation macro 
				//      so that the source names can be transformed into the target names 
				//      using the built-in string substitution functions of <code>make</code>.
					if (multOfType) {
						// This case is not handled - a nameProvider or outputNames must be specified
						List errList = new ArrayList();
						errList.add(ManagedMakeMessages.getResourceString("MakefileGenerator.error.no.nameprovider"));	//$NON-NLS-1$
						myCommandOutputs.add(errList);
					} else {
						String namePattern = type.getNamePattern();
						if (namePattern == null || namePattern.length() == 0) {
							namePattern = outputPrefix + IManagedBuilderMakefileGenerator.WILDCARD;
							String outExt = (type.getOutputExtensions(tool))[0];
							if (outExt != null && outExt.length() > 0) {
								namePattern += DOT + outExt;
							}
						}
						else if (outputPrefix.length() > 0) {
							namePattern = outputPrefix + namePattern;
						}
						
						// Calculate the output name
						// The inputs must have been calculated before we can do this
						if (!inputsCalculated) {
							done = false;
						} else {
							Vector inputs = getEnumeratedInputs();
							String fileName;
							if (inputs.size() > 0) {
								//  Get the input file name
								fileName = (Path.fromOSString((String)inputs.get(0))).removeFileExtension().lastSegment();
								//  Check if this is a build macro.  If so, use the raw macro name.
								if (fileName.startsWith("$(") && fileName.endsWith(")")) {	//$NON-NLS-1$ //$NON-NLS-2$
									fileName = fileName.substring(2,fileName.length()-1);
								}
							} else {
								fileName = "default"; //$NON-NLS-1$
							}
							//  Replace the % with the file name
							if (primaryOutput) {
								myCommandOutputs.add(namePattern.replaceAll("%", fileName)); //$NON-NLS-1$
							}
							typeEnumeratedOutputs.add(namePattern.replaceAll("%", fileName)); //$NON-NLS-1$
							if (variable.length() > 0) {
								List outputs = new ArrayList();
								outputs.add(Path.fromOSString(fileName));
								if (myOutputMacros.containsKey(variable)) {
									List currList = (List)myOutputMacros.get(variable);
									currList.addAll(outputs);
									myOutputMacros.put(variable, currList);
								} else {
									myOutputMacros.put(variable, outputs);
								}
							}
						}
					}
				}					
				if (variable.length() > 0) {
					myBuildVars.add(variable);
					myBuildVarsValues.add(typeEnumeratedOutputs);
				}
				if (primaryOutput) {
					myEnumeratedPrimaryOutputs.addAll(typeEnumeratedOutputs);
				} else {
					myEnumeratedSecondaryOutputs.addAll(typeEnumeratedOutputs);
				}
			}
		} else {
			if (bIsTargetTool) {
				String outputPrefix = tool.getOutputPrefix();
				String outputName = outputPrefix + targetName; 
				if (targetExt.length() > 0) {
					outputName += (DOT + targetExt);
				}
				myCommandOutputs.add(outputName);
				myEnumeratedPrimaryOutputs.add(outputName);
			} else {
				// For support of pre-CDT 3.0 integrations.
				// NOTE WELL:  This only supports the case of a single "target tool"
				//     that consumes exactly all of the object files, $OBJS, produced
				//     by other tools in the build and produces a single output
			}
		}
		
		//  Add the output macros of this tool to the buildOutVars map 
		Iterator iterator = myOutputMacros.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			List newMacroValue = (List)entry.getValue();
			HashMap map = makeGen.getBuildOutputVars(); 
			if (map.containsKey(macroName)) {
				List macroValue = (List)map.get(macroName);
				macroValue.addAll(newMacroValue);
				map.put(macroName, macroValue);
			} else {
				map.put(macroName, newMacroValue);
			}
		}
		outputVariablesCalculated = true;

		if (done) {
			commandOutputs.addAll(myCommandOutputs);
			enumeratedPrimaryOutputs.addAll(myEnumeratedPrimaryOutputs);
			enumeratedSecondaryOutputs.addAll(myEnumeratedSecondaryOutputs);
			outputVariables.addAll(myOutputMacros.keySet());
			outputsCalculated = true;
			for (int i=0; i<myBuildVars.size(); i++) {
				makeGen.addMacroAdditionFiles(makeGen.getTopBuildOutputVars(), (String)myBuildVars.get(i), (Vector)myBuildVarsValues.get(i)); 
			}			
			return true;
		}
				
		return false;		
	}
	
	public boolean calculateDependencies(GnuMakefileGenerator makeGen, HashSet handledInputExtensions, boolean lastChance) {
		// Get the dependencies for this tool invocation
		boolean done = true;
		Vector myCommandDependencies = new Vector();
		//Vector myEnumeratedDependencies = new Vector();
	    HashMap myOutputMacros = new HashMap();

		IInputType[] inTypes = tool.getInputTypes();
		if (inTypes != null && inTypes.length > 0) {
			for (int i=0; i<inTypes.length; i++) {
				IInputType type = inTypes[i];
				
				IManagedDependencyGenerator depGen = type.getDependencyGenerator();
				if (depGen != null) {
					int calcType = depGen.getCalculatorType();
					switch (calcType) {
					case IManagedDependencyGenerator.TYPE_COMMAND:
			 			// iterate over all extensions that the tool knows how to handle
						String[] extensionsList = type.getSourceExtensions(tool);
			 			for (int j=0; j<extensionsList.length; j++) {
			 				String extensionName = extensionsList[j];
			 				
			 				// Generated files should not appear in the list.
			 				if(!makeGen.getOutputExtensions().contains(extensionName) && !handledInputExtensions.contains(extensionName)) {
			 					handledInputExtensions.add(extensionName);
								String depsMacro = calculateSourceMacro(makeGen, extensionName, IManagedBuilderMakefileGenerator.DEP_EXT,
										IManagedBuilderMakefileGenerator.WILDCARD);

								List depsList = new ArrayList();
								depsList.add(Path.fromOSString(depsMacro));
								if (myOutputMacros.containsKey(DEPS_MACRO)) {
									List currList = (List)myOutputMacros.get(DEPS_MACRO);
									currList.addAll(depsList);
									myOutputMacros.put(DEPS_MACRO, currList);
								} else {
									myOutputMacros.put(DEPS_MACRO, depsList);
								}
			 				}
			 			}											
						break;

					case IManagedDependencyGenerator.TYPE_INDEXER:
					case IManagedDependencyGenerator.TYPE_EXTERNAL:
						// The inputs must have been calculated before we can do this
						if (!inputsCalculated) {
							done = false;
						} else {
							Vector inputs = getEnumeratedInputs();
							for (int j=0; j<inputs.size(); j++) {
								IResource[] outNames = depGen.findDependencies(project.getFile((String)inputs.get(j)), project);
								if (outNames != null) {
									for (int k=0; k<outNames.length; k++) {
										myCommandDependencies.add(outNames[k].toString());
									}
								}								
							}
						}
						break;

					default:
						break;
					}
				}
				
				// Add additional dependencies specified in AdditionalInput elements
				IAdditionalInput[] addlInputs = type.getAdditionalInputs();
				if (addlInputs != null && addlInputs.length > 0) {
					for (int j=0; j<addlInputs.length; j++) {
						IAdditionalInput addlInput = addlInputs[j];
						int kind = addlInput.getKind();
						if (kind == IAdditionalInput.KIND_ADDITIONAL_DEPENDENCY ||
							kind == IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY) {
							String[] paths = addlInput.getPaths();
							if (paths != null) {
								for (int k = 0; k < paths.length; k++) {
									// Translate the path from project relative to
									// build directory relative
									String path = paths[k];
									if (!(path.startsWith("$("))) {		//$NON-NLS-1$
										IResource addlResource = project.getFile(path);
										if (addlResource != null) {
											IPath addlPath = addlResource.getLocation();
											if (addlPath != null) {
												path = ManagedBuildManager.calculateRelativePath(makeGen.getTopBuildDir(), addlPath).toString();
											}
										}
									}
									myCommandDependencies.add(path);
									//myEnumeratedInputs.add(path);
								}
							}
						}
					}
				}
			}
		} else {
			if (bIsTargetTool) {
				// For support of pre-CDT 3.0 integrations.
				// NOTE WELL:  This only supports the case of a single "target tool"
				//      with the following characteristics:
				// 1.  The tool consumes exactly all of the object files produced
				//     by other tools in the build and produces a single output
				// 2.  The target name comes from the configuration artifact name
				// The rule looks like:
				//    <targ_prefix><target>.<extension>: $(OBJS) <refd_project_1 ... refd_project_n>
				myCommandDependencies.add("$(OBJS)");			 //$NON-NLS-1$		
				myCommandDependencies.add("$(USER_OBJS)");	 //$NON-NLS-1$		
			} else {
	 			String[] extensionsList = tool.getAllInputExtensions();
	 			
				// Handle dependencies from the dependencyCalculator
				IManagedDependencyGenerator depGen = tool.getDependencyGenerator();
				if (depGen != null) {
					int calcType = depGen.getCalculatorType();
					switch (calcType) {
					case IManagedDependencyGenerator.TYPE_COMMAND:
			 			// iterate over all extensions that the tool knows how to handle
			 			for (int i=0; i<extensionsList.length; i++) {
			 				String extensionName = extensionsList[i];
			 				
			 				// Generated files should not appear in the list.
			 				if(!makeGen.getOutputExtensions().contains(extensionName) && !handledInputExtensions.contains(extensionName)) {
			 					handledInputExtensions.add(extensionName);
								String depsMacro = calculateSourceMacro(makeGen, extensionName, IManagedBuilderMakefileGenerator.DEP_EXT,
										IManagedBuilderMakefileGenerator.WILDCARD);

								List depsList = new ArrayList();
								depsList.add(Path.fromOSString(depsMacro));
								if (myOutputMacros.containsKey(DEPS_MACRO)) {
									List currList = (List)myOutputMacros.get(DEPS_MACRO);
									currList.addAll(depsList);
									myOutputMacros.put(DEPS_MACRO, currList);
								} else {
									myOutputMacros.put(DEPS_MACRO, depsList);
								}
			 				}
			 			}											
						break;

					case IManagedDependencyGenerator.TYPE_INDEXER:
					case IManagedDependencyGenerator.TYPE_EXTERNAL:
						// The inputs must have been calculated before we can do this
						if (!inputsCalculated) {
							done = false;
						} else {
							Vector inputs = getEnumeratedInputs();
							for (int j=0; j<inputs.size(); j++) {
								IResource[] outNames = depGen.findDependencies(project.getFile((String)inputs.get(j)), project);
								if (outNames != null) {
									for (int k=0; k<outNames.length; k++) {
										myCommandDependencies.add(outNames[k].toString());
									}
								}								
							}
						}
						break;

					default:
						break;
					}
				}
			}
		}
		
		//  Add the output macros of this tool to the buildOutVars map 
		Iterator iterator = myOutputMacros.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			List newMacroValue = (List)entry.getValue();
			HashMap map = makeGen.getBuildOutputVars(); 
			if (map.containsKey(macroName)) {
				List macroValue = (List)map.get(macroName);
				macroValue.addAll(newMacroValue);
				map.put(macroName, macroValue);
			} else {
				map.put(macroName, newMacroValue);
			}
		}
		
		if (done) {
			commandDependencies.addAll(myCommandDependencies);
			//enumeratedDependencies.addAll(myEnumeratedDependencies);
			dependenciesCalculated = true;
			return true;
		}
				
		return false;		
	}


	/*
	 * Calculate the source macro for the given extension
	 */
	protected String calculateSourceMacro(GnuMakefileGenerator makeGen, String srcExtensionName, String outExtensionName, String wildcard) {
		StringBuffer macroName = makeGen.getSourceMacroName(srcExtensionName);
		String OptDotExt = ""; //$NON-NLS-1$
		if (outExtensionName != null) {
		    OptDotExt = DOT + outExtensionName; 
		} else
			if (tool.getOutputExtension(srcExtensionName) != "") //$NON-NLS-1$
				OptDotExt = DOT + tool.getOutputExtension(srcExtensionName); 
			           
		// create rule of the form
		// OBJS = $(macroName1: $(ROOT)/%.input1=%.output1) ... $(macroNameN: $(ROOT)/%.inputN=%.outputN)
		StringBuffer objectsBuffer = new StringBuffer();
		objectsBuffer.append(IManagedBuilderMakefileGenerator.WHITESPACE + "$(" + macroName + 					//$NON-NLS-1$
			IManagedBuilderMakefileGenerator.COLON + "$(ROOT)" + 												//$NON-NLS-1$
			IManagedBuilderMakefileGenerator.SEPARATOR + IManagedBuilderMakefileGenerator.WILDCARD +			
				DOT + srcExtensionName + "=" + wildcard + OptDotExt + ")" );	//$NON-NLS-1$ //$NON-NLS-2$
        return objectsBuffer.toString();
	}
	
}
