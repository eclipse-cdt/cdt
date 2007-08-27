/*******************************************************************************
 * Copyright (c) 2006-2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.memory.renderings.actions;

import java.io.File;
import java.math.BigInteger;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ExportMemoryDialog extends SelectionDialog 
{

	private Combo formatCombo;
	
	private IMemoryBlock fMemoryBlock;
	
	private Text startText;
	private Text endText;
	private Text lengthText;
	private Text fileText;
	
	public ExportMemoryDialog(Shell parent, IMemoryBlock memoryBlock)
	{
		super(parent);
		super.setTitle("Export Memory"); 
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getResult()
	 */
	public Object[] getResult() {
		
		Object[] results = super.getResult();
		
		if (results != null)
		{	
			return results;
		}
        return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		
		setResult(null);
		
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		setSelectionResult(new Object[]{ getFormat(), getStartAddress(), getEndAddress(), getLength(), getFile() });
		
		super.okPressed();
	}
	
	public String getFormat()
	{
		return formatCombo.getItem(formatCombo.getSelectionIndex());
	}
	
	public BigInteger getEndAddress()
	{
		String text = endText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger endAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return endAddress;
	}
	
	public BigInteger getStartAddress()
	{
		String text = startText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return startAddress;
	}
	
	public BigInteger getLength()
	{
		String text = lengthText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger lengthAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return lengthAddress;
	}
	
	public File getFile()
	{
		return new File(fileText.getText());
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
		
		getButton(IDialogConstants.OK_ID).setEnabled(isValid);
			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, DebugUIPlugin.getUniqueIdentifier() + ".AddMemoryRenderingDialog_context"); //$NON-NLS-1$ // FIXME
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);
		
		// format
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Format: "); 
		
		formatCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		
		FormData data = new FormData();
		data.top = new FormAttachment(formatCombo, 0, SWT.CENTER);
		textLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(textLabel);
		formatCombo.setLayoutData(data);
		formatCombo.setItems( new String[] { "S-Record" }); // TODO offer extension point
		formatCombo.select(0);
		
		// start address
		
		Label startLabel = new Label(composite, SWT.NONE);
		startLabel.setText("Start address: "); 
		data = new FormData();
		data.top = new FormAttachment(formatCombo);
		startLabel.setLayoutData(data);
		
		startText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(formatCombo);
		data.left = new FormAttachment(startLabel);
		data.width = 100;
		startText.setLayoutData(data);
		
		// end address
		
		Label endLabel = new Label(composite, SWT.NONE);
		endLabel.setText("End address: "); 
		data = new FormData();
		data.top = new FormAttachment(startText, 0, SWT.CENTER);
		data.left = new FormAttachment(startText);
		endLabel.setLayoutData(data);
		
		endText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(startText, 0, SWT.CENTER);
		data.left = new FormAttachment(endLabel);
		data.width = 100;
		endText.setLayoutData(data);
		
		// length
		
		Label lengthLabel = new Label(composite, SWT.NONE);
		lengthLabel.setText("Length: "); 
		data = new FormData();
		data.top = new FormAttachment(startText, 0, SWT.CENTER);
		data.left = new FormAttachment(endText);
		lengthLabel.setLayoutData(data);
		
		lengthText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(startText, 0, SWT.CENTER);
		data.left = new FormAttachment(lengthLabel);
		data.width = 100;
		lengthText.setLayoutData(data);
		
		// file
		
		Label fileLabel = new Label(composite, SWT.NONE);
		fileText = new Text(composite, SWT.NONE);
		Button fileButton = new Button(composite, SWT.PUSH);
		
		fileLabel.setText("File name: "); 
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		data.left = new FormAttachment(fileLabel);
		data.width = 300;
		fileText.setLayoutData(data);
		
		fileButton.setText("Browse...");
		data = new FormData();
		data.top = new FormAttachment(lengthText);
		data.left = new FormAttachment(fileText);
		fileButton.setLayoutData(data);
		
		try
		{
			BigInteger startAddress = null;
			if(fMemoryBlock instanceof IMemoryBlockExtension)
				startAddress = ((IMemoryBlockExtension) fMemoryBlock)
					.getBigBaseAddress(); // FIXME use selection/caret address?
			else
				startAddress = BigInteger.valueOf(fMemoryBlock.getStartAddress());
			
			startText.setText("0x" + startAddress.toString(16));
			endText.setText("0x" + startAddress.toString(16));
			lengthText.setText("0");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// TODO
		}
		
		fileButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(ExportMemoryDialog.this.getShell(), SWT.SAVE);
				dialog.setText("Choose memory export file");
				dialog.setFilterExtensions(new String[] { "*.*" } );
				dialog.setFilterNames(new String[] { "All Files (*.*)" } );
				dialog.setFileName(fileText.getText());
				dialog.open();
			
				if(dialog.getFileName() != null)
				{
					fileText.setText(dialog.getFilterPath() + File.separator + dialog.getFileName());
				}
				
				validate();
			}
			
		});
		
		startText.addModifyListener(new ModifyListener() {
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
				
				startText.setForeground(valid ? Display.getDefault().getSystemColor(SWT.COLOR_BLACK) : 
					Display.getDefault().getSystemColor(SWT.COLOR_RED));
				
				//
				
				BigInteger endAddress = getEndAddress();
				BigInteger startAddress = getStartAddress();

				lengthText.setText(endAddress.subtract(startAddress).toString());
				
				validate();
			}
			
		});
		
		endText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try
				{
					getEndAddress();
					endText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					
					BigInteger endAddress = getEndAddress();
					BigInteger startAddress = getStartAddress();

					String lengthString = endAddress.subtract(startAddress).toString();
					
					if(!lengthText.getText().equals(lengthString))
						lengthText.setText(lengthString);
				}
				catch(Exception ex)
				{
					endText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
				
				validate();
			}
			
		});
		
		lengthText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try
				{
					BigInteger length = getLength();
					lengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					BigInteger startAddress = getStartAddress();
					String endString = "0x" + startAddress.add(length).toString(16);
					if(!endText.getText().equals(endString))
						endText.setText(endString);
				}
				catch(Exception ex)
				{
					lengthText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
		
				validate();
			}
			
		});
		
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		return composite;
	}

	
}
