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

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.debug.ui.memory.transport.model.IMemoryExporter;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
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

public class PlainTextExporter implements IMemoryExporter {

	File fOutputFile;
	BigInteger fStartAddress;
	BigInteger fEndAddress;
	
	private Text fStartText;
	private Text fEndText;
	private Text fLengthText;
	private Text fFileText;
	
	private IMemoryBlock fMemoryBlock;
	
	private ExportMemoryDialog fParentDialog;
	
	private Properties fProperties;
	
	public Control createControl(final Composite parent, IMemoryBlock memBlock, Properties properties, ExportMemoryDialog parentDialog)
	{
		fMemoryBlock = memBlock;
		fParentDialog = parentDialog;
		fProperties = properties;
	
		Composite composite = new Composite(parent, SWT.NONE)
		{
			public void dispose()
			{
				fProperties.setProperty(TRANSFER_FILE, fFileText.getText());
				fProperties.setProperty(TRANSFER_START, fStartText.getText());
				fProperties.setProperty(TRANSFER_END, fEndText.getText());
				
				fStartAddress = getStartAddress();
				fEndAddress = getEndAddress();
				fOutputFile = getFile();
				
				super.dispose();
			}
		};
		
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);
		
		// start address
		
		Label startLabel = new Label(composite, SWT.NONE);
		startLabel.setText("Start address: "); 
		FormData data = new FormData();
		startLabel.setLayoutData(data);
		
		fStartText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.left = new FormAttachment(startLabel);
		data.width = 100;
		fStartText.setLayoutData(data);
		
		// end address
		
		Label endLabel = new Label(composite, SWT.NONE);
		endLabel.setText("End address: "); 
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(fStartText);
		endLabel.setLayoutData(data);
		
		fEndText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(endLabel);
		data.width = 100;
		fEndText.setLayoutData(data);
		
		// length
		
		Label lengthLabel = new Label(composite, SWT.NONE);
		lengthLabel.setText("Length: "); 
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(fEndText);
		lengthLabel.setLayoutData(data);
		
		fLengthText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(fStartText, 0, SWT.CENTER);
		data.left = new FormAttachment(lengthLabel);
		data.width = 100;
		fLengthText.setLayoutData(data);
		
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
		data.top = new FormAttachment(fLengthText);
		data.left = new FormAttachment(fFileText);
		fileButton.setLayoutData(data);
		

		fFileText.setText(properties.getProperty(TRANSFER_FILE, ""));
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
			
			if(properties.getProperty(TRANSFER_END) != null)
				fEndText.setText(properties.getProperty(TRANSFER_END));
			else
				fEndText.setText("0x" + startAddress.toString(16));
			
