import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by junle
 */
public class Main {
    static final int doorNum = 20;
    static final int parNum = 17;
    static final int y = 4;//判断是不是走廊
    public static void main(String[] args) throws FileNotFoundException{
        Map<Integer,Door> doorMap = new HashMap<>();//did和door对应
        D2Dgraph G = new D2Dgraph(doorNum);//建一个D2D图
        for (int i=1;i<=doorNum;i++){
            doorMap.put(i,new Door(i));
        }
        //读取D2D创建图
        Scanner input1 = new Scanner(new File("D2D.txt"));
        while (input1.hasNextLine()){
            String text1 = input1.nextLine();
            Scanner data1 = new Scanner(text1);
            Door d = doorMap.get(data1.nextInt());
            Door dd = doorMap.get(data1.nextInt());
            double weight = data1.nextDouble();
            G.addEdge(d,dd,weight);
        }
        //读取P2D,创建分区
        Map<Integer,Partition> parMap = new HashMap<>();
        parMap.put(0,new Partition(0));
        Scanner input2 = new Scanner(new File("P2D.txt"));
        while (input2.hasNextLine()){
            String text2 = input2.nextLine();
            Scanner data2 = new Scanner(text2);
            int pid= data2.nextInt();
            Partition p = new Partition(pid);
            parMap.put(pid,p);
            Set<Door> D = new LinkedHashSet<>();
            while (data2.hasNextInt()){
                D.add(doorMap.get(data2.nextInt()));
            }
            if (D.size()>y)
                p.isHallWays = true;
            p.doors = D;
        }
        //读取P2D图,获取门与那些分区相连，如果没有与分区相连就设置为0
        Scanner input3 = new Scanner(new File("D2P.txt"));
        //记录邻分区，便于合并
        Set<Partition>[] adjPar = new HashSet[parNum+1];
        for (int i = 1; i <= parNum; i++) {
            adjPar[i] = new HashSet<>();
        }
        while (input3.hasNextLine()){
            String text3 = input3.nextLine();
            Scanner data3 = new Scanner(text3);
            int did= data3.nextInt();
            Door d = doorMap.get(did);
            int pid1 = data3.nextInt();
            int pid2 = data3.nextInt();
            Partition p1 = parMap.get(pid1);
            Partition p2 = parMap.get(pid2);
            if (pid1!=0)
                adjPar[pid1].add(p2);
            if (pid2!=0)
                adjPar[pid2].add(p1);
            d.pars.add(p1);
            d.pars.add(p2);
            d.isAccessDoor = pid1 == 0 || pid2 == 0;
        }
        Partition S = parMap.get(1);
        Partition T = parMap.get(17);
        VipTree vipTree = new VipTree(G,doorMap,parMap,adjPar,S,T);//查询点s所在分区S，查询点t所在分区T
        Door d3 = doorMap.get(2);
        Door d4 = doorMap.get(20);
        System.out.println("Dijkstra shortest path between d"+
                d3.label+" and d"+d4.label+" is "+G.shortestPath(d3,d4));//任意点的距离
        System.out.println("vip tree shortest path between d"+
                d3.label+" and d"+d4.label+" is "+vipTree.findArbitraryDis(d3,d4,S,T));//计算s和t在不同叶节点
        vipTree.printPath(d3,d4);//debug和run结果不一样
    }

}
