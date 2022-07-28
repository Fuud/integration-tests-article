package fuud.test.infra;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fuud.test.infra.components.Component;
import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.nanocloud.VX;
import org.gridkit.vicluster.ViNode;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EnvStarter {
    public static final boolean isDebugEnabled = detectIsDebugEnabled();

    @FunctionalInterface
    public interface NodeProvider {
        Node getNode(String name, Component.BaseComponentConfig config);
    }


    @FunctionalInterface
    public interface TestBlock {
        void performTest(NodeProvider nodeProvider) throws Exception;
    }

    public static void integrationTest(TestBlock block) {
        Cloud cloud = CloudFactory.createCloud();
        applyCommonJvmArgs(cloud);
        NodeProvider nodeProvider = (name, config) -> {
            Node node = new Node(cloud, name);
            if (isDebugEnabled) {
                node.x(VX.JVM).addJvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:" + config.debugPort);
            }
            return node;
        };
        try {
            block.performTest(nodeProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cloud.shutdown();
        }
    }

    private static void applyCommonJvmArgs(Cloud cloud) {
        ViNode allNodes = cloud.node("**");
        allNodes.x(VX.JVM).addJvmArg("-XX:TieredStopAtLevel=1");
        allNodes.x(VX.JVM).addJvmArg("-Xverify:none");
    }

    public static void env(NodeProvider cloud, Component<?>... components) {
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

    // copied from https://stackoverflow.com/a/71375505
    private static boolean detectIsDebugEnabled() {
        ThreadInfo[] infos = ManagementFactory.getThreadMXBean()
                .dumpAllThreads(false, false, 0);
        for (ThreadInfo info : infos) {
            if ("JDWP Command Reader".equals(info.getThreadName())) {
                return true;
            }
        }
        return false;
    }
}
