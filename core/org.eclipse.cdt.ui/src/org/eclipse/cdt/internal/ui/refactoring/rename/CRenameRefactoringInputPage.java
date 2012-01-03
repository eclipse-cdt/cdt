/*******************************************************************************
 * Copyright (c) 2004, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Markus Schorn - initial API and implementation 
 *     Emanuel Graf (Institute for Software, HSR Hochschule fuer Technik)
 *     Sergey Prigogin (Google)
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import org.eclipse.cdt.core.CConventions;

/**
 * Input page added to the standard refactoring wizard.
 */
public class CRenameRefactoringInputPage extends UserInputWizardPage {
	public static final String PAGE_NAME = "RenameRefactoringPage"; //$NON-NLS-1$
    
	final CRenameRefactoringPreferences fPreferences;
	private String fSearchString;
    private int fOptions;
    private int fForcePreviewOptions= 0;
    private int fExhaustiveSearchEnablingOptions;

    private Text fNewName;
	private Button fDoVirtual;
    private Button fWorkspace;
    private Button fDependent;
    private Button fInComment;
    private Button fInString;
    private Button fInInclude;
    private Button fInInactiveCode;
    private Button fReferences;
    private Button fSingle;
    private Button fWorkingSet;
    private Text fWorkingSetSpec;
    private Button fWorkingSetButton;
    private Button fInMacro;
    private Button fInPreprocessor;
    private Button fExhausiveFileSearch;

    public CRenameRefactoringInputPage() {
        super(PAGE_NAME);
        fPreferences = new CRenameRefactoringPreferences();
    }
 
    private boolean hasOption(int options) {
        return (fOptions & options) == options;
    }

