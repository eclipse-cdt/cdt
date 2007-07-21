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

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
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
 * Action for exporting memory.
 */
public class ExportMemoryAction implements IViewActionDelegate {

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
			ExportMemoryDialog dialog = new ExportMemoryDialog(shell, memBlock);
			dialog.open();
			
			Object results[] = dialog.getResult();
			
			if(results != null && results.length == 5)
			{
				String format = (String) results[0];
				BigInteger start = (BigInteger) results[1];
				BigInteger end = (BigInteger) results[2];
				File file = (File) results[4];
				
				if("S-Record".equals(format))
				{
					exportSRecord(file, start, end, (IMemoryBlockExtension) memBlock); // FIXME
				}
			}
		}

	}

	private void exportSRecord(final File outputFile, final BigInteger startAddress, final BigInteger endAddress, final IMemoryBlockExtension memblock)
	{
		Job job = new Job("Memory Export to S-Record File"){ //$NON-NLS-1$
			public IStatus run(IProgressMonitor monitor) {
				
				try
				{
					try
					{	
						// FIXME 4 byte default
						
						BigInteger DATA_PER_RECORD = BigInteger.valueOf(16);
						
						BigInteger transferAddress = startAddress;
						
						FileWriter writer = new FileWriter(outputFile);
						
						BigInteger jobs = endAddress.subtract(transferAddress).divide(DATA_PER_RECORD);
						BigInteger factor = BigInteger.ONE;
						if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
						{
							factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
							jobs = jobs.divide(factor);
						}
							
						monitor.beginTask("Transferring Data", jobs.intValue());
						
						BigInteger jobCount = BigInteger.ZERO;
						while(transferAddress.compareTo(endAddress) < 0 && !monitor.isCanceled())
						{
							BigInteger length = DATA_PER_RECORD;
							if(endAddress.subtract(transferAddress).compareTo(length) < 0)
								length = endAddress.subtract(transferAddress);
							
							writer.write("S3"); // FIXME 4 byte address
							
							StringBuffer buf = new StringBuffer();
							
							BigInteger sRecordLength = BigInteger.valueOf(4); // address size
							sRecordLength = sRecordLength.add(length);
							sRecordLength = sRecordLength.add(BigInteger.ONE); // checksum
							
							String transferAddressString = transferAddress.toString(16);
							
							String lengthString = sRecordLength.toString(16);
							if(lengthString.length() == 1)
								buf.append("0");
							buf.append(lengthString);
							for(int i = 0; i < 8 - transferAddressString.length(); i++)
								buf.append("0");
							buf.append(transferAddressString);
							
							// data
							
							MemoryByte bytes[] = memblock.getBytesFromAddress(transferAddress, 
								length.longValue() / memblock.getAddressableSize());
							for(MemoryByte b : bytes)
							{
								String bString = BigInteger.valueOf(0xFF & b.getValue()).toString(16);
								if(bString.length() == 1)
									buf.append("0");
								buf.append(bString);
							}
							
							BigInteger checksum = BigInteger.ZERO;
							
							for(int i = 0; i < buf.length(); i+=2)
							{
								BigInteger value = new BigInteger(buf.substring(i, i+1), 16);
								checksum = checksum.add(value);
							}
							
							buf.append(BigInteger.valueOf(0xFF - checksum.byteValue()).and(
									BigInteger.valueOf(0xFF)).toString(16));
							
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
