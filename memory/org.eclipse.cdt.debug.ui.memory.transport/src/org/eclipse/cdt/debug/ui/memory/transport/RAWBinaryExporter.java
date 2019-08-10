/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.eclipse.cdt.debug.ui.memory.transport.model.IMemoryExporter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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

public class RAWBinaryExporter implements IMemoryExporter {
	File fOutputFile;
	BigInteger fStartAddress;
	BigInteger fEndAddress;

	private Text fStartText;
	private Text fEndText;
	private Text fLengthText;
	private Text fFileText;

	private IMemoryBlock fMemoryBlock;

	private ExportMemoryDialog fParentDialog;

	private IDialogSettings fProperties;

	@Override
	public Control createControl(final Composite parent, IMemoryBlock memBlock, IDialogSettings properties,
			ExportMemoryDialog parentDialog) {
		fMemoryBlock = memBlock;
		fParentDialog = parentDialog;
		fProperties = properties;

		Composite composite = new Composite(parent, SWT.NONE) {
			@Override
			public void dispose() {
				fProperties.put(TRANSFER_FILE, fFileText.getText().trim());
				fProperties.put(TRANSFER_START, fStartText.getText().trim());
				fProperties.put(TRANSFER_END, fEndText.getText().trim());

				try {
					fStartAddress = getStartAddress();
					fEndAddress = getEndAddress();
					fOutputFile = getFile();
				} catch (Exception e) {
				}

				super.dispose();
			}
		};
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);

		// start address

		Label startLabel = new Label(composite, SWT.NONE);
		startLabel.setText(Messages.getString("RAWBinaryExporter.StartAddress")); //$NON-NLS-1$
		FormData data = new FormData();
		startLabel.setLayoutData(data);

		fStartText = new Text(composite, SWT.BORDER);
		data = new FormData();
		data.left = new FormAttachment(startLabel);
		data.width = 120;
		fStartText.setLayoutData(data);

		// end address

