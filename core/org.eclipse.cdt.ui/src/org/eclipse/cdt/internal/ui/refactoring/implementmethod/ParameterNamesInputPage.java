/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.HashMap;

import org.eclipse.cdt.internal.ui.preferences.formatter.TranslationUnitPreview;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.ValidatingLabeledTextField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * InputPage used by the ImplementMethod refactoring if its necessary to enter additional parameter names.
 *
 * @author Mirko Stocker
 */
public class ParameterNamesInputPage extends UserInputWizardPage {
	private static final int PREVIEW_UPDATE_DELAY = 500;
	private MethodToImplementConfig config;
	private TranslationUnitPreview translationUnitPreview;
	private Job delayedPreviewUpdater;
	private ImplementMethodWizard wizard;

	public ParameterNamesInputPage(MethodToImplementConfig config, ImplementMethodWizard wizard) {
		super(Messages.ParameterNamesInputPage_Title);
		this.config = config;
		this.wizard = wizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite superComposite = new Composite(parent, SWT.NONE);

		superComposite.setLayout(new GridLayout());

		setTitle(Messages.ImplementMethodInputPage_PageTitle);
		setMessage(Messages.ParameterNamesInputPage_CompleteMissingMails);

		ValidatingLabeledTextField validatingLabeledTextField = new ValidatingLabeledTextField(superComposite);
		validatingLabeledTextField
				.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		for (final ParameterInfo actParameterInfo : config.getParaHandler().getParameterInfos()) {
			String type = actParameterInfo.getTypeName();
			String content = actParameterInfo.getParameterName();
			boolean readOnly = !actParameterInfo.hasNewName();

			validatingLabeledTextField.addElement(type, content, readOnly, new ValidatingLabeledTextField.Validator() {
				@Override
				public void hasErrors() {
					setPageComplete(false);
				}

				@Override
				public void hasNoErrors() {
					setPageComplete(true);
				}

				@Override
				public boolean isValidInput(String newName) {
					actParameterInfo.setParameterName(newName);
					updatePreview();
					return true;
				}
			});
		}

		createPreview(superComposite);
		setControl(superComposite);
	}

	private InsertEdit getInsertEdit(CompositeChange compositeChange) {
		for (Change actChange : compositeChange.getChildren()) {
			if (actChange instanceof CompositeChange) {
				return getInsertEdit((CompositeChange) actChange);
			} else if (actChange instanceof CTextFileChange) {
				CTextFileChange textFileChange = (CTextFileChange) actChange;
				TextEdit edit = textFileChange.getEdit();
				if (edit instanceof MultiTextEdit) {
					MultiTextEdit multiEdit = (MultiTextEdit) edit;
					if (multiEdit.getChildrenSize() == 0) {
						continue;
					}
					TextEdit textEdit = multiEdit.getChildren()[0];
					if (textEdit instanceof InsertEdit) {
						return (InsertEdit) textEdit;
					}
				}
			}
		}
		return null;
	}

	public String createFunctionDefinitionSignature(IProgressMonitor monitor) {
		try {
			ModificationCollector collector = new ModificationCollector();
			ImplementMethodRefactoring implementMethodRefactoring = (ImplementMethodRefactoring) wizard
					.getRefactoring();
			CCompositeChange finalChange = null;
			// We can have multiple preview jobs. We don't
			// want multiple jobs concurrently using the same ASTs
			synchronized (implementMethodRefactoring) {
				implementMethodRefactoring.createDefinition(collector, config, monitor);
				finalChange = collector.createFinalChange();
			}
			InsertEdit insertEdit = getInsertEdit(finalChange);
			if (insertEdit == null) {
				return Messages.ImplementMethodRefactoringPage_PreviewGenerationNotPossible;
			}
			return insertEdit.getText().trim();
		} catch (OperationCanceledException e) {
			return Messages.ImplementMethodRefactoringPage_PreviewCanceled;
		} catch (CoreException e) {
			return Messages.ImplementMethodRefactoringPage_PreviewGenerationNotPossible;
		}
	}

	private void createPreview(Composite superComposite) {
		translationUnitPreview = new TranslationUnitPreview(new HashMap<String, String>(), superComposite);
		translationUnitPreview.getControl()
				.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		delayedPreviewUpdater = new Job(Messages.ImplementMethodRefactoringPage_GeneratingPreview) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setPreviewText(Messages.ImplementMethodRefactoringPage_GeneratingPreview);
				String functionDefinitionSignature = createFunctionDefinitionSignature(monitor);
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				setPreviewText(functionDefinitionSignature);
				return Status.OK_STATUS;
			}

			private void setPreviewText(final String text) {
				Shell shell = getShell();
				if (shell != null) {
					Display display = shell.getDisplay();
					if (display != null) {
						display.asyncExec(() -> {
							Control control = translationUnitPreview.getControl();
							if (control != null && !control.isDisposed()) {
								translationUnitPreview.setPreviewText(text);
							}
						});
					}
				}
			}
		};

		delayedPreviewUpdater.schedule(PREVIEW_UPDATE_DELAY);
	}

	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	@Override
	public IWizardPage getNextPage() {
		MethodToImplementConfig nextConfig = ((ImplementMethodRefactoring) wizard.getRefactoring()).getRefactoringData()
				.getNextConfigNeedingParameterNames(config);
		if (nextConfig != null) {
			return wizard.getPageForConfig(nextConfig);
		} else {
			wizard.cancelAndJoinPreviewJobs();
			return computeSuccessorPage();
		}
	}

	/**
	 * @return true if the preview job could still be running, false otherwise
	 */
	protected boolean cancelPreviewJob() {
		if (delayedPreviewUpdater == null) {
			return false;
		}

		// We cannot rely on getState being accurate in all cases so we only use this
		// as a hint to change the preview text
		if (delayedPreviewUpdater.getState() != Job.NONE) {
			translationUnitPreview.setPreviewText(Messages.ImplementMethodRefactoringPage_PreviewCanceled);
		}
		return !delayedPreviewUpdater.cancel();
	}

	protected void joinPreviewJob() {
		if (delayedPreviewUpdater == null)
			return;

		try {
			delayedPreviewUpdater.join();
		} catch (InterruptedException e) {
			CUIPlugin.log(e);
		}
	}

	private void updatePreview() {
		if (translationUnitPreview == null)
			return;

		delayedPreviewUpdater.schedule(PREVIEW_UPDATE_DELAY);
	}

	@Override
	public boolean isPageComplete() {
		if (!config.isChecked()) {
			return true;
		}
		return super.isPageComplete();
	}
}
