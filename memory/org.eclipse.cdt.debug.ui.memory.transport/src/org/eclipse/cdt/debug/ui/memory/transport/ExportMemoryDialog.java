/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport;

import java.util.Properties;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

	private Combo fFormatCombo;
	
	private IMemoryBlock fMemoryBlock;
	
	private Control fCurrentControl = null;
	
	private IMemoryExporter fFormatExporters[];
	private String fFormatNames[];
	
	private Properties fProperties = new Properties();
	
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
		if(fCurrentControl != null)
			fCurrentControl.dispose();
		fFormatExporters[fFormatCombo.getSelectionIndex()].exportMemory();
		
		super.okPressed();
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MemoryTransportPlugin.getUniqueIdentifier() + ".ExportMemoryDialog_context"); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);
		
		// format
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Format: "); 
		
		fFormatCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		
		FormData data = new FormData();
		data.top = new FormAttachment(fFormatCombo, 0, SWT.CENTER);
		textLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(textLabel);
		fFormatCombo.setLayoutData(data);
		
		Vector exporters = new Vector();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
	         registry.getExtensionPoint("org.eclipse.cdt.debug.ui.memory.transport.memoryTransport");
	    IConfigurationElement points[] =
	         extensionPoint.getConfigurationElements();
	     
		for (int i = 0; i < points.length; i++) 
		{
			IConfigurationElement element = points[i];
			if("exporter".equals(element.getName()))
			{
				try 
				{
					exporters.addElement((IMemoryExporter) element.createExecutableExtension("class"));
				}
				catch(Exception e) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
			    		DebugException.INTERNAL_ERROR, "Failure", e));
				}
			}
		}
		
        fFormatExporters = new IMemoryExporter[exporters.size()];
		fFormatNames = new String[exporters.size()];
		for(int i = 0; i < fFormatExporters.length; i++)
		{
			fFormatExporters[i] = (IMemoryExporter) exporters.elementAt(i);
			fFormatNames[i] = ((IMemoryExporter) exporters.elementAt(i)).getName();
		}
		
		final Composite container = new Composite(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(fFormatCombo);
		data.left = new FormAttachment(0);
		container.setLayoutData(data);
		
		fFormatCombo.setItems(fFormatNames);
		
		fFormatCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				if(fCurrentControl != null)
					fCurrentControl.dispose();
				fCurrentControl = fFormatExporters[fFormatCombo.getSelectionIndex()].createControl(container, 
					fMemoryBlock, fProperties, ExportMemoryDialog.this);
			}
		});
		
		
		fFormatCombo.select(0);
		fCurrentControl = fFormatExporters[0].createControl(container, 
				fMemoryBlock, fProperties, ExportMemoryDialog.this);
		
		return composite;
	}

	public void setValid(boolean isValid)
	{
		getButton(IDialogConstants.OK_ID).setEnabled(isValid);
	}
	
}
