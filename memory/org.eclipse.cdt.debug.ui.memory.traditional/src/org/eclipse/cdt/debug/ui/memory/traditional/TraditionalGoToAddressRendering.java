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

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.internal.ui.views.memory.RenderingViewPane;
import org.eclipse.debug.internal.ui.views.memory.renderings.CreateRendering;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

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
		
		final GoToAddressWidget fGotoAddress = new GoToAddressWidget();
		Control fGotoAddressControl = fGotoAddress.createControl(fGotoAddressContainer);
		
		fControl = fGotoAddressControl;
		
		final Runnable goHandler = new Runnable()
		{
			public void run()
			{
				go(fGotoAddress.getExpressionText(), false);
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
		
		button = fGotoAddress.getButton(GoToAddressWidget.ID_GO_NEW_TAB);
		if (button != null)
		{
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					go(fGotoAddress.getExpressionText(), true);
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
	
	private void go(final String expression, final boolean inNewTab)
	{
		IMemoryRenderingContainer containers[] = fSite.getMemoryRenderingContainers();
		for(int i = 0; i < containers.length; i++)
		{
			if(containers[i] instanceof RenderingViewPane)
			{
				final IMemoryRenderingContainer container = containers[i];
				if(containers[i] != null)
				{
					final IMemoryRendering activeRendering = containers[i].getActiveRendering();
					if(activeRendering != null)
					{
						new Thread() {
							public void run()
							{
								IMemoryBlock activeMemoryBlock = activeRendering.getMemoryBlock();
								IMemoryBlockExtension blockExtension = (IMemoryBlockExtension) activeMemoryBlock.getAdapter(IMemoryBlockExtension.class);
								
								if(inNewTab)
								{
									try {
										final IMemoryRendering rendering = new CreateRendering(container);
										IMemoryBlock newBlock = null;
										if(activeMemoryBlock.getDebugTarget() instanceof IMemoryBlockRetrievalExtension)
										{
											newBlock = ((IMemoryBlockRetrievalExtension) activeMemoryBlock.getDebugTarget())
												.getExtendedMemoryBlock(expression, activeMemoryBlock.getDebugTarget());
											
										}
										else
										{
											BigInteger newAddress;
											if(expression.toUpperCase().startsWith("0X"))
												newAddress = new BigInteger(expression.substring(2), 16);
											else
												newAddress = new BigInteger(expression, 16);
											
											newBlock = activeMemoryBlock.getDebugTarget().getMemoryBlock(newAddress.longValue(), 
												activeMemoryBlock.getLength());
										}
										
										final IMemoryBlock finalNewBlock = newBlock;
										Display.getDefault().asyncExec(new Runnable(){
											public void run()
											{
												rendering.init(container, finalNewBlock);
												container.addMemoryRendering(rendering);
											}
										});
										
									} catch (DebugException e) {
										MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
												DebugUIMessages.GoToAddressAction_Address_is_invalid, e);
									}
								}
								else if(activeRendering instanceof IRepositionableMemoryRendering)
								{	
									try
									{
										if(activeMemoryBlock.getDebugTarget() instanceof IMemoryBlockRetrievalExtension)
										{
											IMemoryBlockExtension resolveExpressionBlock = ((IMemoryBlockRetrievalExtension) activeMemoryBlock.getDebugTarget())
												.getExtendedMemoryBlock(expression, activeMemoryBlock.getDebugTarget());
											((IRepositionableMemoryRendering) activeRendering).goToAddress(resolveExpressionBlock.getBigBaseAddress());
										}
										else
										{
											BigInteger newAddress;
											if(expression.toUpperCase().startsWith("0X"))
												newAddress = new BigInteger(expression.substring(2), 16);
											else
												newAddress = new BigInteger(expression, 16);
											
											((IRepositionableMemoryRendering) activeRendering).goToAddress(newAddress);
										}
									}
									catch(DebugException de)
									{
										MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
												DebugUIMessages.GoToAddressAction_Address_is_invalid, de);
									}
								}
								
							}
						}.start();
					}
				}
			}
		}
	}
}

class GoToAddressWidget {
	
	private Text fExpression;
	private Button fOKButton;
	private Button fOKNewTabButton;
	private Composite fComposite;
	
	protected static int ID_GO_NEW_TAB = 2000;

	/**
	 * @param parent
	 * @return
	 */
	public Control createControl(Composite parent)
	{
		fComposite = new Composite(parent, SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fComposite, DebugUIPlugin.getUniqueIdentifier() + ".GoToAddressComposite_context"); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		layout.numColumns = 6;
		layout.makeColumnsEqualWidth = false;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		fComposite.setLayout(layout);
	
		fExpression = new Text(fComposite, SWT.SINGLE | SWT.BORDER);
		fExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fOKButton = new Button(fComposite, SWT.NONE);
		fOKButton.setText("Go");
		
//		fOKNewTabButton = new Button(fComposite, SWT.NONE);
//		fOKNewTabButton.setText("New Tab");
		
		return fComposite;
	}
	
	public int getHeight()
	{
		int height = fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		return height;
	}
	
	public Button getButton(int id)
	{
		if (id == IDialogConstants.OK_ID)
			return fOKButton;
		if (id == ID_GO_NEW_TAB)
			return fOKNewTabButton;
		return null;
	}
	
	public String getExpressionText()
	{
		return fExpression.getText().trim();
	}
	
	public Text getExpressionWidget()
	{
		return fExpression;
	}
}
