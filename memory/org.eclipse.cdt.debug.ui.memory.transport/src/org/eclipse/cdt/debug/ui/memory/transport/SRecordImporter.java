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

public class SRecordImporter implements IMemoryImporter {

	File fInputFile;
	BigInteger fStartAddress;
	Boolean fScrollToStart;
	
	private Text fStartText;
	private Text fFileText;
	
	private Button fComboRestoreToThisAddress;
	private Button fComboRestoreToFileAddress;
	
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
	
		Composite composite = new Composite(parent, SWT.NONE)
		{
			public void dispose()
			{
				fProperties.put(TRANSFER_FILE, fFileText.getText());
				fProperties.put(TRANSFER_START, fStartText.getText());
				fProperties.put(TRANSFER_SCROLL_TO_START, fScrollToBeginningOnImportComplete.getSelection());
				fProperties.put(TRANSFER_CUSTOM_START_ADDRESS, fComboRestoreToThisAddress.getSelection());
				
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
		
		// restore to file address
		
		fComboRestoreToFileAddress = new Button(composite, SWT.RADIO);
		fComboRestoreToFileAddress.setSelection(true);
		fComboRestoreToFileAddress.setText(Messages.getString("SRecordImporter.FileAddressRestore"));  //$NON-NLS-1$
		fComboRestoreToFileAddress.setSelection(!fProperties.getBoolean(TRANSFER_CUSTOM_START_ADDRESS)); 
		//comboRestoreToFileAddress.setLayoutData(data);
		
		// restore to this address
		
		fComboRestoreToThisAddress = new Button(composite, SWT.RADIO);
		fComboRestoreToThisAddress.setText(Messages.getString("SRecordImporter.CustomAddressRestore"));   //$NON-NLS-1$
		fComboRestoreToThisAddress.setSelection(fProperties.getBoolean(TRANSFER_CUSTOM_START_ADDRESS)); 
		FormData data = new FormData();
		data.top = new FormAttachment(fComboRestoreToFileAddress);
		fComboRestoreToThisAddress.setLayoutData(data);
		
		fStartText = new Text(composite, SWT.BORDER);
		data = new FormData();
		data.top = new FormAttachment(fComboRestoreToFileAddress);
		data.left = new FormAttachment(fComboRestoreToThisAddress);
		data.width = 100;
		fStartText.setLayoutData(data);
		
		fComboRestoreToFileAddress.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});
		
