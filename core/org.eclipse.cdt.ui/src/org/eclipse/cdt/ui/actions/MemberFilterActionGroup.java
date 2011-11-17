/*******************************************************************************
 * Copyright (c) 2001, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
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
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.viewsupport.MemberFilter;
import org.eclipse.cdt.internal.ui.viewsupport.MemberFilterAction;

public class MemberFilterActionGroup extends ActionGroup {
	public static final int FILTER_NONPUBLIC= MemberFilter.FILTER_NONPUBLIC;
	public static final int FILTER_STATIC= MemberFilter.FILTER_STATIC;
	public static final int FILTER_FIELDS= MemberFilter.FILTER_FIELDS;
	/**
	 * @since 5.1
	 */
	public static final int FILTER_INACTIVE= MemberFilter.FILTER_INACTIVE;

	/** @deprecated Unsupported filter constant */
	@Deprecated
	public static final int FILTER_LOCALTYPES= MemberFilter.FILTER_LOCALTYPES;

	/**
	 * @deprecated we may choose to add more filters in future versions.
	 */
	@Deprecated
	public static final int ALL_FILTERS= FILTER_NONPUBLIC | FILTER_FIELDS | FILTER_STATIC;

	private static final String TAG_HIDEFIELDS= "hidefields"; //$NON-NLS-1$
	private static final String TAG_HIDESTATIC= "hidestatic"; //$NON-NLS-1$
	private static final String TAG_HIDENONPUBLIC= "hidenonpublic"; //$NON-NLS-1$
	private static final String TAG_HIDEINACTIVE= "hideinactive"; //$NON-NLS-1$

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
		boolean doHideInactive= store.getBoolean(getPreferenceKey(FILTER_INACTIVE));

		fFilter= new MemberFilter();
		if (doHideFields)
			fFilter.addFilter(FILTER_FIELDS);
		if (doHideStatic)
			fFilter.addFilter(FILTER_STATIC);
		if (doHidePublic)
			fFilter.addFilter(FILTER_NONPUBLIC);
		if (doHideInactive)
			fFilter.addFilter(FILTER_INACTIVE);

		// fields
		String title= ActionMessages.MemberFilterActionGroup_hide_fields_label;
		String helpContext= ICHelpContextIds.FILTER_FIELDS_ACTION;
		MemberFilterAction hideFields= new MemberFilterAction(this, title, FILTER_FIELDS, helpContext, doHideFields);
		hideFields.setDescription(ActionMessages.MemberFilterActionGroup_hide_fields_description);
		hideFields.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_fields_tooltip);
		CPluginImages.setImageDescriptors(hideFields, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_FIELDS);

		// static
		title= ActionMessages.MemberFilterActionGroup_hide_static_label;
		helpContext= ICHelpContextIds.FILTER_STATIC_ACTION;
		MemberFilterAction hideStatic= new MemberFilterAction(this, title, FILTER_STATIC, helpContext, doHideStatic);
		hideStatic.setDescription(ActionMessages.MemberFilterActionGroup_hide_static_description);
		hideStatic.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_static_tooltip);
		CPluginImages.setImageDescriptors(hideStatic, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_STATIC);

		// non-public
		title= ActionMessages.MemberFilterActionGroup_hide_nonpublic_label;
		helpContext= ICHelpContextIds.FILTER_PUBLIC_ACTION;
		MemberFilterAction hideNonPublic= new MemberFilterAction(this, title, FILTER_NONPUBLIC, helpContext, doHidePublic);
		hideNonPublic.setDescription(ActionMessages.MemberFilterActionGroup_hide_nonpublic_description);
		hideNonPublic.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_nonpublic_tooltip);
		CPluginImages.setImageDescriptors(hideNonPublic, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_PUBLIC);

		// inactive
		title= ActionMessages.MemberFilterActionGroup_hide_inactive_label;
		MemberFilterAction hideInactive= new MemberFilterAction(this, title, FILTER_INACTIVE, null, doHideInactive);
		hideInactive.setDescription(ActionMessages.MemberFilterActionGroup_hide_inactive_description);
		hideInactive.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_inactive_tooltip);
		CPluginImages.setImageDescriptors(hideInactive, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_INACTIVE);

		// order corresponds to order in toolbar
		fFilterActions= new MemberFilterAction[] { hideFields, hideStatic, hideNonPublic, hideInactive };

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
	 * @param availableFilters Specifies which filter action should be contained. {@link #FILTER_NONPUBLIC},
	 * {@link #FILTER_STATIC}, {@link #FILTER_FIELDS}, {@link #FILTER_INACTIVE}
	 * or a combination of these constants are possible values.
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId, boolean inViewMenu, int availableFilters) {

		fViewer= viewer;
		fViewerId= viewerId;
		fInViewMenu= inViewMenu;

		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		fFilter= new MemberFilter();

		String title, helpContext;
		ArrayList<MemberFilterAction> actions= new ArrayList<MemberFilterAction>(4);

		// fields
		int filterProperty= FILTER_FIELDS;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.MemberFilterActionGroup_hide_fields_label;
			helpContext= ICHelpContextIds.FILTER_FIELDS_ACTION;
			MemberFilterAction hideFields= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideFields.setDescription(ActionMessages.MemberFilterActionGroup_hide_fields_description);
			hideFields.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_fields_tooltip);
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
			title= ActionMessages.MemberFilterActionGroup_hide_static_label;
			helpContext= ICHelpContextIds.FILTER_STATIC_ACTION;
			MemberFilterAction hideStatic= new MemberFilterAction(this, title, FILTER_STATIC, helpContext, filterEnabled);
			hideStatic.setDescription(ActionMessages.MemberFilterActionGroup_hide_static_description);
			hideStatic.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_static_tooltip);
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
			title= ActionMessages.MemberFilterActionGroup_hide_nonpublic_label;
			helpContext= ICHelpContextIds.FILTER_PUBLIC_ACTION;
			MemberFilterAction hideNonPublic= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideNonPublic.setDescription(ActionMessages.MemberFilterActionGroup_hide_nonpublic_description);
			hideNonPublic.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_nonpublic_tooltip);
			CPluginImages.setImageDescriptors(hideNonPublic, CPluginImages.T_LCL, "public_co.gif"); //$NON-NLS-1$
			actions.add(hideNonPublic);
		}

		// non-public
		filterProperty= FILTER_INACTIVE;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.MemberFilterActionGroup_hide_inactive_label;
			MemberFilterAction hideInactive= new MemberFilterAction(this, title, filterProperty, null, filterEnabled);
			hideInactive.setDescription(ActionMessages.MemberFilterActionGroup_hide_inactive_description);
			hideInactive.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_inactive_tooltip);
			CPluginImages.setImageDescriptors(hideInactive, CPluginImages.T_LCL, "filterInactive.gif"); //$NON-NLS-1$
			actions.add(hideInactive);
		}

		// order corresponds to order in toolbar
		fFilterActions= actions.toArray(new MemberFilterAction[actions.size()]);

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
	 * <code>FILTER_PUBLIC</code>, <code>FILTER_PRIVATE</code> and <code>FILTER_INACTIVE</code> as defined by this action
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
				@Override
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
	 * <code>FILTER_PUBLIC</code>, <code>FILTER_PRIVATE</code> and <code>FILTER_INACTIVE</code>
	 * as defined by this action group
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
		memento.putString(TAG_HIDEINACTIVE, String.valueOf(hasMemberFilter(FILTER_INACTIVE)));
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
			new int[] {FILTER_FIELDS, FILTER_STATIC, FILTER_NONPUBLIC, FILTER_INACTIVE},
			new boolean[] {
				Boolean.valueOf(memento.getString(TAG_HIDEFIELDS)).booleanValue(),
				Boolean.valueOf(memento.getString(TAG_HIDESTATIC)).booleanValue(),
				Boolean.valueOf(memento.getString(TAG_HIDENONPUBLIC)).booleanValue(),
				Boolean.valueOf(memento.getString(TAG_HIDEINACTIVE)).booleanValue()
			}, false);
	}

	/* (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	@Override
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
		tbm.add(fFilterActions[3]); // inactive
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
			menu.prependToGroup(filters, fFilterActions[3]); // inactive
		} else {
			menu.add(fFilterActions[0]); // fields
			menu.add(fFilterActions[1]); // static
			menu.add(fFilterActions[2]); // public
			menu.add(fFilterActions[3]); // inactive
		}
	}

	/* (non-Javadoc)
	 * @see ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		fFilterActions= null;
		fFilter= null;
		fViewer= null;

		super.dispose();
	}


}
