/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.propertypages; 

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The property page for a module.
 */
public class ModulePropertyPage extends PropertyPage {

	public class ModulePropertyLabelProvider extends LabelProvider implements ITableLabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex ) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText( Object element, int columnIndex ) {
			if ( element instanceof ModuleProperties.Property ) {
				ModuleProperties.Property property = (ModuleProperties.Property)element;
				if ( ModuleProperties.TYPE.equals( property.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.0" ); //$NON-NLS-1$
					}
					Integer type = (Integer)property.getValue();
					if ( type.intValue() == ICModule.EXECUTABLE ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.1" ); //$NON-NLS-1$
					}
					if ( type.intValue() == ICModule.SHARED_LIBRARY ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.2" ); //$NON-NLS-1$
					}
					if ( type.intValue() == ICModule.CORE ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.3" ); //$NON-NLS-1$
					}
				}
				else if ( ModuleProperties.CPU.equals( property.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.4" ); //$NON-NLS-1$
					}
					String cpu = (String)property.getValue();
					return ( cpu != null ) ? cpu : PropertyPageMessages.getString( "ModulePropertyPage.5" ); //$NON-NLS-1$
				}
				else if ( ModuleProperties.BASE_ADDRESS.equals( property.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.6" ); //$NON-NLS-1$
					}
					IAddress address = (IAddress)property.getValue();
					return ( address != null && !address.isZero() ) ? address.toHexAddressString() : PropertyPageMessages.getString( "ModulePropertyPage.7" ); //$NON-NLS-1$
				}
				else if ( ModuleProperties.SIZE.equals( property.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.8" ); //$NON-NLS-1$
					}
					Long size = (Long)property.getValue();
					return ( size != null && size.longValue() > 0 ) ? size.toString() : PropertyPageMessages.getString( "ModulePropertyPage.9" ); //$NON-NLS-1$
				}
				else if ( ModuleProperties.SYMBOLS_LOADED.equals( property.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.10" ); //$NON-NLS-1$
					}
					Boolean loaded = (Boolean)property.getValue();
					return ( loaded != null && loaded.booleanValue() ) ? PropertyPageMessages.getString( "ModulePropertyPage.11" ) : PropertyPageMessages.getString( "ModulePropertyPage.12" ); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else if ( ModuleProperties.SYMBOLS_FILE.equals( property.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return PropertyPageMessages.getString( "ModulePropertyPage.13" ); //$NON-NLS-1$
					}
					IPath path = (IPath)property.getValue();
					return ( path != null ) ? path.toOSString() : PropertyPageMessages.getString( "ModulePropertyPage.14" ); //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	public class ModulePropertyContentProvider implements IStructuredContentProvider {

		/** 
		 * Constructor for ModulePropertyContentProvider. 
		 */
		public ModulePropertyContentProvider() {
			super();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements( Object inputElement ) {
			if ( inputElement instanceof ICModule ) {
				if ( getModuleProperties() == null ) {
					setModuleProperties( ModuleProperties.create( (ICModule)inputElement ) );
				}
				return getModuleProperties().getProperties();
			}
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			disposeProperties();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
			if ( oldInput != null && oldInput.equals( newInput ) )
				return;
			disposeProperties();
		}

		private void disposeProperties() {
			if ( getModuleProperties() != null ) {
				getModuleProperties().dispose();
				setModuleProperties( null );
			}
		}
	}

	private ModuleProperties fProperties = null;

	private TableViewer fViewer;

	// Column properties
	private static final String CP_NAME = "name"; //$NON-NLS-1$
	private static final String CP_VALUE = "value"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		noDefaultAndApplyButton();
		Composite composite = new Composite( parent, SWT.NONE );
		Font font = parent.getFont();
		composite.setFont( font );
		composite.setLayout( new GridLayout() );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fViewer = new TableViewer( composite, SWT.BORDER );
		Table table = fViewer.getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable( true );
		columns[1].setResizable( true );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 40 ) );

		fViewer.setColumnProperties( new String[]{ CP_NAME, CP_VALUE } );
		fViewer.setCellEditors( new CellEditor[]{ null, createSymbolsFileCellEditor( table ) } );
		fViewer.setCellModifier( createCellModifier() );

		fViewer.setContentProvider( createContentProvider() );
		fViewer.setLabelProvider( createLabelProvider() );

		setValid( true );
		return composite;
	}

	protected ICModule getModule() {
		return (ICModule)getElement();
	}

	private ModulePropertyContentProvider createContentProvider() {
		return new ModulePropertyContentProvider();
	}

	private ModulePropertyLabelProvider createLabelProvider() {
		return new ModulePropertyLabelProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl( Composite parent ) {
		super.createControl( parent );
		getViewer().setInput( getElement() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if ( getModuleProperties() != null && getModuleProperties().isDirty() ) {
			final IPath path = (IPath)getModuleProperties().getProperty( ModuleProperties.SYMBOLS_FILE );
			final ICModule module = getModule(); 
			if ( module != null ) {
				
				DebugPlugin.getDefault().asyncExec( 
						new Runnable() {
							public void run() {
								try {
									module.setSymbolsFileName( path );
								}
								catch( DebugException e ) {
									failed( PropertyPageMessages.getString( "ModulePropertyPage.15" ), e ); //$NON-NLS-1$
								}
							}
						} );
			}
		}
		return super.performOk();
	}

	protected TableViewer getViewer() {
		return fViewer;
	}

	private ICellModifier createCellModifier() {
		return new ICellModifier() {

			public boolean canModify( Object element, String property ) {
				return ( element instanceof ModuleProperties.Property && 
						 ModuleProperties.SYMBOLS_FILE.equals( ((ModuleProperties.Property)element).getKey() ) && 
						 CP_VALUE.equals( property ) && 
						 getModule().canModifySymbolsSource() );
			}

			public void modify( Object element, String property, Object value ) {
				ModuleProperties.Property mp = (ModuleProperties.Property)((TableItem)element).getData();
				if ( ModuleProperties.SYMBOLS_FILE.equals( mp.getKey() ) && 
					 CP_VALUE.equals( property ) && 
					 getModule().canModifySymbolsSource() &&
					 value instanceof IPath ) {
					getModuleProperties().setProperty( mp.getKey(), value );
					if ( getModuleProperties().isDirty() ) {
						getViewer().refresh();
					}
				}
			}

			public Object getValue( Object element, String property ) {
				if ( element instanceof ModuleProperties.Property && 
					 ModuleProperties.SYMBOLS_FILE.equals( ((ModuleProperties.Property)element).getKey() ) && 
					 CP_VALUE.equals( property ) && 
					 getModule().canModifySymbolsSource() ) {
					return ((ModuleProperties.Property)element).getValue();
				}
				return null;
			}
		};
	}

	private CellEditor createSymbolsFileCellEditor( Table table ) {

		return new DialogCellEditor( table ) {

			protected Object openDialogBox( Control cellEditorWindow ) {
				FileDialog dialog = new FileDialog( cellEditorWindow.getShell() );
				dialog.setFileName( ((IPath)getModuleProperties().getProperty( ModuleProperties.SYMBOLS_FILE )).toOSString() );
				String fn = dialog.open();
				return ( fn != null ) ? new Path( dialog.getFilterPath() ).append( dialog.getFileName() ) : getModule().getSymbolsFileName();
			}
			
		};
	}

	protected ModuleProperties getModuleProperties() {
		return fProperties;
	}

	protected void setModuleProperties( ModuleProperties properties ) {
		fProperties = properties;
	}

	protected void failed( String message, Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, message, null );
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), null ) );
		CDebugUtils.error( ms, getModule() );
	}
}
