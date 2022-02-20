package com.carson.click

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 *  修改字节码
 */
class DoubleClickClassVisitor extends ClassVisitor implements Opcodes {

    private static final String PATH = "com/carson/clickdouble/ClickUtils"

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
        println("+++++++++++++++++++++++++++++++++++++++++++++++")
        String nameDesc = name + descriptor
        /**
         * name===>lambda$onCreate$0
         * descriptor===>(Landroid/view/View;)V
         */
        println "name===>" + name
        println "descriptor===>" + descriptor
        methodVisitor = new DefaultMethodVisitor(methodVisitor, access, name, descriptor) {

            @Override
            void visitInvokeDynamicInsn(String name2, String descriptor2, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                super.visitInvokeDynamicInsn(name2, descriptor2, bootstrapMethodHandle, bootstrapMethodArguments)
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

                } catch (Exception e) {
                    e.printStackTrace()
                }
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()
                if (mLambdaNames.contains(nameDesc)) {
                    Type[] types = Type.getArgumentTypes(DESC)
                    int length = types.length
                    /**
                     * (Landroid/view/View;)V arguments ===>1
                     * descriptor arguments ===>1
                     * i===>0, index==>1
                     */
                    println("(Landroid/view/View;)V arguments ===>" + length)
                    Type[] lambdaTypes = Type.getArgumentTypes(descriptor)
                    println("descriptor arguments ===>" + lambdaTypes.length)
                    int paramStart = lambdaTypes.length - length
                    if (paramStart < 0) {
                        return
                    } else {
                        for (int i = 0; i < length; i++) {
                            if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                                return
                            }
                        }
                    }

                    for (int i = paramStart; i < paramStart + 1; i++) {
                        def position = getVisitPosition(lambdaTypes, i, false)
                        println("i===>" + i + ", index==>" + position)
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, position)
                    }
                    insertIfCondition(methodVisitor)
                    return
                }

                if (mInterfaces != null && mInterfaces.length > 0) {
                    if ((mInterfaces.contains('android/view/View$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V')) {
//                        methodVisitor.visitVarInsn(ALOAD, 1)
//                        methodVisitor.visitMethodInsn(INVOKESTATIC, PATH, "test", "(Ljava/lang/String;)V", false)
                        insertIfCondition(methodVisitor)
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
        println("+++++++++++++++++++++++++++++++++++++++++++++++")
        return methodVisitor
    }

    private static void insertIfCondition(MethodVisitor methodVisitor) {
        methodVisitor.visitMethodInsn(INVOKESTATIC, PATH, "preventRepeatClick", "()Z", false)
        Label l0 = new Label()
        methodVisitor.visitJumpInsn(IFEQ, l0)
        methodVisitor.visitInsn(RETURN)
        methodVisitor.visitLabel(l0)
    }

    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error")
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize()
        }
    }
}
