/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.Assert;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ArrayUtil {
	private static final int DEFAULT_LENGTH = 2;

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 * Appends element after the last non-{@code null} element.
	 * If the array is {@code null} or not large enough, a larger one is allocated, using
	 * the given class object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] append(Class<T> c, T[] array, T obj) {
		if (obj == null)
			return array;
		if (array == null || array.length == 0) {
			array = (T[]) Array.newInstance(c, DEFAULT_LENGTH);
			array[0] = obj;
			return array;
		}

		int i = findFirstNull(array);
		if (i >= 0) {
			array[i] = obj;
			return array;
		}

		T[] temp = (T[]) Array.newInstance(c, Math.max(array.length * 2, DEFAULT_LENGTH));
		System.arraycopy(array, 0, temp, 0, array.length);
		temp[array.length] = obj;
		return temp;
	}

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 * Appends element after the last non-{@code null} element.
	 * If the array is not large enough, a larger one is allocated.
	 * Null <code>array</code> is supported for backward compatibility only and only when T is
	 * Object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] append(T[] array, T obj) {
		if (obj == null)
			return array;
		if (array == null || array.length == 0) {
			Class<? extends Object> c = array != null ? array.getClass().getComponentType() : Object.class;
			array = (T[]) Array.newInstance(c, DEFAULT_LENGTH);
			array[0] = obj;
			return array;
		}

		int i = findFirstNull(array);
		if (i >= 0) {
			array[i] = obj;
			return array;
		}

		T[] temp = (T[]) Array.newInstance(array.getClass().getComponentType(),
				Math.max(array.length * 2, DEFAULT_LENGTH));
		System.arraycopy(array, 0, temp, 0, array.length);
		temp[array.length] = obj;
		return temp;
	}

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 *
	 * @return index of first {@code null}, or -1
	 */
	private static int findFirstNull(Object[] array) {
		int low = 0;
		int high = array.length;
		while (low < high) {
			int mid = (low + high) >>> 1;
			if (array[mid] == null) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return high < array.length ? high : -1;
	}

	/**
	 * @deprecated Use {@link #appendAt(Class, Object[], int, Object)} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 4.0
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static Object[] append(Class<?> c, Object[] array, int currentLength, Object obj) {
		return appendAt((Class<Object>) c, array, currentLength, obj);
	}

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 * Appends object using the current length of the array.
	 * @since 5.1
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] appendAt(Class<T> c, T[] array, int currentLength, T obj) {
		if (obj == null)
			return array;
		if (array == null || array.length == 0) {
			array = (T[]) Array.newInstance(c, DEFAULT_LENGTH);
			array[0] = obj;
			return array;
		}

		if (currentLength < array.length) {
			Assert.isTrue(array[currentLength] == null);
			Assert.isTrue(currentLength == 0 || array[currentLength - 1] != null);
			array[currentLength] = obj;
			return array;
		}

		T[] temp = (T[]) Array.newInstance(c, array.length * 2);
		System.arraycopy(array, 0, temp, 0, array.length);
		temp[array.length] = obj;
		return temp;
	}

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 * Appends object using the current length of the array.
	 * @param array The array to append to. Not {@code null}
	 * @param currentLength The number of non-{@code null} elements in the array
	 * @param obj The object to append. Not {@code null}
	 * @return The modified array, which may be the same as the first parameter.
	 * @since 5.4
	 */
	public static <T> T[] appendAt(T[] array, int currentLength, T obj) {
		if (obj == null)
			return array;
		if (currentLength >= array.length) {
			array = Arrays.copyOf(array, Math.max(Math.max(currentLength + 1, array.length * 2), DEFAULT_LENGTH));
		}
		Assert.isTrue(array[currentLength] == null);
		Assert.isTrue(currentLength == 0 || array[currentLength - 1] != null);
		array[currentLength] = obj;
		return array;
	}

	/**
	 * Trims the given array and returns a new array with no {@code null} entries.
	 * Assumes that {@code null}s can be found at the end, only.
	 * if {@code array} is {@code null}, a new array of length 0 is returned
	 * if {@code forceNew} is {@code true}, a new array will always be created.
	 * if {@code forceNew} is {@code false}, a new array will only be created if the original array
	 * contained {@code null} entries.
	 *
	 * @param c the type of the new array
	 * @param array the array to be trimmed
	 * @param forceNew
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] trim(Class<T> c, T[] array, boolean forceNew) {
		if (array == null)
			return (T[]) Array.newInstance(c, 0);

		int i = array.length;
		if (i == 0 || array[i - 1] != null) {
			if (!forceNew) {
				return array;
			}
		} else {
			i = findFirstNull(array);
			Assert.isTrue(i >= 0);
		}

		T[] temp = (T[]) Array.newInstance(c, i);
		System.arraycopy(array, 0, temp, 0, i);
		return temp;
	}

	public static <T> T[] trim(Class<T> c, T[] array) {
		return trim(c, array, false);
	}

	/**
	 * Trims the given array and returns a new array with no {@code null} entries.
	 * Assumes that {@code null}s can be found at the end, only.
	 * if {@code forceNew} is {@code true}, a new array will always be created.
	 * if {@code forceNew} is {@code false}, a new array will only be created if the original array
	 * contained {@code null} entries.
	 *
	 * @param array the array to be trimmed
	 * @param forceNew
	 * @since 5.2
	 */
	public static <T> T[] trim(T[] array, boolean forceNew) {
		int i = array.length;
		if (i == 0 || array[i - 1] != null) {
			if (!forceNew) {
				return array;
			}
		} else {
			i = findFirstNull(array);
			Assert.isTrue(i >= 0);
		}

		return Arrays.copyOf(array, i);
	}

	/**
	 * Trims the given array and returns a new array with no {@code null} entries.
	 * Assumes that {@code null}s can be found at the end, only.
	 *
	 * @param array the array to be trimmed
	 * @since 5.2
	 */
	public static <T> T[] trim(T[] array) {
		return trim(array, false);
	}

	/**
	 * Trims the given array and returns a new array with no {@code null} entries.
	 * Assumes that {@code null}s can be found at the end, only.
	 * Similar to {@link #trimAt(Class, Object[], int)}, but uses the new length instead of index.
	 *
	 * @param array the array to be trimmed
	 * @param newLength the new length of the array, has to be less or equal than
	 *     the current length.
	 * @return the modified array, which may be the same as the first parameter.
	 * @since 5.4
	 */
	public static <T> T[] trim(T[] array, int newLength) {
		if (newLength == array.length)
			return array;
		Assert.isTrue(array[newLength] == null);
		Assert.isTrue(newLength == 0 || array[newLength - 1] != null);
		return Arrays.copyOf(array, newLength);
	}

	/**
	 * Takes contents of the two arrays up to the first {@code null} element and concatenates
	 * them.
	 * @param c The type of the element of the returned array if there was not enough free space
	 *     in the destination array.
	 * @param dest The destination array. The elements of the source array are added to this array
	 *     if there is enough free space in it. May be {@code null}.
	 * @param source The source array. May not be {@code null}.
	 * @return The concatenated array, which may be the same as the first parameter.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addAll(Class<T> c, T[] dest, Object[] source) {
		if (source == null || source.length == 0)
			return dest;

		int numToAdd = findFirstNull(source);
		if (numToAdd == 0) {
			return dest;
		}
		if (numToAdd < 0) {
			numToAdd = source.length;
		}

		if (dest == null || dest.length == 0) {
			dest = (T[]) Array.newInstance(c, numToAdd);
			System.arraycopy(source, 0, dest, 0, numToAdd);
			return dest;
		}

		int firstFree = findFirstNull(dest);
		if (firstFree < 0) {
			firstFree = dest.length;
		}

		if (firstFree + numToAdd <= dest.length) {
			System.arraycopy(source, 0, dest, firstFree, numToAdd);
			return dest;
		}
		T[] oldDest = dest;
		dest = (T[]) Array.newInstance(c, firstFree + numToAdd);
		System.arraycopy(oldDest, 0, dest, 0, firstFree);
		System.arraycopy(source, 0, dest, firstFree, numToAdd);
		return dest;
	}

	/**
	 * Takes contents of the two arrays up to the first {@code null} element and concatenates them.
	 *
	 * @param dest The destination array. The elements of the source array are added to this array
	 *     if there is enough free space in it. May be {@code null}.
	 * @param source The source array. May not be {@code null}.
	 * @return The concatenated array, which may be the same as the first parameter.
	 * @since 5.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addAll(T[] dest, Object[] source) {
		if (source == null || source.length == 0)
			return dest;

		int numToAdd = findFirstNull(source);
		if (numToAdd <= 0) {
			if (numToAdd == 0) {
				return dest;
			}
			numToAdd = source.length;
		}

		if (dest == null || dest.length == 0) {
			Class<? extends Object> c = dest != null ? dest.getClass().getComponentType()
					: source.getClass().getComponentType();
			dest = (T[]) Array.newInstance(c, numToAdd);
			System.arraycopy(source, 0, dest, 0, numToAdd);
			return dest;
		}

		int firstFree = findFirstNull(dest);
		if (firstFree < 0) {
			firstFree = dest.length;
		}

		if (firstFree + numToAdd <= dest.length) {
			System.arraycopy(source, 0, dest, firstFree, numToAdd);
			return dest;
		}
		dest = Arrays.copyOf(dest, firstFree + numToAdd);
		System.arraycopy(source, 0, dest, firstFree, numToAdd);
		return dest;
	}

	/**
	 * Adds all elements of an array to a collection. For an {@link ArrayList} this method is
	 * slightly more efficient than {@link java.util.Collections#addAll(Collection, T...)}.
	 * @since 5.4
	 */
	@SafeVarargs
	public static <T> void addAll(Collection<? super T> collection, T... elements) {
		if (collection instanceof ArrayList) {
			((ArrayList<?>) collection).ensureCapacity(collection.size() + elements.length);
		}
		for (T element : elements) {
			collection.add(element);
		}
	}

	/**
	 * Returns whether the specified array contains the specified object. The comparison is by
	 * object identity.
	 *
	 * @param array the array to search
	 * @param obj the object to search for
	 * @return <code>true</code> if the specified array contains the specified object, or
	 *     the specified array is {@code null}
	 */
	public static <T> boolean contains(T[] array, T obj) {
		return indexOf(array, obj) >= 0;
	}

	/**
	 * Returns the index into the specified array of the specified object, or -1 if the array does
	 * not contain the object, or if the array is {@code null}.  Comparison is by object identity.
	 *
	 * @param array the array to search
	 * @param obj the object to search for
	 * @return the index into the specified array of the specified object, or -1 if the array does
	 *     not contain the object, or if the array is {@code null}
	 */
	public static <T> int indexOf(T[] array, T obj) {
		int result = -1;
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == obj)
					return i;
			}
		}
		return result;
	}

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 * Returns whether the specified array contains the specified object. Comparison is by
	 * object identity.
	 *
	 * @param array the array to search
	 * @param obj the object to search for
	 * @return true if the specified array contains the specified object, or the specified array is
	 *     {@code null}
	 */
	public static <T> boolean containsEqual(T[] array, T obj) {
		return indexOfEqual(array, obj) != -1;
	}

	/**
	 * Assumes that array contains {@code null}s at the end, only.
	 * Returns the index into the specified array of the specified object, or -1 if the array does
	 * not contain the object, or if the array is {@code null}.  Comparison is by equals().
	 *
	 * @param array the array to search
	 * @param obj the object to search for
	 * @return the index into the specified array of the specified object, or -1 if the array does
	 *     not contain an equal object, or if the array is {@code null}
	 */
	public static <T> int indexOfEqual(T[] array, T obj) {
		int result = -1;
		if (array != null) {
			for (int i = 0; (i < array.length) && (array[i] != null); i++) {
				if (array[i].equals(obj))
					return i;
			}
		}
		return result;
	}

	/**
	 * Moves all {@code null} elements to the end of the array. The order of non-{@code null}
	 * elements is preserved.
	 *
	 * @since 5.4
	 */
	public static void compact(Object[] array) {
		int j = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				if (j != i) {
					array[j] = array[i];
					array[i] = null;
				}
				j++;
			}
		}
	}

	/**
	 * Removes all of the {@code null}s from the array and returns a new array that contains all
	 * of the non-{@code null} elements.
	 *
	 * If there are no {@code null}s in the original array then the original array is returned.

	 * Note that this method should only be used when the placement of {@code null}s within
	 * the array is unknown (due to performance efficiency).
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeNulls(Class<T> c, T[] array) {
		if (array == null)
			return (T[]) Array.newInstance(c, 0);

		int i;
		int validEntries = 0;
		for (i = 0; i < array.length; i++) {
			if (array[i] != null)
				validEntries++;
		}

		if (array.length == validEntries)
			return array;

		T[] newArray = (T[]) Array.newInstance(c, validEntries);
		int j = 0;
		for (i = 0; i < array.length; i++) {
			if (array[i] != null)
				newArray[j++] = array[i];
		}

		return newArray;
	}

	/**
	 * Removes all of the {@code null}s from the array and returns a new array that contains all
	 * of the non-{@code null} elements.
	 * <p>
	 * If there are no {@code null}s in the original array then the original array is returned.
	 * <p>
	 * Note that this method should only be used when the placement of {@code null}s within
	 * the array is unknown (due to performance efficiency).
	 * @since 5.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeNulls(T[] array) {
		int validEntries = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null)
				validEntries++;
		}

		if (array.length == validEntries)
			return array;

		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), validEntries);
		int j = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null)
				newArray[j++] = array[i];
		}

		return newArray;
	}

	/**
	 * @deprecated Use {@link #trim(Object[], int)} or {@link #trimAt(Class, Object[], int)} instead
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static Object[] removeNullsAfter(Class<?> c, Object[] array, int index) {
		return trimAt((Class<Object>) c, array, index);
	}

	/**
	 * To improve performance, this method should be used instead of
	 * {@link #removeNulls(Class, Object[])} when all of the non-{@code null} elements in
	 * the array are grouped together at the beginning of the array and all of the s are at
	 * the end of the array. The position of the last non-{@code null} element in the array must
	 * also be known.
	 * <p>
	 * If you don't intend to pass {@code null} array, consider using {@link #trim(Object[], int)}
	 * instead.
	 *
	 * @since 5.1
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] trimAt(Class<T> c, T[] array, int index) {
		final int newLen = index + 1;
		if (array != null && array.length == newLen)
			return array;

		T[] newArray = (T[]) Array.newInstance(c, newLen);
		if (array != null && newLen > 0)
			System.arraycopy(array, 0, newArray, 0, newLen);
		return newArray;
	}

	/**
	 * Inserts the {@code obj} at the beginning of the array, shifting the whole thing one index
	 * Assumes that array contains {@code null}s at the end, only.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] prepend(Class<T> c, T[] array, T obj) {
		if (obj == null)
			return array;
		if (array == null || array.length == 0) {
			array = (T[]) Array.newInstance(c, DEFAULT_LENGTH);
			array[0] = obj;
			return array;
		}

		int i = findFirstNull(array);
		if (i >= 0) {
			System.arraycopy(array, 0, array, 1, i);
			array[0] = obj;
		} else {
			T[] temp = (T[]) Array.newInstance(c, array.length * 2);
			System.arraycopy(array, 0, temp, 1, array.length);
			temp[0] = obj;
			array = temp;
		}

		return array;
	}

	/**
	 * Inserts the {@code obj} at the beginning of the array, shifting the whole thing one index
	 * Assumes that array contains {@code null}s at the end, only.
	 * array must not be {@code null}.
	 * @since 5.2
	 */
	public static <T> T[] prepend(T[] array, T obj) {
		Assert.isNotNull(array);

		if (obj == null)
			return array;
		if (array.length == 0) {
			array = newArray(array, DEFAULT_LENGTH);
			array[0] = obj;
			return array;
		}

		int i = findFirstNull(array);
		if (i >= 0) {
			System.arraycopy(array, 0, array, 1, i);
			array[0] = obj;
		} else {
			T[] temp = newArray(array, array.length * 2);
			System.arraycopy(array, 0, temp, 1, array.length);
			temp[0] = obj;
			array = temp;
		}

		return array;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(T[] array, int newLen) {
		return (T[]) Array.newInstance(array.getClass().getComponentType(), newLen);
	}

	/**
	 * Removes first occurrence of element in array and moves objects behind up front.
	 * @since 4.0
	 */
	public static <T> void remove(T[] array, T element) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (element == array[i]) {
					System.arraycopy(array, i + 1, array, i, array.length - i - 1);
					array[array.length - 1] = null;
					return;
				}
			}
		}
	}

	/**
	 * Stores the specified array contents in a new array of specified runtime type.
	 *
	 * @param target the runtime type of the new array
	 * @param source the source array
	 * @return the current array stored in a new array with the specified runtime type,
	 *     or {@code null} if source is {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public static <S, T> T[] convert(Class<T> target, S[] source) {
		T[] result = null;
		if (source != null) {
			result = (T[]) Array.newInstance(target, source.length);
			for (int i = 0; i < source.length; i++) {
				result[i] = (T) source[i];
			}
		}
		return result;
	}

	/**
	 * Reverses order of elements in an array.
	 * @param array the array
	 * @since 5.4
	 */
	public static void reverse(Object[] array) {
		reverse(array, 0, array.length);
	}

	/**
	 * Reverses order of elements in a subsection of an array.
	 *
	 * @param array the array
	 * @param fromIndex the index of the first affected element (inclusive)
	 * @param toIndex the index of the last affected element (exclusive)
	 * @since 5.4
	 */
	public static void reverse(Object[] array, int fromIndex, int toIndex) {
		for (int i = fromIndex, j = toIndex; i < --j; i++) {
			Object tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	/**
	 * Returns a new array that contains all of the elements of the given array except
	 * the first one.
	 *
	 * @throws NullPointerException if {@code array} is {@code null}
	 * @throws IllegalArgumentException if {@code array} is empty
	 * @since 5.1
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeFirst(T[] array) {
		int n = array.length;
		if (n <= 0)
			throw new IllegalArgumentException();

		T[] newArgs = (T[]) Array.newInstance(array.getClass().getComponentType(), n - 1);
		for (int i = 1; i < n; i++) {
			newArgs[i - 1] = array[i];
		}
		return newArgs;
	}

	/**
	 * Returns a new array that contains elements of the given array except duplicates and
	 * {@code null}s. Duplicates are determined by {@link Object#equals(Object)} method.
	 *
	 * @throws NullPointerException if {@code array} is {@code null}
	 * @since 5.5
	 */
	public static <T> T[] removeDuplicates(T[] array) {
		int k = 0;
		if (array.length >= 16) {
			HashSet<T> set = new HashSet<>(array.length);
			for (int i = 0; i < array.length; i++) {
				T obj = array[i];
				if (obj != null && set.add(obj)) {
					array[k++] = obj;
				}
			}
		} else {
			for (int i = 0; i < array.length; i++) {
				T obj = array[i];
				if (obj != null) {
					array[k++] = obj;
					for (int j = i + 1; j < array.length; j++) {
						if (obj.equals(array[j]))
							array[j] = null;
					}
				}
			}
		}
		if (k == array.length)
			return array;
		return Arrays.copyOf(array, k);
	}

	public static int[] setInt(int[] array, int idx, int val) {
		if (array == null) {
			array = new int[DEFAULT_LENGTH > idx + 1 ? DEFAULT_LENGTH : idx + 1];
			array[idx] = val;
			return array;
		}

		if (array.length <= idx) {
			int newLen = array.length * 2;
			while (newLen <= idx)
				newLen *= 2;
			int[] temp = new int[newLen];
			System.arraycopy(array, 0, temp, 0, array.length);

			array = temp;
		}
		array[idx] = val;
		return array;
	}

	/**
	 * Filter the elements of an array down to just the ones
	 * that match the given predicate.
	 * @since 5.6
	 */
	public static <T> T[] filter(T[] array, IUnaryPredicate<T> predicate) {
		T[] result = array;
		int resultIndex = 0;
		for (int i = 0; i < array.length; ++i) {
			if (predicate.apply(array[i])) {
				if (result != array) {
					result[resultIndex] = array[i];
				}
				++resultIndex;
			} else {
				if (result == array) {
					// There will be at most array.length - 1 filtered elements.
					result = Arrays.copyOf(array, array.length - 1);
					Arrays.fill(result, i, result.length, null);
				}
			}
		}
		return resultIndex == result.length ? result : Arrays.copyOf(result, resultIndex);
	}
}
