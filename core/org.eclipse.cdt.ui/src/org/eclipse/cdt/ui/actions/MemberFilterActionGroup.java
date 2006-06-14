/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

/**
 * Action Group that contributes filter buttons for a view parts showing 
 * methods and fields. Contributed filters are: hide fields, hide static
 * members and hide non-public members.
 * <p>
 * The action group installs a filter on a structured viewer. The filter is connected 
 * to the actions installed in the view part's toolbar menu and is updated when the 
 * state of the buttons changes.
 * <p>
 */
import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.viewsupport.MemberFilter;
import org.eclipse.cdt.internal.ui.viewsupport.MemberFilterAction;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.CPluginImages;

public class MemberFilterActionGroup extends ActionGroup {
	public static final int FILTER_NONPUBLIC= MemberFilter.FILTER_NONPUBLIC;
	public static final int FILTER_STATIC= MemberFilter.FILTER_STATIC;
	public static final int FILTER_FIELDS= MemberFilter.FILTER_FIELDS;
	
	public static final int FILTER_LOCALTYPES= MemberFilter.FILTER_LOCALTYPES;
	public static final int ALL_FILTERS= FILTER_NONPUBLIC | FILTER_FIELDS | FILTER_STATIC | FILTER_LOCALTYPES;
	
	private static final String TAG_HIDEFIELDS= "hidefields"; //$NON-NLS-1$
	private static final String TAG_HIDESTATIC= "hidestatic"; //$NON-NLS-1$
	private static final String TAG_HIDENONPUBLIC= "hidenonpublic"; //$NON-NLS-1$
	
	private MemberFilterAction[] fFilterActions;
	private MemberFilter fFilter;
	
