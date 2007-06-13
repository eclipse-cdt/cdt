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
 * Uwe Stieber (Wind River) - Set action id for identification from plugin.xml menu extensions.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Uwe Stieber (Wind River) - [192202] Default RSE new connection wizard does not allow to query created host instance anymore
 * Uwe Stieber (Wind River) - [189426] System File/Folder Dialogs - New Connection Not Added to Drop Down
 ********************************************************************************/

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterStringReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizard;
import org.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the New Connection wizard
 */
public class SystemNewConnectionAction extends SystemBaseWizardAction {

	private boolean fromPopupMenu = true;
	private ISelectionProvider sp;
	private IRSESystemType[] restrictSystemTypesTo;

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
		// reset the output value
		setValue(null);
		
		// create the new connection wizard instance.
		RSEMainNewConnectionWizard newConnWizard = new RSEMainNewConnectionWizard();
		
		// simulate a selection changed event if the action is not called from
		// a popup menu and a selection provider is set
		if (!fromPopupMenu && sp != null) setSelection(sp.getSelection());

		// First, restrict the wizard in the system types to show if this is
		// requested.
		if (restrictSystemTypesTo != null) {
			newConnWizard.restrictToSystemTypes(restrictSystemTypesTo);
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
		return new WizardDialog(getShell(), wizard);
	}

	/**
	 * Call this to restrict the system types that the user is allowed to choose
	 */
	public void restrictSystemTypes(IRSESystemType[] systemTypes) {
		this.restrictSystemTypesTo = systemTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseWizardAction#postProcessWizard(org.eclipse.jface.wizard.IWizard)
	 */
	protected void postProcessWizard(IWizard wizard) {
		if (wizard instanceof RSEMainNewConnectionWizard) wizard = ((RSEMainNewConnectionWizard)wizard).getSelectedWizard();
		if (wizard instanceof RSEDefaultNewConnectionWizard) {
			setValue(((RSEDefaultNewConnectionWizard)wizard).getCreatedHost());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseWizardAction#getDialogValue(org.eclipse.jface.dialogs.Dialog)
	 */
	protected Object getDialogValue(Dialog dlg) {
		// We have to trick the super implementation a little bit because otherwise
		// we do not get access to the current wizard instance. The postProcessWizard
		// implementation will use setValue() for pushing the created host instance,
		// which in turn we query here again in case the super implementation does
		// return null to us (which is the case if the wizard does not implement ISystemWizard,
		// what is what we do not want to do in case of RSEDefaultNewConnectionWizard anymore).
		Object value = super.getDialogValue(dlg);
		if (value == null && getValue() != null) value = getValue();
		return value;
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