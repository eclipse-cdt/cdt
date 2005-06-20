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
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.content.IContentType;

/**
 * This interface represents an outputType instance in the managed build system.
 * It describes one category of output files created by a Tool.  A tool can 
 * have multiple outputType children.
 * 
 * @since 3.0
 */
public interface IOutputType extends IBuildObject {
	public static final String OUTPUT_TYPE_ELEMENT_NAME = "outputType";	//$NON-NLS-1$
	public static final String OUTPUT_CONTENT_TYPE = "outputContentType";	//$NON-NLS-1$
	public static final String OUTPUTS = "outputs";	//$NON-NLS-1$
	public static final String OPTION = "option";	//$NON-NLS-1$
	public static final String MULTIPLE_OF_TYPE = "multipleOfType";	//$NON-NLS-1$
	public static final String PRIMARY_INPUT_TYPE = "primaryInputType";	//$NON-NLS-1$
	public static final String PRIMARY_OUTPUT = "primaryOutput";	//$NON-NLS-1$
	public static final String OUTPUT_PREFIX = "outputPrefix";	//$NON-NLS-1$
	public static final String OUTPUT_NAMES = "outputNames";	//$NON-NLS-1$
	public static final String NAME_PATTERN = "namePattern";	//$NON-NLS-1$
	public static final String NAME_PROVIDER = "nameProvider";	//$NON-NLS-1$
	public static final String BUILD_VARIABLE = "buildVariable";	//$NON-NLS-1$
	
	/**
	 * Returns the tool defining this OutputType.
	 * 
	 * @return ITool
	 */
	public ITool getParent();

	/**
	 * Returns the <code>IOutputType</code> that is the superclass of this
	 * OutputType, or <code>null</code> if the attribute was not specified.
	 * 
	 * @return IInputType
	 */
	public IOutputType getSuperClass();
	
	/**
	 * Returns the Eclipse <code>IContentType</code> that describes this
	 * output type. If both the outputs attribute and the outputContentType 
	 * attribute are specified, the outputContentType will be used if it 
	 * is defined in Eclipse. 
	 * 
	 * @return IContentType
	 */
	public IContentType getOutputContentType();
	
	/**
	 * Sets the Eclipse <code>IContentType</code> that describes this
	 * output type. 
	 * 
	 * @param contentType  The Eclipse content type
	 */
	public void setOutputContentType(IContentType contentType);
	
	/**
	 * Returns the list of valid output extensions from the
	 * outputs attribute. Note that this value is not used
	 * if output content type is specified and registered with Eclipse.
	 * Also, the user will not be able to modify the set of file 
	 * extensions as they can when outputContentType is specified.
	 * 
	 * @return <code>String[]</code> of extensions
	 */
	public String[] getOutputExtensionsAttribute();
	
	/**
	 * Sets all of the output extensions that the receiver can build.
	 * NOTE: The value of this attribute will NOT be used if a 
	 *       output content type is specified and is registered with
	 *       Eclipse.
	 * 
	 * @param String
	 */
	public void setOutputExtensionsAttribute(String extensions);
	
	/**
	 * Returns the list of the output extensions that the receiver can build.
	 * Note that the list will come from the outputContentType if it 
	 * is specified and registered with Eclipse.  Otherwise the  
	 * outputs attribute will be used.
	 * 
	 * @return String[]
	 */
	public String[] getOutputExtensions();
	
	/**
	 * Answers <code>true</code> if the output type considers the file extension to be 
	 * one associated with an output file.
	 * 
	 * @param ext file extension
	 * @return boolean
	 */
	public boolean isOutputExtension(String ext);

	/**
	 * Returns the id of the option that is associated with this
	 * output type on the command line. The default is to use the Tool 
	 * “outputFlag” attribute if primaryOutput is True.  If option is not 
	 * specified, and primaryOutput is False, then the output file(s) of 
	 * this outputType are not added to the command line.  
	 * When specified, the namePattern, nameProvider and outputNames are ignored.
	 * 
	 * @return String
	 */
	public String getOptionId();

	/**
	 * Sets the id of the option that is associated with this
	 * output type on the command line. 
	 * 
	 * @param optionId  
	 */
	public void setOptionId(String optionId);
	
	/**
	 * Returns <code>true</code> if this outputType creates multiple output
	 * resources in one invocation of the tool, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean getMultipleOfType();
	
	/**
	 * Sets whether this outputType can create multiple output resources in
	 * one invocation of the tool.
	 * 
	 * @param multiple   
	 */
	public void setMultipleOfType(boolean multiple);
	
	/**
	 * Returns the input type that is used in determining the default
	 * names of this output type. 
	 * 
	 * @return IInputType
	 */
	public IInputType getPrimaryInputType();
	
	/**
	 * Sets the input type that is used in determining the default
	 * names of this output type.
	 * 
	 * @param inputType
	 */
	public void setPrimaryInputType(IInputType contentType);
	
	/**
	 * Returns <code>true</code> if this is considered the primary output
	 * of the tool, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean getPrimaryOutput();
	
	/**
	 * Sets whether this is the primary output of the tool.
	 * 
	 * @param primary   
	 */
	public void setPrimaryOutput(boolean primary);

	/**
	 * Returns the prefix that the tool should prepend to the name of the build artifact.
	 * For example, a librarian usually prepends 'lib' to the target.a
	 * @return String
	 */
	public String getOutputPrefix();

	/**
	 * Sets the prefix that the tool should prepend to the name of the build artifact.
	 * For example, a librarian usually prepends 'lib' to the target.a
	 * @param String
	 */
	public void setOutputPrefix(String prefix);

	/**
	 * Returns the file names of the complete set of output files for this outputType 
	 * 
	 * @return String[]
	 */
	public String[] getOutputNames();

	/**
	 * Sets the complete set of output file names for this outputType 
	 * 
	 * @param names
	 */
	public void setOutputNames(String names);

	/**
	 * Returns the pattern, using the Gnu pattern rule syntax, for deriving the 
	 * output resource name from the input resource name.  The default is to use 
	 * the input file base filename with the output extension. 
	 * 
	 * @return String
	 */
	public String getNamePattern();

	/**
	 * Sets the pattern, using the Gnu pattern rule syntax, for deriving the 
	 * output resource name from the input resource name.
	 * 
	 * @return pattern
	 */
	public void setNamePattern(String pattern);
	
	/**
	 * Returns the IManagedOutputNameProvider interface as specified by the nameProvider attribute.
	 * 
	 * @return IManagedOutputNameProvider
	 */
	public IManagedOutputNameProvider getNameProvider();

	/**
	 * Returns the name of the build variable associated this this output type's resources
	 * The variable is used in the build file to represent the list of output files.  
	 * The same variable name can be used by an inputType to identify a set of output 
	 * files that contribute to the tool’s input (i.e., those using the same buildVariable 
	 * name).  The default name is chosen by MBS.
	 * 
	 * @return String
	 */
	public String getBuildVariable();

	/**
	 * Sets the name of the build variable associated this this output type's resources.
	 * 
	 * @return variableName
	 */
	public void setBuildVariable(String variableName);

	/**
	 * Returns <code>true</code> if this element has changes that need to 
	 * be saved in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Returns <code>true</code> if this OutputType was loaded from a manifest file,
	 * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
	 * 
	 * @return boolean 
	 */
	public boolean isExtensionElement();
	
	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);

}
