package org.eclipse.search.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 * Specifies a search result view entry.
 * This entry provides information about the markers
 * it groups by a client defined key. Each entry in the search
 * result view corresponds to a different key.
 * <p>
 * The UI allows stepping through this entry's markers grouped by the key.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ISearchResultViewEntry {
	/**
	 * Returns the key by which this entry's markers
	 * are logically grouped. A line in a text could be such a key.
	 * Clients supply this key as a parameter to <code>ISearchResultView.addMatch</code>.
	 *
	 * @return	the common resource of this entry's markers
	 * @see	ISearchResultView#addMatch
	 */
	public Object getGroupByKey();
	/**
	 * Returns the resource to which this entry's markers are attached.
	 * This is a convenience method for <code>getSelectedMarker().getResource()</code>.
	 *
	 * @return	the common resource of this entry's markers
	 */
	public IResource getResource();
	/**
	 * Returns the number of markers grouped by this entry.
	 *
	 * @return	the number of markers
	 */	
	public int getMatchCount();
	/**
	 * Returns the selected marker of this entry, or the first one
	 * if no marker is selected.
	 * A search results view entry can group markers
	 * which the UI allows the user to step through them while
	 * this entry remains selected.
	 *
	 * @return	the selected marker inside this entry, or
	 *		<code>null</code> if the entry has no markers
	 */	
	public IMarker getSelectedMarker();
}

