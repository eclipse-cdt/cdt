/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.DOMAST;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Find/Replace dialog. The dialog is opened on a particular 
 * target but can be re-targeted. Internally used by the <code>FindReplaceAction</code>
 */
class FindIASTNameDialog extends Dialog {

	private static final String REGULAR_EXPRESSIONS_LABEL = "Regular expressions"; //$NON-NLS-1$
	private static final String WHOLE_WORD_LABEL = "Whole Word"; //$NON-NLS-1$
	private static final String CASE_SENSITIVE_LABEL = "Case Sensitive"; //$NON-NLS-1$
	private static final String OPTIONS_LABEL = "Options"; //$NON-NLS-1$
	private static final String BLANK_STRING = ""; //$NON-NLS-1$
	private static final String NAME_NOT_FOUND = "Name not found."; //$NON-NLS-1$
	private static final String FIND_NEXT_LABEL = "Find Next"; //$NON-NLS-1$
	private static final String FIND_IASTNAME_LABEL = "Find IASTName:"; //$NON-NLS-1$
	/**
	 * Updates the find replace dialog on activation changes.
	 */
	class ActivationListener extends ShellAdapter {
		/*
		 * @see ShellListener#shellActivated(ShellEvent)
		 */
		public void shellActivated(ShellEvent e) {
			
			String oldText= fFindField.getText(); // XXX workaround for 10766
			List oldList= new ArrayList();
			oldList.addAll(fFindHistory);

			readConfiguration();
			

			fFindField.removeModifyListener(fFindModifyListener);

			updateCombo(fFindField, fFindHistory);
			if (!fFindHistory.equals(oldList) && !fFindHistory.isEmpty())
				fFindField.setText((String) fFindHistory.get(0));
			else 
				fFindField.setText(oldText);
			if (findFieldHadFocus())
				fFindField.setSelection(new Point(0, fFindField.getText().length()));
			fFindField.addModifyListener(fFindModifyListener);

			fActiveShell= (Shell)e.widget;
			updateButtonState();
			
			if (findFieldHadFocus() && getShell() == fActiveShell && !fFindField.isDisposed())
				fFindField.setFocus();
		}
		
		/**
		 * Returns <code>true</code> if the find field had focus,
		 * <code>false</code> if it did not.
		 * 
		 * @return <code>true</code> if the find field had focus,
		 *         <code>false</code> if it did not
		 */
		private boolean findFieldHadFocus() {
			/*
			 * See bug 45447. Under GTK and Motif, the focus of the find field
			 * is already gone when shellDeactivated is called. On the other
			 * hand focus has already been restored when shellActivated is
			 * called.
			 * 
			 * Therefore, we select and give focus if either
			 * fGiveFocusToFindField is true or the find field has focus.
			 */
			return fGiveFocusToFindField || okToUse(fFindField) && fFindField.isFocusControl();
		}
		
		/*
		 * @see ShellListener#shellDeactivated(ShellEvent)
		 */
		public void shellDeactivated(ShellEvent e) {
			fGiveFocusToFindField= fFindField.isFocusControl();

			storeSettings();

//			fGlobalRadioButton.setSelection(true);
//			fSelectedRangeRadioButton.setSelection(false);
			fUseSelectedLines= false;

			if (fTarget != null && (fTarget instanceof IFindReplaceTargetExtension))
				((IFindReplaceTargetExtension) fTarget).setScope(null);
			
			fOldScope= null;

			fActiveShell= null;			
			updateButtonState();
		}
	}

	/**
	 * Modify listener to update the search result in case of incremental search.
	 * @since 2.0
	 */
	private class FindModifyListener implements ModifyListener {
		
		/*
		 * @see ModifyListener#modifyText(ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			searchTextChanged = true;
			
			if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked()) {
				if (fFindField.getText().equals(BLANK_STRING) && fTarget != null) { //$NON-NLS-1$
					// empty selection at base location
					int offset= fIncrementalBaseLocation.x;

					if (isForwardSearch() && !fNeedsInitialFindBeforeReplace || !isForwardSearch() && fNeedsInitialFindBeforeReplace)
						offset= offset + fIncrementalBaseLocation.y;

					fNeedsInitialFindBeforeReplace= false;
					findAndSelect(offset, BLANK_STRING, isForwardSearch(), isCaseSensitiveSearch(), isWholeWordSearch(), isRegExSearchAvailableAndChecked()); //$NON-NLS-1$
				} else {
					performSearch(false);
				}
			}
			
			updateButtonState();
		}
	}

	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE= 5;

	protected boolean searchTextChanged=false; 
	private Point fLocation;
	private Point fIncrementalBaseLocation;
	private boolean fWrapInit, fCaseInit, fWholeWordInit, fForwardInit, fGlobalInit, fIncrementalInit;
	/**
	 * Tells whether an initial find operation is needed
	 * before the replace operation.
	 * @since 3.0 
	 */
	private boolean fNeedsInitialFindBeforeReplace;
	/**
	 * Initial value for telling whether the search string is a regular expression.
	 * @since 3.0
	 */
	boolean fIsRegExInit;

