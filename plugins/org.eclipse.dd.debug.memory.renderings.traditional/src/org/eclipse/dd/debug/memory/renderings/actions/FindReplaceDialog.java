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

import java.math.BigInteger;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public class FindReplaceDialog extends SelectionDialog 
{

	private IMemoryBlockExtension fMemoryBlock;
	
	private Text findText;
	private Text replaceText;
	
	private Combo startText;
	private Combo endText;
	
	private Button findButton;
	private Button replaceButton;
	private Button replaceFindButton;
	private Button replaceAllButton;
	private Button closeButton;

	private MemoryView fMemoryView;
	
	Button formatAsciiButton;
	Button formatHexButton;
	Button formatOctalButton;
	Button formatBinaryButton;
	Button formatDecimalButton;
	
	Button caseInSensitiveCheckbox;
	
	Button forwardButton;
	
	public FindReplaceDialog(Shell parent, IMemoryBlockExtension memoryBlock, MemoryView memoryView)
	{
		super(parent);
		super.setTitle(Messages.getString("FindReplaceDialog.Title"));  //$NON-NLS-1$
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
		fMemoryView = memoryView;
		this.setBlockOnOpen(false);
	}
	
	private BigInteger getUserStart()
	{
		String start = startText.getText();
		if(start.toUpperCase().startsWith("0X")) //$NON-NLS-1$
			start = start.substring(2);
		return new BigInteger(start, 16);
	}
	
	private BigInteger getUserEnd()
	{
		String end = endText.getText();
		if(end.toUpperCase().startsWith("0X")) //$NON-NLS-1$
			end = end.substring(2);
		return new BigInteger(end, 16);
	}
	
	private boolean getIsDirectionForward()
	{
		return forwardButton.getSelection();
	}
	
	private SearchPhrase getSearchPhrase()
	{
		SearchPhrase phrase = null;
		
		if(formatAsciiButton.getSelection())
		{
			phrase = new AsciiSearchPhrase(findText.getText(), caseInSensitiveCheckbox.getSelection());
		}
		else if(formatHexButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(findText.getText().toUpperCase().startsWith("0X") 
				? findText.getText().substring(2) : findText.getText(), 16), 16);
		}
		else if(formatOctalButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(findText.getText().startsWith("0") 
					? findText.getText().substring(1) : findText.getText(), 8), 8);
		}
		else if(formatBinaryButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(findText.getText(), 2), 2);
		}
		else if(formatDecimalButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(findText.getText(), 10), 10);
		}
		
		return phrase;
	}
	
	private byte[] getReplaceData()
	{
		if(formatAsciiButton.getSelection())
			return replaceText.getText().getBytes();
		else if(formatHexButton.getSelection())
			return new BigInteger(replaceText.getText().toUpperCase().startsWith("0X") ? replaceText.getText().substring(2) : replaceText.getText(), 16).toByteArray();
		else if(formatOctalButton.getSelection())
			return new BigInteger(replaceText.getText().startsWith("0") ? replaceText.getText().substring(1) : replaceText.getText(), 8).toByteArray();
		else if(formatBinaryButton.getSelection())
			return new BigInteger(replaceText.getText(), 2).toByteArray();
		else if(formatDecimalButton.getSelection())
			return new BigInteger(replaceText.getText(), 10).toByteArray();
		
		return new byte[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		findButton = createButton(parent, 10, Messages.getString("FindReplaceDialog.ButtonFind"), true); //$NON-NLS-1$
		findButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), null, false, false);
				cancelPressed();
			}
		});
		
		replaceFindButton = createButton(parent, 11, Messages.getString("FindReplaceDialog.ButtonReplaceFind"), false); //$NON-NLS-1$
		replaceFindButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), getReplaceData(), false, true);
				cancelPressed();
			}
		});
		
		replaceButton = createButton(parent, 12, Messages.getString("FindReplaceDialog.ButtonReplace"), false); //$NON-NLS-1$
		replaceButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), getReplaceData(), false, false);
				cancelPressed();
			}
		});
		
		replaceAllButton = createButton(parent, 13, Messages.getString("FindReplaceDialog.ButtonReplaceAll"), false); //$NON-NLS-1$
		replaceAllButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), getReplaceData(), true, false);
				cancelPressed();
			}
		});
		
		closeButton = createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("FindReplaceDialog.Close"), false); //$NON-NLS-1$
		
		((GridLayout) parent.getLayout()).numColumns = 2;
				
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
		setSelectionResult(new Object[]{ });
		
		super.okPressed();
	}
	
	public BigInteger getEndAddress()
	{
		String text = endText.getText();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger endAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return endAddress;
	}
	
	public BigInteger getStartAddress()
	{
		String text = startText.getText();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return startAddress;
	}
	
	private void validate()
	{
		// TODO: make sure start & end are valid in relation to the direction
	}
	
	private String pad(int characterCount, String value)
	{
		StringBuffer sb = new StringBuffer(value);
		for(int i = 0; i < characterCount - value.length(); i++)
			sb.insert(0, "0"); //$NON-NLS-1$
		return sb.toString();
	}
	
	private String[] removeNullElements(String strings[])
	{
		Vector nonNullStrings = new Vector<String>();
		for(String string : strings)
			if(string != null)
				nonNullStrings.addElement(string);
		return (String[]) nonNullStrings.toArray(new String[0]);	
	}
	
	private String getMemoryBlockBaseAddress()
	{
		BigInteger base = null;
		try
		{
			base = fMemoryBlock.getBigBaseAddress();
		}
		catch(DebugException de)
		{
		}
		
		if(base == null)
			base = BigInteger.ZERO;
		
		return "0x" + pad(getAddressSize() * 2, base.toString(16).toUpperCase()); //$NON-NLS-1$
	}
	
	private String getViewportStart()
	{
		ISelection selection = fMemoryView.getViewPane(IDebugUIConstants.ID_RENDERING_VIEW_PANE_1).getSelectionProvider().getSelection();
		if(selection instanceof StructuredSelection)
		{
			if(((StructuredSelection) selection).getFirstElement() instanceof IRepositionableMemoryRendering)
			{
				((IRepositionableMemoryRendering) ((StructuredSelection) selection).getFirstElement()).getSelectedAddress();
			}
		}
		return null;
	}
	
	private String getStart()
	{
		BigInteger start = null;
		try
		{
			start = fMemoryBlock.getMemoryBlockStartAddress();
		}
		catch(DebugException de)
		{
		}
		
		if(start == null)
			start = BigInteger.ZERO;
		
		return "0x" + pad(getAddressSize() * 2, start.toString(16).toUpperCase()); //$NON-NLS-1$
	}
	
	private String getEnd()
	{
		BigInteger end = null;
		
		try
		{
			end = fMemoryBlock.getMemoryBlockEndAddress();
		}
		catch(DebugException de)
		{
		}
		
		if(end == null)
		{
			end = BigInteger.ZERO;
			
			for(int i = getAddressSize(); i > 0; i--)
			{
				end = end.shiftLeft(8);
				end = end.or(BigInteger.valueOf(255));
			}
		}
		
		return "0x" + pad(getAddressSize() * 2, end.toString(16).toUpperCase()); //$NON-NLS-1$
	}
	
	private int getAddressSize()
	{
		int addressSize;
		try
		{
			addressSize = fMemoryBlock.getAddressSize();
		}
		catch(DebugException de)
		{
			addressSize = 4; // default to 32bit?
		}
		return addressSize;
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

		// find
		
		Label findLabel = new Label(composite, SWT.NONE);
		Label replaceLabel = new Label(composite, SWT.NONE);
		replaceText = new Text(composite, SWT.BORDER);

		findLabel.setText(Messages.getString("FindReplaceDialog.LabelFind"));  //$NON-NLS-1$
		
		findText = new Text(composite, SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(replaceText, 0, SWT.LEFT);
		data.width = 260;
		findText.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(findText, 0, SWT.CENTER);
		findLabel.setLayoutData(data);
		
		// replace
		
		replaceLabel.setText(Messages.getString("FindReplaceDialog.LabelReplaceWith"));  //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(findText);
		replaceLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(replaceLabel, 0, SWT.CENTER);
		data.left = new FormAttachment(replaceLabel);
		data.width = 260;
		replaceText.setLayoutData(data);
		
		// group direction
		
		Group directionGroup = new Group(composite, SWT.NONE);
		Group formatGroup = new Group(composite, SWT.NONE);
		Group rangeGroup = new Group(composite, SWT.NONE);
		directionGroup.setText(Messages.getString("FindReplaceDialog.LabelDirection")); //$NON-NLS-1$
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		directionGroup.setLayout(layout);
		
		forwardButton = new Button(directionGroup, SWT.RADIO);
		forwardButton.setText(Messages.getString("FindReplaceDialog.ButtonForward")); //$NON-NLS-1$
		Button backwardButton = new Button(directionGroup, SWT.RADIO);
		backwardButton.setText(Messages.getString("FindReplaceDialog.ButtonBackward")); //$NON-NLS-1$
		
		data = new FormData();
		data.top = new FormAttachment(replaceText);
		data.right = new FormAttachment(formatGroup, 0, SWT.RIGHT);
		data.left = new FormAttachment(formatGroup, 0, SWT.LEFT);
		data.bottom = new FormAttachment(rangeGroup, 0, SWT.BOTTOM);
		directionGroup.setLayoutData(data);
		
		// group range
		
		rangeGroup.setText(Messages.getString("FindReplaceDialog.LabelRange")); //$NON-NLS-1$

		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		rangeGroup.setLayout(layout);
		
		// group range - start address
		
		Label startLabel = new Label(rangeGroup, SWT.NONE);
		startLabel.setText(Messages.getString("FindReplaceDialog.LabelStartAddress")); 		 //$NON-NLS-1$
		
		startText = new Combo(rangeGroup, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 200;
		gridData.grabExcessHorizontalSpace = true;
		startText.setLayoutData(gridData);
		
		// group range - end address
		
		Label endLabel = new Label(rangeGroup, SWT.NONE);
		endLabel.setText(Messages.getString("FindReplaceDialog.LabelEndAddress"));  //$NON-NLS-1$
		endText = new Combo(rangeGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 200;
		gridData.grabExcessHorizontalSpace = true;
		endText.setLayoutData(gridData);
		
		data = new FormData();
		data.left = new FormAttachment(directionGroup);	
		data.top = new FormAttachment(directionGroup, 0, SWT.TOP);
		data.right = new FormAttachment(findText, 0, SWT.RIGHT);
		rangeGroup.setLayoutData(data);
		
		startText.setItems(removeNullElements(new String[] { getViewportStart(), getStart(), getEnd(), getMemoryBlockBaseAddress() }));
		endText.setItems(removeNullElements(new String[] { getEnd(), getStart(), getMemoryBlockBaseAddress(), getViewportStart() }));
		startText.select(0);
		endText.select(0);
		
		// format group
		
		formatGroup.setText(Messages.getString("FindReplaceDialog.LabelFormat")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 1;
		formatGroup.setLayout(layout);
		
		formatAsciiButton = new Button(formatGroup, SWT.RADIO);
		formatAsciiButton.setText(Messages.getString("FindReplaceDialog.ButtonASCII")); //$NON-NLS-1$
				
		formatHexButton = new Button(formatGroup, SWT.RADIO);
		formatHexButton.setText(Messages.getString("FindReplaceDialog.ButtonHexadecimal")); //$NON-NLS-1$
		
		formatOctalButton = new Button(formatGroup, SWT.RADIO);
		formatOctalButton.setText(Messages.getString("FindReplaceDialog.ButtonOctal")); //$NON-NLS-1$
		
		formatBinaryButton = new Button(formatGroup, SWT.RADIO);
		formatBinaryButton.setText(Messages.getString("FindReplaceDialog.ButtonBinary")); //$NON-NLS-1$
		
		formatDecimalButton = new Button(formatGroup, SWT.RADIO);
		formatDecimalButton.setText(Messages.getString("FindReplaceDialog.ButtonDecimal")); //$NON-NLS-1$
		
		data = new FormData();
		data.top = new FormAttachment(rangeGroup);
		formatGroup.setLayoutData(data);

		// options group
		
		Group optionsGroup = new Group(composite, SWT.NONE);
		optionsGroup.setText(Messages.getString("FindReplaceDialog.LabelOptions")); //$NON-NLS-1$

		data = new FormData();
		data.left = new FormAttachment(formatGroup);
		data.top = new FormAttachment(rangeGroup);
		data.bottom = new FormAttachment(formatGroup, 0, SWT.BOTTOM);
		data.right = new FormAttachment(rangeGroup, 0, SWT.RIGHT);
		optionsGroup.setLayoutData(data);
		
		layout = new GridLayout();
		layout.numColumns = 1;
		optionsGroup.setLayout(layout);

		
		// wrap
		
		Button wrapCheckbox = new Button(optionsGroup, SWT.CHECK);
		wrapCheckbox.setText(Messages.getString("FindReplaceDialog.ButtonWrapSearch")); //$NON-NLS-1$
		wrapCheckbox.setEnabled(false); // TODO implement wrap
		
		caseInSensitiveCheckbox = new Button(optionsGroup, SWT.CHECK);
		caseInSensitiveCheckbox.setText(Messages.getString("FindReplaceDialog.ButtonCaseInsensitive")); //$NON-NLS-1$
		
		formatAsciiButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				caseInSensitiveCheckbox.setEnabled(true);
			}
		});
		
		SelectionListener nonAsciiListener = new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				caseInSensitiveCheckbox.setEnabled(false);
			}
		};
		formatHexButton.addSelectionListener(nonAsciiListener);
		formatOctalButton.addSelectionListener(nonAsciiListener);
		formatBinaryButton.addSelectionListener(nonAsciiListener);
		formatDecimalButton.addSelectionListener(nonAsciiListener);

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
					
				}
				catch(Exception ex)
				{
					endText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
				
				validate();
			}
			
		});
		
		forwardButton.setSelection(true);
		formatAsciiButton.setSelection(true);
		
		findText.setFocus();
		
		return composite;
	}
	
	private void performFind(final BigInteger start, final BigInteger end, final SearchPhrase searchPhrase, 
		final boolean searchForward, final byte[] replaceData, final boolean isReplaceAll, final boolean replaceThenFind)
	{
		Job job = new Job("Searching memory for " + searchPhrase){ //$NON-NLS-1$
			public IStatus run(IProgressMonitor monitor) {
				BigInteger searchPhraseLength = BigInteger.valueOf(searchPhrase.getByteLength());
				BigInteger range = searchForward ? end.subtract(start) : start.subtract(end);
				BigInteger currentPosition = start;
				
				boolean isReplace = replaceData != null;
				
				BigInteger jobs = range.subtract(searchPhraseLength);
				BigInteger factor = BigInteger.ONE;
				if(jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0)
				{
					factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
					jobs = jobs.divide(factor);
				}
				
				BigInteger jobCount = BigInteger.ZERO;
				
				BigInteger replaceCount = BigInteger.ZERO;
				
				monitor.beginTask(Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase, jobs.intValue()); //$NON-NLS-1$
				
				boolean matched = false;
				while(!matched && 
						((searchForward && currentPosition.compareTo(end.subtract(searchPhraseLength)) < 0) 
								|| (!searchForward && currentPosition.compareTo(end) > 0))
						&& !monitor.isCanceled())
				{
					try
					{
						// TODO cache and reuse previously read bytes?
						MemoryByte bytes[] = fMemoryBlock.getBytesFromAddress(currentPosition, searchPhraseLength.longValue());
						matched = searchPhrase.isMatch(bytes);
					}
					catch(DebugException e)
					{
						// TODO log ?
						// stop search? preference?
					}
					
					if(!matched)
					{
						if(searchForward)
							currentPosition = currentPosition.add(BigInteger.ONE);
						else
							currentPosition = currentPosition.subtract(BigInteger.ONE);
					}
					
					jobCount = jobCount.add(BigInteger.ONE);
					if(jobCount.compareTo(factor) == 0)
					{
						jobCount = BigInteger.ZERO;
						monitor.worked(1);
					}
					
					if(matched)
					{
						if(isReplace)
						{
							try
							{
								fMemoryBlock.setValue(currentPosition, replaceData);
							}
							catch(DebugException de)
							{
								// TODO log?
							}

							replaceCount = replaceCount.add(BigInteger.ONE);
						}
						
						if(isReplace && replaceThenFind && replaceCount.compareTo(BigInteger.ONE) == 0)
						{
							isReplace = false;
							matched = false;
							
							if(searchForward)
								currentPosition = currentPosition.add(BigInteger.ONE);
							else
								currentPosition = currentPosition.subtract(BigInteger.ONE);
						}
						
						if(isReplaceAll)
						{
							matched = false;
							
							if(searchForward)
								currentPosition = currentPosition.add(BigInteger.ONE);
							else
								currentPosition = currentPosition.subtract(BigInteger.ONE);
						}
					}
				}
					
				if(matched)
				{
					ISelection selection = fMemoryView.getViewPane(IDebugUIConstants.ID_RENDERING_VIEW_PANE_1).getSelectionProvider().getSelection();
					if(selection instanceof StructuredSelection)
					{
						if(((StructuredSelection) selection).getFirstElement() instanceof IRepositionableMemoryRendering)
						{
							try
							{
								((IRepositionableMemoryRendering) ((StructuredSelection) selection).getFirstElement()).goToAddress(currentPosition);
							}
							catch(DebugException de)
							{
								// log
							}
						}
					}
				}
				
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	interface SearchPhrase
	{
		boolean isMatch(MemoryByte[] bytes);
		int getByteLength();
		String toString();
	}
	
	class AsciiSearchPhrase implements SearchPhrase
	{
		private String fPhrase;
		private boolean fIsCaseInsensitive;
		
		public AsciiSearchPhrase(String phrase, boolean isCaseInsensitive)
		{
			fPhrase = phrase;
			fIsCaseInsensitive = isCaseInsensitive;
		}
		
		public int getByteLength()
		{
			return fPhrase.length();
		}
		
		public String toString()
		{
			return fPhrase;
		}
		
		public boolean isMatch(MemoryByte[] bytes)
		{
			byte[] targetBytes = new byte[bytes.length];
			for(int i = 0; i < bytes.length; i++)
				targetBytes[i] = bytes[i].getValue();
			
			String searchString = fPhrase;
			String targetString = new String(targetBytes);
			
			if(fIsCaseInsensitive)
			{
				searchString = searchString.toUpperCase();
				targetString = targetString.toUpperCase();
			}
	
			return searchString.equals(targetString);
		}
	}
	
	class BigIntegerSearchPhrase implements SearchPhrase
	{
		private BigInteger fPhrase;
		private int fRadix;
		
		public BigIntegerSearchPhrase(BigInteger phrase, int radix)
		{
			fPhrase = phrase;
			fRadix = radix;
		}
		
		public int getByteLength()
		{
			return fPhrase.toByteArray().length;
		}
		
		public String toString()
		{
			return fPhrase.toString(fRadix);
		}
		
		public boolean isMatch(MemoryByte[] bytes)
		{
			byte[] targetBytes = new byte[bytes.length];
			for(int i = 0; i < bytes.length; i++)
				targetBytes[i] = bytes[i].getValue();
			
			// TODO endian?
			BigInteger targetBigInteger = new BigInteger(targetBytes);
			
			return fPhrase.equals(targetBigInteger);
		}
	}
	
}
