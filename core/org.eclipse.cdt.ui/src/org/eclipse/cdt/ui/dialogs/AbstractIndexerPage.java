/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bogdan Gheorghe (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Properties;

import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.PixelConverter;
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

/**
 * Configuration for indexer.
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {
	protected static final String INDEX_ALL_FILES = DialogsMessages.AbstractIndexerPage_indexAllFiles;
	protected static final String TRUE = String.valueOf(true);

	private Button fAllSources;
	private Button fAllHeadersDefault;
	private Button fAllHeadersAlt;
	private Button fIndexOnOpen;
	private Button fIncludeHeuristics;
	private IntegerFieldEditor fFileSizeLimit;
	private IntegerFieldEditor fIncludedFileSizeLimit;
	private Button fSkipReferences;
	private Button fSkipImplicitReferences;
	private Button fSkipMacroAndTypeReferences;
	private Button fIndexAllHeaderVersions;
	private Text fIndexAllVersionsSpecificHeaders;

	private IPropertyChangeListener validityChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updateValidState();
			}
		}
	};
	/** @since 5.3 */
	protected PixelConverter pixelConverter;

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
		pixelConverter = new PixelConverter(parent);
		GridLayout gl;
		Composite page = new Composite(parent, SWT.NULL);
		page.setFont(parent.getFont());
		page.setLayout(gl = new GridLayout(1, true));
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		page.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite group = new Composite(page, SWT.NONE);
		group.setLayout(gl = new GridLayout(3, false));
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fAllSources = createAllFilesButton(group);
		IProject prj = getCurrentProject();
		if (prj == null || !CProject.hasCCNature(prj)) {
			fAllHeadersDefault = createAllHeadersButton(group);
		} else {
			fAllHeadersDefault = createAllCppHeadersButton(group);
			fAllHeadersAlt = createAllCHeadersButton(group);
		}

		fIndexAllHeaderVersions = ControlFactory.createCheckBox(group,
				DialogsMessages.AbstractIndexerPage_indexAllHeaderVersions);
		fIndexAllHeaderVersions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		Label label = ControlFactory.createLabel(group,
				DialogsMessages.AbstractIndexerPage_indexAllVersionsSpecificHeaders);
		int indent = pixelConverter.convertHorizontalDLUsToPixels(12);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		layoutData.horizontalIndent = indent;
		label.setLayoutData(layoutData);
		fIndexAllVersionsSpecificHeaders = ControlFactory.createTextField(group);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		layoutData.horizontalIndent = indent;
		fIndexAllVersionsSpecificHeaders.setLayoutData(layoutData);

		fIndexOnOpen = createIndexOnOpenButton(group);

		fIncludeHeuristics = createIncludeHeuristicsButton(group);

		group = new Composite(page, SWT.NONE);
		fFileSizeLimit = createFileSizeLimit(group, IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB,
				DialogsMessages.AbstractIndexerPage_fileSizeLimit);
		fIncludedFileSizeLimit = createFileSizeLimit(group, IndexerPreferences.KEY_SKIP_INCLUDED_FILES_LARGER_THAN_MB,
				DialogsMessages.AbstractIndexerPage_includedFileSizeLimit);
		group.setLayout(gl = new GridLayout(3, false));
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		group.setLayoutData(new GridData());

		group = new Composite(page, SWT.NONE);
		group.setLayout(gl = new GridLayout(1, false));
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		group.setLayoutData(new GridData());
		fSkipReferences = createSkipReferencesButton(group);
		fSkipImplicitReferences = createSkipImplicitReferencesButton(group);
		fSkipMacroAndTypeReferences = createSkipMacroAndTypeReferencesButton(group);

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
			boolean indexAllFiles = TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_ALL_FILES));
			fAllSources.setSelection(indexAllFiles);
		}
		if (fAllHeadersDefault != null) {
			boolean indexAllFiles = TRUE
					.equals(properties.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG));
			fAllHeadersDefault.setSelection(indexAllFiles);
		}
		if (fAllHeadersAlt != null) {
			boolean indexAllFiles = TRUE
					.equals(properties.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG));
			fAllHeadersAlt.setSelection(indexAllFiles);
		}
		if (fIndexOnOpen != null) {
			boolean indexOnOpen = TRUE.equals(properties.get(IndexerPreferences.KEY_INDEX_ON_OPEN));
			fIndexOnOpen.setSelection(indexOnOpen);
		}
		if (fIncludeHeuristics != null) {
			Object prop = properties.get(IndexerPreferences.KEY_INCLUDE_HEURISTICS);
			boolean use = prop == null || TRUE.equals(prop);
			fIncludeHeuristics.setSelection(use);
		}
		if (fFileSizeLimit != null) {
			Object prop = properties.get(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB);
			int size = 0;
			if (prop != null) {
				try {
					size = Integer.parseInt(prop.toString());
				} catch (NumberFormatException e) {
				}
			}
			if (size <= 0) {
				size = IndexerPreferences.DEFAULT_FILE_SIZE_LIMIT_MB;
			}
			fFileSizeLimit.setStringValue(String.valueOf(size));
		}
		if (fIncludedFileSizeLimit != null) {
			Object prop = properties.get(IndexerPreferences.KEY_SKIP_INCLUDED_FILES_LARGER_THAN_MB);
			int size = 0;
			if (prop != null) {
				try {
					size = Integer.parseInt(prop.toString());
				} catch (NumberFormatException e) {
				}
			}
			if (size <= 0) {
				size = IndexerPreferences.DEFAULT_INCLUDED_FILE_SIZE_LIMIT_MB;
			}
			fIncludedFileSizeLimit.setStringValue(String.valueOf(size));
		}
		if (fSkipReferences != null) {
			boolean skipReferences = TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_ALL_REFERENCES));
			fSkipReferences.setSelection(skipReferences);
		}
		if (fSkipImplicitReferences != null) {
			boolean skipImplicitReferences = TRUE
					.equals(properties.get(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES));
			fSkipImplicitReferences.setSelection(skipImplicitReferences);
		}
		if (fSkipMacroAndTypeReferences != null) {
			boolean skipTypeReferences = TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES));
			boolean skipMacroReferences = TRUE.equals(properties.get(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES));
			fSkipMacroAndTypeReferences.setSelection(skipTypeReferences && skipMacroReferences);
		}
		if (fIndexAllHeaderVersions != null) {
			boolean indexAllHeaderVersions = TRUE
					.equals(properties.get((IndexerPreferences.KEY_INDEX_ALL_HEADER_VERSIONS)));
			fIndexAllHeaderVersions.setSelection(indexAllHeaderVersions);
		}
		if (fIndexAllVersionsSpecificHeaders != null) {
			String indexAllVersionsSpecificHeaders = properties
					.getProperty((IndexerPreferences.KEY_INDEX_ALL_VERSIONS_SPECIFIC_HEADERS), ""); //$NON-NLS-1$
			fIndexAllVersionsSpecificHeaders.setText(indexAllVersionsSpecificHeaders);
		}
		updateEnablement();
	}

	/**
	 * Return the properties according to the selections on the page.
	 * @since 4.0
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		if (fAllSources != null) {
			props.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(fAllSources.getSelection()));
		}
		if (fAllHeadersDefault != null) {
			props.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG,
					String.valueOf(fAllHeadersDefault.getSelection()));
		}
		if (fAllHeadersAlt != null) {
			props.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG,
					String.valueOf(fAllHeadersAlt.getSelection()));
		}
		if (fIndexOnOpen != null) {
			props.put(IndexerPreferences.KEY_INDEX_ON_OPEN, String.valueOf(fIndexOnOpen.getSelection()));
		}
		if (fIncludeHeuristics != null) {
			props.put(IndexerPreferences.KEY_INCLUDE_HEURISTICS, String.valueOf(fIncludeHeuristics.getSelection()));
		}
		if (fFileSizeLimit != null) {
			props.put(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB, String.valueOf(fFileSizeLimit.getIntValue()));
		}
		if (fIncludedFileSizeLimit != null) {
			props.put(IndexerPreferences.KEY_SKIP_INCLUDED_FILES_LARGER_THAN_MB,
					String.valueOf(fIncludedFileSizeLimit.getIntValue()));
		}
		if (fSkipReferences != null) {
			props.put(IndexerPreferences.KEY_SKIP_ALL_REFERENCES, String.valueOf(fSkipReferences.getSelection()));
		}
		if (fSkipImplicitReferences != null) {
			props.put(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES,
					String.valueOf(fSkipImplicitReferences.getSelection()));
		}
		if (fSkipMacroAndTypeReferences != null) {
			final String value = String.valueOf(fSkipMacroAndTypeReferences.getSelection());
			props.put(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES, value);
			props.put(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES, value);
		}
		if (fIndexAllHeaderVersions != null) {
			props.put((IndexerPreferences.KEY_INDEX_ALL_HEADER_VERSIONS),
					String.valueOf(fIndexAllHeaderVersions.getSelection()));
		}
		if (fIndexAllVersionsSpecificHeaders != null) {
			String[] headers = fIndexAllVersionsSpecificHeaders.getText().split(","); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			for (String header : headers) {
				header = header.trim();
				if (header.isEmpty()) {
					continue;
				}
				if (sb.length() > 0) {
					sb.append(","); //$NON-NLS-1$
				}
				sb.append(header);
			}

			props.put((IndexerPreferences.KEY_INDEX_ALL_VERSIONS_SPECIFIC_HEADERS), sb.toString());
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

		if (fIndexAllHeaderVersions != null) {
			fIndexAllVersionsSpecificHeaders.setEnabled(!fIndexAllHeaderVersions.getSelection());
		}
	}

	private void updateValidState() {
		if (!fFileSizeLimit.isValid()) {
			setErrorMessage(fFileSizeLimit.getErrorMessage());
			setValid(false);
		} else if (!fIncludedFileSizeLimit.isValid()) {
			setErrorMessage(fIncludedFileSizeLimit.getErrorMessage());
			setValid(false);
		} else {
			setValid(true);
		}
		final ICOptionContainer container = getContainer();
		if (container != null) {
			container.updateContainer();
		}
	}

	private Button createAllFilesButton(Composite page) {
		Button result = ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllFiles);
		((GridData) result.getLayoutData()).horizontalSpan = 3;
		return result;
	}

	private Button createAllHeadersButton(Composite page) {
		Button result = ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeaders);
		((GridData) result.getLayoutData()).horizontalSpan = 3;
		return result;
	}

	private Button createAllCHeadersButton(Composite page) {
		Button result = ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeadersC);
		((GridData) result.getLayoutData()).horizontalSpan = 3;
		return result;
	}

	private Button createAllCppHeadersButton(Composite page) {
		Button result = ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexAllHeadersCpp);
		((GridData) result.getLayoutData()).horizontalSpan = 3;
		return result;
	}

	private Button createIndexOnOpenButton(Composite page) {
		Button result = ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_indexOpenedFiles);
		((GridData) result.getLayoutData()).horizontalSpan = 3;
		return result;
	}

	private Button createIncludeHeuristicsButton(Composite page) {
		Button result = ControlFactory.createCheckBox(page, DialogsMessages.AbstractIndexerPage_heuristicIncludes);
		((GridData) result.getLayoutData()).horizontalSpan = 3;
		return result;
	}

	private IntegerFieldEditor createFileSizeLimit(Composite group, String key, String label) {
		IntegerFieldEditor result = new IntegerFieldEditor(key, label, group, 5);
		result.setValidRange(1, 100000);
		ControlFactory.createLabel(group, DialogsMessages.Megabyte);
		Text control = result.getTextControl(group);
		LayoutUtil.setWidthHint(control, pixelConverter.convertWidthInCharsToPixels(10));
		LayoutUtil.setHorizontalGrabbing(control, false);

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
	 * @deprecated parsing files up-front is no longer necessary.
	 */
	@Deprecated
	protected void setSupportForFilesParsedUpFront(boolean enable) {
	}
}
