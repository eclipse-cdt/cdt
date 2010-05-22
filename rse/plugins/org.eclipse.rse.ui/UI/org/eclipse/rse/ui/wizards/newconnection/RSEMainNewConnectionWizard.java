/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
 * Javier Montalvo Orús (Symbian) - Bug 158555 - newConnectionWizardDelegates can only be used once
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Uwe Stieber (Wind River) - [189426] System File/Folder Dialogs - New Connection Not Added to Drop Down
 * Martin Oberhuber (Wind River) - [235197][api] Unusable wizard after cancelling on first page
 * Uwe Stieber (Wind River) - [237816][api] restrictToSystemType does not work for RSEMainNewConnectionWizard
 * Uwe Stieber (Wind River) - [235084] New connection wizard can create connections of disabled type
 * Uwe Stieber (Wind River) - [248685] new connection wizard does not check the default selection against the restricted system type list
 *******************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * The New Connection wizard. This wizard allows users to create new RSE connections.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RSEMainNewConnectionWizard extends Wizard implements INewWizard, ISelectionProvider {
	/**
	 * Dialog settings slot id: Last selected system type id within the wizard.
	 * @since 3.0 made protected String public
	 */
	public static final String LAST_SELECTED_SYSTEM_TYPE_ID = "lastSelectedSystemTypeId"; //$NON-NLS-1$

	// The selected context as passed in from the invoking class.
	// Just pass on to the wizards. Do not interpret here!
	// @see #setSelectedContext(ISelection).
	private ISelection selectedContext;
	// The connection context as determined from the invoking class
	// @see #setConnectionContext(IHost)
	private IHost connectionContext;

	private RSENewConnectionWizardRegistry wizardRegistry;
	private IWizard selectedWizard;
	private IRSESystemType selectedSystemType;
	private boolean selectedWizardCanFinishEarly;

	private RSENewConnectionWizardSelectionPage mainPage;
	private final List initializedWizards = new LinkedList();
	private final List selectionChangedListener = new LinkedList();

	private IRSESystemType[] restrictedSystemTypes;
	private boolean onlySystemType;

	/**
	 * Constructor.
	 */
	public RSEMainNewConnectionWizard() {
		super();
		setWindowTitle(SystemResources.RESID_NEWCONN_TITLE);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);

		// Initialize the dialog settings for this wizard
		IDialogSettings settings = RSEUIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		if (settings.getSection(sectionName) == null) settings.addNewSection(sectionName);
		setDialogSettings(settings.getSection(sectionName));

		wizardRegistry = new RSENewConnectionWizardRegistry();
		selectedContext = null;
		selectedWizard = null;
		mainPage = new RSENewConnectionWizardSelectionPage(wizardRegistry);
		initializedWizards.clear();
		selectionChangedListener.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	public void dispose() {
		super.dispose();

		selectedContext = null;
		selectedSystemType = null;
		selectedWizardCanFinishEarly = false;
		mainPage = null;
		initializedWizards.clear();
		selectionChangedListener.clear();
		restrictedSystemTypes = null;
		onlySystemType = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getDefaultPageImage()
	 */
	public Image getDefaultPageImage() {
		return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTIONWIZARD_ID);
	}

	/**
	 * Restrict to a single system type. Users will not be shown the system type selection page in
	 * the wizard.
	 *
	 * @param systemType the system type to restrict to.
	 */
	public void restrictToSystemType(IRSESystemType systemType) {
		restrictToSystemTypes(new IRSESystemType[] { systemType });
	}

	/**
	 * Restrict system types. Users will only be able to choose from the given system types.
	 *
	 * @param systemTypes the system types to restrict to.
	 */
	public void restrictToSystemTypes(IRSESystemType[] systemTypes) {
		assert systemTypes != null;

		restrictedSystemTypes = systemTypes;
		onlySystemType = restrictedSystemTypes.length == 1;
		mainPage.restrictToSystemTypes(restrictedSystemTypes);

		if (onlySystemType && !restrictedSystemTypes[0].equals(selectedSystemType))
			selectedSystemType = restrictedSystemTypes[0];
		else if (restrictedSystemTypes.length > 0 && !Arrays.asList(restrictedSystemTypes).contains(selectedSystemType))
			selectedSystemType = null;
		else if (restrictedSystemTypes.length == 0)
			selectedSystemType = null;

		onSelectedSystemTypeChanged();
	}

	/**
	 * Returns if or if not the main new connection wizard has been restricted to only
	 * one system type.
	 *
	 * @return <code>True</code> if the wizard is restricted to only one system type, <code>false</code> otherwise.
	 */
	public final boolean isRestrictedToSingleSystemType() {
		return onlySystemType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		assert listener != null;
		if (!selectionChangedListener.contains(listener)) selectionChangedListener.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		assert listener != null;
		selectionChangedListener.remove(listener);
	}

	/**
	 * Notify the registered selection changed listener about a changed selection.
	 */
	private void fireSelectionChanged() {
		if (getSelection() == null) return;

		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Iterator iterator = selectionChangedListener.iterator();
		while (iterator.hasNext()) {
			ISelectionChangedListener listener = (ISelectionChangedListener)iterator.next();
			listener.selectionChanged(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		ISelection selection = null;

		// The system type must be available to construct the selection as
		// the system type is per contract always the first element
		IRSESystemType selected = isRestrictedToSingleSystemType() ? restrictedSystemTypes[0] : selectedSystemType;
		if (selected != null) {
			List selectionElements = new ArrayList();
			selectionElements.add(selected);
			// The second element in the selection is the selected context of the
			// called as passed in to us (if available).
			if (selectedContext != null) {
				selectionElements.add(selectedContext);
			}

			// construct the selection now
			selection = new StructuredSelection(selectionElements);
		}

		return selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			// Reset previous selected system type
			selectedSystemType = null;
			// Only if the first element of the selection is of type IRSESystemType,
			// we re-new the selected system type from the selection
			if (sel.getFirstElement() instanceof IRSESystemType) {
				// Get the system type candidate from the selection.
				IRSESystemType candidate = (IRSESystemType)((IStructuredSelection)selection).getFirstElement();
				// Accept only system types as selection which are enabled or if
				// the wizard has a restricted list of system types, is within the list
				// of restricted system types
				if (candidate.isEnabled() &&
						(restrictedSystemTypes == null ||
								restrictedSystemTypes.length == 0 ||
								Arrays.asList(restrictedSystemTypes).contains(candidate))) {
					selectedSystemType = candidate;
				}
			}

			// signal the system type change
			onSelectedSystemTypeChanged();
		}
	}

	/**
	 * Sets the currently selected context for the wizard as know by the caller
	 * of this method. The selected context is not interpreted by the main wizard,
	 * the selection is passed on as is to the nested wizards.
	 *
	 * @param selectedContext The selected context or <code>null</code>.
	 */
	public void setSelectedContext(ISelection selectedContext) {
		this.selectedContext = selectedContext;
	}

	/**
	 * Set the connection context for the wizard as determinded from
	 * the caller of this method. If non-null, the method will query
	 * the connections context system type and invoke <code>
	 * setSelection(...)</code> to apply the system type as the selected
	 * one.
	 *
	 * @param connectionContext The connection context or <code>null</code>.
	 */
	public void setConnectionContext(IHost connectionContext) {
		this.connectionContext = connectionContext;
		// If there is an connection context, extract the connections
		// system type from the connection context as use as default
		if (connectionContext != null && connectionContext.getSystemType() != null) {
			IRSESystemType systemType = connectionContext.getSystemType();
			// if we have found the system type object, pass on to setSelection(...)!
			if (systemType != null) setSelection(new StructuredSelection(systemType));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setSelection(selection);
	}

	/**
	 * Returns the wizard for the currently selected system type.
	 *
	 * @return The wizard for the currently selected system type. Must be never <code>null</code>.
	 */
	public IWizard getSelectedWizard() {
		return selectedWizard;
	}

	/**
	 * Called either by <code>restrictToSystemTypes(...)</code> or <code>
	 * setSelectedSystemType(...)</code> to notify that the selected system
	 * type has changed.
	 */
	protected void onSelectedSystemTypeChanged() {
		// unregister the previous selected wizard as selection changed listener
		if (selectedWizard instanceof ISelectionChangedListener) removeSelectionChangedListener((ISelectionChangedListener)selectedWizard);

		// Check if a wizard is registered for the selected system type
		IRSENewConnectionWizardDescriptor descriptor = getSelection() != null ?
			wizardRegistry.getWizardForSelection((IStructuredSelection) getSelection())
			: null;
		if (descriptor != null) {
			selectedWizard = descriptor.getWizard();
			selectedWizardCanFinishEarly = descriptor.canFinishEarly();
		} else {
			selectedWizard = null;
			selectedWizardCanFinishEarly = false;
		}

		// Check on the container association of the selected wizard.
		if (getContainer() != null && selectedWizard != null && !getContainer().equals(selectedWizard.getContainer())) {
			selectedWizard.setContainer(getContainer());
		}

		// Check if the wizard defines it's own window title. If not, make sure to pass the
		// main wizards window title.
		if (selectedWizard instanceof Wizard
				&& (selectedWizard.getWindowTitle() == null || "".equals(selectedWizard.getWindowTitle()))) { //$NON-NLS-1$
			((Wizard)selectedWizard).setWindowTitle(getWindowTitle());
		}

		// if the newly selected wizard is the default RSE new connection wizard
		// and the selected context is non-null, set the selected context to the
		// default RSE new connection wizard.
		if (selectedWizard instanceof RSEDefaultNewConnectionWizard) {
			((RSEDefaultNewConnectionWizard)selectedWizard).setSelectedContext(connectionContext);
		}

		// register the newly selected wizard as selection changed listener
		if (selectedWizard instanceof ISelectionChangedListener) {
			addSelectionChangedListener((ISelectionChangedListener)selectedWizard);
		}

		// Initialize the wizard pages and remember which wizard we have initialized already.
		// Note: Do not call IWizard.addPages() here in case the main wizard is restricted to
		//       a single system type. The IWizard.addPages() method will be called from the
		//       enclosing wizard dialog directly instead!
		if ((!onlySystemType || mainPage.getPreviousPage() != null) && selectedWizard != null && !initializedWizards.contains(selectedWizard)) {
			selectedWizard.addPages();
			initializedWizards.add(selectedWizard);
		}

		// notify the selection changed event to the listeners
		fireSelectionChanged();

		// Update the wizard container UI elements
		IWizardContainer container = getContainer();
		if (container != null && container.getCurrentPage() != null) {
			container.updateWindowTitle();
			container.updateTitleBar();
			container.updateButtons();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage(mainPage);
		// and restore the wizard's selection state from last session
		restoreWidgetValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = null;
		if (page != null && page == mainPage) {
			// Save the last selected system type on page transition.
			// But only if it is the main page.
			saveWidgetValues();
			if (getSelectedWizard() != null) nextPage = getSelectedWizard().getStartingPage();
		}

		if (nextPage == null) super.getNextPage(page);
		if (nextPage != null) nextPage.setPreviousPage(page);

		return nextPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		// We can finish from the main new connection wizard only if the selected
		// wizard can finish early
		return selectedWizardCanFinishEarly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizard#performFinish()
	 */
	public boolean performFinish() {
		// Save the current selection to the dialog settings
		saveWidgetValues();

		if (mainPage != null) mainPage.saveWidgetValues();

		return true;
	}

	/**
	 * Save the last selected system type id to the dialog settings.
	 * Called from <code>onSelectedSystemTypeChanged</code> and <code>
	 * performFinish</code>.
	 */
	protected void saveWidgetValues() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null && getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)getSelection();
			if (selection.getFirstElement() instanceof IRSESystemType) {
				dialogSettings.put(LAST_SELECTED_SYSTEM_TYPE_ID, ((IRSESystemType)selection.getFirstElement()).getId());
			}
		}
	}

	/**
	 * Restore the persistent saved wizard state. This method
	 * is called from the wizards constructor.
	 */
	protected void restoreWidgetValues() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			// Note: Current mode is that the remembered last selected system type id
			//       has priority over the type of a host selection within the remote
			//       systems view. We are leaving it that way for now out of consistency
			//       reason. If the host selection within the remote system view should
			//       get ever the priority, check for 'selectedSystemType == null' additional
			//       to the check for 'systemTypeId != null'.
			String systemTypeId = dialogSettings.get(LAST_SELECTED_SYSTEM_TYPE_ID);
			if (systemTypeId != null) {
				IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(systemTypeId);
				if (systemType != null && systemType.isEnabled()) {
					setSelection(new StructuredSelection(systemType));
				}
			}
		}
	}
}
