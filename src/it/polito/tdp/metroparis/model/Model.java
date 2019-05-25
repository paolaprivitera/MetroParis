package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	// *** INNER CLASS
	private class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultEdge>{

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
			Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
			Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
			
			if(!backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex)) {
				backVisit.put(targetVertex, sourceVertex);
			}
			else if(!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex)) { // nel caso di grafi non orientati
				backVisit.put(sourceVertex, targetVertex);
			}
	
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {			
		}
		
	}
	

	// Grafo semplice, orientato, non pesato

	private Graph<Fermata, DefaultEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	private Map<Fermata, Fermata> backVisit;

	public void creaGrafo() {

		// Creo l'oggetto grafo
		this.grafo = new SimpleDirectedGraph<>(DefaultEdge.class);

		// Devo leggere le fermate dal database per aggiungere i vertici
			
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		
		this.fermateIdMap = new HashMap<>();
		for(Fermata f : this.fermate)
			fermateIdMap.put(f.getIdFermata(), f);
		
		Graphs.addAllVertices(this.grafo, this.fermate);

		// Due vertici saranno collegati da un arco se e solo se
		// esiste almeno una Connessione tra le due fermate

		// Aggiungo gli archi

		// OPZIONE 1) (ma richiede molto tempo in caso di tanti vertici)
		/*for(Fermata partenza : this.grafo.vertexSet()) {
			for(Fermata arrivo : this.grafo.vertexSet()) {
				if(dao.esisteConnessione(partenza, arrivo)) {
					this.grafo.addEdge(partenza, arrivo);
				}
			}
		}*/

		// OPZIONE 2)
		// Chiedo per ogni stazione di partenza una lista con le stazioni di arrivo
		// In questo modo faccio meno query
		for(Fermata partenza : this.grafo.vertexSet()) {
			List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);

			for(Fermata arrivo : arrivi) {
				this.grafo.addEdge(partenza, arrivo); // Errore --> stampa null
			}

		}
		
		// Corretta soluzione con mappe (vedi Artsmia)
		
		// OPZIONE 3)
		// Modifico la query (studiando un vertice per volta):
		// String sql = "SELECT id_stazP, id_stazA FROM connessione";
		// Costruisco una query con un risultato le cui righe sono i risultati che mi servono
		// (Caso raro perche' solitamente devo fare dei join)
		// Non da' il problema del creare la fermata "finta" nel dao
		
		// DA IMPLEMENTARE !!!!!!!!!!!!!!!!!!!
		
	}
	
	// Ma se io sono su una certa stazione, quali sono le stazioni raggiungibili da questa?
	public List<Fermata> fermateRaggiungibili(Fermata source) {
		
		// Creo un iteratore in ampiezza per la visita del grafo
		/* GraphIterator<Fermata, DefaultEdge> it = new BreadthFirstIterator<>(this.grafo);
		// In questo caso parte da un punto a piacere*/
		
		List<Fermata> result = new ArrayList<Fermata>();
		
		backVisit = new HashMap<>();
		
		GraphIterator<Fermata, DefaultEdge> it = new BreadthFirstIterator<>(this.grafo, source);
		// In questo caso parte da source
		
		// Se volessi visitare in profondita' il grafo piuttosto che in ampiezza
		// il procedimento e' lo stesso; devo solo creare un DepthFirstIterator
		// piuttosto che un BreadthFirstIterator
		
		// GraphIterator<Fermata, DefaultEdge> it = new DepthFirstIterator<>(this.grafo, source);
		
		// **(TestModel) Quando mi interessa sapere quali sono i percorsi devo chiedere all'iteratore
		// di informarmi su come lavora al suo interno
		
		// ---> void      edgeTraversed(EdgeTraversalEvent<E> e)
		
		// Creiamo una classe che intercetti gli eventi del grafo che implementi l'interfaccia TraversalListener
		// EdgeTraversedGraphListener (nome Classe)
		// new Class -> add -> TraversalListener
		
		
		// Dopo aver creato l'iteratore e prima di farlo lavorare,
		// dobbiamo regitrare l'ascoltatore (il listener) sull'iteratore
		
		// it.addTraversalListener(new EdgeTraversedGraphListener(grafo, backVisit));
		// --> nel caso in cui la classe non e' privata al model
		
		// altrimenti
		
		it.addTraversalListener(new Model.EdgeTraversedGraphListener());
		
		/*
		// Addirittura potrei avere una classe "anonima" (sconsigliato in questo caso perche' ha molti metodi)
		// scarsa leggibilita'
		
		it.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {
		
		 // --> new "classe che non so come si chiama" che implementa TraversalListener
		
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> arg0) {
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
			}
				
		});
		*/
		
		// Poiche' la classe EdgeTraversedGraphListener sara' chiamata solo in questo punto
		// allora possiamo renderla privata per il modello
		// e in questo modo avra' diretto accesso a tutte le variabili di istanza
		// Per fare cio' devo dichiarare la classe all'interno del model. vedi ***
		// e implemento tutti i metodi
		
		// Prima di iniziare l'iterazione, devo popolare la mappa
		// almeno con il nodo sorgente
		
		backVisit.put(source, null); // null perche' la radice non ha un padre
		
		while(it.hasNext()) {
			result.add(it.next());
			// it.next() restituisce il prossimo elemento e avanza alla fermata successiva
		}
		
		
		// System.out.println(backVisit);
		// Data una fermata posso risalire al percorso per arrivare ad una certa fermata target
		// Vedi percorsoFinoA(target)
		
		return result;
	}

	public List<Fermata> percorsoFinoA(Fermata target) {
		
		if(!backVisit.containsKey(target)) {
			// Il target non e' raggiungibile dalla source
			return null;
		}
		
		List<Fermata> percorso = new LinkedList<>();
		
		Fermata f = target;
		
		while(f!=null) { // se f == null vuol dire che sono arrivato alla radice
		// Aggiungo il target al percorso
		
		
		// percorso.add(f);
		
		
		// In questo modo mi fornisce le fermate al contrario, cioe' dall'ultima alla prima
		// Potrei aggiungere con percorso.add(index, f);
		// che aggiunge la fermata nella posizione dell'index e sposta tutte le altre
		// Questo e' possibile avendo creato la linked list
		// percorso.add(0, f); -> aggiungo sempre nella prima posizione
		
		percorso.add(0, f);	
			
		// Passo al padre
		f = backVisit.get(f);
		}
		
		return percorso;
	}
	
	// ATTENZIONE! (ma non e' obbligatorio implementarlo -> si puo' usare il metodo da noi implementato)
	// Per trovare l'arco avrei potuto usare il metodo getSpanningTreeEdge(V v)
	// che ritorna l'arco che ha portato a scoprire quel vertice
	
	
	public Graph<Fermata, DefaultEdge> getGrafo() {
		return grafo;
	}

	public List<Fermata> getFermate() {
		return fermate;
	}


}
