/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.pages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.cdt.ui.templateengine.event.PatternEvent;
import org.eclipse.cdt.ui.templateengine.event.PatternEventListener;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIComposite;


/**
 * UIWizardPage provides implementation IWizardPage. UIWizardPage is a
 * WizardPage.
 */

public class UIWizardPage extends UIPage implements IWizardDataPage, PatternEventListener {

	/**
	 * This map will contain reference to the source widgets, which has generated the
	 * SWT events. If this map contains an event source, the error message will not be cleared.
	 */
	HashMap<Object, String> validInvalid;

	/**
	 * Page Name
	 */
	private String name;

	/**
	 * The Wizard to which the Page belongs. null, If this page is yet to be added to a Wizard.
	 */
	private IWizard wizard = null;

	/**
	 * Indicates whether this page is complete.
	 */
	private boolean isPageComplete;

	/**
	 * That page that was shown right before this page became visible. null if none.
	 */
	private IWizardPage previousPage = null;

	private IWizardPage nextPage = null;

	/**
	 * Title of the page, Page Name and UIElement group are the parameters.
	 *
	 * @param title
	 *            Title of this page
	 * @param pageName
	 *            Name of this page
	 * @param uiElement
	 *            The UIElement group.
	 */
	public UIWizardPage(String title, String pageName, UIElement uiElement, Map<String, String> valueStore) {
		super(title, uiElement, valueStore);
		name = pageName;
		validInvalid = new HashMap<Object, String>();
		isPageComplete = uiElement.isValid();
	}

	/**
	 * returns true if the page is complete, and there is a next page to flip.
	 *
	 * @return boolean. true if can flip to next page, otherwise false.
	 */
	@Override
	public boolean canFlipToNextPage() {
		boolean retVal = false;

		if (isPageComplete() && (getNextPage() != null))
			retVal = true;

		return retVal;
	}

	/**
	 * Returns the wizard container for this wizard page. null, if wizard page
	 * is yet to be adedd to a wizard, or the wizard is yet to be added to a
	 * container.
	 */
	protected IWizardContainer getContainer() {
		if (wizard == null)
			return null;

		return wizard.getContainer();
	}

	/**
	 * Returns the dialog setting for this wizard page. null, if none exists.
	 *
	 * @return IDialogSettings, if Wizard is not set null.
	 */

	protected IDialogSettings getDialogSettings() {
		if (wizard == null)
			return null;

		return wizard.getDialogSettings();
	}

	/**
	 * Overloaded from DialogPage get the Image from the super class,
	 * DialogPage. if not defined, then the default page Image is returned.
	 *
	 * @return Image.
	 */

	@Override
	public Image getImage() {
		Image result = super.getImage();

		if (result == null && wizard != null)
			return wizard.getDefaultPageImage();

		return result;
	}

	/**
	 * @return String, page Name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * gets the Nextpage to be displayed, if set.
	 *
	 * @return IWizardPage.
	 */
	@Override
	public IWizardPage getNextPage() {
		if (nextPage != null)
			return nextPage;

		if (wizard == null)
			return null;

		return wizard.getNextPage(this);
	}

	@Override
	public void setNextPage(IWizardPage page) {
		nextPage  = page;
	}

	/**
	 * returns the PreviousPage, if Previous page is not initialized. Wizard is
	 * checked for previous page. if wizard for this page is not set null is
	 * returned.
	 *
	 * @return IWizardPage
	 */
	@Override
	public IWizardPage getPreviousPage() {
		if (previousPage != null)
			return previousPage;

		if (wizard == null)
			return null;

		return wizard.getPreviousPage(this);

	}

	/**
	 * Overloaded from DialogPage
	 */
	@Override
	public Shell getShell() {
		IWizardContainer container = getContainer();

		if (container == null)
			return null;

		return container.getShell();
	}

	/**
	 * returns the Wizard instance to which this page is added.
	 *
	 * @return IWizard.
	 */
	@Override
	public IWizard getWizard() {
		return wizard;
	}

	/**
	 * is this is the current page being displayed.
	 *
	 * @return boolean, true if this is the current page. otherwise false.
	 */
	protected boolean isCurrentPage() {
		boolean retVal = false;
		if ((getContainer() != null) && (this == getContainer().getCurrentPage()))
			retVal = true;

		return retVal;
	}

	/**
	 * @return boolean, true if this page is complete, otherwise false.
	 */
	@Override
	public boolean isPageComplete() {
		return isPageComplete;
	}

	/**
	 * Methods from IDialOogPage
	 */

	public void setPageComplete(boolean complete) {
		isPageComplete = complete;

		if (isCurrentPage())
			getContainer().updateButtons();
	}

	/**
	 * Method from IWizardPage
	 *
	 */
	@Override
	public void setPreviousPage(IWizardPage page) {
		previousPage = page;
	}

	/**
	 * set the Wizard for this page, the wizard will contain this page. In the
	 * list of pages which will be displayed as part of this Wizard.
	 *
	 */
	@Override
	public void setWizard(IWizard newWizard) {
		wizard = newWizard;
	}

	/**
	 * @return String, Page name of this page.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Creates the top level control for this dialog page under the given parent
	 * composite.
	 *
	 * @param parent
	 *            the parent composite
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		uiComposite = new UIComposite(parent, uiElement, valueStore);
		uiComposite.addPatternListener(this);
		uiElement.createWidgets(uiComposite);

		// set the focus so that InfoPop is displayed when F1 is Pressed.
		uiComposite.setFocus();

		setControl(uiComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), pageId);

		setPageComplete(uiComposite.isValid());
	}

	/**
	 * This method is implemented to handle the PatternEvent's generated by SWT
	 * widgets contained in this page. When the user enters data violating the
	 * pattern of data expected by this SWT widgets, a PatternEvent is fired to
	 * the Container. Which has to reflect the same as an ErrorMeesage. Get the
	 * source of the PatternEvent, and the error String causing the
	 * PatternEvent. Store this pair in validInvalid HashMap.
	 *
	 */
	@Override
	public void patternPerformed(PatternEvent patternEvent) {
		if (!patternEvent.getValid()) {

			validInvalid.put(patternEvent.getSource(), patternEvent.toString());
			setErrorMessage(getErrorString());
			setPageComplete(validInvalid.isEmpty() && uiComposite.isValid());

		} else {

			validInvalid.remove(patternEvent.getSource());
			setPageComplete(validInvalid.isEmpty() && uiComposite.isValid());
			if (validInvalid.isEmpty()) {
				setErrorMessage(null);
			} else {
				setErrorMessage(getErrorString());
			}
		}

		getContainer().updateMessage();
	}

	/**
	 * Iterate through the validInvalid HashMap, formulate the error string
	 * return the same. This will ensure that the proper error string is
	 * updated.
	 *
	 * @return
	 */
	private String getErrorString() {
		Iterator<Object> iterator = validInvalid.keySet().iterator();
		String message = ""; //$NON-NLS-1$

		// only display one error message at a time
		if (iterator.hasNext()) {
			message = validInvalid.get(iterator.next());
		}
		return message;
	}
}

