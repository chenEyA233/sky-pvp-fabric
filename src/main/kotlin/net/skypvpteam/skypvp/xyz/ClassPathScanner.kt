package net.skypvpteam.skypvp.xyz

import java.io.File
import java.net.URL
import java.util.jar.JarFile
import kotlin.reflect.KClass

object ClassPathScanner {
    fun getClasses(packageName: String, classLoader: ClassLoader): List<KClass<*>> {
        val path = packageName.replace('.', '/')
        val resources = classLoader.getResources(path)
        val classes = mutableListOf<KClass<*>>()

        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            when (resource.protocol) {
                "file" -> processDirectory(File(resource.file), packageName, classes)
                "jar" -> processJar(resource, packageName, classes)
            }
        }

        return classes
    }

    private fun processDirectory(directory: File, packageName: String, classes: MutableList<KClass<*>>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                processDirectory(file, "$packageName.${file.name}", classes)
            } else if (file.name.endsWith(".class")) {
                val className = packageName + '.' + file.name.substring(0, file.name.length - 6)
                try {
                    classes.add(Class.forName(className).kotlin)
                } catch (e: ClassNotFoundException) {
                    // Ignore
                }
            }
        }
    }

    private fun processJar(resource: URL, packageName: String, classes: MutableList<KClass<*>>) {
        val jarPath = resource.path.substring(5, resource.path.indexOf("!"))
        val jarFile = JarFile(jarPath)
        val entries = jarFile.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.name.endsWith(".class")) {
                val className = entry.name.replace('/', '.').substring(0, entry.name.length - 6)
                if (className.startsWith(packageName)) {
                    try {
                        classes.add(Class.forName(className).kotlin)
                    } catch (e: ClassNotFoundException) {
                        // Ignore
                    }
                }
            }
        }
    }
}