package bitimage.domain.analysis.entities;

import bitimage.domain.sharedkernel.entities.Label;
import bitimage.regex.RegexPatterns;
import java.util.List;
import java.util.stream.Collectors;

public class CleanImageLabels implements IFilter<List<Label>, List<Label>> {

  final int ROUND_DECIMAL_SPACES = 4;

  public List<Label> process(List<Label> labels) {
    return labels.stream()
        .map(this::cleanLabelName)
        .map(this::roundLabelConfidenceScore)
        .collect(Collectors.toList());
  }

  private Label cleanLabelName(Label label) {
    final String cleanedName =
        label.getName().toLowerCase().replaceAll(RegexPatterns.ALPHA_NUMERIC_SPACE, "");

    label.setName(cleanedName);

    return label;
  }

  private Label roundLabelConfidenceScore(Label label) {
    final double roundedConfidenceScore =
        this.roundDecimalPlaces(label.getConfidenceScore(), this.ROUND_DECIMAL_SPACES);

    label.setConfidenceScore(roundedConfidenceScore);

    return label;
  }

  private double roundDecimalPlaces(double num, int places) {
    double scale = Math.pow(10, places);
    return Math.round(num * scale) / scale;
  }
}
