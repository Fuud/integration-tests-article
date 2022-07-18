package fuud.test.infra;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fuud.test.infra.components.Component;
import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EnvStarter {
    @FunctionalInterface
    public interface TestBlock {
        void performTest(Cloud cloud) throws Exception;
    }

    public static void integrationTest(TestBlock block) {
        Cloud cloud = CloudFactory.createCloud();
        try {
            block.performTest(cloud);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cloud.shutdown();
        }
    }

    public static void env(Cloud cloud, Component<?>... components) {
        printConfigsToConsole(components);

        for (Component<?> component : components) {
            component.start(cloud, Arrays.asList(components));
        }
    }

    private static void printConfigsToConsole(Component<?>[] components) {
        ObjectWriter objectWriter = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .writerWithDefaultPrettyPrinter();

        Map<String, Object> configMap = new HashMap<>();
        for (Component<?> component : components) {
            configMap.put(component.getClass().getSimpleName(), component.getConfig());
        }

        try {
            System.out.println(objectWriter.writeValueAsString(configMap));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
