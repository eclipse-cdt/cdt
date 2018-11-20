/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.symboltable;

/**
 * An immutable map, like you would find in a functional programming language.
 *
 * Inserting a new pair into the map leaves the original map untouched,
 * instead a new map that contains the pair is returned. Therefore
 * an assignment is needed to "modify" the map (just like with Strings).
 *
 * <code>
 * myMap = myMap.insert(key,value);
 * </code>
 *
 * There is no remove() method because it is not needed. In order to
 * "delete" a pair from the map simply save a reference to an old version
 * of the map and restore the map from that old reference. This makes
 * "undo" operations trivial to implement.
 *
 * <code>
 * FunctionalMap oldMap = myMap;     // save a reference
 * myMap = myMap.insert(key,value);  // insert the pair into the map
 * myMap = oldMap;                   // delete the pair from the map
 * </code>
 *
 * This map is implemented as a red-black tree data structure,
 * and is based on the implementation found at:
 * http://www.eecs.usma.edu/webs/people/okasaki/jfp99.ps
 *
 * @author Mike Kucera
 */
public class FunctionalMap<K extends Comparable<K>, V> {

	private static final boolean RED = true, BLACK = false;

	private static class Node<K, V> {
		final K key;
		final V val;
		Node<K, V> left;
		Node<K, V> right;
		boolean color;

		public Node(K key, V val, boolean color, Node<K, V> left, Node<K, V> right) {
			this.key = key;
			this.val = val;
			this.left = left;
			this.right = right;
			this.color = color;
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "Node(" + key + "," + val + "," + (color ? "R" : "B") + ")";
		}
	}

	private Node<K, V> root = null;

	private FunctionalMap() {
		// private constructor, use static factory method to instantiate
	}

	// factory method makes it cleaner to instantiate objects
	public static <K extends Comparable<K>, V> FunctionalMap<K, V> emptyMap() {
		return new FunctionalMap<>();
	}

	/**
	 * Returns a new map that contains the key-value pair.
	 * @throws NullPointerException if key is null
	 */
	public FunctionalMap<K, V> insert(K key, V val) {
		if (key == null)
			throw new NullPointerException();

		FunctionalMap<K, V> newMap = new FunctionalMap<>();
		newMap.root = insert(this.root, key, val);
		newMap.root.color = BLACK; // force the root to be black

		assert checkInvariants(newMap.root);

		return newMap;
	}

	private Node<K, V> insert(Node<K, V> n, K key, V val) {
		if (n == null)
			return new Node<>(key, val, RED, null, null); // new nodes are always red

		int c = key.compareTo(n.key);
		if (c < 0)
			return balance(n.key, n.val, n.color, insert(n.left, key, val), n.right);
		else if (c > 0)
			return balance(n.key, n.val, n.color, n.left, insert(n.right, key, val));
		else // equal, create a new node that overwrites the old value
			return new Node<>(key, val, n.color, n.left, n.right);
	}

	private Node<K, V> balance(K key, V val, boolean color, Node<K, V> left, Node<K, V> right) {
		if (color == RED)
			return new Node<>(key, val, color, left, right);

		final Node<K, V> newLeft, newRight;

		// now for the madness...

		if (left != null && left.color == RED) {
			if (left.left != null && left.left.color == RED) {
				newLeft = new Node<>(left.left.key, left.left.val, BLACK, left.left.left, left.left.right);
				newRight = new Node<>(key, val, BLACK, left.right, right);
				return new Node<>(left.key, left.val, RED, newLeft, newRight);
			}
			if (left.right != null && left.right.color == RED) {
				newLeft = new Node<>(left.key, left.val, BLACK, left.left, left.right.left);
				newRight = new Node<>(key, val, BLACK, left.right.right, right);
				return new Node<>(left.right.key, left.right.val, RED, newLeft, newRight);
			}
		}
		if (right != null && right.color == RED) {
			if (right.left != null && right.left.color == RED) {
				newLeft = new Node<>(key, val, BLACK, left, right.left.left);
				newRight = new Node<>(right.key, right.val, BLACK, right.left.right, right.right);
				return new Node<>(right.left.key, right.left.val, RED, newLeft, newRight);
			}
			if (right.right != null && right.right.color == RED) {
				newLeft = new Node<>(key, val, BLACK, left, right.left);
				newRight = new Node<>(right.right.key, right.right.val, BLACK, right.right.left, right.right.right);
				return new Node<>(right.key, right.val, RED, newLeft, newRight);
			}
		}

		return new Node<>(key, val, BLACK, left, right);
	}

