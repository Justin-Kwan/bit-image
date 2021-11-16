package bitimage.analysis.entities;

import bitimage.shared.entities.Label;
import bitimage.regexp.RegexPatterns;

import java.util.List;
import java.util.stream.Collectors;

public class ImageLabelCleaner
        implements ImageFilter<List<Label>, List<Label>>
{
    private final static int ROUND_DECIMAL_SPACES = 4;

    public List<Label> process(List<Label> labels)
    {
        return labels.stream()
                .map(this::cleanLabelName)
                .map(this::roundLabelConfidenceScore)
                .collect(Collectors.toList());
    }

    private Label cleanLabelName(Label label)
    {
        String cleanedName = label
                .getName()
                .toLowerCase()
                .replaceAll(RegexPatterns.ALPHA_NUMERIC_SPACE, "");

        label.setName(cleanedName);
        return label;
    }

    private Label roundLabelConfidenceScore(Label label)
    {
        double roundedConfidenceScore = roundDecimalPlaces(
                label.getConfidenceScore(),
                ROUND_DECIMAL_SPACES);

        label.setConfidenceScore(roundedConfidenceScore);
        return label;
    }

    private double roundDecimalPlaces(double num, int places)
    {
        double scale = Math.pow(10, places);
        return Math.round(num * scale) / scale;
    }
}
