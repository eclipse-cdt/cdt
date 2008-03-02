/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.memory.renderings.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.debug.memory.renderings.traditional.TraditionalRenderingPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewIdRegistry;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for downloading memory.
 */
public class ImportMemoryAction implements IViewActionDelegate {


	private MemoryView fView;

	public void init(IViewPart view) {
		if (view instanceof MemoryView)
			fView = (MemoryView) view;
	}

	public void run(IAction action) {

		String secondaryId = MemoryViewIdRegistry
				.getUniqueSecondaryId(IDebugUIConstants.ID_MEMORY_VIEW);

		ISelection selection = fView.getSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strucSel = (IStructuredSelection) selection;

			// return if current selection is empty
			if (strucSel.isEmpty())
				return;

			Object obj = strucSel.getFirstElement();

			if (obj == null)
				return;

			IMemoryBlock memBlock = null;

			if (obj instanceof IMemoryRendering) {
				memBlock = ((IMemoryRendering) obj).getMemoryBlock();
			} else if (obj instanceof IMemoryBlock) {
				memBlock = (IMemoryBlock) obj;
			}
			
			Shell shell = DebugUIPlugin.getShell();
			ImportMemoryDialog dialog = new ImportMemoryDialog(shell, memBlock);
			dialog.open();
			
			Object results[] = dialog.getResult();
			
			if(results != null && results.length == 4)
			{
				String format = (String) results[0];
				Boolean useCustomAddress = (Boolean) results[1];
				BigInteger start = (BigInteger) results[2];
				File file = (File) results[3];
				
				if("S-Record".equals(format)) //$NON-NLS-1$
				{
					downloadSRecord(file, start, useCustomAddress.booleanValue(), (IMemoryBlockExtension) memBlock); // FIXME
				}
			}
		}

	}

	private void downloadSRecord(final File inputFile, final BigInteger startAddress, final boolean useCustomAddress, final IMemoryBlockExtension memblock)
	{
		Job job = new Job("Memory Download from S-Record File"){ //$NON-NLS-1$
			@Override
			public IStatus run(IProgressMonitor monitor) {
				
				try
				{
					try
					{	
						// FIXME 4 byte default
						
						final int CHECKSUM_LENGTH = 1;
						
						BigInteger offset = null;
						if(!useCustomAddress)
							offset = BigInteger.ZERO;
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
						
						BigInteger jobs = BigInteger.valueOf(inputFile.length());
						BigInteger factor = BigInteger.ONE;
						if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
						{
							factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
							jobs = jobs.divide(factor);
						}
							
						monitor.beginTask("Transferring Data", jobs.intValue()); //$NON-NLS-1$
						
						BigInteger jobCount = BigInteger.ZERO;
						String line = reader.readLine();
						while(line != null && !monitor.isCanceled())
						{
							String recordType = line.substring(0, 2);
							int recordCount = Integer.parseInt(line.substring(2, 4), 16);
							int bytesRead = 4 + recordCount;
							int position = 4;
							int addressSize = 0;
							
							BigInteger recordAddress = null;
				
							if("S3".equals(recordType)) //$NON-NLS-1$
								addressSize = 4;
							else if("S1".equals(recordType)) //$NON-NLS-1$
								addressSize = 2;
							else if("S2".equals(recordType)) //$NON-NLS-1$
								addressSize = 3;
							
							recordAddress = new BigInteger(line.substring(position, position + addressSize * 2), 16);
							recordCount -= addressSize;
							position += addressSize * 2;
							
							if(offset == null)
								offset = startAddress.subtract(recordAddress);
							
							recordAddress = recordAddress.add(offset);
							
							byte data[] = new byte[recordCount - CHECKSUM_LENGTH];
							for(int i = 0; i < data.length; i++)
							{
								data[i] = new BigInteger(line.substring(position++, position++ + 1), 16).byteValue();
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
								BigInteger value = new BigInteger(buf.substring(i, i+2), 16);
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
								return new Status( IStatus.ERROR, TraditionalRenderingPlugin.getUniqueIdentifier(), "Checksum failure of line = " + line); //$NON-NLS-1$
							}
							
							// FIXME error on incorrect checksum
							
							memblock.setValue(recordAddress.subtract(memblock.getBigBaseAddress()), data);

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
					}
					catch(Exception e) { e.printStackTrace();}
				}
				catch(Exception e) {e.printStackTrace();}
				return Status.OK_STATUS;
			}};
		job.setUser(true);
		job.schedule();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}
