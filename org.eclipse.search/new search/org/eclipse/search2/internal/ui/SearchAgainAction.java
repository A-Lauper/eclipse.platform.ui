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
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.search.ui.ISearchResult;

import org.eclipse.search.internal.ui.SearchPluginImages;

class SearchAgainAction extends Action {
	private SearchView fView;
	
	/**
	 *	Create a new instance of this class
	 */
	public SearchAgainAction(SearchView view) {
		setText(SearchMessages.getString("SearchAgainAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchAgainAction.tooltip")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_TOOL, SearchPluginImages.IMG_TOOL_SEARCH);
		fView= view;	
	}
	/**
	 *	Invoke the resource wizard selection wizard
	 *
	 *	@param browser org.eclipse.jface.parts.Window
	 */
	public void run() {
		final ISearchResult search= fView.getCurrentSearchResult();
		if (search != null) {
			InternalSearchUI.getInstance().cancelSearch(search.getQuery());;
			InternalSearchUI.getInstance().runAgain(search.getQuery(), search);
		}
	}
}
