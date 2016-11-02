package msi.gama.runtime;

import java.util.Collections;
import java.util.Map;

import gnu.trove.map.hash.THashMap;

public class ExecutionContext implements IExecutionContext {

	Map<String, Object> local;
	final IExecutionContext outer;

	public ExecutionContext() {
		this(null);
	}

	ExecutionContext(final IExecutionContext outer) {
		this.outer = outer;
	}

	@Override
	public final IExecutionContext getOuterContext() {
		return outer;
	}

	@Override
	public void setTempVar(final String name, final Object value) {
		if (local == null || !local.containsKey(name)) {
			if (outer != null)
				outer.setTempVar(name, value);
		} else {
			local.put(name, value);
		}

	}

	@Override
	public Object getTempVar(final String name) {
		if (local == null || !local.containsKey(name))
			return outer == null ? null : outer.getTempVar(name);
		// if (name == ExecutionScope.EACH)
		// System.out.println("EACH value found in context " + this);
		return local.get(name);
	}

	@Override
	public ExecutionContext createCopyContext() {
		final ExecutionContext r = new ExecutionContext(outer);
		if (local != null)
			r.local = new THashMap<String, Object>(local);
		return r;
	}

	@Override
	public Map<? extends String, ? extends Object> getLocalVars() {
		return local == null ? Collections.EMPTY_MAP : local;
	}

	@Override
	public void clearLocalVars() {
		local = null;
	}

	@Override
	public void putLocalVar(final String varName, final Object val) {
		if (local == null)
			local = new THashMap<>();
		// if (varName == ExecutionScope.EACH)
		// System.out.println("EACH value set to " + val + " in context " +
		// this);
		local.put(varName, val);
	}

	@Override
	public Object getLocalVar(final String string) {
		if (local == null)
			return null;
		return local.get(string);
	}

	@Override
	public boolean hasLocalVar(final String name) {
		if (local == null)
			return false;
		return local.containsKey(name);
	}

	@Override
	public void removeLocalVar(final String name) {
		if (local == null)
			return;
		local.remove(name);
	}

	@Override
	public String toString() {
		return "execution context " + local;
	}

}