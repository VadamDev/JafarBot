package net.vadamdev.jdautils.configuration;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author VadamDev
 * @since 18/10/2022
 */
public final class ConfigurationLoader {
    private ConfigurationLoader() {}

    /**
     * Load a configuration
     * @param configuration The configuration that needs to be loaded
     */
    public static void loadConfiguration(Configuration configuration) throws IOException, IllegalAccessException {
        final YamlFile yamlFile = configuration.getYamlFile();
        if(!yamlFile.exists())
            yamlFile.createNewFile();

        yamlFile.load();

        for (Field field : configuration.getClass().getDeclaredFields()) {
            final ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if(annotation == null)
                continue;

            final String path = annotation.path();
            if(yamlFile.isSet(path))
                field.set(configuration, yamlFile.get(path));
            else
                yamlFile.addDefault(path, field.get(configuration));
        }

        configuration.save();
    }

    /**
     * Load multiple configurations
     * @param configurations The configurations that need to be loaded
     */
    public static void loadConfigurations(Configuration... configurations) throws IOException, IllegalAccessException {
        for(Configuration configuration : configurations)
            loadConfiguration(configuration);
    }
}