	/**
	 * Returns the value if it is in the map, null otherwise.
	 * @throws NullPointerException if key is null
	 */
	public V lookup(K key) {
		if (key == null)
			throw new NullPointerException();

		// no need for recursion here
		Node<K, V> n = root;
		while (n != null) {
			int x = key.compareTo(n.key); // throws NPE if key is null
			if (x == 0)
				return n.val;
			n = (x < 0) ? n.left : n.right;
		}
		return null;
	}

	/**
	 * Returns true if there exists a mapping with the given key
	 * in this map.
	 * @throws NullPointerException if key is null
	 */
	public boolean containsKey(K key) {
		if (key == null)
			throw new NullPointerException();

		// lookup uses an iterative algorithm
		Node<K, V> n = root;
		while (n != null) {
			int x = key.compareTo(n.key); // throws NPE if key is null
			if (x == 0)
				return true;
			n = (x < 0) ? n.left : n.right;
		}
		return false;
	}

	public boolean isEmpty() {
		return root == null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder('[');
		inorderPrint(root, sb);
		sb.append(']');
		return sb.toString();
	}

	private static <K, V> void inorderPrint(Node<K, V> n, StringBuilder sb) {
		if (n == null)
			return;

		inorderPrint(n.left, sb);
		if (sb.length() > 1)
			sb.append(", ");//$NON-NLS-1$
		sb.append(n.toString());
		inorderPrint(n.right, sb);
	}

	void printStructure() {
		if (root == null)
			System.out.println("empty map"); //$NON-NLS-1$
		else
			printStructure(root, 0);
	}

	private static <K, V> void printStructure(Node<K, V> node, int level) {
		for (int i = 0; i < level; i++)
			System.out.print("--");//$NON-NLS-1$

		if (node == null) {
			System.out.println("null");//$NON-NLS-1$
		} else if (node.right == null && node.left == null) {
			System.out.println(node);
		} else {
			System.out.println(node);
			printStructure(node.right, level + 1);
			printStructure(node.left, level + 1);
		}
	}

	private static <K, V> int depth(Node<K, V> node) {
		if (node == null)
			return 0;
		return Math.max(depth(node.left), depth(node.right)) + 1;
	}

	/**
	 * Warning, this is a linear operation.
	 */
	public int size() {
		return size(root);
	}

	private static <K, V> int size(Node<K, V> node) {
		if (node == null)
			return 0;
		return size(node.left) + size(node.right) + 1;
	}

	/**********************************************************************************************
	 * Built-in testing
	 **********************************************************************************************/

	private boolean checkInvariants(Node<K, V> n) {
		// the number of black nodes on every path through the tree is the same
		assertBalanced(n);
		return true;
	}

	// not exactly sure if this is right
	private int assertBalanced(Node<K, V> node) {
		if (node == null)
			return 1; // nulls are considered as black children

		// both children of every red node are black
		if (node.color == RED) {
			assert node.left == null || node.left.color == BLACK;
			assert node.right == null || node.right.color == BLACK;
		}

		int left = assertBalanced(node.left);
		int right = assertBalanced(node.right);

		assert left == right;

		return left + (node.color == BLACK ? 1 : 0);
	}

}
