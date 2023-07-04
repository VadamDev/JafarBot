package net.vadamdev.jdautils.configuration;

import org.simpleyaml.configuration.file.YamlFile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author VadamDev
 * @since 18/10/2022
 */
public class Configuration {
    private final YamlFile yamlFile;

    public Configuration(@Nonnull String filePath) {
        this.yamlFile = new YamlFile(filePath);
    }

    public YamlFile getYamlFile() {
        return yamlFile;
    }

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

    public boolean hasField(@Nonnull String fieldName) {
        try {
            return Arrays.stream(getClass().getField(fieldName).getAnnotations())
                    .anyMatch(ConfigValue.class::isInstance);
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }

    public void save() throws IOException {
        yamlFile.save();
    }
}
