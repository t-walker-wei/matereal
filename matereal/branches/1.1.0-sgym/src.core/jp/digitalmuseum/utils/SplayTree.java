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
		// System.out.println("insert: "+key);
		// System.out.print(size());
		splay(key);
		// System.out.println(" "+size());
		if ((c = comparator.compare(key, root.key)) == 0) {
			// throw new DuplicateItemException(x.toString());
			return;
		}
		n = new BinaryNode<T>(key);
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
		n.size = size(n.left) + size(n.right);
		root = n;
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

	/**
	 * Doesn't work well...
	 * @return size of this tree.
	 */
	public int size() {
		return size(root);
	}

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
					t.size -= 1 + size(y.left);

					y.right = t;
					y.size += 1 + size(t.right);

					t = y;

					if (t.left == null) {
						break;
					}
				}

				// Link right
				int diff = -size(rightTreeMin.left) + size(t) - size(t.left);
				BinaryNode<T> n = header.left;
				if (n != null) {
					while ((n = n.left) != rightTreeMin && n != null) {
						n.size += diff;
					}
				}
				rightTreeMin.size += diff;
				rightTreeMin.left = t;
				rightTreeMin = t;
				// rightTreeMin.left will be overwritten in the next call of "Link right"
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
				int diff = -size(leftTreeMax.right) + size(t) - size(t.right);
				BinaryNode<T> n = header.right;
				if (n != null) {
					while ((n = n.right) != leftTreeMax && n != null) {
						n.size += diff;
					}
				}
				leftTreeMax.size += diff;
				leftTreeMax.right = t;
				leftTreeMax = t;
				// leftTreeMax.right will be overwritten in the next call of "Link right"
				t = t.right;
			} else {
				break;
			}
		}

		// Reassemble

		leftTreeMax.size -= size(leftTreeMax.right);
		leftTreeMax.right = t.left;
		leftTreeMax.size += size(t.left);

		rightTreeMin.size -= size(rightTreeMin.left);
		rightTreeMin.left = t.right;
		rightTreeMin.size += size(t.right);

		t.left = header.right;
		t.right = header.left;
		t.size = size(header.left) + size(header.right) + 1;
		root = t;
	}

	// test code stolen from Weiss
	public static void main(String[] args) {
		SplayTree<Integer> t = new SplayTree<Integer>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		final int NUMS = 200;
		final int GAP = 3;

		System.out.println("Checking... (no bad output means success)");

		// int idx = 0;
		for (int i = GAP; i != 0; i = (i + GAP) % NUMS) {
			/*
			if ((idx ++ > 120) && (idx < 140)) {
				System.out.println(idx +" " +t.size());
			}
			*/
			t.insert(i);
		}
		System.out.println("Inserts complete");
		// System.out.println(t.size());
		// System.out.println(idx);

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