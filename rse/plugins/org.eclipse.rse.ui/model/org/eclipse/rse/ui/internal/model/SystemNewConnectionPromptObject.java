/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 *******************************************************************************/

package org.eclipse.rse.ui.internal.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
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
		setSystemTypes(RSECorePlugin.getTheCoreRegistry().getSystemTypes());
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemPromptableObject#getSystemTypes()
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
				systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
			}

			// Note: Do not filter out the disabled system types here. The system
			// type enabling is dynamic, so we have to filter them dynamic. Here
			// we build a static list of _all_ available system types.
			if (systemTypes != null) {
				children = new ISystemPromptableObject[systemTypes.length];
				for (int idx = 0; idx < children.length; idx++) {
					children[idx] = new SystemNewConnectionPromptObject(this, systemTypes[idx]);
				}
			}
		}

		return getChildrenFiltered(children);
	}

	private static class RSESystemTypeSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			if (o1 instanceof IRSESystemType && o2 instanceof IRSESystemType) {
				return ((IRSESystemType)o1).getLabel().compareTo(((IRSESystemType)o2).getLabel());
			} else if (o1 instanceof ISystemPromptableObject && o2 instanceof ISystemPromptableObject) {
				return ((ISystemPromptableObject)o1).getText().compareTo(((ISystemPromptableObject)o2).getText());
			}
			return 0;
		}
	}
	
	protected final static Comparator SYSTEM_TYPE_SORTER = new RSESystemTypeSorter();
	
	/**
	 * Filter the list of children to return and remove the disabled children.
	 * 
	 * @param children The list of children to filter.
	 * @return The filtered list of children or <code>null</code> if the passed in list of children had been <code>null</code>.
	 */
	private ISystemPromptableObject[] getChildrenFiltered(ISystemPromptableObject[] children) {
		if (children == null) return null;
		
		List filtered = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			ISystemPromptableObject promptObject = children[i];
			if (promptObject instanceof SystemNewConnectionPromptObject) {
				IRSESystemType[] systemTypes = ((SystemNewConnectionPromptObject)promptObject).getSystemTypes();
				boolean enabled = true;
				for (int j = 0; j < systemTypes.length && enabled; j++) {
					IRSESystemType systemType = systemTypes[j];
					
					// As we should consistently show the same new connection wizards list
					// within the new connection wizard itself and this new connection prompt
					// object, we have to do the same here as what the wizard is doing for
					// filtering the selectable list of new connection wizards.
					ViewerFilter filter = (ViewerFilter)(systemType.getAdapter(ViewerFilter.class));
					if (filter != null && !filter.select(null, null, systemType)) {
						enabled = false;
					}

					if (enabled) {
						RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemType.getAdapter(RSESystemTypeAdapter.class));
						if (adapter != null && !adapter.isEnabled(systemType)) {
							enabled = false;
						}
					}
				}
				
				if (enabled) filtered.add(promptObject);
			} else {
				// it's a ISystemPromptableObject but not a SystemNewConnectionPromptObject?
				filtered.add(promptObject);
			}
		}
		
		if (!filtered.isEmpty()) Collections.sort(filtered, SYSTEM_TYPE_SORTER);
		return (ISystemPromptableObject[])filtered.toArray(new ISystemPromptableObject[filtered.size()]);
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
			RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemTypes[0].getAdapter(RSESystemTypeAdapter.class));
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
				newConnText = systemTypes[0].getLabel() + " ..."; //$NON-NLS-1$
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
			action.restrictSystemTypes(systemTypes);
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