	StructuredViewer fViewer;
	private String fViewerId;
	private boolean fInViewMenu;
	
	
	/**
	 * Creates a new <code>MemberFilterActionGroup</code>.
	 * 
	 * @param viewer the viewer to be filtered
	 * @param viewerId a unique id of the viewer. Used as a key to to store 
	 * the last used filter settings in the preference store
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId) {
		this(viewer, viewerId, false);
	}
	
	/**
	 * Creates a new <code>MemberFilterActionGroup</code>.
	 * 
	 * @param viewer the viewer to be filtered
	 * @param viewerId a unique id of the viewer. Used as a key to to store 
	 * the last used filter settings in the preference store
	 * @param inViewMenu if <code>true</code> the actions are added to the view
	 * menu. If <code>false</code> they are added to the toobar.
	 * 
	 * @since 2.1
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId, boolean inViewMenu) {
		fViewer= viewer;
		fViewerId= viewerId;
		fInViewMenu= inViewMenu;
		
		// get initial values
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		boolean doHideFields= store.getBoolean(getPreferenceKey(FILTER_FIELDS));
		boolean doHideStatic= store.getBoolean(getPreferenceKey(FILTER_STATIC));
		boolean doHidePublic= store.getBoolean(getPreferenceKey(FILTER_NONPUBLIC));

		fFilter= new MemberFilter();
		if (doHideFields)
			fFilter.addFilter(FILTER_FIELDS);
		if (doHideStatic)
			fFilter.addFilter(FILTER_STATIC);			
		if (doHidePublic)
			fFilter.addFilter(FILTER_NONPUBLIC);		
	
		// fields
		String title= ActionMessages.getString("MemberFilterActionGroup.hide_fields.label"); //$NON-NLS-1$
		String helpContext= ICHelpContextIds.FILTER_FIELDS_ACTION;
		MemberFilterAction hideFields= new MemberFilterAction(this, title, FILTER_FIELDS, helpContext, doHideFields);
		hideFields.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_fields.description")); //$NON-NLS-1$
		hideFields.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_fields.tooltip")); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(hideFields, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_FIELDS); //$NON-NLS-1$
		
		// static
		title= ActionMessages.getString("MemberFilterActionGroup.hide_static.label"); //$NON-NLS-1$
		helpContext= ICHelpContextIds.FILTER_STATIC_ACTION;
		MemberFilterAction hideStatic= new MemberFilterAction(this, title, FILTER_STATIC, helpContext, doHideStatic);
		hideStatic.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_static.description")); //$NON-NLS-1$
		hideStatic.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_static.tooltip")); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(hideStatic, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_STATIC); //$NON-NLS-1$
		
		// non-public
		title= ActionMessages.getString("MemberFilterActionGroup.hide_nonpublic.label"); //$NON-NLS-1$
		helpContext= ICHelpContextIds.FILTER_PUBLIC_ACTION;
		MemberFilterAction hideNonPublic= new MemberFilterAction(this, title, FILTER_NONPUBLIC, helpContext, doHidePublic);
		hideNonPublic.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_nonpublic.description")); //$NON-NLS-1$
		hideNonPublic.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_nonpublic.tooltip")); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(hideNonPublic, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_PUBLIC); //$NON-NLS-1$
	
		// order corresponds to order in toolbar
		fFilterActions= new MemberFilterAction[] { hideFields, hideStatic, hideNonPublic };
		
		fViewer.addFilter(fFilter);
	}
	
	/**
	 * Creates a new <code>MemberFilterActionGroup</code>.
	 * 
	 * @param viewer the viewer to be filtered
	 * @param viewerId a unique id of the viewer. Used as a key to to store 
	 * the last used filter settings in the preference store
	 * @param inViewMenu if <code>true</code> the actions are added to the view
	 * menu. If <code>false</code> they are added to the toobar.
	 * @param availableFilters Specifies which filter action should be contained. <code>FILTER_NONPUBLIC</code>,
	 * <code>FILTER_STATIC</code>, <code>FILTER_FIELDS</code> and <code>FILTER_LOCALTYPES</code>
	 * or a combination of these constants are possible values. Use <code>ALL_FILTERS</code> to select all available filters.
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId, boolean inViewMenu, int availableFilters) {	
				
		fViewer= viewer;
		fViewerId= viewerId;
		fInViewMenu= inViewMenu;
		
		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		fFilter= new MemberFilter();
		
		String title, helpContext;
		ArrayList actions= new ArrayList(4);
		
		// fields
		int filterProperty= FILTER_FIELDS;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.getString("MemberFilterActionGroup.hide_fields.label"); //$NON-NLS-1$
			helpContext= ICHelpContextIds.FILTER_FIELDS_ACTION;
			MemberFilterAction hideFields= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideFields.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_fields.description")); //$NON-NLS-1$
			hideFields.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_fields.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(hideFields, CPluginImages.T_LCL, "fields_co.gif"); //$NON-NLS-1$
			actions.add(hideFields);
		}
			
		// static
		filterProperty= FILTER_STATIC;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.getString("MemberFilterActionGroup.hide_static.label"); //$NON-NLS-1$
			helpContext= ICHelpContextIds.FILTER_STATIC_ACTION;
			MemberFilterAction hideStatic= new MemberFilterAction(this, title, FILTER_STATIC, helpContext, filterEnabled);
			hideStatic.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_static.description")); //$NON-NLS-1$
			hideStatic.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_static.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(hideStatic, CPluginImages.T_LCL, "static_co.gif"); //$NON-NLS-1$
			actions.add(hideStatic);
		}
		
		// non-public
		filterProperty= FILTER_NONPUBLIC;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.getString("MemberFilterActionGroup.hide_nonpublic.label"); //$NON-NLS-1$
			helpContext= ICHelpContextIds.FILTER_PUBLIC_ACTION;
			MemberFilterAction hideNonPublic= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideNonPublic.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_nonpublic.description")); //$NON-NLS-1$
			hideNonPublic.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_nonpublic.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(hideNonPublic, CPluginImages.T_LCL, "public_co.gif"); //$NON-NLS-1$
			actions.add(hideNonPublic);
		}
		
		// local types
		filterProperty= FILTER_LOCALTYPES;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.getString("MemberFilterActionGroup.hide_localtypes.label"); //$NON-NLS-1$
			helpContext= ICHelpContextIds.FILTER_LOCALTYPES_ACTION;
			MemberFilterAction hideLocalTypes= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideLocalTypes.setDescription(ActionMessages.getString("MemberFilterActionGroup.hide_localtypes.description")); //$NON-NLS-1$
			hideLocalTypes.setToolTipText(ActionMessages.getString("MemberFilterActionGroup.hide_localtypes.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(hideLocalTypes, CPluginImages.T_LCL, "localtypes_co.gif"); //$NON-NLS-1$
			actions.add(hideLocalTypes);
		}
		
		// order corresponds to order in toolbar
		fFilterActions= (MemberFilterAction[]) actions.toArray(new MemberFilterAction[actions.size()]);
		
		fViewer.addFilter(fFilter);
	}

	private boolean isSet(int flag, int set) {
		return (flag & set) != 0;
	}

	private String getPreferenceKey(int filterProperty) {
		return "MemberFilterActionGroup." + fViewerId + '.' + String.valueOf(filterProperty); //$NON-NLS-1$
	}
	
	/**
	 * Sets the member filters.
	 * 
	 * @param filterProperty the filter to be manipulated. Valid values are <code>FILTER_FIELDS</code>, 
	 * <code>FILTER_PUBLIC</code>, and <code>FILTER_PRIVATE</code> as defined by this action 
	 * group
	 * @param set if <code>true</code> the given filter is installed. If <code>false</code> the
	 * given filter is removed
	 * .
	 */	
	public void setMemberFilter(int filterProperty, boolean set) {
		setMemberFilters(new int[] {filterProperty}, new boolean[] {set}, true);
	}

