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

package org.eclipse.ui.contentassist;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.contentassist.AbstractControlContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.ComboContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.contentassist.TextContentAssistSubjectAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A content assistant handler which handles the key binding and
 * the cue for a {@link org.eclipse.jface.text.contentassist.ContentAssistant}
 * and its subject adapter.
 * 
 * @since 3.0
 */
public class ContentAssistHandler {
	/**
	 * The target control.
	 */
	private Control fControl;
	/**
	 * The content assist subject adapter.
	 */
	private AbstractControlContentAssistSubjectAdapter fContentAssistSubjectAdapter;
	/**
	 * The content assistant.
	 */
	private SubjectControlContentAssistant fContentAssistant;
	/**
	 * The currently installed HandlerSubmission, or <code>null</code> iff none installed.
	 * This is also used as flag to tell whether content assist is enabled
	 */
	private FocusListener fFocusListener;
	/**
	 * The currently installed HandlerSubmission, or <code>null</code> iff none installed.
	 */
	private HandlerSubmission fHandlerSubmission;
	
	/**
	 * Creates a new {@link ContentAssistHandler} for the given {@link Combo}.
	 * Only a single {@link ContentAssistHandler} may be installed on a {@link Combo} instance.
	 * Content Assist is enabled by default.
	 * 
	 * @param combo target combo
	 * @param contentAssistant a configured content assistant
	 * @return a new {@link ContentAssistHandler}
	 */
	public static ContentAssistHandler createHandlerForCombo(Combo combo, SubjectControlContentAssistant contentAssistant) {
		return new ContentAssistHandler(combo, new ComboContentAssistSubjectAdapter(combo), contentAssistant);
	}
	
	/**
	 * Creates a new {@link ContentAssistHandler} for the given {@link Text}.
	 * Only a single {@link ContentAssistHandler} may be installed on a {@link Text} instance.
	 * Content Assist is enabled by default.
	 * 
	 * @param text target text
	 * @param contentAssistant a configured content assistant
	 * @return a new {@link ContentAssistHandler}
	 */
	public static ContentAssistHandler createHandlerForText(Text text, SubjectControlContentAssistant contentAssistant) {
		return new ContentAssistHandler(text, new TextContentAssistSubjectAdapter(text), contentAssistant);
	}
	
	/**
	 * Internal constructor.
	 * 
	 * @param control target control
	 * @param subjectAdapter content assist subject adapter
	 * @param contentAssistant content assistant
	 */
	private ContentAssistHandler(
			Control control,
			AbstractControlContentAssistSubjectAdapter subjectAdapter,
			SubjectControlContentAssistant contentAssistant) {
		fControl= control;
		fContentAssistant= contentAssistant;
		fContentAssistSubjectAdapter= subjectAdapter;
		setEnabled(true);
		fControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				setEnabled(false);
			}
		});
	}
	
	/**
	 * @return <code>true</code> iff content assist is enabled
	 */
	public boolean isEnabled() {
		return fFocusListener != null;
	}
	
	/**
	 * Controls enablement of content assist.
	 * When enabled, a cue is shown next to the focused field
	 * and the affordance hover shows the shortcut.
	 * 
	 * @param enable enable content assist iff true
	 */
	public void setEnabled(boolean enable) {
		if (enable == isEnabled())
			return;
		
		if (enable)
			enable();
		else
			disable();
	}
	
	/**
	 * Enable content assist.
	 */
	private void enable() {
		if (! fControl.isDisposed()) {
			fContentAssistant.install(fContentAssistSubjectAdapter);
			installCueLabelProvider();
			installFocusListener();
			if (fControl.isFocusControl())
				activateHandler();
		}
	}
	
	/**
	 * Disable content assist.
	 */
	private void disable() {
		if (! fControl.isDisposed()) {
			fContentAssistant.uninstall();
			fContentAssistSubjectAdapter.setContentAssistCueProvider(null);
			fControl.removeFocusListener(fFocusListener);
			fFocusListener= null;
			if (fHandlerSubmission != null)
				deactivateHandler();
		}
	}
	
	/**
	 * Create and install the {@link LabelProvider} for fContentAssistSubjectAdapter.
	 */
	private void installCueLabelProvider() {
		ILabelProvider labelProvider= new LabelProvider() {
			/*
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				ICommandManager commandManager= PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
				ICommand command= commandManager.getCommand(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
				List bindings= command.getKeySequenceBindings();
				if (bindings.size() == 0)
					return ContentAssistMessages.getString("ContentAssistHandler.contentAssistAvailable"); //$NON-NLS-1$
				IKeySequenceBinding ksb= (IKeySequenceBinding) bindings.get(0);
				Object[] args= { ksb.getKeySequence().format() };
				return ContentAssistMessages.getFormattedString("ContentAssistHandler.contentAssistAvailableWithKeyBinding", args); //$NON-NLS-1$
			}
		};
		fContentAssistSubjectAdapter.setContentAssistCueProvider(labelProvider);
	}

	/**
	 * Create fFocusListener and install it on fControl.
	 */
	private void installFocusListener() {
		fFocusListener= new FocusListener() {
			public void focusGained(final FocusEvent e) {
				activateHandler();
			}
			public void focusLost(FocusEvent e) {
				if (fHandlerSubmission != null)
					deactivateHandler();
			}
		};
		fControl.addFocusListener(fFocusListener);
	}
	
	/**
	 * Create and register fHandlerSubmission.
	 */
	private void activateHandler() {
		final IHandler handler= new AbstractHandler() {
			public Object execute(Map parameterValuesByName) throws ExecutionException {
				if (isEnabled())
					fContentAssistant.showPossibleCompletions();
				return null;
			}
		};
		fHandlerSubmission= new HandlerSubmission(null, fControl.getShell(), null,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM);
		IWorkbenchCommandSupport commandSupport= PlatformUI.getWorkbench().getCommandSupport();
		commandSupport.addHandlerSubmission(fHandlerSubmission);
	}
	
	/**
	 * Unregister the {@link HandlerSubmission} from the shell.
	 */
	private void deactivateHandler() {
		IWorkbenchCommandSupport commandSupport= PlatformUI.getWorkbench().getCommandSupport();
		commandSupport.removeHandlerSubmission(fHandlerSubmission);
		fHandlerSubmission= null;
	}
}
