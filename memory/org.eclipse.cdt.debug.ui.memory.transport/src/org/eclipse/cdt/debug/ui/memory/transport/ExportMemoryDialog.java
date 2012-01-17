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

import java.math.BigInteger;
import java.util.Vector;

import org.eclipse.cdt.debug.ui.memory.transport.model.IMemoryExporter;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ExportMemoryDialog extends SelectionDialog 
{

	private static final String EXPORT_SETTINGS = "EXPORT_DIALOG"; //$NON-NLS-1$

	private static final String SELECTED_EXPORTER = "SELECTED_EXPORTER"; //$NON-NLS-1$

	private Combo fFormatCombo;
	
	private IMemoryBlock fMemoryBlock;
	
	private Control fCurrentControl = null;
	
	private IMemoryExporter fFormatExporters[];
	private String fFormatNames[];
	
	private IDialogSettings fProperties = MemoryTransportPlugin.getDefault().getDialogSettings(EXPORT_SETTINGS);
	
	private final String INITIAL_ADDRESS = "Initial address";

	public ExportMemoryDialog(Shell parent, IMemoryBlock memoryBlock, BigInteger initialStartAddr)
	{
		super(parent);
		super.setTitle(Messages.getString("ExportMemoryDialog.Title"));  //$NON-NLS-1$
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
		
		String addrstr = "0x" + initialStartAddr.toString(16); //$NON-NLS-1$
		
		String initialAddress = fProperties.get(INITIAL_ADDRESS);
		
		if ( initialAddress == null ) {
			fProperties.put(IMemoryExporter.TRANSFER_START, addrstr);
			fProperties.put(IMemoryExporter.TRANSFER_END, addrstr);
			fProperties.put(INITIAL_ADDRESS , addrstr);
		} 
		else {
			if ( ! initialAddress.equals(addrstr) ) {
				fProperties.put(IMemoryExporter.TRANSFER_START, addrstr);
				fProperties.put(IMemoryExporter.TRANSFER_END, addrstr);
				fProperties.put(INITIAL_ADDRESS , addrstr);
			}
			else {
				String startAddr = fProperties.get(IMemoryExporter.TRANSFER_START);
				String endAddr   = fProperties.get(IMemoryExporter.TRANSFER_END);

				if ( startAddr == null || endAddr == null ) {
					fProperties.put(IMemoryExporter.TRANSFER_START, addrstr);
					fProperties.put(IMemoryExporter.TRANSFER_END, addrstr);
					fProperties.put(INITIAL_ADDRESS , addrstr);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getResult()
	 */
	@Override
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
	@Override
	protected void cancelPressed() {
		
		setResult(null);
		
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if(fCurrentControl != null)
			fCurrentControl.dispose();
		IMemoryExporter currentExporter = getCurrentExporter();
		currentExporter.exportMemory();
	
		fProperties.put(SELECTED_EXPORTER, currentExporter.getId());
	
		super.okPressed();
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MemoryTransportPlugin.getUniqueIdentifier() + ".ExportMemoryDialog_context"); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);
		
		// format
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(Messages.getString("ExportMemoryDialog.Format"));  //$NON-NLS-1$
		
		fFormatCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		
		FormData data = new FormData();
		data.top = new FormAttachment(fFormatCombo, 0, SWT.CENTER);
		textLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(textLabel);
		fFormatCombo.setLayoutData(data);
		
		Vector<IMemoryExporter> exporters = new Vector<IMemoryExporter>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
	         registry.getExtensionPoint("org.eclipse.cdt.debug.ui.memory.transport.memoryTransport"); //$NON-NLS-1$
	    IConfigurationElement points[] =
	         extensionPoint.getConfigurationElements();
	     
		for (int i = 0; i < points.length; i++) 
		{
			IConfigurationElement element = points[i];
			if("exporter".equals(element.getName())) //$NON-NLS-1$
			{
				try 
				{
					exporters.addElement((IMemoryExporter) element.createExecutableExtension("class")); //$NON-NLS-1$
				}
				catch(Exception e) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
			    		DebugException.INTERNAL_ERROR, "Failure", e)); //$NON-NLS-1$
				}
			}
		}
		
        fFormatExporters = new IMemoryExporter[exporters.size()];
		fFormatNames = new String[exporters.size()];
		for(int i = 0; i < fFormatExporters.length; i++)
		{
			fFormatExporters[i] = exporters.elementAt(i);
			fFormatNames[i] = exporters.elementAt(i).getName();
		}
		
		final Composite container = new Composite(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(fFormatCombo);
		data.left = new FormAttachment(0);
		container.setLayoutData(data);
		
		fFormatCombo.setItems(fFormatNames);
		
		fFormatCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(fCurrentControl != null) {
					fCurrentControl.dispose();
				}
				fCurrentControl = getCurrentExporter().createControl(container,	fMemoryBlock, fProperties, ExportMemoryDialog.this);
			}
		});
		
		
		setCurrentExporter(fProperties.get(SELECTED_EXPORTER));

		fCurrentControl = getCurrentExporter().createControl(container, fMemoryBlock, fProperties, ExportMemoryDialog.this);
		
		return composite;
	}

	public void setValid(boolean isValid)
	{
		getButton(IDialogConstants.OK_ID).setEnabled(isValid);
	}

	private IMemoryExporter getCurrentExporter() {
		return fFormatExporters[fFormatCombo.getSelectionIndex()];
	}
	
	private void setCurrentExporter(String id) {
		if ( id == null || id.length() == 0 ) {
			fFormatCombo.select(0);
		}
		
		for (int index = 0; index< fFormatExporters.length; ++index) {
			if (fFormatExporters[index].getId().equals(id)){
				fFormatCombo.select(index);
				return;
			}
		}
		
		fFormatCombo.select(0);
	}

}
