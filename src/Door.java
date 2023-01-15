import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by junle
 */
public class Door {
    boolean isAccessDoor;
    int label;    //门的编号
    boolean isMarked = false;
    boolean isGlobal = false;
    boolean isVisit = false;//用于路径分解，避免重复
    Set<Partition> pars = new HashSet<>(2);
    public Door(int label){
        this.label = label;
    }
    public Door(int label,boolean isAccessDoor){
        this.label = label;
        this.isAccessDoor = isAccessDoor;
    }
    public int getLabel() {
        return label;
    }

    public boolean isAccessDoor(){
        return isAccessDoor;
    }

}
