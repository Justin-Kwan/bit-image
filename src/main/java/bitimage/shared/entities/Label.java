package bitimage.shared.entities;

public class Label
        extends Entity
{
    private String name;
    private final EntityID imageID;

    private String contentCategory;
    private double confidenceScore;

    private Label(EntityID id, EntityID imageID, String name)
    {
        super(id);

        this.name = name;
        this.imageID = imageID;
    }

    public static Label CreateNew(EntityID imageID, String name)
    {
        EntityID id = EntityID.CreateNew();
        return new Label(id, imageID, name);
    }

    public void setContentCategory(String contentCategory)
    {
        this.contentCategory = contentCategory;
    }

    public void setConfidenceScore(double confidenceScore)
    {
        if (confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException(
                    "Label confidence score must be between 0% to 100%");
        }

        this.confidenceScore = confidenceScore;
    }

    public String getName()
    {
        return name;
    }

    public EntityID getImageID()
    {
        return imageID;
    }

    public String getContentCategory()
    {
        return contentCategory;
    }

    public double getConfidenceScore()
    {
        return confidenceScore;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