	private List fFindHistory;
	private IRegion fOldScope;

	private IFindReplaceTarget fTarget;
	private Shell fParentShell;
	private Shell fActiveShell;

	private final ActivationListener fActivationListener= new ActivationListener();
	private final ModifyListener fFindModifyListener= new FindModifyListener();

	private Label fStatusLabel;
	private Button fForwardRadioButton, fGlobalRadioButton, fSelectedRangeRadioButton;
	private Button fCaseCheckBox, fWrapCheckBox, fWholeWordCheckBox, fIncrementalCheckBox;

	/**
	 * Checkbox for selecting whether the search string is a regular expression.
	 * @since 3.0
	 */
	private Button fIsRegExCheckBox;
	
	private Button fFindNextButton;
	Combo fFindField, fReplaceField;
	private Rectangle fDialogPositionInit;

	private IDialogSettings fDialogSettings;
	/**
	 * Tells whether the target supports regular expressions.
	 * <code>true</code> if the target supports regular expressions
	 * @since 3.0
	 */
	private boolean fIsTargetSupportingRegEx;
	/**
	 * Tells whether fUseSelectedLines radio is checked.
	 * @since 3.0
	 */
	private boolean fUseSelectedLines;
	/**
	 * <code>true</code> if the find field should receive focus the next time
	 * the dialog is activated, <code>false</code> otherwise.
	 * @since 3.0
	 */
	private boolean fGiveFocusToFindField= true;


	
	/**
	 * Creates a new dialog with the given shell as parent.
	 * @param parentShell the parent shell
	 */
	public FindIASTNameDialog(Shell parentShell, IFindReplaceTarget target) {
		super(parentShell);
		
		fParentShell= null;
		updateTarget(target, false);

		fDialogPositionInit= null;
		fFindHistory= new ArrayList(HISTORY_SIZE - 1);

		fWrapInit= false;
		fCaseInit= false;
		fIsRegExInit= false;
		fWholeWordInit= false;
		fIncrementalInit= false;
		fGlobalInit= true;
		fForwardInit= true;

		readConfiguration();
		
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	/**
	 * Returns this dialog's parent shell.
	 * @return the dialog's parent shell
	 */
	public Shell getParentShell() {
		return super.getParentShell();
	}
	
	
	/**
	 * Returns <code>true</code> if control can be used.
	 *
	 * @param control the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}
	
	/*
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		
		super.create();
		
		Shell shell= getShell();
		shell.addShellListener(fActivationListener);
		if (fLocation != null)
			shell.setLocation(fLocation);
		
		// set help context
//		WorkbenchHelp.setHelp(shell, IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG);

		// fill in combo contents
		fFindField.removeModifyListener(fFindModifyListener);
		updateCombo(fFindField, fFindHistory);
		fFindField.addModifyListener(fFindModifyListener);
//		updateCombo(fReplaceField, fReplaceHistory);

		// get find string
		initFindStringFromSelection();
		
		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);
		
		shell.setText(FIND_IASTNAME_LABEL);
		// shell.setImage(null);
	}

	/**
	 * Create the button section of the find/replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the button section
	 */
	private Composite createButtonSection(Composite parent) {
		
		Composite panel= new Composite(parent, SWT.NULL);		
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		panel.setLayout(layout);
		
		fFindNextButton= makeButton(panel, FIND_NEXT_LABEL, 102, true, new SelectionAdapter() { //$NON-NLS-1$
			public void widgetSelected(SelectionEvent e) {
				if (fTarget instanceof FindIASTNameTarget && searchTextChanged) {
					((FindIASTNameTarget)fTarget).clearMatchingNames();
					searchTextChanged = false;
				}
				
				if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
					initIncrementalBaseLocation();

				fNeedsInitialFindBeforeReplace= false;
				performSearch();
				updateFindHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fFindNextButton, GridData.FILL, true, GridData.FILL, false);
		
		return panel;
	}
	
	/**
	 * Creates the options configuration section of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options configuration section
	 */
	private Composite createConfigPanel(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		panel.setLayout(layout);

		Composite directionGroup= createDirectionGroup(panel);
		setGridData(directionGroup, GridData.FILL, true, GridData.FILL, false);

		Composite optionsGroup= createOptionsGroup(panel);
		setGridData(optionsGroup, GridData.FILL, true, GridData.FILL, false);
		GridData data= (GridData) optionsGroup.getLayoutData();
		data.horizontalSpan= 2;
		optionsGroup.setLayoutData(data);

		return panel;
	}
	
	/*
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);

		Composite inputPanel= createInputPanel(panel);
		setGridData(inputPanel, GridData.FILL, true, GridData.CENTER, false);

		Composite configPanel= createConfigPanel(panel);
		setGridData(configPanel, GridData.FILL, true, GridData.CENTER, true);
		
		Composite buttonPanelB= createButtonSection(panel);
		setGridData(buttonPanelB, GridData.FILL, true, GridData.CENTER, false);
		
		Composite statusBar= createStatusAndCloseButton(panel);
		setGridData(statusBar, GridData.FILL, true, GridData.CENTER, false);
		
		updateButtonState();
		
		applyDialogFont(panel);
		
		return panel;
	}
	
	/**
	 * Creates the direction defining part of the options defining section
	 * of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the direction defining part
	 */
	private Composite createDirectionGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText("Direction"); //$NON-NLS-1$
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		SelectionListener selectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
					initIncrementalBaseLocation();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fForwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fForwardRadioButton.setText("Forward"); //$NON-NLS-1$
		setGridData(fForwardRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);
		fForwardRadioButton.addSelectionListener(selectionListener);

		Button backwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		backwardRadioButton.setText("Backward"); //$NON-NLS-1$
		setGridData(backwardRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);
		backwardRadioButton.addSelectionListener(selectionListener);

		backwardRadioButton.setSelection(!fForwardInit);
		fForwardRadioButton.setSelection(fForwardInit);

		return panel;
	}

	/**
	 * Tells the dialog to perform searches only in the scope given by the actually selected lines.
	 * @param selectedLines <code>true</code> if selected lines should be used
	 * @since 2.0
	 */
	private void useSelectedLines(boolean selectedLines) {
		if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
			initIncrementalBaseLocation();

		if (fTarget == null || !(fTarget instanceof IFindReplaceTargetExtension))
			return;

		IFindReplaceTargetExtension extensionTarget= (IFindReplaceTargetExtension) fTarget;

		if (selectedLines) {

			IRegion scope;
			if (fOldScope == null) {
				Point lineSelection= extensionTarget.getLineSelection();
				scope= new Region(lineSelection.x, lineSelection.y);
			} else {
				scope= fOldScope;
				fOldScope= null;
			}

			int offset= isForwardSearch()
				? scope.getOffset() 
				: scope.getOffset() + scope.getLength();					

			extensionTarget.setSelection(offset, 0);					
			extensionTarget.setScope(scope);
		} else {
			fOldScope= extensionTarget.getScope();
			extensionTarget.setScope(null);			
		}
	}

	/**
	 * Creates the panel where the user specifies the text to search
	 * for and the optional replacement text.
	 *
	 * @param parent the parent composite
	 * @return the input panel
	 */
	private Composite createInputPanel(Composite parent) {
		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		panel.setLayout(layout);

		Label findLabel= new Label(panel, SWT.LEFT);
		findLabel.setText("Find Name:"); //$NON-NLS-1$
		setGridData(findLabel, GridData.BEGINNING, false, GridData.CENTER, false);

		fFindField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(fFindField, GridData.FILL, true, GridData.CENTER, false);
		fFindField.addModifyListener(fFindModifyListener);

		return panel;
	}

	/**
	 * Creates the functional options part of the options defining
	 * section of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options group
	 */
	private Composite createOptionsGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_NONE);
		group.setText(OPTIONS_LABEL); //$NON-NLS-1$
		GridLayout groupLayout= new GridLayout();
		groupLayout.numColumns= 2;
		groupLayout.makeColumnsEqualWidth= true;		
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		SelectionListener selectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				storeSettings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fCaseCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fCaseCheckBox.setText(CASE_SENSITIVE_LABEL); //$NON-NLS-1$
		setGridData(fCaseCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fCaseCheckBox.setSelection(fCaseInit);
		fCaseCheckBox.addSelectionListener(selectionListener);

		fWholeWordCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWholeWordCheckBox.setText(WHOLE_WORD_LABEL); //$NON-NLS-1$
		setGridData(fWholeWordCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fWholeWordCheckBox.setSelection(fWholeWordInit);
		fWholeWordCheckBox.addSelectionListener(selectionListener);

		fIsRegExCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fIsRegExCheckBox.setText(REGULAR_EXPRESSIONS_LABEL); //$NON-NLS-1$
		setGridData(fIsRegExCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		((GridData)fIsRegExCheckBox.getLayoutData()).horizontalSpan= 2;
		fIsRegExCheckBox.setSelection(fIsRegExInit);
		fIsRegExCheckBox.addSelectionListener(new SelectionAdapter() {
			/*
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				boolean newState= fIsRegExCheckBox.getSelection();
				if (fTarget instanceof FindIASTNameTarget)
					((FindIASTNameTarget)fTarget).clearMatchingNames();
				
				updateButtonState();
				storeSettings();
			}
		});
		fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
		fWholeWordCheckBox.addSelectionListener(new SelectionAdapter() {
			/*
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				if (fTarget instanceof FindIASTNameTarget)
					((FindIASTNameTarget)fTarget).clearMatchingNames();
				
				updateButtonState();
			}
		});
		return panel;
	}
	
	/**
	 * Creates the status and close section of the dialog.
	 *
	 * @param parent the parent composite
	 * @return the status and close button
	 */
	private Composite createStatusAndCloseButton(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;		
		panel.setLayout(layout);

		fStatusLabel= new Label(panel, SWT.LEFT);
		setGridData(fStatusLabel, GridData.FILL, true, GridData.CENTER, false);

		String label= "Close"; //$NON-NLS-1$
		Button closeButton= createButton(panel, 101, label, false);
		setGridData(closeButton, GridData.END, false, GridData.END, false);

		return panel;
	}

	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
	}
	
	// ------- action invocation ---------------------------------------

	/**
	 * Returns the position of the specified search string, or <code>-1</code> if the string can
	 * not be found when searching using the given options.
	 * 
	 * @param findString the string to search for
	 * @param startPosition the position at which to start the search
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive	should the search be case sensitive
	 * @param wrapSearch	should the search wrap to the start/end if arrived at the end/start
	 * @param wholeWord does the search string represent a complete word
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the occurrence of the find string following the options or <code>-1</code> if nothing found
	 * @since 3.0
	 */
	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch) {

		if (forwardSearch) {
			if (wrapSearch) {
				int index= findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch);
				if (index == -1) {
					if (okToUse(getShell()) && !isIncrementalSearch())
						getShell().getDisplay().beep();
					index= findAndSelect(-1, findString, true, caseSensitive, wholeWord, regExSearch);
				}
				return index;
			}
			return findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch);
		}

		// backward
		if (wrapSearch) {
			int index= findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord, regExSearch);
			if (index == -1) {
				if (okToUse(getShell()) && !isIncrementalSearch())
					getShell().getDisplay().beep();
				index= findAndSelect(-1, findString, false, caseSensitive, wholeWord, regExSearch);
			}
			return index;
		}
		return findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord, regExSearch);
	}
	
