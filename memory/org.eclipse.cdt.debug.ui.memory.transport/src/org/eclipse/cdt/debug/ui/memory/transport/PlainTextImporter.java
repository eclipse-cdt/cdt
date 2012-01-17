/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.ui.memory.transport.model.IMemoryImporter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
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

public class PlainTextImporter implements IMemoryImporter {

	File fInputFile;
	BigInteger fStartAddress;
	boolean fUseCustomAddress; 
	Boolean fScrollToStart;
	
	private Text fStartText;
	private Text fFileText;
	private Button fScrollToBeginningOnImportComplete;
	
	private IMemoryBlock fMemoryBlock;
	
	private ImportMemoryDialog fParentDialog;
	
	private IDialogSettings fProperties;
	
	private static final int BUFFER_LENGTH = 64 * 1024;
	
	public Control createControl(final Composite parent, IMemoryBlock memBlock, IDialogSettings properties, ImportMemoryDialog parentDialog)
	{
		fMemoryBlock = memBlock;
		fParentDialog = parentDialog;
		fProperties = properties;
		
		fUseCustomAddress = true;
	
		Composite composite = new Composite(parent, SWT.NONE)
		{
			@Override
			public void dispose()
			{
				fProperties.put(TRANSFER_FILE, fFileText.getText());
				fProperties.put(TRANSFER_START, fStartText.getText());
				fProperties.put(TRANSFER_SCROLL_TO_START, fScrollToBeginningOnImportComplete.getSelection());
				
				fStartAddress = getStartAddress();
				fInputFile = getFile();
				fScrollToStart = getScrollToStart();
				
				super.dispose();
			}
		};
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);
		
//		// restore to file address
//		
//		fComboRestoreToFileAddress = new Button(composite, SWT.RADIO);
//		fComboRestoreToFileAddress.setText("Restore to address specified in the file");
//		//comboRestoreToFileAddress.setLayoutData(data);
//		
//		// restore to this address
//		
//		fComboRestoreToThisAddress = new Button(composite, SWT.RADIO);
//		fComboRestoreToThisAddress.setText("Restore to this address: "); 
		FormData data = new FormData();
//		data.top = new FormAttachment(fComboRestoreToFileAddress);
//		fComboRestoreToThisAddress.setLayoutData(data);
		
		Label labelStartText = new Label(composite, SWT.NONE);
		labelStartText.setText(Messages.getString("PlainTextImporter.RestoreAddress")); //$NON-NLS-1$
		
		fStartText = new Text(composite, SWT.BORDER);
		data = new FormData();
//		data.top = new FormAttachment(fComboRestoreToFileAddress);
		data.left = new FormAttachment(labelStartText);
		data.width = 100;
		fStartText.setLayoutData(data);
		
		// file
		
		Label fileLabel = new Label(composite, SWT.NONE);
		fFileText = new Text(composite, SWT.BORDER);
		Button fileButton = new Button(composite, SWT.PUSH);
		
