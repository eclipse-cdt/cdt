/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
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
	private MemoryView fMemoryView;
	private MemoryPresentation fPresentation;
	private int fIndex = 0;
	private ICMemoryManager fMemoryManager = null;

	private Text fAddressText;
	private MemoryText fMemoryText;
	
	private int fFormat = ICMemoryManager.MEMORY_FORMAT_HEX;
	private int fWordSize = ICMemoryManager.MEMORY_SIZE_BYTE;
	private int fNumberOfRows = 40;
	private int fNumberOfColumns = 16;
	private char fPaddingChar = '.';

	/**
	 * Constructor for MemoryControlArea.
	 * @param parent
	 * @param style
	 */
	public MemoryControlArea( Composite parent, int style, int index, MemoryView view )
	{
		super( parent, style );
		fMemoryView = view;
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		GridData gridData = new GridData( GridData.FILL_BOTH | 
										  GridData.GRAB_HORIZONTAL | 
										  GridData.GRAB_VERTICAL );
		setLayout( layout );
		setLayoutData( gridData );
		setIndex( index );
		fPresentation = createPresentation();
		fAddressText = createAddressText( this );
		fMemoryText = createMemoryText( this, style, fPresentation );
	}

	private MemoryPresentation createPresentation()
	{
/*
		IPreferenceStore pstore = CDebugUIPlugin.getDefault().getPreferenceStore();
		char[] paddingCharStr = pstore.getString( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR ).toCharArray();
		char paddingChar = ( paddingCharStr.length > 0 ) ? paddingCharStr[0] : '.';
*/
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

	protected void handleAddressEnter()
	{
		if ( getMemoryManager() != null )
		{
			String address = fAddressText.getText().trim();
			try
			{
				removeBlock();
				if ( address.length() > 0 )
				{
					createBlock( address );
				}
			}
			catch( DebugException e )
			{
				CDebugUIPlugin.errorDialog( "Unable to get memory block.", e.getStatus() );
			}
			refresh();
			fMemoryView.updateObjects();
		}
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
	
	public void setInput( Object input )
	{
		setMemoryManager( ( input instanceof ICMemoryManager ) ? (ICMemoryManager)input : null );
		getPresentation().setMemoryBlock( getMemoryBlock() );
		setState();
		refresh();
	}
	
	protected void refresh()
	{
		fAddressText.setText( ( getPresentation() != null ) ? getPresentation().getAddressExpression() : "" );
		fMemoryText.refresh();
	}
	
	protected void setMemoryManager( ICMemoryManager mm )
	{
		fMemoryManager = mm;
	}

	protected ICMemoryManager getMemoryManager()
	{
		return fMemoryManager;
	}
	
	protected IFormattedMemoryBlock getMemoryBlock()
	{
		return ( getMemoryManager() != null ) ? getMemoryManager().getBlock( getIndex() ) : null;
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

	protected int getIndex()
	{
		return fIndex;
	}

	protected void setIndex( int index )
	{
		fIndex = index;
	}

	private void createBlock( String address ) throws DebugException
	{
		if ( getMemoryManager() != null )
		{
			getMemoryManager().setBlockAt( getIndex(), 
										   CDebugModel.createFormattedMemoryBlock( (IDebugTarget)getMemoryManager().getAdapter( IDebugTarget.class ),
										   										   address,
																				   getFormat(),
																				   getWordSize(),
																				   getNumberOfRows(),
																				   getNumberOfColumns(),
																				   getPaddingChar() ) );
			getPresentation().setMemoryBlock( getMemoryBlock() );
		}
		setMemoryTextState();
	}
	
	private void removeBlock() throws DebugException
	{
		if ( getMemoryManager() != null )
		{
			getMemoryManager().removeBlock( getIndex() );
			getPresentation().setMemoryBlock( null );
		}
		setMemoryTextState();
	}

	public int getFormat()
	{
		return fFormat;
	}

	public int getNumberOfColumns()
	{
		return fNumberOfColumns;
	}

	public int getNumberOfRows()
	{
		return fNumberOfRows;
	}

	public char getPaddingChar()
	{
		return fPaddingChar;
	}

	public int getWordSize()
	{
		return fWordSize;
	}

	public void setFormat(int format)
	{
		fFormat = format;
	}

	public void setNumberOfColumns( int numberOfColumns )
	{
		fNumberOfColumns = numberOfColumns;
	}

	public void setNumberOfRows( int numberOfRows )
	{
		fNumberOfRows = numberOfRows;
	}

	public void setPaddingChar( char paddingChar )
	{
		fPaddingChar = paddingChar;
	}

	public void setWordSize( int wordSize )
	{
		fWordSize = wordSize;
	}
	
	private void enableAddressText( boolean enable )
	{
		fAddressText.setEnabled( enable );
	}
	
	protected void setState()
	{
		enableAddressText( getMemoryManager() != null );
		setMemoryTextState();
	}
	
	private void setMemoryTextState()
	{
		fMemoryText.setEditable( getMemoryManager() != null && getMemoryBlock() != null );
	}
	
	protected MemoryText getMemoryText()
	{
		return fMemoryText;
	}
}
