/**
 * Created by drogoul, 9 janv. 2014
 * 
 */
package msi.gama.util.file;

import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Class GamaXMLFile.
 * TODO: Everything ! What kind of buffer should be returned from here ?
 * @author drogoul
 * @since 9 janv. 2014
 * 
 */
@file(name = "xml", extensions = "xml")
public class GamaXMLFile extends GamaFile {

	/**
	 * @param scope
	 * @param pathName
	 * @throws GamaRuntimeException
	 */
	public GamaXMLFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Method computeEnvelope()
	 * @see msi.gama.util.file.IGamaFile#computeEnvelope(msi.gama.runtime.IScope)
	 */
	@Override
	public Envelope computeEnvelope(final IScope scope) {
		return null;
	}

	/**
	 * Method fillBuffer()
	 * @see msi.gama.util.file.GamaFile#fillBuffer(msi.gama.runtime.IScope)
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {}

	/**
	 * Method flushBuffer()
	 * @see msi.gama.util.file.GamaFile#flushBuffer()
	 */
	@Override
	protected void flushBuffer() throws GamaRuntimeException {}

}