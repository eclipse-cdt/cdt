/*******************************************************************************
 * Copyright (c) 2012 Doug Schaefer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.cdt.ui.templateengine.Template;

/**
 * @since 5.4
 */
public interface ProjectTypePage extends IWizardPage {

	/**
	 * Init the page. Return false if the page isn't needed.
	 * 
	 * @param template The selected template
	 * @param wizard The wizard object
	 * @param nextPage The next page after this one
	 * @return whether page is really needed
	 */
	boolean init(Template template, IWizard wizard, IWizardPage nextPage);
	
}
