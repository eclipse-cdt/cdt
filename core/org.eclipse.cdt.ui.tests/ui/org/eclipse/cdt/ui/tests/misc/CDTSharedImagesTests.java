/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import junit.framework.TestCase;

/**
 * Tests for CDT shared images repository.
 */
public class CDTSharedImagesTests extends TestCase {
	private static final ImageDescriptor MISSING_IMAGE_DESCRIPTOR = ImageDescriptor.getMissingImageDescriptor();

	// sample image (IMG_OBJS_TUNIT) from CUIPlugin bundle
	private static final String KEY_OBJS_TUNIT = CDTSharedImages.IMG_OBJS_TUNIT;
	private static final IPath PATH_OBJS_TUNIT = new Path("icons/obj16/c_file_obj.gif");
	private static final URL URL_OBJS_TUNIT = FileLocator.find(CUIPlugin.getDefault().getBundle(), PATH_OBJS_TUNIT,
			null);

	/**
	 * Handling of missing keys.
	 */
	public void testNoImage() throws Exception {
		ImageDescriptor descriptor = CDTSharedImages.getImageDescriptor("missing key 1");
		assertSame(MISSING_IMAGE_DESCRIPTOR, descriptor);

		Image image1 = CDTSharedImages.getImage("missing key 1");
		Image image2 = CDTSharedImages.getImage("missing key 2");
		assertSame(image1, image2);
	}

	/**
	 * Test regular images.
	 */
	public void testImage() throws Exception {
		// create descriptor from CDTSharedImages key
		ImageDescriptor descriptorFromKey = CDTSharedImages.getImageDescriptor(KEY_OBJS_TUNIT);
		assertNotSame(MISSING_IMAGE_DESCRIPTOR, descriptorFromKey);

		// create descriptor from registered bundle URL as a key
		CDTSharedImages.register(URL_OBJS_TUNIT);
		ImageDescriptor descriptorFromUrl = CDTSharedImages.getImageDescriptor(URL_OBJS_TUNIT.toString());
		assertNotSame(MISSING_IMAGE_DESCRIPTOR, descriptorFromUrl);
		assertSame(descriptorFromKey, descriptorFromUrl);

		// verify that it is the same image
		Image imageFromKey = CDTSharedImages.getImage(KEY_OBJS_TUNIT);
		Image imageFromUrl = CDTSharedImages.getImage(URL_OBJS_TUNIT.toString());
		assertSame(imageFromKey, imageFromUrl);

		// verify that no leaks on second access
		Image imageFromKey2 = CDTSharedImages.getImage(KEY_OBJS_TUNIT);
		assertSame(imageFromKey, imageFromKey2);
	}

	/**
	 * Test images with overlays.
	 */
	public void testOverlays() throws Exception {
		{
			Image image1 = CDTSharedImages.getImageOverlaid(KEY_OBJS_TUNIT, new String[5]);
			Image image2 = CDTSharedImages.getImage(KEY_OBJS_TUNIT);
			assertSame(image1, image2);
		}

		{
			String[] overlayKeys = new String[5];
			overlayKeys[IDecoration.BOTTOM_LEFT] = CDTSharedImages.IMG_OVR_WARNING;
			Image imageOver1 = CDTSharedImages.getImageOverlaid(KEY_OBJS_TUNIT, overlayKeys);
			Image imageOver2 = CDTSharedImages.getImageOverlaid(KEY_OBJS_TUNIT, CDTSharedImages.IMG_OVR_WARNING,
					IDecoration.BOTTOM_LEFT);
			Image imageOver3 = CDTSharedImages.getImageWithWarning(KEY_OBJS_TUNIT);
			assertSame(imageOver1, imageOver2);
			assertSame(imageOver1, imageOver3);
		}
	}

	/**
	 * Verify that CDTSharedImages constants define existing images.
	 */
	public void testVerifyFields() throws Exception {
		Class<CDTSharedImages> clazz = CDTSharedImages.class;

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			if (name.startsWith("IMG_")) {
				assertEquals("CDTSharedImages." + name + " is not a String", String.class, field.getType());
				assertTrue("CDTSharedImages." + name + " is not a static field",
						(field.getModifiers() & Modifier.STATIC) != 0);
				assertTrue("CDTSharedImages." + name + " is not a public field",
						(field.getModifiers() & Modifier.PUBLIC) != 0);
				String imageKey = (String) field.get(null);
				ImageDescriptor descriptor = CDTSharedImages.getImageDescriptor(imageKey);
				assertTrue("Missing image CDTSharedImages." + name + "=\"" + imageKey + "\"",
						descriptor != MISSING_IMAGE_DESCRIPTOR);
			}
		}
	}
}