	/**
	 * Searches for a string starting at the given offset and using the specified search
	 * directives. If a string has been found it is selected and its start offset is 
	 * returned.
	 *
	 * @param offset the offset at which searching starts
	 * @param findString the string which should be found
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
	 * @param wholeWord if <code>true</code> only occurrences are reported in which the findString stands as a word by itself 
	 * @param regExSearch if <code>true</code> findString represents a regular expression 
	 * @return the position of the specified string, or -1 if the string has not been found
	 * @since 3.0
	 */
	private int findAndSelect(int offset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3)fTarget).findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
		return fTarget.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord);
	}

	/**
	 * Replaces the selection with <code>replaceString</code>. If
	 * <code>regExReplace</code> is <code>true</code>,
	 * <code>replaceString</code> is a regex replace pattern which will get
	 * expanded if the underlying target supports it. Returns the region of the
	 * inserted text; note that the returned selection covers the expanded
	 * pattern in case of regex replace.
	 * 
	 * @param replaceString the replace string (or a regex pattern)
	 * @param regExReplace <code>true</code> if <code>replaceString</code>
	 *        is a pattern
	 * @return the selection after replacing, i.e. the inserted text
	 * @since 3.0
	 */
	Point replaceSelection(String replaceString, boolean regExReplace) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			((IFindReplaceTargetExtension3)fTarget).replaceSelection(replaceString, regExReplace);
		else
			fTarget.replaceSelection(replaceString);
		
		return fTarget.getSelection();
	}
	
	/**
	 * Returns whether the specified search string can be found using the given options.
	 * 
	 * @param findString the string to search for
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive	should the search be case sensitive
	 * @param wrapSearch	should the search wrap to the start/end if arrived at the end/start
	 * @param wholeWord does the search string represent a complete word
	 * @param incremental is this an incremental search
	 * @param regExSearch if <code>true</code> findString represents a regular expression 
	 * @return <code>true</code> if the search string can be found using the given options
	 * 
	 * @since 3.0
	 */
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean incremental, boolean regExSearch) {

		if (fTarget == null)
			return false;

		Point r= null;
		if (incremental)
			r= fIncrementalBaseLocation;
		else
			r= fTarget.getSelection();

		int findReplacePosition= r.x;
		if (forwardSearch && !fNeedsInitialFindBeforeReplace || !forwardSearch && fNeedsInitialFindBeforeReplace)
			findReplacePosition += r.y;

		fNeedsInitialFindBeforeReplace= false;

		int index= findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch, wholeWord, regExSearch);

		if (index != -1)
			return true;
		
		return false;
	}

	/**
	 * Returns the dialog's boundaries.
	 * @return the dialog's boundaries
	 */
	private Rectangle getDialogBoundaries() {
		if (okToUse(getShell()))
			return getShell().getBounds();
		return fDialogPositionInit;
	}
	
	/**
	 * Returns the dialog's history.
	 * @return the dialog's history
	 */
	private List getFindHistory() {
		return fFindHistory;
	}

	// ------- accessors ---------------------------------------

	/**
	 * Retrieves the string to search for from the appropriate text input field and returns it. 
	 * @return the search string
	 */
	private String getFindString() {
		if (okToUse(fFindField)) {
			return fFindField.getText();
		}
		return BLANK_STRING; //$NON-NLS-1$
	}
	
	// ------- init / close ---------------------------------------

	/**
	 * Returns the actual selection of the find replace target.
	 * @return the selection of the target
	 */
	private String getSelectionString() {
		String selection= fTarget.getSelectionText();
		if (selection != null && selection.length() > 0) {
			int[] info= TextUtilities.indexOf(TextUtilities.DELIMITERS, selection, 0);
			if (info[0] > 0)
				return selection.substring(0, info[0]);
			else if (info[0] == -1)
				return selection;
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		handleDialogClose();
		return super.close();
	}
	
	/**
	 * Removes focus changed listener from browser and stores settings for re-open.
	 */
	private void handleDialogClose() {

		// remove listeners
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);
		}
		
		if (fParentShell != null) {
			fParentShell.removeShellListener(fActivationListener);
			fParentShell= null;
		}
		
		getShell().removeShellListener(fActivationListener);
		
		// store current settings in case of re-open
		storeSettings();

		if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		// prevent leaks
		fActiveShell= null;
		fTarget= null;
		
	}

	/**
	 * Writes the current selection to the dialog settings.
	 * @since 3.0
	 */
	private void writeSelection() {
		if (fTarget == null)
			return;
		String selection= fTarget.getSelectionText();
		if (selection == null)
			selection= BLANK_STRING; //$NON-NLS-1$

		IDialogSettings s= getDialogSettings();
		s.put("selection", selection); //$NON-NLS-1$
	}
	
	/**
	 * Stores the current state in the dialog settings.
	 * @since 2.0
	 */
	private void storeSettings() {
		fDialogPositionInit= getDialogBoundaries();
		fWrapInit= isWrapSearch();
		fWholeWordInit= isWholeWordSetting();
		fCaseInit= isCaseSensitiveSearch();
		fIsRegExInit= isRegExSearch();
		fIncrementalInit= isIncrementalSearch();
		fForwardInit= isForwardSearch();

		writeConfiguration();		
	}
	
	/**
	 * Initializes the string to search for and the appropriate
	 * text inout field based on the selection found in the
	 * action's target.
	 */
	private void initFindStringFromSelection() {
		if (fTarget != null && okToUse(fFindField)) {
			String selection= getSelectionString();
			fFindField.removeModifyListener(fFindModifyListener);
			if (selection != null) {
				fFindField.setText(selection);
				if (!selection.equals(fTarget.getSelectionText())) {
					useSelectedLines(true);
					fGlobalRadioButton.setSelection(false);
					fSelectedRangeRadioButton.setSelection(true);
					fUseSelectedLines= true;
				}
			} else {
				if (BLANK_STRING.equals(fFindField.getText())) { //$NON-NLS-1$
					if (fFindHistory.size() > 0)
						fFindField.setText((String) fFindHistory.get(0));
					else
						fFindField.setText(BLANK_STRING); //$NON-NLS-1$				
				}
			}
			fFindField.setSelection(new Point(0, fFindField.getText().length()));
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Initializes the anchor used as starting point for incremental searching.
	 * @since 2.0
	 */
	private void initIncrementalBaseLocation() {
		if (fTarget != null && isIncrementalSearch() && !isRegExSearchAvailableAndChecked()) {
			fIncrementalBaseLocation= fTarget.getSelection();
		} else {
			fIncrementalBaseLocation= new Point(0, 0);	
		}
	}

	// ------- history ---------------------------------------
		
	/**
	 * Retrieves and returns the option case sensitivity from the appropriate check box.
	 * @return <code>true</code> if case sensitive
	 */
	private boolean isCaseSensitiveSearch() {
		if (okToUse(fCaseCheckBox)) {
			return fCaseCheckBox.getSelection();
		}
		return fCaseInit;
	}

	/**
	 * Retrieves and returns the regEx option from the appropriate check box.
	 * 
	 * @return <code>true</code> if case sensitive
	 * @since 3.0
	 */
	private boolean isRegExSearch() {
		if (okToUse(fIsRegExCheckBox)) {
			return fIsRegExCheckBox.getSelection();
		}
		return fIsRegExInit;
	}

	/**
	 * If the target supports regular expressions search retrieves and returns
	 * regEx option from appropriate check box.
	 * 
	 * @return <code>true</code> if regEx is available and checked
	 * @since 3.0
	 */
	private boolean isRegExSearchAvailableAndChecked() {
		if (okToUse(fIsRegExCheckBox)) {
			return fIsTargetSupportingRegEx && fIsRegExCheckBox.getSelection();
		}
		return fIsRegExInit;
	}

	/**
	 * Retrieves and returns the option search direction from the appropriate check box.
	 * @return <code>true</code> if searching forward
	 */
	private boolean isForwardSearch() {
		if (okToUse(fForwardRadioButton)) {
			return fForwardRadioButton.getSelection();
		}
		return fForwardInit;
	}

	/**
	 * Retrieves and returns the option search whole words from the appropriate check box.
	 * @return <code>true</code> if searching for whole words
	 */
	private boolean isWholeWordSetting() {
		if (okToUse(fWholeWordCheckBox)) {
			return fWholeWordCheckBox.getSelection();
		}
		return fWholeWordInit;
	}

	/**
	 * Returns <code>true</code> if searching should be restricted to entire
	 * words, <code>false</code> if not. This is the case if the respective
	 * checkbox is turned on, regex is off, and the checkbox is enabled, i.e. 
	 * the current find string is an entire word.
	 * 
	 * @return <code>true</code> if the search is restricted to whole words
	 */
	private boolean isWholeWordSearch() {
		return isWholeWordSetting() && !isRegExSearchAvailableAndChecked() && (okToUse(fWholeWordCheckBox) ? fWholeWordCheckBox.isEnabled() : true);
	}

	/**
	 * Retrieves and returns the option wrap search from the appropriate check box.
	 * @return <code>true</code> if wrapping while searching
	 */
	private boolean isWrapSearch() {
		if (okToUse(fWrapCheckBox)) {
			return fWrapCheckBox.getSelection();
		}
		return fWrapInit;
	}

	/**
	 * Retrieves and returns the option incremental search from the appropriate check box.
	 * @return <code>true</code> if incremental search
	 * @since 2.0
	 */
	private boolean isIncrementalSearch() {
		if (okToUse(fIncrementalCheckBox)) {
			return fIncrementalCheckBox.getSelection();
		}
		return fIncrementalInit;
	}

	/**
	 * Creates a button.
	 * @param parent the parent control
	 * @param key the key to lookup the button label
	 * @param id the button id
	 * @param dfltButton is this button the default button
	 * @param listener a button pressed listener
	 * @return the new button
	 */
	private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
		Button b= createButton(parent, id, label, dfltButton);
		b.addSelectionListener(listener);
		return b;
	}

	/**
	 * Returns the status line manager of the active editor or <code>null</code> if there is no such editor.
	 * @return the status line manager of the active editor
	 */
	private IEditorStatusLine getStatusLineManager() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page= window.getActivePage();
		if (page == null)
			return null;
			
		IEditorPart editor= page.getActiveEditor();
		if (editor == null)
			return null;

		return (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
	}

	/**
	 * Sets the given status message in the status line.
	 * 
	 * @param error <code>true</code> if it is an error
	 * @param message the error message
	 */
	private void statusMessage(boolean error, String message) {
		fStatusLabel.setText(message);

		if (error)
			fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
		else
			fStatusLabel.setForeground(null);

		IEditorStatusLine statusLine= getStatusLineManager();
		if (statusLine != null)
			statusLine.setMessage(error, message, null);	

		if (error)
			getShell().getDisplay().beep();
	}

	/**
	 * Sets the given error message in the status line.
	 * @param message the message
	 */
	private void statusError(String message) {
		statusMessage(true, message);
	}

	/**
	 * Sets the given message in the status line.
	 * @param message the message
	 */
	private void statusMessage(String message) {
		statusMessage(false, message);
	}

	/**
	 * Locates the user's findString in the text of the target.
	 */
	private void performSearch() {
		performSearch(isIncrementalSearch() && !isRegExSearchAvailableAndChecked());
	}
	
	/**
	 * Locates the user's findString in the text of the target.
	 * 
	 * @param mustInitIncrementalBaseLocation <code>true</code> if base location must be initialized
	 * @since 3.0
	 */
	private void performSearch(boolean mustInitIncrementalBaseLocation) {

		if (mustInitIncrementalBaseLocation)
			initIncrementalBaseLocation();
		
		String findString= getFindString();

		if (findString != null && findString.length() > 0) {

			try {
				boolean somethingFound= findNext(findString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch(), isIncrementalSearch() && !isRegExSearchAvailableAndChecked(), isRegExSearchAvailableAndChecked());
				if (somethingFound) {
					statusMessage(BLANK_STRING); //$NON-NLS-1$
				} else {
					statusMessage(NAME_NOT_FOUND); //$NON-NLS-1$
				}
			} catch (PatternSyntaxException ex) {
				statusError(ex.getLocalizedMessage());
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
		writeSelection();
		updateButtonState();
	}
	
	// ------- UI creation ---------------------------------------
	
	/**
	 * Attaches the given layout specification to the <code>component</code>.
	 * 
	 * @param component the component
	 * @param horizontalAlignment horizontal alignment
	 * @param grabExcessHorizontalSpace grab excess horizontal space
	 * @param verticalAlignment vertical alignment
	 * @param grabExcessVerticalSpace grab excess vertical space
	 */
	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd= new GridData();
		gd.horizontalAlignment= horizontalAlignment;
		gd.grabExcessHorizontalSpace= grabExcessHorizontalSpace;
		gd.verticalAlignment= verticalAlignment;
		gd.grabExcessVerticalSpace= grabExcessVerticalSpace;
		component.setLayoutData(gd);
	}

	/** 
	 * Updates the enabled state of the buttons.
	 * 
	 * @since 3.0
	 */
	private void updateButtonState() {
		if (okToUse(getShell()) && okToUse(fFindNextButton)) {
			boolean enable= fTarget != null && (fActiveShell == fParentShell || fActiveShell == getShell());
			String str= getFindString();
			boolean findString= str != null && str.length() > 0;
			
			boolean wholeWord= isWord(str) && !isRegExSearchAvailableAndChecked();
			fWholeWordCheckBox.setEnabled(wholeWord);

			fFindNextButton.setEnabled(enable && findString);
		}
	}
	
	/**
	 * Tests whether each character in the given
	 * string is a letter.
	 * 
	 * @param str
	 * @return <code>true</code> if the given string is a word
	 * @since 3.0
	 */
	private boolean isWord(String str) {
		if (str == null || str.length() == 0)
			return false;
		
		for (int i= 0; i < str.length(); i++) {
			if (!Character.isJavaIdentifierPart(str.charAt(i)))
				return false;
		}
		return true;
	}
	
	/**
	 * Updates the given combo with the given content.
	 * @param combo combo to be updated
	 * @param content to be put into the combo
	 */
	private void updateCombo(Combo combo, List content) {
		combo.removeAll();
		for (int i= 0; i < content.size(); i++) {
			combo.add(content.get(i).toString());
		}
	}

	// ------- open / reopen ---------------------------------------

	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);
			updateHistory(fFindField, fFindHistory);
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Updates the combo with the history.
	 * @param combo to be updated
	 * @param history to be put into the combo
	 */
	private void updateHistory(Combo combo, List history) {
		String findString= combo.getText();
		int index= history.indexOf(findString);
		if (index != 0) {
			if (index != -1) {
				history.remove(index);
			}
			history.add(0, findString);
			updateCombo(combo, history);
			combo.setText(findString);
		}
	}
	
	/**
	 * Updates this dialog because of a different target.
	 * @param target the new target
	 * @param isTargetEditable <code>true</code> if the new target can be modifed
	 * @since 2.0
	 */
	public void updateTarget(IFindReplaceTarget target, boolean isTargetEditable) {
		
		fNeedsInitialFindBeforeReplace= true;
		
		if (target != fTarget) {
			if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) fTarget).endSession();

			fTarget= target;
			if (target != null)
				fIsTargetSupportingRegEx= target instanceof IFindReplaceTargetExtension3;

			if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension) {
				((IFindReplaceTargetExtension) fTarget).beginSession();

				fGlobalInit= true;
				fGlobalRadioButton.setSelection(fGlobalInit);
				fSelectedRangeRadioButton.setSelection(!fGlobalInit);
				fUseSelectedLines= !fGlobalInit;
			}
		}

		if (okToUse(fIsRegExCheckBox))
			fIsRegExCheckBox.setEnabled(fIsTargetSupportingRegEx);
	
		if (okToUse(fWholeWordCheckBox))
			fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
		
		if (okToUse(fIncrementalCheckBox))
			fIncrementalCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());

		// see pr 51073
		fGiveFocusToFindField= true;
	}

	/** 
	 * Sets the parent shell of this dialog to be the given shell.
	 *
	 * @param shell the new parent shell
	 */
	public void setParentShell(Shell shell) {
		if (shell != fParentShell) {
			
			if (fParentShell != null)
				fParentShell.removeShellListener(fActivationListener);
							
			fParentShell= shell;
			fParentShell.addShellListener(fActivationListener);
		}
		
		fActiveShell= shell;
	}
	
	
	//--------------- configuration handling --------------
	
	/**
	 * Returns the dialog settings object used to share state
	 * between several find/replace dialogs.
	 * 
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings= TextEditorPlugin.getDefault().getDialogSettings();
		fDialogSettings= settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}
	
	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();

		try {
			int x= s.getInt("x"); //$NON-NLS-1$
			int y= s.getInt("y"); //$NON-NLS-1$
			fLocation= new Point(x, y);
		} catch (NumberFormatException e) {
			fLocation= null;
		}
			
		fWrapInit= s.getBoolean("wrap"); //$NON-NLS-1$
		fCaseInit= s.getBoolean("casesensitive"); //$NON-NLS-1$
		fWholeWordInit= s.getBoolean("wholeword"); //$NON-NLS-1$
		fIncrementalInit= s.getBoolean("incremental"); //$NON-NLS-1$
		fIsRegExInit= s.getBoolean("isRegEx"); //$NON-NLS-1$
		
		String[] findHistory= s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistory != null) {
			List history= getFindHistory();
			history.clear();
			for (int i= 0; i < findHistory.length; i++)
				history.add(findHistory[i]);
		}		
	}
	
	/**
	 * Stores its current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();

		Point location= getShell().getLocation();
		s.put("x", location.x); //$NON-NLS-1$
		s.put("y", location.y); //$NON-NLS-1$
		
		s.put("wrap", fWrapInit); //$NON-NLS-1$
		s.put("casesensitive", fCaseInit); //$NON-NLS-1$
		s.put("wholeword", fWholeWordInit); //$NON-NLS-1$
		s.put("incremental", fIncrementalInit); //$NON-NLS-1$
		s.put("isRegEx", fIsRegExInit); //$NON-NLS-1$
		List history= getFindHistory();
		while (history.size() > 8)
			history.remove(8);
		String[] names= new String[history.size()];
		history.toArray(names);
		s.put("findhistory", names); //$NON-NLS-1$
	}
	
}
