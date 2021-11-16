package bitimage.domain.analysis.entities;

public interface IFilter<I, O>
{
    O process(I input)
            throws Exception;
}
