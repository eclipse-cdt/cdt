/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * 
 * @since 3.0
 */
public interface IEnvVarBuildPath {
	public static final int BUILDPATH_INCLUDE = 1;
	public static final int BUILDPATH_LIBRARY = 2;

	public static final String BUILD_PATH_ELEMENT_NAME = "envVarBuildPath";	//$NON-NLS-1$
	public static final String TYPE = "pathType";	//$NON-NLS-1$
	public static final String LIST = "variableList";	//$NON-NLS-1$
	public static final String PATH_DELIMITER = "pathDelimiter";	//$NON-NLS-1$
	public static final String BUILD_PATH_RESOLVER = "buildPathResolver";	//$NON-NLS-1$
	
	public static final String TYPE_INCLUDE = "buildpathInclude";	//$NON-NLS-1$
	public static final String TYPE_LIBRARY = "buildpathLibrary";	//$NON-NLS-1$
	
	public static final String NAME_SEPARATOR = ",";	//$NON-NLS-1$
	
	/**
	 *
	 * @return one of the ITool.BUILDPATH _xxx
	 */
	public int getType();

	/**
	 *
	 * @return the array of String representing the build variable names
	 */
	public String[] getVariableNames();

	/**
	 *
	 * @return the String representing the path delimiter used in the variables returned by
	 * the getVariableNames() method
	 */
	public String getPathDelimiter();

	/**
	 *
	 * @return the IBuildPathResolver interface implementation provided by the tool-integrator
	 * in order to specify his/her own logic of resolving the variable values to the build paths
	 * (see also the "Specifying the Includes and Library paths environment variables" and
	 * the "IBuildPathResolver" sections for more detail and for explanation why this callback
	 * might be needed)
	 */
	public IBuildPathResolver getBuildPathResolver();
}

