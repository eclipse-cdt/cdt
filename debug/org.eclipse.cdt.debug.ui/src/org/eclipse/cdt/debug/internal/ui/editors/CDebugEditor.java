/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.editors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.ui.wizards.AddDirectorySourceLocationWizard;
import org.eclipse.cdt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * The Debugger specific extension fo the C editor.
 */
public class CDebugEditor extends CEditor {

	public class AttachSourceForm implements IPropertyChangeListener {

		/** The horizontal scroll increment. */
		private static final int HORIZONTAL_SCROLL_INCREMENT = 10;

		/** The vertical scroll increment. */
		private static final int VERTICAL_SCROLL_INCREMENT = 10;

		/** The form's root widget */
		private Font fFont;

		/** The form's root widget */
		private ScrolledComposite fScrolledComposite;

		/** The background color */
		private Color fBackgroundColor;

		/** The foreground color */
		private Color fForegroundColor;

		/** The separator's color */
		private Color fSeparatorColor;

		/** The form headers */
		private List fHeaderLabels = new ArrayList();

		/** The form banners */
		private List fBannerLabels = new ArrayList();

		/** The form text */
		private Label fInputLabel;

		/** The attach source button */
		private Button fAttachButton = null;

		/** The preference change listener */
		//		private IPropertyChangeListener fPropertyChangeListener;
		private IEditorInput fInput = null;

		public AttachSourceForm( Composite parent, IEditorInput input ) {
			Display display = parent.getDisplay();
			fBackgroundColor = display.getSystemColor( SWT.COLOR_LIST_BACKGROUND );
			fForegroundColor = display.getSystemColor( SWT.COLOR_LIST_FOREGROUND );
			fSeparatorColor = new Color( display, 152, 170, 203 );
			JFaceResources.getFontRegistry().addListener( AttachSourceForm.this );
			fScrolledComposite = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
			fScrolledComposite.setAlwaysShowScrollBars( false );
			fScrolledComposite.setExpandHorizontal( true );
			fScrolledComposite.setExpandVertical( true );
			fScrolledComposite.addDisposeListener( new DisposeListener() {

				public void widgetDisposed( DisposeEvent e ) {
					JFaceResources.getFontRegistry().removeListener( AttachSourceForm.this );
					setScrolledComposite( null );
					getSeparatorColor().dispose();
					setSeparatorColor( null );
					getBannerLabels().clear();
					getHeaderLabels().clear();
					if ( getFont() != null ) {
						getFont().dispose();
						setFont( null );
					}
				}
			} );
			fScrolledComposite.addControlListener( new ControlListener() {

				public void controlMoved( ControlEvent e ) {
				}

				public void controlResized( ControlEvent e ) {
					Rectangle clientArea = getScrolledComposite().getClientArea();
					ScrollBar verticalBar = getScrolledComposite().getVerticalBar();
					verticalBar.setIncrement( VERTICAL_SCROLL_INCREMENT );
					verticalBar.setPageIncrement( clientArea.height - verticalBar.getIncrement() );
					ScrollBar horizontalBar = getScrolledComposite().getHorizontalBar();
					horizontalBar.setIncrement( HORIZONTAL_SCROLL_INCREMENT );
					horizontalBar.setPageIncrement( clientArea.width - horizontalBar.getIncrement() );
				}
			} );
			Composite composite = createComposite( fScrolledComposite );
			composite.setLayout( new GridLayout() );
			createTitleLabel( composite, EditorMessages.getString( "CDebugEditor.0" ) ); //$NON-NLS-1$
			createLabel( composite, null );
			createLabel( composite, null );
			createHeadingLabel( composite, EditorMessages.getString( "CDebugEditor.1" ) ); //$NON-NLS-1$
			Composite separator = createCompositeSeparator( composite );
			GridData data = new GridData( GridData.FILL_HORIZONTAL );
			data.heightHint = 2;
			separator.setLayoutData( data );
			fInputLabel = createLabel( composite, "" ); //$NON-NLS-1$
			createLabel( composite, EditorMessages.getString( "CDebugEditor.2" ) ); //$NON-NLS-1$
			createLabel( composite, null );
			fAttachButton = createButton( composite, EditorMessages.getString( "CDebugEditor.3" ) ); //$NON-NLS-1$
			fAttachButton.addSelectionListener( new SelectionListener() {

				public void widgetSelected( SelectionEvent event ) {
					attachSourceLocation();
				}

				public void widgetDefaultSelected( SelectionEvent e ) {
				}
			} );
			separator = createCompositeSeparator( composite );
			data = new GridData( GridData.FILL_HORIZONTAL );
			data.heightHint = 2;
			separator.setLayoutData( data );
			fScrolledComposite.setContent( composite );
			fScrolledComposite.setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
			if ( getEditorInput() != null ) {
				setInputLabelText( getEditorInput() );
			}
			fInput = input;
		}

