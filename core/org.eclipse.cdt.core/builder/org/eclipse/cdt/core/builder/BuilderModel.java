package org.eclipse.cdt.core.builder;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class BuilderModel {
	
	private static BuilderModel buildModel = null;

	public final static String PLUGIN_ID = "org.eclipse.cdt.core";
	public final static String BUILDER_NAME = "cbuilder";
	public final static String BUILDER_ID = PLUGIN_ID + "." + BUILDER_NAME;

	public static String getBuilderName () {
		return BUILDER_NAME;
	}

	public static String getBuilderId () {
		return BUILDER_ID;
	}
/*
	public IBuildPath getBuildPath(IProject project) {
		return null;
	}

	public void setBuildPath(IProject project, IBuildPath bp) {
	}
*/
	public void addBuildListener () {
	}

	public void removeBuildListener() {
	}

	public void build(IProject project, IPath workingDir, String[] args) {
	}

	/**
	 * Adds default C Builder.
	 */
	public void addCToBuildSpec(IProject project) throws CoreException {
		addToBuildSpec(project, getBuilderId());
	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	public void addToBuildSpec(IProject project, String builderID) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		ICommand command = null;
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].getBuilderName().equals(builderID)) {
				command = commands[i];
				break;
			}
		}
		if (command == null) {
			command = description.newCommand();
			command.setBuilderName(builderID);

			// Add a build spec before other builders (1FWJK7I)
			ICommand[] newCommands = new ICommand[commands.length + 1];
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			// Commit the spec change into the project
			description.setBuildSpec(newCommands);
			project.setDescription(description, null);
		}
	}

	/**
	 * Removes the default C Builder.
	 */
	public void removeCFromBuildSpec(IProject project) throws CoreException {
		removeFromBuildSpec(project, getBuilderId());
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	public void removeFromBuildSpec(IProject project, String builderID) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
			}
		}
	}

	private BuilderModel() {
	}

	public static BuilderModel getDefault() {
		if (buildModel == null) {
			buildModel = new BuilderModel();
		}
		return buildModel;
	}
}
