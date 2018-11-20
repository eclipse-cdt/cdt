/*******************************************************************************
 * Copyright (c) 2012 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils;

import static org.eclipse.cdt.internal.core.ByteUtils.makeInt;
import static org.eclipse.cdt.internal.core.ByteUtils.makeLong;
import static org.eclipse.cdt.internal.core.ByteUtils.makeShort;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ByteUtilsTest extends TestCase {

	public static Test suite() {
		return new TestSuite(ByteUtilsTest.class);
	}

	// Allows us to avoid ugly misalignment in the source
	private static byte Ox80 = (byte) 0x80;
	private static byte Oxff = (byte) 0xff;

	public void testMakeShort() throws Exception {
		Assert.assertEquals((short) 0x0000, makeShort(new byte[] { 0x00, 0x00 }, 0, false));
		Assert.assertEquals((short) 0x7f00, makeShort(new byte[] { 0x7f, 0x00 }, 0, false));
		Assert.assertEquals((short) 0x007f, makeShort(new byte[] { 0x00, 0x7f }, 0, false));
		Assert.assertEquals((short) 0x8000, makeShort(new byte[] { Ox80, 0x00 }, 0, false));
		Assert.assertEquals((short) 0x0080, makeShort(new byte[] { 0x00, Ox80 }, 0, false));
		Assert.assertEquals((short) 0xff00, makeShort(new byte[] { Oxff, 0x00 }, 0, false));
		Assert.assertEquals((short) 0x00ff, makeShort(new byte[] { 0x00, Oxff }, 0, false));
		Assert.assertEquals((short) 0xffff, makeShort(new byte[] { Oxff, Oxff }, 0, false));

		Assert.assertEquals((short) 0x0000, makeShort(new byte[] { 0x00, 0x00 }, 0, true));
		Assert.assertEquals((short) 0x007f, makeShort(new byte[] { 0x7f, 0x00 }, 0, true));
		Assert.assertEquals((short) 0x7f00, makeShort(new byte[] { 0x00, 0x7f }, 0, true));
		Assert.assertEquals((short) 0x0080, makeShort(new byte[] { Ox80, 0x00 }, 0, true));
		Assert.assertEquals((short) 0x8000, makeShort(new byte[] { 0x00, Ox80 }, 0, true));
		Assert.assertEquals((short) 0x00ff, makeShort(new byte[] { Oxff, 0x00 }, 0, true));
		Assert.assertEquals((short) 0xff00, makeShort(new byte[] { 0x00, Oxff }, 0, true));
		Assert.assertEquals((short) 0xffff, makeShort(new byte[] { Oxff, Oxff }, 0, true));

		Assert.assertEquals(0x0102, makeShort(new byte[] { 0, 0, 0, 0x01, 0x02 }, 3, false));
		Assert.assertEquals(0x0201, makeShort(new byte[] { 0, 0, 0, 0x01, 0x02 }, 3, true));
	}

	public void testMakeInt() throws Exception {
		Assert.assertEquals(0x00000000, makeInt(new byte[] { 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x7f000000, makeInt(new byte[] { 0x7f, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x007f0000, makeInt(new byte[] { 0x00, 0x7f, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00007f00, makeInt(new byte[] { 0x00, 0x00, 0x7f, 0x00 }, 0, false));
		Assert.assertEquals(0x0000007f, makeInt(new byte[] { 0x00, 0x00, 0x00, 0x7f }, 0, false));
		Assert.assertEquals(0x80000000, makeInt(new byte[] { Ox80, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00800000, makeInt(new byte[] { 0x00, Ox80, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00008000, makeInt(new byte[] { 0x00, 0x00, Ox80, 0x00 }, 0, false));
		Assert.assertEquals(0x00000080, makeInt(new byte[] { 0x00, 0x00, 0x00, Ox80 }, 0, false));
		Assert.assertEquals(0xff000000, makeInt(new byte[] { Oxff, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00ff0000, makeInt(new byte[] { 0x00, Oxff, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000ff00, makeInt(new byte[] { 0x00, 0x00, Oxff, 0x00 }, 0, false));
		Assert.assertEquals(0x000000ff, makeInt(new byte[] { 0x00, 0x00, 0x00, Oxff }, 0, false));
		Assert.assertEquals(0xffffffff, makeInt(new byte[] { Oxff, Oxff, Oxff, Oxff }, 0, false));

		Assert.assertEquals(0x00000000, makeInt(new byte[] { 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000007f, makeInt(new byte[] { 0x7f, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00007f00, makeInt(new byte[] { 0x00, 0x7f, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x007f0000, makeInt(new byte[] { 0x00, 0x00, 0x7f, 0x00 }, 0, true));
		Assert.assertEquals(0x7f000000, makeInt(new byte[] { 0x00, 0x00, 0x00, 0x7f }, 0, true));
		Assert.assertEquals(0x00000080, makeInt(new byte[] { Ox80, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00008000, makeInt(new byte[] { 0x00, Ox80, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00800000, makeInt(new byte[] { 0x00, 0x00, Ox80, 0x00 }, 0, true));
		Assert.assertEquals(0x80000000, makeInt(new byte[] { 0x00, 0x00, 0x00, Ox80 }, 0, true));
		Assert.assertEquals(0x000000ff, makeInt(new byte[] { Oxff, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000ff00, makeInt(new byte[] { 0x00, Oxff, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00ff0000, makeInt(new byte[] { 0x00, 0x00, Oxff, 0x00 }, 0, true));
		Assert.assertEquals(0xff000000, makeInt(new byte[] { 0x00, 0x00, 0x00, Oxff }, 0, true));
		Assert.assertEquals(0xffffffff, makeInt(new byte[] { Oxff, Oxff, Oxff, Oxff }, 0, true));

		Assert.assertEquals(0x01020304, makeInt(new byte[] { 0, 0, 0, 0x01, 0x02, 0x03, 0x04 }, 3, false));
		Assert.assertEquals(0x04030201, makeInt(new byte[] { 0, 0, 0, 0x01, 0x02, 0x03, 0x04 }, 3, true));
	}

	public void testMakeLong() throws Exception {
		Assert.assertEquals(0x0000000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x7f00000000000000L,
				makeLong(new byte[] { 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x007f000000000000L,
				makeLong(new byte[] { 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00007f0000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000007f00000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x000000007f000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00000000007f0000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000000000007f00L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00 }, 0, false));
		Assert.assertEquals(0x000000000000007fL,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f }, 0, false));
		Assert.assertEquals(0x8000000000000000L,
				makeLong(new byte[] { Ox80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0080000000000000L,
				makeLong(new byte[] { 0x00, Ox80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000800000000000L,
				makeLong(new byte[] { 0x00, 0x00, Ox80, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000008000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, Ox80, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000000080000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, Ox80, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000000000800000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, Ox80, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000000000008000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Ox80, 0x00 }, 0, false));
		Assert.assertEquals(0x0000000000000080L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Ox80 }, 0, false));
		Assert.assertEquals(0xff00000000000000L,
				makeLong(new byte[] { Oxff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00ff000000000000L,
				makeLong(new byte[] { 0x00, Oxff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000ff0000000000L,
				makeLong(new byte[] { 0x00, 0x00, Oxff, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x000000ff00000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, Oxff, 0x00, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x00000000ff000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, Oxff, 0x00, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x0000000000ff0000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, Oxff, 0x00, 0x00 }, 0, false));
		Assert.assertEquals(0x000000000000ff00L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Oxff, 0x00 }, 0, false));
		Assert.assertEquals(0x00000000000000ffL,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Oxff }, 0, false));
		Assert.assertEquals(0xffffffffffffffffL,
				makeLong(new byte[] { Oxff, Oxff, Oxff, Oxff, Oxff, Oxff, Oxff, Oxff }, 0, false));

		Assert.assertEquals(0x0000000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x000000000000007fL,
				makeLong(new byte[] { 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000000000007f00L,
				makeLong(new byte[] { 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00000000007f0000L,
				makeLong(new byte[] { 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x000000007f000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000007f00000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00007f0000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x007f000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00 }, 0, true));
		Assert.assertEquals(0x7f00000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f }, 0, true));
		Assert.assertEquals(0x0000000000000080L,
				makeLong(new byte[] { Ox80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000000000008000L,
				makeLong(new byte[] { 0x00, Ox80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000000000800000L,
				makeLong(new byte[] { 0x00, 0x00, Ox80, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000000080000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, Ox80, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000008000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, Ox80, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000800000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, Ox80, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0080000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Ox80, 0x00 }, 0, true));
		Assert.assertEquals(0x8000000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Ox80 }, 0, true));
		Assert.assertEquals(0x00000000000000ffL,
				makeLong(new byte[] { Oxff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x000000000000ff00L,
				makeLong(new byte[] { 0x00, Oxff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000000000ff0000L,
				makeLong(new byte[] { 0x00, 0x00, Oxff, 0x00, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00000000ff000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, Oxff, 0x00, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x000000ff00000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, Oxff, 0x00, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x0000ff0000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, Oxff, 0x00, 0x00 }, 0, true));
		Assert.assertEquals(0x00ff000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Oxff, 0x00 }, 0, true));
		Assert.assertEquals(0xff00000000000000L,
				makeLong(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, Oxff }, 0, true));
		Assert.assertEquals(0xffffffffffffffffL,
				makeLong(new byte[] { Oxff, Oxff, Oxff, Oxff, Oxff, Oxff, Oxff, Oxff }, 0, true));

		Assert.assertEquals(0x0102030405060708L,
				makeLong(new byte[] { 0, 0, 0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 }, 3, false));
		Assert.assertEquals(0x0807060504030201L,
				makeLong(new byte[] { 0, 0, 0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 }, 3, true));
	}
}