		private Composite createComposite( Composite parent ) {
			Composite composite = new Composite( parent, SWT.NONE );
			composite.setBackground( fBackgroundColor );
			return composite;
		}

		private Label createLabel( Composite parent, String text ) {
			Label label = new Label( parent, SWT.NONE );
			if ( text != null )
				label.setText( text );
			label.setBackground( fBackgroundColor );
			label.setForeground( fForegroundColor );
			return label;
		}

		private Label createTitleLabel( Composite parent, String text ) {
			Label label = new Label( parent, SWT.NONE );
			if ( text != null )
				label.setText( text );
			label.setBackground( fBackgroundColor );
			label.setForeground( fForegroundColor );
			label.setFont( JFaceResources.getHeaderFont() );
			fHeaderLabels.add( label );
			return label;
		}

		private Label createHeadingLabel( Composite parent, String text ) {
			Label label = new Label( parent, SWT.NONE );
			if ( text != null )
				label.setText( text );
			label.setBackground( fBackgroundColor );
			label.setForeground( fForegroundColor );
			label.setFont( JFaceResources.getBannerFont() );
			fBannerLabels.add( label );
			return label;
		}

		private Composite createCompositeSeparator( Composite parent ) {
			Composite composite = new Composite( parent, SWT.NONE );
			composite.setBackground( fSeparatorColor );
			return composite;
		}

