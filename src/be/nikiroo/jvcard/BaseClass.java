package be.nikiroo.jvcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is basically a List with a parent and a "dirty" state check. It
 * sends all commands down to the initial list, but will mark itself and its
 * children as dirty or not when needed.
 * 
 * All child elements can identify their parent.
 * 
 * The dirty state is bubbling up (when dirty = true) or down (when dirty =
 * false) -- so, making changes to a child element will also mark its parent as
 * "dirty", and marking an element as pristine will also affect all its child
 * elements.
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

	/**
	 * Create a new {@link BaseClass} with the given list as its descendants.
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
		List<E> del = new ArrayList<E>();
		List<E> add = new ArrayList<E>();

		for (E oldChild : this) {
			if (!list.contains(oldChild)) {
				del.add(oldChild);
			}
		}
		for (E newChild : list) {
			if (!contains(newChild)) {
				add.add(newChild);
			}
		}

		removeAll(del);
		addAll(add);
	}

	/**
	 * Notify that this element has unsaved changes.
	 */
	void setDirty() {
		dirty = true;
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
		child.setParent(this);
		if (!initialLoad)
			child.setDirty();
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
