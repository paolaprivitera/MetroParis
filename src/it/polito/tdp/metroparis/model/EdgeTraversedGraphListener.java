// Classe inutile perche' l'abbiamo resa privata al model
// ma equivalente

package it.polito.tdp.metroparis.model;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;

public class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultEdge> {

	Map<Fermata, Fermata> back;
	
	Graph<Fermata, DefaultEdge> grafo;
	
	public EdgeTraversedGraphListener(Graph<Fermata, DefaultEdge> grafo, Map<Fermata, Fermata> back) {
		this.grafo = grafo;
		this.back = back;
	}

	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {		
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
		// Mi conviene rappresentare il fatto che il successivo si raggiunga dal precedente
		// perche' il predecessore e' sempre e solo uno mentre i successori possono essere tanti
		// Procedendo a ristroso e' quindi garantito che riusciro' ad arrivare alla radice (source)
		// Albero di visita al contrario che rappresento come mappa dove avro'
		// il figlio come chiave e il padre come valore
		
		
		// Quindi metto nella mappa l'informazione sul vertice che ho raggiunto
		// associata all'informazione sul vertice da cui l'ho raggiunto
		
		/*
		 * back codifica relazioni del tipo child->parent
		 * 
		 * per un nuovo vertice 'child' scoperto
		 * devo avere che:
		 * - child e' ancora sconosciuto (non ancora trovato)
		 * - parent e' gia' stato visitato
		 */
		
		Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
		Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
		// In questo modo ho estratto i vertici estremi dell'arco
		
		/*
		 * Se il grafo e' orientato,
		 * allora source == parent, target == child;
		 * 
		 * Se il grafo non e' orientato,
		 * potrebbe essere al contrario...
		 */
		
		// Devo essere sicuro che il figlio non sia ancora stato usato come chiave della mappa
		// e che il padre esista
		
		if(!back.containsKey(targetVertex) && back.containsKey(sourceVertex)) {
			back.put(targetVertex, sourceVertex);
		}
		else if(!back.containsKey(sourceVertex) && back.containsKey(targetVertex)) { // nel caso di grafi non orientati
			back.put(sourceVertex, targetVertex);
		}
		
		// In tutti gli altri casi non dobbiamo fare nulla perche'
		// sono grafi che non ci portano nuove informazioni
		
		// back.put(ev.getEdge().destinationVertex(), ev.getEdge().sourceVertex());
		
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
	}

}
