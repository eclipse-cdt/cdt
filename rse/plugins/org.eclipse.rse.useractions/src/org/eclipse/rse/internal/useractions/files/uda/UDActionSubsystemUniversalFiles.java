package org.eclipse.rse.internal.useractions.files.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Vector;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionManager;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeManager;

/**
 * This is a specialization of the universal files user action support, for 
 *  stuff unique to non-Local, non-IFS files.
 */
public class UDActionSubsystemUniversalFiles extends UDActionSubsystemFiles {
	/**
	 * Universal non-iseries actions 
	 */
	protected static final String UNIVERSAL_FILE_ACTIONS[][] =
	//      name,    refresh, singleSel, collect, types,         cmd
	{
	// these probably should be deleted, as these are more appropriate as compile commands
	//       {"gcc",    "true",  "false",   "false", "CPP_COMPILABLE C_COMPILABLE","gcc -c ${resource_name} -o ${resource_name_root}.o"}, 
	//     {"cc",     "true",  "false",   "false", "CPP_COMPILABLE C_COMPILABLE","cc -c ${resource_name} -o ${resource_name_root}.o"}, 
	//{"IBM C",  "true",  "false",   "false", "C_COMPILABLE", "xlc -c -qinfo=all ${resource_name} -o ${resource_name_root}.o"},
	//{"IBM C++","true",  "false",   "false", "CPP_COMPILABLE", "xlC -c -qinfo=all ${resource_name} -o ${resource_name_root}.o"},
	};

	/**
	 * Constructor 
	 */
	public UDActionSubsystemUniversalFiles() {
		super();
	}

	/**
	 * Parent intercept for adding default pre-defined types that are unique to us.
	 */
	protected void primeAdditionalDefaultUniversalTypes(SystemUDTypeManager typeMgr, Vector vectorOfTypes) {
		return; // nothing unique
	}

	/**
	 * Parent intercept for adding default pre-defined actions that are unique to us.
	 */
	protected void primeAdditionalDefaultUniversalActions(SystemUDActionManager actionMgr, ISystemProfile profile, Vector vectorOfActions) {
		// add file actions
		int domain = DOMAIN_FILE;
		SystemUDActionElement newAction;
		for (int idx = 0; idx < UNIVERSAL_FILE_ACTIONS.length; idx++) {
			newAction = actionMgr.addAction(profile, UNIVERSAL_FILE_ACTIONS[idx][0], domain);
			vectorOfActions.addElement(newAction);
			newAction.setCommand(UNIVERSAL_FILE_ACTIONS[idx][5]);
			newAction.setPrompt(true); // may as well always allow users chance to change command as its submitted
			newAction.setRefresh(UNIVERSAL_FILE_ACTIONS[idx][1].equals("true")); //$NON-NLS-1$
			newAction.setShow(true);
			newAction.setSingleSelection(UNIVERSAL_FILE_ACTIONS[idx][2].equals("true")); //$NON-NLS-1$
			newAction.setCollect(UNIVERSAL_FILE_ACTIONS[idx][3].equals("true")); //$NON-NLS-1$
			newAction.setFileTypes(convertStringToArray(UNIVERSAL_FILE_ACTIONS[idx][4]));
		}
	}

	/**
	 * We disable user defined actions if we are in work-offline mode.
	 * Currently, how we determine this is dependent on the subsystem factory.
	 */
	public boolean getWorkingOfflineMode() {
		return false; // todo... set to preferences setting when offline mode supported for universal
	}

	/**
	 * Parent intercept for restoring one of our unique IBM-supplied actions to its original state.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	protected boolean restoreAdditionalDefaultAction(SystemUDActionElement element, int domain, String actionName) {
		boolean ok = false;
		if (domain == DOMAIN_FOLDER) return ok;
		int match = -1;
		for (int idx = 0; (match == -1) && (idx < UNIVERSAL_FILE_ACTIONS.length); idx++) {
			if (UNIVERSAL_FILE_ACTIONS[idx][0].equals(actionName)) match = idx;
		}
		if (match != -1) {
			element.setName(UNIVERSAL_FILE_ACTIONS[match][0]);
			element.setPrompt(true); // may as well always allow users chance to change command as its submitted
			element.setRefresh(UNIVERSAL_FILE_ACTIONS[match][1].equals("true")); //$NON-NLS-1$
			element.setShow(true);
			element.setSingleSelection(UNIVERSAL_FILE_ACTIONS[match][2].equals("true")); //$NON-NLS-1$
			element.setCollect(UNIVERSAL_FILE_ACTIONS[match][3].equals("true")); //$NON-NLS-1$
			element.setFileTypes(convertStringToArray(UNIVERSAL_FILE_ACTIONS[match][4]));
			element.setCommand(UNIVERSAL_FILE_ACTIONS[match][5]);
			ok = true;
		}
		return ok;
	}

	/**
	 * Override of parent method to restore unique type supplied by us, to its original state.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	protected boolean restoreAdditionalDefaultType(SystemUDTypeElement element, int domain, String typeName) {
		return false; // nothing unique
	}
}
