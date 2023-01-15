import java.util.*;

/**
 * Created by junle
 */
public class VipTree {
    D2Dgraph G;
    Map<Integer,Node[]> nodesMap;//一个level对应一行的节点
    Map<Integer,Partition> parMap;
    Node[] nodes1;//叶子节点
    int pos; //查询Partition S对应叶节点的位置(暂时没用)，映射到叶节点
    Partition S;
    Door midDoori,midDoorj;
    Map<FromTo,Door> midDoorMap = new HashMap<>();
    final static int t = 2;// t be the minimum degree of the IP-Tree denoting the minimum number of children in each
                           //non-root node

    VipTree(D2Dgraph d2Dgraph, Map<Integer,Door> doorMap,Map<Integer,Partition> parMap,Set<Partition>[] adjPar,
            Partition S,Partition T){
        G = d2Dgraph;
        this.parMap = parMap;
        this.S = S;
        int level = 1;
        Set<Door[]> doors = new LinkedHashSet<>();
        nodes1 = createLeafNode(parMap,adjPar);//获得叶子节点
        nodesMap = new HashMap<>();
        nodesMap.put(1,nodes1);
        int i = 1;
        while (nodesMap.get(i).length>1) {
            createNextLevel(nodesMap.get(i), t);
            i+=1;
        }
        Map<FromTo,Double> map = getDistances(doorMap.get(2),nodesMap.get(3)[0],S);
    }

     class Node{
        int level;
        int degree;
        boolean isVisit = false;//在create next level中找最多公共门时候用到
        Set<Door> accessDoors;
        Set<Partition> pSet;
        Node parent = null;
        Map<FromTo,Element> distanceMap;
        boolean isLeafNode = false;
        Set<Door> doors;
        Node(int l){
            level = l;
            degree = 1;
        }
        //叶子节点
        Node(int l,Set<Door> doors,Set<Partition> parS,boolean isLeafNode){
            degree = 1;
            this.doors = doors;//叶节点包含的所有门
            this.isLeafNode = isLeafNode;
            this.pSet = parS;//叶节点包含的所有分区
            Set<Door> aDoor = new HashSet<>();
            for (Door door:doors){
                if (door.isAccessDoor)
                    aDoor.add(door);
            }
            this.accessDoors = aDoor;//叶节点包含的所有access door
            distanceMap = new HashMap<>();//叶节点的距离矩阵，用map表示
            for (Door Ad:accessDoors)
                for (Door d:doors){
                    FromTo Ds = new FromTo(Ad,d);
                    if (Ad.equals(d)){
                        distanceMap.put(Ds,new Element(null,0));
                        continue;
                    }
                    Element E = G.getNextHopDoorAndDistance(Ad,d);
                    distanceMap.put(Ds,E);
                }
        }
    }

    public Node[] createLeafNode(Map<Integer,Partition> parMap,Set<Partition>[] adjPar){
        List<Node> nodeList = new ArrayList<>();
        for (int i=1;i < parMap.size();i++){
            Set<Partition> mergePartitions = new LinkedHashSet<>();
            Partition h = parMap.get(i);
            if (h.isHallWays){
                mergePartitions.add(h);    //感觉合并有更多公共门的走廊和分区复杂度很高。。。
                Set<Door> doors = new HashSet<>(h.doors);
                if (S.equals(h))
                    pos = nodeList.size();
                for (Partition P:adjPar[i]){
                    if (!P.isVisit&&!P.isHallWays&&P.pid!=0){
                        mergePartitions.add(P);
                        if (S.equals(P))
                            pos = nodeList.size();
                        P.isVisit = true;
                        doors.addAll(P.doors);
                    }
                }
                ////获得连接两个节点的access door,前面已经标记的access door是连接外面的
                for (Door door:doors){
                    Iterator<Partition> partitionIterator= door.pars.iterator();
                    Partition p1 = partitionIterator.next();
                    Partition p2 = partitionIterator.next();
                    if (!mergePartitions.contains(p1)||!mergePartitions.contains(p2)){
                        door.isAccessDoor = true;
                    }
                }
                nodeList.add(new Node(1,doors,mergePartitions,true));
            }
        }

        Node[] nodes1 = new Node[nodeList.size()];
        nodeList.toArray(nodes1);
        return nodes1;

    }

