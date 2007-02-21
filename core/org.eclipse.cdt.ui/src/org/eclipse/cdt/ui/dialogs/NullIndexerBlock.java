/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/**
 * @author Bogdan Gheorghe
 */
public class NullIndexerBlock extends AbstractIndexerPage {

	public void createControl(Composite parent) {
	    Composite comp = new Composite(parent, SWT.NULL);
        setControl(comp);
	}

	public Properties getProperties() {
		return new Properties();
	}

	public void setProperties(Properties properties) {
	}
}
