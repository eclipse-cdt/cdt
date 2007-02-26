/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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

package org.eclipse.rse.ui.actions;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterStringReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the New Connection wizard
 */
public class SystemNewConnectionAction extends SystemBaseWizardAction {

	private boolean fromPopupMenu = true;
	private ISelectionProvider sp;
	private String[] restrictSystemTypesTo;

	// The current selection the action is knowing of. Just pass on
	// to the wizards. Do not interpret here!
	private ISelection selectedContext;
	// The associated connection object of the selected context if
	// determinable from the selected context
	private IHost connectionContext;

	/**
	 * Constructor.
	 * 
	 * @param shell The parent shell to host the new wizard
	 * @param fromPopupMenu true if being launched from the Remote System Explorer view directly,
	 *                      false if being launched from a dialog
	 * @param sp The selection provider that will supply the selection via getSelection, if 
	 *            fromPopupMenu is false
	 */
	public SystemNewConnectionAction(Shell shell, boolean fromPopupMenu, ISelectionProvider sp) {
		this(shell, fromPopupMenu, true, sp);
	}

	/**
	 * Constructor for SystemNewConnectionAction when you don't want the icon. 
	 * @param shell The parent shell to host the new wizard
	 * @param fromPopupMenu true if being launched from the Remote System Explorer view directly,
	 *                      false if being launched from a dialog
	 * @param wantIcon true if you want the icon to show beside the action, false if not
	 * @param sp The selection provider that will supply the selection via getSelection, if 
	 *            fromPopupMenu is false
	 */
	public SystemNewConnectionAction(Shell shell, boolean fromPopupMenu, boolean wantIcon, ISelectionProvider sp) {
		this(shell, SystemResources.ACTION_NEWCONN_LABEL, SystemResources.ACTION_NEWCONN_TOOLTIP, fromPopupMenu, wantIcon, sp);
	}

