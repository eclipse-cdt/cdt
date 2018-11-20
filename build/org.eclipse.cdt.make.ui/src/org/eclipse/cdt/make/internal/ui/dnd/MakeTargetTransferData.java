/*******************************************************************************
 * Copyright (c) 2008, 2009 Andrew Gvozdev.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.dnd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * {@code MakeTargetTransferData} serves for transferring data during a drag and
 * drop operation between different plug-ins. This object contains an extension
 * identifier and a block of bytes. When the drop occurs, the data is
 * interpreted by an action defined in the specified extension.
 * <p>
 * Clients using MakeTargetTransfer should create an instance to contain the
 * drop data.
 * </p>
 *
 * @see IMakeTarget
 */
public class MakeTargetTransferData {
	class MakeTargetData {
		private final String name;

		// IMakeTarget attributes
		private final boolean runAllBuilders;
		private final boolean appendProjectEnvironment;
		private final String buildTarget;

		// IMakeCommonBuildInfo attributes
		private final String buildCommand;
		private final String buildArguments;
		private final boolean stopOnError;
		private final boolean useDefaultBuildCmd;
		private final boolean appendEnvironment;

		/**
		 * Constructor to populate the transfer data record with
		 * {@link IMakeTarget} data.
		 *
		 * @param name - make target name.
		 * @param runAllBuilders - make target runAllBuilders flag.
		 * @param appendProjectEnvironment - make target
		 *        appendProjectEnvironment flag.
		 * @param buildTarget - make target buildTarget.
		 * @param buildCommand - make target buildCommand.
		 * @param buildArguments - make target buildArguments.
		 * @param stopOnError - make target stopOnError flag.
		 * @param useDefaultBuildCmd - make target useDefaultBuildCmd flag.
		 * @param appendEnvironment - make target appendEnvironment flag.
		 */
		MakeTargetData(String name, boolean runAllBuilders, boolean appendProjectEnvironment, String buildTarget,
				String buildCommand, String buildArguments, boolean stopOnError, boolean useDefaultBuildCmd,
				boolean appendEnvironment) {

			this.name = name;
			this.runAllBuilders = runAllBuilders;
			this.appendProjectEnvironment = appendProjectEnvironment;
			this.buildTarget = buildTarget;
			this.buildCommand = buildCommand;
			this.buildArguments = buildArguments;
			this.stopOnError = stopOnError;
			this.useDefaultBuildCmd = useDefaultBuildCmd;
			this.appendEnvironment = appendEnvironment;

		}

		boolean runAllBuilders() {
			return runAllBuilders;
		}

		boolean appendProjectEnvironment() {
			return appendProjectEnvironment;
		}

		String getBuildTarget() {
			return buildTarget;
		}

		String getBuildCommand() {
			return buildCommand;
		}

		String getBuildArguments() {
			return buildArguments;
		}

		boolean isStopOnError() {
			return stopOnError;
		}

		boolean isDefaultBuildCmd() {
			return useDefaultBuildCmd;
		}

		boolean appendEnvironment() {
			return appendEnvironment;
		}

		String getName() {
			return name;
		}
	}

	private final List<MakeTargetData> makeTargetData;

	/**
	 * Default constructor.
	 *
	 */
	public MakeTargetTransferData() {
		makeTargetData = new ArrayList<>();
	}

	/**
	 * Method to add make target to the target list in preparation to transfer.
	 *
	 * @param target - make target being added.
	 */
	public void addMakeTarget(IMakeTarget target) {
		addMakeTarget(target.getName(), target.runAllBuilders(), target.appendProjectEnvironment(),
				target.getBuildAttribute(IMakeTarget.BUILD_TARGET, ""), //$NON-NLS-1$
				target.getBuildAttribute(IMakeTarget.BUILD_COMMAND, ""), //$NON-NLS-1$
				target.getBuildAttribute(IMakeTarget.BUILD_ARGUMENTS, ""), //$NON-NLS-1$
				target.isStopOnError(), target.isDefaultBuildCmd(), target.appendEnvironment());
	}

	/**
	 * Add another target to the list of make target transfer records.
	 *
	 * @param name - make target name.
	 * @param runAllBuilders - make target runAllBuilders flag.
	 * @param appendProjectEnvironment - make target appendProjectEnvironment flag.
	 * @param buildTarget - make target buildTarget.
	 * @param buildCommand - make target buildCommand.
	 * @param buildArguments - make target buildArguments.
	 * @param stopOnError - make target stopOnError flag.
	 * @param useDefaultBuildCmd - make target useDefaultBuildCmd flag.
	 * @param appendEnvironment - make target appendEnvironment flag.
	 */
	public void addMakeTarget(String name, boolean runAllBuilders, boolean appendProjectEnvironment, String buildTarget,
			String buildCommand, String buildArguments, boolean stopOnError, boolean useDefaultBuildCmd,
			boolean appendEnvironment) {
		makeTargetData.add(new MakeTargetData(name, runAllBuilders, appendProjectEnvironment, buildTarget, buildCommand,
				buildArguments, stopOnError, useDefaultBuildCmd, appendEnvironment));
	}

	/**
	 * Create an array of make targets from transfer records. Creating of actual {@link IMakeTarget}s
	 * requires supplying a project where they will be assigned by {@link IMakeTargetManager}.
	 *
	 * @param project - project to assign the targets.
	 * @return the array of created {@link IMakeTarget}s
	 *
	 * @see IMakeTarget
	 * @see IMakeTargetManager
	 */
	public IMakeTarget[] createMakeTargets(IProject project) {
		IMakeTargetManager makeTargetManager = MakeCorePlugin.getDefault().getTargetManager();
		ArrayList<IMakeTarget> makeTargets = new ArrayList<>(makeTargetData.size());
		String[] ids = makeTargetManager.getTargetBuilders(project);
		String builderId = ids[0];

		for (MakeTargetData element : makeTargetData) {
			try {
				IMakeTarget target = makeTargetManager.createTarget(project, element.getName(), builderId);

				target.setRunAllBuilders(element.runAllBuilders());
				target.setAppendProjectEnvironment(element.appendProjectEnvironment());
				target.setBuildAttribute(IMakeTarget.BUILD_TARGET, element.getBuildTarget());
				target.setBuildAttribute(IMakeTarget.BUILD_COMMAND, element.getBuildCommand());
				target.setBuildAttribute(IMakeTarget.BUILD_ARGUMENTS, element.getBuildArguments());
				target.setStopOnError(element.isStopOnError());
				target.setUseDefaultBuildCmd(element.isDefaultBuildCmd());
				target.setAppendEnvironment(element.appendEnvironment());

				makeTargets.add(target);
			} catch (CoreException e) {
				MakeUIPlugin.log(e);
			}
		}
		return makeTargets.toArray(new IMakeTarget[makeTargets.size()]);
	}

	/**
	 * @return list of make target transfer records for {@link MakeTargetTransfer}.
	 */
	List<MakeTargetData> getMakeTargetDataList() {
		return makeTargetData;
	}
}
