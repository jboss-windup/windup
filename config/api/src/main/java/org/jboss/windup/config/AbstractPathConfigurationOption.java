package org.jboss.windup.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a base for validating {@link ConfigurationOption}s of type {@link File}. This uses the results of {@link ConfigurationOption#getUIType()}
 * to determine whether to validate as a file or as a directory.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public abstract class AbstractPathConfigurationOption extends AbstractConfigurationOption
{
    private boolean mustExist = false;

    /**
     * If mustExist is set to true, then the path will fail to validate if it does not already exist.
     */
    protected AbstractPathConfigurationOption(boolean mustExist)
    {
        this.mustExist = mustExist;
    }

    @Override
    public Class<?> getType()
    {
        return File.class;
    }

    protected File castToFile(Object file)
    {
        if (file instanceof Path)
            return ((Path) file).toFile();
        else
            return (File) file;
    }

    @Override
    public ValidationResult validate(Object fileObject)
    {
        if (fileObject == null && isRequired())
        {
            return new ValidationResult(ValidationResult.Level.ERROR, getName() + " is required!");
        }
        else if (fileObject == null)
        {
            return ValidationResult.SUCCESS;
        }

        if (fileObject instanceof Iterable && !(fileObject instanceof Path))                                  // path isn't the type of iterable we
                                                                                                              // are
        // looking
        // for
        {
            for (Object listItem : (Iterable) fileObject)
            {
                // FIXME: "Recursion" into overriden method is an antipattern and can lead to SOEx.
                ValidationResult result = validate(listItem);
                if (result.getLevel() != ValidationResult.Level.SUCCESS)
                    return result;
            }
            return ValidationResult.SUCCESS;
        }

        File file = castToFile(fileObject);
        Path path = file.toPath();
        if (mustExist)
        {
            if (getUIType() == InputType.DIRECTORY && !Files.isDirectory(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist and be a directory!");
            }
            else if (getUIType() == InputType.FILE && !Files.isRegularFile(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist and be a regular file!");
            }
            else if (getUIType() == InputType.FILE_OR_DIRECTORY && !Files.exists(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist!");
            }
            else if (!Files.exists(path))
            {
                return new ValidationResult(ValidationResult.Level.ERROR, getName() + " must exist!");
            }
        }
        return ValidationResult.SUCCESS;
    }
}
