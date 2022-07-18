package fuud.test.infra;

import fuud.test.infra.components.Component;
import org.gridkit.nanocloud.Cloud;

import java.util.Arrays;

public class EnvStarter {
    public static void env(Cloud cloud, Component... components) {
        for (Component component : components) {
            component.start(cloud, Arrays.asList(components));
        }
    }
}
