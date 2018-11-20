/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.util.Map;

import org.eclipse.cdt.ui.templateengine.pages.UIWizardPage;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard Node
 */
class WizardNode implements IWizardNode {
	private IWizard wizard;
	private Template template;
	private TemplateListSelectionPage parentPage;

	/**
	 * Constructor.
	 * @param parentPage
	 * @param template
	 */
	public WizardNode(TemplateListSelectionPage parentPage, Template template) {
		this.parentPage = parentPage;
		this.template = template;
	}

	@Override
	public void dispose() {
		if (wizard != null) {
			wizard.dispose();
			wizard = null;
		}
	}

	/**
	 * Returns the Template
	 */
	public Template getTemplate() {
		return template;
	}

	@Override
	public Point getExtent() {
		return new Point(-1, -1);
	}

	/**
	 * Returns the Wizard.
	 */
	@Override
	public IWizard getWizard() {
		if (wizard != null) {
			return wizard;
		}
		wizard = new Wizard() {
			{
				setWindowTitle(template.getLabel());
			}

			private boolean finishPressed;

			@Override
			public void addPages() {
				IWizardPage[] wpages = null;
				try {
					wpages = parentPage.getPagesAfterTemplateSelection();
					for (IWizardPage wpage : wpages) {
						addPage(wpage);
					}
				} catch (Exception e) {
				}

				Map<String, UIWizardPage> pages = template.getUIPages();
				for (Object element : template.getPagesOrderVector()) {
					String id = (String) element;
					addPage(pages.get(id));
				}

				wpages = parentPage.getPagesAfterTemplatePages();
				for (IWizardPage wpage : wpages) {
					addPage(wpage);
				}
			}

			@Override
			public boolean performFinish() {
				Map<String, String> valueStore = template.getValueStore();
				finishPressed = true;
				getContainer().updateButtons();
				IWizardPage[] wpages = getPages();
				for (IWizardPage page : wpages) {
					if (page instanceof UIWizardPage)
						valueStore.putAll(((UIWizardPage) page).getPageData());
				}
				template.getValueStore().putAll(parentPage.getDataInPreviousPages());
				return true;
			}

			@Override
			public boolean canFinish() {
				return !finishPressed && super.canFinish();
			}

			@Override
			public void createPageControls(Composite pageContainer) {
				super.createPageControls(pageContainer);
				parentPage.adjustTemplateValues(template);
				IWizardPage[] wpages = getPages();
				for (IWizardPage page : wpages) {
					if (page instanceof UIWizardPage)
						((UIWizardPage) page).getComposite().getUIElement().setValues(template.getValueStore());
				}
			}

			@Override
			public Image getDefaultPageImage() {
				return parentPage.getImage();
			}
		};
		return wizard;
	}

	@Override
	public boolean isContentCreated() {
		return wizard != null;
	}
}
