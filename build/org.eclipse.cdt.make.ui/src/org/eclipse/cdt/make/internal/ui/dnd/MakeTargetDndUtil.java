/*******************************************************************************
 * Copyright (c) 2008, 2009 Andrew Gvozdev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.dnd;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Shell;

/**
 * A collection of various functions for Make Target View drag and drop support.
 */
public class MakeTargetDndUtil {
	private static final int ANSWER_YES_ID = 0;
	private static final int ANSWER_YES_TO_ALL_ID = 1;
	private static final int ANSWER_NO_ID = 2;
	private static final int ANSWER_NO_TO_ALL_ID = 3;
	private static final int ANSWER_CANCEL_ID = 4;

	/**
	 * The previous answer to the question of overwriting all targets.
	 */
	protected static int lastUserAnswer = ANSWER_YES_ID;

	/**
	 * Default build command.
	 */
	public static final String DEFAULT_BUILD_COMMAND = "make"; //$NON-NLS-1$

	/**
	 * @return build command from project settings.
	 */
	public static String getProjectBuildCommand(IProject project) {
		IMakeTargetManager targetManager = MakeCorePlugin.getDefault().getTargetManager();
		String[] targetBuilders = targetManager.getTargetBuilders(project);
		if (targetBuilders==null || targetBuilders.length==0) {
			return DEFAULT_BUILD_COMMAND;
		}

		String builderId = targetManager.getBuilderID(targetBuilders[0]);
		String buildCommand = DEFAULT_BUILD_COMMAND;
		try {
			IMakeBuilderInfo buildInfo = MakeCorePlugin.createBuildInfo(project, builderId);
			buildCommand = buildInfo.getBuildCommand().toString().trim();
		} catch (CoreException e) {
			// keep default value
		}
		return buildCommand;
	}

