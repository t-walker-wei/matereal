package com.phybots.utils;

import java.util.Comparator;
import java.util.Random;
import java.util.Stack;

/**
 * A top-down splay tree based on Danny Sleator's implementation.
 *
 * @param <T> Type of elements of the tree.
 * @see {@link ftp://ftp.cs.cmu.edu/user/sleator/splaying/SplayTree.java}
 */
public class SplayTree<T> implements Cloneable {
	private BinaryNode<T> root;
	private Comparator<T> comparator;

	public SplayTree(Comparator<T> comparator) {
		this(null, comparator);
	}

	public SplayTree(SplayTree<T> splayTree) {
		this(splayTree.root, splayTree.comparator);
	}

	public SplayTree(BinaryNode<T> root, Comparator<T> comparator) {
		this.root = root;
		this.comparator = comparator;
	}

	/**
	 * Insert into the tree.
	 *
	 * @param x
	 *            the item to insert.
	 * @return
	 *            false if x is already present, otherwise true.
	 */
	public boolean insert(T x) {
		if (root == null) {
			root = new BinaryNode<T>(x);
			return true;
		}
		splay(x);

		int c;
		if ((c = comparator.compare(x, root.key)) == 0) {
			return false;
		}

		BinaryNode<T> n = new BinaryNode<T>(x);
		n.size = size(root) + 1;
		if (c < 0) {
			n.left = root.left;
			n.right = root;
			root.size -= size(root.left);
			root.left = null;
		} else {
			n.right = root.right;
			n.left = root;
			root.size -= size(root.right);
			root.right = null;
		}
		root = n;
		return true;
	}

	/**
	 * Remove from the tree.
	 *
	 * @param x
	 *            the item to remove.
	 * @throws ItemNotFoundException
	 *             if x is not found.
	 */
	public void remove(T x) {
		splay(x);

		if (comparator.compare(x, root.key) != 0) {
			// throw new ItemNotFoundException(x.toString());
			return;
		}

		// Now delete the root
		if (root.left == null) {
			root = root.right;
		} else {
			BinaryNode<T> n = root.right;
			root = root.left;
			splay(x);
			root.right = n;
			root.size += size(n);
		}
	}

	/**
	 * Find the smallest item in the tree.
	 */
	public T findMin() {
		if (root == null) {
			return null;
		}
		BinaryNode<T> n = root;
		while (n.left != null) {
			n = n.left;
		}
		splay(n.key);
		return n.key;
	}

	/**
	 * Find the largest item in the tree.
	 */
	public T findMax() {
		if (root == null) {
			return null;
		}
		BinaryNode<T> n = root;
		while (n.right != null) {
			n = n.right;
		}
		splay(n.key);
		return n.key;
	}

	/**
	 * Find an item in the tree.
	 *
	 * @param x
	 *            the item to find.
	 * @return the item if found, otherwise null.
	 */
	public T find(T x) {
		if (root == null) {
			return null;
		}
		splay(x);
		if (comparator.compare(root.key, x) != 0) {
			return null;
		}
		return root.key;
	}

	/**
	 * Split the tree with the specified index.
	 *
	 * @param index
	 *            index of the separator item.
	 * @return  a new splay tree with items of the values greater than the item of the specified index.
	 */
	public SplayTree<T> split(int index) {
		return splitByValue(get(index));
	}

	/**
	 * Split the tree with the specified value.
	 *
	 * @param x
	 *            the separator item.
	 * @return a new splay tree with items of greater values.
	 */
	public SplayTree<T> splitByValue(T x) {
		if (root == null) {
			return null;
		}
		splay(x);
		SplayTree<T> splayTree = new SplayTree<T>(root.right, comparator);
		root.size -= size(root.right);
		root.right = null;
		return splayTree;
	}

	/**
	 * Find an item which has the specified index.
	 *
	 * @param index
	 *            index of the item to find.
	 * @return the item if found, otherwise null.
	 */
	public T get(int index) {
		BinaryNode<T> n = root;
		while (true) {
			if (n == null) {
				return null;
			}
			int size = size(n.left);
			if (size > index) {
				n = n.left;
				continue;
			} else if (size < index) {
				n = n.right;
				index -= size + 1;
				continue;
			} else {
				return n.key;
			}
		}
	}

