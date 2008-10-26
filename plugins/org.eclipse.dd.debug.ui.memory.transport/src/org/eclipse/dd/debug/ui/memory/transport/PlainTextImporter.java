/*******************************************************************************
 * Copyright (c) 2006-2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.ui.memory.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.debug.ui.memory.transport.model.IMemoryImporter;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
	
//	private Button fComboRestoreToThisAddress;
//	private Button fComboRestoreToFileAddress;
	
	private Button fScrollToBeginningOnImportComplete;
	
	private IMemoryBlock fMemoryBlock;
	
	private ImportMemoryDialog fParentDialog;
	
	private Properties fProperties;
	
	public Control createControl(final Composite parent, IMemoryBlock memBlock, Properties properties, ImportMemoryDialog parentDialog)
	{
		fMemoryBlock = memBlock;
		fParentDialog = parentDialog;
		fProperties = properties;
		
		fUseCustomAddress = true;
	
		Composite composite = new Composite(parent, SWT.NONE)
		{
			public void dispose()
			{
				fProperties.setProperty(TRANSFER_FILE, fFileText.getText());
				fProperties.setProperty(TRANSFER_START, fStartText.getText());
				fProperties.setProperty(TRANSFER_SCROLL_TO_START, fScrollToStart.toString());
				
				fStartAddress = getStartAddress();
				fInputFile = getFile();
				
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
		labelStartText.setText("Restore to address: ");
		
		fStartText = new Text(composite, SWT.NONE);
		data = new FormData();
//		data.top = new FormAttachment(fComboRestoreToFileAddress);
		data.left = new FormAttachment(labelStartText);
		data.width = 100;
		fStartText.setLayoutData(data);
		
		// file
		
		Label fileLabel = new Label(composite, SWT.NONE);
		fFileText = new Text(composite, SWT.NONE);
		Button fileButton = new Button(composite, SWT.PUSH);
		
		fileLabel.setText("File name: "); 
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		data.left = new FormAttachment(fileLabel);
		data.width = 300;
		fFileText.setLayoutData(data);
		
		fileButton.setText("Browse...");
		data = new FormData();
		data.top = new FormAttachment(fStartText);
		data.left = new FormAttachment(fFileText);
		fileButton.setLayoutData(data);
		
		fFileText.setText(properties.getProperty(TRANSFER_FILE, ""));
		fScrollToStart = new Boolean(properties.getProperty(TRANSFER_SCROLL_TO_START, "true"));
		try
		{
			BigInteger startAddress = null;
			if(fMemoryBlock instanceof IMemoryBlockExtension)
				startAddress = ((IMemoryBlockExtension) fMemoryBlock)
					.getBigBaseAddress(); // FIXME use selection/caret address?
			else
				startAddress = BigInteger.valueOf(fMemoryBlock.getStartAddress());
			
			if(properties.getProperty(TRANSFER_START) != null)
				fStartText.setText(properties.getProperty(TRANSFER_START));
			else
				fStartText.setText("0x" + startAddress.toString(16));
			
		}
		catch(Exception e)
		{
			DebugUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
		    	DebugException.INTERNAL_ERROR, "Failure", e));
		}
		
		fileButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
				dialog.setText("Choose memory import file");
				dialog.setFilterExtensions(new String[] { "*.*;*" } );
				dialog.setFilterNames(new String[] { "All Files" } );
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
		fScrollToBeginningOnImportComplete.setText("Scroll to File Start Address");
		data = new FormData();
		data.top = new FormAttachment(fileButton);
		fScrollToBeginningOnImportComplete.setLayoutData(data);
		
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
		boolean hex = text.startsWith("0x");
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
		return "PlainTextImporter";
	}
	
	public String getName()
	{
		return "Plain Text";	
	}
	
	public void importMemory() {
		Job job = new Job("Memory Import from Plain Text File"){ //$NON-NLS-1$
			
			public IStatus run(IProgressMonitor monitor) {
				
				try
				{
					try
					{	
						BigInteger offset = null;
						if(!fUseCustomAddress)
							offset = BigInteger.ZERO;
						
						BigInteger scrollToAddress = null;
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fInputFile)));
						
						BigInteger jobs = BigInteger.valueOf(fInputFile.length());
						BigInteger factor = BigInteger.ONE;
						if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
						{
							factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
							jobs = jobs.divide(factor);
						}
							
						monitor.beginTask("Transferring Data", jobs.intValue()); //$NON-NLS-1$
						
						BigInteger jobCount = BigInteger.ZERO;
						BigInteger recordAddress = fStartAddress;
						String line = reader.readLine();
						while(line != null && !monitor.isCanceled())
						{
							StringTokenizer st = new StringTokenizer(line, " ");
							int bytesRead = 0;
							while(st.hasMoreElements())
							{
								String valueString = (String) st.nextElement();
								int position = 0;
								byte data[] = new byte[valueString.length() / 2];
								for(int i = 0; i < data.length; i++)
								{
									data[i] = new BigInteger(valueString.substring(position++, position++ + 1), 16).byteValue();
								}
								
								if(scrollToAddress == null)
									scrollToAddress = recordAddress;
								
								((IMemoryBlockExtension) fMemoryBlock).setValue(recordAddress.subtract(((IMemoryBlockExtension) 
									fMemoryBlock).getBigBaseAddress()).add(BigInteger.valueOf(bytesRead)), data);
								
								bytesRead += data.length;
							}
							
							recordAddress = recordAddress.and(BigInteger.valueOf(bytesRead));
							
							jobCount = jobCount.add(BigInteger.valueOf(bytesRead));
							while(jobCount.compareTo(factor) >= 0)
							{
								jobCount = jobCount.subtract(factor);
								monitor.worked(1);
							}
							
							line = reader.readLine();
 						}
						
						reader.close();
						monitor.done();
						
						if(fProperties.getProperty(TRANSFER_SCROLL_TO_START, "false").equals("true"))
							fParentDialog.scrollRenderings(scrollToAddress);
					}
					catch(Exception e) 
					{ 
						DebugUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					    	DebugException.INTERNAL_ERROR, "Failure", e));	
					}
				}
				catch(Exception e) {e.printStackTrace();}
				return Status.OK_STATUS;
			}};
		job.setUser(true);
		job.schedule();
	}
}
