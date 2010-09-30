/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bogdan Gheorghe (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

/**
 * Configuration for indexer.
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {
	protected static final String INDEX_ALL_FILES = DialogsMessages.AbstractIndexerPage_indexAllFiles;
	protected static final String TRUE = String.valueOf(true);

	private Button fAllSources;
	private Button fAllHeadersDefault;
	private Button fAllHeadersAlt;
	private Button fIncludeHeuristics;
	private IntegerFieldEditor fFileSizeLimit;
	private Text fFilesToParseUpFront;
	private Button fSkipReferences;
	private Button fSkipImplicitReferences;
	private Button fSkipMacroAndTypeReferences;

    private IPropertyChangeListener validityChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updateValidState();
			}
        }
    };

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
		GridLayout gl;
		Composite page = ControlFactory.createComposite(parent, 1);
		Composite group= new Composite(page, SWT.NONE);
		
		fAllSources= createAllFilesButton(group);
		IProject prj= getCurrentProject();
		if (prj == null || !CProject.hasCCNature(prj)) {
			fAllHeadersDefault= createAllHeadersButton(group);
		} else {
			fAllHeadersDefault= createAllCppHeadersButton(group);
			fAllHeadersAlt= createAllCHeadersButton(group);
		}

		fIncludeHeuristics= createIncludeHeuristicsButton(group);
		fFileSizeLimit= createFileSizeLimit(group);

		group.setLayout(gl= new GridLayout(3, false));
		gl.marginWidth= 0;
		group.setLayoutData(new GridData());
		

		group= new Composite(page, SWT.NONE);
		group.setLayout(gl= new GridLayout(1, false));
		gl.marginWidth= 0;
		group.setLayoutData(new GridData());
		fSkipReferences= createSkipReferencesButton(group);
		fSkipImplicitReferences= createSkipImplicitReferencesButton(group);
		fSkipMacroAndTypeReferences= createSkipMacroAndTypeReferencesButton(group);
		
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
		if (fFileSizeLimit != null) {
			Object prop= properties.get(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB);
			int size= 0;
			if (prop != null) {
				try {
					size= Integer.parseInt(prop.toString());
				} catch (NumberFormatException e) {
				}
			}
			if (size <= 0) {
				size= IndexerPreferences.DEFAULT_FILE_SIZE_LIMIT;
			}
			fFileSizeLimit.setStringValue(String.valueOf(size));
		}
		if (fSkipReferences != null) {
			boolean skipReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_ALL_REFERENCES));
			fSkipReferences.setSelection(skipReferences);
		}
		if (fSkipImplicitReferences != null) {
			boolean skipImplicitReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES));
			fSkipImplicitReferences.setSelection(skipImplicitReferences);
		}		
		if (fSkipMacroAndTypeReferences != null) {
			boolean skipTypeReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES));
			boolean skipMacroReferences= TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES));
			fSkipMacroAndTypeReferences.setSelection(skipTypeReferences && skipMacroReferences);
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
		if (fFileSizeLimit != null) {
			props.put(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB, String.valueOf(fFileSizeLimit.getIntValue()));
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
		if (fSkipMacroAndTypeReferences != null) {
			final String value = String.valueOf(fSkipMacroAndTypeReferences.getSelection());
			props.put(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES, value);
			props.put(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES, value);
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
			if (fSkipMacroAndTypeReferences != null) {
				fSkipMacroAndTypeReferences.setEnabled(!skipReferences);
			}
		}
	}
	
    private void updateValidState() {
    	if (!fFileSizeLimit.isValid()) {
    		setErrorMessage(fFileSizeLimit.getErrorMessage());
    		setValid(false);
		} else {
    		setValid(true);
    	}
        final ICOptionContainer container = getContainer();
        if (container != null) {
        	container.updateContainer();
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
		((GridData) l.getLayoutData()).verticalIndent= 5;
		return ControlFactory.createTextField(page);
	} 

	private Button createAllFilesButton(Composite page) {
		Button result= ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllFiles);
		((GridData) result.getLayoutData()).horizontalSpan= 3;
		return result;
	}

	private Button createAllHeadersButton(Composite page) {
		Button result= ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeaders);
		((GridData) result.getLayoutData()).horizontalSpan= 3;
		return result;
	}

	private Button createAllCHeadersButton(Composite page) {
		Button result= ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeadersC);
		((GridData) result.getLayoutData()).horizontalSpan= 3;
		return result;
	}

	private Button createAllCppHeadersButton(Composite page) {
		Button result= ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeadersCpp);
		((GridData) result.getLayoutData()).horizontalSpan= 3;
		return result;
	}

	private Button createIncludeHeuristicsButton(Composite page) {
		Button result= ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_heuristicIncludes);
		((GridData) result.getLayoutData()).horizontalSpan= 3;
		return result;
	}
	
	private IntegerFieldEditor createFileSizeLimit(Composite group) {
		IntegerFieldEditor result= new IntegerFieldEditor(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB, DialogsMessages.AbstractIndexerPage_fileSizeLimit, group, 5);
		result.setValidRange(1, 100000);
		ControlFactory.createLabel(group, DialogsMessages.CacheSizeBlock_MB); 
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalAlignment= GridData.FILL;
		result.getLabelControl(group).setLayoutData(gd);
		result.setPropertyChangeListener(validityChangeListener);
		return result;
	}

	private Button createSkipReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipAllReferences);
	}

	private Button createSkipImplicitReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipImplicitReferences);
	}

	private Button createSkipMacroAndTypeReferencesButton(Composite page) {
		return ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_skipTypeAndMacroReferences);
	}
	
	/**
	 * Enable or disable support for parsing files up front. Essentially the according widget will be 
	 * enabled or disabled.
	 * There will be no effect by calling this function before the IndexerPage is created 
	 * (by {@link #createControl(Composite)}). 
	 * <p> By default, support for parsing files up front is enabled.
	 * 
	 * @since 5.3
	 */
	protected void setSupportForFilesParsedUpFront(boolean enable){
		if(fFilesToParseUpFront!=null){
			fFilesToParseUpFront.setEnabled(enable);
		}
	}
}
