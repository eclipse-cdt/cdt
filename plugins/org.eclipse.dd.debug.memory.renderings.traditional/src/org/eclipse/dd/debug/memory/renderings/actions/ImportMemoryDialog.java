package org.eclipse.dd.debug.memory.renderings.actions;

import java.io.File;
import java.math.BigInteger;

import javax.swing.ButtonGroup;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ImportMemoryDialog extends SelectionDialog 
{

	private Combo formatCombo;
	
	private IMemoryBlock fMemoryBlock;
	
	private Text startText;
	private Text fileText;
	
	private Button comboRestoreToThisAddress;
	private Button comboRestoreToFileAddress;
	
	public ImportMemoryDialog(Shell parent, IMemoryBlock memoryBlock)
	{
		super(parent);
		super.setTitle("Download to Memory");  
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		fMemoryBlock = memoryBlock;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
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
		setSelectionResult(new Object[]{ getFormat(), new Boolean(comboRestoreToThisAddress.getSelection()),
			getStartAddress(), getFile() });
		
		super.okPressed();
	}
	
	public String getFormat()
	{
		return formatCombo.getItem(formatCombo.getSelectionIndex());
	}
	
	public BigInteger getStartAddress()
	{
		String text = startText.getText();
		boolean hex = text.startsWith("0x");
		BigInteger startAddress = new BigInteger(hex ? text.substring(2) : text,
			hex ? 16 : 10); 
		
		return startAddress;
	}
	
	public File getFile()
	{
		return new File(fileText.getText());
	}
	
	private void validate()
	{
		boolean isValid = true;
		
		try
		{
			getStartAddress();
		}
		catch(Exception e)
		{
			isValid = false;
		}
		
		getButton(IDialogConstants.OK_ID).setEnabled(isValid);
			
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
		
		// format
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Format: "); 
		
		formatCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		
		FormData data = new FormData();
		data.top = new FormAttachment(formatCombo, 0, SWT.CENTER);
		textLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(textLabel);
		formatCombo.setLayoutData(data);
		formatCombo.setItems( new String[] { "S-Record" }); // TODO offer extension point
		formatCombo.select(0);
		
		// restore to file address
		
		comboRestoreToFileAddress = new Button(composite, SWT.RADIO);
		comboRestoreToFileAddress.setText("Restore to address specified in the file"); 
		data = new FormData();
		data.top = new FormAttachment(formatCombo);
		comboRestoreToFileAddress.setLayoutData(data);
		
		// restore to this address
		
		comboRestoreToThisAddress = new Button(composite, SWT.RADIO);
		comboRestoreToThisAddress.setText("Restore to this address: "); 
		data = new FormData();
		data.top = new FormAttachment(comboRestoreToFileAddress);
		comboRestoreToThisAddress.setLayoutData(data);
		
		startText = new Text(composite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(comboRestoreToFileAddress);
		data.left = new FormAttachment(comboRestoreToThisAddress);
		data.width = 100;
		startText.setLayoutData(data);
		
		// file
		
		Label fileLabel = new Label(composite, SWT.NONE);
		fileText = new Text(composite, SWT.NONE);
		Button fileButton = new Button(composite, SWT.PUSH);
		
		fileLabel.setText("File name: "); 
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(fileButton, 0, SWT.CENTER);
		data.left = new FormAttachment(fileLabel);
		data.width = 300;
		fileText.setLayoutData(data);
		
		fileButton.setText("Browse...");
		data = new FormData();
		data.top = new FormAttachment(startText);
		data.left = new FormAttachment(fileText);
		fileButton.setLayoutData(data);
		
		try
		{
			BigInteger startAddress = null;
			if(fMemoryBlock instanceof IMemoryBlockExtension)
				startAddress = ((IMemoryBlockExtension) fMemoryBlock)
					.getBigBaseAddress(); // FIXME use selection/caret address?
			else
				startAddress = BigInteger.valueOf(fMemoryBlock.getStartAddress());
			
			startText.setText("0x" + startAddress.toString(16));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// TODO
		}
		
		fileButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(ImportMemoryDialog.this.getShell(), SWT.SAVE);
				dialog.setText("Choose memory export file");
				dialog.setFilterExtensions(new String[] { "*.*" } );
				dialog.setFilterNames(new String[] { "All Files (*.*)" } );
				dialog.setFileName(fileText.getText());
				dialog.open();
			
				if(dialog.getFileName() != null)
				{
					fileText.setText(dialog.getFilterPath() + File.separator + dialog.getFileName());
				}
				
				validate();
			}
			
		});
		
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

				validate();
			}
			
		});
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		return composite;
	}

	
}