		Label endLabel = new Label(composite, SWT.NONE);
		endLabel.setText(Messages.getString("RAWBinaryExporter.EndAddress")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(fStartText);
		endLabel.setLayoutData(data);

		fEndText = new Text(composite, SWT.BORDER);
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(endLabel);
		data.width = 120;
		fEndText.setLayoutData(data);

		// length

		Label lengthLabel = new Label(composite, SWT.NONE);
		lengthLabel.setText(Messages.getString("RAWBinaryExporter.Length")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(fEndText);
		lengthLabel.setLayoutData(data);

		fLengthText = new Text(composite, SWT.BORDER);
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(lengthLabel);
		data.width = 120;
		fLengthText.setLayoutData(data);

		// file

		Label fileLabel = new Label(composite, SWT.NONE);
		fFileText = new Text(composite, SWT.BORDER);
		Button fileButton = new Button(composite, SWT.PUSH);

		fileLabel.setText(Messages.getString("Exporter.FileName")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		data.left = new FormAttachment(fileLabel);
		data.width = 360;
		fFileText.setLayoutData(data);

		fileButton.setText(Messages.getString("Exporter.Browse")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fLengthText);
		data.left = new FormAttachment(fFileText);
		fileButton.setLayoutData(data);

		String textValue = fProperties.get(TRANSFER_FILE);
		fFileText.setText(textValue != null ? textValue : ""); //$NON-NLS-1$

		textValue = fProperties.get(TRANSFER_START);
		fStartText.setText(textValue != null ? textValue : "0x0"); //$NON-NLS-1$

		try {
			getStartAddress();
		} catch (Exception e) {
			fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		}

		textValue = fProperties.get(TRANSFER_END);
		fEndText.setText(textValue != null ? textValue : "0x0"); //$NON-NLS-1$

		try {
			getEndAddress();
		} catch (Exception e) {
			fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		}

		try {
			BigInteger length = getEndAddress().subtract(getStartAddress());
			fLengthText.setText(length.toString());
			if (length.compareTo(BigInteger.ZERO) <= 0) {
				fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			}
		} catch (Exception e) {
			fLengthText.setText("0");
			fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		}

		fileButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
				dialog.setText(Messages.getString("RAWBinaryExporter.ChooseFile")); //$NON-NLS-1$
				dialog.setFilterExtensions(new String[] { "*.*;*" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { Messages.getString("Exporter.AllFiles") }); //$NON-NLS-1$
				dialog.setFileName(fFileText.getText().trim());
				dialog.open();

				String filename = dialog.getFileName();
				if (filename != null && filename.length() != 0) {
					fFileText.setText(dialog.getFilterPath() + File.separator + filename);
				}

				validate();
			}

		});

		fStartText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

					BigInteger startAddress = getStartAddress();
					BigInteger actualLength = getEndAddress().subtract(startAddress);
					fLengthText.setText(actualLength.toString());

					if (actualLength.compareTo(BigInteger.ZERO) <= 0) {
						fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}

					if (startAddress.compareTo(BigInteger.ZERO) < 0) {
						fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}

					BigInteger endAddress = getEndAddress();
					if (endAddress.compareTo(BigInteger.ZERO) < 0) {
						fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
				} catch (Exception ex) {
					fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}

				validate();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		fEndText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

					BigInteger actualLength = getEndAddress().subtract(getStartAddress());
					fLengthText.setText(actualLength.toString());

					if (actualLength.compareTo(BigInteger.ZERO) <= 0) {
						fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}

					BigInteger startAddress = getStartAddress();
					if (startAddress.compareTo(BigInteger.ZERO) < 0) {
						fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}

					BigInteger endAddress = getEndAddress();
					if (endAddress.compareTo(BigInteger.ZERO) < 0) {
						fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
				} catch (Exception ex) {
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}

				validate();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

		});

		fLengthText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

					fStartText.setText(fStartText.getText().trim());

					BigInteger length = getLength();
					String endString;
					BigInteger startAddress = getStartAddress();
					BigInteger endAddress = startAddress.add(length);

					if (length.compareTo(BigInteger.ZERO) <= 0) {
						if (endAddress.compareTo(BigInteger.ZERO) < 0) {
							endString = endAddress.toString(16); //$NON-NLS-1$
						} else {
							endString = "0x" + endAddress.toString(16); //$NON-NLS-1$
						}
					} else {
						endString = "0x" + endAddress.toString(16); //$NON-NLS-1$
					}

					fEndText.setText(endString);

					if (length.compareTo(BigInteger.ZERO) <= 0) {
						fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}

					if (startAddress.compareTo(BigInteger.ZERO) < 0) {
						fStartText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}

					if (endAddress.compareTo(BigInteger.ZERO) < 0) {
						fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
				} catch (Exception ex) {
					if (fLengthText.getText().trim().length() != 0) {
						fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}

				validate();
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		fFileText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				validate();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		composite.pack();
		parent.pack();

		/*
		 *  We need to perform a validation. If we do it immediately we will get an exception
		 *  because things are not totally setup. So we schedule an immediate running of  the
		 *  validation. For a very brief time the view logically may show a state which  does
		 *  not reflect the true state of affairs.  But the validate immediately corrects the
		 *  info. In practice the user never sees the invalid state displayed, because of the
		 *  speed of the draw of the dialog.
		 */
		Display.getDefault().asyncExec(() -> validate());

		return composite;
	}

	public BigInteger getEndAddress() {
		String text = fEndText.getText();
		text = text.trim();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger endAddress = new BigInteger(hex ? text.substring(2) : text, hex ? 16 : 10);

		return endAddress;
	}

	public BigInteger getStartAddress() {
		String text = fStartText.getText();
		text = text.trim();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text, hex ? 16 : 10);

		return startAddress;
	}

	public BigInteger getLength() {
		String text = fLengthText.getText();
		text = text.trim();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger lengthAddress = new BigInteger(hex ? text.substring(2) : text, hex ? 16 : 10);

		return lengthAddress;
	}

	public File getFile() {
		return new File(fFileText.getText().trim());
	}

	private void validate() {
		boolean isValid = true;

		try {
			getEndAddress();
			getStartAddress();

			BigInteger length = getLength();

			if (length.compareTo(BigInteger.ZERO) <= 0)
				isValid = false;

			if (fFileText.getText().trim().length() == 0)
				isValid = false;

			File file = getFile();
			if (file != null) {
				File parentFile = file.getParentFile();

				if (parentFile != null && !parentFile.exists())
					isValid = false;

				if (parentFile != null && parentFile.exists() && (!parentFile.canRead() || !parentFile.isDirectory()))
					isValid = false;

				if (file.isDirectory())
					isValid = false;
			}
		} catch (Exception e) {
			isValid = false;
		}

		fParentDialog.setValid(isValid);
	}

	@Override
	public String getId() {
		return "rawbinary"; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return Messages.getString("RAWBinaryExporter.Name"); //$NON-NLS-1$
	}

	@Override
	public void exportMemory() {
		Job job = new Job("Memory Export to RAW Binary File") { //$NON-NLS-1$
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					BigInteger DATA_PER_RECORD = BigInteger.valueOf(1024);

					BigInteger transferAddress = fStartAddress;

					FileOutputStream writer = new FileOutputStream(fOutputFile);

					BigInteger jobs = fEndAddress.subtract(transferAddress).divide(DATA_PER_RECORD);
					BigInteger factor = BigInteger.ONE;
					if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
						factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
						jobs = jobs.divide(factor);
					}

					monitor.beginTask(Messages.getString("Exporter.ProgressTitle"), jobs.intValue()); //$NON-NLS-1$

					BigInteger jobCount = BigInteger.ZERO;
					while (transferAddress.compareTo(fEndAddress) < 0 && !monitor.isCanceled()) {
						BigInteger length = DATA_PER_RECORD;
						if (fEndAddress.subtract(transferAddress).compareTo(length) < 0)
							length = fEndAddress.subtract(transferAddress);

						monitor.subTask(String.format(Messages.getString("Exporter.Progress"), length.toString(10), //$NON-NLS-1$
								transferAddress.toString(16)));

						// data
						byte[] byteValues = new byte[length.intValue()];

						MemoryByte bytes[] = ((IMemoryBlockExtension) fMemoryBlock).getBytesFromAddress(transferAddress,
								length.longValue() / ((IMemoryBlockExtension) fMemoryBlock).getAddressableSize());
						for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
							byteValues[byteIndex] = bytes[byteIndex].getValue();
						}

						writer.write(byteValues);

						transferAddress = transferAddress.add(length);

						jobCount = jobCount.add(BigInteger.ONE);
						if (jobCount.compareTo(factor) == 0) {
							jobCount = BigInteger.ZERO;
							monitor.worked(1);
						}
					}

					writer.close();
					monitor.done();
				} catch (IOException ex) {
					MemoryTransportPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
									DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrFile"), ex)); //$NON-NLS-1$
					return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrFile"), ex); //$NON-NLS-1$

				} catch (DebugException ex) {
					MemoryTransportPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
									DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrReadTarget"), ex)); //$NON-NLS-1$
					return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrReadTarget"), ex); //$NON-NLS-1$
				} catch (Exception ex) {
					MemoryTransportPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
									DebugException.INTERNAL_ERROR, Messages.getString("Exporter.Falure"), ex)); //$NON-NLS-1$
					return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.INTERNAL_ERROR, Messages.getString("Exporter.Falure"), ex); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

}
