package ummisco.gama.network.skills;

import java.util.Map;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaMap;

public interface IConnector {
	public void connectToServer(IScope scope, String dest, String server) throws Exception;
	public void sendMessage(IScope scope, String dest, Map<String, String >  data) ;
	public GamaMap<String, String> fetchMessageBox(final IAgent agt);
	public boolean emptyMessageBox(IAgent agt);
}