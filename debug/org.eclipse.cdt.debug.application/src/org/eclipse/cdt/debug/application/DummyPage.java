/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

// This class is just to replace a category reference in org.eclipse.cdt.ui that
// we exclude because we do not bring in the org.eclipse.cdt.managedbuild.ui plug-in.
public class DummyPage extends PropertyPage {

	public DummyPage() {
		// do nothing
	}

	@Override
	protected Control createContents(Composite parent) {
		return null;
	}

}
