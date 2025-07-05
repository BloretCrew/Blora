package blora.plugin.library;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

public class BloraLibraryLoader implements PluginLoader {

    private final static List<String> LIBRARIES = List.of(
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2",
            "org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1",
            "net.peanuuutz.tomlkt:tomlkt-jvm:0.4.0",
            "net.benwoodworth.knbt:knbt:0.11.8",
            "plutoproject.adventurekt:core:2.1.1",
            "org.jetbrains.exposed:exposed-core:0.61.0",
            "org.jetbrains.exposed:exposed-jdbc:0.61.0",
            "org.jetbrains.exposed:exposed-dao:0.61.0",
            "org.jetbrains.exposed:exposed-java-time:0.61.0"
    );

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "plutoproject",
                        "default",
                        "https://maven.nostal.ink/repository/maven-public"
                ).build()
        );
        for (String dependency : LIBRARIES) {
            resolver.addDependency(
                    new Dependency(
                            new DefaultArtifact(dependency),
                            null
                    )
            );
        }

        classpathBuilder.addLibrary(resolver);
    }

}
