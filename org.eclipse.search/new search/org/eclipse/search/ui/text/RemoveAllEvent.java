/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui.text;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;
/**
 * An event indicating that all matches have been removed from a <code>AbstractTextSearchResult</code>.
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public class RemoveAllEvent extends SearchResultEvent {
	public RemoveAllEvent(ISearchResult searchResult) {
		super(searchResult);
	}
}
