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

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateEngineUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIComposite;

/**
 * 
 * The UIPage extends DialogPage, it implements the createControl() abstract
 * method. The UIPage is the base class for UIWizardPage. The UIPage contains a
 * UIComposite, which extends a SWT composite. The SWT widgets are added to
 * UIComposite.
 */
public abstract class UIPage extends DialogPage {
	/**
	 * The Composite belonging to this page. The SWT widgets are added to this Composite. UIComposite instance is the top level control of this page. This top level control is initialized in createControl method.
	 */
	protected UIComposite uiComposite;

	/**
	 * resources ID which will be displayed as F1 help in Title area.
	 */
	protected static String RESOURCES_ID = CCorePlugin.PLUGIN_ID + ".resources"; //$NON-NLS-1$

	/**
	 * The UIElement (group), to which this page corresponds. Every UIElement group corresponds to a UIPage. The children of this goup are UIElement's (SWT widgets). Which are added to the UIComposite.
	 */
	protected final UIElement uiElement;

	/**
	 * ValueStore for this instance of Template.
	 */
	protected final Map<String, String> valueStore;

	/**
	 * Title set for this WizardPage.
	 */
	protected String title;

	/**
	 * Property Group Id corresponding this page
	 */
	protected String pageId;

	private ImageDescriptor imageDescriptor;

	/**
	 * 
	 * @param name
	 *            Name of this UIPage.
	 * @param element
	 *            The group UIElement.
	 */
	protected UIPage(String name, UIElement element, Map<String, String> valueStore) {
		super(name);
		setTitle(name);
		setDescription(element.getAttributes().get(UIElement.DESCRIPTION));
		try {
			String imageLoc = element.getAttributes().get(UIElement.IMAGELOCATION);
			if (imageLoc != null) {
				URL url = FileLocator.toFileURL(FileLocator.find(CCorePlugin.getDefault().getBundle(), new Path(imageLoc), null));
				imageDescriptor = ImageDescriptor.createFromURL(url);
			}
		} catch (Exception e) {
			TemplateEngineUtil.log(e);
		}

		super.setImageDescriptor(imageDescriptor);
		//TODO: Fix the imagedescriptor later.
		//setImageDescriptor(TemplateEnginePlugin.imageDescriptorFromPlugin(TemplateEnginePlugin.getDefault().getWizardIconPluginID(), TemplateEnginePlugin.getDefault().getWizardIconFile()));

		title = name;
		uiElement = element;
		uiElement.setValues(valueStore);
		this.valueStore = valueStore;
		//TODO: Check the from which plugin the PLUGIN_ID comes from i.e. from CCorePlugin or CUIPlugin
		pageId = CUIPlugin.getPluginId() + "." + (uiElement.getAttributes()).get(UIElement.ID); //$NON-NLS-1$
	}

	/**
	 * The data contained in the Input elements (SWT widgets), on this page is
	 * extracted and put into an HashMap. The same is returned.
	 * 
	 * @return HashMap. The data contained in the widgets on this page.
	 */
	public Map<String, String> getPageData() {
		return uiElement.getValues();
	}

	/**
	 * This is an overridden definition for the same method in DialogPage. The
	 * top level control is returned.
	 * 
	 * @return Control.
	 */
	@Override
	public Control getControl() {
		return uiComposite;
	}

	/**
	 * 
	 * This returns UICompostie as UIComposite instance. Unlike the getControl.
	 * 
	 * @return UIComposite, used in this page.
	 */
	public UIComposite getComposite() {
		return uiComposite;
	}
}