		fComboRestoreToThisAddress.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});
		
		// file
		
		Label fileLabel = new Label(composite, SWT.NONE);
		fFileText = new Text(composite, SWT.BORDER);
		Button fileButton = new Button(composite, SWT.PUSH);
		
		fileLabel.setText(Messages.getString("Importer.File"));   //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		data.left = new FormAttachment(fileLabel);
		data.width = 300;
		fFileText.setLayoutData(data);
		
		fileButton.setText(Messages.getString("Importer.Browse"));  //$NON-NLS-1$
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
				dialog.setText(Messages.getString("SRecordImporter.ChooseFile"));  //$NON-NLS-1$
				dialog.setFilterExtensions(new String[] { "*.*;*" } ); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { Messages.getString("Importer.AllFiles") } );  //$NON-NLS-1$
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
		fScrollToBeginningOnImportComplete.setText(Messages.getString("SRecordImporter.ScrollToStart"));  //$NON-NLS-1$
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
			boolean restoreToAddress = fComboRestoreToThisAddress.getSelection();
			if ( restoreToAddress ) {
				getStartAddress();
			}
			
			boolean restoreToAddressFromFile = fComboRestoreToFileAddress.getSelection();
			if ( restoreToAddressFromFile ) {
				if(!getFile().exists()) {
					isValid = false;
				}
			}
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
		return "srecord"; //$NON-NLS-1$
	}
	
	public String getName()
	{
		return Messages.getString("SRecordImporter.Name"); //$NON-NLS-1$
	}
	
	public void importMemory() {
		Job job = new Job("Memory Import from S-Record File"){ //$NON-NLS-1$
			
			public IStatus run(IProgressMonitor monitor) {
				
				try
				{	
					BufferedMemoryWriter memoryWriter = new BufferedMemoryWriter((IMemoryBlockExtension) fMemoryBlock, BUFFER_LENGTH);
					
					// FIXME 4 byte default
					
					final int CHECKSUM_LENGTH = 1;
					
					BigInteger scrollToAddress = null;
					
					BigInteger offset = null;
					if(!fProperties.getBoolean(TRANSFER_CUSTOM_START_ADDRESS))  
						offset = BigInteger.ZERO;
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fInputFile)));
					
					BigInteger jobs = BigInteger.valueOf(fInputFile.length());
					BigInteger factor = BigInteger.ONE;
					if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
					{
						factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
						jobs = jobs.divide(factor);
					}
						
					monitor.beginTask(Messages.getString("Importer.ProgressTitle"), jobs.intValue());  //$NON-NLS-1$
					
					String line = reader.readLine();
					int lineNo = 1; // line error reporting
					while(line != null && !monitor.isCanceled())
					{
						String recordType = line.substring(0, 2);
						int recordCount = 0;
						try {
							recordCount = Integer.parseInt(line.substring(2, 4), 16);
						} catch (NumberFormatException ex) {
							return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							    	DebugException.REQUEST_FAILED, String.format(Messages.getString("SRecordImporter.InvalidLineLength"), lineNo ), ex);  //$NON-NLS-1$
						}

						int bytesRead = 4 + recordCount;
						int position = 4;
						int addressSize = 0;
						
						BigInteger recordAddress = null;
			
						if("S3".equals(recordType))  //$NON-NLS-1$
							addressSize = 4;
						else if("S1".equals(recordType))  //$NON-NLS-1$
							addressSize = 2;
						else if("S2".equals(recordType))  //$NON-NLS-1$
							addressSize = 3;
						else if("S0".equals(recordType) || "S5".equals(recordType) ||"S7".equals(recordType) ||
								"S8".equals(recordType) || "S9".equals(recordType) )	//$NON-NLS-1$
						{	// ignore S0, S5, S7, S8 and S9 records
							line = reader.readLine();
							lineNo++;
							continue; 
						}
						try {
							recordAddress = new BigInteger(line.substring(position, position + addressSize * 2), 16);
						} catch (NumberFormatException ex) {
							return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							    	DebugException.REQUEST_FAILED, String.format(Messages.getString("SRecordImporter.InvalidAddress"), lineNo ), ex);  //$NON-NLS-1$
						}
						recordCount -= addressSize;
						position += addressSize * 2;
						
						if(offset == null)
							offset = fStartAddress.subtract(recordAddress);
						
						recordAddress = recordAddress.add(offset);
						
						byte data[] = new byte[recordCount - CHECKSUM_LENGTH];
						for(int i = 0; i < data.length; i++)
						{
							try {
								data[i] = new BigInteger(line.substring(position++, position++ + 1), 16).byteValue();
							} catch (NumberFormatException ex) {
								return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
								    	DebugException.REQUEST_FAILED, String.format(Messages.getString("SRecordImporter.InvalidData"), lineNo ), ex);  //$NON-NLS-1$
							}
						}

						/*
						 * The least significant byte of the one's complement of the sum of the values
                         * represented by the pairs of characters making up the records length, address,
                         * and the code/data fields.
						 */
						StringBuffer buf = new StringBuffer(line.substring(2));
						byte checksum = 0;
						
						for(int i = 0; i < buf.length(); i+=2)
						{
							BigInteger value = null;
							try {
								value = new BigInteger(buf.substring(i, i+2), 16);
							} catch (NumberFormatException ex) {
								return new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
								    	DebugException.REQUEST_FAILED, String.format(Messages.getString("SRecordImporter.InvalidChecksum"), lineNo ), ex);  //$NON-NLS-1$
							}
							checksum += value.byteValue();
						}
						
						/*
						 * Since we included the checksum in the checksum calculation the checksum
						 * ( if correct ) will always be 0xFF which is -1 using the signed byte size
						 * calculation here.
						 */
						if ( checksum != (byte) -1 ) {
							reader.close();
							monitor.done();
							return new Status( IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(), Messages.getString("SRecordImporter.ChecksumFalure") + line);  //$NON-NLS-1$
						}
						
						if(scrollToAddress == null)
							scrollToAddress = recordAddress;
						
						// FIXME error on incorrect checksum
						
						memoryWriter.write(recordAddress.subtract(((IMemoryBlockExtension) fMemoryBlock).getBigBaseAddress()), data);

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
