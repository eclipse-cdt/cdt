/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder.internal;

import java.text.MessageFormat;
import java.util.Hashtable;

import org.eclipse.cdt.core.builder.model.ICToolType;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.builder.ICToolTabGroup;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @author sam.robb
 * 
 * Manages contributed configuration tabs
 */
public class CBuildConfigPresentationManager {
	
	/**
	 * The singleton configuration presentation manager
	 */
	private static CBuildConfigPresentationManager fgDefault;
			
	/**
	 * Collection of configuration tab group extensions
	 * defined in plug-in xml. Entries are keyed by
	 * type identifier (<code>String</code>), and entires
	 * are <code>CToolTabGroupPoint</code>.
	 */
	private Hashtable fTabGroupExtensions;	
		
	/**
	 * Constructs the singleton configuration presentation manager.
	 */
	private CBuildConfigPresentationManager() {
		fgDefault = this;
		initializeTabGroupExtensions();
	}

	/**
	 * Returns the configuration presentation manager
	 */
	public static CBuildConfigPresentationManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new CBuildConfigPresentationManager();
		}
		return fgDefault;
	}
		
	/**
	 * Creates launch configuration tab group extensions for each extension
	 * defined in XML, and adds them to the table of tab group extensions.
	 */
	private void initializeTabGroupExtensions() {
		fTabGroupExtensions = new Hashtable();
		IPluginDescriptor descriptor= CUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_TAB_GROUPS);
		IConfigurationElement[] groups = extensionPoint.getConfigurationElements();
		for (int i = 0; i < groups.length; i++) {
			CToolTabGroupPoint group = new CToolTabGroupPoint(groups[i]);
			String typeId = group.getId();
			if (typeId == null) {
				IExtension ext = groups[i].getDeclaringExtension();
				IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.STATUS_INVALID_EXTENSION_DEFINITION,
 					 MessageFormat.format("Configuration tab group extension {0} does not specify configuration type", (new String[] {ext.getUniqueIdentifier()})), null); //$NON-NLS-1$
					DebugUIPlugin.log(status);
			} else {
				// verify it references a valid launch configuration type
				ILaunchConfigurationType lct = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
				if (lct == null) {
					IExtension ext = groups[i].getDeclaringExtension();
					IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.STATUS_INVALID_EXTENSION_DEFINITION,
					 MessageFormat.format("Launch configuration tab group extension {0} refers to non-existant launch configuration_type_{1}", (new String[] {ext.getUniqueIdentifier(), typeId})), null); //$NON-NLS-1$
					DebugUIPlugin.log(status);
				}
			}
			if (typeId != null) {
				fTabGroupExtensions.put(typeId, group);
			}
		}
	}	
	
	/**
	 * Returns the tab group for the given type of launch configuration.
	 * 
	 * @return the tab group for the given type of launch configuration
	 * @exception CoreException if an exception occurrs creating the group
	 */
	public ICToolTabGroup getTabGroup(ICToolType type) throws CoreException {
		CToolTabGroupPoint ext = (CToolTabGroupPoint)fTabGroupExtensions.get(type.getId());
		if (ext == null) {
			IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR,
			 MessageFormat.format("No tab group defined for configuration type {0}", (new String[] {type.getId()})), null);			; //$NON-NLS-1$
			 throw new CoreException(status);
		} else {
			return ext.getProvider();
		}
	}
	
}

