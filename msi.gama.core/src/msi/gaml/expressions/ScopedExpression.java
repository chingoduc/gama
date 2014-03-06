/**
 * Created by drogoul, 20 janv. 2014
 * 
 */
package msi.gaml.expressions;

import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

/**
 * Class ScopedExpression.
 * 
 * @author drogoul
 * @since 20 janv. 2014
 * 
 */
public class ScopedExpression implements IExpression {

	final IExpression wrapped;
	final IScope scope;

	public static ScopedExpression with(final IScope scope, final IExpression wrapped) {
		if ( wrapped == null ) { return null; }
		return new ScopedExpression(scope, wrapped);
	}

	private ScopedExpression(final IScope scope, final IExpression wrapped) {
		this.scope = scope;
		this.wrapped = wrapped;
	}

	/**
	 * Method dispose()
	 * @see msi.gaml.descriptions.IGamlDescription#dispose()
	 */
	@Override
	public void dispose() {
		wrapped.dispose();
	}

	/**
	 * Method getTitle()
	 * @see msi.gaml.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() {
		return wrapped.getTitle();
	}

	/**
	 * Method getDocumentation()
	 * @see msi.gaml.descriptions.IGamlDescription#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return wrapped.getDocumentation();
	}

	/**
	 * Method getName()
	 * @see msi.gaml.descriptions.IGamlDescription#getName()
	 */
	@Override
	public String getName() {
		return wrapped.getName();
	}

	/**
	 * Method getType()
	 * @see msi.gama.common.interfaces.ITyped#getType()
	 */
	@Override
	public IType getType() {
		return wrapped.getType(); // Species in two different models ?
	}

	// /**
	// * Method getContentType()
	// * @see msi.gama.common.interfaces.ITyped#getContentType()
	// */
	// @Override
	// public IType getContentType() {
	// return wrapped.getContentType();
	// }
	//
	// /**
	// * Method getKeyType()
	// * @see msi.gama.common.interfaces.ITyped#getKeyType()
	// */
	// @Override
	// public IType getKeyType() {
	// return wrapped.getKeyType();
	// }

	/**
	 * Method value()
	 * @see msi.gaml.expressions.IExpression#value(msi.gama.runtime.IScope)
	 */
	@Override
	public Object value(final IScope unused) throws GamaRuntimeException {
		return wrapped.value(this.scope);
	}

	/**
	 * Method isConst()
	 * @see msi.gaml.expressions.IExpression#isConst()
	 */
	@Override
	public boolean isConst() {
		return wrapped.isConst();
	}

	/**
	 * Method toGaml()
	 * @see msi.gaml.expressions.IExpression#toGaml()
	 */
	@Override
	public String toGaml() {
		return wrapped.toGaml();
	}

	/**
	 * Method literalValue()
	 * @see msi.gaml.expressions.IExpression#literalValue()
	 */
	@Override
	public String literalValue() {
		return wrapped.literalValue();
	}

	/**
	 * Method resolveAgainst()
	 * @see msi.gaml.expressions.IExpression#resolveAgainst(msi.gama.runtime.IScope)
	 */
	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return wrapped.resolveAgainst(this.scope);
	}

	/**
	 * Method shouldBeParenthesized()
	 * @see msi.gaml.expressions.IExpression#shouldBeParenthesized()
	 */
	@Override
	public boolean shouldBeParenthesized() {
		return wrapped.shouldBeParenthesized();
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

}