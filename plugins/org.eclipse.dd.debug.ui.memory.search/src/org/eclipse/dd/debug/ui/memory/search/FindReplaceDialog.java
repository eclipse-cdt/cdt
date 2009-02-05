/*******************************************************************************
 * Copyright (c) 2007-2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.ui.memory.search;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
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
	
	final static int preFetchSize = 10 * 1024;
	
	private Text fFindText;
	private Text fReplaceText;
	
	private Combo fStartText;
	private Combo fEndText;
	
	private Button fFindButton;
	private Button fFindAllButton;
	private Button fReplaceButton;
	private Button fReplaceFindButton;
	private Button fReplaceAllButton;
	private Button fCloseButton;

	private MemoryView fMemoryView;
	
	private Button fFormatAsciiButton;
	private Button fFormatHexButton;
	private Button fFormatOctalButton;
	private Button fFormatBinaryButton;
	private Button fFormatDecimalButton;
	private Button fFormatByteSequenceButton;
	
	private Button fCaseInSensitiveCheckbox;
	
	private Button fWrapCheckbox;
	
	private Button fForwardButton;
	
	private Properties fProperties;
	
	protected final static String SEARCH_FIND = "SEARCH_FIND"; //$NON-NLS-1$
	protected final static String SEARCH_REPLACE = "SEARCH_REPLACE"; //$NON-NLS-1$
	protected final static String SEARCH_START = "SEARCH_START"; //$NON-NLS-1$
	protected final static String SEARCH_END = "SEARCH_END"; //$NON-NLS-1$
	protected final static String SEARCH_LAST_FOUND = "SEARCH_LAST_FOUND"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT = "SEARCH_FORMAT"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_ASCII = "SEARCH_FORMAT_ASCII"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_HEX = "SEARCH_FORMAT_HEX"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_OCTAL = "SEARCH_FORMAT_OCTAL"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_BINARY = "SEARCH_FORMAT_BINARY"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_DECIMAL = "SEARCH_FORMAT_DECIMAL"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_BYTESEQUENCE = "SEARCH_FORMAT_BYTESEQUENCE"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_CASEINSENSTIVE = "SEARCH_FORMAT_CASEINSENSTIVE"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_FORWARD = "SEARCH_FORMAT_FORWARD"; //$NON-NLS-1$
	protected final static String SEARCH_FORMAT_WRAP = "SEARCH_FORMAT_WRAP"; //$NON-NLS-1$
	protected final static String SEARCH_ENABLE_FIND_NEXT = "SEARCH_ENABLE_FIND_NEXT"; //$NON-NLS-1$
	
	public FindReplaceDialog(Shell parent, IMemoryBlockExtension memoryBlock, MemoryView memoryView, Properties properties)
	{
		super(parent);
		super.setTitle(Messages.getString("FindReplaceDialog.Title"));  //$NON-NLS-1$
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
		fMemoryView = memoryView;
		fProperties = properties;
		this.setBlockOnOpen(false);
	}
	
	private BigInteger getUserStart()
	{
		String start = fStartText.getText();
		if(start.toUpperCase().startsWith("0X")) //$NON-NLS-1$
			start = start.substring(2);
		return new BigInteger(start, 16);
	}
	
	private BigInteger getUserEnd()
	{
		String end = fEndText.getText();
		if(end.toUpperCase().startsWith("0X")) //$NON-NLS-1$
			end = end.substring(2);
		return new BigInteger(end, 16);
	}
	
	private boolean getIsDirectionForward()
	{
		return fForwardButton.getSelection();
	}
	
	private SearchPhrase getSearchPhrase()
	{
		SearchPhrase phrase = null;
		
		if(fFormatAsciiButton.getSelection())
		{
			phrase = new AsciiSearchPhrase(fFindText.getText(), fCaseInSensitiveCheckbox.getSelection());
		}
		else if(fFormatHexButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(fFindText.getText().toUpperCase().startsWith("0X")  //$NON-NLS-1$
				? fFindText.getText().substring(2) : fFindText.getText(), 16), 16);
		}
		else if(fFormatOctalButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(fFindText.getText().startsWith("0")  //$NON-NLS-1$
					? fFindText.getText().substring(1) : fFindText.getText(), 8), 8);
		}
		else if(fFormatBinaryButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(fFindText.getText().toUpperCase().startsWith("0B")  //$NON-NLS-1$
					? fFindText.getText().substring(2) : fFindText.getText(), 2), 2);
		}
		else if(fFormatDecimalButton.getSelection())
		{
			phrase = new BigIntegerSearchPhrase(new BigInteger(fFindText.getText(), 10), 10);
		}
		else if(fFormatByteSequenceButton.getSelection())
		{
			phrase = new ByteSequenceSearchPhrase(fFindText.getText());
		}
		
		return phrase;
	}
	
	protected byte[] parseByteSequence(String s)
	{
		Vector<Byte> sequence = new Vector<Byte>();
		StringTokenizer st = new StringTokenizer(s, " "); //$NON-NLS-1$
		while(st.hasMoreElements())
		{
			String element = ((String) st.nextElement()).trim();
			if(element.length() > 0)
			{
				BigInteger value;
				if(element.toUpperCase().startsWith("0X")) //$NON-NLS-1$
					value = new BigInteger(element.substring(2), 16);
				else if(element.toUpperCase().startsWith("0B")) //$NON-NLS-1$
					value = new BigInteger(element.substring(2), 2);
				else if(element.toUpperCase().startsWith("0")) //$NON-NLS-1$
					value = new BigInteger(element.substring(1), 8);
				else
					value = new BigInteger(element, 10);
				Byte b = new Byte(value.byteValue());
				
				if(value.compareTo(BigInteger.valueOf(255)) > 0)
					return null;
				
				sequence.addElement(b);
			}
		}
		Byte seq[] = sequence.toArray(new Byte[sequence.size()]);
		byte[] bytes = new byte[seq.length];
		for(int i = 0; i < seq.length; i++)
			bytes[i] = seq[i].byteValue();
		return bytes;
	}
	
	private byte[] getReplaceData()
	{
		if(fFormatAsciiButton.getSelection())
			return fReplaceText.getText().getBytes();
		else if(fFormatHexButton.getSelection())
			return removeZeroPrefixByte(new BigInteger(fReplaceText.getText().toUpperCase().startsWith("0X") ? fReplaceText.getText().substring(2) : fReplaceText.getText(), 16).toByteArray()); //$NON-NLS-1$
		else if(fFormatOctalButton.getSelection())
			return removeZeroPrefixByte(new BigInteger(fReplaceText.getText().startsWith("0") ? fReplaceText.getText().substring(1) : fReplaceText.getText(), 8).toByteArray()); //$NON-NLS-1$
		else if(fFormatBinaryButton.getSelection())
			return removeZeroPrefixByte(new BigInteger(fReplaceText.getText().toUpperCase().startsWith("0B") ? fReplaceText.getText().substring(2) : fReplaceText.getText(), 2).toByteArray()); //$NON-NLS-1$
		else if(fFormatDecimalButton.getSelection())
			return removeZeroPrefixByte(new BigInteger(fReplaceText.getText(), 10).toByteArray());
		else if(fFormatByteSequenceButton.getSelection())
			return parseByteSequence(fReplaceText.getText());
		
		return new byte[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		
		fFindButton = createButton(parent, 10, Messages.getString("FindReplaceDialog.ButtonFind"), true); //$NON-NLS-1$
		fFindButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), null, false, false);
				cancelPressed();
			}
		});
		
		fFindAllButton = createButton(parent, 10, Messages.getString("FindReplaceDialog.ButtonFindAll"), true); //$NON-NLS-1$
		fFindAllButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), null, true, false);
				cancelPressed();
			}
		});
		
		fReplaceFindButton = createButton(parent, 11, Messages.getString("FindReplaceDialog.ButtonReplaceFind"), false); //$NON-NLS-1$
		fReplaceFindButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), getReplaceData(), false, true);
				cancelPressed();
			}
		});
		
		fReplaceButton = createButton(parent, 12, Messages.getString("FindReplaceDialog.ButtonReplace"), false); //$NON-NLS-1$
		fReplaceButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), getReplaceData(), false, false);
				cancelPressed();
			}
		});
		
		fReplaceAllButton = createButton(parent, 13, Messages.getString("FindReplaceDialog.ButtonReplaceAll"), false); //$NON-NLS-1$
		fReplaceAllButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				performFind(getUserStart(), getUserEnd(), getSearchPhrase(), getIsDirectionForward(), getReplaceData(), true, false);
				cancelPressed();
			}
		});
		
		fCloseButton = createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("FindReplaceDialog.Close"), false); //$NON-NLS-1$
		
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
		
		fProperties.setProperty(SEARCH_FIND, fFindText.getText());
		fProperties.setProperty(SEARCH_REPLACE, fReplaceText.getText());
		fProperties.setProperty(SEARCH_START, fStartText.getText());
		fProperties.setProperty(SEARCH_END, fEndText.getText());
		if(fFormatAsciiButton.getSelection())
			fProperties.setProperty(SEARCH_FORMAT, SEARCH_FORMAT_ASCII);
		else if(fFormatBinaryButton.getSelection())
			fProperties.setProperty(SEARCH_FORMAT, SEARCH_FORMAT_BINARY);
		else if(fFormatByteSequenceButton.getSelection())
			fProperties.setProperty(SEARCH_FORMAT, SEARCH_FORMAT_BYTESEQUENCE);
		else if(fFormatDecimalButton.getSelection())
			fProperties.setProperty(SEARCH_FORMAT, SEARCH_FORMAT_DECIMAL);
		else if(fFormatHexButton.getSelection())
			fProperties.setProperty(SEARCH_FORMAT, SEARCH_FORMAT_HEX);
		else if(fFormatOctalButton.getSelection())
			fProperties.setProperty(SEARCH_FORMAT, SEARCH_FORMAT_OCTAL);
		
		fProperties.setProperty(SEARCH_FORMAT_FORWARD, "" + fForwardButton.getSelection());
		
		fProperties.setProperty(SEARCH_FORMAT_CASEINSENSTIVE, "" + fCaseInSensitiveCheckbox.getSelection());
		
		fProperties.setProperty(SEARCH_FORMAT_WRAP, "" + fWrapCheckbox.getSelection());

		fProperties.setProperty(SEARCH_ENABLE_FIND_NEXT, "false");
		
		setResult(null);
		
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		setSelectionResult(new Object[]{ fProperties });
		
		super.okPressed();
	}
	
	public BigInteger getEndAddress()
	{
		String text = fEndText.getText();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger endAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return endAddress;
	}
	
	public BigInteger getStartAddress()
	{
		String text = fStartText.getText();
		boolean hex = text.startsWith("0x"); //$NON-NLS-1$
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return startAddress;
	}
	
	private void validate()
	{
		boolean valid = false;
		boolean replaceValid = false;
		
		try
		{
			BigInteger endAddress = getEndAddress();
			BigInteger startAddress = getStartAddress();
			
			/*
			 * The end-address must be larger that the start-address.
			 */
			if ( startAddress.compareTo(endAddress) == -1 ) {
				/*
				 * Validate the search phrase.
				 */
				if(getSearchPhrase() != null && getSearchPhrase().getByteLength() > 0) {
					valid = true;
				}
				
				/*
				 * Validate the replacement phrase.
				 */
				if(getReplaceData() != null && getReplaceData().length > 0) {
					replaceValid = true;
				}
			}
		}
		catch(Throwable ex)
		{
			// do nothing
		}
		
		fFindButton.setEnabled(valid);
		fFindAllButton.setEnabled(valid);
		fReplaceButton.setEnabled(replaceValid);
		fReplaceFindButton.setEnabled(replaceValid);
		fReplaceAllButton.setEnabled(replaceValid);
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
			// do nothing
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
			// do nothing
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
			// do nothing
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

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MemorySearchPlugin.getUniqueIdentifier() + ".MemorySearchDialog_context"); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = formLayout.marginHeight = 9;
		composite.setLayout(formLayout);

		// find
		
		Label findLabel = new Label(composite, SWT.NONE);
		Label replaceLabel = new Label(composite, SWT.NONE);
		fReplaceText = new Text(composite, SWT.BORDER);

		findLabel.setText(Messages.getString("FindReplaceDialog.LabelFind"));  //$NON-NLS-1$
		
		fFindText = new Text(composite, SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(fReplaceText, 0, SWT.LEFT);
		data.width = 260;
		fFindText.setLayoutData(data);
		fFindText.setText(fProperties.getProperty(SEARCH_FIND, ""));
		
		data = new FormData();
		data.top = new FormAttachment(fFindText, 0, SWT.CENTER);
		findLabel.setLayoutData(data);
		
		// replace
		
		replaceLabel.setText(Messages.getString("FindReplaceDialog.LabelReplaceWith"));  //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(fFindText);
		replaceLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(replaceLabel, 0, SWT.CENTER);
		data.left = new FormAttachment(replaceLabel);
		data.width = 260;
		fReplaceText.setLayoutData(data);
		fReplaceText.setText(fProperties.getProperty(SEARCH_REPLACE, ""));
		
		// group direction
		
		Group directionGroup = new Group(composite, SWT.NONE);
		Group formatGroup = new Group(composite, SWT.NONE);
		Group rangeGroup = new Group(composite, SWT.NONE);
		directionGroup.setText(Messages.getString("FindReplaceDialog.LabelDirection")); //$NON-NLS-1$
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		directionGroup.setLayout(layout);
		
		fForwardButton = new Button(directionGroup, SWT.RADIO);
		fForwardButton.setText(Messages.getString("FindReplaceDialog.ButtonForward")); //$NON-NLS-1$
		final Button backwardButton = new Button(directionGroup, SWT.RADIO);
		backwardButton.setText(Messages.getString("FindReplaceDialog.ButtonBackward")); //$NON-NLS-1$
		final boolean isForward = fProperties.getProperty(SEARCH_FORMAT_FORWARD, "true").equalsIgnoreCase("true");
		fForwardButton.setSelection(isForward);
		backwardButton.setSelection(!isForward);
		
		data = new FormData();
		data.top = new FormAttachment(fReplaceText);
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
		
		fStartText = new Combo(rangeGroup, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 200;
		gridData.grabExcessHorizontalSpace = true;
		fStartText.setLayoutData(gridData);
		
		// group range - end address
		
		Label endLabel = new Label(rangeGroup, SWT.NONE);
		endLabel.setText(Messages.getString("FindReplaceDialog.LabelEndAddress"));  //$NON-NLS-1$
		fEndText = new Combo(rangeGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 200;
		gridData.grabExcessHorizontalSpace = true;
		fEndText.setLayoutData(gridData);
		
		data = new FormData();
		data.left = new FormAttachment(directionGroup);	
		data.top = new FormAttachment(directionGroup, 0, SWT.TOP);
		data.right = new FormAttachment(fFindText, 0, SWT.RIGHT);
		rangeGroup.setLayoutData(data);
		
		fStartText.setItems(removeNullElements(new String[] { getViewportStart(), getStart(), getEnd(), getMemoryBlockBaseAddress() }));
		fEndText.setItems(removeNullElements(new String[] { getEnd(), getStart(), getMemoryBlockBaseAddress(), getViewportStart() }));
		if(fProperties.getProperty(SEARCH_START) != null)
			fStartText.add(fProperties.getProperty(SEARCH_START), 0);
		if(fProperties.getProperty(SEARCH_END) != null)
			fEndText.add(fProperties.getProperty(SEARCH_END), 0);
		fStartText.select(0);
		fEndText.select(0);
		
		// format group
		
		formatGroup.setText(Messages.getString("FindReplaceDialog.LabelFormat")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 1;
		formatGroup.setLayout(layout);
		
		fFormatAsciiButton = new Button(formatGroup, SWT.RADIO);
		fFormatAsciiButton.setText(Messages.getString("FindReplaceDialog.ButtonASCII")); //$NON-NLS-1$
		
		fFormatHexButton = new Button(formatGroup, SWT.RADIO);
		fFormatHexButton.setText(Messages.getString("FindReplaceDialog.ButtonHexadecimal")); //$NON-NLS-1$
		
		fFormatOctalButton = new Button(formatGroup, SWT.RADIO);
		fFormatOctalButton.setText(Messages.getString("FindReplaceDialog.ButtonOctal")); //$NON-NLS-1$
		
		fFormatBinaryButton = new Button(formatGroup, SWT.RADIO);
		fFormatBinaryButton.setText(Messages.getString("FindReplaceDialog.ButtonBinary")); //$NON-NLS-1$
		
		fFormatDecimalButton = new Button(formatGroup, SWT.RADIO);
		fFormatDecimalButton.setText(Messages.getString("FindReplaceDialog.ButtonDecimal")); //$NON-NLS-1$
		
		fFormatByteSequenceButton = new Button(formatGroup, SWT.RADIO);
		fFormatByteSequenceButton.setText(Messages.getString("FindReplaceDialog.ButtonByteSequence")); //$NON-NLS-1$
		
		final String format = fProperties.getProperty(SEARCH_FORMAT, FindReplaceDialog.SEARCH_FORMAT_ASCII);
		
		fFormatAsciiButton.setSelection(format.equals(SEARCH_FORMAT_ASCII));
		fFormatOctalButton.setSelection(format.equals(SEARCH_FORMAT_OCTAL));
		fFormatBinaryButton.setSelection(format.equals(SEARCH_FORMAT_BINARY));
		fFormatDecimalButton.setSelection(format.equals(SEARCH_FORMAT_DECIMAL));
		fFormatHexButton.setSelection(format.equals(SEARCH_FORMAT_HEX));
		fFormatByteSequenceButton.setSelection(format.equals(SEARCH_FORMAT_BYTESEQUENCE));
			
		
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
		
		fWrapCheckbox = new Button(optionsGroup, SWT.CHECK);
		fWrapCheckbox.setText(Messages.getString("FindReplaceDialog.ButtonWrapSearch")); //$NON-NLS-1$
		fWrapCheckbox.setEnabled(false); // TODO implement wrap
		
		fCaseInSensitiveCheckbox = new Button(optionsGroup, SWT.CHECK);
		fCaseInSensitiveCheckbox.setText(Messages.getString("FindReplaceDialog.ButtonCaseInsensitive")); //$NON-NLS-1$
		
		fFormatAsciiButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				fCaseInSensitiveCheckbox.setEnabled(true);
			}
		});
		
		SelectionListener nonAsciiListener = new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				fCaseInSensitiveCheckbox.setEnabled(false);
				validate();
			}
		};
		fFormatHexButton.addSelectionListener(nonAsciiListener);
		fFormatOctalButton.addSelectionListener(nonAsciiListener);
		fFormatBinaryButton.addSelectionListener(nonAsciiListener);
		fFormatDecimalButton.addSelectionListener(nonAsciiListener);
		fFormatByteSequenceButton.addSelectionListener(nonAsciiListener);

		fStartText.addModifyListener(new ModifyListener() {
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
				
				fStartText.setForeground(valid ? Display.getDefault().getSystemColor(SWT.COLOR_BLACK) : 
					Display.getDefault().getSystemColor(SWT.COLOR_RED));
				
				validate();
			}
			
		});
		
		fEndText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try
				{
					getEndAddress();
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				}
				catch(Exception ex)
				{
					fEndText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
				
				validate();
			}
			
		});
		
		fFindText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});
		
		fReplaceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});
		
		composite.setTabList(new Control[] {
			fFindText, 
			fReplaceText, 			
			directionGroup,
			rangeGroup,
			formatGroup,
			optionsGroup,
		});
			
		fFindText.setFocus();
		
		return composite;
	}
	

	class FindReplaceMemoryCache
	{
		BigInteger memoryCacheStartAddress = BigInteger.ZERO;
		MemoryByte memoryCacheData[] = new MemoryByte[0];
	}
	
	MemoryByte[] getBytesFromAddress(final BigInteger address, final int length, FindReplaceMemoryCache cache) 
		throws DebugException
	{
		if(! (address.compareTo(cache.memoryCacheStartAddress) >= 0 
			&& address.add(BigInteger.valueOf(length)).compareTo(cache.memoryCacheStartAddress
					.add(BigInteger.valueOf(cache.memoryCacheData.length))) < 0))
		{
			BigInteger fetchAddress = address.subtract(BigInteger.valueOf(preFetchSize));
			if(fetchAddress.compareTo(BigInteger.ZERO) < 0) // TODO replace ZERO with address space start
				fetchAddress = BigInteger.ZERO;
			
			MemoryByte bytes[] = fMemoryBlock.getBytesFromAddress(fetchAddress, preFetchSize * 2);
			
			cache.memoryCacheStartAddress = fetchAddress;
			cache.memoryCacheData = bytes;
		}
		
		MemoryByte bytes[] = new MemoryByte[length];
		System.arraycopy(cache.memoryCacheData, address.subtract(cache.memoryCacheStartAddress).intValue(), 
			bytes, 0, length);
		return bytes;
	}
	
	private BigInteger parseHexBigInteger(String s)
	{
		if(s.toUpperCase().startsWith("0X"))
			return new BigInteger(s.substring(2), 16);
		else
			return new BigInteger(s, 16);
	}
	
	protected void performFindNext()
	{
		try
		{
			BigInteger start = parseHexBigInteger(fProperties.getProperty(SEARCH_LAST_FOUND));
			BigInteger end = parseHexBigInteger(fProperties.getProperty(SEARCH_END));
			boolean searchForward = fProperties.getProperty(SEARCH_FORMAT_FORWARD, "false").equals("true");
			boolean caseInSensitive = fProperties.getProperty(SEARCH_FORMAT_CASEINSENSTIVE, "false").equals("true");
			if(searchForward)
				start = start.add(BigInteger.ONE);
			else
				start = start.subtract(BigInteger.ONE);
			SearchPhrase phrase = null;
			String findText = fProperties.getProperty(SEARCH_FIND);
			
			if(fProperties.getProperty(SEARCH_FORMAT).equals(SEARCH_FORMAT_ASCII))
				phrase = new AsciiSearchPhrase(findText, caseInSensitive);
			else if(fProperties.getProperty(SEARCH_FORMAT).equals(SEARCH_FORMAT_HEX))
				phrase = new BigIntegerSearchPhrase(new BigInteger(findText.toUpperCase().startsWith("0X")  //$NON-NLS-1$
					? findText.substring(2) : findText, 16), 16);
			else if(fProperties.getProperty(SEARCH_FORMAT).equals(SEARCH_FORMAT_OCTAL))
				phrase = new BigIntegerSearchPhrase(new BigInteger(findText.startsWith("0")  //$NON-NLS-1$
					? findText.substring(1) : findText, 8), 8);
			else if(fProperties.getProperty(SEARCH_FORMAT).equals(SEARCH_FORMAT_BINARY))
				phrase = new BigIntegerSearchPhrase(new BigInteger(findText.toUpperCase().startsWith("0B")  //$NON-NLS-1$
					? findText.substring(2) : findText, 2), 2);
			else if(fProperties.getProperty(SEARCH_FORMAT).equals(SEARCH_FORMAT_DECIMAL))
				phrase = new BigIntegerSearchPhrase(new BigInteger(findText, 10), 10);
			else if(fProperties.getProperty(SEARCH_FORMAT).equals(SEARCH_FORMAT_BYTESEQUENCE))
				phrase = new ByteSequenceSearchPhrase(findText);
			
			performFind(start, end, phrase, searchForward, null, false, false);
			
		}
		catch(Exception e)
		{
			MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.MemorySearchFailure"), e);
		}
	}
	
	private void performFind(final BigInteger start, final BigInteger end, final SearchPhrase searchPhrase, 
		final boolean searchForward, final byte[] replaceData, final boolean all, final boolean replaceThenFind)
	{		
		final ISearchQuery query = new IMemorySearchQuery() 
		{
			private ISearchResult fSearchResult = null;
			
			public boolean canRerun() {
				return false;
			}

			public boolean canRunInBackground() {
				return true;
			}

			public String getLabel() {
				return Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase; //$NON-NLS-1$
			}

			public ISearchResult getSearchResult() {
				if(fSearchResult == null)
					fSearchResult = new MemorySearchResult(this, Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase);	 //$NON-NLS-1$
				return fSearchResult;
			}

			public IStatus run(IProgressMonitor monitor)
					throws OperationCanceledException {

				final BigInteger searchPhraseLength = BigInteger.valueOf(searchPhrase.getByteLength());
				BigInteger range = start.subtract(end);
				//BigInteger range = searchForward ? end.subtract(start) : start.subtract(end);
				//BigInteger currentPosition = start;
				BigInteger currentPosition = searchForward ? start : end.subtract(searchPhraseLength);

				if ( range.compareTo(searchPhraseLength) >= 0 ) {
					return Status.OK_STATUS;
				}
				
				boolean isReplace = replaceData != null;
				
				BigInteger jobs = range;
				BigInteger factor = BigInteger.ONE;
				if(jobs.compareTo(BigInteger.valueOf(0x07FFFFFF)) > 0)
				{
					factor = jobs.divide(BigInteger.valueOf(0x07FFFFFF));
					jobs = jobs.divide(factor);
				}
				
				BigInteger jobCount = BigInteger.ZERO;
				
				BigInteger replaceCount = BigInteger.ZERO;
				
				FindReplaceMemoryCache cache = new FindReplaceMemoryCache();
				
				monitor.beginTask(Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase, jobs.intValue()); //$NON-NLS-1$
		
				boolean matched = false;
				while(((searchForward && currentPosition.compareTo(end.subtract(searchPhraseLength)) < 0) 
					|| (!searchForward && currentPosition.compareTo(start) > 0)) && !monitor.isCanceled()) 
				{
					try
					{
						MemoryByte bytes[] = getBytesFromAddress(currentPosition, searchPhraseLength.intValue(), cache);
						matched = searchPhrase.isMatch(bytes);
						if(matched)
						{
							if(all && !isReplace)
								((MemorySearchResult) getSearchResult()).addMatch(new MemoryMatch(currentPosition, searchPhraseLength));
						
							if(isReplace)
							{
								try
								{
									fMemoryBlock.setValue(currentPosition.subtract(fMemoryBlock.getBigBaseAddress()), replaceData);
								}
								catch(DebugException de)
								{
									MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.MemoryReadFailed"), de); //$NON-NLS-1$
								}

								replaceCount = replaceCount.add(BigInteger.ONE);
							}
							
							if(isReplace && replaceThenFind && replaceCount.compareTo(BigInteger.ONE) == 0)
							{
								isReplace = false;
								matched = false;
							}
							
							if(matched && !all)
							{
								final BigInteger finalCurrentPosition = currentPosition;
								Display.getDefault().asyncExec(new Runnable(){

									public void run() {
										IMemoryRenderingContainer containers[] = getMemoryView().getMemoryRenderingContainers();
										for(int i = 0; i < containers.length; i++)
										{
											IMemoryRendering rendering = containers[i].getActiveRendering();
											if(rendering instanceof IRepositionableMemoryRendering)
											{
												try {
													((IRepositionableMemoryRendering) rendering).goToAddress(finalCurrentPosition);
												} catch (DebugException e) {
													MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.RepositioningMemoryViewFailed"), e); //$NON-NLS-1$
												}
											}
											if(rendering != null)
											{
												// Temporary, until platform accepts/adds new interface for setting the selection
												try {
													Method m = rendering.getClass().getMethod("setSelection", new Class[] { BigInteger.class, BigInteger.class } );
													if(m != null)
														m.invoke(rendering, finalCurrentPosition, finalCurrentPosition.add(searchPhraseLength));
												} catch (Exception e) {
													// do nothing
												}
											}
										}
									}
									
								});
								
								fProperties.setProperty(SEARCH_ENABLE_FIND_NEXT, "true");
								fProperties.setProperty(SEARCH_LAST_FOUND, "0x" + finalCurrentPosition.toString(16));
								return Status.OK_STATUS;
							}
						}
						
						matched = false;
						
						if(searchForward)
							currentPosition = currentPosition.add(BigInteger.ONE);
						else
							currentPosition = currentPosition.subtract(BigInteger.ONE);
						
					}
					catch(DebugException e)
					{
						MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.MemorySearchFailure"), e); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					}
					
					jobCount = jobCount.add(BigInteger.ONE);
					if(jobCount.compareTo(factor) == 0)
					{
						jobCount = BigInteger.ZERO;
						monitor.worked(1);
					}	
				}
				
				if(monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				return Status.OK_STATUS;
			}

			public MemoryView getMemoryView() {
				return fMemoryView;
			}
		};
		
		if(all && replaceData == null)
		{
			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					NewSearchUI.activateSearchResultView();
					NewSearchUI.runQueryInBackground(query);
				}
			});
		}
		else
		{
			Job job = new Job("Searching memory for " + searchPhrase){ //$NON-NLS-1$
				public IStatus run(IProgressMonitor monitor) {
					return query.run(monitor);
				}
			};
			job.schedule();
		}
			
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
	
	class ByteSequenceSearchPhrase implements SearchPhrase
	{
		private byte[] fBytes = null;
		
		public ByteSequenceSearchPhrase(String phrase)
		{
			fBytes = parseByteSequence(phrase);
		}
		
		public int getByteLength()
		{
			if ( fBytes != null ) {
				return fBytes.length;
			}
			else {
				return 0;
			}
		}
		
		public String toString()
		{
			if(fBytes == null)
				return ""; //$NON-NLS-1$
			StringBuffer buf = new StringBuffer();
			for(int i = 0; i < fBytes.length; i++)
				buf.append(BigInteger.valueOf(fBytes[i]).toString(16) + " "); //$NON-NLS-1$
			return buf.toString();
		}
		
		public boolean isMatch(MemoryByte[] bytes)
		{
			if ( fBytes == null )
				return false;
			for(int i = 0; i < bytes.length; i++)
				if(bytes[i].getValue() != fBytes[i])
					return false;
			return true;
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
			return removeZeroPrefixByte(fPhrase.toByteArray()).length;
		}
		
		public String toString()
		{
			return fPhrase.toString(fRadix);
		}
		
		public boolean isMatch(MemoryByte[] bytes)
		{
			byte[] targetBytes = new byte[bytes.length + 1];
			targetBytes[0] = 0;
			for(int i = 0; i < bytes.length; i++)
				targetBytes[i + 1] = bytes[i].getValue();
			
			// TODO endian?
			BigInteger targetBigInteger = new BigInteger(targetBytes);

			return fPhrase.equals(targetBigInteger);
		}
	}
	
	private byte[] removeZeroPrefixByte(byte[] bytes)
	{
		if(bytes[0] != 0 || bytes.length == 1)
			return bytes;
		
		byte[] processedBytes = new byte[bytes.length - 1];
		System.arraycopy(bytes, 1, processedBytes, 0, processedBytes.length);
		return processedBytes;
	}
	
	interface IMemorySearchQuery extends ISearchQuery
	{
		public MemoryView getMemoryView();
	};
	
}
