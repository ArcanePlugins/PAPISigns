package com.mrivanplays.papisigns.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class PapiSignsLoader implements PluginLoader {

  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    Manifest manifest;
    try {
      var jar = new JarFile(classpathBuilder.getContext().getPluginSource().toFile());
      manifest = jar.getManifest();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    var cloudVersion = manifest.getMainAttributes().getValue("cloudVersion");
    var annotatedConfigVersion = manifest.getMainAttributes().getValue("acVersion");

    var resolver = new MavenLibraryResolver();
    resolver.addRepository(
        new RemoteRepository.Builder(
                "ivan", "default", "https://repo.mrivanplays.com/repository/ivan/")
            .build());
    resolver.addRepository(
        new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2")
            .build());

    resolver.addDependency(
        new Dependency(
            new DefaultArtifact(
                "com.mrivanplays:annotationconfig-yaml:%s".formatted(annotatedConfigVersion)),
            null));
    resolver.addDependency(
        new Dependency(
            new DefaultArtifact(
                "cloud.commandframework:cloud-paper:%s".formatted(cloudVersion)),
            null));
    resolver.addDependency(
        new Dependency(
            new DefaultArtifact(
                "cloud.commandframework:cloud-minecraft-extras:%s".formatted(cloudVersion)),
            null));

    classpathBuilder.addLibrary(resolver);
  }
}