		private Button createButton( Composite parent, String text ) {
			Button button = new Button( parent, SWT.FLAT );
			button.setBackground( fBackgroundColor );
			button.setForeground( fForegroundColor );
			if ( text != null )
				button.setText( text );
			return button;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange( PropertyChangeEvent event ) {
			for( Iterator iterator = fBannerLabels.iterator(); iterator.hasNext(); ) {
				Label label = (Label)iterator.next();
				label.setFont( JFaceResources.getBannerFont() );
			}
			for( Iterator iterator = fHeaderLabels.iterator(); iterator.hasNext(); ) {
				Label label = (Label)iterator.next();
				label.setFont( JFaceResources.getHeaderFont() );
			}
			Control control = fScrolledComposite.getContent();
			fScrolledComposite.setMinSize( control.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
			fScrolledComposite.setContent( control );
			fScrolledComposite.layout( true );
			fScrolledComposite.redraw();
		}

		private void setInputLabelText( IEditorInput input ) {
			FileNotFoundElement element = (FileNotFoundElement)input.getAdapter( FileNotFoundElement.class );
			if ( element != null )
				fInputLabel.setText( MessageFormat.format( EditorMessages.getString( "CDebugEditor.4" ), new String[]{ element.getFullPath().toOSString() } ) ); //$NON-NLS-1$
		}

		protected ScrolledComposite getScrolledComposite() {
			return fScrolledComposite;
		}

		protected void setScrolledComposite( ScrolledComposite scrolledComposite ) {
			fScrolledComposite = scrolledComposite;
		}

		protected Color getSeparatorColor() {
			return fSeparatorColor;
		}

		protected void setSeparatorColor( Color separatorColor ) {
			fSeparatorColor = separatorColor;
		}

		protected List getBannerLabels() {
			return fBannerLabels;
		}

		protected void setBannerLabels( List bannerLabels ) {
			fBannerLabels = bannerLabels;
		}

		protected List getHeaderLabels() {
			return fHeaderLabels;
		}

		protected void setHeaderLabels( List headerLabels ) {
			fHeaderLabels = headerLabels;
		}

		protected Font getFont() {
			return fFont;
		}

		protected void setFont( Font font ) {
			fFont = font;
		}

		public Control getControl() {
			return fScrolledComposite;
		}

		public IEditorInput getInput() {
			return fInput;
		}
	}

	public static final String EDITOR_ID = CDebugUIPlugin.getUniqueIdentifier() + ".editor.CDebugEditor"; //$NON-NLS-1$

	private AttachSourceForm fAttachSourceForm = null;

	/**
	 * Constructor for CDebugEditor.
	 */
	public CDebugEditor() {
		super();
		setDocumentProvider( CUIPlugin.getDefault().getDocumentProvider() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl( Composite parent ) {
		super.createPartControl( parent );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	protected void doSetInput( IEditorInput input ) throws CoreException {
		IEditorInput newInput = input;
		if ( input instanceof EditorInputDelegate && ((EditorInputDelegate)input).getDelegate() != null ) {
			newInput = ((EditorInputDelegate)input).getDelegate();
		}
		IEditorInput oldInput = getEditorInput();
		if ( oldInput instanceof EditorInputDelegate ) {
			oldInput = ((EditorInputDelegate)oldInput).getDelegate();
		}
		if ( oldInput != null ) {
			CUIPlugin.getDefault().getWorkingCopyManager().disconnect( oldInput );
		}
		super.doSetInput( newInput );
		// This hack should be after the super.doSetInput();
		CUIPlugin.getDefault().getWorkingCopyManager().connect( newInput );
	}

	protected void attachSourceLocation() {
		if ( getEditorInput() != null && getEditorInput().getAdapter( FileNotFoundElement.class ) != null ) {
			FileNotFoundElement element = (FileNotFoundElement)getEditorInput().getAdapter( FileNotFoundElement.class );
			if ( element.getLaunch() != null && element.getLaunch().getSourceLocator() instanceof IAdaptable ) {
				ILaunch launch = element.getLaunch();
				ICSourceLocator locator = (ICSourceLocator)((IAdaptable)element.getLaunch().getSourceLocator()).getAdapter( ICSourceLocator.class );
				if ( locator != null ) {
					IPath path = element.getFullPath();
					INewSourceLocationWizard wizard = null;
					if ( path.isAbsolute() ) {
						path = path.removeLastSegments( 1 );
						wizard = new AddDirectorySourceLocationWizard( path );
					}
					else {
						wizard = new AddSourceLocationWizard( locator.getSourceLocations() );
					}
					WizardDialog dialog = new WizardDialog( CDebugUIPlugin.getActiveWorkbenchShell(), wizard );
					if ( dialog.open() == Window.OK ) {
						ICSourceLocation[] locations = locator.getSourceLocations();
						ArrayList list = new ArrayList( Arrays.asList( locations ) );
						list.add( wizard.getSourceLocation() );
						locator.setSourceLocations( (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] ) );
						if ( locator instanceof IPersistableSourceLocator ) {
							ILaunchConfiguration configuration = launch.getLaunchConfiguration();
							saveChanges( configuration, (IPersistableSourceLocator)launch.getSourceLocator() );
						}
						Object newElement = locator.getSourceElement( element.getStackFrame() );
						IEditorInput newInput = null;
						if ( newElement instanceof IFile ) {
							newInput = new FileEditorInput( (IFile)newElement );
						}
						else if ( newElement instanceof FileStorage ) {
							newInput = new ExternalEditorInput( (IStorage)newElement );
						}
						IEditorInput oldInput = ((EditorInputDelegate)getEditorInput()).getDelegate();
						CUIPlugin.getDefault().getWorkingCopyManager().disconnect( oldInput );
						((EditorInputDelegate)getEditorInput()).setDelegate( newInput );
						resetInput( element.getStackFrame() );
					}
				}
			}
		}
	}

	private void resetInput( IStackFrame frame ) {
		setInput( getEditorInput() );
		IViewPart view = CDebugUIPlugin.getActivePage().findView( IDebugUIConstants.ID_DEBUG_VIEW );
		if ( view instanceof IDebugView ) {
			((IDebugView)view).getViewer().setSelection( new StructuredSelection( frame ) );
		}
	}

	protected void saveChanges( ILaunchConfiguration configuration, IPersistableSourceLocator locator ) {
		try {
			ILaunchConfigurationWorkingCopy copy = configuration.copy( configuration.getName() );
			copy.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			copy.doSave();
		}
		catch( CoreException e ) {
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.StatusTextEditor#createStatusControl(Composite, IStatus)
	 */
	protected Control createStatusControl( Composite parent, IStatus status ) {
		fAttachSourceForm = new AttachSourceForm( parent, getEditorInput() );
		return fAttachSourceForm.getControl();
	}

	/**
	 * @see org.eclipse.ui.texteditor.StatusTextEditor#updatePartControl(IEditorInput)
	 */
	public void updatePartControl( IEditorInput input ) {
		if ( fAttachSourceForm != null ) {
			if ( fAttachSourceForm.getInput() != null && !fAttachSourceForm.getInput().equals( input ) ) {
				fAttachSourceForm = null;
				super.updatePartControl( input );
			}
		}
		else
			super.updatePartControl( input );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		IEditorInput input = getEditorInput();
		IEditorInput newInput = input;
		if ( input instanceof EditorInputDelegate && ((EditorInputDelegate)input).getDelegate() != null ) {
			newInput = ((EditorInputDelegate)input).getDelegate();
		}
		CUIPlugin.getDefault().getWorkingCopyManager().disconnect( newInput );
		super.dispose();
	}
}
