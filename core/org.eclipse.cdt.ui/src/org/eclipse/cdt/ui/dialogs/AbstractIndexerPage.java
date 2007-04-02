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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;


/**
 * @author Bogdan Gheorghe
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {
	protected static final String INDEX_ALL_FILES = DialogsMessages.AbstractIndexerPage_indexAllFiles;
	protected static final String TRUE = String.valueOf(true);

	private Button fAllFiles;
	private Text fFilesToParseUpFront;

	protected AbstractIndexerPage() {
		super();
	}

	final public IProject getCurrentProject() {
		ICOptionContainer container = getContainer();
		if (container != null) {
			return container.getProject();
		}
		return null;
	}

	public void createControl(Composite parent) {
		Composite page = ControlFactory.createComposite(parent, 1);
		fAllFiles= createAllFilesButton(page);
		fFilesToParseUpFront= createParseUpFrontTextField(page);
		setControl(page);
	}

	/**
	 * Use the properties to initialize the controls of the page. Fill in defaults 
	 * for properties that are missing.
	 * @since 4.0
	 */
	public void setProperties(Properties properties) {
		if (fAllFiles != null) {
			boolean indexAllFiles= TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_ALL_FILES));
			fAllFiles.setSelection(indexAllFiles);
		}
		if (fFilesToParseUpFront != null) {
			String files = getNotNull(properties, IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT);
			fFilesToParseUpFront.setText(files);
		}
	}

	/**
	 * Return the properties according to the selections on the page.
	 * @since 4.0
	 */
	public Properties getProperties(){
		Properties props= new Properties();
		if (fAllFiles != null) {
			props.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(fAllFiles.getSelection()));
		}
		if (fFilesToParseUpFront != null) {
			props.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, fFilesToParseUpFront.getText());
		}
		return props;
	}

	/**
	 * {@link #getProperties()} will be called instead.
	 */
	final public void performApply(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@link #setProperties(Properties)} will be called instead.
	 */
	final public void performDefaults() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated, never called.
	 */
	public void updateEnablement() {
	}
	
	private String getNotNull(Properties properties, String key) {
		String files= (String) properties.get(key);
		if (files == null) {
			files= ""; //$NON-NLS-1$
		}
		return files;
	}

	private Text createParseUpFrontTextField(Composite page) {
		new Label(page, SWT.NONE);
		ControlFactory.createLabel(page, DialogsMessages.AbstractIndexerPage_indexUpFront);
		return ControlFactory.createTextField(page);
	}

	private Button createAllFilesButton(Composite page) {
		return ControlFactory.createCheckBox(page, INDEX_ALL_FILES);
	}
}
