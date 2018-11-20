/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class FPutilities {
	private static final int BYTE_MASK = 0xFF;

	// ANSI C "Smallest" and "largest" negative and positive float and double values

	public static final float floatNegMax = -3.40282347E+38f; // Largest negative float value; farthest from zero
	public static final float floatNegMin = -1.17549435E-38f; // Smallest negative float value; closest to zero
	public static final float floatPosMax = 1.17549435E+38f; // Largest positive float value; farthest from zero
	public static final float floatPosMin = 3.40282347E-38f; // Smallest positive float value; closest to zero

	public static final double doubleNegMax = -1.7976931348623157E+308; // Largest positive double value
	public static final double doubleNegMin = -2.2250738585072014E-308; // Smallest positive double value
	public static final double doublePosMax = 1.7976931348623157E+308; // Largest positive double value
	public static final double doublePosMin = 2.2250738585072014E-308; // Smallest positive double value

	public enum FPDataType {
		// Value (for persisteance), Bitsize of type, Number of internal precision decimal digits, Default displayed precision

		FLOAT(10, 32, 7, 8), // C/C++ single-precision "float"
		DOUBLE(20, 64, 15, 8), // C/C++ double-precision "double"
		FLOAT_80(30, 80, 19, 16), // Extended precision
		FLOAT_96(40, 96, 0, 0), // TODO: unknown internal decimal digit precision; C/C++ extended-precision "long double"

		// Future work

		FLOAT_128(50, 128, 33, 16), // TODO: known values, but not currently implmented
		FLOAT_256(60, 256, 0, 0), // TODO: unknown internal decimal digit precision
		FLOAT_512(70, 512, 0, 0); // TODO: unknown internal decimal digit precision

		// Member variables

		private int value;
		private int bitsize;
		private int decimalPrecision;
		private int displayedPrecision;

		// Constructor

		private FPDataType(int value, int bitSize, int precisionDigits, int defaultDisplayPrecision) {
			this.value = value;
			this.bitsize = bitSize;
			this.decimalPrecision = precisionDigits;
			this.displayedPrecision = defaultDisplayPrecision;
		}

		// Getters

		public int getValue() {
			return value;
		}

		public int getBitsize() {
			return bitsize;
		}

		public int getDecimalPrecision() {
			return decimalPrecision;
		}

		public int getDisplayedPrecision() {
			return displayedPrecision;
		}

		public int getInternalPrecision() {
			return decimalPrecision;
		}

		public int getByteLength() {
			return bitsize / Byte.SIZE;
		}
	}

	// Byte ordering

	public enum Endian {
		// Value

		LITTLE(10), BIG(20);

		// Member variables

		private int value;

		// Constructor

		private Endian(int value) {
			this.value = value;
		}

		// Getters

		public int getValue() {
			return value;
		}
	}

	// Justification (latent support)

	public enum Justification {
		LEFT, RIGHT, CENTER;
	}

	// Convert raw float bits to a byte array

	public static byte[] rawFloatBitsToByteArray(int floatBits) {
		int byteCount = Integer.SIZE / Byte.SIZE;
		byte[] result = new byte[byteCount];

		for (int index = 0; index < byteCount; index++) {
			int offset = (result.length - 1 - index) * 8;
			result[index] = (byte) ((floatBits >>> offset) & BYTE_MASK);
		}

		return result;
	}

	// Convert raw double bits to a byte array

	public static byte[] rawDoubleBitsToByteArray(long doubleBits) {
		int byteCount = Long.SIZE / Byte.SIZE;
		byte[] result = new byte[byteCount];

		for (int index = 0; index < byteCount; index++) {
			int offset = (result.length - 1 - index) * 8;
			result[index] = (byte) ((doubleBits >>> offset) & BYTE_MASK);
		}

		return result;
	}

	// Return a byte array that is in reverse order of the passed-in array parameter

	public static byte[] reverseByteOrder(byte[] byteArray) {
		if (byteArray.length == 0)
			return new byte[0];

		byte tempByte = 0;
		byte[] reversedByteArray = new byte[byteArray.length];

		// Copy the array that is passed in to the array that will be returned

		System.arraycopy(byteArray, 0, reversedByteArray, 0, byteArray.length);

		// Reverse the bytes

		for (int start = 0, end = reversedByteArray.length - 1; start < end; ++start, --end) {
			tempByte = reversedByteArray[start];
			reversedByteArray[start] = reversedByteArray[end];
			reversedByteArray[end] = tempByte;
		}

		return reversedByteArray;
	}

	// Convert a representation of a float or double in a byte array to a scientific notation string (Should we use BigDecimal here???)

	public static String byteArrayToSciNotation(FPDataType dt, boolean isLittleEndian, FPMemoryByte[] memByteArray,
			int maxDisplayDigits) throws ArithmeticException {
		int displayedDigits = 8;

		// If the byte array is not a 32-bit float or 64-bit double, throw an exception.

		if (memByteArray.length != (FPDataType.FLOAT.getByteLength())
				&& memByteArray.length != (FPDataType.DOUBLE.getByteLength()))
			throw new ArithmeticException(
					"Conversion of the floating point number cannot be performed; invalid data type or byte array length."); //$NON-NLS-1$

		// Create and initialize a DecimalFormat object for scientific notation.  Specify a space
		// for the preceding plus-sign, which lines up the first significant digit, decimal point
		// and exponent character.  Define the symbol strings for "Not a Number" and "Infinity."

		DecimalFormat df = new DecimalFormat("0.0E0"); //$NON-NLS-1$
		df.setPositivePrefix(" "); //$NON-NLS-1$

		DecimalFormatSymbols dfSymbols = new DecimalFormatSymbols();
		dfSymbols.setNaN(" " + FPRenderingMessages.getString("FPRendering.NAN")); //$NON-NLS-1$ //$NON-NLS-2$
		dfSymbols.setInfinity(FPRenderingMessages.getString("FPRendering.INFINITY")); //$NON-NLS-1$
		df.setDecimalFormatSymbols(dfSymbols);

		// Set the integer and fraction digits for normalized scientific notation.

		df.setMinimumIntegerDigits(1);
		df.setMaximumIntegerDigits(1);

		if (dt == FPDataType.FLOAT)
			displayedDigits = Math.min(maxDisplayDigits, FPDataType.FLOAT.getInternalPrecision());

		if (dt == FPDataType.DOUBLE)
			displayedDigits = Math.min(maxDisplayDigits, FPDataType.DOUBLE.getInternalPrecision());

		df.setMinimumFractionDigits(displayedDigits - 1);
		df.setMaximumFractionDigits(displayedDigits - 1);

		// Convert the byte array to a scientific notation floating point number string (only floats and doubles currently supported)

		ByteOrder byteOrder = isLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

		return df.format(dt == FPDataType.FLOAT
				? ByteBuffer.wrap(memoryBytesToByteArray(memByteArray)).order(byteOrder).getFloat()
				: ByteBuffer.wrap(memoryBytesToByteArray(memByteArray)).order(byteOrder).getDouble());
	}

	// Convert a floating point string to a byte array (*** only 'floats' and 'doubles' currently supported ***)

	public static byte[] floatingStringToByteArray(FPDataType dt, String valueString, int dataTypeBitCount)
			throws NumberFormatException {
		// Remove whitespace and check for non-zero length
		valueString = valueString.trim().replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$

		if (valueString.length() != 0) {
			// Float handling

			if (dt == FPDataType.FLOAT || FPDataType.FLOAT.getBitsize() == dataTypeBitCount) {
				// Convert the string to a float.  Check the range.  Convert to byte array.

				float floatValue = new Float(valueString).floatValue();
				floatValue = floatLimitCheck(floatValue);
				return rawFloatBitsToByteArray(Float.floatToRawIntBits(floatValue));
			}

			// Double handling

			if (dt == FPDataType.DOUBLE || FPDataType.DOUBLE.getBitsize() == dataTypeBitCount) {
				// Convert the string to a double.  Check the range.  Convert to byte array.

				double doubleValue = new Double(valueString).doubleValue();
				doubleValue = doubleLimitCheck(doubleValue);
				return rawDoubleBitsToByteArray(Double.doubleToRawLongBits(doubleValue));
			}
		}

		return new byte[0];
	}

	// Convert from an FPMemoryByte array to a byte array

	public static byte[] memoryBytesToByteArray(FPMemoryByte[] memoryByteArray) {
		byte[] byteArray = new byte[memoryByteArray.length];

		for (int index = 0; index < memoryByteArray.length; index++)
			byteArray[index] = memoryByteArray[index].getValue();

		return byteArray;
	}

	// Convert from a byte array to a MemoryByte array

	public static FPMemoryByte[] byteArrayToMemoryBytes(Endian endian, byte[] byteArray) {
		FPMemoryByte[] memoryBytes = new FPMemoryByte[byteArray.length];

		for (int index = 0; index < byteArray.length; index++) {
			memoryBytes[index] = new FPMemoryByte();
			memoryBytes[index].setBigEndian(endian == Endian.BIG);
			memoryBytes[index].setValue(byteArray[index]);
		}

		return memoryBytes;
	}

	// Check the character for being valid for number entry, both standard and scientific notation

	public static boolean validEditCharacter(char character) {
		return (character >= '0' && character <= '9') || character == '+' || character == '-' || character == 'e'
				|| character == 'E' || character == '.' || character == ' ';
	}

	// Validate floating point number string

	public static boolean isValidFormat(String string) {
		// Rules:
		//  - A minimum of one digit preceding the optional exponent character is required.
		//  - Allowable characters: 0-9, a decimal point, '+' and '-' number
		//    signs, exponent characters 'e' and 'E', and spaces.
		//
		// Strings may also have:
		//      - One [optional] decimal point
		//      - A maximum of two [optional] number signs (one before the number and one after the exponent character)
		//      - Only one [optional] exponent character is allowed

		boolean digit = false;
		char[] charArray = string.toCharArray();

		// Phase I check:

		String scientificNotationPattern = "^[-+]??(\\d++[.]\\d*?|[.]?\\d+?|\\d+(?=[eE]))([eE][-+]??\\d++)?$"; //$NON-NLS-1$

		if (!Pattern.matches(scientificNotationPattern, string))
			return false;

		// Phase II check

		for (int index = 0; index < string.length(); index++) {
			// Check for a digit

			if (charArray[index] >= '0' && charArray[index] <= '9')
				digit = true;

			// Make sure it's a valid/allowable character

			if (!validEditCharacter(charArray[index]))
				return false;

			// Only one decimal point and exponent character is allowed

			if (FPutilities.countMatches(string.toLowerCase(), ".") > 1 //$NON-NLS-1$
					|| FPutilities.countMatches(string.toLowerCase(), "e") > 1) //$NON-NLS-1$
				return false;

			// Number signs are only allowed in the first position and following the exponent character.

			if (((charArray[index] == '+' || charArray[index] == '-') && index != 0)
					&& (charArray[index - 1] != 'e' && charArray[index - 1] != 'E'))
				return false;

			// Decimal points are not allowed after the exponent character

			int eIndex = string.toLowerCase().indexOf('e');

			if (charArray[index] == '.' && eIndex != -1 && eIndex < index)
				return false;
		}

		return digit;
	}

	// Return a string of the specified length filled with the specified character

	public static String fillString(int length, char character) {
		if (length < 1)
			return ""; //$NON-NLS-1$
		char[] charArray = new char[length];
		Arrays.fill(charArray, character);
		return new String(charArray);
	}

	// Count the 'subString' matches in 'string'

	public static int countMatches(String string, String subString) {
		if (string.length() == 0 || subString.length() == 0)
			return 0;

		int count = 0;
		int index = 0;

		while ((index = string.indexOf(subString, index)) != -1) {
			count++;
			index += subString.length();
		}

		return count;
	}

	// Print out a stack trace; useful for UI operations where stopping at a breakpoint causes button press context to be lost

	public static void stackTrace(int depth) {
		int offset = 3; // Ignore frames contributed to the stack based on call to this method
		if (depth == 0)
			depth = 4; // Default depth if zero supplied

		// Get the stack frames for the current thread; start at the offset

		StackTraceElement[] seArray = Thread.currentThread().getStackTrace();

		if (seArray.length > offset) {
			System.out.println("Displaying " + depth + " of " + seArray.length + " stack trace elements"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			for (int index = offset; index < Math.min(depth + offset, seArray.length + offset); index++)
				System.out.println("   " + seArray[index].getClassName() + "." + seArray[index].getMethodName() //$NON-NLS-1$//$NON-NLS-2$
						+ ": line " + seArray[index].getLineNumber()); //$NON-NLS-1$
		} else
			System.out.println("No stack frames to display"); //$NON-NLS-1$
	}

	// Pop up a message inside the UI thread

	public static void popupMessage(final String title, final String errorText, final Status status) {
		UIJob job = new UIJob("Floating Point Renderer") //$NON-NLS-1$
		{
			// Notify the user of some condition via a pop-up box.

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, errorText,
						status);
				return Status.OK_STATUS;
			}
		};

		job.setSystem(true);
		job.schedule();
	}

	// Check float range.  Returns -Infinity, the original value or +Infinity

	public static float floatLimitCheck(float floatValue) {
		if (floatValue != 0.0f && floatValue != Float.NEGATIVE_INFINITY && floatValue != Float.POSITIVE_INFINITY) {
			if (floatValue < 0) {
				if (Float.compare(floatValue, floatNegMax) < 0 || Float.compare(floatValue, floatNegMin) > 0)
					return Float.NEGATIVE_INFINITY;
			} else {
				if (Float.compare(floatValue, floatPosMin) < 0 || Float.compare(floatValue, floatPosMax) > 0)
					return Float.POSITIVE_INFINITY;
			}
		}

		return floatValue;
	}

	// Check double range.  Returns a value of RangeCheck

	public static double doubleLimitCheck(double doubleValue) {
		if (doubleValue != 0.0 && doubleValue != Double.NEGATIVE_INFINITY && doubleValue != Double.POSITIVE_INFINITY) {
			if (doubleValue < 0) {
				if (Double.compare(doubleValue, doubleNegMax) < 0 || Double.compare(doubleValue, doubleNegMin) > 0)
					return Double.NEGATIVE_INFINITY;
			} else {
				if (Double.compare(doubleValue, doublePosMin) < 0 || Double.compare(doubleValue, doublePosMax) > 0)
					return Double.POSITIVE_INFINITY;
			}
		}

		return doubleValue;
	}

	// Convert a BigInteger to a hex String and return only the ending number of specified digits.

	public static String bi2HexStr(BigInteger bi, int lastDigits) {
		final int PAD_LENGTH = 12;
		String base16 = bi.toString(16);
		base16 = fillString(PAD_LENGTH - base16.length(), '0') + base16;
		return "0x" + base16.substring(PAD_LENGTH - lastDigits).toUpperCase(); //$NON-NLS-1$
	}

	// Convert a BigInteger to a decimal String and return only the ending number of
	// specified digits.  For example:  bi2HexStr(239248506, 5) = "48506"

	public static String bi2DecStr(BigInteger bi, int lastDigits) {
		final int PAD_LENGTH = 12;
		String base10 = bi.toString();
		base10 = fillString(PAD_LENGTH - base10.length(), '0') + base10;
		return base10.substring(PAD_LENGTH - lastDigits);
	}
}
