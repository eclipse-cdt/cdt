/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.ui.wizards.AddDirectorySourceLocationWizard;
import org.eclipse.cdt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Enter type comment.
 * 
 * @since: Feb 21, 2003
 */
public class AttachSourceEditor extends EditorPart 
								implements IPropertyChangeListener
{
	public static final String EDITOR_ID = CDebugUIPlugin.getUniqueIdentifier() + ".editor.AttachSourceEditor";

	/** The horizontal scroll increment. */
	private static final int HORIZONTAL_SCROLL_INCREMENT = 10;
	/** The vertical scroll increment. */
	private static final int VERTICAL_SCROLL_INCREMENT = 10;

	private ScrolledComposite fScrolledComposite;
	private Color fBackgroundColor;
	private Color fForegroundColor;
	private Color fSeparatorColor;
	private List fBannerLabels= new ArrayList();
	private List fHeaderLabels= new ArrayList();
	private Font fFont;
	private Button fAttachButton;
	private Label fInputLabel;

	/**
	 * Constructor for AttachSourceEditor.
	 */
	public AttachSourceEditor()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave( IProgressMonitor monitor )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#doSaveAs()
	 */
	public void doSaveAs()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#gotoMarker(org.eclipse.core.resources.IMarker)
	 */
	public void gotoMarker( IMarker marker )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init( IEditorSite site, IEditorInput input ) throws PartInitException
	{
		setInput( input );
		setSite( site );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#isDirty()
	 */
	public boolean isDirty()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl( Composite parent )
	{
		Display display = parent.getDisplay();
		fBackgroundColor = display.getSystemColor( SWT.COLOR_LIST_BACKGROUND );
		fForegroundColor = display.getSystemColor( SWT.COLOR_LIST_FOREGROUND );
		fSeparatorColor = new Color( display, 152, 170, 203 );

		JFaceResources.getFontRegistry().addListener( this );

		fScrolledComposite = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
		fScrolledComposite.setAlwaysShowScrollBars( false );
		fScrolledComposite.setExpandHorizontal( true );
		fScrolledComposite.setExpandVertical( true );
		fScrolledComposite.addDisposeListener( 
						new DisposeListener()
							{
								public void widgetDisposed( DisposeEvent e )
								{
									JFaceResources.getFontRegistry().removeListener( AttachSourceEditor.this );
									fScrolledComposite = null;
									fSeparatorColor.dispose();
									fSeparatorColor = null;
									fBannerLabels.clear();
									fHeaderLabels.clear();
									if ( fFont != null )
									{
										fFont.dispose();
										fFont = null;
									}
								}
							} );

		fScrolledComposite.addControlListener( 
						new ControlListener()
							{
								public void controlMoved( ControlEvent e )
								{
								}
					
								public void controlResized( ControlEvent e )
								{
									Rectangle clientArea = fScrolledComposite.getClientArea();
					
									ScrollBar verticalBar = fScrolledComposite.getVerticalBar();
									verticalBar.setIncrement( VERTICAL_SCROLL_INCREMENT );
									verticalBar.setPageIncrement( clientArea.height - verticalBar.getIncrement() );
					
									ScrollBar horizontalBar = fScrolledComposite.getHorizontalBar();
									horizontalBar.setIncrement( HORIZONTAL_SCROLL_INCREMENT );
									horizontalBar.setPageIncrement( clientArea.width - horizontalBar.getIncrement() );
								}
							});

		Composite composite = createComposite( fScrolledComposite );
		composite.setLayout( new GridLayout() );

		createTitleLabel( composite, "C/C++ File Editor" );
		createLabel( composite, null );
		createLabel( composite, null );

		createHeadingLabel( composite, "Source not found" );

		Composite separator = createCompositeSeparator( composite );
		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		data.heightHint = 2;
		separator.setLayoutData( data );

		fInputLabel = createLabel( composite, "" );
		createLabel( composite, "You can attach the source location by pressing the button below:" );
		createLabel( composite, null );

		fAttachButton = createButton( composite, "&Attach Source..." );
		fAttachButton.addSelectionListener( 
					new SelectionListener()
						{
							public void widgetSelected( SelectionEvent event )
							{
								AttachSourceEditor.this.attachSourceLocation();
							}

							public void widgetDefaultSelected( SelectionEvent e )
							{
							}
						} );

		separator = createCompositeSeparator( composite );
		data = new GridData( GridData.FILL_HORIZONTAL );
		data.heightHint = 2;
		separator.setLayoutData( data );

		fScrolledComposite.setContent( composite );
		fScrolledComposite.setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

		if ( getEditorInput() != null )
		{
			setInputLabelText( getEditorInput().getName() );
		}
	}

	private Composite createComposite( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setBackground( fBackgroundColor );
		return composite;
	}

	private Label createLabel( Composite parent, String text )
	{
		Label label = new Label( parent, SWT.NONE );
		if ( text != null )
			label.setText( text );
		label.setBackground( fBackgroundColor );
		label.setForeground( fForegroundColor );
		return label;
	}

	private Label createTitleLabel( Composite parent, String text )
	{
		Label label = new Label( parent, SWT.NONE );
		if ( text != null )
			label.setText( text );
		label.setBackground( fBackgroundColor );
		label.setForeground( fForegroundColor );
		label.setFont( JFaceResources.getHeaderFont() );
		fHeaderLabels.add( label );
		return label;
	}

	private Label createHeadingLabel( Composite parent, String text )
	{
		Label label = new Label( parent, SWT.NONE );
		if ( text != null )
			label.setText( text );
		label.setBackground( fBackgroundColor );
		label.setForeground( fForegroundColor );
		label.setFont( JFaceResources.getBannerFont() );
		fBannerLabels.add( label );
		return label;
	}

	private Composite createCompositeSeparator( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setBackground( fSeparatorColor );
		return composite;
	}

	private Button createButton( Composite parent, String text )
	{
		Button button = new Button( parent, SWT.FLAT );
		button.setBackground( fBackgroundColor );
		button.setForeground( fForegroundColor );
		if ( text != null )
			button.setText( text );
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		if ( fAttachButton != null )
			fAttachButton.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	protected void setInput( IEditorInput input )
	{
		super.setInput( input );
		if ( input != null && fInputLabel != null )
			setInputLabelText( getEditorInput().getName() );
	}
	
	private void setInputLabelText( String inputName )
	{
		fInputLabel.setText( MessageFormat.format( "There is no source for the file {0}", new String[] { inputName } ) );
	}

	protected void attachSourceLocation()
	{
		if ( getEditorInput() != null && getEditorInput().getAdapter( FileNotFoundElement.class ) != null )
		{
			FileNotFoundElement element = (FileNotFoundElement)getEditorInput().getAdapter( FileNotFoundElement.class );
			if ( element.getLaunch() != null && element.getLaunch().getSourceLocator() instanceof IAdaptable )
			{
				ILaunch launch = element.getLaunch();
				ICSourceLocator locator = (ICSourceLocator)((IAdaptable)element.getLaunch().getSourceLocator()).getAdapter( ICSourceLocator.class );
				if ( locator != null )
				{
					IPath path = new Path( element.getName() );
					INewSourceLocationWizard wizard = null;
					if ( path.isAbsolute() )
					{
						path = path.removeLastSegments( 1 );
						wizard = new AddDirectorySourceLocationWizard( path );
					}
					else
					{
						wizard = new AddSourceLocationWizard( locator.getSourceLocations() );
					}
					WizardDialog dialog = new WizardDialog( CDebugUIPlugin.getActiveWorkbenchShell(), wizard );
					if ( dialog.open() == Window.OK )
					{
						ICSourceLocation[] locations = locator.getSourceLocations();
						ArrayList list = new ArrayList( Arrays.asList( locations ) );
						list.add( wizard.getSourceLocation() );
						locator.setSourceLocations( (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] ) );

						if ( locator instanceof IPersistableSourceLocator )
						{
							ILaunchConfiguration configuration = launch.getLaunchConfiguration();
							saveChanges( configuration, (IPersistableSourceLocator)launch.getSourceLocator() );
						}
					}
				}
			}
		}
	}

	protected void saveChanges( ILaunchConfiguration configuration, IPersistableSourceLocator locator )
	{
		try
		{
			ILaunchConfigurationWorkingCopy copy = configuration.copy( configuration.getName() );
			copy.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			copy.doSave();
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
		}
	}
}
