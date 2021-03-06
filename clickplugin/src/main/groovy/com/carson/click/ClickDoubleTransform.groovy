package com.carson.click

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import groovy.io.FileType

class ClickDoubleTransform extends Transform {

    private static final String CLICK_DOUBLE_TRANSFORM_NAME = "clickDouble"
    private Project project
    private ClickDoubleExtension clickDoubleExtension

    ClickDoubleTransform(Project project, ClickDoubleExtension clickDoubleExtension) {
        this.project = project
        this.clickDoubleExtension = clickDoubleExtension
    }

    @Override
    String getName() {
        return CLICK_DOUBLE_TRANSFORM_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        project.logger.warn("clickDoubleExtension.isOpen--->" + clickDoubleExtension.isOpen)
        if (clickDoubleExtension.isOpen) {
            transform(transformInvocation.getContext(),
                    transformInvocation.inputs,
                    transformInvocation.outputProvider,
                    transformInvocation.isIncremental()
            )
        } else {
            super.transform(transformInvocation)
        }
    }

    void transform(@NonNull Context context,
                           @NonNull Collection<TransformInput> inputs,
                           @Nullable TransformOutputProvider outputProvider,
                           boolean isIncremental) {
        /**如果不是增量更新，每次都清空*/
        if (!isIncremental) outputProvider.deleteAll()

        inputs.each { TransformInput input ->

            /**源码方式参与项目编译的所有目录结构及其目录下的源码文件*/
            input.directoryInputs.each { DirectoryInput directoryInput ->

                project.logger.warn("=============================================")

                File destFile = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                /***
                 * /Users/yujunyao/git/clickdouble/app/build/intermediates/transforms/clickDouble/debug/0 //放java文件
                 *
                 * /Users/yujunyao/git/clickdouble/app/build/intermediates/transforms/clickDouble/debug/1 //放kotlin文件
                 */

                project.logger.warn("destFile.absolutePath--->" + destFile.absolutePath)

                File dirFile = directoryInput.file
                /***
                 * /Users/yujunyao/git/clickdouble/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes
                 *
                 * /Users/yujunyao/git/clickdouble/app/build/tmp/kotlin-classes/debug
                 */
                project.logger.warn("dirFile.absolutePath--->" + dirFile.absolutePath)

                if (dirFile) {
                    /**比较杂，只需要.class后缀的文件*/
//                    dirFile.traverse { File classFile ->
//                        project.logger.warn("classFile.absolutePath--->" + classFile.absolutePath)
//                    }
                    HashMap<String, File> modifyMap = new HashMap<>()
                    /**筛选.class后缀的文件*/
                    dirFile.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
                        project.logger.warn("classFile.absolutePath--->" + classFile.absolutePath)
                        if (Utils.isShouldHit(clickDoubleExtension.filterPackageName, classFile.absolutePath)) {
                            project.logger.warn("classFile.absolutePath--->" + classFile.absolutePath)

                            File fileModified = Utils.modifyClassFile(dirFile, classFile, context.getTemporaryDir())

                            if (fileModified != null) {
                                String key = classFile.absolutePath.replace(dirFile.absolutePath, "")
                                modifyMap.put(key, fileModified)
                            }
                        }
                    }

                    /**复制到transforms/clickDouble/debug/目录下*/
                    FileUtils.copyDirectory(directoryInput.file, destFile)

                    /**extension扩展配置的参数*/
//                    clickDoubleExtension.filterPackageName.each { String packageName ->
//                        project.logger.warn("packageName--->" + packageName)
//                    }

                    modifyMap.entrySet().each {
                        Map.Entry<String, File> entry ->
                            File target = new File(destFile.absolutePath + entry.getKey())
                            if (target.exists()) {
                                target.delete()
                            }
                            FileUtils.copyFile(entry.getValue(), target)
                            entry.getValue().delete()
                    }

                }

                project.logger.warn("=============================================")
            }

            /**以jar包方式参与项目编译的所有本地jar包和远程jar包，包括aar*/
            input.jarInputs.each { JarInput jarInput ->

                String destName = jarInput.file.name

                String hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)

                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                File modifyFile = Utils.modifyJar(jarInput.file, context.getTemporaryDir(), true)

                if (modifyFile == null) {
                    modifyFile = jarInput.file
                }

                FileUtils.copyFile(modifyFile, dest)
            }

        }
    }
}
