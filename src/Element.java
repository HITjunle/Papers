/**
 * Created by junle
 */
// the element of the distance matrix
public class Element {
    Door nextHopDoor;
    double distance;
    public Element(Door nextHopDoor,double dis){
        this.nextHopDoor = nextHopDoor;
        distance = dis;
    }
}