		fileLabel.setText(Messages.getString("Importer.File"));  //$NON-NLS-1$
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

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
				dialog.setText(Messages.getString("PlainTextImporter.ChooseFile")); //$NON-NLS-1$
				dialog.setFilterExtensions(new String[] { "*.*;*" } ); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { Messages.getString("Importer.AllFiles") } ); //$NON-NLS-1$
				dialog.setFileName(fFileText.getText());
				dialog.open();
			
				String filename = dialog.getFileName();
				if(filename != null && filename.length() != 0 )
				{
					fFileText.setText(dialog.getFilterPath() + File.separator + filename);
				}
				
				validate();
			}
			
		});
		
		fStartText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean valid = true;
				try
				{
					getStartAddress();
				}
				catch(Exception ex)
				{
					valid = false;
				}
				
				fStartText.setForeground(valid ? Display.getDefault().getSystemColor(SWT.COLOR_BLACK) : 
					Display.getDefault().getSystemColor(SWT.COLOR_RED));
				
				//

				validate();
			}
			
		});
		fFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		fScrollToBeginningOnImportComplete = new Button(composite, SWT.CHECK);
		fScrollToBeginningOnImportComplete.setText(Messages.getString("PlainTextImporter.ScrollToStart")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fileButton);
		fScrollToBeginningOnImportComplete.setLayoutData(data);
		final boolean scrollToStart = fProperties.getBoolean(TRANSFER_SCROLL_TO_START); 
		fScrollToBeginningOnImportComplete.setSelection(scrollToStart);
		
		composite.pack();
		parent.pack();
		
		Display.getDefault().asyncExec(new Runnable(){
			public void run()
			{
				validate();
			}
		});

		return composite;
	}
	
	private void validate()
	{
		boolean isValid = true;
		
		try
		{
			getStartAddress();
			if(!getFile().exists())
				isValid = false;
		}
		catch(Exception e)
		{
			isValid = false;
		}
		
		fParentDialog.setValid(isValid);	
	}
	
	public boolean getScrollToStart()
	{
		return fScrollToBeginningOnImportComplete.getSelection();
	}
	
	public BigInteger getStartAddress()
	{
		String text = fStartText.getText();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return startAddress;
	}
	
	public File getFile()
	{
		return new File(fFileText.getText());
	}
	
	public String getId()
	{
		return "PlainTextImporter"; //$NON-NLS-1$
	}
	
	public String getName()
	{
		return Messages.getString("PlainTextImporter.Name");	 //$NON-NLS-1$
	}
	
	public void importMemory() {
		Job job = new Job("Memory Import from Plain Text File"){ //$NON-NLS-1$
			
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try
				{	
					BufferedMemoryWriter memoryWriter = new BufferedMemoryWriter((IMemoryBlockExtension) fMemoryBlock, BUFFER_LENGTH);
					
					BigInteger scrollToAddress = null;
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fInputFile)));
					
					BigInteger jobs = BigInteger.valueOf(fInputFile.length());
					BigInteger factor = BigInteger.ONE;
					if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
					{
						factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
						jobs = jobs.divide(factor);
					}
						
					monitor.beginTask(Messages.getString("Importer.ProgressTitle"), jobs.intValue()); //$NON-NLS-1$
					
					BigInteger recordAddress = fStartAddress;
					String line = reader.readLine();
					int lineNo = 1; // line error reporting
					while(line != null && !monitor.isCanceled())
					{
						StringTokenizer st = new StringTokenizer(line, " "); //$NON-NLS-1$
						int bytesRead = 0;
						while(st.hasMoreElements())
						{
							String valueString = (String) st.nextElement();
							int position = 0;
							byte data[] = new byte[valueString.length() / 2];
							for(int i = 0; i < data.length; i++)
							{
								try {
									data[i] = new BigInteger(valueString.substring(position++, position++ + 1), 16).byteValue();
								} catch (NumberFormatException ex) {
									return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
									    	DebugException.REQUEST_FAILED, String.format(Messages.getString("PlainTextImporter.ErrInvalidFormat"), lineNo ), ex); //$NON-NLS-1$
								}
							}
							
							if(scrollToAddress == null)
								scrollToAddress = recordAddress;
							
							BigInteger writeAddress = 
								
								recordAddress.subtract(((IMemoryBlockExtension)fMemoryBlock).getBigBaseAddress()).add(BigInteger.valueOf(bytesRead));
							
							memoryWriter.write(writeAddress, data);
							
							bytesRead += data.length;
						}
						
						recordAddress = recordAddress.add(BigInteger.valueOf(bytesRead));
						
						BigInteger jobCount = BigInteger.valueOf(bytesRead).divide(factor);
						monitor.worked(jobCount.intValue());

						line = reader.readLine();
						lineNo++;
					}

					if (!monitor.isCanceled())
						memoryWriter.flush();
					
					reader.close();
					monitor.done();
					
					if (fScrollToStart)
						fParentDialog.scrollRenderings(scrollToAddress);
				} catch (IOException ex) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED, Messages.getString("Importer.ErrReadFile"), ex));  //$NON-NLS-1$
					return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					    	DebugException.REQUEST_FAILED, Messages.getString("Importer.ErrReadFile"), ex);  //$NON-NLS-1$
					
				} catch (DebugException ex) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED, Messages.getString("Importer.ErrWriteTarget"), ex));	  //$NON-NLS-1$
					return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					    	DebugException.REQUEST_FAILED, Messages.getString("Importer.ErrWriteTarget"), ex);						  //$NON-NLS-1$
				} catch (Exception ex) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.INTERNAL_ERROR, Messages.getString("Importer.FalureImporting"), ex));  //$NON-NLS-1$
					return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					    	DebugException.INTERNAL_ERROR, Messages.getString("Importer.FalureImporting"), ex);  //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}};
		job.setUser(true);
		job.schedule();
	}
}
