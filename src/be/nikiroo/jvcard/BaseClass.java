package be.nikiroo.jvcard;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import be.nikiroo.jvcard.resources.StringUtils;

/**
 * This class is basically a List with a parent and a "dirty" state check. It
 * sends all commands down to the initial list, but will mark itself and its
 * children as dirty or not when needed.
 * 
 * <p>
 * All child elements can identify their parent, and must not be added to 2
 * different objects without without first being removed from the previous one.
 * </p>
 * 
 * <p>
 * The dirty state is bubbling up (when dirty = true) or down (when dirty =
 * false) -- so, making changes to a child element will also mark its parent as
 * "dirty", and marking an element as pristine will also affect all its child
 * elements.
 * </p>
 * 
 * @author niki
 *
 * @param <E>
 *            the type of the child elements
 */
public abstract class BaseClass<E extends BaseClass<?>> implements List<E> {
	protected boolean dirty;
	protected BaseClass<?> parent;
	private List<E> list;

	private Comparator<E> comparator = new Comparator<E>() {
		@Override
		public int compare(E o1, E o2) {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null && o2 != null)
				return -1;
			if (o1 != null && o2 == null)
				return 1;

			return o1.getId().compareTo(o2.getId());
		}
	};

	/**
	 * Create a new {@link BaseClass} with the items in the given list as its
	 * descendants.
	 * 
	 * Note: the elements will be copied from the {@link List}, you cannot
	 * manage the {@link List} from outside
	 * 
	 * @param list
	 *            the descendants of this object, or NULL if none
	 */
	protected BaseClass(List<E> list) {
		this.list = new ArrayList<E>();

		if (list != null) {
			this.list.addAll(list);
		}

		for (E child : this) {
			_enter(child, true);
		}
	}

	/**
	 * Check if this element has unsaved changes.
	 * 
	 * @return TRUE if it has
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Delete this element from its parent if any.
	 * 
	 * @return TRUE in case of success
	 */
	public boolean delete() {
		if (parent != null) {
			return parent.remove(this);
		}

		return false;
	}

	/**
	 * Replace the elements contained in this with those in the given
	 * {@link List}.
	 * 
	 * Note: the elements will be copied from the {@link List}, you cannot
	 * manage the {@link List} from outside
	 * 
	 * @param list
	 *            the list of new elements
	 */
	public void replaceListContent(List<E> list) {
		List<E> del = new LinkedList<E>();
		List<E> add = new LinkedList<E>();

		if (!compare(list, add, del, del, add)) {
			removeAll(del);
			addAll(add);
		}
	}

	/**
	 * Compare the elements contained in <tt>this</tt> with those in the given
	 * {@link List}. It will return TRUE in case of equality, will return FALSE
	 * if not.
	 * 
	 * If not equals, the differences will be represented by the given
	 * {@link List}s if they are not NULL.
	 * <ul>
	 * <li><tt>added</tt> will represent the elements in <tt>list</tt> but not
	 * in <tt>this</tt></li>
	 * <li><tt>removed</tt> will represent the elements in <tt>this</tt> but not
	 * in <tt>list</tt></li>
	 * <li><tt>from<tt> will represent the elements in <tt>list</tt> that are
	 * already contained in <tt>this</tt> but are not equals to them (the
	 * original element from <tt>this</tt> is stored here)</li>
	 * <li><tt>to<tt> will represent the elements in <tt>list</tt> that are
	 * already contained in <tt>this</tt> but are not equals to them (the
	 * changed element from <tt>list</tt> is stored here)</li>
	 * </ul>
	 * 
	 * @param list
	 *            the list of new elements
	 * @param added
	 *            the list to add the <tt>added</tt> elements to, or NULL
	 * @param removed
	 *            the list to add the <tt>removed</tt> elements to, or NULL
	 * @param from
	 *            the map to add the <tt>from</tt> elements, or NULL
	 * @param to
	 *            the map to add the <tt>to</tt> elements, or NULL
	 * 
	 * @return TRUE if the elements are identical
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean compare(List<E> list, List<E> added, List<E> removed,
			List<E> from, List<E> to) {
		Collections.sort(this.list, comparator);

		List<E> mine = new LinkedList<E>(this.list);
		List<E> other = new LinkedList<E>(list);

		Collections.sort(other, comparator);

		boolean equ = true;
		E here = mine.size() > 0 ? mine.remove(0) : null;
		E there = other.size() > 0 ? other.remove(0) : null;

		while (here != null || there != null) {
			if (here == null
					|| (there != null && comparator.compare(here, there) > 0)) {
				if (added != null)
					added.add(there);
				there = null;
				equ = false;
			} else if (there == null || comparator.compare(here, there) < 0) {
				if (removed != null)
					removed.add(here);
				here = null;
				equ = false;
			} else {
				// they represent the same item
				if (!((BaseClass) here).isEquals(there, false)) {
					if (from != null)
						from.add(here);
					if (to != null)
						to.add(there);
					equ = false;
				}
				here = null;
				there = null;
			}

			if (here == null && mine.size() > 0)
				here = mine.remove(0);
			if (there == null && other.size() > 0)
				there = other.remove(0);
		}

		return equ;
	}

	/**
	 * Check if the given instance and this one represent the same objects (they
	 * may have different states).
	 * 
	 * @param other
	 *            the other instance
	 * 
	 * @return TRUE if they represent the same object
	 */
	public boolean isSame(BaseClass<E> other) {
		if (other == null)
			return false;

		if (!getClass().equals(other.getClass()))
			return false;

		return getId().equals(other.getId());
	}

	/**
	 * Check if the given instance and this one are equivalent (both objects in
	 * the same state, all child elements equivalent).
	 * 
	 * @param other
	 *            the other instance
	 * 
	 * @param contentOnly
	 *            do not check the state of the object itslef, only its content
	 * 
	 * @return TRUE if they are equivalent
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isEquals(BaseClass<E> other, boolean contentOnly) {
		if (other == null)
			return false;

		if (size() != other.size())
			return false;

		if (!contentOnly) {
			if (!isSame(other))
				return false;

			if (!getState().equals(other.getState()))
				return false;
		}

		Collections.sort(list, comparator);
		Collections.sort(other.list, other.comparator);
		for (int index = 0; index < size(); index++) {
			if (!((BaseClass) get(index)).isEquals(other.get(index), false))
				return false;
		}

		return true;
	}

	/**
	 * Get the recursive state of the current object, i.e., its children
	 * included. It represents the full state information about this object's
	 * children. It may not contains spaces nor new lines.
	 * 
	 * <p>
	 * Not that this state is <b>lossy</b>. You cannot retrieve the data from
	 * the state, it can only be used as an ID to check if data are identical.
	 * </p>
	 * 
	 * @param self
	 *            also include state information about the current object itself
	 *            (as opposed to its children)
	 * 
	 * @return a {@link String} representing the current content state of this
	 *         object, i.e., its children included
	 */
	public String getContentState(boolean self) {
		StringBuilder builder = new StringBuilder();
		buildContentStateRaw(builder, self);
		return StringUtils.getHash(builder.toString());
	}

	/**
	 * Return the (first) child element with the given ID or NULL if not found.
	 * 
	 * @param id
	 *            the id to look for
	 * 
	 * @return the child element or NULL
	 */
	public E getById(String id) {
		for (E child : this) {
			if (id == null) {
				if (child.getId() == null)
					return child;
			} else {
				if (id.equals(child.getId()))
					return child;
			}
		}

		return null;
	}

	/**
	 * Return a {@link String} that can be used to identify this object in DEBUG
	 * mode, i.e., a "toString" method that can identify the object's content
	 * but still be readable in a log.
	 * 
	 * @param depth
	 *            the depth into which to descend (0 = only this object, not its
	 *            children)
	 * 
	 * @return the debug {@link String}
	 */
	public String getDebugInfo(int depth) {
		StringBuilder builder = new StringBuilder();
		getDebugInfo(builder, depth, 0);
		return builder.toString();
	}

	/**
	 * Return the current ID of this object -- it is allowed to change over time
	 * (so, do not cache it).
	 * 
	 * @return the current ID
	 */
	abstract public String getId();

	/**
	 * Get the state of the current object, children <b>not included</b>. It
	 * represents the full state information about this object, but do not check
	 * its children (see {@link BaseClass#getContentState()} for that). It may
	 * not contains spaces nor new lines.
	 * 
	 * <p>
	 * Not that this state is <b>lossy</b>. You cannot retrieve the data from
	 * the state, it can only be used as an ID to check if thw data are
	 * identical.
	 * </p>
	 * 
	 * @return a {@link String} representing the current state of this object,
	 *         children not included
	 */
	abstract public String getState();

	/**
	 * Get the recursive state of the current object, i.e., its children
	 * included. It represents the full state information about this object's
	 * children.
	 * 
	 * It is not hashed.
	 * 
	 * @param builder
	 *            the {@link StringBuilder} that will represent the current
	 *            content state of this object, i.e., its children included
	 * @param self
	 *            also include state information about the current object itself
	 *            (as opposed to its children)
	 */
	void buildContentStateRaw(StringBuilder builder, boolean self) {
		Collections.sort(this.list, comparator);
		if (self)
			builder.append(getState());
		for (E child : this) {
			child.buildContentStateRaw(builder, true);
		}
	}

	/**
	 * Populate a {@link StringBuilder} that can be used to identify this object
	 * in DEBUG mode, i.e., a "toString" method that can identify the object's
	 * content but still be readable in a log.
	 * 
	 * @param depth
	 *            the depth into which to descend (0 = only this object, not its
	 *            children)
	 * 
	 * @param tab
	 *            the current tabulation increment
	 */
	void getDebugInfo(StringBuilder builder, int depth, int tab) {
		for (int i = 0; i < tab; i++)
			builder.append("	");
		builder.append(getContentState(false) + "	" + getId());

		if (depth > 0)
			builder.append(": [");

		if (depth > 0) {
			for (E child : this) {
				builder.append("\n");
				child.getDebugInfo(builder, depth - 1, tab + 1);
			}
		}
		if (depth > 0) {
			builder.append("\n");
			for (int i = 0; i < tab; i++)
				builder.append("	");
			builder.append("]");
		}
	}

	/**
	 * Notify that this element has unsaved changes.
	 */
	void setDirty() {
		dirty = true;
		if (parent != null) {
			parent.setDirty();
		}
	}

	/**
	 * Notify this element <i>and all its descendants</i> that it is in pristine
	 * state (as opposed to dirty).
	 */
	void setPristine() {
		dirty = false;
		for (E child : this) {
			child.setPristine();
		}
	}

	/**
	 * Set the parent of this element <i>and all its descendants</i>.
	 * 
	 * @param parent
	 *            the new parent
	 */
	void setParent(BaseClass<?> parent) {
		this.parent = parent;
		for (E child : this) {
			child.setParent(this);
		}
	}

	/**
	 * Each element that leaves the parent will pass trough here.
	 * 
	 * @param child
	 *            the element to remove from this
	 */
	private void _leave(E child) {
		if (child.parent != null && child.parent != this) {
			throw new InvalidParameterException(
					"You are removing this child from its rightful parent, it must be yours to do so");
		}

		child.parent = null;
		setDirty();
	}

	/**
	 * Each element that enters the parent will pass trough here.
	 * 
	 * @param child
	 *            the element to add to this
	 */
	private void _enter(E child) {
		_enter(child, false);
	}

	/**
	 * Each element that enters the parent will pass trough here.
	 * 
	 * @param child
	 *            the element to add to this
	 */
	private void _enter(E child, boolean initialLoad) {
		if (child.parent != null && child.parent != this) {
			throw new InvalidParameterException(
					"You are stealing this child from its rightful parent, you must remove it first");
		}

		child.setParent(this);
		if (!initialLoad) {
			setDirty();
			child.setDirty();
		}
	}

	@Override
	public boolean add(E e) {
		_enter(e, false);
		return list.add(e);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if (list.remove(o)) {
			if (o instanceof BaseClass<?>) {
				_leave((E) o); // expected warning
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E child : c) {
			_enter(child);
		}

		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		for (E child : c) {
			_enter(child);
		}

		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;

		for (Object o : c) {
			if (remove(o))
				changed = true;
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		ArrayList<Object> del = new ArrayList<Object>();
		for (Object o : c) {
			del.add(o);
		}
		return removeAll(del);
	}

	@Override
	public void clear() {
		for (E child : this) {
			_leave(child);
		}

		list.clear();
	}

	@Override
	public E set(int index, E element) {
		E child = get(index);
		if (child != null)
			_leave(child);
		_enter(element);

		return list.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		_enter(element);
		list.add(index, element);
	}

	@Override
	public E remove(int index) {
		E child = get(index);
		_leave(child);
		return list.remove(index);
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		final int i = index;
		return new ListIterator<E>() {
			ListIterator<E> base = list.listIterator(i);
			E last;

			@Override
			public boolean hasNext() {
				return base.hasNext();
			}

			@Override
			public E next() {
				last = base.next();
				return last;
			}

			@Override
			public boolean hasPrevious() {
				return base.hasPrevious();
			}

			@Override
			public E previous() {
				last = base.previous();
				return last;
			}

			@Override
			public int nextIndex() {
				return base.nextIndex();
			}

			@Override
			public int previousIndex() {
				return base.previousIndex();
			}

			@Override
			public void remove() {
				base.remove();
				_leave(last);
			}

			@Override
			public void set(E e) {
				base.set(e);
				_leave(last);
				_enter(e);
			}

			@Override
			public void add(E e) {
				base.add(e);
				_enter(e);
			}
		};
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
}
