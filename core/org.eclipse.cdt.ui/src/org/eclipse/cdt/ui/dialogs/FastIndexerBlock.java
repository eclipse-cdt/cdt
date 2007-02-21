/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.dialogs;

import java.util.Properties;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

public class FastIndexerBlock extends AbstractIndexerPage {
	private Button fAllFiles;

	public void createControl(Composite parent) {
		Composite page = ControlFactory.createComposite(parent, 1);
		fAllFiles= ControlFactory.createCheckBox(page, INDEX_ALL_FILES);
		setControl(page);
	}

	public Properties getProperties() {
		Properties props= new Properties();
		props.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(fAllFiles.getSelection()));
		return props;
	}

	public void setProperties(Properties properties) {
		boolean indexAllFiles= TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_ALL_FILES));
		fAllFiles.setSelection(indexAllFiles);
	}
}