    public void createNextLevel(Node[] nodes, int t){
        Comparator<Node> nodeComparable = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.degree-o2.degree;
            }
        };
        PriorityQueue<Node> H = new PriorityQueue<>(nodeComparable);
        for (Node node : nodes) {
            H.add(node);
            node.degree = 1;
        }
        while (true){
            assert H.peek() != null;
            if (!(H.peek().degree < t)) break;
            Node N_i = H.remove();
            Node N_j = findHighestNumberCommonAccessDoor(N_i,nodes);
            H.remove(N_j);
            Node N_k = merge(N_i,N_j);
            H.add(N_k);
        }
        Node[] NL = new Node[H.size()];
        int j = 0;
        for (Node N:H){
            NL[j] = N;
            j+=1;
        }
        nodesMap.put(NL[0].level+1,NL);
    }

    private Node findHighestNumberCommonAccessDoor(Node N,Node[] nodes){
        Set<Door> D1 = N.accessDoors;
        N.isVisit = true;
        Node n = null;
        int maxInsertNum = 0;
        for (Node node:nodes){
            if (!node.isVisit){
                Set<Door> D = new HashSet<>(node.accessDoors);
                D.retainAll(D1);
                if (maxInsertNum < D.size()){
                    maxInsertNum = D.size();
                    n = node;
                }
            }
        }
        n.isVisit = true;
        return n;
    }

    public Node merge(Node N1,Node N2){
        Node node = new Node(N1.level+1);
        Set<Door> accessDoors = new HashSet<>();
        Set<Door> insertDoors = new HashSet<>();
        for (Door door:N1.accessDoors){
            for (Door door1:N2.accessDoors){
                accessDoors.add(door);
                accessDoors.add(door1);
                if (door1.equals(door))
                    insertDoors.add(door1);
            }
        }

        Map<FromTo,Element> distanceMap = new HashMap<>();
        for (Door door1:accessDoors)
            for (Door door2:accessDoors){
                FromTo Ds = new FromTo(door1,door2);
                if (door1.equals(door2)){
                    distanceMap.put(Ds,new Element(null,0));
                    continue;
                }
                Element E = G.getNextHopDoorAndDistance(door1,door2);
                distanceMap.put(Ds,E);
            }

        for (Door d:insertDoors){
            accessDoors.remove(d);
        }
        node.accessDoors = accessDoors;//l+1 level的access door
        node.distanceMap = distanceMap;
        node.degree = N1.degree+ N2.degree;
        N1.parent = node;
        N2.parent = node;
        return node;
    }

    public boolean isSuperior(Door d,Partition p,Node Leaf){
        Set<Door> globalDoor = new HashSet<>();
        for (Door D:Leaf.accessDoors){
            if (d.equals(D)&&p.doors.contains(d))
                return true;// local access door
            if (!D.isGlobal&&!p.doors.contains(D))
                D.isGlobal = true;//global access door
        }
        for (Door door:Leaf.accessDoors){
            if (door.isGlobal){
                Door nextHopDoor = Leaf.distanceMap.get(new FromTo(d,door)).nextHopDoor;
                if (nextHopDoor==null||!p.doors.contains(nextHopDoor))
                    return true;
            }
        }
        return false;
    }

    //Shortest distance between s and an access door d that is in AD(Leaf(s)).

    public double getDis(Door s,Door d,Partition S){
        double min = Double.MAX_VALUE;
        Node Leaf = getLeafNode(s);
        Door mid = null;
        for(Door di:S.doors){
            if (isSuperior(di,S,Leaf)){
                double dis1 = G.shortestPath(s,di);
                double dis2 = Leaf.distanceMap.get(new FromTo(di,d)).distance;
                if (min > dis1+dis2){
                    min = dis1+dis2;
                }
            }
        }
        return min;
    }

    public Node getLeafNode(Door s){
        for (Node node:nodes1){
            if (node.doors.contains(s))
                return node;
        }
        return null;
    }

//Shortest distance between s and all access doors of an ancestor of Leaf(s).
    public Map<FromTo,Double> getDistances(Door s,Node N,Partition S){
        //N is an ancestor node of Leaf(s)
        Node ns = getLeafNode(s);//Leaf(s)
        Node nParent =  ns.parent;
        Node nChild = ns;
        Map<FromTo,Double> m = new HashMap<>();
        Set<Door> markDoor = new HashSet<>();
        Door mid = null;
        while(!nChild.equals(N)){
            for (Door d: nParent.accessDoors){
                if (!d.isMarked){
                    double min = Double.MAX_VALUE;
                    for (Door d1:nChild.accessDoors){
                        double dis1,dis2;
                        if (m.containsKey(new FromTo(s,d1)))
                            dis1 = m.get(new FromTo(s,d1));
                        else
                            dis1 = getDis(s,d1,S);
                        if (m.containsKey(new FromTo(d1,d)))
                            dis2 = m.get(new FromTo(d1,d));
                        else
                            dis2 = nParent.distanceMap.get(new FromTo(d1,d)).distance;
                        double dis = dis1+dis2;
                        if (dis < min){
                            min = dis;
                            mid = d1;
                        }
                    }
                    m.put(new FromTo(s,d),min);
                    if (!mid.equals(s)&&!mid.equals(d))
                        midDoorMap.put(new FromTo(s,d),mid);
                    d.isMarked = true;
                    markDoor.add(d);
                }
            }
            for (Door door:markDoor)
                door.isMarked = false;
            nChild = nParent;
            nParent = nParent.parent;
        }
        return m;
    }




