/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.core.tests;

import junit.framework.TestCase;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;

import org.eclipse.search2.internal.ui.InternalSearchUI;


/**
 * @author Thomas M�der
 *
 */
public class TestSearchResult extends TestCase {
	
	public void testAddMatch() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();
		InternalSearchUI.getInstance().addQuery(query);
		
		String object= "object"; //$NON-NLS-1$
		
		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertEquals(result.getMatchCount(), 2);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 2);
	}
	
	public void testRemoveMatch() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();
		InternalSearchUI.getInstance().addQuery(query);
		
		String object= "object"; //$NON-NLS-1$
		
		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertEquals(result.getMatchCount(), 2);
		
		result.removeMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		result.removeMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		
	}
	
	public void testMatchEvent() {
		final boolean [] wasAdded= { false };
		final boolean [] wasRemoved= { false };
	
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();
		InternalSearchUI.getInstance().addQuery(query);
		
		result.addListener(new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				if (e instanceof MatchEvent) {
					MatchEvent evt= (MatchEvent) e;
					if (evt.getKind() == MatchEvent.ADDED) {
						wasAdded[0]= true;
					} else {
						wasRemoved[0]= true;
					}
				}
			}
		});
		
		String object= "object"; //$NON-NLS-1$
		
		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		assertTrue(wasAdded[0]);
		wasAdded[0]= false;
		result.addMatch(match1);
		assertFalse(wasAdded[0]);
		
		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertTrue(wasAdded[0]);
		wasAdded[0]= false;
		
		result.removeMatch(match2);
		assertTrue(wasRemoved[0]);
		wasRemoved[0]= false;
		
		result.removeMatch(match2);
		assertFalse(wasRemoved[0]);
	}
}
