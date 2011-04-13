package jp.digitalmuseum.utils;

import java.util.Comparator;

/**
 * A tow-down splay tree based on Danny Sleator's implementation.
 *
 * @param <T> Type of elements of this tree.
 * @see {@link ftp://ftp.cs.cmu.edu/user/sleator/splaying/SplayTree.java}
 */
public class SplayTree<T> {
	private BinaryNode<T> root;
	private Comparator<T> comparator;

	public SplayTree(Comparator<T> comparator) {
		this(null, comparator);
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
	 * @throws DuplicateItemException
	 *             if x is already present.
	 */
	public void insert(T key) {
		BinaryNode<T> n;
		int c;
		if (root == null) {
			root = new BinaryNode<T>(key);
			return;
		}
		splay(key);
		if ((c = comparator.compare(key, root.key)) == 0) {
			// throw new DuplicateItemException(x.toString());
			return;
		}
		n = new BinaryNode<T>(key);
		if (c < 0) {
			n.left = root.left;
			n.right = root;
			root.left = null;
		} else {
			n.right = root.right;
			n.left = root;
			root.right = null;
		}
		root = n;
		root.size = 1
				+ (root.left == null ? 0 : root.left.size)
				+ (root.right == null ? 0 : root.right.size);
	}

	/**
	 * Remove from the tree.
	 *
	 * @param x
	 *            the item to remove.
	 * @throws ItemNotFoundException
	 *             if x is not found.
	 */
	public void remove(T key) {
		BinaryNode<T> x;
		splay(key);

		if (comparator.compare(key, root.key) != 0) {
			// throw new ItemNotFoundException(x.toString());
			return;
		}

		// Now delete the root
		if (root.left == null) {
			root = root.right;
		} else {
			x = root.right;
			root = root.left;
			splay(key);
			root.right = x;
		}
	}

	/**
	 * Find the smallest item in the tree.
	 */
	public T findMin() {
		BinaryNode<T> x = root;
		if (root == null) {
			return null;
		}
		while (x.left != null) {
			x = x.left;
		}
		splay(x.key);
		return x.key;
	}

	/**
	 * Find the largest item in the tree.
	 */
	public T findMax() {
		BinaryNode<T> x = root;
		if (root == null)
			return null;
		while (x.right != null)
			x = x.right;
		splay(x.key);
		return x.key;
	}

	/**
	 * Find an item in the tree.
	 */
	public T find(T key) {
		if (root == null) {
			return null;
		}
		splay(key);
		if (comparator.compare(root.key, key) != 0) {
			return null;
		}
		return root.key;
	}

	public SplayTree<T> split(T key) {
		if (root == null) {
			return null;
		}
		splay(key);
		SplayTree<T> splayTree = new SplayTree<T>(root.right, comparator);
		root.right = null;
		return splayTree;
	}

	public SplayTree<T> split(int index) {
		return null;
	}

	public T get(int index) {
		return get(root, index);
	}

	private T get(BinaryNode<T> root, int index) {
		if (index == 0) {
			return root.key;
		}
		T key = get(root.left, index - 1);
		if (key != null) {
			return key;
		}
		return get(root.right, index - 1 - root.left.size);
	}

	public void join(SplayTree<T> splayTree) {
		if (splayTree == null || splayTree.root == null) {
			return;
		}
		if (root == null) {
			root = splayTree.root;
			return;
		}

		splay(splayTree.root.key);

		if (root.left == null) {
			root.left = splayTree.root;
			return;
		}

		if (root.right == null) {
			root.right = splayTree.root;
			return;
		}

		/* root.left != null && root.right != null */
	}

	/**
	 * Test if the tree is logically empty.
	 *
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return root == null;
	}

	private int size(BinaryNode<T> node) {
		return node == null ? 0 : node.size;
	}

	/*
	public int size() {
		return size(root);
	}
	*/

	private void toString(BinaryNode<T> p, StringBuilder sb) {
		if (p == null) {
			return;
		}
		toString(p.left, sb);
		sb.append(p.key);
		sb.append(" ");
		toString(p.right, sb);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(root, sb);
		return sb.toString();
	}

	private BinaryNode<T> header = new BinaryNode<T>(null); // For splay

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
		BinaryNode<T> leftTreeMax, rightTreeMin, t, y;
		leftTreeMax = rightTreeMin = header;
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
					y.right = t;
					t.size = size(t.left) + size(t.right) + 1;
					y.size = size(y.left) + size(t) + 1;
					t = y;
					if (t.left == null) {
						break;
					}
				}

				// Link right
				rightTreeMin.left = t;
				rightTreeMin.size = size(t) + size(rightTreeMin.right) + 1;
				rightTreeMin = t;
				t = t.left;
			} else if (comparator.compare(key, t.key) > 0) {
				if (t.right == null) {
					break;
				}

				// Rotate left
				if (comparator.compare(key, t.right.key) > 0) {
					y = t.right;
					t.right = y.left;
					y.left = t;
					t.size = size(t.left) + size(t.right) + 1;
					y.size = size(t) + size(y.right) + 1;
					t = y;
					if (t.right == null) {
						break;
					}
				}

				// Link left
				leftTreeMax.right = t;
				leftTreeMax.size = size(leftTreeMax.left) + size(t) + 1;
				leftTreeMax = t;
				t = t.right;
			} else {
				break;
			}
		}

		// Assemble
		leftTreeMax.right = t.left;
		leftTreeMax.size = size(leftTreeMax.left) + size(leftTreeMax.right) + 1;
		rightTreeMin.left = t.right;
		rightTreeMin.size = size(rightTreeMin.left) + size(rightTreeMin.right) + 1;
		t.left = header.right;
		t.right = header.left;
		root = t;
		root.size = size(root.left) + size(root.right) + 1;
	}

	// test code stolen from Weiss
	public static void main(String[] args) {
		SplayTree<Integer> t = new SplayTree<Integer>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		final int NUMS = 40000;
		final int GAP = 307;

		System.out.println("Checking... (no bad output means success)");

		int idx = 0;
		for (int i = GAP; i != 0; i = (i + GAP) % NUMS) {
			if ((idx ++ > 120) && (idx < 140)) {
				//System.out.println(idx +" " +t.size());
			}
			t.insert(i);
		}
		System.out.println("Inserts complete");
		// System.out.println(t.size());
		System.out.println(idx);

		for (int i = 1; i < NUMS; i += 2) {
			t.remove(i);
		}
		System.out.println("Removes complete");
		// System.out.println(t.size());

		if (((Integer) (t.findMin())).intValue() != 2
				|| ((Integer) (t.findMax())).intValue() != NUMS - 2)
			System.out.println("FindMin or FindMax error!");

		for (int i = 2; i < NUMS; i += 2)
			if (((Integer) t.find(i)).intValue() != i)
				System.out.println("Error: find fails for " + i);

		for (int i = 1; i < NUMS; i += 2)
			if (t.find(i) != null)
				System.out.println("Error: Found deleted item " + i);
	}

	public static class BinaryNode<T> {
		BinaryNode(T theKey) {
			key = theKey;
			left = right = null;
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