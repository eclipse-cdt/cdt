/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.CSourceLocator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * A source locator that prompts the user to find source when source cannot
 * be found on the current source lookup path.
 * 
 * @since Sep 24, 2002
 */
public class CUISourceLocator implements IPersistableSourceLocator
{
	/**
	 * Identifier for the 'Prompting C/C++ Source Locator' extension
	 * (value <code>"org.eclipse.cdt.debug.ui.cSourceLocator"</code>).
	 */
	public static final String ID_PROMPTING_C_SOURCE_LOCATOR = CDebugUIPlugin.getUniqueIdentifier() + ".cSourceLocator"; //$NON-NLS-1$

	/**
	 * The project being debugged.
	 */
	private IProject fProject = null; 
	
	/**
	 * Underlying source locator.
	 */
	private CSourceLocator fSourceLocator;
	
	/**
	 * Whether the user should be prompted for source.
	 * Initially true, until the user checks the 'do not
	 * ask again' box.
	 */
	private boolean fAllowedToAsk;


	/**
	 * Constructor for CUISourceLocator.
	 */
	public CUISourceLocator()
	{
		fSourceLocator = new CSourceLocator();
		fAllowedToAsk = true;
	}

	/**
	 * Constructor for CUISourceLocator.
	 */
	public CUISourceLocator( IProject project )
	{
		fProject = project;
		fSourceLocator = new CSourceLocator( project );
		fAllowedToAsk = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		return fSourceLocator.getMemento();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(String)
	 */
	public void initializeFromMemento( String memento ) throws CoreException
	{
		fSourceLocator.initializeFromMemento( memento );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException
	{
		fSourceLocator.initializeDefaults( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object res = fSourceLocator.getSourceElement( stackFrame );
		if ( res == null && fAllowedToAsk )
		{
			IStackFrameInfo frameInfo = (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class );
			if ( frameInfo != null )
			{
				showDebugSourcePage( frameInfo.getFile(), stackFrame.getLaunch().getLaunchConfiguration() );
				res = fSourceLocator.getSourceElement( stackFrame );
			}
		}
		return res;
	}

	/**
	 * Prompts to locate the source of the given type. 
	 * 
	 * @param frameInfo the frame information for which source
	 *  could not be located
	 */
	private void showDebugSourcePage( final String fileName, final ILaunchConfiguration lc )
	{
		Runnable prompter = new Runnable()
								{
									public void run()
									{
										SourceLookupDialog dialog = new SourceLookupDialog( CDebugUIPlugin.getActiveWorkbenchShell(), fileName, lc, CUISourceLocator.this );
										dialog.open();
										fAllowedToAsk = !dialog.isNotAskAgain();
									}
								};
		CDebugUIPlugin.getStandardDisplay().syncExec( prompter );
	}

	/**
	 * Dialog that prompts for source lookup path.
	 */
	private static class SourceLookupDialog extends Dialog
	{

		private CUISourceLocator fLocator;
		private ILaunchConfiguration fConfiguration;
		private String fFileName;
		private boolean fNotAskAgain;
		private Button fAskAgainCheckBox;

		public SourceLookupDialog( Shell shell,
								   String fileName,
								   ILaunchConfiguration configuration,
								   CUISourceLocator locator )
		{
			super( shell );
			fFileName = fileName;
			fNotAskAgain = false;
			fAskAgainCheckBox = null;
			fLocator = locator;
			fConfiguration = configuration;
		}

		public boolean isNotAskAgain()
		{
			return fNotAskAgain;
		}

		protected Control createDialogArea(Composite parent)
		{
			getShell().setText( "Debugger Source Lookup" );

			Composite composite = (Composite)super.createDialogArea( parent );
			composite.setLayout( new GridLayout() );
			Label message = new Label( composite, SWT.LEFT + SWT.WRAP );
			message.setText( MessageFormat.format( "The source could not be shown as the file ''{0}'' was not found.", new String[] { fFileName }  ) );
			GridData data = new GridData();
			data.widthHint = convertWidthInCharsToPixels( message, 70 );
			message.setLayoutData( data );
			fAskAgainCheckBox = new Button( composite, SWT.CHECK + SWT.WRAP );
			data = new GridData();
			data.widthHint = convertWidthInCharsToPixels( fAskAgainCheckBox, 70 );
			fAskAgainCheckBox.setLayoutData(data);
			fAskAgainCheckBox.setText( "Do &not ask again" );

			return composite;
		}

		/**
		 * @see Dialog#convertWidthInCharsToPixels(FontMetrics, int)
		 */
		protected int convertWidthInCharsToPixels( Control control, int chars )
		{
			GC gc = new GC( control );
			gc.setFont( control.getFont() );
			FontMetrics fontMetrics = gc.getFontMetrics();
			gc.dispose();
			return Dialog.convertWidthInCharsToPixels( fontMetrics, chars );
		}

		protected void okPressed()
		{
			try
			{
				if ( fAskAgainCheckBox != null )
				{
					fNotAskAgain = fAskAgainCheckBox.getSelection();
				}
				ILaunchConfigurationWorkingCopy wc = fConfiguration.getWorkingCopy();
				if ( !fConfiguration.contentsEqual( wc ) )
				{
					fConfiguration = wc.doSave();
					fLocator.initializeDefaults( fConfiguration );
				}
			}
			catch( CoreException e )
			{
				CDebugUIPlugin.log( e );
			}
			super.okPressed();
		}
	}
}
