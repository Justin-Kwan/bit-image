package bitimage.analysis.entities;

public interface ImageFilter<I, O>
{
    O process(I input)
            throws Exception;
}
