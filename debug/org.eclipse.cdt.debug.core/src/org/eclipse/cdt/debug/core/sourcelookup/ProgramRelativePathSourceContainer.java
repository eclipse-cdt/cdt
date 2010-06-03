/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.sourcelookup;

import java.io.File;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A source container that converts relative paths to absolute ones using the
 * program (executable) location as the reference point. The conversion is
 * successful only if such a file actually exists.
 * 
 * @since 7.0
 */
public class ProgramRelativePathSourceContainer extends AbstractSourceContainer{

	/**
	 * Unique identifier for the relative path source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.programRelativePath</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.programRelativePath"; //$NON-NLS-1$
	
	/**
	 * The program's path.
	 */
	private IPath fProgramPath = Path.EMPTY;

	/**
	 * Default constructor.
	 */
	public ProgramRelativePathSourceContainer() {
	}
	
	/**
	 * Special constructor used when trying to locate a source file without a
	 * launch or launch configuration context, but when a Binary context is
	 * available. Normally, this class is instantiated with the default (no arg)
	 * constructor and such instances are added to the source locator of a
	 * launch configuration. In those cases, we can obtain the the program
	 * (executable) context from the launch configuration. But in cases were CDT
	 * needs to search for a source file and there is no
	 * launch/launch-configuration context, it can explicitly create an instance
	 * using this constructor and call our {@link #findSourceElements(String)}
	 * method.
	 * 
	 * @param program
	 *            the executable context. Calling this with null is equivalent
	 *            to calling the default constructor.
	 */
	public ProgramRelativePathSourceContainer(IBinary program) {
		if (program != null) {
			fProgramPath = program.getPath();
		}
	}
	
	/**
	 * If [sourceName] is a relative path, and applying it to the location of
	 * the program (executable) produces an absolute path that points to an
	 * actual file, then we return a LocalFileStorage for that file. Otherwise
	 * we return an empty array. We always return at most one element.
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements( String sourceName ) throws CoreException {
		
		if (sourceName == null){
			return new Object[0];
		}
		
		// check if source path is a relative path
		IPath sourcePath = new Path(sourceName);
		if (sourcePath.isAbsolute()){
			return new Object[0];
		}
		
		// get program (executable) absolute path
		IPath programPath = getProgramLocation();
		if (programPath == Path.EMPTY){
			return new Object[0];
		}

		// remove the name of the program from the program path
		programPath = programPath.removeLastSegments(1); 
		// append the relative source path to the absolute location of the program
		sourcePath = programPath.append(sourcePath);

		// check if source file exists and is valid
		File sourceFile = sourcePath.toFile();
		if ( sourceFile.exists() && sourceFile.isFile() ) {
			return new Object[] { new LocalFileStorage( sourceFile ) };
		}

		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return SourceLookupMessages.getString("ProgramRelativePathSourceContainer.0"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType( TYPE_ID );
	}

	private synchronized IPath getProgramLocation() throws CoreException {

		// compute fProgramPath only if doesn't exist already
		if (fProgramPath.isEmpty()){
			// get launch configuration
			ISourceLookupDirector director = getDirector();
			if (director == null) {
				return fProgramPath; // return empty path
			}
			ILaunchConfiguration configuration = director.getLaunchConfiguration();
			if (configuration == null) {
				return fProgramPath; // return empty path
			}
			
			// Get current project. Unlike CDI, DSF supports debugging
			// executables that are not in an Eclipse project, so this may be
			// null for a DSF session. See bugzilla 304433.
			ICProject project = null;
			String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName != null) {
				project = CoreModel.getDefault().getCModel().getCProject(projectName);
				if (project == null || !project.exists()) {
					return fProgramPath; // return empty path
				}
			}
	
			// get program name
			String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
			if (programName == null){
				return fProgramPath; // return empty path
			}

			// get executable file
			IFile exeFile = null;
			try {
				if (project != null) {
					exeFile = project.getProject().getFile(new Path(programName));
				}
				else {
					// A DSF launch config need not reference a project. Try
					// treating program name as either an absolute path or a
					// path relative to the working directory
					IPath path = new Path(programName);
					if (path.toFile().exists()) {
						fProgramPath = path;
						return fProgramPath;
					}
					else {
						return fProgramPath; // return empty path
					}
				}
				
			}
			catch (IllegalArgumentException e){
				return fProgramPath; // return empty path
			}

			if (!exeFile.exists()){
				return fProgramPath; // return empty path
			}

			// get program absolute path
			fProgramPath = exeFile.getLocation();
		}

		// return program absolute path
		return fProgramPath;
	}
}
