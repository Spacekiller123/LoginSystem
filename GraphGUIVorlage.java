
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;

public class GraphGUIVorlage extends JFrame implements ActionListener {
    private JTextField ausgabe;
    private JTextArea ausgabe2;
    private JTextField start_eingabe, von_eingabe, nach_eingabe, datei_eingabe;

    private Graph g;
    private boolean zielErreicht;

  
    /* ------------------------------------------------------------------- 
    Tiefen- und Breitensuche
    ------------------------------------------------------------------- */

    private void sucheTief(String start) {  
        GraphNode startnode = g.getNode(start);
        Stack<GraphNode> s = new Stack();
        s.push(startnode);
        ausgabe2.append(s.top().getName() + "\n");
        while(!s.isEmpty()){
            GraphNode aktuell = s.top();
            aktuell.mark();          
            List<GraphNode> l = g.getNeighbours(aktuell);
            l.toFirst();          
            while(l.hasAccess()){
                GraphNode nachbar = l.getContent();
                if(nachbar.isMarked()){
                    l.remove();                  
                }
                else{
                    l.next();  
                }
            }
            l = sortiereListe(l);
            if(!l.isEmpty()){
                l.toFirst();
                s.push(l.getContent());
                ausgabe2.append(s.top().getName() + "\n");
            }
            else{
                s.pop();
            }
        }
    }

  
    private void sucheBreit(String start) {
        GraphNode startnode = g.getNode(start);
        Queue<GraphNode> q=new Queue<GraphNode>();
        q.enqueue(startnode);
        while (!q.isEmpty()) {
            GraphNode aktuell = q.front();
            aktuell.mark();
            ausgabe2.append(aktuell.toString().toString());
            q.dequeue();
            List<GraphNode> l = g.getNeighbours(aktuell);
            l.toFirst();
            while (l.hasAccess()) { 
                GraphNode nachbar=l.getContent();
                if (!nachbar.isMarked()) {
                    q=nichtDoppeltEinfuegen(q,nachbar);
                } // end of if
                l.next();
            } // end of while

        } // end of while
    }

    private Queue nichtDoppeltEinfuegen(Queue q, GraphNode node) {

        Queue qneu = new Queue();
        boolean insert = true;

        while (!q.isEmpty()) {
            GraphNode n = (GraphNode) q.front();
            q.dequeue();
            if (n.getName().equals(node.getName())) {
                insert = false;
            }
            qneu.enqueue(n);
        }

        if (insert) { qneu.enqueue(node); }

        return qneu;
    }

    /* ------------------------------------------------------------------- 
    Backtracking: Weg von A nach B
    ------------------------------------------------------------------- */

    public void sucheWeg(GraphNode pVon, GraphNode pNach) {        
        g.resetMarks();   
        List<GraphNode> Wegliste = new List<GraphNode>();       
        sucheRek(pVon, pNach, Wegliste);
    }
    
    public void sucheRek(GraphNode pVon, GraphNode pNach,List<GraphNode> Wegliste){
    	
    	if(pVon == pNach){
    		Wegliste.append(pVon);
    		druckeWegAus(Wegliste);
    	}
    	else{   	
    		pVon.mark();   		
    		List<GraphNode> Nachbarn = g.getNeighbours(pVon);
    		Nachbarn.toFirst();
    		while(Nachbarn.hasAccess()){
                GraphNode nachbar = Nachbarn.getContent();
                if(nachbar.isMarked()){
                	Nachbarn.remove();                  
                }
                else{
                	Nachbarn.next();  
                }
            }
    		Nachbarn.toFirst();
    		
    		if(!Nachbarn.isEmpty()){
    			Wegliste.append(pVon);
    			Wegliste.toFirst();
    			sucheRek(Nachbarn.getContent(), pNach, Wegliste);
    		}
    		else{
    			if(!Wegliste.isEmpty()){
    				Wegliste.toLast();
    				GraphNode tmp = Wegliste.getContent();
    				Wegliste.remove();
    				sucheRek(tmp, pNach, Wegliste);
    			}
    			else{
    				ausgabe2.append("kein Weg gefunden");
    			}
    		}
    	}
    }


