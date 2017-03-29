package org.jboss.windup.tooling.data;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class IssueCategoryImpl implements IssueCategory
{
    private static final long serialVersionUID = 1L;

    private String categoryID;
    private String origin;
    private String name;
    private String description;
    private Integer priority;

    public IssueCategoryImpl(String categoryId, String origin, String name, String description, Integer priority)
    {
        this.categoryID = categoryId;
        this.origin = origin;
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    @Override
    public String getCategoryID()
    {
        return categoryID;
    }

    @Override
    public String getOrigin()
    {
        return origin;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Integer getPriority()
    {
        return priority;
    }
}
