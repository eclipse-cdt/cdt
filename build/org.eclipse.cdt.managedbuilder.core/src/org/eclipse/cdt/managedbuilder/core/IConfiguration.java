/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * A tool-integrator defines default configurations as children of the project type.
 * These provide a template for the configurations added to the user's project, 
 * which are stored in the project's .cdtbuild file.  
 * <p>
 * The configuration contains one child of type tool-chain.  This describes how the 
 * project's resources are transformed into the build artifact. The configuration can
 * contain one or more children of type resourceConfiguration.  These describe build
 * settings of individual resources that are different from the configuration as a whole.
 * 
 * @since 2.1
 */
public interface IConfiguration extends IBuildObject {
	public static final String ARTIFACT_NAME = "artifactName";	//$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cleanCommand";	//$NON-NLS-1$
    public static final String PREBUILD_STEP = "prebuildStep";      //$NON-NLS-1$ 
    public static final String POSTBUILD_STEP = "postbuildStep";    //$NON-NLS-1$ 
    public static final String PREANNOUNCEBUILD_STEP = "preannouncebuildStep";      //$NON-NLS-1$ 
    public static final String POSTANNOUNCEBUILD_STEP = "postannouncebuildStep";    //$NON-NLS-1$ 
	// Schema element names
	public static final String CONFIGURATION_ELEMENT_NAME = "configuration";	//$NON-NLS-1$
	public static final String ERROR_PARSERS = "errorParsers";	//$NON-NLS-1$
	public static final String EXTENSION = "artifactExtension";	//$NON-NLS-1$
	public static final String PARENT = "parent";	//$NON-NLS-1$
	
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$

	
	
	/**
	 * Returns the description of the configuration.
	 * 
	 * @return String
	 */
	public String getDescription();
	
	/**
	 * Sets the description of the receiver to the value specified in the argument
	 * 
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 * Creates a child resource configuration corresponding to the passed in file.
	 * 
	 * @param file
	 * @return IResourceConfiguration
	 */
	public IResourceConfiguration createResourceConfiguration(IFile file);

	/**
	 * Creates the <code>IToolChain</code> child of this configuration.
	 *
	 * @param ToolChain The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * 
	 * @return IToolChain
	 */
	public IToolChain createToolChain(IToolChain superClass, String Id, String name, boolean isExtensionElement);
	
	/**
	 * Returns the extension that should be applied to build artifacts created by 
	 * this configuration.
	 * 
	 * @return String
	 */
	public String getArtifactExtension();	

	/**
	 * Returns the name of the final build artifact.
	 * 
	 * @return String
	 */
	public String getArtifactName();

	/**
	 * Returns the build arguments from this configuration's builder 
	 * 
	 * @return String
	 */
	public String getBuildArguments();

	/**
	 * Returns the build command from this configuration's builder 
	 * 
	 * @return String
	 */
	public String getBuildCommand();
	
	/**
     * Returns the prebuild step command
     * 
     * @return String 
     */ 
    public String getPrebuildStep(); 
       
    /** 
     * Returns the postbuild step command 
     * 
     * @return String 
     */ 
    public String getPostbuildStep(); 
 
    /** 
     * Returns the display string associated with the prebuild step 
     * 
     * @return String 
     */ 
    public String getPreannouncebuildStep(); 
       
    /** 
     * Returns the display string associated with the postbuild step 
     * 
     * @return String 
     */ 
    public String getPostannouncebuildStep();       
       
    /** 
	 * Answers the OS-specific command to remove files created by the build
	 * of this configuration.
	 *  
	 * @return String
	 */
	public String getCleanCommand();

	/**
	 * Answers the semicolon separated list of unique IDs of the error parsers associated 
	 * with this configuration.
	 * 
	 * @return String
	 */
	public String getErrorParserIds();

	/**
	 * Answers the ordered list of unique IDs of the error parsers associated 
	 * with this configuration.
	 * 
	 * @return String[]
	 */
	public String[] getErrorParserList();
	
	/**
	 * Projects have C or CC natures. Tools can specify a filter so they are not 
	 * misapplied to a project. This method allows the caller to retrieve a list 
	 * of tools from a project that are correct for a project's nature.  
	 * 
	 * @return an array of <code>ITools</code> that have compatible filters 
	 * for this configuration.
	 */
	ITool[] getFilteredTools();
	
	/**
	 * Returns the managed-project parent of this configuration, if this is a
	 * project configuration.  Otherwise, returns <code>null</code>.
	 * 
	 * @return IManagedProject
	 */
	public IManagedProject getManagedProject();
	
	/**
	 * Returns the Eclipse project that owns the configuration.
	 * 
	 * @return IResource
	 */
	public IResource getOwner();
	
	/**
	 * Returns the configuration that this configuration is based on. 
	 * 
	 * @return IConfiguration
	 */
	public IConfiguration getParent();
	
	/**
	 * Returns the project-type parent of this configuration, if this is an
	 * extension configuration.  Otherwise, returns <code>null</code>.
	 * 
	 * @return IProjectType
	 */
	public IProjectType getProjectType();
	
	/**
	 * Returns the resource configuration child of this configuration
	 * that is associated with the project resource, or <code>null</code> if none.
	 * 
	 * @return IResourceConfiguration
	 */
	public IResourceConfiguration getResourceConfiguration(String path);
	
	/**
	 * Returns the resource configuration children of this configuration.
	 * 
	 * @return IResourceConfigurations[]
	 */
	public IResourceConfiguration[] getResourceConfigurations();
	