    private void druckeWegAus(List<GraphNode> Wegliste) {
       Wegliste.toFirst();
       while(!Wegliste.isEmpty()){
    	   ausgabe2.append(Wegliste.getContent().getName() + "\n");
    	   Wegliste.remove();
       }
    }

    /* ------------------------------------------------------------------- 
    Dijkstra
    ------------------------------------------------------------------- */

    public void starteDijkstra(String startKnoten) {

        // 1. VORBEREITUNGEN
        GraphNode vonKnoten = g.getNode(startKnoten);

        // Alle Knoten ohne Markierung
        g.resetMarks();

        // Bestimme die Anzahl der Knoten im Graphen
        List l = g.getNodes();
        l.toFirst();
        int n = 0;
        while (l.hasAccess()) {
            n++;
            l.next();
        }

        // Distanzen bis auf den Startknoten auf "unendlich" setzen
        double[] distanz = new double[n+1];
        GraphNode[] nodes = new GraphNode[n+1];

        l.toFirst();
        int i=0;
        while (l.hasAccess()) {
            i++;
            GraphNode node = (GraphNode) l.getContent();
            distanz[i] = 10000.0;
            if (node==vonKnoten) {
                distanz[i] = 0.0;
            }
            nodes[i] = node;
            l.next();
        }

        // 2. KERNALGORITHMUS
        for (i=1; i<=n-1; i++) {

            // Bestimme unmarkierten Knoten mit minimaler Distanz
            int aktuell_nr = -1;
            double min = 10000.0;
            for (int j=1; j<=n; j++) {
                if ((!nodes[j].isMarked()) && (distanz[j]<min)) {
                    aktuell_nr = j;
                    min = distanz[j];
                }
            }

            if (aktuell_nr>0) { // War �berhaupt noch ein unmarkierter Knoten da?

                GraphNode aktuell_node = nodes[aktuell_nr];
                aktuell_node.mark();

                List nachbarn = g.getNeighbours(aktuell_node);
                nachbarn.toFirst();

                // F�r alle unmarkierten Nachbarknoten q von aktuell...
                while (nachbarn.hasAccess()) {
                    GraphNode q_node = (GraphNode) nachbarn.getContent();

                    // Welche Nummer hat q?
                    int q_nr = -1;
                    for (int k=1; k<=n; k++) {
                        if (nodes[k]==q_node) { 
                            q_nr = k;
                            break;
                        }
                    }

                    // Kann die aktuelle Distanz noch unterboten werden?
                    if (!q_node.isMarked()) {
                        if (distanz[aktuell_nr]+g.getEdgeWeight(aktuell_node,q_node) < distanz[q_nr]) {
                            distanz[q_nr] = distanz[aktuell_nr]+g.getEdgeWeight(aktuell_node,q_node);
                        }                
                    }
                    nachbarn.next();           
                }

            }

        }

        // 3. AUSGABE
        ausgabe2.setText("");
        for (i=1; i<=n; i++) {
            ausgabe2.append(i+": "+distanz[i]+"\n");
        }
    }   

    /* ------------------------------------------------------------------- 
    Laden eines Graphen aus einer Textdatei
    ------------------------------------------------------------------- */

