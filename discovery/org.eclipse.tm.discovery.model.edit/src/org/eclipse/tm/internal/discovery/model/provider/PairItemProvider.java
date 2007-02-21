/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.model.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITableItemLabelProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.eclipse.tm.discovery.model.ModelPackage;
import org.eclipse.tm.discovery.model.Pair;

/**
 * This is the item provider adapter for a {@link org.eclipse.tm.discovery.model.Pair} object.
 * 
 * @generated not
 */
public class PairItemProvider
	extends ItemProviderAdapter
	implements	
		IEditingDomainItemProvider, 
		IStructuredItemContentProvider, 
		ITreeItemContentProvider, 
		IItemLabelProvider, 
		IItemPropertySource, 
		ITableItemLabelProvider
		{
	/**
	 * This constructs an instance from a factory and a notifier.
	 * @param adapterFactory 
	 * 
	 * @generated
	 */
	public PairItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * 
	 * @generated
	 */
	public List getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addKeyPropertyDescriptor(object);
			addValuePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Key feature.
	 * 
	 * @generated
	 */
	protected void addKeyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Pair_key_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Pair_key_feature", "_UI_Pair_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 ModelPackage.Literals.PAIR__KEY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Value feature.
	 * 
	 * @generated
	 */
	protected void addValuePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Pair_value_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Pair_value_feature", "_UI_Pair_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 ModelPackage.Literals.PAIR__VALUE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This returns Pair.gif.
	 * 
	 * @generated NOT
	 */
	public Object getImage(Object object) {
		return null;
	}
	
	/**
	 * This returns the label text for the adapted class.
	 * 
	 * @generated NOT
	 */
	public String getText(Object object) {
		return null;
	}
	
	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * 
	 * @generated
	 */
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(Pair.class)) {
			case ModelPackage.PAIR__KEY:
			case ModelPackage.PAIR__VALUE:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
	 * describing all of the children that can be created under this object.
	 * 
	 * @generated
	 */
	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * 
	 * @generated
	 */
	public ResourceLocator getResourceLocator() {
		return DiscoveryModelEditPlugin.INSTANCE;
	}

	/**
	 *  @generated NOT
	 */
	
	public Object getColumnImage(Object object, int columnIndex) {
		return null;
	}
	
	/**
	 *  @generated NOT
	 */
	
	
	public String getColumnText(Object object, int columnIndex) {
		
		String returnString = null;
		
		if(columnIndex == 0)
		{
			returnString = ((Pair)object).getKey();
		}
		else if(columnIndex == 1)
		{
			returnString = ((Pair)object).getValue();
		}
		return returnString;
	}

}
