/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;


/**
 * A dialog to select a type from a list of types.
 */
public class TypeSelectionDialog extends TwoPaneElementSelector {

	private static class TypeFilterMatcher implements FilteredList.FilterMatcher {

		private static final char END_SYMBOL = '<';
		private static final char ANY_STRING = '*';
		
		private StringMatcher fNameMatcher = null;
		private StringMatcher[] fSegmentMatchers = null;
		private boolean fMatchGlobalNamespace = false;
		private Collection fVisibleTypes = new HashSet();
		private boolean fShowLowLevelTypes = false;
		
		/*
		 * @see FilteredList.FilterMatcher#setFilter(String, boolean)
		 */
		public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
			// parse pattern into segments
			QualifiedTypeName qualifiedName = new QualifiedTypeName(pattern);
			String[] segments = qualifiedName.segments();
			int length = segments.length;

			// append wildcard to innermost segment
			segments[length-1] = adjustPattern(segments[length-1]);
			
			fMatchGlobalNamespace = false;
			fSegmentMatchers = new StringMatcher[length];
			int count = 0;
			for (int i = 0; i < length; ++i) {
				if (segments[i].length() > 0) {
					// create StringMatcher for this segment
					fSegmentMatchers[count++] = new StringMatcher(segments[i], ignoreCase, ignoreWildCards);
				} else if (i == 0) {
					// allow outermost segment to be blank (e.g. "::foo*")
					fMatchGlobalNamespace = true;
				} else {
					// skip over blank segments (e.g. treat "foo::::b*" as "foo::b*")
				}
			}
			if (count != length) {
				if (count > 0) {
					// resize array
					StringMatcher[] newMatchers = new StringMatcher[count];
					System.arraycopy(fSegmentMatchers, 0, newMatchers, 0, count);
					fSegmentMatchers = newMatchers;
				} else {
					// fallback to wildcard (should never get here)
					fSegmentMatchers = new StringMatcher[1];
					fSegmentMatchers[0] = new StringMatcher(String.valueOf(ANY_STRING), ignoreCase, ignoreWildCards);
				}
			}
			// match simple name with innermost segment
			fNameMatcher = fSegmentMatchers[fSegmentMatchers.length-1];
		}
		
		public void setVisibleTypes(Collection visibleTypes) {
			fVisibleTypes.clear();
			fVisibleTypes.addAll(visibleTypes);
		}

		public Collection getVisibleTypes() {
			return fVisibleTypes;
		}
		
		public void setShowLowLevelTypes(boolean show) {
			fShowLowLevelTypes = show;
		}

		public boolean getShowLowLevelTypes() {
			return fShowLowLevelTypes;
		}

		/*
		 * @see FilteredList.FilterMatcher#match(Object)
		 */
		public boolean match(Object element) {
			if (!(element instanceof ITypeInfo))
				return false;

			ITypeInfo info = (ITypeInfo) element;
			IQualifiedTypeName qualifiedName = info.getQualifiedTypeName();
			
			if (fVisibleTypes != null && !fVisibleTypes.contains(new Integer(info.getCElementType())))
				return false;

			if (!fShowLowLevelTypes && qualifiedName.isLowLevel())
				return false;
			
			if (fSegmentMatchers.length == 1 && !fMatchGlobalNamespace)
				return fNameMatcher.match(qualifiedName.getName());
			
			return matchQualifiedName(info);
		}

		private boolean matchQualifiedName(ITypeInfo info) {
			IQualifiedTypeName qualifiedName = info.getQualifiedTypeName();
			if (fSegmentMatchers.length != qualifiedName.segmentCount())
				return false;
			
			if (fMatchGlobalNamespace) {
				// must match global namespace (eg ::foo)
				if (info.getRootNamespace(false) != null)
					return false;
			}
			
			boolean matchFound = true;
			int max = Math.min(fSegmentMatchers.length, qualifiedName.segmentCount());
			for (int i = 0; i < max; ++i) {
				StringMatcher matcher = fSegmentMatchers[i];
				String name = qualifiedName.segment(i);
				if (name == null || !matcher.match(name)) {
					matchFound = false;
					break;
				}
			}
			return matchFound;
		}