			fLengthText.setText(getEndAddress().subtract(getStartAddress()).toString());
		}
		catch(Exception e)
		{
			MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
		    	DebugException.INTERNAL_ERROR, "Failure", e));
		}
		
		fileButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
				dialog.setText("Choose memory export file");
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
		
		fStartText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
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
				
				BigInteger endAddress = getEndAddress();
				BigInteger startAddress = getStartAddress();

				fLengthText.setText(endAddress.subtract(startAddress).toString());
				
				validate();
			}
			
			public void keyPressed(KeyEvent e) {}
		});
		
		fEndText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				try
				{
					getEndAddress();
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					
					BigInteger endAddress = getEndAddress();
					BigInteger startAddress = getStartAddress();

					String lengthString = endAddress.subtract(startAddress).toString();
					
					if(!fLengthText.getText().equals(lengthString))
						fLengthText.setText(lengthString);
				}
				catch(Exception ex)
				{
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
				
				validate();
			}
			
			public void keyPressed(KeyEvent e) {}
			
		});
		
		fLengthText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				try
				{
					BigInteger length = getLength();
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					BigInteger startAddress = getStartAddress();
					String endString = "0x" + startAddress.add(length).toString(16);
					if(!fEndText.getText().equals(endString))
						fEndText.setText(endString);
				}
				catch(Exception ex)
				{
					fLengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
		
				validate();
			}
			
			

			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		fFileText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				validate();
			}
			
			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		composite.pack();
		
		return composite;
	}

	public BigInteger getEndAddress()
	{
		String text = fEndText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger endAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return endAddress;
	}
	
	public BigInteger getStartAddress()
	{
		String text = fStartText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return startAddress;
	}
	
	public BigInteger getLength()
	{
		String text = fLengthText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger lengthAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return lengthAddress;
	}
	
	public File getFile()
	{
		return new File(fFileText.getText());
	}
	
	private void validate()
	{
		boolean isValid = true;
		
		try
		{
			getEndAddress();
			
			getStartAddress();
			
			BigInteger length = getLength();
			
			if(length.compareTo(BigInteger.ZERO) <= 0)
				isValid = false;
			
			if(!getFile().getParentFile().exists())
				isValid = false;
		}
		catch(Exception e)
		{
			isValid = false;
		}
		
		fParentDialog.setValid(isValid);
			
	}

	public String getId()
	{
		return "PlainTextExporter";
	}
	
	public String getName()
	{
		return "Plain Text";
	}
	
	public void exportMemory() {
		Job job = new Job("Memory Export to Plain Text File"){ //$NON-NLS-1$
			public IStatus run(IProgressMonitor monitor) {
				
				try
				{
					try
					{	
						// FIXME 4 byte default
						
						BigInteger CELLSIZE = BigInteger.valueOf(4);
						
						BigInteger COLUMNS = BigInteger.valueOf(5); // FIXME
						
						BigInteger DATA_PER_LINE = CELLSIZE.multiply(COLUMNS);
						
						BigInteger transferAddress = fStartAddress;
						
						FileWriter writer = new FileWriter(fOutputFile);
						
						BigInteger jobs = fEndAddress.subtract(transferAddress).divide(DATA_PER_LINE);
						BigInteger factor = BigInteger.ONE;
						if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
						{
							factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
							jobs = jobs.divide(factor);
						}
							
						monitor.beginTask("Transferring Data", jobs.intValue());
						
						BigInteger jobCount = BigInteger.ZERO;
						while(transferAddress.compareTo(fEndAddress) < 0 && !monitor.isCanceled())
						{
							BigInteger length = DATA_PER_LINE;
							if(fEndAddress.subtract(transferAddress).compareTo(length) < 0)
								length = fEndAddress.subtract(transferAddress);
							
							StringBuffer buf = new StringBuffer();
							
//							String transferAddressString = transferAddress.toString(16);
							
							// future option
//							for(int i = 0; i < 8 - transferAddressString.length(); i++)
//								buf.append("0");
//							buf.append(transferAddressString);
//							buf.append(" "); // TODO tab?
							
							// data
							
							for(int i = 0; i < length.divide(CELLSIZE).intValue(); i++)
							{
								if(i != 0)
									buf.append(" ");
								MemoryByte bytes[] = ((IMemoryBlockExtension) fMemoryBlock).getBytesFromAddress(
									transferAddress.add(CELLSIZE.multiply(BigInteger.valueOf(i))), 
									CELLSIZE.longValue() / ((IMemoryBlockExtension) fMemoryBlock).getAddressableSize());
								for(int byteIndex = 0; byteIndex < bytes.length; byteIndex++)
								{
									String bString = BigInteger.valueOf(0xFF & bytes[byteIndex].getValue()).toString(16);
									if(bString.length() == 1)
										buf.append("0");
									buf.append(bString);
								}
							}
							
							writer.write(buf.toString().toUpperCase());
							writer.write("\n");
							
							transferAddress = transferAddress.add(length);
							
							jobCount = jobCount.add(BigInteger.ONE);
							if(jobCount.compareTo(factor) == 0)
							{
								jobCount = BigInteger.ZERO;
								monitor.worked(1);
							}
 						}
						
						writer.close();
						monitor.done();
					}
					catch(Exception e) 
					{ 
						MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					    	DebugException.INTERNAL_ERROR, "Failure", e));
					}
				}
				catch(Exception e) 
				{
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
				    	DebugException.INTERNAL_ERROR, "Failure", e));
				}
				return Status.OK_STATUS;
			}};
		job.setUser(true);
		job.schedule();
	}
}
