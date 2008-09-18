/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.dd.debug.memory.renderings.traditional;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.internal.ui.views.memory.RenderingViewPane;
import org.eclipse.debug.internal.ui.views.memory.renderings.CreateRendering;
import org.eclipse.debug.internal.ui.views.memory.renderings.GoToAddressComposite;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class TraditionalGoToAddressRendering extends AbstractMemoryRendering {

	private IMemoryRenderingSite fSite;
	private IMemoryRenderingContainer fContainer;
	
	@Override
	public void init(IMemoryRenderingContainer container, IMemoryBlock block) {
		super.init(container, block);
		fSite = container.getMemoryRenderingSite();
		fContainer = container;
	}

	public TraditionalGoToAddressRendering(String renderingId) {
		super(renderingId);
	}

	private Control fControl;
	
	public Control createControl(Composite parent) {
		
		Composite fGotoAddressContainer = parent;
		
		final GoToAddressComposite fGotoAddress = new GoToAddressComposite();
		Control fGotoAddressControl = fGotoAddress.createControl(fGotoAddressContainer);
		
		fControl = fGotoAddressControl;
		
		final Runnable goHandler = new Runnable()
		{
			public void run()
			{
				String expression = fGotoAddress.getExpressionText();
				int radix = 10;
				if(expression.toUpperCase().startsWith("0X"))
				{
					expression = expression.substring(2);
					radix = 16;
				}
				else if(fGotoAddress.isHex())
				{
					radix = 16;
				}
				
				BigInteger address = null;
				try
				{
					address = new BigInteger(expression, radix);
				}
				catch(NumberFormatException nfe)
				{
					MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
							DebugUIMessages.GoToAddressAction_Address_is_invalid, nfe);
				}
	
				IMemoryRenderingContainer containers[] = fSite.getMemoryRenderingContainers();
				for(int i = 0; i < containers.length; i++)
				{
					if(containers[i] instanceof RenderingViewPane)
					{
						BigInteger absoluteAddress = null;
						if(fGotoAddress.isGoToAddress())
							absoluteAddress = address;
						
						IMemoryBlock activeMemoryBlock = null;
						
						if(address != null && containers[i] != null)
						{
							IMemoryRendering activeRendering = containers[i].getActiveRendering();
							if(activeRendering != null)
							{
								activeMemoryBlock = activeRendering.getMemoryBlock();
								IMemoryBlockExtension blockExtension = (IMemoryBlockExtension) activeMemoryBlock.getAdapter(IMemoryBlockExtension.class);
								if(blockExtension != null)
								{
									BigInteger baseAddress = null;
									BigInteger addressableSize = null;
									try
									{
										baseAddress = blockExtension.getBigBaseAddress();
										addressableSize = BigInteger.valueOf(blockExtension.getAddressableSize());
									}
									catch(DebugException de)
									{
										// TODO
									}
									
									if(baseAddress != null)
									{
										if(fGotoAddress.isOffset())
											absoluteAddress = baseAddress.add(address);
										else if(fGotoAddress.isJump() && addressableSize != null)
											absoluteAddress = baseAddress.add(address.multiply(addressableSize));
									}
								}
								
								// 1) Try to reposition the renderings using the IRepositionableMemoryRendering interface
								if(absoluteAddress != null && activeRendering instanceof IRepositionableMemoryRendering)
								{
									try
									{
										((IRepositionableMemoryRendering) activeRendering).goToAddress(absoluteAddress);
									}
									catch(DebugException de)
									{
										// do nothing
									}
								}
							}
						}
					}
				}
				
			}
		};
		
		Button button = fGotoAddress.getButton(IDialogConstants.OK_ID);
		if (button != null)
		{
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					goHandler.run();
				}
			});
		}
		
		fGotoAddress.getExpressionWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR)
					goHandler.run();
				super.keyPressed(e);
			}
		});

		
		return fControl;
	}

	public Control getControl() {
		return fControl;
	}

}
