
import java.util.*;

/**
 * Created by junle
 */
public class D2Dgraph {
    int V;
    List<Edge>[] adj;
    private double[] disTo;
    private boolean[] marked;
    private Door[] edgeTo;//父节点
    Set<Door> doors = new HashSet<>();
    public D2Dgraph(int V) {
        this.V = V;
        adj = (List<Edge>[]) new ArrayList[V+1];
        for (int v = 1; v <= V; v++) {
            adj[v] = new ArrayList<Edge>();
        }
    }
    public class Edge{
        double weight;
        Door d1;
        Door d2;

        public double getWeight() {
            return weight;
        }

        Edge(Door d1, Door d2, double weight){
            this.d1 = d1;
            this.d2 = d2;
            this.weight = weight;
        }

        Door from(){
            return d1;
        }

        Door to(){
            return d2;
        }

    }
    public void addEdge(Door d1, Door d2,double weight) {
        int label1 = d1.getLabel();
        int label2 = d2.getLabel();
        doors.add(d1);
        doors.add(d2);
        Edge edge1 = new Edge(d1,d2,weight);
        Edge edge2 = new Edge(d2,d1,weight);
        adj[label1].add(edge1);
        adj[label2].add(edge2);
    }

    public Comparator<Door> doorComparable = new Comparator<Door>() {

        @Override
        public int compare(Door o1, Door o2) {
            return (int) (disTo[o1.getLabel()]-disTo[o2.getLabel()]);
        }
    };

    public double shortestPath(Door d1 , Door d2){
        initDisTo();
        initMarked();
        initEdgeto();
        PriorityQueue<Door> pq = new PriorityQueue<>(doorComparable);
        pq.add(d1);
        disTo[d1.getLabel()] = 0;
        while (!pq.isEmpty()){
            Door door = pq.remove();
            if (door.equals(d2))
                return disTo[d2.getLabel()];
            marked[door.getLabel()] = true;
            for (Edge e : adj[door.getLabel()]){
                Door door1 = e.to();
                if (marked[door1.getLabel()])
                    continue;
                if (disTo[door1.getLabel()] > disTo[door.getLabel()] + e.weight){
                    disTo[door1.getLabel()] = disTo[door.getLabel()] + e.weight;
                    edgeTo[door1.getLabel()] = door;
                }
                pq.add(door1);
            }
        }

        return disTo[d2.getLabel()];
    }

    private void initDisTo(){
        disTo = new double[V+1];
        for (int i = 1; i <= V; i++){
            disTo[i] = Double.MAX_VALUE;
        }
    }

    private void initMarked(){
        marked = new boolean[V+1];
        for (int i = 0;i <= V;i++)
            marked[i] = false;
    }
    private void initEdgeto(){
        edgeTo = new Door[V+1];
    }

    public Stack<Door> SPath(Door d1,Door d2){
        if (d1.equals(d2)){
            Stack<Door> stack1 = new Stack<>();
            stack1.add(d1);
            return stack1;
        }
        shortestPath(d1, d2);
        Stack<Door> stack = new Stack<>();
        stack.add(d2);
        while (edgeTo[d2.getLabel()]!=d1){
            stack.add(edgeTo[d2.getLabel()]);
            d2 = edgeTo[d2.getLabel()];
        }
        stack.add(d1);
        return stack;
    }
    public void printSPath(Door d1,Door d2){
        Stack<Door> stack = SPath(d1, d2);
        System.out.print(stack.pop().label);
        while (!stack.isEmpty()){
            Door d = stack.pop();
            System.out.print("->");
            System.out.print(d.label);
        }
    }
    public Element getNextHopDoorAndDistance(Door d1,Door d2){
        double dis = shortestPath(d1, d2);
        Stack<Door> stack= SPath(d1, d2);
        if (stack.size()==2){
            return new Element(null,dis);
        }
        Door door = stack.pop();
        if (edgeTo[door.getLabel()]!=d1)
            door = stack.pop();
        return new Element(door,dis);
    }

}
