package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.build.standard.StandardBuildManager;
import org.eclipse.cdt.core.resources.IStandardBuildInfo;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;



public class CProjectNature implements IProjectNature {

    public static final String BUILDER_NAME= "cbuilder";
    public static final String BUILDER_ID= CCorePlugin.PLUGIN_ID + "." + BUILDER_NAME;
    public static final String C_NATURE_ID= CCorePlugin.PLUGIN_ID + ".cnature";

    private IProject fProject;
    private IStandardBuildInfo fBuildInfo;

    public CProjectNature() {
    }

    public CProjectNature(IProject project) {
		setProject(project);
    }

	public static void addCNature(IProject project, IProgressMonitor mon) throws CoreException {
		addNature(project, C_NATURE_ID, mon);
	}

	public static void removeCNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, C_NATURE_ID, mon);
	}
	
	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param proj the project to add the nature
	 * @param natureId the id of the nature to assign to the project
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures= description.getNatureIds();
		for (int i= 0; i < prevNatures.length; i++) {
			if (natureId.equals(prevNatures[i]))
				return;
		}
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param proj the project to remove the nature from
	 * @param natureId the nature id to remove
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures= description.getNatureIds();
		List newNatures = new ArrayList(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

    /**
     * Sets the path of the build command executable.
     */
    public void setBuildCommand(IPath locationPath, IProgressMonitor monitor) throws CoreException {
		String newLocation= locationPath.toString();
		String oldLocation= fBuildInfo.getBuildLocation();
		if (!newLocation.equals(oldLocation)) {
		    fBuildInfo.setBuildLocation(newLocation);
		}
    }

    /**
     * Gets the path of the build command executable.
     */
    public IPath getBuildCommand() throws CoreException {
		String buildLocation= fBuildInfo.getBuildLocation();
	    return new Path(buildLocation);
    }

    /**
     * Sets the arguments for the full build.
     */
    public void setFullBuildArguments(String arguments, IProgressMonitor monitor) throws CoreException {
		String oldArguments= fBuildInfo.getFullBuildArguments();
		if (!arguments.equals(oldArguments)) {
		    fBuildInfo.setFullBuildArguments(arguments);
		}
    }

    /**
     * Gets the arguments for the full build
     */
    public String getFullBuildArguments() throws CoreException {
		String buildArguments= fBuildInfo.getFullBuildArguments();
		if (buildArguments == null) {
			buildArguments= "";
		}
		return buildArguments;
    }
    
    /**
     * Sets the arguments for the incremental build.
     */
    public void setIncrBuildArguments(String arguments, IProgressMonitor monitor) throws CoreException {
		String oldArguments= fBuildInfo.getIncrementalBuildArguments();
		if (!arguments.equals(oldArguments)) {
			fBuildInfo.setIncrementalBuildArguments(arguments);
		}
    }

    /**
     * Gets the arguments for the incremental build
     */
    public String getIncrBuildArguments() throws CoreException {
		String buildArguments= fBuildInfo.getIncrementalBuildArguments();
		if (buildArguments == null) {
			buildArguments= "";
		}
		return buildArguments;
    }

    /**
     * Sets Stop on Error
     */
    public void setStopOnError(boolean on) throws CoreException {
		boolean oldArgument= fBuildInfo.isStopOnError();
		if (on != oldArgument) {
		    fBuildInfo.setStopOnError(on);
		}
    }

    public void setBuildCommandOverride(boolean on) throws CoreException {
		boolean oldArgument= fBuildInfo.isDefaultBuildCmd();
		if (on != oldArgument) {
		    fBuildInfo.setUseDefaultBuildCmd(on);
		}
    }

    /**
     * Gets Stop on Error
     */
    public boolean isStopOnError() throws CoreException {
		return fBuildInfo.isStopOnError();
    }

    public boolean isDefaultBuildCmd() throws CoreException {
		return fBuildInfo.isDefaultBuildCmd();
    }

	public static boolean hasCBuildSpec(IProject project) {
		boolean found= false;
		try {
			IProjectDescription description = project.getDescription();
			ICommand[] commands= description.getBuildSpec();
			for (int i= 0; i < commands.length; ++i) {
				if (commands[i].getBuilderName().equals(BUILDER_ID)) {
					found= true;
					break;
				}
			}
		} catch (CoreException e) {
		}
		return found;
	}

	public void addCBuildSpec(IProgressMonitor mon) throws CoreException {
		addToBuildSpec(getBuilderID(), mon);
	}

	public static void addCBuildSpec(IProject project, IProgressMonitor mon) throws CoreException {
		addToBuildSpec(project, getBuilderID(), mon);
	}

    public void addToBuildSpec(String builderID, IProgressMonitor mon) throws CoreException {
		addToBuildSpec(getProject(), builderID, mon);
	}

    /**
     * Adds a builder to the build spec for the given project.
     */
    public static void addToBuildSpec(IProject project, String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description= project.getDescription();
		ICommand[] commands= description.getBuildSpec();
		boolean found= false;
		for (int i= 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				found= true;
				break;
			}
		}
		if (!found) {
			ICommand command= description.newCommand();
			command.setBuilderName(builderID);
			ICommand[] newCommands= new ICommand[commands.length + 1];
			// Add it before other builders. See 1FWJK7I: ITPJCORE:WIN2000
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0]= command;
			description.setBuildSpec(newCommands);
			project.setDescription(description, mon);
		}
	}

	public void removeCBuildSpec(IProgressMonitor mon) throws CoreException {
		removeFromBuildSpec(getBuilderID(), mon);
	}

    /**
     * Removes the given builder from the build spec for the given project.
     */
    public void removeFromBuildSpec(String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description= getProject().getDescription();
		ICommand[] commands= description.getBuildSpec();
		for (int i= 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands= new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				return;
			}
		}
		getProject().setDescription(description, mon);
	}

    /**
     * Get the correct builderID
     */
    public static String getBuilderID() {
    	Plugin plugin = (Plugin)CCorePlugin.getDefault();
    	IPluginDescriptor descriptor = plugin.getDescriptor();
    	if (descriptor.getExtension(BUILDER_NAME) != null) {
			return descriptor.getUniqueIdentifier() + "." + BUILDER_NAME;
    	}
    	return BUILDER_ID;
    }

    /**
     * @see IProjectNature#configure
     */
    public void configure() throws CoreException {
		addToBuildSpec(getBuilderID(), null);
		IStandardBuildInfo info = BuildInfoFactory.create();
		fBuildInfo.setBuildLocation(info.getBuildLocation());
		fBuildInfo.setFullBuildArguments("");
		fBuildInfo.setIncrementalBuildArguments("");
    }

    /**
     * @see IProjectNature#deconfigure
     */
    public void deconfigure() throws CoreException {
		removeFromBuildSpec(getBuilderID(), null);
		fBuildInfo.setBuildLocation(null);
		fBuildInfo.setFullBuildArguments(null);
		fBuildInfo.setIncrementalBuildArguments(null);
    }

    /**
     * @see IProjectNature#getProject
     */
    public IProject getProject() {
		return fProject;
    }

    /**
     * @see IProjectNature#setProject
     */
    public void setProject(IProject project) {
    	try {
			fProject= project;
			fBuildInfo = StandardBuildManager.getBuildInfo(fProject, true);
		} catch (CoreException e) {
		}
    }
    
}