    @Override
	public void createControl(Composite parent) {
        CRenameProcessor processor= getRenameProcessor();
        fSearchString= processor.getArgument().getName();
        fOptions= processor.getAvailableOptions();
        fForcePreviewOptions= processor.getOptionsForcingPreview();
        fExhaustiveSearchEnablingOptions= processor.getOptionsEnablingExhaustiveSearch();
        
        Composite top= new Composite(parent, SWT.NONE);
        initializeDialogUnits(top);
        setControl(top);

        top.setLayout(new GridLayout(2, false));

        // New name
        Composite group= top;
        GridData gd;
        GridLayout gl;

        Label label= new Label(top, SWT.NONE);
        label.setText(RenameMessages.CRenameRefactoringInputPage_label_newName);
        fNewName= new Text(top, SWT.BORDER);
        String name = processor.getReplacementText();
        if (name == null) {
        	name = fSearchString;
        }
		fNewName.setText(name);
        fNewName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));        
        fNewName.selectAll();
        
        if (hasOption(CRefactory.OPTION_DO_VIRTUAL)) {
        	fDoVirtual= new Button(top, SWT.CHECK);
        	fDoVirtual.setText(RenameMessages.CRenameRefactoringInputPage_renameBaseAndDerivedMethods);
        	fDoVirtual.setLayoutData(gd= new GridData());
        	gd.horizontalSpan= 2;
        }

        boolean skippedLine= false;
        group= null;
        if (hasOption(CRefactory.OPTION_IN_CODE_REFERENCES)) {
            group= createLabelAndGroup(group, skippedLine, top);
        	fReferences= new Button(group, SWT.CHECK);
        	fReferences.setText(RenameMessages.CRenameRefactoringInputPage_button_sourceCode);
        }
    	if (hasOption(CRefactory.OPTION_IN_INACTIVE_CODE)) {
            group= createLabelAndGroup(group, skippedLine, top);
    	    fInInactiveCode= new Button(group, SWT.CHECK);
    	    fInInactiveCode.setText(RenameMessages.CRenameRefactoringInputPage_button_inactiveCode);
    	}
        if (hasOption(CRefactory.OPTION_IN_COMMENT)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInComment= new Button(group, SWT.CHECK);
            fInComment.setText(RenameMessages.CRenameRefactoringInputPage_button_comments);
        }
        if (hasOption(CRefactory.OPTION_IN_STRING_LITERAL)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInString= new Button(group, SWT.CHECK);
            fInString.setText(RenameMessages.CRenameRefactoringInputPage_button_strings);
        }
        if (hasOption(CRefactory.OPTION_IN_MACRO_DEFINITION)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInMacro= new Button(group, SWT.CHECK);
            fInMacro.setText(RenameMessages.CRenameRefactoringInputPage_button_macroDefinitions);
        }
        if (hasOption(CRefactory.OPTION_IN_INCLUDE_DIRECTIVE)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInInclude= new Button(group, SWT.CHECK);
            fInInclude.setText(RenameMessages.CRenameRefactoringInputPage_button_includes);
        }
        if (hasOption(CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE)) {
            group= createLabelAndGroup(group, skippedLine, top);
            fInPreprocessor= new Button(group, SWT.CHECK);
            fInPreprocessor.setText(RenameMessages.CRenameRefactoringInputPage_button_preprocessor);
        }

        if (hasOption(CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH)) {
    		skipLine(top);
            fExhausiveFileSearch= new Button(top, SWT.CHECK);
            fExhausiveFileSearch.setText(RenameMessages.CRenameRefactoringInputPage_button_exhaustiveFileSearch);
            fExhausiveFileSearch.setLayoutData(gd= new GridData());
    		gd.horizontalIndent= 5;
        	gd.horizontalSpan= 2;

        	// Specify the scope.
            group= new Composite(top, SWT.NONE);
            group.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
            gd.horizontalSpan= 2;
            gd.horizontalIndent= 16;
            group.setLayout(gl= new GridLayout(4, false));
            gl.marginHeight= 0;
            gl.marginLeft = gl.marginWidth;
            gl.marginWidth = 0;

            fWorkspace= new Button(group, SWT.RADIO);
            fWorkspace.setText(RenameMessages.CRenameRefactoringInputPage_button_workspace);
            fWorkspace.setLayoutData(gd= new GridData());
            
            fDependent= new Button(group, SWT.RADIO);
            fDependent.setText(RenameMessages.CRenameRefactoringInputPage_button_relatedProjects);
            fDependent.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    		gd.horizontalIndent= 8;

            fSingle= new Button(group, SWT.RADIO);
            fSingle.setText(RenameMessages.CRenameRefactoringInputPage_button_singleProject);
            fSingle.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    		gd.horizontalIndent= 8;
    		gd.horizontalSpan= 2;

            fWorkingSet= new Button(group, SWT.RADIO);
            fWorkingSet.setText(RenameMessages.CRenameRefactoringInputPage_button_workingSet);

            fWorkingSetSpec= new Text(group, SWT.SINGLE|SWT.BORDER|SWT.READ_ONLY);
            fWorkingSetSpec.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
    		gd.horizontalIndent= 8;
    		gd.horizontalSpan= 2;
            fWorkingSetButton= new Button(group, SWT.PUSH);
            fWorkingSetButton.setText(RenameMessages.CRenameRefactoringInputPage_button_chooseWorkingSet);
            setButtonLayoutData(fWorkingSetButton);
        }

        Dialog.applyDialogFont(top);
        hookSelectionListeners();
        readPreferences();
        onSelectOption();	// transfers the option to the refactoring / enablement
        updatePageComplete();
    }

    private Composite createLabelAndGroup(Composite group, boolean skippedLine, Composite top) {
        if (group != null) {
            return group;
        }
        if (!skippedLine) {
            skipLine(top);
        }
        GridData gd;
        Label label = new Label(top, SWT.NONE);
        label.setText(RenameMessages.CRenameRefactoringInputPage_label_updateWithin);
        label.setLayoutData(gd= new GridData());
        gd.horizontalSpan= 2;
        group= new Composite(top, SWT.NONE);
        group.setLayoutData(gd= new GridData());
        gd.horizontalSpan= 2;
        group.setLayout(new GridLayout(1, true));
        return group;
    }

    private void skipLine(Composite top) {
        new Label(top, SWT.NONE);
        new Label(top, SWT.NONE);
    }

    private void hookSelectionListeners() {
    	fNewName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onKeyReleaseInNameField();
			}
		});

        registerScopeListener(fWorkspace, TextSearchWrapper.SCOPE_WORKSPACE);
        registerScopeListener(fDependent, TextSearchWrapper.SCOPE_RELATED_PROJECTS);
        registerScopeListener(fSingle, TextSearchWrapper.SCOPE_SINGLE_PROJECT);
        registerScopeListener(fWorkingSet, TextSearchWrapper.SCOPE_WORKING_SET);
            
        if (fWorkingSetButton != null) {    
            fWorkingSetButton.addSelectionListener(new SelectionAdapter() {
                @Override
				public void widgetSelected(SelectionEvent e) {
                    onSelectWorkingSet();
                }
            });
        }
        SelectionListener listenOption= new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                onSelectOption();
            }
        };
        registerOptionListener(fDoVirtual, listenOption);
        registerOptionListener(fReferences, listenOption);
        registerOptionListener(fInComment, listenOption);
        registerOptionListener(fInInactiveCode, listenOption);
        registerOptionListener(fInInclude, listenOption);
        registerOptionListener(fInMacro, listenOption);
        registerOptionListener(fInString, listenOption);
        registerOptionListener(fInPreprocessor, listenOption);
        registerOptionListener(fExhausiveFileSearch, listenOption);
    }

    private void registerScopeListener(Button button, final int scope) {
        if (button != null) {
            button.addSelectionListener(new SelectionAdapter() {
                @Override
				public void widgetSelected(SelectionEvent e) {
                    onSelectedScope(scope);
                }
            });
        }
    }

    private void registerOptionListener(Button button, SelectionListener listenOption) {
        if (button != null) {
            button.addSelectionListener(listenOption);
        }
    }

    protected void onSelectedScope(int scope) {
        getRenameProcessor().setExhaustiveSearchScope(scope);
        updateEnablement();
    }

    private void onSelectOption() {
        int selectedOptions= computeSelectedOptions();
        boolean forcePreview= fForcePreviewOptions == -1 ||
       			(selectedOptions & fForcePreviewOptions) != 0;
        getRenameProcessor().setSelectedOptions(selectedOptions);
        getRefactoringWizard().setForcePreviewReview(forcePreview);
        updateEnablement();
    }            

    protected void onKeyReleaseInNameField() {
        getRenameProcessor().setReplacementText(fNewName.getText());
        updatePageComplete();
    }

    @Override
	public void dispose() {
        storePreferences();
        super.dispose();
    }
    
    private void readPreferences() {
        CRenameProcessor processor= getRenameProcessor();
        
        if (fWorkspace != null) {
            int scope = fPreferences.getScope();
            
            switch (scope) {
            case TextSearchWrapper.SCOPE_WORKSPACE:
                fWorkspace.setSelection(true);
                break;
            case TextSearchWrapper.SCOPE_SINGLE_PROJECT:
                fSingle.setSelection(true);
                break;
            case TextSearchWrapper.SCOPE_WORKING_SET:
                fWorkingSet.setSelection(true);
                break;
            default:
                scope= TextSearchWrapper.SCOPE_RELATED_PROJECTS;
                fDependent.setSelection(true);
                break;
            }
            processor.setExhaustiveSearchScope(scope);
       
            String workingSet= fPreferences.getWorkingSet();
    	    processor.setWorkingSetName(workingSet);  // CRenameProcessor validates the working set name.
            fWorkingSetSpec.setText(processor.getWorkingSetName());
        }
        
        if (fDoVirtual != null) {
            boolean val= !fPreferences.getBoolean(CRenameRefactoringPreferences.KEY_IGNORE_VIRTUAL);
            fDoVirtual.setSelection(val);
        }
        if (fReferences != null) {
            boolean val= !fPreferences.getBoolean(CRenameRefactoringPreferences.KEY_REFERENCES_INV);
            fReferences.setSelection(val);
        }
        initOption(fInInactiveCode, CRenameRefactoringPreferences.KEY_INACTIVE);
        initOption(fInComment, CRenameRefactoringPreferences.KEY_COMMENT);
        initOption(fInString, CRenameRefactoringPreferences.KEY_STRING);
        initOption(fInInclude, CRenameRefactoringPreferences.KEY_INCLUDE);
        initOption(fInMacro, CRenameRefactoringPreferences.KEY_MACRO_DEFINITION);
        initOption(fInPreprocessor, CRenameRefactoringPreferences.KEY_PREPROCESSOR);
        initOption(fExhausiveFileSearch, CRenameRefactoringPreferences.KEY_EXHAUSTIVE_FILE_SEARCH);
    }

    private int computeSelectedOptions() {
        int options= fPreferences.getOptions();
        options = updateOptions(options, fDoVirtual, CRefactory.OPTION_DO_VIRTUAL);
        options = updateOptions(options, fInInactiveCode, CRefactory.OPTION_IN_INACTIVE_CODE);
        options = updateOptions(options, fInComment, CRefactory.OPTION_IN_COMMENT);
        options = updateOptions(options, fInString, CRefactory.OPTION_IN_STRING_LITERAL);
        options = updateOptions(options, fInInclude, CRefactory.OPTION_IN_INCLUDE_DIRECTIVE);
        options = updateOptions(options, fInMacro, CRefactory.OPTION_IN_MACRO_DEFINITION);
        options = updateOptions(options, fInPreprocessor, CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE);
        options = updateOptions(options, fReferences, CRefactory.OPTION_IN_CODE_REFERENCES);
        options = updateOptions(options, fExhausiveFileSearch, CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
        return options;
    }

    private int updateOptions(int options, Button button, int optionMask) {
        if (button == null)
        	return options;
        if (button.getSelection()) {
            return options | optionMask;
        } else {
        	return options & ~optionMask;
        }
    }

    private void initOption(Button button, String key) {
    	initOption(button, key, false);
    }

    private void initOption(Button button, String key, boolean defaultValue) {
        if (button != null) {
            boolean val= fPreferences.getBoolean(key, defaultValue);
            button.setSelection(val);
        }
    }

    private void storePreferences() {
        if (fWorkspace != null) {
            int choice= TextSearchWrapper.SCOPE_RELATED_PROJECTS;
            if (fWorkspace.getSelection()) {
                choice= TextSearchWrapper.SCOPE_WORKSPACE;
            } else if (fSingle.getSelection()) {
                choice= TextSearchWrapper.SCOPE_SINGLE_PROJECT;
            } else if (fWorkingSet.getSelection()) {
                choice= TextSearchWrapper.SCOPE_WORKING_SET;	
            }
            fPreferences.put(CRenameRefactoringPreferences.KEY_SCOPE, choice);
            fPreferences.put(CRenameRefactoringPreferences.KEY_WORKING_SET_NAME, fWorkingSetSpec.getText());
        }
        if (fDoVirtual != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_IGNORE_VIRTUAL, !fDoVirtual.getSelection());
        }
        if (fReferences != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_REFERENCES_INV, !fReferences.getSelection());
        }
        if (fInComment != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_COMMENT, fInComment.getSelection());
        }
        if (fInString != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_STRING, fInString.getSelection());
        }
        if (fInInclude != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_INCLUDE, fInInclude.getSelection());
        }
        if (fInPreprocessor != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_PREPROCESSOR, fInPreprocessor.getSelection());
        }
        if (fInMacro != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_MACRO_DEFINITION, fInMacro.getSelection());
        }
        if (fInInactiveCode != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_INACTIVE, fInInactiveCode.getSelection());
        }
        if (fExhausiveFileSearch != null) {
            fPreferences.put(CRenameRefactoringPreferences.KEY_EXHAUSTIVE_FILE_SEARCH, fExhausiveFileSearch.getSelection());
        }
    }

    protected void onSelectWorkingSet() {
		CRenameProcessor processor= getRenameProcessor();
        String wsName= fWorkingSetSpec.getText();
		IWorkingSetManager wsManager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dlg= 
		    wsManager.createWorkingSetSelectionDialog(getShell(), false);
		IWorkingSet currentWorkingSet= wsManager.getWorkingSet(wsName);
		if (currentWorkingSet != null) {
			dlg.setSelection(new IWorkingSet[] { currentWorkingSet });
		}
		IWorkingSet ws= null;
		if (dlg.open() == Window.OK) {
			IWorkingSet wsa[]= dlg.getSelection();
			if (wsa != null && wsa.length > 0) {
				ws= wsa[0];
			}
			if (ws != null) {
			    fWorkspace.setSelection(false);
			    fDependent.setSelection(false);
			    fSingle.setSelection(false);
			    fWorkingSet.setSelection(true);
			    processor.setExhaustiveSearchScope(TextSearchWrapper.SCOPE_WORKING_SET);
			    wsName= ws.getName();
			}
		}
	    
	    processor.setWorkingSetName(wsName);  // CRenameProcessor validates the working set name.
		fWorkingSetSpec.setText(processor.getWorkingSetName());
	    updateEnablement();
    }

    protected void updatePageComplete() {
        String txt= fNewName.getText();
        if (txt.length() == 0 || txt.equals(fSearchString)) {
        	setErrorMessage(null);
        	setPageComplete(false);
        } else if (!CConventions.isValidIdentifier(txt)) {
        	setErrorMessage(NLS.bind(RenameMessages.CRenameRefactoringInputPage_errorInvalidIdentifier, txt));
        	setPageComplete(false);
        } else {
        	setErrorMessage(null);
        	setPageComplete(true);
        }
    }

    protected void updateEnablement() {
    	if (fExhausiveFileSearch != null) {
	        boolean enable= fExhaustiveSearchEnablingOptions == -1 ||
	        		(computeSelectedOptions() & fExhaustiveSearchEnablingOptions) != 0;
	
	        fExhausiveFileSearch.setEnabled(enable);
	        enable = enable && fExhausiveFileSearch.getSelection();
	        if (fWorkspace != null) {
	            fWorkspace.setEnabled(enable);
	            fDependent.setEnabled(enable);
	            fSingle.setEnabled(enable);
	
	            boolean enableSpec= false;
	            fWorkingSet.setEnabled(enable);
	            if (enable && fWorkingSet.getSelection()) {
	                enableSpec= true;
	            }
	            fWorkingSetSpec.setEnabled(enableSpec);
	            fWorkingSetButton.setEnabled(enable);
	        }
    	}
    }
    
    private CRenameProcessor getRenameProcessor() {
        return (CRenameProcessor) ((CRenameRefactoring) getRefactoring()).getProcessor();
    }
}
