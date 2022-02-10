package com.carson.click

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 *  修改字节码
 */
class DoubleClickClassVisitor extends ClassVisitor implements Opcodes {

    DoubleClickClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor =  super.visitMethod(access, name, descriptor, signature, exceptions)

        String nameDesc = name + descriptor

        methodVisitor = new AdviceAdapter(Opcodes.ASM6, methodVisitor, access, name, descriptor) {

            @Override
            void visitInvokeDynamicInsn(String name2, String descriptor2, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()
            }

            @Override
            void visitEnd() {
                super.visitEnd()
            }
        }

        return methodVisitor
    }

}
