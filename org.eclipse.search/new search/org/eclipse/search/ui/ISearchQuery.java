/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
/**
 * <p>
 * Represents a particular search query (In a Java example, this a query might
 * be "find all occurrences of 'foo' in workspace"). When it's run method is
 * called, the query places any results it finds in the
 * <code>ISearchResult</code> it that can be accessed via getSearchResult().
 * Note that <code>getSearchResult</code> may be called at any time, even
 * before the <code>run()</code> method has been called. An empty search
 * result should be returned in that case.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.0
 */
public interface ISearchQuery {
	/**
	 * This is the method that actually does the work, i.e. finds the results of
	 * the search query.
	 * 
	 * @param monitor
	 *            The progress monitor to be used
	 * @return The status after completion of the search job.
	 */
	IStatus run(IProgressMonitor monitor);
	/**
	 * Returns the a user readable label for this query. This will be used, for
	 * example to set the <code>Job</code> name if this query is executed in
	 * the background. Note that progress notification (for example, the number
	 * of matches found) should be done via the progress monitor passed into the
	 * <code>run(IProgressMonitor)</code> method
	 * 
	 * @return The user readable label of this query.
	 */
	String getLabel();
	/**
	 * Returns whether the query can be run more than once. Some queries may
	 * depend on transient information and return <code>false</code>.
	 * 
	 * @return Whether this query can be run more than once.
	 */
	boolean canRerun();
	/**
	 * Returns whether this query can be run in the background. Note that
	 * queries must do proper locking when they are run in the background (e.g.
	 * get the appropriate workspace locks).
	 * 
	 * @return Whether this query can be run in the background.
	 */
	boolean canRunInBackground();
	/**
	 * Returns the search result associated with this query. This method can be
	 * called before run is called.
	 * 
	 * @return This query's search result.
	 */
	ISearchResult getSearchResult();
}