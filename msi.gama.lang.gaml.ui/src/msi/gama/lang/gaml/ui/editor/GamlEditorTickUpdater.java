/*********************************************************************************************
 * 
 *
 * 'GamlEditorTickUpdater.java', in plugin 'msi.gama.lang.gaml.ui', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.lang.gaml.ui.editor;

import msi.gama.gui.swt.IGamaIcons;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.*;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.ui.editor.*;

/**
 * The class GamlEditorTickUpdater.
 * 
 * @author drogoul
 * @since 8 sept. 2013
 * 
 */
public class GamlEditorTickUpdater extends XtextEditorErrorTickUpdater {

	@Override
	protected void updateEditorImage(final XtextEditor editor) {
		Severity severity = getSeverity(editor);
		if ( severity == null || severity == Severity.INFO ) {
			ImageDescriptor descriptor = IGamaIcons.OVERLAY_OK.descriptor();
			DecorationOverlayIcon decorationOverlayIcon =
				new DecorationOverlayIcon(editor.getDefaultImage(), descriptor, IDecoration.BOTTOM_LEFT);
			scheduleUpdateEditor(decorationOverlayIcon);
		} else {
			super.updateEditorImage(editor);
		}

	}

	@Override
	public void modelChanged(final IAnnotationModel model) {
		// TODO A place where we can hook something ? (for instance the feedback to another editor, etc..
		super.modelChanged(model);
	}

}