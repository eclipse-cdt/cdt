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
 *  stuff unique to Local files.
 */
public class UDActionSubsystemLocalFiles extends UDActionSubsystemFiles {
	/**
	 * Constructor for UDActionSubsystemLocalFiles.
	 */
	public UDActionSubsystemLocalFiles() {
		super();
	}

	/**
	 * Parent intercept for adding default pre-defined types that are unique to us.
	 */
	protected void primeAdditionalDefaultUniversalTypes(SystemUDTypeManager typeMgr, Vector vectorOfTypes) {
		// I have decided not to include the iSeries unique types as there are user actions I can imagine for them,
		//  and they clutter up the namespace for non-iSeries users...
		/*
		 // the following contains ONLY those types that are unique to local.
		 // for now these are only local copies of remote iSeries members
		 final String fileTypes[][] = 
		 {     
		 {"CBL_400", "cbl,cblle,lbl,sqlcblle" }, 
		 {"CL_400",  "clp,clle,cmd400,icl" }, 
		 {"DDS_400", "dds,dspf,prtf,pf,lf,icff" }, 
		 {"RPG_400", "rpg,rpgle,rpt" },
		 };
		 
		 for (int i = 0; i < fileTypes.length; i++) 
		 {
		 SystemUDTypeElement ft = typeMgr.addType(DOMAIN_FILE, fileTypes[i][0]);
		 if (null == ft)
		 continue;
		 vectorOfTypes.addElement(ft);
		 ft.setTypes(fileTypes[i][1]);
		 }	 	
		 */
	}

	/**
	 * Parent intercept for adding default pre-defined actions that are unique to us.
	 */
	protected void primeAdditionalDefaultUniversalActions(SystemUDActionManager actionMgr, ISystemProfile profile, Vector vectorOfActions) {
		// duplicate the non-iseries types...
		int domain = DOMAIN_FILE;
		SystemUDActionElement newAction;
		for (int idx = 0; idx < UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS.length; idx++) {
			newAction = actionMgr.addAction(profile, UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][0], domain);
			vectorOfActions.addElement(newAction);
			newAction.setCommand(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][5]);
			newAction.setPrompt(true); // may as well always allow users chance to change command as its submitted
			newAction.setRefresh(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][1].equals("true")); //$NON-NLS-1$
			newAction.setShow(true);
			newAction.setSingleSelection(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][2].equals("true")); //$NON-NLS-1$
			newAction.setCollect(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][3].equals("true")); //$NON-NLS-1$
			newAction.setFileTypes(convertStringToArray(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][4]));
		}
		// add actions unique to local...
	}

	/**
	 * Parent intercept for restoring one of our unique IBM-supplied actions to its original state.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	protected boolean restoreAdditionalDefaultAction(SystemUDActionElement element, int domain, String actionName) {
		boolean ok = false;
		if (domain == DOMAIN_FOLDER) return ok;
		int match = -1;
		for (int idx = 0; (match == -1) && (idx < UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS.length); idx++) {
			if (UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[idx][0].equals(actionName)) match = idx;
		}
		if (match != -1) {
			element.setName(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[match][0]);
			element.setPrompt(true); // may as well always allow users chance to change command as its submitted
			element.setRefresh(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[match][1].equals("true")); //$NON-NLS-1$
			element.setShow(true);
			element.setSingleSelection(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[match][2].equals("true")); //$NON-NLS-1$
			element.setCollect(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[match][3].equals("true")); //$NON-NLS-1$
			element.setFileTypes(convertStringToArray(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[match][4]));
			element.setCommand(UDActionSubsystemUniversalFiles.UNIVERSAL_FILE_ACTIONS[match][5]);
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