	/**
	 * Merge two splay trees.
	 *
	 * @param splayTree
	 *            a splay tree to be merged.
	 * @return whether the operation is succeeded or not.
	 */
	public boolean join(SplayTree<T> splayTree) {
		if (splayTree == null || splayTree.root == null) {
			return false;
		}
		if (root == null) {
			root = splayTree.root;
			return true;
		}

		splay(splayTree.root.key);

		if (root.left == null) {
			root.size += splayTree.size();
			root.left = splayTree.root;
			return true;
		}

		if (root.right == null) {
			root.size += splayTree.size();
			root.right = splayTree.root;
			return true;
		}

		/* root.left != null && root.right != null */
		return false;
	}

	/**
	 * Test if the tree is logically empty.
	 *
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return root == null;
	}

	public void clear() {
		root = null;
	}

	private int size(BinaryNode<T> node) {
		return node == null ? 0 : node.size;
	}

	/**
	 * Returns size of the tree.
	 *
	 * @return size of the tree.
	 */
	public int size() {
		return size(root);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		Stack<BinaryNode<T>> stack = new Stack<BinaryNode<T>>();
		BinaryNode<T> n = root;
		while (true) {
			while (n != null) {
				stack.push(n);
				n = n.left;
			}
			if (stack.isEmpty()) {
				break;
			}
			n = stack.pop();
			sb.append(n.key.toString());
			sb.append(" ");
			n = n.right;
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Temporary node used for splay operation.
	 */
	private BinaryNode<T> header = new BinaryNode<T>(null);

	/**
	 * Internal method to perform a top-down splay.
	 *
	 * splay(key) does the splay operation on the given key. If key is in the
	 * tree, then the BinaryNode containing that key becomes the root. If key is
	 * not in the tree, then after the splay, key.root is either the greatest
	 * key < key in the tree, or the lest key > key in the tree.
	 *
	 * This means, among other things, that if you splay with a key that's
	 * larger than any in the tree, the rightmost node of the tree becomes the
	 * root. This property is used in the delete() method.
	 */
	private void splay(T key) {
		/**
		 * Reference to the leaf which holds the maximum value in the left tree.
		 */
		BinaryNode<T> l;
		/**
		 * Reference to the leaf which holds the minimum value in the right tree.
		 */
		BinaryNode<T> r;
		BinaryNode<T> t, y;
		l = r = header;
		header.left = header.right = null;
		t = root;
		for (;;) {
			if (comparator.compare(key, t.key) < 0) {
				if (t.left == null) {
					break;
				}

				// Rotate right
				if (comparator.compare(key, t.left.key) < 0) {
					y = t.left;

					t.left = y.right;
					t.size -= 1 + size(y.left);

					y.right = t;
					y.size += 1 + size(t.right);

					t = y;

					if (t.left == null) {
						break;
					}
				}

				// Link right
				t.size -= size(t.left);
				int diff = size(t);
				BinaryNode<T> n = header;
				while ((n = n.left) != r && n != null) {
					n.size += diff;
				}
				r.size += diff;
				r.left = t;
				r = t;
				// r.left will be overwritten in the next call of "Link right"
				t = t.left;
			} else if (comparator.compare(key, t.key) > 0) {
				if (t.right == null) {
					break;
				}

				// Rotate left
				if (comparator.compare(key, t.right.key) > 0) {
					y = t.right;

					t.right = y.left;
					t.size -= 1 + size(y.right);

					y.left = t;
					y.size += 1 + size(t.left);

					t = y;
					if (t.right == null) {
						break;
					}
				}

				// Link left
				t.size -= size(t.right);
				int diff = size(t);
				BinaryNode<T> n = header;
				while ((n = n.right) != l && n != null) {
					n.size += diff;
				}
				l.size += diff;
				l.right = t;
				l = t;
				// l.right will be overwritten in the next call of "Link right"
				t = t.right;
			} else {
				break;
			}
		}

		// Reassemble

		int diff = size(t.left);
		BinaryNode<T> n = header;
		while ((n = n.right) != l && n != null) {
			n.size += diff;
		}
		l.size += diff;
		l.right = t.left;

		diff = size(t.right);
		n = header;
		while ((n = n.left) != r && n != null) {
			n.size += diff;
		}
		r.size += diff;
		r.left = t.right;

		t.left = header.right;
		t.right = header.left;
		t.size = size(header.left) + size(header.right) + 1;
		root = t;
	}

	@Override
	public SplayTree<T> clone() {
		try {
			@SuppressWarnings("unchecked")
			SplayTree<T> splayTree = (SplayTree<T>) super.clone();
			if (root == null) {
				return splayTree;
			}

			BinaryNode<T> newNode = new BinaryNode<T>();
			BinaryNode<T> n;
			splayTree.root = newNode;

			Stack<BinaryNode<T>> newStack = new Stack<BinaryNode<T>>();
			Stack<BinaryNode<T>> stack = new Stack<BinaryNode<T>>();
			newStack.push(newNode);
			stack.push(root);

			while (!stack.isEmpty()) {
				n = stack.pop();
				newNode = newStack.pop();
				newNode.key = n.key;

				if (n.left != null) {
					newNode.left = new BinaryNode<T>();
					newStack.push(newNode.left);
					stack.push(n.left);
				}

				if (n.right != null) {
					newNode.right = new BinaryNode<T>();
					newStack.push(newNode.right);
					stack.push(n.right);
				}
			}
			return splayTree;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public static void main(String[] args) {
		SplayTree<Integer> t = new SplayTree<Integer>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		test1(t);
		t.clear();
		test2(t);
	}

	/**
	 * Test code stolen from Weiss.
	 *
	 * @param t a splay tree to be tested.
	 */
	private static void test1(SplayTree<Integer> t) {
		final int NUMS = 20000;
		final int GAP = 307;

		System.out.println("TEST1: Checking... (no bad output means success)");

		int n = 0;
		for (int i = GAP; i != 0; i = (i + GAP) % NUMS) {
			n ++;
			t.insert(i);
		}
		System.out.println("Inserts complete");

		for (int i = 1; i < NUMS; i += 2) {
			t.remove(i);
		}
		System.out.println("Removes complete");

		if (t.findMin().intValue() != 2
				|| t.findMax().intValue() != NUMS - 2) {
			System.out.println("FindMin or FindMax error!");
		}

		for (int i = 2; i < NUMS; i += 2) {
			if (t.find(i).intValue() != i) {
				System.out.println("Error: find fails for " + i);
			}
		}

		for (int i = 1; i < NUMS; i += 2) {
			if (t.find(i) != null) {
				System.out.println("Error: Found deleted item " + i);
			}
		}
	}

	/**
	 * @param t a splay tree to be tested.
	 */
	private static void test2(SplayTree<Integer> t) {
		final int NUMS = 20;
		System.out.println("TEST2: Inserting " + NUMS + " items...");
		int num = 0;
		Random random = new Random();
		for (int i = 0; i < NUMS; i ++) {
			if (t.insert(random.nextInt(100))) {
				num ++;
			}
		}
		System.out.println("Size: " + t.size() + " (should be " + num + ")");
		System.out.println(t.toString());
		System.out.print("[");
		for (int i = 0; i < t.size(); i ++) {
			System.out.print(" ");
			System.out.print(t.get(i));
		}
		System.out.println(" ]");
	}

	public static class BinaryNode<T> {

		BinaryNode() {
			left = right = null;
		}

		BinaryNode(T theKey) {
			this();
			key = theKey;
		}

		/**
		 * The data in the node
		 */
		T key;

		/**
		 * Left child
		 */
		BinaryNode<T> left;

		/**
		 * Right child
		 */
		BinaryNode<T> right;

		int size = 1;
	}
}