	/**
	 * Returns the <code>ITool</code> in this configuration's tool-chain with
	 * the same id as the argument, or <code>null</code>. 
	 * 
	 * @param id unique identifier to search for
	 * @return ITool
	 */
	public ITool getTool(String id);
	
	/**
	 * Returns the <code>IToolChain</code> child of this configuration.
	 * 
	 * @return IToolChain
	 */
	public IToolChain getToolChain();
	
	/**
	 * Returns the command-line invocation command for the specified tool.
	 * 
	 * @param tool The tool that will have its command retrieved.
	 * @return String The command
	 */
	public String getToolCommand(ITool tool);
	
	/**
	 * Returns the tools that are used in this configuration's tool-chain.
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();

	/**
	 * Returns the tool in this configuration that creates the build artifact.  
	 * 
	 * @return ITool
	 */
	public ITool getTargetTool();

	/**
	 * Returns <code>true</code> if this configuration has overridden the default build
	 * build command in this configuration, otherwise <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean hasOverriddenBuildCommand();
	
	/**
	 * Returns <code>true</code> if this configuration has changes that need to 
	 * be saved in the project file, else <code>false</code>.  
	 * Should not be called for an extension configuration.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Returns <code>true</code> if this configuration was loaded from a manifest file,
	 * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
	 * 
	 * @return boolean 
	 */
	public boolean isExtensionElement();

	/**
	 * Returns whether this configuration has been changed and requires the 
	 * project to be rebuilt.
	 * 
	 * @return <code>true</code> if the configuration contains a change 
	 * that needs the project to be rebuilt.
	 * Should not be called for an extension configuration.
	 */
	public boolean needsRebuild();

	/**
	 * Removes a resource configuration from the configuration's list.
	 * 
	 * @param option
	 */
	public void removeResourceConfiguration(IResourceConfiguration resConfig);
	
	/**
	 * Set (override) the extension that should be appended to the build artifact
	 * for the receiver.
	 *  
	 * @param extension
	 */
	public void setArtifactExtension(String extension);

	/**
	 * Set the name of the artifact that will be produced when the receiver
	 * is built.
	 * 
	 * @param name
	 */
	public void setArtifactName(String name);

	/**
	 * Sets the arguments to be passed to the build utility used by the 
	 * receiver to produce a build goal.
	 * 
	 * @param makeArgs
	 */
	public void setBuildArguments(String makeArgs);

	/**
	 * Sets the build command for the receiver to the value in the argument.
	 * 
	 * @param command
	 */
	public void setBuildCommand(String command);

	/**
     * Sets the prebuild step for the receiver to the value in the argument. 
     * 
     * @param step 
     */ 
    public void setPrebuildStep(String step); 
   
    /** 
     * Sets the postbuild step for the receiver to the value in the argument. 
     * 
     * @param step 
     */ 
    public void setPostbuildStep(String step); 
 
    /** 
     * Sets the prebuild step display string for the receiver to the value in the argument. 
     * 
     * @param announceStep 
     */     
    public void setPreannouncebuildStep(String announceStep); 
   
    /** 
     * Sets the postbuild step display string for the receiver to the value in the argument. 
     * 
     * @param announceStep 
     */     
    public void setPostannouncebuildStep(String announceStep); 
   
    /** 
	 * Sets the command used to clean the outputs of this configuration.
	 * 
	 * @param name
	 */
	public void setCleanCommand(String command);

	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);

	/**
	 * Sets the semicolon separated list of error parser ids
	 * 
	 * @param ids
	 */
	public void setErrorParserIds(String ids);

	/**
	 * Sets the name of the receiver to the value specified in the argument
	 * 
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Sets the value of a boolean option for this configuration.
	 * 
	 * @param tool The Tool parent of the option.
	 * @param option The option to change.
	 * @param value The value to apply to the option.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @throws BuildException
	 */
	public IOption setOption(ITool tool, IOption option, boolean value) 
		throws BuildException;	

	/**
	 * Sets the value of a string option for this configuration.
	 * 
	 * @param tool The Tool parent of the option.
	 * @param option The option that will be effected by change.
	 * @param value The value to apply to the option.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @throws BuildException
	 */
	public IOption setOption(ITool tool, IOption option, String value)
		throws BuildException;
	
	/**
	 * Sets the value of a list option for this configuration.
	 * 
	 * @param tool The Tool parent of the option.
	 * @param option The option to change.
	 * @param value The values to apply to the option.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @throws BuildException
	 */
	public IOption setOption(ITool tool, IOption option, String[] value)
		throws BuildException;

	/**
	 * Sets the rebuild state in this configuration.
	 * 
	 * @param rebuild <code>true</code> will force a rebuild the next time the project builds
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setRebuildState(boolean)
	 */
	void setRebuildState(boolean rebuild);

	/**
	 * Overrides the tool command for a tool defined in this configuration's tool-chain.
	 * 
	 * @param tool The tool that will have its command modified.
	 * @param command The command
	 */
	public void setToolCommand(ITool tool, String command);
		
	/**
	 * Returns <code>true</code> if the configuration's tool-chain is supported on the system
	 * otherwise returns <code>false</code>
	 * 
	 * @return boolean 
	 */	
	public boolean isSupported();
	
	/**
	 * Returns the implementation of the IConfigurationEnvironmentVariableSupplier provided
	 * by the tool-integrator or <code>null</code> if none.
	 * 
	 * @return IConfigurationEnvironmentVariableSupplier 
	 */	
	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier();
}
