/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation, QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - [272416] Rework the working set configurations
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.osgi.util.NLS;

public final class WorkingSetMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.workingsets.WorkingSetMessages";//$NON-NLS-1$

	private WorkingSetMessages() {
		// Do not instantiate
	}

	public static String CElementWorkingSetPage_title;
	public static String CElementWorkingSetPage_name;
	public static String CElementWorkingSetPage_description;
	public static String CElementWorkingSetPage_content;
	public static String CElementWorkingSetPage_warning_nameMustNotBeEmpty;
	public static String CElementWorkingSetPage_warning_workingSetExists;
	public static String CElementWorkingSetPage_warning_resourceMustBeChecked;

	public static String ProjConfigController_activeConfig;

	public static String WorkingSetMenus_enumPattern;
	public static String WorkspaceSnapshot_buildNoProj;

	public static String WSConfig_build_problems;
	public static String WSConfig_build_task;

	public static String WSConfigDialog_activate_label;
	public static String WSConfigDialog_active_config;
	public static String WSConfigDialog_add_label;
	public static String WSConfigDialog_build_label;
	public static String WSConfigDialog_buildPrompt_message;
	public static String WSConfigDialog_buildPrompt_title;
	public static String WSConfigDialog_implicit_config;
	public static String WSConfigDialog_projTree_accessible_name;
	public static String WSConfigDialog_projTree_label;
	public static String WSConfigDialog_remove_label;
	public static String WSConfigDialog_rename_label;
	public static String WSConfigDialog_title;
	public static String WSConfigDialog_wsTree_accessible_name;
	public static String WSConfigDialog_wsTree_label;

	public static String WSConfigManager_closeFailed;
	public static String WSConfigManager_loadFailed;
	public static String WSConfigManager_save_job;
	public static String WSConfigManager_saveFailed;

	public static String WSConfigsController_addDlg_defaultName;
	public static String WSConfigsController_addDlg_emptyName;
	public static String WSConfigsController_addDlg_msg;
	public static String WSConfigsController_addDlg_nameExists;
	public static String WSConfigsController_addDlg_title;
	public static String WSConfigsController_buildFailedDlgMsg;
	public static String WSConfigsController_buildFailedDlgTitle;
	public static String WSConfigsController_buildFailedLog;
	public static String WSConfigsController_renameDlg_msg;
	public static String WSConfigsController_renameDlg_title;

	public static String WSetConfigsPage_noProjects;

	public static String WSProjConfig_activatedWarning;
	public static String WSProjConfig_buildProblem;
	public static String WSProjConfig_noConfig;

	public static String WSProjConfigFactory_badFactory;
	public static String WSProjConfigFactory_factoryFailed;
	public static String WSProjConfigFactory_noFactoryID;
	public static String WSProjConfigFactory_noNatureID;

	static {
		NLS.initializeMessages(BUNDLE_NAME, WorkingSetMessages.class);
	}
}