    private void ladeGraph(String dateiname) {

        In in = new In();
        in.open(dateiname);
        String zeile;
        ausgabe2.setText("Lese Graph ein.\n");

        // Anzahl Knoten
        int nodes = 0;
        if (in.isAnotherLine()) {
            zeile = in.readLine();
            try { 
                nodes = (new Integer(zeile)).intValue();
            }
            catch (Exception e) {}
        }
        ausgabe2.append(nodes+" Knoten, ");

        // Knoten anlegen
        g = new Graph();
        String nodename;
        for (int i=1; i<=nodes; i++) {
            nodename = (new Integer(i)).toString();
            GraphNode node = new GraphNode(nodename);
            g.addNode(node);
        }

        // Anzahl Kanten
        int edges = 0;
        if (in.isAnotherLine()) {
            zeile = in.readLine();
            try { 
                edges = (new Integer(zeile)).intValue();
            }
            catch (Exception e) {}
        }
        ausgabe2.append(edges+" Kanten.\n");

        // Kanten einlesen und anlegen      
        for (int i=0; i<edges; i++) {
            if (in.isAnotherLine()) {
                zeile = in.readLine();
                String[] parts = zeile.split(",");
                if (parts.length>=2) {
                    String n1 = parts[0];
                    String n2 = parts[1];
                    GraphNode node1 = g.getNode(n1);
                    GraphNode node2 = g.getNode(n2);
                    double weight = 0;
                    if (parts.length>=3) {
                        try { 
                            weight = (new Double(parts[2])).doubleValue();
                        }
                        catch (Exception e) {}
                    }
                    g.addEdge(node1, node2, weight);
                    ausgabe2.append(n1+"-"+n2+" ("+weight+")\n");
                }
            }
        }

        in.close();
    }

    
    /* ------------------------------------------------------------------- 
    GUI-Programmierung
    ------------------------------------------------------------------- */

    public GraphGUIVorlage() {          

        /* a) Fenster-Vorbereitungen */
        /* ------------------------- */

        /* In JFrame wird zwischen dem Fenster und der Zeichenflaeche
         * unterschieden. cpane repraesentiert die Zeichenflaeche. */
        Container cpane = getContentPane(); 

        /* verwendet KEINEN LayoutManager, d.h. alle Bestandteile des
        Fenster werden absolut positioniert und wachsen bei Aenderung
        der Fenstergoesse NICHT mit. */
        cpane.setLayout(null);

        /* Titel, erscheint am oberen Rand des Fensters. */
        setTitle("GraphGUIVorlage");

        /* Position des Fensters und Groesse (abh. von Button-Zahl) */
        setBounds(200,200,800,450);

        /* Verhalten beim Schliessen des Fensters - Programmende! */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* b) Einfuegen der Ausgabezeilen */
        /* ----------------------------- */

        /* Erzeugen eines Schriftzugs */
        JLabel ausgabe_label = new JLabel("Ausgabe");
        ausgabe_label.setBounds(250,15,200,20);

        /* Einfuegen des Schriftzugs in die Zeichenflaeche */
        cpane.add(ausgabe_label);

        /* Erzeugen der Ausgabezeile */
        ausgabe = new JTextField("");
        ausgabe.setBounds(250,40,300,60);
        ausgabe.setEditable(false);

        /* Einfugen der Eingabezeile in die Zeichenflaeche */
        cpane.add(ausgabe);

        /* Erzeugen eines Schriftzugs */
        JLabel ausgabe2_label = new JLabel("Graph");
        ausgabe2_label.setBounds(20,15,200,20);

        /* Einfuegen des Schriftzugs in die Zeichenflaeche */
        cpane.add(ausgabe2_label);

        /* Erzeugen des Ausgabebereichs */
        ausgabe2 = new JTextArea("");
        ausgabe2.setEditable(false);

        JScrollPane ausgabe2_roll = new JScrollPane(ausgabe2);
        ausgabe2_roll.setBounds(20,40,200,300);

        /* Einfuegen der Eingabezeile in die Zeichenflaeche */
        cpane.add(ausgabe2_roll);

        /* c) Einfuegen der Buttons */
        /* ----------------------- */

        /* Erzeugen eines Laden-Buttons */
        JButton startbutton = new JButton("Laden");
        startbutton.setBounds(250,120,80,20);

        /* Kommando, das beim Druecken uebermittelt wird */
        startbutton.setActionCommand("Laden");

