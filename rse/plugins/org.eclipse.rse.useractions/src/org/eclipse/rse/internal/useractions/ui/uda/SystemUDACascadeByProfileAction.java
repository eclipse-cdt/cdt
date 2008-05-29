package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.ui.view.SystemViewMenuListener;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.ISystemViewMenuListener;
import org.eclipse.rse.ui.actions.SystemBaseDummyAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.swt.widgets.Shell;

/**
 * A cascading submenu action for "User Actions->".
 * This is after the first cascade, which lists profiles.
 * Here, for that profile, we list actions
 */
public class SystemUDACascadeByProfileAction extends SystemBaseSubMenuAction implements IMenuListener {
	private ISystemProfile profile;
	private SystemUDActionSubsystem udsubsystem;

	//private IStructuredSelection  selection;
	/**
	 * Constructor.
	 */
	public SystemUDACascadeByProfileAction(Shell shell, SystemUDActionSubsystem udss, IStructuredSelection selection, ISystemProfile profile) {
		super(profile.getName(), RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID), shell);
		this.profile = profile;
		this.udsubsystem = udss;
		super.setSelection(selection);
		setCreateMenuEachTime(false);
		setPopulateMenuEachTime(true);
		//System.out.println("Inside ctor for SystemUDACascadeByProfileAction");
	}

	/**
	 * @see org.eclipse.rse.ui.actions.SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu) {
		//System.out.println("Inside populateSubMenu for SystemUDACascadeByProfileAction");
		menu.addMenuListener(this);
		menu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		menu.add(new SystemBaseDummyAction());
		return menu;
	}

	/**
	 * Called when submenu is about to show. Called because we
	 * implement IMenuListener, and registered ourself for this event.
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu) {
		//System.out.println("Inside menuAboutToShow for SystemUDACascadeByProfileAction");
		Shell shell = getShell();
		udsubsystem.addUserActions(ourSubMenu, getSelection(), profile, shell);
	}

	/**
	 * Overridable method from parent that instantiates the menu listener who job is to add mnemonics.
	 * @param setMnemonicsOnlyOnce true if the menu is static and so mnemonics need only be set once. False if it is dynamic
	 */
	protected ISystemViewMenuListener createMnemonicsListener(boolean setMnemonicsOnlyOnce) {
		return new SystemViewMenuListener(false); // our menu is re-built dynamically each time
	}
}
