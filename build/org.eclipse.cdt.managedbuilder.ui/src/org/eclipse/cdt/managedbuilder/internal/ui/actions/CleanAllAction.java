/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     LSI Corporation	 - added symmetric project clean action
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.actions;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Action which changes active build configuration of the current project to
 * the given one.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 7.0
 */
public class CleanAllAction extends CommonBuildCleanAllAction {
	@Override
	protected String getTIP_ALL() { return Messages.getString("CleanAllAction.0");}//$NON-NLS-1$
	@Override
	protected String getLBL_ALL() { return Messages.getString("CleanAllAction.1");}//$NON-NLS-1$
	@Override
	protected String getJOB_MSG() { return Messages.getString("CleanAllAction.2");}//$NON-NLS-1$
	@Override
	protected String getERR_MSG() { return Messages.getString("CleanAllAction.3");}//$NON-NLS-1$
	@Override
	protected String getLBL_SEL() { return Messages.getString("CleanAllAction.4");}//$NON-NLS-1$
	@Override
	protected String getTIP_SEL() {	return Messages.getString("CleanAllAction.5");}//$NON-NLS-1$
	@Override
	protected String getDLG_TEXT(){ return Messages.getString("CleanAllAction.6"); }//$NON-NLS-1$
	@Override
	protected String getDLG_TITLE(){ return Messages.getString("CleanAllAction.7");}//$NON-NLS-1$
	@Override
	protected void performAction(IConfiguration[] configs,
			IProgressMonitor monitor) throws CoreException {
		ManagedBuildManager.cleanConfigurations(configs, monitor);
	}
}
