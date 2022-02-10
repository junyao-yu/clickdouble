package com.carson.click;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;


public class Utils {

    public static boolean isShouldHit(List<String> filterPackageName, String className) {
        for (String packageName : filterPackageName) {
            if (className.contains(packageName)) {
                return true;
            }
        }

        if (className.contains("R$") ||
                className.contains("R2$") ||
                className.contains("R.class") ||
                className.contains("R2.class") ||
                className.contains("BuildConfig.class")) {
            return false;
        }

        return true;
    }

    public static String pathNameConvert(String pathName) {
        return pathName.replace(File.separator, ".").replace(".class", "");
    }

    public static File modifyClassFile(File dir, File classFile, File tempDir) {
        File fileModified = null;
        try {
            String className = pathNameConvert(classFile.getAbsolutePath().replace(dir.getAbsolutePath() + File.separator, ""));
            byte[] srcClassBytes = IOUtils.toByteArray(new FileInputStream(classFile));
            //ams修改class文件
            byte[] dstClassBytes = modifyClass(srcClassBytes);
            if (dstClassBytes != null) {
                fileModified = new File(tempDir, className.replace(".", "") + ".class");
                if (fileModified.exists()) {
                    fileModified.delete();
                }
                fileModified.createNewFile();
                new FileOutputStream(fileModified).write(dstClassBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileModified = classFile;
        }
        return fileModified;
    }

    private static byte[] modifyClass(byte[] srcClassBytes) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new DoubleClickClassVisitor(classWriter);
        ClassReader classReader = new ClassReader(srcClassBytes);
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }

}
