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

    public static boolean isShouldHit(List<String> filterPackageName, String classPath) {
        System.out.println("classPath ---> " + classPath);
        for (String packageName : filterPackageName) {
            if (classPath.contains(packageName)) {
                return true;
            }
        }

        if (classPath.contains("R$") ||
                classPath.contains("R2$") ||
                classPath.contains("R.class") ||
                classPath.contains("R2.class") ||
                classPath.contains("BuildConfig.class")) {
            return false;
        }

        return false;
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
        System.out.println("start ams");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new DoubleClickClassVisitor(classWriter);
        ClassReader classReader = new ClassReader(srcClassBytes);
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        System.out.println("end ams");
        return classWriter.toByteArray();
    }

}
