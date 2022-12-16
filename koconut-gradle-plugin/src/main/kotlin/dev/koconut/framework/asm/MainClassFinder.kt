package dev.koconut.framework.asm

import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import java.io.File
import java.io.InputStream

object MainClassFinder {

    private const val MAIN_METHOD_NAME = "main"
    private val MAIN_METHOD_TYPE = Type.getMethodType(Type.VOID_TYPE, Type.getType(Array<String>::class.java))

    fun mainClasses(rootDirectory: File): Set<String> =
        rootDirectory
            .walkTopDown()
            .onEnter { !it.name.startsWith(".") }
            .filter { it.name.endsWith(".class") }
            .map { file -> file.inputStream().use { createClassDescriptor(it) } }
            .filter { it.isMainMethod }
            .map { it.className }
            .toSet()

    private fun createClassDescriptor(stream: InputStream): ClassDescriptor =
        with(ClassReader(stream)) {
            ClassDescriptor(className).also { accept(it, ClassReader.SKIP_CODE) }
        }

    private class ClassDescriptor(
        val className: String
    ) : ClassVisitor(Opcodes.ASM9) {
        var isMainMethod: Boolean = false

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            if (
                (access and Opcodes.ACC_PUBLIC) != 0 &&
                (access and Opcodes.ACC_STATIC) != 0 &&
                MAIN_METHOD_NAME.equals(name) &&
                MAIN_METHOD_TYPE.descriptor.equals(descriptor)
            ) {
                isMainMethod = true
            }
            return null
        }
    }
}