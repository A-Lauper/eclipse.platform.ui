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
package org.eclipse.search.internal.ui.text;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.widgets.Item;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

/* package */ class ReplaceAction2 extends Action {
	
	private IWorkbenchSite fSite;
	private IFile[] fElements;
	private FileSearchPage fPage;
	
	private static class ItemIterator implements Iterator {
		private Item[] fArray;
		private int fNextPosition;
		ItemIterator(Item[] array) {
			fArray= array;
			fNextPosition= 0;
		}

		public boolean hasNext() {
			return fNextPosition < fArray.length;
		}

		public Object next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return fArray[fNextPosition++].getData();
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public ReplaceAction2(FileSearchPage page, IFile[] elements) {
		Assert.isNotNull(page);
		fSite= page.getSite();
		if (elements != null)
			fElements= elements;
		else
			fElements= new IFile[0];
		fPage= page;
		
		setText(SearchMessages.getString("ReplaceAction.label_all")); //$NON-NLS-1$
		setEnabled(!(fElements.length == 0));
	}
	
	public ReplaceAction2(FileSearchPage page) {
		Assert.isNotNull(page);
		fSite= page.getSite();
		fPage= page;
		
		Item[] items= null;
		StructuredViewer viewer= fPage.getViewer();
		if (viewer instanceof TreeViewer) {
			items= ((TreeViewer)viewer).getTree().getItems();
		} else if (viewer instanceof TableViewer) {
			items= ((TableViewer)viewer).getTable().getItems();
		}
		fElements= collectFiles(new ItemIterator(items));
		
		setText(SearchMessages.getString("ReplaceAction.label_all")); //$NON-NLS-1$
		setEnabled(!(fElements.length == 0));
	}

	
	public ReplaceAction2(FileSearchPage page, IStructuredSelection selection) {
		fSite= page.getSite();
		fPage= page;
		setText(SearchMessages.getString("ReplaceAction.label_selected")); //$NON-NLS-1$
		fElements= collectFiles(selection.iterator());
		setEnabled(!(fElements.length == 0));
	}
	
	private IFile[] collectFiles(Iterator resources) {
		final Set files= new HashSet();
		while (resources.hasNext()) {
			IResource resource= (IResource) resources.next();
			try {
				resource.accept(new IResourceProxyVisitor() {
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if (proxy.getType() == IResource.FILE) {
							files.add(proxy.requestResource());
							return false;
						}
						return true;
					}
				}, IContainer.NONE);
			} catch (CoreException e) {
				// TODO Don't know yet how to handle this. This is called when we open the context
				// menu. A bad time to show a dialog.
				SearchPlugin.getDefault().getLog().log(e.getStatus());
			}
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}


	public void run() {
		if (validateResources((FileSearchQuery) fPage.getInput().getQuery())) {
			ReplaceDialog2 dialog= new ReplaceDialog2(fSite.getShell(), fElements, fPage);
			dialog.open();
		}
	}
	
	private boolean validateResources(final FileSearchQuery operation) {
		final List outOfDateEntries= new ArrayList();
		for (int j= 0; j < fElements.length; j++) {
			IFile entry = fElements[j];
			Match[] markers= fPage.getDisplayedMatches(entry);
			for (int i= 0; i < markers.length; i++) {
				if (isOutOfDate((FileMatch)markers[i])) {
					outOfDateEntries.add(entry);
					break;
				}				
			}
		}
	
		final List outOfSyncEntries= new ArrayList();
		for (int i= 0; i < fElements.length; i++) {
			IFile entry = fElements[i];
			if (isOutOfSync(entry)) {
				outOfSyncEntries.add(entry);
			}
		}
		
		if (outOfDateEntries.size() > 0 || outOfSyncEntries.size() > 0) {
			if (askForResearch(outOfDateEntries, outOfSyncEntries)) {
				ProgressMonitorDialog pmd= new ProgressMonitorDialog(fSite.getShell());
				try {
					pmd.run(true, true, new WorkspaceModifyOperation(null) {
						protected void execute(IProgressMonitor monitor) throws CoreException {
							research(monitor, outOfDateEntries, operation);
						}
					});
					return true;
				} catch (InvocationTargetException e) {
					ExceptionHandler.handle(e, fSite.getShell(), SearchMessages.getString("ReplaceAction.label"), SearchMessages.getString("ReplaceAction.research.error")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (InterruptedException e) {
					// canceled
				}
			}
			return false;
		}
		return true;
	}

	private void research(IProgressMonitor monitor, List outOfDateEntries, FileSearchQuery operation) throws CoreException {
		IStatus status= null;
		for (Iterator elements = outOfDateEntries.iterator(); elements.hasNext();) {
			IFile entry = (IFile) elements.next();
				status = research(operation, monitor, entry);
			if (status != null && !status.isOK()) {
				throw new CoreException(status);
			}
		}
	}

	private boolean askForResearch(List outOfDateEntries, List outOfSyncEntries) {
		SearchAgainConfirmationDialog dialog= new SearchAgainConfirmationDialog(fSite.getShell(), (ILabelProvider) fPage.getViewer().getLabelProvider(), outOfSyncEntries, outOfDateEntries);
		return dialog.open() == IDialogConstants.OK_ID;
	}
	
	private boolean isOutOfDate(FileMatch match) {
		
		if (match.getCreationTimeStamp() != match.getFile().getModificationStamp())
			return true;
		ITextFileBufferManager bm= FileBuffers.getTextFileBufferManager();
		ITextFileBuffer fb= bm.getTextFileBuffer(match.getFile().getFullPath());
		if (fb != null && fb.isDirty())
			return true;
		return false;
	}

	private boolean isOutOfSync(IFile entry) {
		return !entry.isSynchronized(IResource.DEPTH_ZERO); 
	}
		
	private IStatus research(FileSearchQuery operation, final IProgressMonitor monitor, IFile entry) {
		Match[] matches= fPage.getDisplayedMatches(entry);
		IStatus status= operation.searchInFile(getResult(), monitor, entry);
		if (status == null || status.isOK()) {
			for (int i= 0; i < matches.length; i++) {
				getResult().removeMatch(matches[i]);
			}
		}
		return status;
	}

	private AbstractTextSearchResult getResult() {
		return fPage.getInput();
	}
	
}
