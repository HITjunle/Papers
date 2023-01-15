/**
 * Created by junle
 */
public class FromTo {
    Door from;
    Door to;
    private int hashCode;
    public FromTo(Door from,Door to){
        this.from = from;
        this.to = to;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FromTo that = (FromTo) o;
        return (from == that.from && to == that.to) || (from == that.to && to == that.from);
    }
    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
