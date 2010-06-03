/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.util;

import org.eclipse.swt.graphics.RGB;

/**
 * HSL (Hue, Saturation, Luminance) color model.
 */
public class HSL {

	public double hue;
	public double saturation;
	public double luminance;

	/**
	 * Create HSL from RGB.
	 */
	public HSL(RGB rgb) {
		super();
		double red = rgb.red / 255.0;
		double green = rgb.green / 255.0;
		double blue = rgb.blue / 255.0;
		double cmax= Math.max(Math.max(red, green), blue);
		double cmin= Math.min(Math.min(red, green), blue);
		luminance = (cmax+cmin)/2;
		if (cmax == cmin) {
			hue = 0;
			saturation = 0;
		} else {
			double delta = cmax-cmin;
			if (luminance < 0.5) {
				saturation = delta / (cmax + cmin);
			} else {
				saturation = delta / (2 - cmax - cmin);
			}
			if (red == cmax) {
				hue = (green - blue) / delta;
			} else if (green == cmax) {
				hue = 2 + (blue - red) / delta;
			} else {
				hue = 4 + (red - green) / delta;
			}
			hue /= 6;
			if (hue < 0) {
				hue += 1;
			} else if (hue > 1) {
				hue -= 1;
			}
		}
	}

	public RGB toRGB() {
		int red,green,blue;
		if (saturation == 0) {
			red = (int)Math.round(255*luminance);
			green = red;
			blue = red;
		} else {
			double m1, m2;
			if (luminance <= 0.5) {
				m2 = luminance * (1 + saturation);
			} else {
				m2 = luminance + saturation - luminance * saturation;
			}
			m1 = 2 * luminance - m2;
			red = hueToColorValue(hue + 1./3., m1, m2);
			green = hueToColorValue(hue, m1, m2);
			blue = hueToColorValue(hue - 1./3., m1, m2);
		}
		return new RGB(red, green, blue);
	}

	private static int hueToColorValue(double hue, double m1, double m2) {
		double v;
		if (hue < 0) {
			hue += 1;
		} else if (hue > 1) {
			hue -= 1;
		}
		if (6*hue < 1) {
			v = m1 + (m2-m1) * hue * 6;
		} else if (2*hue < 1) {
			v = m2;
		} else if (3*hue < 2) {
			v = m1 + (m2-m1) * (2./3. - hue) * 6;
		} else {
			v = m1;
		}
		return (int)Math.round(255 * v);
	}

	/**
	 * Returns a string containing a concise, human-readable
	 * description of the receiver.
	 *
	 * @return a string representation of the <code>HSL</code>
	 */
	@Override
	public String toString () {
		return "HSL {" + hue + ", " + saturation + ", " + luminance + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
