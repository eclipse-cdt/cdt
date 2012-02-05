/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <bmuskalla@eclipsesource.com>
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.refactoring.ChangeParametersControl;
import org.eclipse.cdt.internal.ui.refactoring.ChangeParametersControl.Mode;
import org.eclipse.cdt.internal.ui.refactoring.IParameterListChangeListener;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.StubTypeContext;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.cdt.internal.ui.util.RowLayouter;

public class ExtractFunctionInputPage extends UserInputWizardPage {
	public static final String PAGE_NAME = "ExtractFunctionInputPage";//$NON-NLS-1$
	static final String DIALOG_SETTING_SECTION = "ExtractFunctionWizard"; //$NON-NLS-1$

	private ExtractFunctionRefactoring refactoring;
	private ExtractFunctionInformation info;
	private Text textField;
	private boolean firstTime;
	private CSourceViewer signaturePreview;
	private Document signaturePreviewDocument;
	private IDialogSettings settings;

	private static final String DESCRIPTION = Messages.ExtractFunctionInputPage_description;
	private static final String ACCESS_MODIFIER = "AccessModifier"; //$NON-NLS-1$

	public ExtractFunctionInputPage() {
		super(PAGE_NAME);
		setImageDescriptor(CPluginImages.DESC_WIZBAN_REFACTOR_TU);
		setDescription(DESCRIPTION);
		firstTime = true;
		signaturePreviewDocument = new Document();
	}

	@Override
	public void createControl(Composite parent) {
		refactoring = (ExtractFunctionRefactoring) getRefactoring();
		info = ((ExtractFunctionRefactoring) getRefactoring()).getRefactoringInfo();
		loadSettings();

		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);
		RowLayouter layouter = new RowLayouter(2);
		GridData gd = null;

		initializeDialogUnits(result);

		Label label = new Label(result, SWT.NONE);
		label.setText(getLabelText());

		textField = createTextInputField(result, SWT.BORDER);
		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		layouter.perform(label, textField, 1);

		label = new Label(result, SWT.NONE);
		label.setText(Messages.ExtractFunctionInputPage_access_modifier);

