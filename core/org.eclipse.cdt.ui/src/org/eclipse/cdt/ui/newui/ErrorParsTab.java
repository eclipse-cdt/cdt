/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Andrew Gvozdev (Quoin Inc.) - Regular expression error parsers
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParserNamed;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.IInputStatusValidator;
import org.eclipse.cdt.ui.dialogs.InputStatusDialog;
import org.eclipse.cdt.ui.dialogs.RegexErrorParserOptionPage;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.newui.Messages;


/**
 * This class represents Error Parser Tab in Project Properties or workspace Preferences
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ErrorParsTab extends AbstractCPropertyTab {
	private static final int DEFAULT_HEIGHT = 130;
	private static final int BUTTON_ADD = 0;
	private static final int BUTTON_EDIT = 1;
	private static final int BUTTON_DELETE = 2;
	// there is a separator instead of button = 3
	private static final int BUTTON_MOVEUP = 4;
	private static final int BUTTON_MOVEDOWN = 5;

	private static final String[] BUTTONS = new String[] {
		ADD_STR,
		EDIT_STR,
		DEL_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR,
	};

	private static final String OOPS = "OOPS"; //$NON-NLS-1$

	private Table fTable;
	private CheckboxTableViewer fTableViewer;
	private ICConfigurationDescription fCfgDesc;

	private final Map<String, IErrorParserNamed> fAvailableErrorParsers = new LinkedHashMap<String, IErrorParserNamed>();
	private final Map<String, ICOptionPage> fOptionsPageMap = new HashMap<String, ICOptionPage>();
	private ICOptionPage fCurrentOptionsPage = null;

	private Composite fCompositeForOptionsPage;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {

		super.createControls(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(usercomp, ICHelpContextIds.ERROR_PARSERS_PAGE);

		usercomp.setLayout(new GridLayout(1, false));

		// SashForm
		SashForm sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setBackground(sashForm.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		sashForm.setOrientation(SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		// table
		Composite compositeSashForm = new Composite(sashForm, SWT.NONE);
		compositeSashForm.setLayout(new GridLayout(2, false));
		fTable = new Table(compositeSashForm, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectedOptionPage();
				updateButtons();
		}});
		fTableViewer = new CheckboxTableViewer(fTable);
		fTableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		fTableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					String id = (String)element;
					IErrorParserNamed errorParser = fAvailableErrorParsers.get(id);
					if (errorParser!=null) {
						String name = errorParser.getName();
						if (name!=null && name.length()>0) {
							return name;
						}
					}
					return NLS.bind(Messages.ErrorParsTab_error_NonAccessibleID, id); 
				}
				return OOPS;
			}
		});

		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveChecked();
			}});

		// Buttons
		Composite compositeButtons = new Composite(compositeSashForm, SWT.NONE);
		compositeButtons.setLayoutData(new GridData(GridData.END));
		initButtons(compositeButtons, BUTTONS);

		fCompositeForOptionsPage = new Composite(sashForm, SWT.NULL);
		GridData gd = new GridData();
		fCompositeForOptionsPage.setLayout(new TabFolderLayout());

		PixelConverter converter = new PixelConverter(parent);
		gd.heightHint = converter.convertHorizontalDLUsToPixels(DEFAULT_HEIGHT);

		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		fCompositeForOptionsPage.setLayoutData(gd);

		sashForm.setWeights(new int[] {50, 50});

		// init data
		ICResourceDescription resDecs = getResDesc();
		fCfgDesc = resDecs!=null ? resDecs.getConfiguration() : null;
		initMapParsers();
		updateData(getResDesc());
	}

	private void initMapParsers() {
		fAvailableErrorParsers.clear();
		fOptionsPageMap.clear();
		for (String id : ErrorParserManager.getErrorParserAvailableIds()) {
			IErrorParserNamed errorParser = ErrorParserManager.getErrorParserCopy(id);
			fAvailableErrorParsers.put(id, errorParser);
			initializeOptionsPage(id);
		}

		String ids[];
		if (fCfgDesc!=null) {
			ICConfigurationDescription srcCfgDesc = fCfgDesc.getConfiguration();
			if (srcCfgDesc instanceof ICMultiConfigDescription) {
				String[][] ss = ((ICMultiConfigDescription)srcCfgDesc).getErrorParserIDs();
				ids = CDTPrefUtil.getStrListForDisplay(ss);
			} else {
				ids = srcCfgDesc.getBuildSetting().getErrorParserIDs();
			}
			Set<String> setIds = new LinkedHashSet<String>(Arrays.asList(ids));
			setIds.addAll(fAvailableErrorParsers.keySet());
			fTableViewer.setInput(setIds.toArray(new String[0]));
		} else {
			fTableViewer.setInput(fAvailableErrorParsers.keySet().toArray(new String[0]));
			ids = ErrorParserManager.getDefaultErrorParserIds();
		}
		fTableViewer.setCheckedElements(ids);

		displaySelectedOptionPage();
	}

	private void initializeOptionsPage(String id) {
		IErrorParserNamed errorParser = fAvailableErrorParsers.get(id);
		if (errorParser!=null) {
			String name = errorParser.getName();
			if (name!=null && name.length()>0) {
				// RegexErrorParser has an Options page
				if (errorParser instanceof RegexErrorParser) {
					// allow to edit only for Build Settings Preference Page (where cfgd==null)
					RegexErrorParserOptionPage optionsPage = new RegexErrorParserOptionPage((RegexErrorParser) errorParser, isErrorParsersEditable());
					fOptionsPageMap.put(id, optionsPage);
					optionsPage.setContainer(page);
					optionsPage.createControl(fCompositeForOptionsPage);
					optionsPage.setVisible(false);
					fCompositeForOptionsPage.layout(true);
				}
			}
		}
	}

	private void displaySelectedOptionPage() {
		if (fCurrentOptionsPage != null)
			fCurrentOptionsPage.setVisible(false);

		int pos = fTable.getSelectionIndex();
		if (pos<0)
			return;

		String parserId = (String)fTable.getItem(pos).getData();
		ICOptionPage optionsPage = fOptionsPageMap.get(parserId);
		if (optionsPage != null) {
			optionsPage.setVisible(true);
		}
		fCurrentOptionsPage = optionsPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#buttonPressed(int)
	 */
	@Override
	public void buttonPressed (int n) {
		switch (n) {
		case BUTTON_ADD:
			addErrorParser();
			break;
		case BUTTON_EDIT:
			editErrorParser();
			break;
		case BUTTON_DELETE:
			deleteErrorParser();
			break;
		case BUTTON_MOVEUP:
			moveItem(true);
			break;
		case BUTTON_MOVEDOWN:
			moveItem(false);
			break;
		default:
			break;
		}
		updateButtons();
	}

	// Move item up / down
	private void moveItem(boolean up) {
		int n = fTable.getSelectionIndex();
		if (n < 0 || (up && n == 0) || (!up && n+1 == fTable.getItemCount()))
			return;

		String id = (String)fTableViewer.getElementAt(n);
		boolean checked = fTableViewer.getChecked(id);
		fTableViewer.remove(id);
		n = up ? n-1 : n+1;
		fTableViewer.insert(id, n);
		fTableViewer.setChecked(id, checked);
		fTable.setSelection(n);

		saveChecked();
	}

	private String makeId(String name) {
		return CUIPlugin.PLUGIN_ID+'.'+name;
	}

	private void addErrorParser() {
		IInputStatusValidator inputValidator = new IInputStatusValidator() {
			public IStatus isValid(String newText) {
				StatusInfo status = new StatusInfo();
				if (newText.trim().length() == 0) {
					status.setError(Messages.ErrorParsTab_error_NonEmptyName); 
				} else if (newText.indexOf(ErrorParserManager.ERROR_PARSER_DELIMITER)>=0) {
					String message = MessageFormat.format( Messages.ErrorParsTab_error_IllegalCharacter, 
							new Object[] { ErrorParserManager.ERROR_PARSER_DELIMITER });
					status.setError(message);
				} else if (fAvailableErrorParsers.containsKey(makeId(newText))) {
					status.setError(Messages.ErrorParsTab_error_NonUniqueID); 
				}
				return status;
			}

		};
		InputStatusDialog addDialog = new InputStatusDialog(usercomp.getShell(),
				Messages.ErrorParsTab_title_Add, 
				Messages.ErrorParsTab_label_EnterName, 
				Messages.ErrorParsTab_label_DefaultRegexErrorParserName, 
				inputValidator);
		addDialog.setHelpAvailable(false);

		if (addDialog.open() == Window.OK) {
			String newName = addDialog.getValue();
			String newId = makeId(newName);
			IErrorParserNamed errorParser = new RegexErrorParser(newId, newName);
			fAvailableErrorParsers.put(newId, errorParser);

			fTableViewer.add(newId);
			fTableViewer.setChecked(newId, true);
			fTable.setSelection(fTable.getItemCount()-1);

			initializeOptionsPage(newId);
			displaySelectedOptionPage();
			updateButtons();
		}
	}

	private void editErrorParser() {
		int n = fTable.getSelectionIndex();
		Assert.isTrue(n>=0);

		String id = (String)fTableViewer.getElementAt(n);
		IErrorParserNamed errorParser = fAvailableErrorParsers.get(id);

		IInputStatusValidator inputValidator = new IInputStatusValidator() {
			public IStatus isValid(String newText) {
				StatusInfo status = new StatusInfo();
				if (newText.trim().length() == 0) {
					status.setError(Messages.ErrorParsTab_error_NonEmptyName); 
				} else if (newText.indexOf(ErrorParserManager.ERROR_PARSER_DELIMITER)>=0) {
					String message = MessageFormat.format( Messages.ErrorParsTab_error_IllegalCharacter, 
							new Object[] { ErrorParserManager.ERROR_PARSER_DELIMITER });
					status.setError(message);
				}
				return status;
			}

		};
		InputStatusDialog addDialog = new InputStatusDialog(usercomp.getShell(),
				Messages.ErrorParsTab_title_Edit, 
				Messages.ErrorParsTab_label_EnterName, 
				errorParser.getName(),
				inputValidator);
		addDialog.setHelpAvailable(false);

		if (addDialog.open() == Window.OK) {
			errorParser.setName(addDialog.getValue());
			fTableViewer.refresh(id);
		}
	}

	private void deleteErrorParser() {
		int n = fTable.getSelectionIndex();
		if (n < 0)
			return;

		fTableViewer.remove(fTableViewer.getElementAt(n));

		int last = fTable.getItemCount() - 1;
		if (n>last)
			n = last;
		if (n>=0)
			fTable.setSelection(n);

		saveChecked();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateData(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	public void updateData(ICResourceDescription resDecs) {
		ICConfigurationDescription oldCfgDesc = fCfgDesc;
		fCfgDesc = resDecs!=null ? resDecs.getConfiguration() : null;
		if (oldCfgDesc!=fCfgDesc) {
			initMapParsers();
		}
		displaySelectedOptionPage();
		updateButtons();
	}

	private static boolean isExtensionId(String id) {
		for (String extId : ErrorParserManager.getErrorParserExtensionIds()) {
			if (extId.equals(id)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateButtons()
	 */
	@Override
	public void updateButtons() {
		int pos = fTable.getSelectionIndex();
		int count = fTable.getItemCount();
		int last = count - 1;
		boolean selected = pos >= 0 && pos <= last;
		String id = (String)fTableViewer.getElementAt(pos);

		buttonSetEnabled(BUTTON_ADD, isErrorParsersEditable());
		buttonSetEnabled(BUTTON_EDIT, isErrorParsersEditable() && selected);
		buttonSetEnabled(BUTTON_DELETE, isErrorParsersEditable() && selected && !isExtensionId(id));
		buttonSetEnabled(BUTTON_MOVEUP, selected && pos != 0);
		buttonSetEnabled(BUTTON_MOVEDOWN, selected && pos != last);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription, org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();

		if (!page.isForPrefs()) {
			ICConfigurationDescription sd = src.getConfiguration();
			ICConfigurationDescription dd = dst.getConfiguration();
			String[] s = null;
			if (sd instanceof ICMultiConfigDescription) {
				String[][] ss = ((ICMultiConfigDescription)sd).getErrorParserIDs();
				s = CDTPrefUtil.getStrListForDisplay(ss);
			} else {
				s = sd.getBuildSetting().getErrorParserIDs();
			}
			if (dd instanceof ICMultiConfigDescription)
				((ICMultiConfigDescription)dd).setErrorParserIDs(s);
			else
				dd.getBuildSetting().setErrorParserIDs(s);
			initMapParsers();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performOK()
	 */
	@Override
	protected void performOK() {
		informPages(true);

		if (page.isForPrefs()) {
			if (fCfgDesc==null) {
				// Build Settings page
				try {
					IErrorParserNamed[] errorParsers = new IErrorParserNamed[fTable.getItemCount()];
					int i=0;
					for (TableItem item : fTable.getItems()) {
						if (item.getData() instanceof String) {
							String id = (String) item.getData();
							errorParsers[i] = fAvailableErrorParsers.get(id);
							i++;
						}
					}
	
					Object[] checkedElements = fTableViewer.getCheckedElements();
					String[] checkedErrorParserIds = new String[checkedElements.length];
					System.arraycopy(checkedElements, 0, checkedErrorParserIds, 0, checkedElements.length);
	
					ErrorParserManager.setUserDefinedErrorParsers(errorParsers);
					ErrorParserManager.setDefaultErrorParserIds(checkedErrorParserIds);
				} catch (BackingStoreException e) {
					CUIPlugin.log(Messages.ErrorParsTab_error_OnApplyingSettings, e); 
				} catch (CoreException e) {
					CUIPlugin.log(Messages.ErrorParsTab_error_OnApplyingSettings, e); 
				}
			}
			initMapParsers();
		}
	}

	private void saveChecked() {
		if (fCfgDesc!=null) {
			Object[] objs = fTableViewer.getCheckedElements();
			String[] ids = new String[objs.length];
			System.arraycopy(objs, 0, ids, 0, objs.length);

			if (fCfgDesc instanceof ICMultiConfigDescription)
				((ICMultiConfigDescription)fCfgDesc).setErrorParserIDs(ids);
			else
				fCfgDesc.getBuildSetting().setErrorParserIDs(ids);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#canBeVisible()
	 */
	@Override
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}

	/**
	 * @return {@code true} if the error parsers are allowed to be editable,
	 *     i.e. Add/Edit/Delete buttons are enabled and Options page edits enabled.
	 *     This will evaluate to {@code true} for Preference Build Settings page but
	 *     not for Preference New CDT Project Wizard/Makefile Project.
	 */
	private boolean isErrorParsersEditable() {
		return fCfgDesc==null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (isErrorParsersEditable()) {
			// Must be Build Settings Preference Page
			if (MessageDialog.openQuestion(usercomp.getShell(),
					Messages.ErrorParsTab_title_ConfirmReset, 
					Messages.ErrorParsTab_message_ConfirmReset)) { 

				try {
					ErrorParserManager.setUserDefinedErrorParsers(null);
					ErrorParserManager.setDefaultErrorParserIds(null);
				} catch (BackingStoreException e) {
					CUIPlugin.log(Messages.ErrorParsTab_error_OnRestoring, e); 
				} catch (CoreException e) {
					CUIPlugin.log(Messages.ErrorParsTab_error_OnRestoring, e); 
				}
			}
		} else {
			if (fCfgDesc instanceof ICMultiConfigDescription)
				((ICMultiConfigDescription) fCfgDesc).setErrorParserIDs(null);
			else
				fCfgDesc.getBuildSetting().setErrorParserIDs(null);
		}
		initMapParsers();
		updateButtons();
	}

	private void informPages(boolean apply) {
		Collection<ICOptionPage> pages = fOptionsPageMap.values();
		for (ICOptionPage dynamicPage : pages) {
			if (dynamicPage!=null && dynamicPage.isValid() && dynamicPage.getControl() != null) {
				try {
					if (apply)
						dynamicPage.performApply(new NullProgressMonitor());
					else
						dynamicPage.performDefaults();
				} catch (CoreException e) {
					CUIPlugin.log(Messages.ErrorParsTab_error_OnApplyingSettings, e); 
				}
			}
		}
	}
}