        /* Setzt das aktuelle Fenster als Listener (d.h. das Fenster
         * wird benachrichtigt, wenn der Button gedrueckt wird. */
        startbutton.addActionListener(this);

        /* Einf�gen des Buttons in die Zeichenflaeche */
        cpane.add(startbutton);

        /* Erzeugen der Schriftzuege */
        JLabel datei_label = new JLabel("Dateiname");
        datei_label.setBounds(350,100,100,20);
        cpane.add(datei_label);

        /* Erzeugen der Eingabezeilen */
        datei_eingabe = new JTextField("graph1.txt");
        datei_eingabe.setBounds(350,120,200,20);        
        cpane.add(datei_eingabe);

        /* Erzeugen eines Breite-Buttons */
        JButton einfuegenbutton = new JButton("Breite");
        einfuegenbutton.setBounds(250,200,100,20);

        /* Kommando, das beim Druecken uebermittelt wird */
        einfuegenbutton.setActionCommand("Breite");

        /* Setzt das aktuelle Fenster als Listener (d.h. das Fenster
         * wird benachrichtigt, wenn der Button gedrueckt wird. */
        einfuegenbutton.addActionListener(this);

        /* Einfuegen des Buttons in die Zeichenflaeche */
        cpane.add(einfuegenbutton);

        /* Erzeugen der Schriftzuege */
        JLabel kdnr1_label = new JLabel("Start");
        kdnr1_label.setBounds(370,180,50,20);
        cpane.add(kdnr1_label);

        /* Erzeugen der Eingabezeilen */
        start_eingabe = new JTextField("");
        start_eingabe.setBounds(370,200,100,20);        
        cpane.add(start_eingabe);

        /* Erzeugen eines Tiefe-Buttons */
        JButton loeschenbutton = new JButton("Tiefe");
        loeschenbutton.setBounds(250,240,100,20);

        /* Kommando, das beim Dr�cken �bermittelt wird */
        loeschenbutton.setActionCommand("Tiefe");

        /* Setzt das aktuelle Fenster als Listener (d.h. das Fenster
         * wird benachrichtigt, wenn der Button gedr�ckt wird. */
        loeschenbutton.addActionListener(this);

        /* Einf�gen des Buttons in die Zeichenfl�che */
        cpane.add(loeschenbutton);

        /* Erzeugen eines Backtracking-Buttons */
        JButton suchenbutton = new JButton("Backtrack");
        suchenbutton.setBounds(250,300,100,20);

        /* Kommando, das beim Dr�cken �bermittelt wird */
        suchenbutton.setActionCommand("Backtrack");

        /* Setzt das aktuelle Fenster als Listener (d.h. das Fenster
         * wird benachrichtigt, wenn der Button gedr�ckt wird. */
        suchenbutton.addActionListener(this);

        /* Einf�gen des Buttons in die Zeichenfl�che */
        cpane.add(suchenbutton);

        /* Erzeugen eines Dijkstra-Buttons */
        JButton suchenbutton2 = new JButton("Dijkstra");
        suchenbutton2.setBounds(250,350,100,20);

        /* Kommando, das beim Dr�cken �bermittelt wird */
        suchenbutton2.setActionCommand("Dijkstra");

        /* Setzt das aktuelle Fenster als Listener (d.h. das Fenster
         * wird benachrichtigt, wenn der Button gedr�ckt wird. */
        suchenbutton2.addActionListener(this);

        /* Einf�gen des Buttons in die Zeichenfl�che */
        cpane.add(suchenbutton2);

        /* Erzeugen der Schriftzuege */
        JLabel kdnr2_label = new JLabel("Start");
        kdnr2_label.setBounds(370,280,50,20);
        cpane.add(kdnr2_label);

        JLabel name2_label = new JLabel("Ende");
        name2_label.setBounds(490,280,50,20);
        cpane.add(name2_label);

        /* Erzeugen der Eingabezeilen */
        von_eingabe = new JTextField("");
        von_eingabe.setBounds(370,300,100,20);        
        cpane.add(von_eingabe);