		Composite group = new Composite(result, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginWidth = 0;
		group.setLayout(layout);

		String[] labels = new String[] {
			Messages.ExtractFunctionInputPage_public,
			Messages.ExtractFunctionInputPage_protected,
			Messages.ExtractFunctionInputPage_private
		};
		VisibilityEnum[] visibilityValues = new VisibilityEnum[] {
				VisibilityEnum.v_public, VisibilityEnum.v_protected, VisibilityEnum.v_private
			};
		VisibilityEnum visibility = info.getVisibility();
		for (int i = 0; i < labels.length; i++) {
			Button radio = new Button(group, SWT.RADIO);
			radio.setText(labels[i]);
			radio.setData(visibilityValues[i]);
			if (visibilityValues[i].equals(visibility))
				radio.setSelection(true);
			radio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					final VisibilityEnum selectedModifier = (VisibilityEnum) event.widget.getData();
					settings.put(ACCESS_MODIFIER, selectedModifier.toString());
					setVisibility(selectedModifier);
				}
			});
		}
		layouter.perform(label, group, 1);

		if (!info.getParameters().isEmpty()) {
			Mode mode = info.getMandatoryReturnVariable() != null ?
					Mode.EXTRACT_METHOD_FIXED_RETURN : Mode.EXTRACT_METHOD;
			ChangeParametersControl paramControl = new ChangeParametersControl(result, SWT.NONE,
					Messages.ExtractFunctionInputPage_parameters,
					new IParameterListChangeListener() {
						@Override
						public void parameterChanged(NameInformation parameter) {
							parameterModified();
						}

						@Override
						public void parameterListChanged() {
							parameterModified();
						}

						@Override
						public void parameterAdded(NameInformation parameter) {
							updatePreview(getText());
						}
					},
					mode,
					new StubTypeContext(refactoring.getTranslationUnit()));
			gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = 2;
			paramControl.setLayoutData(gd);
			paramControl.setInput(info.getParameters());
		}

		int duplicates = refactoring.getNumberOfDuplicates();
		Button checkBox = new Button(result, SWT.CHECK);
		if (duplicates == 0) {
			checkBox.setText(Messages.ExtractFunctionInputPage_duplicates_none);
		} else if (duplicates == 1) {
			checkBox.setText(Messages.ExtractFunctionInputPage_duplicates_single);
		} else {
			checkBox.setText(NLS.bind(
				Messages.ExtractFunctionInputPage_duplicates_multi, Integer.valueOf(duplicates)));
		}
		checkBox.setSelection(info.isReplaceDuplicates());
		checkBox.setEnabled(duplicates > 0);
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				info.setReplaceDuplicates(((Button) e.widget).getSelection());
			}
		});
		layouter.perform(checkBox);

		label = new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layouter.perform(label);

		createSignaturePreview(result, layouter);

		Dialog.applyDialogFont(result);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				ICHelpContextIds.EXTRACT_FUNCTION_WIZARD_PAGE);
	}

	private Text createTextInputField(Composite parent, int style) {
		Text result = new Text(parent, style);
		result.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				textModified(getText());
			}
		});
		TextFieldNavigationHandler.install(result);
		return result;
	}

	private String getText() {
		if (textField == null)
			return null;
		return textField.getText();
	}

	private String getLabelText(){
		return Messages.ExtractFunctionInputPage_label_text;
	}

	private void setVisibility(VisibilityEnum visibility) {
		info.setVisibility(visibility);
		updatePreview(getText());
	}

	private void createSignaturePreview(Composite composite, RowLayouter layouter) {
		Label previewLabel = new Label(composite, SWT.NONE);
		previewLabel.setText(Messages.ExtractFunctionInputPage_signature_preview);
		layouter.perform(previewLabel);

		IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
		signaturePreview = new CSourceViewer(composite, null, null, false, SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP /*| SWT.BORDER*/, store);
		signaturePreview.configure(new CSourceViewerConfiguration(CUIPlugin.getDefault().getTextTools().getColorManager(), store, null, null));
		signaturePreview.getTextWidget().setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
		signaturePreview.adaptBackgroundColor(composite);
		signaturePreview.setDocument(signaturePreviewDocument);
		signaturePreview.setEditable(false);

		// Layouting problems with wrapped text: see http://bugs.eclipse.org/bugs/show_bug.cgi?id=9866
		Control signaturePreviewControl = signaturePreview.getControl();
		PixelConverter pixelConverter = new PixelConverter(signaturePreviewControl);
		GridData gdata = new GridData(GridData.FILL_BOTH);
		gdata.widthHint = pixelConverter.convertWidthInCharsToPixels(50);
		gdata.heightHint = pixelConverter.convertHeightInCharsToPixels(3);
		signaturePreviewControl.setLayoutData(gdata);
		layouter.perform(signaturePreviewControl);
	}

	private void updatePreview(String methodName) {
		if (signaturePreview == null)
			return;

		if (methodName.isEmpty()) {
			methodName = StubUtility.suggestMethodName("someMethodName", null, //$NON-NLS-1$
					refactoring.getTranslationUnit());	
		}

		int top = signaturePreview.getTextWidget().getTopPixel();
		String signature = refactoring.getSignature(methodName);
		signaturePreviewDocument.set(signature);
		signaturePreview.getTextWidget().setTopPixel(top);
	}

	private void loadSettings() {
		settings = getDialogSettings().getSection(DIALOG_SETTING_SECTION);
		if (settings == null) {
			settings = getDialogSettings().addNewSection(DIALOG_SETTING_SECTION);
			settings.put(ACCESS_MODIFIER, VisibilityEnum.v_private.toString());
		}
		final String accessModifier = settings.get(ACCESS_MODIFIER);
		if (accessModifier != null) {
			info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(accessModifier));
		}
	}

	//---- Input validation ------------------------------------------------------

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			if (firstTime) {
				firstTime = false;
				setPageComplete(false);
				updatePreview(getText());
				textField.setFocus();
			} else {
				setPageComplete(validatePage(true));
			}
		}
		super.setVisible(visible);
	}

	private void textModified(String methodName) {
		info.setMethodName(methodName);
		RefactoringStatus status = validatePage(true);
		if (!status.hasFatalError()) {
			updatePreview(methodName);
		} else {
			signaturePreviewDocument.set(""); //$NON-NLS-1$
		}
		setPageComplete(status);
	}

	private void parameterModified() {
		updatePreview(getText());
		setPageComplete(validatePage(false));
	}

	private RefactoringStatus validatePage(boolean text) {
		RefactoringStatus result = new RefactoringStatus();
		if (text) {
			result.merge(validateMethodName());
			result.merge(validateParameters());
		} else {
			result.merge(validateParameters());
			result.merge(validateMethodName());
		}
		return result;
	}

	private RefactoringStatus validateMethodName() {
		RefactoringStatus result = new RefactoringStatus();
		String methodName = getText();
		if (methodName.isEmpty()) {
			result.addFatalError(Messages.ExtractFunctionInputPage_validation_empty_function_name);
			return result;
		}
		result.merge(refactoring.checkMethodName());
		return result;
	}

	private RefactoringStatus validateParameters() {
		RefactoringStatus result = new RefactoringStatus();
		for (NameInformation paramInfo : info.getParameters()) {
			if (paramInfo.getNewName().isEmpty()) {
				result.addFatalError(Messages.ExtractFunctionInputPage_validation_empty_parameter_name);
				return result;
			}
		}
		result.merge(refactoring.checkParameterNames());
		return result;
	}
}