	/**
	 * Constructor for SystemNewConnectionAction when you possibly don't want the icon, and want to 
	 * supply your own label. This is the "full" flavoured constructor!
	 * 
	 * @param shell The parent shell to host the new wizard
	 * @param label The label for the action
	 * @param tooltip the tooltip for the action
	 * @param fromPopupMenu true if being launched from the Remote System Explorer view directly,
	 *                      false if being launched from a dialog
	 * @param wantIcon true if you want the icon to show beside the action, false if not
	 * @param sp The selection provider that will supply the selection via getSelection, if 
	 *            fromPopupMenu is false
	 */
	public SystemNewConnectionAction(Shell shell, String label, String tooltip, boolean fromPopupMenu, boolean wantIcon, ISelectionProvider sp) {
		super(label, tooltip, wantIcon ? RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION_ID) : null, shell);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		this.fromPopupMenu = fromPopupMenu;
		this.sp = sp;
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0000"); //$NON-NLS-1$
	}

	/**
	 * The default processing for the run method calls createDialog, which
	 *  in turn calls this method to return an instance of our wizard.
	 * <p>
	 * Our default implementation is to return <code>RSEMainNewConnectionWizard</code>.
	 */
	protected IWizard createWizard() {
		// create the new connection wizard instance.
		RSEMainNewConnectionWizard newConnWizard = new RSEMainNewConnectionWizard();
		
		// simulate a selection changed event if the action is not called from
		// a popup menu and a selection provider is set
		if (!fromPopupMenu && sp != null) setSelection(sp.getSelection());

		// First, restrict the wizard in the system types to show if this is
		// requested.
		if (restrictSystemTypesTo != null) {
			// Till now, we get the list of system types to restrict to via system
			// type name. This should be changed to be a list of system type objects
			// as soon as possible. Till than, we have to translate the lists here.
			List systemTypes = new LinkedList();
			for (int i = 0; i < restrictSystemTypesTo.length; i++) {
				IRSESystemType systemType = RSECorePlugin.getDefault().getRegistry().getSystemType(restrictSystemTypesTo[i]);
				if (systemType != null) systemTypes.add(systemType);
			}

			newConnWizard.restrictToSystemTypes((IRSESystemType[])systemTypes.toArray(new IRSESystemType[systemTypes.size()]));
		}

		// If there is an remembered selection, we pass on the selected context
		// totally untranslated to the wizards. The specific wizards have to
		// interpret the selection themself. We simple cannot know here what is
		// necessary and what not. Wizard providers may want to get selections
		// we have no idea from. Only chance to do so, pass the selection on.
		newConnWizard.setSelectedContext(selectedContext);
		
		// if we had determined the connection context of the selected context, pass
		// on as well to the new connection wizard.
		newConnWizard.setConnectionContext(connectionContext); 
		
		// If the wizard is restricted to only one system type, the main wizard has to be skipped
		// and the dialog needs to be initialized directly with the selected wizard.
		if (newConnWizard.isRestrictedToSingleSystemType()) {
			IWizard wizard = newConnWizard.getSelectedWizard();
			if (wizard instanceof ISelectionChangedListener) {
				((ISelectionChangedListener)wizard).selectionChanged(new SelectionChangedEvent(newConnWizard, newConnWizard.getSelection()));
			}
			return wizard;
		}
		
		return newConnWizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseWizardAction#doCreateWizardDialog(org.eclipse.swt.widgets.Shell, org.eclipse.jface.wizard.IWizard)
	 */
	protected WizardDialog doCreateWizardDialog(Shell shell, IWizard wizard) {
		// The new connection action is always using the standard Eclipse WizardDialog!!!
		WizardDialog dialog = new WizardDialog(getShell(), wizard) {
			private boolean computeShellSizeFromScratch = true;
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.window.Window#getConstrainedShellBounds(org.eclipse.swt.graphics.Rectangle)
			 */
			protected Rectangle getConstrainedShellBounds(Rectangle preferredSize) {
				// The parent shell might have very large bounds if badly selected. We
				// will recalculate the wizard dialogs shell size the first time we end
				// up here. That asures that the dialog does not inherit shell bounds
				// from a much larger window.
				if (computeShellSizeFromScratch) {
					Point newSize = getInitialSize();
					Point newLoaction = getInitialLocation(newSize);
					
					preferredSize.height = newSize.y;
					preferredSize.width = newSize.x;
					preferredSize.y = newLoaction.y;
					preferredSize.x = newLoaction.x;
					
					computeShellSizeFromScratch = false;
				}
				
				Rectangle bounds = super.getConstrainedShellBounds(preferredSize);
				// We allow to resize the dialog in height, but not in width
				// to more to 500 pixel.
				bounds.width = Math.min(bounds.width, 500);
				return bounds;
			}
		};

		return dialog;
	}

	/**
	 * Call this to restrict the system types that the user is allowed to choose
	 */
	public void restrictSystemTypes(String[] systemTypes) {
		this.restrictSystemTypesTo = systemTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		boolean enabled = super.updateSelection(selection); 
		// store the selection. The wizard contributor may want to analyse
		// the current selection by themself.
		selectedContext = selection;
		
		// and try to determine the connection context from the selection
		if (enabled) {
			Object firstSelection = getFirstSelection();
			IHost conn = null;
			if (firstSelection != null) {
				if (firstSelection instanceof IHost)
					conn = (IHost)firstSelection;
				else if (firstSelection instanceof ISubSystem)
					conn = ((ISubSystem)firstSelection).getHost();
				else if (firstSelection instanceof ISystemFilterPoolReference) {
					ISystemFilterPoolReference sfpr = (ISystemFilterPoolReference)firstSelection;
					ISubSystem ss = (ISubSystem)sfpr.getProvider();
					conn = ss.getHost();
				} else if (firstSelection instanceof ISystemFilterReference) {
					ISystemFilterReference sfr = (ISystemFilterReference)firstSelection;
					ISubSystem ss = (ISubSystem)sfr.getProvider();
					conn = ss.getHost();
				} else if (firstSelection instanceof ISystemFilterStringReference) {
					ISystemFilterStringReference sfsr = (ISystemFilterStringReference)firstSelection;
					ISubSystem ss = (ISubSystem)sfsr.getProvider();
					conn = ss.getHost();
				}
			}

			connectionContext = conn;
		}
		
		return enabled;
	}
}