/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.*;
import java.lang.String;
import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 *  This class implements the Dependency Manager and Output Name Provider interfaces
 *  for a very "quick & dirty" ifort tool-chain on Win32
 */
public class DefaultFortranDependencyCalculator implements IManagedDependencyGenerator,
														   IManagedOutputNameProvider
{
	public static final String MODULE_EXTENSION = "mod";	//$NON-NLS-1$
	
	/*
	 * Return a list of the names of all modules used by a file
	 */
	private String[] findUsedModuleNames(File file) {
		ArrayList names = new ArrayList();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			Reader r = new BufferedReader(new InputStreamReader(in));
			StreamTokenizer st = new StreamTokenizer(r);
			st.commentChar('!');
			st.eolIsSignificant(false);
			st.slashSlashComments(false);
			st.slashStarComments(false);
			st.wordChars('_', '_');
			
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_WORD) {
					if (st.sval.equalsIgnoreCase("use")) {
						st.nextToken();
						if (st.ttype == StreamTokenizer.TT_WORD) {
							names.add(st.sval);
						} else {
							st.pushBack();
						}
					}
				}
			}
		}
		catch (Exception e) {
			return new String[0];
		}
		return (String[]) names.toArray(new String[names.size()]);
	}
	
	/*
	 * Return a list of the names of all modules defined in a file
	 */
	private String[] findModuleNames(File file) {
		ArrayList names = new ArrayList();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			Reader r = new BufferedReader(new InputStreamReader(in));
			StreamTokenizer st = new StreamTokenizer(r);
			st.commentChar('!');
			st.eolIsSignificant(false);
			st.slashSlashComments(false);
			st.slashStarComments(false);
			st.wordChars('_', '_');
			
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_WORD) {
					if (st.sval.equalsIgnoreCase("module")) {
						st.nextToken();
						if (st.ttype == StreamTokenizer.TT_WORD) {
							names.add(st.sval);
						} else {
							st.pushBack();
						}
					}
				}
			}
		}
		catch (Exception e) {
			return new String[0];
		}
		return (String[]) names.toArray(new String[names.size()]);
	}

	/*
	 * Returns true if the resource is a Fortran source file
	 */
	private boolean isFortranFile(ITool tool, IResource resource) {
		// TODO:  Get the file extensions from the tool's primary input type
		String ext = resource.getFileExtension();
		if (ext != null) {
			if (ext.equalsIgnoreCase("f")) return true;
			if (ext.equalsIgnoreCase("for")) return true;
			if (ext.equalsIgnoreCase("f90")) return true;
		}
		return false;
	}
	
	/*
	 * Given a set of the module names used by a source file, and a set of resources to search, determine
	 * if any of the source files implements the module names.
	 */
	private IResource[] FindModulesInResources(IProject project, ITool tool, IResource resource, IResource[] resourcesToSearch, 
							String topBuildDir, String[] usedNames) {
		ArrayList modRes = new ArrayList();
		for (int ir = 0; ir < resourcesToSearch.length; ir++) {
			if (resourcesToSearch[ir].equals(resource)) continue;
			if (resourcesToSearch[ir].getType() == IResource.FILE) {
				File projectFile = resourcesToSearch[ir].getLocation().toFile();
				if (!isFortranFile(tool, resourcesToSearch[ir])) continue;
				String[] modules = findModuleNames(projectFile);
				if (modules != null) {
					for (int iu = 0; iu < usedNames.length; iu++) {
						boolean foundDependency = false;
						for (int im = 0; im < modules.length; im++) {
							if (usedNames[iu].equalsIgnoreCase(modules[im])) {
								//  Get the path to the module file that will be created by the build.  By default, ifort appears
								//  to generate .mod files in the directory from which the compiler is run.  For MBS, this
								//  is the top-level build directory.  
								//  TODO: Support the /module:path option and use that in determining the path of the module file 
								IPath modName = Path.fromOSString(topBuildDir + Path.SEPARATOR + modules[im] + "." + MODULE_EXTENSION);
								modRes.add(project.getFile(modName));
								modRes.add(resourcesToSearch[ir]);
								foundDependency = true;
								break;
							}
						}
						if (foundDependency) break;
					}
				}
			} else if (resourcesToSearch[ir].getType() == IResource.FOLDER) {
				try {
					IResource[] modFound = FindModulesInResources(project, tool, resource, ((IFolder)resourcesToSearch[ir]).members(), 
							topBuildDir, usedNames);
					if (modFound != null) {
						for (int i=0; i<modFound.length; i++) {
							modRes.add(modFound[i]);
						}
					}
				} catch(Exception e) {}
			}
		}		
		return (IResource[]) modRes.toArray(new IResource[modRes.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		ArrayList dependencies = new ArrayList();

		//  TODO:  This method should be passed the ITool and the relative path of the top build directory
		//         For now we'll figure this out from the project.
		IManagedBuildInfo mngInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = mngInfo.getDefaultConfiguration();
		ITool tool = null;
		ITool[] tools = config.getTools();
		for (int i=0; i<tools.length; i++) {
			if (tools[i].getName().equals("Fortran (ifort) Compiler for Win32")) {
				tool = tools[i];
				break;
			}
		}
		
		File file = resource.getLocation().toFile();
		try {
			if (!isFortranFile(tool, resource)) return null;
	
			//  Get the names of the modules USE'd by the source file
			String[] usedNames = findUsedModuleNames(file);
			if (usedNames.length == 0) return null;
			
			//  Search the project files for a Fortran source that creates the module.  If we find one, then compiling this
			//  source file is dependent upon first compiling the found source file.
			IResource[] resources = project.members();	
			IResource[] modRes = FindModulesInResources(project, tool, resource, resources, config.getName(), usedNames);
			if (modRes != null) {
				for (int i=0; i<modRes.length; i++) {
					dependencies.add(modRes[i]);
				}
			}
		}
		catch (Exception e)
		{
			return null;
		}
		
		return (IResource[]) dependencies.toArray(new IResource[dependencies.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		return TYPE_EXTERNAL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		/* 
		 * The type of this IManagedDependencyGenerator is TYPE_EXTERNAL,
		 * so implement findDependencies() rather than getDependencyCommand().
		 * */
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider#getOutputNames(org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath[])
	 */
	public IPath[] getOutputNames(ITool tool, IPath[] primaryInputNames) {
		//  TODO:  This method should be passed the relative path of the top build directory?
		ArrayList outs = new ArrayList();
		if (primaryInputNames.length > 0) {
			// Get the names of modules created by this source file
			String[] modules = findModuleNames(primaryInputNames[0].toFile());
			// Add any generated modules
			if (modules != null) {
				for (int i = 0; i < modules.length; i++) {
					//  Return the path to the module file that will be created by the build.  By default, ifort appears
					//  to generate .mod files in the directory from which the compiler is run.  For MBS, this
					//  is the top-level build directory.  
					//  TODO: Support the /module:path option and use that in determining the path of the module file
					//  TODO: The nameProvider documentation should note that the returned path is relative to the top-level 
					//        build directory.  HOWEVER, if only a file name is returned, MBS will automatically add on the
					//        directory path relative to the top-level build directory.  The relative path comes from the source
					//        file location.  In order to specify that this output file is always in the top-level build 
					//        directory, regardless of the source file directory structure, return "./path".
					IPath modName = Path.fromOSString("." + Path.SEPARATOR + modules[i] + "." + MODULE_EXTENSION);
					outs.add(modName);				
				}
			}
		}
		return (IPath[]) outs.toArray(new IPath[outs.size()]);
	}

}