		private static String adjustPattern(String pattern) {
			int length = pattern.length();
			if (length > 0) {
				switch (pattern.charAt(length - 1)) {
					case END_SYMBOL:
						return pattern.substring(0, length - 1);
					case ANY_STRING:
						return pattern;
				}
			}
			return pattern + ANY_STRING;
		}
	}
	
	private static class StringComparator implements Comparator {
	    public int compare(Object left, Object right) {
	     	String leftString = (String) left;
	     	String rightString = (String) right;
	     	
			int result = leftString.compareToIgnoreCase(rightString);			
			if (result == 0)
				result = leftString.compareTo(rightString);

			return result;
	    }
	}

	private static final String DIALOG_SETTINGS = TypeSelectionDialog.class.getName();
	private static final String SETTINGS_X_POS = "x"; //$NON-NLS-1$
	private static final String SETTINGS_Y_POS = "y"; //$NON-NLS-1$
	private static final String SETTINGS_WIDTH = "width"; //$NON-NLS-1$
	private static final String SETTINGS_HEIGHT = "height"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_NAMESPACES = "show_namespaces"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_CLASSES = "show_classes"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_STRUCTS = "show_structs"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_TYPEDEFS = "show_typedefs"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_ENUMS = "show_enums"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_UNIONS = "show_unions"; //$NON-NLS-1$
	private static final String SETTINGS_SHOW_LOWLEVEL = "show_lowlevel"; //$NON-NLS-1$

	private static final TypeInfoLabelProvider fElementRenderer = new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY);
	private static final TypeInfoLabelProvider fQualifierRenderer = new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_ENCLOSING_TYPE_ONLY + TypeInfoLabelProvider.SHOW_PATH);
	
	static final TypeFilterMatcher fFilterMatcher = new TypeFilterMatcher();
	private static final StringComparator fStringComparator = new StringComparator();

	private static final int[] fAllTypes = { ICElement.C_NAMESPACE, ICElement.C_CLASS,
			ICElement.C_STRUCT, ICElement.C_TYPEDEF, ICElement.C_ENUMERATION,
			ICElement.C_UNION };

	private Set fKnownTypes = new HashSet(fAllTypes.length);
	private Text fTextWidget;
	private boolean fSelectFilterText = false;
	private FilteredList fNewFilteredList;
	private String fDialogSection;
	private Point fLocation;
	private Point fSize;
 
	/**
	 * Constructs a type selection dialog.
	 * @param parent  the parent shell.
	 */
	public TypeSelectionDialog(Shell parent) {
		super(parent, fElementRenderer, fQualifierRenderer);
		setMatchEmptyString(false);
		setUpperListLabel(TypeInfoMessages.getString("TypeSelectionDialog.upperLabel")); //$NON-NLS-1$
		setLowerListLabel(TypeInfoMessages.getString("TypeSelectionDialog.lowerLabel")); //$NON-NLS-1$
		setVisibleTypes(fAllTypes);
		setDialogSettings(DIALOG_SETTINGS);
	}
	
	/**
	 * Sets the filter pattern.
	 * @param filter the filter pattern.
	 * @param selectText <code>true</code> if filter text should be initially selected
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#setFilter(java.lang.String)
	 */
	public void setFilter(String filter, boolean selectText) {
		super.setFilter(filter);
		fSelectFilterText = selectText;
	}

	/**
	 * Sets which CElement types are visible in the dialog.
	 * 
	 * @param types Array of CElement types.
	 */
	public void setVisibleTypes(int[] types) {
		fKnownTypes.clear();
		for (int i = 0; i < types.length; ++i) {
			fKnownTypes.add(new Integer(types[i]));
		}
	}
	
	/**
	 * Sets section name to use when storing the dialog settings.
	 * 
	 * @param section Name of section.
	 */
	public void setDialogSettings(String section) {
		fDialogSection = section + "Settings"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#createFilterText(org.eclipse.swt.widgets.Composite)
	 */
 	protected Text createFilterText(Composite parent) {
 		fTextWidget = super.createFilterText(parent);

		// create type checkboxes below filter text
 		createTypeFilterArea(parent);
		
 		return fTextWidget;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#createFilteredList(org.eclipse.swt.widgets.Composite)
	 */
 	protected FilteredList createFilteredList(Composite parent) {
 		fNewFilteredList = super.createFilteredList(parent);
		fNewFilteredList.setFilterMatcher(fFilterMatcher);
		fNewFilteredList.setComparator(fStringComparator);
		return fNewFilteredList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		if (!fSelectFilterText)
			fTextWidget.setSelection(0,0);
	}

	/*
	 * @see Window#close()
	 */
	public boolean close() {
		writeSettings(getDialogSettings());
		return super.close();
	}

	/*
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		readSettings(getDialogSettings());
		return super.createContents(parent);
	}

	/**
	 * Creates a type filter checkbox.
	 */
	private void createTypeCheckbox(Composite parent, Integer typeObject) {
		String name;
		int type = typeObject.intValue();
		switch (type) {
			case ICElement.C_NAMESPACE:
				name = TypeInfoMessages.getString("TypeSelectionDialog.filterNamespaces"); //$NON-NLS-1$
			break;
			case ICElement.C_CLASS:
				name = TypeInfoMessages.getString("TypeSelectionDialog.filterClasses"); //$NON-NLS-1$
			break;
			case ICElement.C_STRUCT:
				name = TypeInfoMessages.getString("TypeSelectionDialog.filterStructs"); //$NON-NLS-1$
			break;
			case ICElement.C_TYPEDEF:
				name = TypeInfoMessages.getString("TypeSelectionDialog.filterTypedefs"); //$NON-NLS-1$
			break;
			case ICElement.C_ENUMERATION:
				name = TypeInfoMessages.getString("TypeSelectionDialog.filterEnums"); //$NON-NLS-1$
			break;
			case ICElement.C_UNION:
				name = TypeInfoMessages.getString("TypeSelectionDialog.filterUnions"); //$NON-NLS-1$
			break;
			default:
				return;
		}
		Image icon = TypeInfoLabelProvider.getTypeIcon(type);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight = 0;
		composite.setLayout(layout);
		
		final Integer fTypeObject = typeObject;
		Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setFont(composite.getFont());
		checkbox.setImage(icon);
		checkbox.setSelection(fFilterMatcher.getVisibleTypes().contains(fTypeObject));
		checkbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof Button) {
					Button checkbox = (Button) e.widget;
					if (checkbox.getSelection())
						fFilterMatcher.getVisibleTypes().add(fTypeObject);
					else
						fFilterMatcher.getVisibleTypes().remove(fTypeObject);
					updateElements();
				}
			}
		});
		
		Label label = new Label(composite, SWT.LEFT);
		label.setFont(composite.getFont());
		label.setText(name);
	}

	/**
	 * Creates an area to filter types.
	 * 
	 * @param parent area to create controls in
	 */
	private void createTypeFilterArea(Composite parent) {
		createLabel(parent, TypeInfoMessages.getString("TypeSelectionDialog.filterLabel")); //$NON-NLS-1$
		
		Composite upperRow = new Composite(parent, SWT.NONE);
		GridLayout upperLayout = new GridLayout(3, true);
		upperLayout.verticalSpacing = 2;
		upperLayout.marginHeight = 0;
		upperLayout.marginWidth = 0;
		upperRow.setLayout(upperLayout);

		// the for loop is here to guarantee we always
		// create the checkboxes in the same order
		for (int i = 0; i < fAllTypes.length; ++i) {
			Integer typeObject = new Integer(fAllTypes[i]);
			if (fKnownTypes.contains(typeObject))
				createTypeCheckbox(upperRow, typeObject);
		}

		Composite lowerRow = new Composite(parent, SWT.NONE);
		GridLayout lowerLayout = new GridLayout(1, true);
		lowerLayout.verticalSpacing = 2;
		lowerLayout.marginHeight = 0;
		upperLayout.marginWidth = 0;
		lowerRow.setLayout(lowerLayout);

		Composite composite = new Composite(lowerRow, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		String name = TypeInfoMessages.getString("TypeSelectionDialog.filterLowLevelTypes"); //$NON-NLS-1$
		Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setFont(composite.getFont());
		checkbox.setText(name);
		checkbox.setSelection(fFilterMatcher.getShowLowLevelTypes());
		checkbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof Button) {
					Button button = (Button) e.widget;
					fFilterMatcher.setShowLowLevelTypes(button.getSelection());
					updateElements();
				}
			}
		});
	}
	
	/**
	 * Forces redraw of elements list.
	 */
	void updateElements() {
		fNewFilteredList.setFilter(fTextWidget.getText());
	}
	
	/**
	 * Returns the dialog settings object used to save state
	 * for this dialog.
	 *
	 * @return the dialog settings to be used
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings allSettings = CUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = allSettings.getSection(fDialogSection);		
		if (section == null) {
			section = allSettings.addNewSection(fDialogSection);
			writeDefaultSettings(section);
		}
		return section;
	}
	
	/**
	 * Stores current configuration in the dialog store.
	 */
	protected void writeSettings(IDialogSettings section) {
		Point location = getShell().getLocation();
		section.put(SETTINGS_X_POS, location.x);
		section.put(SETTINGS_Y_POS, location.y);

		Point size = getShell().getSize();
		section.put(SETTINGS_WIDTH, size.x);
		section.put(SETTINGS_HEIGHT, size.y);

		section.put(SETTINGS_SHOW_NAMESPACES, fFilterMatcher.getVisibleTypes().contains(new Integer(ICElement.C_NAMESPACE)));
		section.put(SETTINGS_SHOW_CLASSES, fFilterMatcher.getVisibleTypes().contains(new Integer(ICElement.C_CLASS)));
		section.put(SETTINGS_SHOW_STRUCTS, fFilterMatcher.getVisibleTypes().contains(new Integer(ICElement.C_STRUCT)));
		section.put(SETTINGS_SHOW_TYPEDEFS, fFilterMatcher.getVisibleTypes().contains(new Integer(ICElement.C_TYPEDEF)));
		section.put(SETTINGS_SHOW_ENUMS, fFilterMatcher.getVisibleTypes().contains(new Integer(ICElement.C_ENUMERATION)));
		section.put(SETTINGS_SHOW_UNIONS, fFilterMatcher.getVisibleTypes().contains(new Integer(ICElement.C_UNION)));
		section.put(SETTINGS_SHOW_LOWLEVEL, fFilterMatcher.getShowLowLevelTypes());
	}

	/**
	 * Stores default dialog settings.
	 */
	protected void writeDefaultSettings(IDialogSettings section) {
		section.put(SETTINGS_SHOW_NAMESPACES, true); 
		section.put(SETTINGS_SHOW_CLASSES, true);
		section.put(SETTINGS_SHOW_STRUCTS, true);
		section.put(SETTINGS_SHOW_TYPEDEFS, true);
		section.put(SETTINGS_SHOW_ENUMS, true);
		section.put(SETTINGS_SHOW_UNIONS, true);
		section.put(SETTINGS_SHOW_LOWLEVEL, false);
	}

	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	protected void readSettings(IDialogSettings section) {
		try {
			int x = section.getInt(SETTINGS_X_POS);
			int y = section.getInt(SETTINGS_Y_POS);
			fLocation = new Point(x, y);
			int width = section.getInt(SETTINGS_WIDTH);
			int height = section.getInt(SETTINGS_HEIGHT);
			fSize = new Point(width, height);
		} catch (NumberFormatException e) {
			fLocation = null;
			fSize = null;
		}

		if (section.getBoolean(SETTINGS_SHOW_NAMESPACES)) {
			Integer typeObject = new Integer(ICElement.C_NAMESPACE);
			if (fKnownTypes.contains(typeObject))
				fFilterMatcher.getVisibleTypes().add(typeObject);
		}
		if (section.getBoolean(SETTINGS_SHOW_CLASSES)) {
			Integer typeObject = new Integer(ICElement.C_CLASS);
			if (fKnownTypes.contains(typeObject))
				fFilterMatcher.getVisibleTypes().add(typeObject);
		}
		if (section.getBoolean(SETTINGS_SHOW_STRUCTS)) {
			Integer typeObject = new Integer(ICElement.C_STRUCT);
			if (fKnownTypes.contains(typeObject))
				fFilterMatcher.getVisibleTypes().add(typeObject);
		}
		if (section.getBoolean(SETTINGS_SHOW_TYPEDEFS)) {
			Integer typeObject = new Integer(ICElement.C_TYPEDEF);
			if (fKnownTypes.contains(typeObject))
				fFilterMatcher.getVisibleTypes().add(typeObject);
		}
		if (section.getBoolean(SETTINGS_SHOW_ENUMS)) {
			Integer typeObject = new Integer(ICElement.C_ENUMERATION);
			if (fKnownTypes.contains(typeObject))
				fFilterMatcher.getVisibleTypes().add(typeObject);
		}
		if (section.getBoolean(SETTINGS_SHOW_UNIONS)) {
			Integer typeObject = new Integer(ICElement.C_UNION);
			if (fKnownTypes.contains(typeObject))
				fFilterMatcher.getVisibleTypes().add(typeObject);
		}
		fFilterMatcher.setShowLowLevelTypes(section.getBoolean(SETTINGS_SHOW_LOWLEVEL));
	}
	
	/* (non-Cdoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point result = super.getInitialSize();
		if (fSize != null) {
			result.x = Math.max(result.x, fSize.x);
			result.y = Math.max(result.y, fSize.y);
			Rectangle display = getShell().getDisplay().getClientArea();
			result.x = Math.min(result.x, display.width);
			result.y = Math.min(result.y, display.height);
		}
		return result;
	}
	
	/* (non-Cdoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point result = super.getInitialLocation(initialSize);
		if (fLocation != null) {
			result.x = fLocation.x;
			result.y = fLocation.y;
			Rectangle display = getShell().getDisplay().getClientArea();
			int xe = result.x + initialSize.x;
			if (xe > display.width) {
				result.x -= xe - display.width; 
			}
			int ye = result.y + initialSize.y;
			if (ye > display.height) {
				result.y -= ye - display.height; 
			}
		}
		return result;
	}	
	
	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		ITypeInfo selection = (ITypeInfo) getLowerSelectedElement();
		if (selection == null)
			return;
			
		List result = new ArrayList(1);
		result.add(selection);
		setResult(result);
	}
}
