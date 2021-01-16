package bitimage.domain.analysis.entities;

public interface IFilter<I, O> {
  public O process(I input) throws Exception;
}
