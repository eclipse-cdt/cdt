/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/


package org.eclipse.cdt.internal.ui.refactoring.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;


/**
 * @author Mirko Stocker
 * 
 * Text field with a description and error handling using the Validator-Callback. Can also be used for multiple inputs.
 *
 */
public class ValidatingLabeledTextField extends Composite {
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private final Map<Text, Boolean> validationStatus = new HashMap<Text, Boolean>();
	
	private final ArrayList<Listener> inputTextListeners = new ArrayList<Listener>();
	
	private final Color errorColor = new Color(getShell().getDisplay(), new RGB(255, 208, 196));
	
	/**
	 * The Validator is used for feedback about the validation status of the inputs and to validate the input.
	 */
	public static abstract class Validator {

		/**
		 * Is called if all input texts contain valid input.
		 */
		public void hasErrors() {}
		
		/**
		 * Is called if any input text contains invalid input.
		 */
		public void hasNoErrors() {}
		
		/**
		 * @param text the new value of the field
		 * @return whether the value is valid or not
		 */
		public boolean isValidInput(String text) { return true; }

		public String errorMessageForEmptyField() {
			return Messages.ValidatingLabeledTextField_CantBeEmpty; 
		}

		public String errorMessageForInvalidInput() {
			return Messages.ValidatingLabeledTextField_InvalidCharacters; 
		}

		public String errorMessageForDuplicateValues() {
			return Messages.ValidatingLabeledTextField_DuplicatedNames; 
		}

		public String errorIsKeywordMessage() {
			return Messages.ValidatingLabeledTextField_IsKeyword;
		}
	}

	public ValidatingLabeledTextField(Composite parent, int style) {
		super(parent, style);
		
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 4;
	     
	    setLayout(gridLayout);
	}

	public ValidatingLabeledTextField(Composite parent) {
		this(parent, SWT.NONE);
	}
	
	public void addElement(String description, String initialText, boolean readOnly, final Validator validator) {
		
		Label label = new Label(this, SWT.NONE);
		label.setText(description);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		final Text textField = new Text(this, SWT.BORDER |SWT.SINGLE);
		textField.setText(initialText);
		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(readOnly) {
			//readOnly inputs are always valid:
			validationStatus.put(textField, Boolean.TRUE);
			textField.setEnabled(false);
			return;
		}
		validationStatus.put(textField, Boolean.FALSE);
		
		final Label errorImageLabel = new Label(this, SWT.NONE);
		errorImageLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
		errorImageLabel.setLayoutData(new GridData());
		errorImageLabel.setVisible(false);
		
		final Label errorLabel = new Label(this, SWT.NONE);
		errorLabel.setLayoutData(new GridData());

		final Color defaultColor = textField.getBackground();
		
		Listener listener = new Listener(){
			
			@SuppressWarnings("unchecked")
			public void checkField() {
				String newName = textField.getText();
				

				boolean isEmpty = (newName.length() == 0);

				boolean isNameAlreadyInUse = nameAlreadyInUse(textField, newName);
				boolean isValid = validator.isValidInput(newName);
				boolean isKeyword = NameHelper.isKeyword(newName);
				boolean isValidName = NameHelper.isValidLocalVariableName(newName);

				boolean isOk = isValid && !isNameAlreadyInUse && !isEmpty && !isKeyword && isValidName;
				if (isOk) {
					setErrorStatus(EMPTY_STRING);
				} else if (isEmpty) {
					setErrorStatus(validator.errorMessageForEmptyField());
				} else if (!isValid  || !isValidName) {
					setErrorStatus(validator.errorMessageForInvalidInput());
				} else if (isKeyword) {
					setErrorStatus(validator.errorIsKeywordMessage());
				}else if (isNameAlreadyInUse) {
					setErrorStatus(validator.errorMessageForDuplicateValues());
				} 
				validationStatus.put(textField, isOk);
				
				if(validationStatus.values().contains(Boolean.FALSE) || isEmpty || isNameAlreadyInUse || isKeyword || !isValidName) {
					validator.hasErrors();
				} else {
					validator.hasNoErrors();
				}
				
				layout();
				
				// recheck all other listeners in case duplicate names have been resolved, 
				// but remove this first to avoid an infinite loop
				inputTextListeners.remove(this);
				for(Listener listener : (ArrayList<Listener>) inputTextListeners.clone()) {
					listener.handleEvent(null);
				}
				inputTextListeners.add(this);
			}

			private boolean nameAlreadyInUse(final Text textField, String newName) {
				for (Text text : validationStatus.keySet()) {
					if(text != textField && text.getText().equals(newName)) {
						return true;
					}
				}
				return false;
			}

			private void setErrorStatus(String errorMessage) {
				if (EMPTY_STRING.equals(errorMessage)) {
					textField.setBackground(defaultColor);
					errorLabel.setText(EMPTY_STRING);
					errorImageLabel.setVisible(false);
				} else {
					textField.setBackground(errorColor);
					errorLabel.setText(errorMessage);
					errorImageLabel.setVisible(true);
				}
			}
			
			@Override
			public void handleEvent(Event event) {
				checkField();
			}};
			
		//we need to keep a list of all listeners so we get access from other textfields to resolve duplicate names
		inputTextListeners.add(listener);
			
		listener.handleEvent(null);
		
		textField.addListener(SWT.Modify, listener);
	}
}