	private void setMemberFilters(int[] propertyKeys, boolean[] propertyValues, boolean refresh) {
		if (propertyKeys.length == 0)
			return;
		Assert.isTrue(propertyKeys.length == propertyValues.length);
		
		for (int i= 0; i < propertyKeys.length; i++) {
			int filterProperty= propertyKeys[i];
			boolean set= propertyValues[i];
			if (set) {
				fFilter.addFilter(filterProperty);
			} else {
				fFilter.removeFilter(filterProperty);
			}
			IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
			
			for (int j= 0; j < fFilterActions.length; j++) {
				int currProperty= fFilterActions[j].getFilterProperty();
				if (currProperty == filterProperty) {
					fFilterActions[j].setChecked(set);
				}
				store.setValue(getPreferenceKey(currProperty), hasMemberFilter(currProperty));
			}
		}
		if (refresh) {
			fViewer.getControl().setRedraw(false);
			BusyIndicator.showWhile(fViewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					fViewer.refresh();
				}
			});
			fViewer.getControl().setRedraw(true);
		}
	}

	/**
	 * Returns <code>true</code> if the given filter is installed.
	 * 
	 * @param filterProperty the filter to be tested. Valid values are <code>FILTER_FIELDS</code>, 
	 * <code>FILTER_PUBLIC</code>, and <code>FILTER_PRIVATE</code> as defined by this action 
	 * group
	 */	
	public boolean hasMemberFilter(int filterProperty) {
		return fFilter.hasFilter(filterProperty);
	}
	
	/**
	 * Saves the state of the filter actions in a memento.
	 * 
	 * @param memento the memento to which the state is saved
	 */
	public void saveState(IMemento memento) {
		memento.putString(TAG_HIDEFIELDS, String.valueOf(hasMemberFilter(FILTER_FIELDS)));
		memento.putString(TAG_HIDESTATIC, String.valueOf(hasMemberFilter(FILTER_STATIC)));
		memento.putString(TAG_HIDENONPUBLIC, String.valueOf(hasMemberFilter(FILTER_NONPUBLIC)));
	}
	
	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 * @param memento the memento from which the state is restored
	 */	
	public void restoreState(IMemento memento) {
		setMemberFilters(
			new int[] {FILTER_FIELDS, FILTER_STATIC, FILTER_NONPUBLIC},
			new boolean[] {
				Boolean.valueOf(memento.getString(TAG_HIDEFIELDS)).booleanValue(),
				Boolean.valueOf(memento.getString(TAG_HIDESTATIC)).booleanValue(),
				Boolean.valueOf(memento.getString(TAG_HIDENONPUBLIC)).booleanValue()
			}, false);
	}
	
	/* (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		contributeToToolBar(actionBars.getToolBarManager());
	}
	
	/**
	 * Adds the filter actions to the given tool bar
	 * 
	 * @param tbm the tool bar to which the actions are added
	 */
	public void contributeToToolBar(IToolBarManager tbm) {
		if (fInViewMenu)
			return;
		tbm.add(fFilterActions[0]); // fields
		tbm.add(fFilterActions[1]); // static
		tbm.add(fFilterActions[2]); // public
	}
	
	/**
	 * Adds the filter actions to the given menu manager.
	 * 
	 * @param menu the menu manager to which the actions are added
	 * @since 2.1
	 */
	public void contributeToViewMenu(IMenuManager menu) {
		if (!fInViewMenu)
			return;
		final String filters= "filters"; //$NON-NLS-1$
		if (menu.find(filters) != null) {
			menu.prependToGroup(filters, fFilterActions[0]); // fields
			menu.prependToGroup(filters, fFilterActions[1]); // static
			menu.prependToGroup(filters, fFilterActions[2]); // public
		} else {
			menu.add(fFilterActions[0]); // fields
			menu.add(fFilterActions[1]); // static
			menu.add(fFilterActions[2]); // public
		}
	}
	
	/* (non-Javadoc)
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		fFilterActions= null;
		fFilter= null;
		fViewer= null;
		
		super.dispose();
	}


}
