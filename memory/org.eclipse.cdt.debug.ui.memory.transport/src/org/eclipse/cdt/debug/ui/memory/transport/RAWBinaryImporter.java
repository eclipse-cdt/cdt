/*******************************************************************************
 * Copyright (c) 2006, 2020 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Alexander Fedorov (ArSysOp) - headless part extraction
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport;

import java.io.File;
import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.IScrollMemory;
import org.eclipse.cdt.debug.core.memory.transport.ImportRequest;
import org.eclipse.cdt.debug.internal.core.memory.transport.RAWBinaryImport;
import org.eclipse.cdt.debug.internal.core.memory.transport.TransportJob;
import org.eclipse.cdt.debug.internal.ui.memory.transport.ScrollMemory;
import org.eclipse.cdt.debug.internal.ui.memory.transport.WriteMemoryBlock;
import org.eclipse.cdt.debug.ui.memory.transport.model.IMemoryImporter;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RAWBinaryImporter implements IMemoryImporter {

	File fInputFile;
	BigInteger fStartAddress;
	Boolean fScrollToStart;

	private Text fStartText;
	private Text fFileText;

	private Button fScrollToBeginningOnImportComplete;

	private IMemoryBlock fMemoryBlock;

	private ImportMemoryDialog fParentDialog;

	private IDialogSettings fProperties;

	@Override
	public Control createControl(final Composite parent, IMemoryBlock memBlock, IDialogSettings properties,
			ImportMemoryDialog parentDialog) {
		fMemoryBlock = memBlock;
		fParentDialog = parentDialog;
		fProperties = properties;

		Composite composite = new Composite(parent, SWT.NONE) {
			@Override
			public void dispose() {
				fProperties.put(TRANSFER_FILE, fFileText.getText().trim());
				fProperties.put(TRANSFER_START, fStartText.getText().trim());
				fProperties.put(TRANSFER_SCROLL_TO_START, fScrollToBeginningOnImportComplete.getSelection());

				try {
					fStartAddress = getStartAddress();
					fInputFile = getFile();
					fScrollToStart = getScrollToStart();
				} catch (Exception e) {
				}

				super.dispose();
			}
		};
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);

		// restore to this address

		Label labelStartText = new Label(composite, SWT.NONE);
		labelStartText.setText(Messages.getString("RAWBinaryImporter.RestoreAddress")); //$NON-NLS-1$

		fStartText = new Text(composite, SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(labelStartText);
		data.width = 120;
		fStartText.setLayoutData(data);

		// file

		Label fileLabel = new Label(composite, SWT.NONE);
		fFileText = new Text(composite, SWT.BORDER);
		Button fileButton = new Button(composite, SWT.PUSH);

		fileLabel.setText(Messages.getString("Importer.File")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		data.left = new FormAttachment(fileLabel);
		data.width = 300;
		fFileText.setLayoutData(data);

		fileButton.setText(Messages.getString("Importer.Browse")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fStartText);
		data.left = new FormAttachment(fFileText);
		fileButton.setLayoutData(data);

		String textValue = fProperties.get(TRANSFER_FILE);
		fFileText.setText(textValue != null ? textValue : ""); //$NON-NLS-1$

		textValue = fProperties.get(TRANSFER_START);
		fStartText.setText(textValue != null ? textValue : "0x0"); //$NON-NLS-1$

		fileButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
				dialog.setText(Messages.getString("RAWBinaryImporter.ChooseFile")); //$NON-NLS-1$
				dialog.setFilterExtensions(new String[] { "*.*;*" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { Messages.getString("Importer.AllFiles") }); //$NON-NLS-1$
				dialog.setFileName(fFileText.getText().trim());
				dialog.open();

				String filename = dialog.getFileName();
				if (filename != null && filename.length() != 0) {
					fFileText.setText(dialog.getFilterPath() + File.separator + filename);
				}

				validate();
			}

		});

		fStartText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = true;
				try {
					getStartAddress();
				} catch (Exception ex) {
					valid = false;
				}

				fStartText.setForeground(valid ? Display.getDefault().getSystemColor(SWT.COLOR_BLACK)
						: Display.getDefault().getSystemColor(SWT.COLOR_RED));

				//

				validate();
			}

		});
		fFileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		fScrollToBeginningOnImportComplete = new Button(composite, SWT.CHECK);
		fScrollToBeginningOnImportComplete.setText(Messages.getString("RAWBinaryImporter.ScrollToStart")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fileButton);
		fScrollToBeginningOnImportComplete.setLayoutData(data);
		final boolean scrollToStart = properties.getBoolean(TRANSFER_SCROLL_TO_START);
		fScrollToBeginningOnImportComplete.setSelection(scrollToStart);

		composite.pack();
		parent.pack();

		Display.getDefault().asyncExec(() -> validate());

		return composite;
	}

	private void validate() {
		boolean isValid = true;

		try {
			getStartAddress();

			if (fFileText.getText().trim().length() == 0)
				isValid = false;

			if (!getFile().exists()) {
				isValid = false;
			}
		} catch (Exception e) {
			isValid = false;
		}

		fParentDialog.setValid(isValid);
	}

	public boolean getScrollToStart() {
		return fScrollToBeginningOnImportComplete.getSelection();
	}

	public BigInteger getStartAddress() {
		String text = fStartText.getText();
		text = text.trim();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text, hex ? 16 : 10);

		return startAddress;
	}

	public File getFile() {
		return new File(fFileText.getText().trim());
	}

	@Override
	public String getId() {
		return "rawbinary"; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return Messages.getString("RAWBinaryImporter.Name"); //$NON-NLS-1$
	}

	@Override
	public void importMemory() {
		try {
			IScrollMemory scroll = fScrollToStart ? new ScrollMemory(fParentDialog) : IScrollMemory.ignore();
			IMemoryBlockExtension block = (IMemoryBlockExtension) fMemoryBlock;
			ImportRequest request = new ImportRequest(block.getBigBaseAddress(), fStartAddress,
					new WriteMemoryBlock(block));
			RAWBinaryImport memoryImport = new RAWBinaryImport(fInputFile, request, scroll);
			TransportJob job = new TransportJob(//
					"Memory Import from RAW Binary File", memoryImport);
			job.setUser(true);
			job.schedule();
		} catch (DebugException e) {
			//FIXME: unreachable with current implementation, to be i18n after UI rework
			ErrorDialog.openError(fParentDialog.getShell(), //
					"Import Failure", //
					"Failed to retrieve base memory address", //
					e.getStatus());
		}
	}

}
