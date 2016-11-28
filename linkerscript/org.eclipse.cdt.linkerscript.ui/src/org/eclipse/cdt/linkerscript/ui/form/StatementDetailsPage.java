/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;

public class StatementDetailsPage extends AbstractDetailsPage implements IDetailsPage {

	public StatementDetailsPage(ILinkerScriptModel model) {
		super(model, "Statement Details", "Details of generic linker statement", 0);

	}

	@Override
	protected void doUpdate() {
		// no additional controls to update
	}

	@Override
	public void doCreateContents(Composite section) {
		// no additional controls
	}

}
