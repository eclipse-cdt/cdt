/*******************************************************************************
 * Copyright (c) 2008  QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems   - Initial API and implementation
 * Windriver and Ericsson - Updated for DSF
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.launching; 

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.IGDBLaunchConfigurationConstants;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.SessionType;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
 
/**
 * The shared launch configuration delegate for the DSF/GDB debugger.
 * This delegate supports all configuration types (local, remote, attach, etc)
 */
@ThreadSafe
public class GdbLaunchDelegate extends LaunchConfigurationDelegate 
    implements ILaunchConfigurationDelegate2
{
    public final static String GDB_DEBUG_MODEL_ID = "org.eclipse.dd.gdb"; //$NON-NLS-1$
        
    public void launch( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( mode.equals( ILaunchManager.DEBUG_MODE ) ) {
			launchDebugger( config, launch, monitor );
		}
	}

	private void launchDebugger( ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		monitor.beginTask("Launching debugger session", 10); //$NON-NLS-1$
		if ( monitor.isCanceled() ) {
			return;
		}

		try {
    		launchDebugSession( config, launch, monitor );
		}
		finally {
			monitor.done();
		}		
	}

	private void launchDebugSession( final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor ) throws CoreException {
		if ( monitor.isCanceled() ) {
			return;
		}
		
		SessionType sessionType = getSessionType(config);
		boolean attach = getIsAttach(config);
		
        final GdbLaunch launch = (GdbLaunch)l;

        if (sessionType == SessionType.REMOTE) {
            monitor.subTask( "Debugging remote C/C++ application" ); //$NON-NLS-1$    	
        } else {
            monitor.subTask( "Debugging local C/C++ application" ); //$NON-NLS-1$
        }
        
        // First verify we are dealing with a proper project.
        ICProject project = LaunchUtils.verifyCProject(config);
        // Now verify we know the program to debug.
		IPath exePath = LaunchUtils.verifyProgramPath(config, project);
		// Finally, make sure the program is a proper binary.
		LaunchUtils.verifyBinary(config, exePath);
    	
        monitor.worked( 1 );

        // Create and invoke the launch sequence to create the debug control and services
        final ServicesLaunchSequence servicesLaunchSequence = 
            new ServicesLaunchSequence(launch.getSession(), launch, exePath, sessionType, attach);
        launch.getSession().getExecutor().execute(servicesLaunchSequence);
        try {
            servicesLaunchSequence.get();
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in services launch sequence", e1.getCause())); //$NON-NLS-1$
        }
        
        // The initializeControl method should be called after the GdbControl class has
        // be initialized (in the ServicesLaunchSequence above.)  This is because it is the 
        // GdbControl class that will trigger the launch cleanup through a GDBControl.GDBExitedEvent
        launch.initializeControl();

        // Add the CLI and "inferior" process objects to the launch.
        launch.addCLIProcess("gdb"); //$NON-NLS-1$
        launch.addInferiorProcess(exePath.lastSegment());

        // Create and invoke the final launch sequence to setup GDB
        final FinalLaunchSequence finalLaunchSequence = 
        	new FinalLaunchSequence(launch.getSession().getExecutor(), launch, sessionType, attach);
        launch.getSession().getExecutor().execute(finalLaunchSequence);
        try {
        	finalLaunchSequence.get();
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in final launch sequence", e1.getCause())); //$NON-NLS-1$
        }
	}

	private SessionType getSessionType(ILaunchConfiguration config) {
    	try {
    		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
    		if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
    			return SessionType.LOCAL;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
    			return SessionType.LOCAL;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
    			return SessionType.CORE;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
    			return SessionType.REMOTE;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH)) {
    		    return SessionType.REMOTE;
    	    }
    	} catch (CoreException e) {    		
    	}
    	return SessionType.LOCAL;
    }
    
	private boolean getIsAttach(ILaunchConfiguration config) {
    	try {
    		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
    		if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
    			return false;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
    			return true;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
    			return false;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
    			return false;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH)) {
    		    return true;
    	    }
    	} catch (CoreException e) {    		
    	}
    	return false;
    }
    

	@Override
    public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		// no pre launch check for core file
		if (mode.equals(ILaunchManager.DEBUG_MODE) && getSessionType(config) == SessionType.CORE) return true; 
		
		return super.preLaunchCheck(config, mode, monitor);
	}

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        // Need to configure the source locator before creating the launch
        // because once the launch is created and added to launch manager, 
        // the adapters will be created for the whole session, including 
        // the source lookup adapter.
        ISourceLocator locator = getSourceLocator(configuration);
        
        return new GdbLaunch(configuration, mode, locator);
    }

    private ISourceLocator getSourceLocator(ILaunchConfiguration configuration) throws CoreException {
        String type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
        if (type == null) {
            type = configuration.getType().getSourceLocatorId();
        }
        if (type != null) {
            IPersistableSourceLocator locator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type);
            String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
            if (memento == null) {
                locator.initializeDefaults(configuration);
            } else {
                if(locator instanceof IPersistableSourceLocator2)
                    ((IPersistableSourceLocator2)locator).initializeFromMemento(memento, configuration);
                else
                    locator.initializeFromMemento(memento);
            }
            return locator;
        }
        return null;
    }
    
	/**
	 * Recursively creates a set of projects referenced by the current project
	 * 
	 * @param proj
	 *            The current project
	 * @param referencedProjSet
	 *            A set of referenced projects
	 * @throws CoreException
	 *             if an error occurs while getting referenced projects from the
	 *             current project
	 */
	private HashSet<IProject> getReferencedProjectSet(IProject proj, HashSet<IProject> referencedProjSet) throws CoreException {
		// The top project is a reference too and it must be added at the top to avoid cycles
		referencedProjSet.add(proj);

		IProject[] projects = proj.getReferencedProjects();
		for (IProject refProject : projects) {
			if (refProject.exists() && !referencedProjSet.contains(refProject)) {
				getReferencedProjectSet(refProject, referencedProjSet);
			}
		}
		return referencedProjSet;
	}
	
	/**
	 * Returns the order list of projects to build before launching.
	 *  Used in buildForLaunch() 
	 */
	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		IProject[] orderedProjects = null;
		ArrayList<IProject> orderedProjList = null;

		ICProject cProject = LaunchUtils.verifyCProject(configuration);
		if (cProject != null) {
			HashSet<IProject> projectSet = getReferencedProjectSet(cProject.getProject(), new HashSet<IProject>());

			String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
			if (orderedNames != null) {
				//Projects may not be in the build order but should still be built if selected
				ArrayList<IProject> unorderedProjects = new ArrayList<IProject>(projectSet.size());
				unorderedProjects.addAll(projectSet);
				orderedProjList = new ArrayList<IProject>(projectSet.size());

				for (String projectName : orderedNames) {
					for (IProject proj : unorderedProjects) {
						if (proj.getName().equals(projectName)) {
							orderedProjList.add(proj);
							unorderedProjects.remove(proj);
							break;
						}
					}
				}

				// Add any remaining projects to the end of the list
				orderedProjList.addAll(unorderedProjects);

				orderedProjects = orderedProjList.toArray(new IProject[orderedProjList.size()]);
			} else {
				// Try the project prerequisite order then
				IProject[] projects = projectSet.toArray(new IProject[projectSet.size()]);
				orderedProjects = ResourcesPlugin.getWorkspace().computeProjectOrder(projects).projects;
			}
		}
		return orderedProjects;
	}

	/* Used in finalLaunchCheck() */
	@Override
	protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return getBuildOrder(configuration, mode);
	}

	/**
	 * Searches for compile errors in the specified project
	 * Used in finalLaunchCheck() 
	 * @param proj
	 *            The project to search
	 * @return true if compile errors exist, otherwise false
	 */
	@Override
	protected boolean existsProblems(IProject proj) throws CoreException {
		IMarker[] markers = proj.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers.length > 0) {
			for (IMarker marker : markers) {
				Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
				if (severity != null) {
					return severity.intValue() >= IMarker.SEVERITY_ERROR;
				}
			}
		}
		return false;
	}

    private static class LaunchUtils {
       	/**
    	 * Verify the following things about the project:
    	 * - is a valid project name given
    	 * - does the project exist
    	 * - is the project open
    	 * - is the project a C/C++ project
    	 */
    	public static ICProject verifyCProject(ILaunchConfiguration configuration) throws CoreException {
    		String name = getProjectName(configuration);
    		if (name == null) {
    			abort(LaunchMessages.getString("AbstractCLaunchDelegate.C_Project_not_specified"), null, //$NON-NLS-1$
    					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
    		}
    		ICProject cproject = getCProject(configuration);
    		if (cproject == null) {
    			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    			if (!proj.exists()) {
    				abort(
    						LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Project_NAME_does_not_exist", name), null, //$NON-NLS-1$
    						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
    			} else if (!proj.isOpen()) {
    				abort(LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Project_NAME_is_closed", name), null, //$NON-NLS-1$
    						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
    			}
    			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Not_a_C_CPP_project"), null, //$NON-NLS-1$
    					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
    		}
    		return cproject;
    	}
    	

    	/**
    	 * Verify that program name of the configuration can be found as a file.
    	 * 
    	 * @return Absolute path of the program location
    	 */
    	public static IPath verifyProgramPath(ILaunchConfiguration configuration, ICProject cproject) throws CoreException {
    		String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
    		if (programName == null) {
				abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_not_specified"), null, //$NON-NLS-1$
					  ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
    		}
    		
    		IPath programPath = new Path(programName);    			 
    		if (programPath.isEmpty()) {
				abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), null, //$NON-NLS-1$
					  ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
    		}
    		
    		if (!programPath.isAbsolute()) {
    			// Find the specified program within the specified project
       			IFile wsProgramPath = cproject.getProject().getFile(programPath);
       			programPath = wsProgramPath.getLocation();
    		}
    		
    		if (!programPath.toFile().exists()) {
    			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), //$NON-NLS-1$
    				  new FileNotFoundException(
    						  LaunchMessages.getFormattedString("AbstractCLaunchDelegate.PROGRAM_PATH_not_found",  //$NON-NLS-1$ 
    						                                    programPath.toOSString())),
    				  ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
    		}
    		
    		return programPath;
    	}

 
    	/**
    	 * Verify that the executable path points to a valid binary file.
    	 * 
    	 * @return An object representing the binary file. 
    	 */
    	public static IBinaryObject verifyBinary(ILaunchConfiguration configuration, IPath exePath) throws CoreException {
    		ICExtensionReference[] parserRefs = CCorePlugin.getDefault().getBinaryParserExtensions(getCProject(configuration).getProject());
    		for (ICExtensionReference parserRef : parserRefs) {
    			try {
    				IBinaryParser parser = (IBinaryParser)parserRef.createExtension();
    				IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);
    				if (exe != null) {
    					return exe;
    				}
    			} catch (ClassCastException e) {
    			} catch (IOException e) {
    			}
    		}

    		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
    		try {
    			return (IBinaryObject)parser.getBinary(exePath);
    		} catch (ClassCastException e) {
    		} catch (IOException e) {
    		}
    		
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_is_not_a_recognized_executable"), //$NON-NLS-1$
 				  new FileNotFoundException(
 						  LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Program_is_not_a_recognized_executable",  //$NON-NLS-1$
 								  						    exePath.toOSString())),
 		          ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY);
			
			return null;
    	}

    	/**
    	 * Throws a core exception with an error status object built from the given
    	 * message, lower level exception, and error code.
    	 * 
    	 * @param message
    	 *            the status message
    	 * @param exception
    	 *            lower level exception associated with the error, or
    	 *            <code>null</code> if none
    	 * @param code
    	 *            error code
    	 */
    	private static void abort(String message, Throwable exception, int code) throws CoreException {
    		MultiStatus status = new MultiStatus(GdbPlugin.PLUGIN_ID, code, message, exception);
    		status.add(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, code, 
    				              exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
    				              exception));
    		throw new CoreException(status);
    	}

    	/**
    	 * Returns an ICProject based on the project name provided in the configuration.
    	 * First look for a project by name, and then confirm it is a C/C++ project.
    	 */
    	private static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
    		String projectName = getProjectName(configuration);
    		if (projectName != null) {
    			projectName = projectName.trim();
    			if (projectName.length() > 0) {
    				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
    				if (cProject != null && cProject.exists()) {
    					return cProject;
    				}
    			}
    		}
    		return null;
    	}

    	private static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
    		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
    	}
    }
    
}
