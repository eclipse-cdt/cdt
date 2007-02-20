/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others. All rights reserved.
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
 * Javier Montalvo Orús (Symbian) - Bug 158555 - newConnectionWizardDelegates can only be used once
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 ********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

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
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * The New Connection wizard. This wizard allows users to create new RSE connections.
 */
public class RSEMainNewConnectionWizard extends Wizard implements INewWizard, ISelectionProvider {
	protected static final String LAST_SELECTED_SYSTEM_TYPE_ID = "lastSelectedSystemTypeId"; //$NON-NLS-1$
	
	private IWizard selectedWizard;
	private IRSESystemType selectedSystemType;
	private boolean selectedWizardCanFinishEarly;
	
	private final RSENewConnectionWizardSelectionPage mainPage;
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
		
		mainPage = new RSENewConnectionWizardSelectionPage();
		initializedWizards.clear();
		selectionChangedListener.clear();
		
		// and finally restore the wizard state
		restoreFromDialogSettings();
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
		onSelectedSystemTypeChanged();
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
		IRSESystemType selected = onlySystemType ? restrictedSystemTypes[0] : selectedSystemType;
		return selected != null ? new StructuredSelection(selected) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection 
				&& ((IStructuredSelection)selection).getFirstElement() instanceof IRSESystemType) {
			selectedSystemType = (IRSESystemType)((IStructuredSelection)selection).getFirstElement();
		} else {
			selectedSystemType = null;
		}
		onSelectedSystemTypeChanged();
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
		IRSENewConnectionWizardDescriptor descriptor = getSelection() != null ? RSENewConnectionWizardRegistry.getInstance().getWizardForSelection((IStructuredSelection)getSelection()) : null;
		if (descriptor != null) {
			selectedWizard = descriptor.getWizard();
			selectedWizardCanFinishEarly = descriptor.canFinishEarly();
		} else {
			selectedWizard = null;
			selectedWizardCanFinishEarly = false;
		}
		
		// register the newly selected wizard as selection changed listener
		if (selectedWizard instanceof ISelectionChangedListener) {
			addSelectionChangedListener((ISelectionChangedListener)selectedWizard);
		}
		
		// notify the selection changed event to the listeners
		fireSelectionChanged();
		
		// Initialize the wizard pages and remember which wizard we have initialized already
		if (selectedWizard != null && !initializedWizards.contains(selectedWizard)) {
			selectedWizard.addPages();
			initializedWizards.add(selectedWizard);
		}

		// Update the wizard container UI elements
		IWizardContainer container = getContainer();
		if (container != null && container.getCurrentPage() != null) {
			container.updateWindowTitle();
			container.updateTitleBar();
			container.updateButtons();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
	 */
	public IWizardPage getStartingPage() {
		if (onlySystemType && getSelectedWizard() != null) return getSelectedWizard().getStartingPage();
		return super.getStartingPage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizard#addPages()
	 */
	public void addPages() {
		// It we are not restricted to only one system type, add the
		// system type selection page.
		if (!onlySystemType) addPage(mainPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = null;
		if (page != null && page == mainPage && getSelectedWizard() != null) {
			 nextPage = getSelectedWizard().getStartingPage();
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
		// Note: Do _NOT_ delegate the performFinish from here to the selected
		// wizard!! The outer wizard dialog is handling the nested wizards by
		// default already itself!!

		// Save the current selection to the dialog settings
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null && getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)getSelection();
			if (selection.getFirstElement() instanceof IRSESystemType) {
				dialogSettings.put(LAST_SELECTED_SYSTEM_TYPE_ID, ((IRSESystemType)selection.getFirstElement()).getId());
			}
		}
		
		return true;
	}

	/**
	 * Restore the persistent saved wizard state. This method
	 * is called from the wizards constructor.
	 */
	protected void restoreFromDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			String systemTypeId = dialogSettings.get(LAST_SELECTED_SYSTEM_TYPE_ID);
			if (systemTypeId != null) {
				IRSESystemType systemType = RSECorePlugin.getDefault().getRegistry().getSystemTypeById(systemTypeId);
				if (systemType != null) {
					setSelection(new StructuredSelection(systemType));
				}
			}
		}
	}
}
