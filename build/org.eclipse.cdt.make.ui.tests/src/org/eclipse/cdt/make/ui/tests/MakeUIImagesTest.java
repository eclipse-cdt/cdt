/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.junit.Test;

public class MakeUIImagesTest {
	private static final ImageDescriptor MISSING_IMAGE_DESCRIPTOR = ImageDescriptor.getMissingImageDescriptor();

	// sample image (IMG_ETOOL_MAKEFILE) from MakeUIPlugin bundle
	private static final String KEY_ETOOL_MAKEFILE = MakeUIImages.IMG_ETOOL_MAKEFILE;
	private static final IPath PATH_ETOOL_MAKEFILE = new Path("icons/etool16/makefile.gif");
	private static final URL URL_ETOOL_MAKEFILE = FileLocator.find(MakeUIPlugin.getDefault().getBundle(),
			PATH_ETOOL_MAKEFILE, null);

	/**
	 * Handling of missing keys.
	 */
	@Test
	public void testNoImage() throws Exception {
		ImageDescriptor descriptor = MakeUIImages.getImageDescriptor("missing key 1");
		assertSame(MISSING_IMAGE_DESCRIPTOR, descriptor);

		Image image1 = MakeUIImages.getImage("missing key 1");
		Image image2 = MakeUIImages.getImage("missing key 2");
		assertSame(image1, image2);
	}

	/**
	 * Test regular images.
	 */
	@Test
	public void testImage() throws Exception {
		// create descriptor from MakeUIImages key
		ImageDescriptor descriptorFromKey = MakeUIImages.getImageDescriptor(KEY_ETOOL_MAKEFILE);
		assertNotSame(MISSING_IMAGE_DESCRIPTOR, descriptorFromKey);

		// create descriptor from registered bundle URL as a key
		MakeUIImages.register(URL_ETOOL_MAKEFILE);
		ImageDescriptor descriptorFromUrl = MakeUIImages.getImageDescriptor(URL_ETOOL_MAKEFILE.toString());
		assertNotSame(MISSING_IMAGE_DESCRIPTOR, descriptorFromUrl);
		assertSame(descriptorFromKey, descriptorFromUrl);

		// verify that it is the same image
		Image imageFromKey = MakeUIImages.getImage(KEY_ETOOL_MAKEFILE);
		Image imageFromUrl = MakeUIImages.getImage(URL_ETOOL_MAKEFILE.toString());
		assertSame(imageFromKey, imageFromUrl);

		// verify that no leaks on second access
		Image imageFromKey2 = MakeUIImages.getImage(KEY_ETOOL_MAKEFILE);
		assertSame(imageFromKey, imageFromKey2);
	}

	/**
	 * Test images with overlays.
	 */
	@Test
	public void testOverlays() throws Exception {
		{
			Image image1 = MakeUIImages.getImageOverlaid(KEY_ETOOL_MAKEFILE, new String[5]);
			Image image2 = MakeUIImages.getImage(KEY_ETOOL_MAKEFILE);
			assertSame(image1, image2);
		}

		{
			String[] overlayKeys = new String[5];
			overlayKeys[IDecoration.BOTTOM_LEFT] = MakeUIImages.IMG_OVR_AUTOMATIC;
			Image imageOver1 = MakeUIImages.getImageOverlaid(KEY_ETOOL_MAKEFILE, overlayKeys);
			Image imageOver2 = MakeUIImages.getImageOverlaid(KEY_ETOOL_MAKEFILE, MakeUIImages.IMG_OVR_AUTOMATIC,
					IDecoration.BOTTOM_LEFT);
			assertSame(imageOver1, imageOver2);
		}
	}

	/**
	 * Verify that MakeUIImages constants define existing images.
	 */
	@Test
	public void testVerifyFields() throws Exception {
		Class<MakeUIImages> clazz = MakeUIImages.class;

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			if (name.startsWith("IMG_")) {
				assertEquals("MakeUIImages." + name + " is not a String", String.class, field.getType());
				assertTrue("MakeUIImages." + name + " is not a static field",
						(field.getModifiers() & Modifier.STATIC) != 0);
				assertTrue("MakeUIImages." + name + " is not a public field",
						(field.getModifiers() & Modifier.PUBLIC) != 0);
				String imageKey = (String) field.get(null);
				ImageDescriptor descriptor = MakeUIImages.getImageDescriptor(imageKey);
				assertTrue("Missing image MakeUIImages." + name + "=\"" + imageKey + "\"",
						descriptor != MISSING_IMAGE_DESCRIPTOR);
			}
		}
	}
}
