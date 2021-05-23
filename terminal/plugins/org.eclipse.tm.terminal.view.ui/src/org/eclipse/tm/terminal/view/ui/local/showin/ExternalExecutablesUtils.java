/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.local.showin;

import java.io.File;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Function;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class ExternalExecutablesUtils {

	/**
	 * Loads the image data suitable for showing an icon in a menu
	 * (16 x 16, 8bit depth) from the given file.
	 *
	 * @param path The image file path. Must not be <code>null</code>.
	 * @return The image data or <code>null</code>.
	 */
	public static ImageData loadImage(String path) {
		Assert.isNotNull(path);

		ImageData id = null;
		ImageData biggest = null;

		ImageLoader loader = new ImageLoader();
		ImageData[] data = loader.load(path);

		if (data != null) {
			for (ImageData d : data) {
				if (d.height == 16 && d.width == 16) {
					if (id == null || id.height != 16 && id.width != 16) {
						id = d;
					} else if (d.depth < id.depth && d.depth >= 8) {
						id = d;
					}
				} else {
					if (id == null) {
						id = d;
						biggest = d;
					} else if (id.height != 16 && d.height < id.height && id.width != 16 && d.width < id.width) {
						id = d;
					} else if (biggest == null || d.height > biggest.height && d.width > biggest.width) {
						biggest = d;
					}
				}
			}
		}

		// if the icon is still too big -> downscale the biggest
		if (id != null && id.height > 16 && id.width > 16 && biggest != null) {
			id = biggest.scaledTo(16, 16);
		}

		return id;
	}

	public static <T> Optional<T> visitPATH(Function<String, Optional<T>> r) {
		String path = System.getenv("PATH"); //$NON-NLS-1$
		if (path != null) {
			StringTokenizer tokenizer = new StringTokenizer(path, File.pathSeparator);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();

				Optional<T> apply = r.apply(token);
				if (apply.isPresent()) {
					return apply;
				}
			}
		}
		return Optional.empty();
	}

}
