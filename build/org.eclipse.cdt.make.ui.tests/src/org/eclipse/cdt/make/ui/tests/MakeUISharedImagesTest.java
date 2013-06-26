/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.MakeUISharedImages;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.junit.Test;

public class MakeUISharedImagesTest {
	private static final ImageDescriptor MISSING_IMAGE_DESCRIPTOR = ImageDescriptor.getMissingImageDescriptor();

	// sample image (IMG_ETOOL_MAKEFILE) from MakeUIPlugin bundle
	private static final String KEY_ETOOL_MAKEFILE = MakeUISharedImages.IMG_ETOOL_MAKEFILE;
	private static final IPath PATH_ETOOL_MAKEFILE = new Path("icons/etool16/makefile.gif");
	private static final URL URL_ETOOL_MAKEFILE= FileLocator.find(MakeUIPlugin.getDefault().getBundle(), PATH_ETOOL_MAKEFILE, null);

	/**
	 * Handling of missing keys.
	 */
	@Test
	public void testNoImage() throws Exception {
		ImageDescriptor descriptor = MakeUISharedImages.getImageDescriptor("missing key 1");
		assertSame(MISSING_IMAGE_DESCRIPTOR, descriptor);

		Image image1 = MakeUISharedImages.getImage("missing key 1");
		Image image2 = MakeUISharedImages.getImage("missing key 2");
		assertSame(image1, image2);
	}

	/**
	 * Test regular images.
	 */
	@Test
	public void testImage() throws Exception {
		// create descriptor from MakeUISharedImages key
		ImageDescriptor descriptorFromKey = MakeUISharedImages.getImageDescriptor(KEY_ETOOL_MAKEFILE);
		assertNotSame(MISSING_IMAGE_DESCRIPTOR, descriptorFromKey);

		// create descriptor from registered bundle URL as a key
		MakeUISharedImages.register(URL_ETOOL_MAKEFILE);
		ImageDescriptor descriptorFromUrl = MakeUISharedImages.getImageDescriptor(URL_ETOOL_MAKEFILE.toString());
		assertNotSame(MISSING_IMAGE_DESCRIPTOR, descriptorFromUrl);
		assertSame(descriptorFromKey, descriptorFromUrl);

		// verify that it is the same image
		Image imageFromKey = MakeUISharedImages.getImage(KEY_ETOOL_MAKEFILE);
		Image imageFromUrl = MakeUISharedImages.getImage(URL_ETOOL_MAKEFILE.toString());
		assertSame(imageFromKey, imageFromUrl);

		// verify that no leaks on second access
		Image imageFromKey2 = MakeUISharedImages.getImage(KEY_ETOOL_MAKEFILE);
		assertSame(imageFromKey, imageFromKey2);
	}

	/**
	 * Test images with overlays.
	 */
	@Test
	public void testOverlays() throws Exception {
		{
			Image image1 = MakeUISharedImages.getImageOverlaid(KEY_ETOOL_MAKEFILE, new String[5]);
			Image image2 = MakeUISharedImages.getImage(KEY_ETOOL_MAKEFILE);
			assertSame(image1, image2);
		}

		{
			String[] overlayKeys = new String[5];
			overlayKeys[IDecoration.BOTTOM_LEFT] = MakeUISharedImages.IMG_OVR_AUTOMATIC;
			Image imageOver1 = MakeUISharedImages.getImageOverlaid(KEY_ETOOL_MAKEFILE, overlayKeys);
			Image imageOver2 = MakeUISharedImages.getImageOverlaid(KEY_ETOOL_MAKEFILE, MakeUISharedImages.IMG_OVR_AUTOMATIC, IDecoration.BOTTOM_LEFT);
			assertSame(imageOver1, imageOver2);
		}
	}

	/**
	 * Verify that MakeUISharedImages constants define existing images.
	 */
	@Test
	public void testVerifyFields() throws Exception {
		Class<MakeUISharedImages> clazz = MakeUISharedImages.class;

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			if (name.startsWith("IMG_")) {
				assertEquals("MakeUISharedImages."+name+" is not a String", String.class, field.getType());
				assertTrue("MakeUISharedImages."+name+" is not a static field", (field.getModifiers() & Modifier.STATIC) != 0);
				assertTrue("MakeUISharedImages."+name+" is not a public field", (field.getModifiers() & Modifier.PUBLIC) != 0);
				String imageKey = (String) field.get(null);
				ImageDescriptor descriptor = MakeUISharedImages.getImageDescriptor(imageKey);
				assertTrue("Missing image MakeUISharedImages."+name+"=\""+imageKey+"\"", descriptor!=MISSING_IMAGE_DESCRIPTOR);
			}
		}
	}
}
