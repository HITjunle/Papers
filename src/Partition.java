import java.util.Set;

/**
 * Created by junle
 */
public class Partition {
    Set<Door> doors;
    boolean isVisit = false;
    VipTree.Node Leaf;
    boolean isHallWays = false;
    int pid;

    Partition(Set<Door> doors,int pid) {
        this.doors = doors;
        this.pid = pid;
    }

    Partition(int pid) {
        this.pid = pid;
    }

}
