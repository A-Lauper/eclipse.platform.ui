/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * A preference page to set the font used in the default text editor.
 * This preference page uses the text editor's preference bundle and
 * uses the key <code>"PreferencePage.description"</code> to look up
 * the page description. In addition, it uses <code>"PreferencePage.fontEditor"</code>
 * for the editor description.
 */
public class TextEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Indicates whether the preferences this page maniulates have been initialized.
	 * @since 2.0
	 */
	private static boolean fgInitialized= false;
	
	/**
	 * Create the preference page.
	 */
	public TextEditorPreferencePage() {
		super(GRID);
		
		setDescription(TextEditorMessages.getString("PreferencePage.description"));		 //$NON-NLS-1$
		Plugin plugin= Platform.getPlugin("org.eclipse.ui.workbench"); //$NON-NLS-1$
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin= (AbstractUIPlugin) plugin;
			setPreferenceStore(uiPlugin.getPreferenceStore());
		}
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
	}	
	
	/*
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	public void createFieldEditors() {
		addField(new FontFieldEditor(AbstractTextEditor.PREFERENCE_FONT, TextEditorMessages.getString("PreferencePage.fontEditor"), getFieldEditorParent())); //$NON-NLS-1$
	}

	/*
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Initialzes the defaults for the given store.
	 * @param store the preference store
	 * @since 2.0
	 */
	public static void initDefaults(IPreferenceStore store) {
		
		if (fgInitialized)
			return;
			
		fgInitialized= true;
		
		Font font= JFaceResources.getTextFont();
		if (font != null) {
			FontData[] data= font.getFontData();
			if (data != null && data.length > 0)
				PreferenceConverter.setDefault(store, AbstractTextEditor.PREFERENCE_FONT, data[0]);
		}
		
		Display display= Display.getDefault();
		Color color= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, color.getRGB());
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);
		
		color= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, color.getRGB());
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);
	}
}
