package bitimage.shared.entities;

public class NullImage
        extends Image
{

    public NullImage()
    {
        super(new Builder(null, null, null));
    }

    public boolean isNull()
    {
        return true;
    }
}
