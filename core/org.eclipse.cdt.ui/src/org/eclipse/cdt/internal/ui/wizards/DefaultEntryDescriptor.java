/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;

/**
 * Class to distinguish default entry descriptors from regular ones.
 */
public class DefaultEntryDescriptor extends EntryDescriptor {
	public DefaultEntryDescriptor(String _id, String _par, String _name, boolean _cat, CWizardHandler _h, Image _image) {
		super(_id, _par, _name, _cat, _h, _image);
	}

	public boolean isDefaultForCategory() {
		return true;
	}
}
