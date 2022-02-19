package com.carson.click

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 *  修改字节码
 */
class DoubleClickClassVisitor extends ClassVisitor implements Opcodes {

    private static final String PATH = "com/carson/clickdouble/TestUtils"

    private static final String DESC = '(Landroid/view/View;)V'
    private static final String PARENT = 'Landroid/view/View$OnClickListener;'
    private static final String NAME = 'onClick'

    private String[] mInterfaces

    private List<String> mLambdaNames = new ArrayList<>()

    DoubleClickClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        println "DoubleClickClassVisitor"
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mInterfaces = interfaces
        println "visit--interfaces.size=" + interfaces.size()
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        println "MethodVisitor"
        MethodVisitor methodVisitor =  super.visitMethod(access, name, descriptor, signature, exceptions)

        String nameDesc = name + descriptor

        methodVisitor = new DefaultMethodVisitor(methodVisitor, access, name, descriptor) {

            @Override
            void visitInvokeDynamicInsn(String name2, String descriptor2, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)
                /**
                 * +++++++++++++++++++++++++++++++++++++++++++++++
                 * desc3===>(Landroid/view/View;)V
                 * descriptor2===>Landroid/view/View$OnClickListener;
                 * name2===>onClick
                 * handle.name===>lambda$onCreate$0
                 * handle.desc===>(Landroid/view/View;)V
                 * +++++++++++++++++++++++++++++++++++++++++++++++
                 */
                try {
                    println("+++++++++++++++++++++++++++++++++++++++++++++++")
                    String desc3 = (String) bootstrapMethodArguments[0]
                    String parent = Type.getReturnType(descriptor2).getDescriptor()
                    println("desc3===>" + desc3)
                    println("parent===>" + parent)
                    println("name2===>" + name2)
                    if (desc3 == DESC && parent == PARENT && name2 == NAME) {
                        Handle handle = (Handle) bootstrapMethodArguments[1]
                        println("handle.name===>" + handle.name)
                        println("handle.desc===>" + handle.desc)
                        mLambdaNames.add(handle.name + handle.desc)
                    }
                    println("+++++++++++++++++++++++++++++++++++++++++++++++")
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()
                if (mLambdaNames.contains(nameDesc)) {

                }

                if (mInterfaces != null && mInterfaces.length > 0) {
                    if ((mInterfaces.contains('android/view/View$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V')) {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, PATH, "test", "(Ljava/lang/String;)V", false)
                    }
                }
            }

            @Override
            void visitEnd() {
                super.visitEnd()
                if (mLambdaNames.contains(nameDesc)) {
                    mLambdaNames.remove(nameDesc)
                }
            }
        }

        return methodVisitor
    }

}
