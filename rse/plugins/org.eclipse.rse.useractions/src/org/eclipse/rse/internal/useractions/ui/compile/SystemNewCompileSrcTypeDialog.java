package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMassager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ISystemValidatorUniqueString;
import org.eclipse.rse.ui.validators.ValidatorSourceType;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog used when in the Work With Compile Commands dialog when the user presses
 *  the "Add..." button to add a new source type.
 * <p>
 * This class is designed so that it need not be subclassed. Rather, the mri, validation
 *  and massaging can all be configured.
 */
public class SystemNewCompileSrcTypeDialog extends SystemPromptDialog {
	// gui
	protected Text srcTypeText;
	// input
	protected SystemCompileManager manager;
	protected ISystemValidator srcTypeValidator;
	protected ISystemMassager srcTypeMassager;
	protected boolean caseSensitive;
	protected String[] existingTypes;
	// output
	protected String newSrcType;
	// state
	protected SystemMessage errorMessage;
	protected boolean ignoreEvents;
	// mri
	protected String mriVerbageLabel, mriPromptLabel, mriPromptTooltip;

	/**
	 * Constructor when you want to use the default title.
	 * @param shell The parent window hosting this dialog
	 * @param compileManager The compile manager that manages these compile commands
	 * @param caseSensitive True if source types are case-sensitive. False if not.
	 */
	public SystemNewCompileSrcTypeDialog(Shell shell, SystemCompileManager compileManager, boolean caseSensitive) {
		this(shell, compileManager, caseSensitive, SystemUDAResources.RESID_COMPILE_NEWSRCTYPE_TITLE);
	}

	/**
	 * Constructor when you want to supply your own title.
	 * @param shell The parent window hosting this dialog
	 * @param compileManager The compile manager that manages these compile commands
	 * @param caseSensitive True if source types are case-sensitive. False if not.
	 */
	public SystemNewCompileSrcTypeDialog(Shell shell, SystemCompileManager compileManager, boolean caseSensitive, String title) {
		super(shell, title);
		this.manager = compileManager;
		this.caseSensitive = caseSensitive;
		setMRI(SystemUDAResources.RESID_COMPILE_NEWSRCTYPE_VERBAGE_LABEL, SystemUDAResources.RESID_COMPILE_NEWSRCTYPE_PROMPT_LABEL, SystemUDAResources.RESID_COMPILE_NEWSRCTYPE_PROMPT_TOOLTIP);
	}

	public void setMRI(String verbageMRILabel, String promptMRILabel, String promptMRITooltip) {
		if (verbageMRILabel != null) this.mriVerbageLabel = verbageMRILabel;
		if (promptMRILabel != null) this.mriPromptLabel = promptMRILabel;
		if (promptMRITooltip != null) this.mriPromptTooltip = promptMRITooltip;
	}

	/**
	 * Set the validator for the new src type
	 */
	public void setSrcTypeValidator(ISystemValidator validator) {
		this.srcTypeValidator = validator;
		if ((existingTypes != null) && (srcTypeValidator instanceof ISystemValidatorUniqueString)) {
			((ISystemValidatorUniqueString) srcTypeValidator).setExistingNamesList(existingTypes);
		}
	}

	/**
	 * Set the existing source types so error checking will prevent them from being entered again.
	 */
	public void setExistingSrcTypes(String[] srcTypes) {
		this.existingTypes = srcTypes;
		if ((srcTypeValidator != null) && (srcTypeValidator instanceof ISystemValidatorUniqueString)) {
			((ISystemValidatorUniqueString) srcTypeValidator).setExistingNamesList(existingTypes);
		}
	}

	/**
	 * Set the massager for the new src type. This will be 
	 *  called to massage the source type before it is returned.
	 * Eg, you might pass MassagerFoldCase to fold the result to uppercase or lowercase.
	 */
	public void setSrcTypeMassager(ISystemMassager massager) {
		this.srcTypeMassager = massager;
	}

	/**
	 * Create GUI controls, populate into given composite.
	 */
	protected Control createInner(Composite parent) {
		if (srcTypeValidator == null) setSrcTypeValidator(new ValidatorSourceType(caseSensitive));
		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// VERBAGE	
		SystemWidgetHelpers.createVerbiage(composite_prompts, mriVerbageLabel, nbrColumns, false, 250);
		// ENTRY FIELD		
		srcTypeText = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, mriPromptLabel, mriPromptTooltip);
		// add keystroke listeners...
		srcTypeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		setPageComplete(false); // since input is currently empty
		return composite_prompts;
	}

	/**
	 * Return widget to set initial focus to
	 */
	protected Control getInitialFocusControl() {
		return srcTypeText;
	}

	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() {
		newSrcType = srcTypeText.getText().trim();
		if (srcTypeMassager != null) {
			newSrcType = srcTypeMassager.massage(newSrcType);
			ignoreEvents = true;
			srcTypeText.setText(newSrcType);
			ignoreEvents = false;
		}
		boolean closeDialog = verify();
		if (closeDialog) {
			setOutputObject(newSrcType);
		}
		return closeDialog;
	}

	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() {
		errorMessage = validateInput();
		if (errorMessage == null)
			clearErrorMessage();
		else {
			srcTypeText.setFocus();
			setErrorMessage(errorMessage);
		}
		return (errorMessage == null);
	}

	/**
	 * This hook method is called whenever the text changes in the cmd input field.
	 * Currently not used.
	 */
	protected SystemMessage validateInput() {
		if (ignoreEvents) return errorMessage;
		errorMessage = null;
		if (srcTypeValidator != null) errorMessage = srcTypeValidator.validate(srcTypeText.getText().trim());
		if (errorMessage != null)
			setErrorMessage(errorMessage);
		else
			clearErrorMessage();
		setPageComplete();
		return errorMessage;
	}

	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete() {
		boolean pageComplete = false;
		if (errorMessage == null) {
			pageComplete = (srcTypeText.getText().trim().length() > 0);
		}
		return pageComplete;
	}

	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete() {
		setPageComplete(isPageComplete());
	}

	/**
	 * Returns the user-specified new source type
	 */
	public String getNewSrcType() {
		//System.out.println("Returning " + newSrcType);
		return newSrcType;
	}
}
