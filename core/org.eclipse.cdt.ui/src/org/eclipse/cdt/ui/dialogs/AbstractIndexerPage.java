/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;


/**
 * @author Bogdan Gheorghe
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {
	protected static final String INDEX_ALL_FILES = DialogsMessages.AbstractIndexerPage_indexAllFiles;
	protected static final String TRUE = String.valueOf(true);

	private Button fAllSources;
	private Button fAllHeadersDefault;
	private Button fAllHeadersAlt;
	private Button fIncludeHeuristics;
	private Text fFilesToParseUpFront;
	private Button fSkipReferences;
	private Button fSkipTypeReferences;
	private Button fSkipImplicitReferences;
	private Button fSkipMacroReferences;

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

	@Override
	public void createControl(Composite parent) {
		Composite page = ControlFactory.createComposite(parent, 1);
		fAllSources= createAllFilesButton(page);
		IProject prj= getCurrentProject();
		if (prj == null || !CProject.hasCCNature(prj)) {
			fAllHeadersDefault= createAllHeadersButton(page);
		} else {
			fAllHeadersDefault= createAllCppHeadersButton(page);
			fAllHeadersAlt= createAllCHeadersButton(page);
		}
		fIncludeHeuristics= createIncludeHeuristicsButton(page);
		fSkipReferences= createSkipReferencesButton(page);
		fSkipImplicitReferences= createSkipImplicitReferencesButton(page);
		fSkipTypeReferences= createSkipTypeReferencesButton(page);
		fSkipMacroReferences= createSkipMacroReferencesButton(page);
		fFilesToParseUpFront= createParseUpFrontTextField(page);
		
		final SelectionAdapter selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		};
		fSkipReferences.addSelectionListener(selectionListener);
		fAllSources.addSelectionListener(selectionListener);
		setControl(page);
	}

	/**
	 * Use the properties to initialize the controls of the page. Fill in defaults 
	 * for properties that are missing.
	 * @since 4.0
	 */
	public void setProperties(Properties properties) {
		if (fAllSources != null) {
			boolean indexAllFiles= TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_ALL_FILES));
			fAllSources.setSelection(indexAllFiles);
		}
		if (fAllHeadersDefault != null) {
			boolean indexAllFiles= TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG));
			fAllHeadersDefault.setSelection(indexAllFiles);
		}
		if (fAllHeadersAlt != null) {
			boolean indexAllFiles= TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG));
			fAllHeadersAlt.setSelection(indexAllFiles);
		}
		if (fIncludeHeuristics != null) {
			Object prop= properties.get(IndexerPreferences.KEY_INCLUDE_HEURISTICS);
			boolean use= prop == null || TRUE.equals(prop);
			fIncludeHeuristics.setSelection(use);
		}
		if (fSkipReferences != null) {
			boolean skipReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_ALL_REFERENCES));
			fSkipReferences.setSelection(skipReferences);
		}
		if (fSkipImplicitReferences != null) {
			boolean skipImplicitReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES));
			fSkipImplicitReferences.setSelection(skipImplicitReferences);
		}		
		if (fSkipTypeReferences != null) {
			boolean skipTypeReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES));
			fSkipTypeReferences.setSelection(skipTypeReferences);
		}		
		if (fSkipMacroReferences != null) {
			boolean skipMacroReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES));
			fSkipMacroReferences.setSelection(skipMacroReferences);
		}		
		if (fFilesToParseUpFront != null) {
			String files = getNotNull(properties, IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT);
			fFilesToParseUpFront.setText(files);
		}
		updateEnablement();
	}

	/**
	 * Return the properties according to the selections on the page.
	 * @since 4.0
	 */
	public Properties getProperties(){
		Properties props= new Properties();
		if (fAllSources != null) {
			props.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(fAllSources.getSelection()));
		}
		if (fAllHeadersDefault != null) {
			props.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, String.valueOf(fAllHeadersDefault.getSelection()));
		}
		if (fAllHeadersAlt != null) {
			props.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, String.valueOf(fAllHeadersAlt.getSelection()));
		}
		if (fIncludeHeuristics != null) {
			props.put(IndexerPreferences.KEY_INCLUDE_HEURISTICS, String.valueOf(fIncludeHeuristics.getSelection()));
		}
		if (fFilesToParseUpFront != null) {
			props.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, fFilesToParseUpFront.getText());
		}
		if (fSkipReferences != null) {
			props.put(IndexerPreferences.KEY_SKIP_ALL_REFERENCES, String.valueOf(fSkipReferences.getSelection()));
		}
		if (fSkipImplicitReferences != null) {
			props.put(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES, String.valueOf(fSkipImplicitReferences.getSelection()));
		}
		if (fSkipTypeReferences != null) {
			props.put(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES, String.valueOf(fSkipTypeReferences.getSelection()));
		}
		if (fSkipMacroReferences != null) {
			props.put(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES, String.valueOf(fSkipMacroReferences.getSelection()));
		}
		return props;
	}

	/**
	 * {@link #getProperties()} will be called instead.
	 */
	@Override
	final public void performApply(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@link #setProperties(Properties)} will be called instead.
	 */
	@Override
	final public void performDefaults() {
		throw new UnsupportedOperationException();
	}

	public void updateEnablement() {
		if (fSkipReferences != null) {
			final boolean skipReferences = fSkipReferences.getSelection();
			if (fSkipImplicitReferences != null) {
				fSkipImplicitReferences.setEnabled(!skipReferences);
			}
			if (fSkipTypeReferences != null) {
				fSkipTypeReferences.setEnabled(!skipReferences);
			}
			if (fSkipMacroReferences != null) {
				fSkipMacroReferences.setEnabled(!skipReferences);
			}
		}
		if (fAllSources != null) {
			final boolean all= fAllSources.getSelection();
			if (fAllHeadersDefault != null) {
				fAllHeadersDefault.setEnabled(all);
			}
			if (fAllHeadersAlt != null) {
				fAllHeadersAlt.setEnabled(all);
			}
		}
	}
	
	private String getNotNull(Properties properties, String key) {
		String files= (String) properties.get(key);
		if (files == null) {
			files= ""; //$NON-NLS-1$
		}
		return files;
	}

	private Text createParseUpFrontTextField(Composite page) {
		Label l= ControlFactory.createLabel(page, DialogsMessages.AbstractIndexerPage_indexUpFront);
		((GridData) l.getLayoutData()).verticalIndent=5;
		return ControlFactory.createTextField(page);
	} 

	private Button createAllFilesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllFiles);
	}

	private Button createAllHeadersButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeaders);
	}

	private Button createAllCHeadersButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeadersC);
	}

	private Button createAllCppHeadersButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeadersCpp);
	}

	private Button createIncludeHeuristicsButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_heuristicIncludes);
	}

	private Button createSkipReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipAllReferences);
	}

	private Button createSkipImplicitReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipImplicitReferences);
	}

	private Button createSkipTypeReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipTypeReferences);
	}

	private Button createSkipMacroReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipMacroReferences);
	}
}
