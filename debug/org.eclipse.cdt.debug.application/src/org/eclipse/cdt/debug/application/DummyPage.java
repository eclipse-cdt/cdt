/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

// This class is just to replace a category reference in org.eclipse.cdt.ui that
// we exclude because we do not bring in the org.eclipse.cdt.managedbuild.ui plug-in.
public class DummyPage extends PropertyPage implements IWorkbenchPropertyPage {

	public DummyPage() {
		// do nothing
	}

	@Override
	protected Control createContents(Composite parent) {
		return null;
	}

}
