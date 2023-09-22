package net.vadamdev.jdautils.configuration;

import org.simpleyaml.configuration.file.YamlFile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Represents a configuration file.
 * <br>Every primitive field annotated with {@link ConfigValue} will be configurable in the associated yml file.
 *
 * @author VadamDev
 * @since 18/10/2022
 */
public class Configuration {
    private final YamlFile yamlFile;

    public Configuration(@Nonnull String filePath) {
        this.yamlFile = new YamlFile(filePath);
    }

    /**
     * Change the provided value in the associated yml and field.
     * <br>Use the save() function to save the changes in the yml file.
     *
     * @param name Field name
     * @param value New value
     */
    public void setValue(@Nonnull String name, Object value) {
        try {
            final Field field = getClass().getField(name);

            for (Annotation annotation : field.getAnnotations()) {
                if(annotation instanceof ConfigValue) {
                    field.set(this, value);
                    yamlFile.set(((ConfigValue) annotation).path(), value);
                    return;
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the provided fieldName exists.
     *
     * @param fieldName Field name to check
     * @return True if the provided field name is a field
     */
    public boolean hasField(@Nonnull String fieldName) {
        try {
            return Arrays.stream(getClass().getField(fieldName).getAnnotations())
                    .anyMatch(ConfigValue.class::isInstance);
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }

    /**
     * Save current changes in the yml file.
     *
     * @throws IOException
     */
    public void save() throws IOException {
        yamlFile.save();
    }

    public YamlFile getYamlFile() {
        return yamlFile;
    }
}
