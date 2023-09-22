package net.vadamdev.jdautils.configuration;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author VadamDev
 * @since 18/10/2022
 */
public final class ConfigurationLoader {
    private ConfigurationLoader() {}

    public static void loadConfiguration(Configuration configuration) throws IOException, IllegalAccessException {
        final YamlFile yamlFile = configuration.getYamlFile();
        if(!yamlFile.exists())
            yamlFile.createNewFile();

        yamlFile.load();

        for (Field field : configuration.getClass().getDeclaredFields()) {
            for(Annotation annotation : field.getAnnotations()) {
                if(!(annotation instanceof ConfigValue))
                    continue;

                final String path = ((ConfigValue) annotation).path();

                if(yamlFile.isSet(path))
                    field.set(configuration, yamlFile.get(path));
                else
                    yamlFile.addDefault(path, field.get(configuration));
            }
        }

        yamlFile.save();
    }

    public static void loadConfigurations(Configuration... configurations) throws IOException, IllegalAccessException {
        for(Configuration configuration : configurations)
            loadConfiguration(configuration);
    }
}
