/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.model;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.view.ISystemViewRunnableObject;
import org.eclipse.swt.widgets.Shell;

/**
 * This class represents a special case object in the system view (when used in browse
 *  dialogs) that allows users to create a new connection. 
 * <p>
 * It shows as "New Connection..." in the tree. When expanded, they get the new connection
 *  wizard. 
 */
public class SystemNewConnectionPromptObject implements ISystemPromptableObject, ISystemViewRunnableObject, IAdaptable {
	private Object parent;
	private IRSESystemType[] systemTypes;
	private ISystemPromptableObject[] children;
	private SystemNewConnectionAction action = null;
	private boolean systemTypesSet = false;
	private String newConnText;
	private boolean isRootPrompt = false;

	/**
	 * Constructor
	 */
	public SystemNewConnectionPromptObject() {
		setSystemTypes(RSECorePlugin.getDefault().getRegistry().getSystemTypes());
		isRootPrompt = true;
	}

	/**
	 * Constructor for child instances
	 */
	public SystemNewConnectionPromptObject(SystemNewConnectionPromptObject parent, IRSESystemType systemType) {
		this.parent = parent;
		setSystemTypes(new IRSESystemType[] { systemType });
	}

	// ----------------------------------------------------
	// METHODS FOR CONFIGURING THIS OBJECT
	// ----------------------------------------------------

	/**
	 * Set the system types to restrict the New Connection wizard to
	 */
	public void setSystemTypes(IRSESystemType[] systemTypes) {
		this.systemTypes = systemTypes;
		this.systemTypesSet = true;
	}
	
	/**
	 * @deprecated Use {@link #setSystemTypes(IRSESystemType[])}.
	 */
	public void setSystemTypes(String[] systemTypes) {
		if (systemTypes != null) {
			List types = new ArrayList();
			for (int i = 0; i < systemTypes.length; i++) {
				IRSESystemType type = RSECorePlugin.getDefault().getRegistry().getSystemType(systemTypes[i]);
				if (type != null) types.add(type);
			}
			setSystemTypes((IRSESystemType[])types.toArray(new IRSESystemType[types.size()]));
		} else {
			setSystemTypes((IRSESystemType[])null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.model.ISystemPromptableObject#getSystemTypes()
	 */
	public IRSESystemType[] getSystemTypes() {
		IRSESystemType[] types = systemTypes;
		if (types == null || !systemTypesSet)
			types = new IRSESystemType[0];
		return types;
	}

	/**
	 * Set the parent object so that we can respond to getParent requests
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}

	// ----------------------------------------------------
	// METHODS CALLED BY THE SYSTEMVIEWPROMPTABLEADAPTER...
	// ----------------------------------------------------

	/**
	 * Get the parent object (within tree view)
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * Return the child promptable objects.
	 * If this returns null, then SystemViewPromptableAdapter will subsequently
	 * call {@link #run(Shell)}.
	 */
	public ISystemPromptableObject[] getChildren() {
		if (!hasChildren())
			return null;

		if (children == null) {
			// Basically, once initialized, the list of system types cannot change, but
			// it doesn't hurt to check this in case it changes because of future extensions.
			if (isRootPrompt) {
				systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
			}

			if (systemTypes != null) {
				children = new ISystemPromptableObject[systemTypes.length];
				for (int idx = 0; idx < children.length; idx++) {
					children[idx] = new SystemNewConnectionPromptObject(this, systemTypes[idx]);
				}
			}
		}

		return children;
	}

	/**
	 * Return true if we have children, false if run when expanded
	 */
	public boolean hasChildren() {

		// DKM - I think we shouuld indicate children even if there's only one connection type	
		//if (systemTypes.length == 1)	
		if (systemTypes == null || (systemTypes.length == 1 && !isRootPrompt))
			return false;
		else
			return true;
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * Calls getImage on the subsystem's owning factory.
	 */
	public ImageDescriptor getImageDescriptor() {
		if (hasChildren())
			return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION_ID);
		else {
			RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemTypes[0].getAdapter(IRSESystemType.class));
			return adapter.getImageDescriptor(systemTypes[0]);
		}
	}

	/**
	 * Return the label for this object
	 */
	public String getText() {
		if (newConnText == null) {
			if (isRootPrompt || getSystemTypes().length == 0) {
				if (hasChildren())
					newConnText = SystemResources.RESID_NEWCONN_PROMPT_LABEL;
				else
					newConnText = SystemResources.RESID_NEWCONN_PROMPT_LABEL + " ..."; //$NON-NLS-1$
			} else if (getSystemTypes().length > 0) {
				newConnText = systemTypes[0].getName() + " ..."; //$NON-NLS-1$
			}
		}

		return newConnText;
	}

	/**
	 * Return the type label for this object
	 */
	public String getType() {
		if (hasChildren())
			return SystemResources.RESID_NEWCONN_EXPANDABLEPROMPT_VALUE;
		else
			return SystemResources.RESID_NEWCONN_PROMPT_VALUE;
	}

	/**
	 * Run this prompt. This should return an appropriate ISystemMessageObject to show
	 * as the child, reflecting if it ran successfully, was cancelled or failed. 
	 */
	public Object[] run(Shell shell) {
		if (action == null) {
			action = new SystemNewConnectionAction(shell, false, false, null);
		}
		if (systemTypes != null) {
			List systemTypeNames = new ArrayList();
			for (int i = 0; i < systemTypes.length; i++) systemTypeNames.add(systemTypes[i].getName());
			action.restrictSystemTypes((String[])systemTypeNames.toArray(new String[systemTypeNames.size()]));
		}

		try {
			action.run();
		} catch (Exception exc) {
			return new Object[] { new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, null) };
		}

		IHost newConnection = (IHost)action.getValue();

		// create appropriate object to return...
		ISystemMessageObject result = null;
		if (newConnection != null) {
			result = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CONNECTIONCREATED), ISystemMessageObject.MSGTYPE_OBJECTCREATED,
																				null);
		} else
			result = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, null);
		return new Object[] { result };
	}

	// ----------------------------------------------------
	// METHODS REQUIRED BY THE IADAPTABLE INTERFACE...
	// ----------------------------------------------------

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}
}
