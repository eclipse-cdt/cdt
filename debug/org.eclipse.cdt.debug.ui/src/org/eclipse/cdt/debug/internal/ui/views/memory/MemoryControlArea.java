/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * The tab content in the memory view.
 * 
 * @since Jul 25, 2002
 */
public class MemoryControlArea extends Composite
{
	private MemoryPresentation fPresentation;
	private int fIndex = 0;
	private IFormattedMemoryRetrieval fInput = null;
	private IFormattedMemoryBlock fMemoryBlock = null;

	private Text fAddressText;
	private MemoryText fMemoryText;
	/**
	 * Constructor for MemoryControlArea.
	 * @param parent
	 * @param style
	 */
	public MemoryControlArea( Composite parent, int style, int index )
	{
		super( parent, style );
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		GridData gridData = new GridData( GridData.FILL_BOTH | 
										  GridData.GRAB_HORIZONTAL | 
										  GridData.GRAB_VERTICAL );
		setLayout( layout );
		setLayoutData( gridData );
		fIndex = index;
		fPresentation = createPresentation();
		fAddressText = createAddressText( this );
		fMemoryText = createMemoryText( this, style, fPresentation );
	}

	private MemoryPresentation createPresentation()
	{
		IPreferenceStore pstore = CDebugUIPlugin.getDefault().getPreferenceStore();
		char[] paddingCharStr = pstore.getString( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR ).toCharArray();
		char paddingChar = ( paddingCharStr.length > 0 ) ? paddingCharStr[0] : '.';
		return new MemoryPresentation();
	}

	public MemoryPresentation getPresentation()
	{
		return fPresentation;
	}
	
	private Text createAddressText( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout( 2, false ) );
		composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		// create label
		Label label = new Label( composite, SWT.RIGHT );
		label.setText( "Address: " );
		label.pack();
	
		// create address text
		Text text = new Text( composite, SWT.BORDER );
		text.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		text.addKeyListener( new KeyAdapter()
								  {
									  public void keyReleased( KeyEvent e )
									  {
										  if ( e.character == SWT.CR && e.stateMask == 0 )
										      handleAddressEnter();
									  }
								  } );
		return text;
	}

	private MemoryText createMemoryText(  Composite parent, 
										  int styles,
										  MemoryPresentation presentation )
	{
		return new MemoryText( parent, SWT.BORDER | SWT.HIDE_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL, presentation );
	}

	private void handleAddressEnter()
	{
		String address = fAddressText.getText().trim();
	}

	public void propertyChange( PropertyChangeEvent event )
	{
		if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB ) )
		{
			fMemoryText.setBackgroundColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB ) )
		{
			fMemoryText.setForegroundColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_FONT ) )
		{
			fMemoryText.changeFont();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_ADDRESS_RGB ) )
		{
			fMemoryText.setAddressColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_CHANGED_RGB ) )
		{
			fMemoryText.setChangedColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_DIRTY_RGB ) )
		{
			fMemoryText.setDirtyColor();
		}
		else
		{
//			updatePresentation( event );
//			fMemoryText.refresh();
		}
	}
	
	public void setInput( IFormattedMemoryRetrieval input )
	{
		fInput = input;
		fMemoryBlock = CDebugUIPlugin.getDefault().getBlock( fInput, fIndex );
		fPresentation.setMemoryBlock( fMemoryBlock );
		refresh();		
	}
	
	private void refresh()
	{
		fAddressText.setText( fPresentation.getStartAddress() );
		fMemoryText.refresh();
	}
/*	
	private void updatePresentation( PropertyChangeEvent event )
	{
		if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_MEMORY_SIZE ) )
		{
			fPresentation.setSize( CDebugUIPlugin.getDefault().getPreferenceStore().getInt( ICDebugPreferenceConstants.PREF_MEMORY_SIZE ) );
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_MEMORY_BYTES_PER_ROW ) )
		{
			fPresentation.setBytesPerRow( CDebugUIPlugin.getDefault().getPreferenceStore().getInt( ICDebugPreferenceConstants.PREF_MEMORY_BYTES_PER_ROW ) );
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_MEMORY_DISPLAY_ASCII ) )
		{
			fPresentation.setDisplayASCII( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_MEMORY_DISPLAY_ASCII ) );
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_MEMORY_FORMAT ) )
		{
			fPresentation.setFormat( CDebugUIPlugin.getDefault().getPreferenceStore().getInt( ICDebugPreferenceConstants.PREF_MEMORY_FORMAT ) );
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR ) )
		{
			fPresentation.setPaddingChar( CDebugUIPlugin.getDefault().getPreferenceStore().getString( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR ).charAt( 0 ) );
		}
	}
*/
}
