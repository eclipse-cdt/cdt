/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 * Kevin Doyle (IBM)   - [222828] Icons for some Actions Missing
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends SystemBasePlugin {
	//The shared instance.
	private static Activator plugin;
	public static final String PLUGIN_ID = "org.eclipse.rse.useractions"; //$NON-NLS-1$
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.SystemBasePlugin#initializeImageRegistry()
	 */
	protected void initializeImageRegistry()
    {
		putImageInRegistry(IUserActionsImageIds.COMPILE_0, "icons/full/dlcl16/compile.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.COMPILE_1, "icons/full/elcl16/compile.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.WORK_WITH_COMPILE_COMMANDS_0, "icons/full/dlcl16/workwithcompilecmds.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.WORK_WITH_COMPILE_COMMANDS_1, "icons/full/elcl16/workwithcompilecmds.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.WORK_WITH_NAMED_TYPES_0, "icons/full/dlcl16/workwithnamedtypes.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.WORK_WITH_NAMED_TYPES_1, "icons/full/elcl16/workwithnamedtypes.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.WORK_WITH_USER_ACTIONS_0, "icons/full/dlcl16/workwithuseractions.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.WORK_WITH_USER_ACTIONS_1, "icons/full/elcl16/workwithuseractions.gif"); //$NON-NLS-1$
		
		putImageInRegistry(IUserActionsImageIds.USERACTION_NEW, "icons/full/obj16/user_action_new_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERACTION_USR, "icons/full/obj16/user_action_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERACTION_IBM, "icons/full/obj16/user_action_ibm_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERACTION_IBMUSR, "icons/full/obj16/user_action_ibm_user_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERTYPE_NEW, "icons/full/obj16/user_type_new_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERTYPE_USR, "icons/full/obj16/user_type_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERTYPE_IBM, "icons/full/obj16/user_type_ibm_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.USERTYPE_IBMUSR, "icons/full/obj16/user_type_ibm_user_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.COMPILE_NEW, "icons/full/obj16/compcmd_new_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.COMPILE_USR, "icons/full/obj16/compcmd_user_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.COMPILE_IBM, "icons/full/obj16/compcmd_ibm_obj.gif"); //$NON-NLS-1$
		putImageInRegistry(IUserActionsImageIds.COMPILE_IBMUSR, "icons/full/obj16/compcmd_ibmuser_obj.gif"); //$NON-NLS-1$
    }

}
