/*********************************************************************************************
 *
 * 'FieldDrawingAttributes.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.statements.draw;

import java.util.ArrayList;
import java.util.List;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaPair;

public class FieldDrawingAttributes extends FileDrawingAttributes {

	private static GamaPoint zeroPoint = new GamaPoint(0, 0);
	// empty whether or not we apply the texture and/or color
	// border whether or not we draw the lines

	public double depth;
	public boolean empty;
	public List<?> textures;
	public String speciesName;
	public boolean triangulated;
	public boolean grayScaled;
	public boolean withText;
	public GamaPoint fieldSize;
	public GamaPoint cellSize;

	public FieldDrawingAttributes(final ILocation size, final Double depth, final GamaPair<Double, GamaPoint> rotation,
			final ILocation location, final Boolean empty, final GamaColor color, final GamaColor border,
			final List<?> textures, final IAgent agent) {
		super(size, rotation, location, color, border, agent);
		this.depth = depth == null ? 1.0 : depth.doubleValue();
		this.empty = empty == null ? false : empty.booleanValue();
		this.border = border == null && this.empty ? color : border;
		this.textures = textures == null ? null : new ArrayList<>(textures);
	}

	/**
	 * @param name
	 * @param lineColor
	 */
	public FieldDrawingAttributes(final String name, final GamaColor border) {
		super(zeroPoint, null, border);
		speciesName = name;
		textures = null;
	}

	@Override
	public List<?> getTextures() {
		return textures;
	}

	public void setSpeciesName(final String name) {
		speciesName = name;
	}

	@Override
	public String getSpeciesName() {
		return speciesName;
	}

	@Override
	public double getDepth() {
		return depth;
	}

}