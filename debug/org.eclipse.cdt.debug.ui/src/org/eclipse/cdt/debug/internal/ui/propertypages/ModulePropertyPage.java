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

import java.util.Map;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
			if ( element instanceof Map.Entry ) {
				Map.Entry entry = (Map.Entry)element;
				if ( CModuleProperties.TYPE.equals( entry.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return "Type";
					}
					else {
						Integer type = (Integer)entry.getValue();
						if ( type.intValue() == ICModule.EXECUTABLE ) {
							return "executable";
						}
						if ( type.intValue() == ICModule.SHARED_LIBRARY ) {
							return "shared library";
						}
						if ( type.intValue() == ICModule.CORE ) {
							return "core file";
						}
					}
				}
				else if ( CModuleProperties.CPU.equals( entry.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return "CPU";
					}
					String cpu = (String)entry.getValue();
					return ( cpu != null ) ? cpu : "not available";
				}
				else if ( CModuleProperties.BASE_ADDRESS.equals( entry.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return "Base address";
					}
					IAddress address = (IAddress)entry.getValue();
					return ( address != null && !address.isZero() ) ? address.toHexAddressString() : "not available";
				}
				else if ( CModuleProperties.SIZE.equals( entry.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return "Size";
					}
					Long size = (Long)entry.getValue();
					return ( size != null && size.longValue() > 0 ) ? size.toString() : "not available";
				}
				else if ( CModuleProperties.SYMBOLS_LOADED.equals( entry.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return "Symbols";
					}
					Boolean loaded = (Boolean)entry.getValue();
					return ( loaded != null && loaded.booleanValue() ) ? "loaded" : "not loaded";
				}
				else if ( CModuleProperties.SYMBOLS_FILE.equals( entry.getKey() ) ) {
					if ( columnIndex == 0 ) {
						return "Symbols file";
					}
					IPath path = (IPath)entry.getValue();
					return ( path != null ) ? path.toOSString() : "not found";
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
				return CModuleProperties.create( (ICModule)inputElement ).getProperties();
			}
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
		}
	}

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

	private TableViewer getViewer() {
		return fViewer;
	}
}
