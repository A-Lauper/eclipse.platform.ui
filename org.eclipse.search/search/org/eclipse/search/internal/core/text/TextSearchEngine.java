/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.util.Assert;
import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search.ui.NewSearchUI;

public class TextSearchEngine {
	
	/**
	 * @param workspace Current worspave
	 * @param scope Search scope
	 * @param visitDerived Select to visit derived resource
	 * @param collector
	 * @param matchLocator
	 * @return Returns the status
	 * @deprecated Use {@link #search(ISearchScope, boolean, ITextSearchResultCollector, MatchLocator)} instead
	 */
	public IStatus search(IWorkspace workspace, ISearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator) {
		return search(scope, visitDerived, collector, matchLocator);
	}
	
	public IStatus search(ISearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator) {
		boolean disableNIOSearch= SearchPlugin.getDefault().getPluginPreferences().getBoolean("org.eclipse.search.disableNIOSearch"); //$NON-NLS-1$
		return search(scope, visitDerived, collector, matchLocator, !disableNIOSearch);
	}
	
	public IStatus search(ISearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator, boolean allowNIOSearch) {
		Assert.isNotNull(scope);
		Assert.isNotNull(collector);
		Assert.isNotNull(matchLocator);
		IProgressMonitor monitor= collector.getProgressMonitor();
		
		IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Collection openProjects= new ArrayList(10);
		for (int i= 0; i < projects.length; i++) {
			IProject project= projects[i];
			if (project.isOpen())
				openProjects.add(project);
		}
		String message= SearchMessages.getString("TextSearchEngine.statusMessage"); //$NON-NLS-1$
		MultiStatus status= new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, message, null);
		if (!openProjects.isEmpty()) {
			int amountOfWork= (new AmountOfWorkCalculator(status, visitDerived)).process(openProjects, scope);		
			try {
				monitor.beginTask("", amountOfWork); //$NON-NLS-1$
				if (amountOfWork > 0) {
					Integer[] args= new Integer[] {new Integer(1), new Integer(amountOfWork)};
					monitor.setTaskName(SearchMessages.getFormattedString("TextSearchEngine.scanning", args)); //$NON-NLS-1$
				}				
				collector.aboutToStart();
				TextSearchVisitor visitor= new TextSearchVisitor(matchLocator, scope, visitDerived, collector, status, amountOfWork);
				visitor.setAllowNIOSearch(allowNIOSearch);
				visitor.process(openProjects);
			} catch (CoreException ex) {
				status.add(ex.getStatus());
			} finally {
				monitor.done();
				try {
					collector.done();
				} catch (CoreException ex) {
					status.add(ex.getStatus());
				}
			}
		}
		return status;
	}
}