	/**
	 * Determine if the selection is allowed to be dragged from Make Target
	 * View. It should be homogeneous having {@code IMakeTrget}s only and no
	 * duplicate names are allowed. Make targets could be selected from
	 * different folders.
	 *
	 * @param selection - make targets selection.
	 * @return {@code true} if the selection is allowed to be dragged.
	 */
	public static boolean isDragable(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			List<?> selectedElements = ((IStructuredSelection) selection).toList();
			if (selectedElements.size() == 0) {
				return false;
			}

			List<String> names = new ArrayList<String>(selectedElements.size());
			for (Object element : selectedElements) {
				if (!(element instanceof IMakeTarget)) {
					return false;
				}
				String makeTargetName = ((IMakeTarget) element).getName();
				for (String name : names) {
					if (makeTargetName == null || makeTargetName.equals(name)) {
						return false;
					}
				}
				names.add(makeTargetName);
			}
			return true;
		}
		return false;
	}

	/**
	 * Perform action of copying or moving or dropping make targets to specified
	 * container. This action displays a progress bar if user copies more than 1
	 * target.
	 *
	 * @param makeTargets - array of make targets to copy.
	 * @param container - where to copy the selection.
	 * @param operation - copying operation. Should be one of
	 *        {@link org.eclipse.swt.dnd.DND} operations.
	 * @param shell - shell to display a progress bar.
	 *
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 * @see DND#DROP_DEFAULT
	 */
	public static void copyTargets(IMakeTarget[] makeTargets, IContainer container, int operation,
		Shell shell) {
		if (makeTargets == null || makeTargets.length == 0 || container == null) {
			return;
		}

		lastUserAnswer = ANSWER_YES_ID;

		if (makeTargets.length == 1) {
			try {
				// Do not slow down generating modal window for a single target
				copyOneTarget(makeTargets[0], container, operation, shell);
			} catch (CoreException e) {
				// log any problem then ignore it
				MakeUIPlugin.log(e);
			}
		} else if (makeTargets.length > 1) {
			copyTargetsWithProgressIndicator(makeTargets, container, operation, shell);
		}
	}

	/**
	 * Copy/move one make target to the specified container.
	 *
	 * @param makeTarget - make target.
	 * @param container - container to copy/move to.
	 * @param operation - copying operation. Should be one of
	 *        {@link org.eclipse.swt.dnd.DND} operations.
	 * @param shell - shell to display user warnings.
	 * @throws CoreException on the failure of {@link IMakeTargetManager} or
	 *         {@link IMakeTarget} operation.
	 *
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 * @see DND#DROP_DEFAULT
	 */
	public static void copyOneTarget(IMakeTarget makeTarget, IContainer container,
		final int operation, Shell shell) throws CoreException {

		IMakeTargetManager makeTargetManager = MakeCorePlugin.getDefault().getTargetManager();
		IMakeTarget exists = makeTargetManager.findTarget(container, makeTarget.getName());
		if (exists != null) {
			int userAnswer = overwriteMakeTargetDialog(makeTarget.getName(), shell);
			if (userAnswer == ANSWER_YES_ID || userAnswer == ANSWER_YES_TO_ALL_ID) {
				copyTargetData(makeTarget, exists);
				if (operation == DND.DROP_MOVE) {
					makeTargetManager.removeTarget(makeTarget);
				}
			} else if (userAnswer == ANSWER_NO_ID || userAnswer == ANSWER_NO_TO_ALL_ID) {
				// no action
			}
		} else {
			makeTargetManager.addTarget(container, cloneTarget(makeTarget, container.getProject()));
			if (operation == DND.DROP_MOVE) {
				makeTargetManager.removeTarget(makeTarget);
			}
		}
	}

	/**
	 * Copy/move make targets to a given container. Displays progress bar.
	 *
	 * @param makeTargets - array of make targets to copy.
	 * @param container - container to copy/move to.
	 * @param operation - copying operation. Should be one of
	 *        {@link org.eclipse.swt.dnd.DND} operations.
	 * @param shell - shell to display a progress bar.
	 *
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 * @see DND#DROP_DEFAULT
	 *
	 */
	private static void copyTargetsWithProgressIndicator(final IMakeTarget[] makeTargets,
		final IContainer container, final int operation, final Shell shell) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
				boolean isMove = operation == DND.DROP_MOVE;
				String textHeader = isMove ? MakeUIPlugin.getResourceString("MakeTargetDnD.moving") //$NON-NLS-1$
					: MakeUIPlugin.getResourceString("MakeTargetDnD.copying"); //$NON-NLS-1$
				String textAction = isMove ? MakeUIPlugin
					.getResourceString("MakeTargetDnD.moving.one") //$NON-NLS-1$
					: MakeUIPlugin.getResourceString("MakeTargetDnD.copying.one"); //$NON-NLS-1$
				monitor.beginTask(textHeader + ' ' + container.getName(), makeTargets.length - 1);
				for (IMakeTarget makeTarget : makeTargets) {
					if (makeTarget != null) {
						monitor.subTask(textAction + ' ' + makeTarget.getName());
						try {
							copyOneTarget(makeTarget, container, operation, shell);
						} catch (CoreException e) {
							// log failures but ignore all targets which failed
							MakeUIPlugin.log(e);
						}
						if (lastUserAnswer == ANSWER_CANCEL_ID) {
							break;
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						break;
					}
				}
				monitor.done();
				lastUserAnswer = ANSWER_YES_ID;
			}
		};

		IRunnableContext context = new ProgressMonitorDialog(shell);
		try {
			context.run(false, true, runnable);
		} catch (InvocationTargetException e) {
			MakeUIPlugin.log(e);
		} catch (InterruptedException e) {
			MakeUIPlugin.log(e);
		}
	}

	/**
	 * Overwrite Make Target dialog.
	 *
	 * @param name - name of make target to display to a user.
	 * @param shell - shell where to display the dialog.
	 *
	 * @return user's answer.
	 */
	private static int overwriteMakeTargetDialog(String name, Shell shell) {

		if (lastUserAnswer == ANSWER_YES_TO_ALL_ID || lastUserAnswer == ANSWER_NO_TO_ALL_ID) {
			return lastUserAnswer;
		}

		String labels[] = new String[] { IDialogConstants.YES_LABEL,
			IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL,
			IDialogConstants.NO_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL, };
		String title = MakeUIPlugin.getResourceString("MakeTargetDnD.title.overwriteTargetConfirm"); //$NON-NLS-1$
		String question = MessageFormat.format(MakeUIPlugin
			.getResourceString("MakeTargetDnD.message.overwriteTargetConfirm"), //$NON-NLS-1$
			new Object[] { name });

		MessageDialog dialog = new MessageDialog(shell, title, null, question,
			MessageDialog.QUESTION, labels, 0);

		try {
			dialog.open();
			lastUserAnswer = dialog.getReturnCode();
		} catch (SWTException e) {
			MakeUIPlugin.log(e);
			lastUserAnswer = ANSWER_CANCEL_ID;
		}

		if (lastUserAnswer == SWT.DEFAULT) {
			// A window close returns SWT.DEFAULT, which has to be
			// mapped to a cancel
			lastUserAnswer = ANSWER_CANCEL_ID;
		}
		return lastUserAnswer;
	}

	/**
	 * Creating a copy of IMakeTarget in a different project.
	 *
	 * @param makeTarget - make target.
	 * @param project - project where to assign the make target.
	 * @return newly created make target.
	 * @throws CoreException if there is a problem with creating or copying the
	 *         target.
	 */
	private static IMakeTarget cloneTarget(IMakeTarget makeTarget, IProject project)
		throws CoreException {
		IMakeTargetManager makeTargetManager = MakeCorePlugin.getDefault().getTargetManager();
		String[] ids = makeTargetManager.getTargetBuilders(project);
		String builderId = ids[0];

		IMakeTarget newMakeTarget = makeTargetManager.createTarget(project, makeTarget.getName(),
			builderId);
		copyTargetData(makeTarget, newMakeTarget);
		return newMakeTarget;
	}

	/**
	 * Populate destination make target with data from source make target.
	 *
	 * @param source - source make target.
	 * @param destination - destination make target.
	 * @throws CoreException if there is a problem populating the target.
	 *
	 * @see MakeTarget
	 */
	private static void copyTargetData(IMakeTarget source, IMakeTarget destination)
		throws CoreException {

		// IMakeTarget attributes
		// destination.project and destination.targetBuilderID are not changed
		destination.setRunAllBuilders(source.runAllBuilders());
		destination.setAppendProjectEnvironment(source.appendProjectEnvironment());
		destination.setBuildAttribute(IMakeTarget.BUILD_TARGET,
			source.getBuildAttribute(IMakeTarget.BUILD_TARGET, "")); //$NON-NLS-1$

		// IMakeCommonBuildInfo attributes
		// Ignore IMakeCommonBuildInfo.BUILD_LOCATION in order not to pick
		// location of another project (or another folder)
		destination.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND,
			source.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, DEFAULT_BUILD_COMMAND));
		destination.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS,
			source.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		destination.setStopOnError(source.isStopOnError());
		destination.setUseDefaultBuildCmd(source.isDefaultBuildCmd());
		destination.setEnvironment(source.getEnvironment());
		destination.setAppendEnvironment(source.appendEnvironment());
		// setErrorParsers() is not supported in MakeTarget yet
	}

	/**
	 * Create @{code MakeTarget} from basic data elements available during
	 * copy/paste or drag/drop operations. The other data will be set to default.
	 *
	 * @param name - name of make target being created.
	 * @param targetStr - build target.
	 * @param command - make command. ("make" by default).
	 * @param container - container where to place the target.
	 * @return newly created {@link IMakeTarget}.
	 * @throws CoreException if there was a problem creating new make target.
	 */
	public static IMakeTarget createMakeTarget(String name, String targetStr, String command,
		IContainer container) throws CoreException {
		IMakeTargetManager makeTargetManager = MakeCorePlugin.getDefault().getTargetManager();

		IProject project = container.getProject();
		String[] ids = makeTargetManager.getTargetBuilders(project);
		String builderId = ids[0];

		// IMakeTarget attributes
		IMakeTarget newMakeTarget = makeTargetManager.createTarget(project, name, builderId);
		if (targetStr != null) {
			newMakeTarget.setBuildAttribute(IMakeTarget.BUILD_TARGET, targetStr);
		}

		// IMakeCommonBuildInfo attributes
		String projectBuildCommand = getProjectBuildCommand(project);
		if (command != null && command.length() > 0 && !command.equals(projectBuildCommand)) {
			newMakeTarget.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, command);
			newMakeTarget.setUseDefaultBuildCmd(false);
		} else {
			newMakeTarget.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, projectBuildCommand);
			newMakeTarget.setUseDefaultBuildCmd(true);
		}

		return newMakeTarget;
	}
}
