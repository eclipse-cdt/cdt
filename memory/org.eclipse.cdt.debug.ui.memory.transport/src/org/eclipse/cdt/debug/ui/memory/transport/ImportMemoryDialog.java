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
import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.cdt.debug.ui.memory.transport.model.IMemoryImporter;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.progress.UIJob;

public class ImportMemoryDialog extends SelectionDialog 
{

	private static final String IMPORT_SETTINGS = "IMPORT_DIALOG"; //$NON-NLS-1$

	private static final String SELECTED_IMPORTER = "SELECTED_IMPORTER"; //$NON-NLS-1$

	private Combo fFormatCombo;
	
	private IMemoryBlock fMemoryBlock;
	
	private Control fCurrentControl = null;
	
	private IMemoryImporter fFormatImporters[];
	private String fFormatNames[];
	
	private IDialogSettings fProperties = MemoryTransportPlugin.getDefault().getDialogSettings(IMPORT_SETTINGS);
	
	private IMemoryRenderingSite fMemoryView;
	
	private final String INITIAL_ADDRESS = "Initial address";
	
	public ImportMemoryDialog(Shell parent, IMemoryBlock memoryBlock, BigInteger initialStartAddr, IMemoryRenderingSite renderingSite)
	{
		super(parent);
		super.setTitle(Messages.getString("ImportMemoryDialog.Title"));   //$NON-NLS-1$
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
		fMemoryView = renderingSite;
		
		String initialAddress = fProperties.get(INITIAL_ADDRESS);
		
		if ( initialAddress == null ) {
			String addrstr = "0x" + initialStartAddr.toString(16); //$NON-NLS-1$
			fProperties.put(IMemoryImporter.TRANSFER_START, addrstr);
			fProperties.put(INITIAL_ADDRESS , addrstr);
		} 
		else {
			String addrstr = "0x" + initialStartAddr.toString(16); //$NON-NLS-1$
			
			if ( ! initialAddress.equals(addrstr) ) {
				fProperties.put(IMemoryImporter.TRANSFER_START, addrstr);
				fProperties.put(INITIAL_ADDRESS , addrstr);
			}
			else {
				String startAddr = fProperties.get(IMemoryImporter.TRANSFER_START);

				if ( startAddr == null ) {
					fProperties.put(IMemoryImporter.TRANSFER_START, addrstr);
					fProperties.put(INITIAL_ADDRESS , addrstr);
				}
			}
		}
	}
	
	public void scrollRenderings(final BigInteger address)
	{
		UIJob job = new UIJob("repositionRenderings"){ //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (IMemoryRenderingContainer container : fMemoryView.getMemoryRenderingContainers()) {
					IMemoryRendering rendering = container.getActiveRendering();
					if(rendering instanceof IRepositionableMemoryRendering)	{
						try  {
							((IRepositionableMemoryRendering) rendering).goToAddress(address);
						} 
						catch (DebugException ex) {
							MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
									DebugException.REQUEST_FAILED, MessageFormat.format(Messages.getString("ImportMemoryDialog.ErrRepositioningRendering"), address.toString(16)), ex));  //$NON-NLS-1$
						}
					}
				}

				return Status.OK_STATUS;
			}};
		job.setSystem(true); 
		job.setThread(Display.getDefault().getThread());
		job.schedule();
		
	
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
		
		IMemoryImporter currentImporter = getCurrentImporter();
		currentImporter.importMemory();
		fProperties.put(SELECTED_IMPORTER, currentImporter.getId());
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MemoryTransportPlugin.getUniqueIdentifier() + ".ImportMemoryDialog_context"); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);
		
		// format
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(Messages.getString("ImportMemoryDialog.Format"));  //$NON-NLS-1$
	
		fFormatCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		
		FormData data = new FormData();
		data.top = new FormAttachment(fFormatCombo, 0, SWT.CENTER);
		textLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(textLabel);
		fFormatCombo.setLayoutData(data);
		
		Vector<Object> importers = new Vector<Object>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
	         registry.getExtensionPoint("org.eclipse.cdt.debug.ui.memory.transport.memoryTransport"); //$NON-NLS-1$
	    IConfigurationElement points[] =
	         extensionPoint.getConfigurationElements();
	     
		for (int i = 0; i < points.length; i++) 
		{
			IConfigurationElement element = points[i];
			if("importer".equals(element.getName())) //$NON-NLS-1$
			{
				try 
				{
					importers.addElement(element.createExecutableExtension("class")); //$NON-NLS-1$
				}
				catch(Exception e) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
			    		DebugException.INTERNAL_ERROR, "Failure", e)); //$NON-NLS-1$
				}
			}
		}
		
        fFormatImporters = new IMemoryImporter[importers.size()];
		fFormatNames = new String[importers.size()];
		for(int i = 0; i < fFormatImporters.length; i++)
		{
			fFormatImporters[i] = (IMemoryImporter) importers.elementAt(i);
			fFormatNames[i] = ((IMemoryImporter) importers.elementAt(i)).getName();
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
				fCurrentControl = getCurrentImporter().createControl(container, fMemoryBlock, fProperties, ImportMemoryDialog.this);
			}
		});
		
		setCurrentImporter(fProperties.get(SELECTED_IMPORTER));
		
		fCurrentControl = getCurrentImporter().createControl(container,fMemoryBlock, fProperties, ImportMemoryDialog.this);
		
		return composite;
	}

	public void setValid(boolean isValid)
	{
		getButton(IDialogConstants.OK_ID).setEnabled(isValid);
	}
	
	private IMemoryImporter getCurrentImporter() {
		return fFormatImporters[fFormatCombo.getSelectionIndex()];
	}
	
	private void setCurrentImporter(String id) {
		if ( id == null || id.length() == 0 ) {
			fFormatCombo.select(0);
		}
		
		for (int index = 0; index< fFormatImporters.length; ++index) {
			if (fFormatImporters[index].getId().equals(id)){
				fFormatCombo.select(index);
				return;
			}
		}
		
		fFormatCombo.select(0);
	}

}