//Shortest distance between two arbitrary points s and t
//dist(s, t) when s and t are in different leaf nodes
    public double findArbitraryDis(Door s,Door t,Partition S,Partition T){
        Node Ns = getLeafNode(s);
        Node Nt = getLeafNode(t);
        if (Ns.equals(Nt))
            return getDis(s,t,S);
        Node Lca = LCA(Ns,Nt);
        while (!Ns.parent.equals(Lca))
            Ns = Ns.parent;
        while (!Nt.parent.equals(Lca))
            Nt = Nt.parent;
        Map<FromTo,Double> m1 = getDistances(s,Ns,S);
        Map<FromTo,Double> m2 = getDistances(t,Nt,T);
        double min = Double.MAX_VALUE;
        // 当NS=Leaf(s),Nt = Leaf(t)
        if (m1.isEmpty()&&m2.isEmpty()){
            for (Door di:Ns.accessDoors)
                for (Door dj:Nt.accessDoors){
                    double dis1 = getDis(s,di,S);
                    double dis2 = Lca.distanceMap.get(new FromTo(di,dj)).distance;
                    double dis3 = getDis(t,dj,T);
                    double dis = dis1+dis2+dis3;
                    if (dis<min){
                        min = dis;
                        midDoori = di;
                        midDoorj = dj;
                    }
                }

        }
        else {
            for (Door di : Ns.accessDoors)
                for (Door dj : Nt.accessDoors) {
                    double dis1 = m1.get(new FromTo(s, di));
                    double dis2 = Lca.distanceMap.get(new FromTo(di, dj)).distance;
                    double dis3 = m2.get(new FromTo(t, dj));
                    double dis = dis1 + dis2 + dis3;
                    if (dis<min){
                        min = dis;
                        midDoori = di;
                        midDoorj = dj;
                    }
                }
        }

        return min;
    }

    public void printPath(Door s,Door t){
        Set<Door> doorSet = new LinkedHashSet<>();
        Door d1 = midDoorMap.get(new FromTo(s,midDoori));
        Door d2 = midDoorMap.get(new FromTo(midDoorj,t));
        if (d1==null){
            doorSet = Decompose(s,midDoori,doorSet);
        }
        else {
            doorSet = Decompose(s, d1, doorSet);
            doorSet = Decompose(d1, midDoori, doorSet);
        }
        if (!midDoori.equals(midDoorj))
            doorSet = Decompose(midDoori,midDoorj,doorSet);
        if (d2==null){
            doorSet = Decompose(midDoorj,t,doorSet);
        }
        else {
            doorSet = Decompose(midDoorj, d2, doorSet);
            doorSet = Decompose(d2, t, doorSet);
        }

        System.out.print("the path :");
        for (Door door:doorSet)
            System.out.print("d"+door.label+" ");
        System.out.println();
    }

    //LCA(s, t) be the lowest common ancestor node
    //of Leaf(s) and Leaf(t).
    private Node LCA(Node s,Node t){
        while (!s.equals(t)){
            s = s.parent;
            t = t.parent;
        }
        return s;
    }
    //judge whether N is an ancestor of n
    private boolean isAncestor(Node n,Node N){
        while (n.parent!=null){
            if (n.parent.equals(N))
                return true;
        }
        return false;
    }

    //分解di和dj的路径
    public Set<Door> Decompose(Door di,Door dj,Set<Door> route){
        //判断是不是在同一叶节点，如果是直接算
        if (isInTheSameAccessDoor(di,dj)){
            Stack<Door> doorStack = G.SPath(di,dj);
            while (!doorStack.isEmpty()){
                route.add(doorStack.pop());
            }
            return route;
        }
        //不在同一叶节点，或者在同一叶节点，但是该叶节点的access door
        if (di.equals(dj))
            route.add(di);
        else {
            if (!di.isAccessDoor && !dj.isAccessDoor) {
                route.add(di);
                route.add(dj);
            } else {
                Node N = null;
                if (di.isAccessDoor && dj.isAccessDoor) {
                    Node ni = getLeafNode(di);
                    Node nj = getLeafNode(dj);
                    N = LCA(ni, nj);
                } else {
                    for (Node node : nodes1) {
                        if (node.doors.contains(di) && node.doors.contains(dj))
                            N = node;
                    }
                }
                if (N!=null&&!N.distanceMap.containsKey(new FromTo(di,dj))){
                    route.add(di);
                    route.add(dj);
                }
                else if (N!=null){
                    Door dk = N.distanceMap.get(new FromTo(di, dj)).nextHopDoor;
                    if (dk == null) {
                        route.add(di);
                        route.add(dj);
                    } else {
                        route = Decompose(di, dk, route);
                        route = Decompose(dk, dj, route);
                    }
                }
            }
        }
        return route;
    }
    private boolean isInTheSameAccessDoor(Door di,Door dj){
        Node i = getLeafNode(di);
        Node j = getLeafNode(dj);
        if (i.equals(j))
            return true;
        else {
            for (Partition partition:dj.pars)
                if (di.pars.contains(partition))
                    return true;
        }
        return false;
    }



}