        nach_eingabe = new JTextField("");
        nach_eingabe.setBounds(490,300,100,20);        
        cpane.add(nach_eingabe);

    }

    /** Eine Aktion wurde ausgeloest. Fenster reagiert entsprechend. */
    public void actionPerformed(ActionEvent event) {

        String kommando = event.getActionCommand();

        if (kommando.equals("Laden")) {
            String dateiname = datei_eingabe.getText();  
            ladeGraph(dateiname);
        }
        else if (kommando.equals("Tiefe")) {
            /*
            String start = start_eingabe.getText();
            GraphNode startnode = g.getNode(start);
            if (startnode==null) {
            ausgabe.setText("Knoten "+start+" nicht gefunden!");
            return;
            }
            testSort(start);
             */
            String start = start_eingabe.getText();
            GraphNode startnode = g.getNode(start);
            if (startnode==null) {
                ausgabe.setText("Knoten "+start+" nicht gefunden!");
                return;
            }
            ausgabe2.setText("Besuchte Knoten:\n");          
            g.resetMarks();
            sucheTief(start);

        }
        else if (kommando.equals("Breite")) {
            String start = start_eingabe.getText();
            GraphNode startnode = g.getNode(start);
            if (startnode==null) {
                ausgabe.setText("Knoten "+start+" nicht gefunden!");
                return;
            }
            ausgabe2.setText("Besuchte Knoten:\n");      
            g.resetMarks();
            sucheBreit(start);
        }
        else if (kommando.equals("Backtrack")) {
            String start = von_eingabe.getText();
            String ziel = nach_eingabe.getText();
            GraphNode vonKnoten = g.getNode(start);
            GraphNode nachKnoten = g.getNode(ziel);

            if (vonKnoten == null) {
                ausgabe.setText("Startknoten "+start+" nicht gefunden!");
                return;
            }          
            if (nachKnoten == null) {
                ausgabe.setText("Zielknoten "+ziel+" nicht gefunden!");
                return;
            }

            ausgabe2.setText("Suche einen Weg...\n");
            sucheWeg(vonKnoten,nachKnoten);
            ausgabe2.append("Fertig.\n");
        }
        else if (kommando.equals("Dijkstra")) {
            String start = von_eingabe.getText();
            GraphNode vonKnoten = g.getNode(start);

            if (vonKnoten == null) {
                ausgabe.setText("Startknoten "+start+" nicht gefunden!");
                return;
            }          

            starteDijkstra(start);
        }

    }

    /** Das Fenster kann direkt gestartet werden, da es die main-Methode
     *  implementiert. */
    public static void main(String[] args) {

        GraphGUIVorlage fenster = new GraphGUIVorlage();    
        fenster.setVisible(true);

    }

    public void testSort(String start){
        GraphNode startnode = g.getNode(start);
        List<GraphNode> l = g.getNeighbours(startnode);
        l = sortiereListe(l);
        l.toFirst();
        while(l.hasAccess()){
            System.out.println(l.getContent().getName());
            l.next();
        }
    }

    public List<GraphNode> sortiereListe(List<GraphNode> l) {

        if (l.isEmpty()) { return l; }

        // 1. Lege Pivotelement fest
        l.toFirst();
        GraphNode pivot = l.getContent();
        l.remove();

        // 2. Verteile auf linke und rechte Liste
        List<GraphNode> r = new List<GraphNode>();

        while (l.hasAccess()) {
            GraphNode aktuell = l.getContent();
            if (aktuell.getName().compareTo(pivot.getName())>0) { // aktuell>pivot
                r.append(aktuell);
                l.remove();
            }
            else {
                l.next();
            }
        }

        // 3. Selbstaufruf
        l = sortiereListe(l);
        r = sortiereListe(r);

        // 4. Verschmelzen
        l.append(pivot);
        l.concat(r);

        return l;
    }
}
