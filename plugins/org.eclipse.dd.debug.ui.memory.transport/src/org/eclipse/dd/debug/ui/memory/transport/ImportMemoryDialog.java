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

import java.math.BigInteger;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.debug.ui.memory.transport.model.IMemoryImporter;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.internal.ui.views.memory.RenderingViewPane;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.progress.UIJob;

public class ImportMemoryDialog extends SelectionDialog 
{

	private Combo fFormatCombo;
	
	private IMemoryBlock fMemoryBlock;
	
	private Control fCurrentControl = null;
	
	private IMemoryImporter fFormatImporters[];
	private String fFormatNames[];
	
	private Properties fProperties = new Properties();
	
	private MemoryView fMemoryView;
	
	public ImportMemoryDialog(Shell parent, IMemoryBlock memoryBlock, MemoryView view)
	{
		super(parent);
		super.setTitle("Download to Memory");  
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
		fMemoryView = view;
	}
	
	protected void scrollRenderings(final BigInteger address)
	{
		UIJob job = new UIJob("repositionRenderings"){ //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final IMemoryRenderingContainer containers[] = fMemoryView.getMemoryRenderingContainers();
				for(int i = 0; i < containers.length; i++)
				{
					if(containers[i] instanceof RenderingViewPane)
					{
						IMemoryRendering rendering = containers[i].getActiveRendering();
						
						if(rendering instanceof IRepositionableMemoryRendering)
						{
							try 
							{
								((IRepositionableMemoryRendering) rendering).goToAddress(address);
							} 
							catch (DebugException e) 
							{
								// do nothing
							}
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
		fFormatImporters[fFormatCombo.getSelectionIndex()].importMemory();
		
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MemoryTransportPlugin.getUniqueIdentifier() + ".ImportMemoryDialog_context"); //$NON-NLS-1$
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
		
		Vector importers = new Vector();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
	         registry.getExtensionPoint("org.eclipse.dd.debug.ui.memory.transport.memoryTransport");
	    IConfigurationElement points[] =
	         extensionPoint.getConfigurationElements();
	     
		for (int i = 0; i < points.length; i++) 
		{
			IConfigurationElement element = points[i];
			if("importer".equals(element.getName()))
			{
				try 
				{
					importers.addElement((IMemoryImporter) element.createExecutableExtension("class"));
				}
				catch(Exception e) {
					MemoryTransportPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
			    		DebugException.INTERNAL_ERROR, "Failure", e));
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
		
		fFormatCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				if(fCurrentControl != null)
					fCurrentControl.dispose();
				fCurrentControl = fFormatImporters[fFormatCombo.getSelectionIndex()].createControl(container, 
					fMemoryBlock, fProperties, ImportMemoryDialog.this);
			}
		});
		
		fFormatCombo.select(0);
		fCurrentControl = 
			fFormatImporters[0].createControl(container,fMemoryBlock, fProperties, ImportMemoryDialog.this);
		
		return composite;
	}
	
	public void setValid(boolean isValid)
	{
		getButton(IDialogConstants.OK_ID).setEnabled(isValid);
	}
}
