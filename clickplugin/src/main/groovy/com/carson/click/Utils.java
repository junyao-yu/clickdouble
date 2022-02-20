package com.carson.click;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;


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

    public static File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        try {
            /**
             * 读取原 jar
             */
            JarFile file = new JarFile(jarFile, false);

            /**
             * 设置输出到的 jar
             */
            String hexName = "";
            if (nameHex) {
                hexName = DigestUtils.md5Hex(jarFile.getAbsolutePath()).substring(0, 8);
            }
            File outputJar = new File(tempDir, hexName + jarFile.getName());
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
            Enumeration enumeration = file.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                InputStream inputStream = null;
                try {
                    inputStream = file.getInputStream(jarEntry);
                } catch (Exception e) {
                    return null;
                }
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                    //ignore
                } else {
                    String className;
                    JarEntry jarEntry2 = new JarEntry(entryName);
                    jarOutputStream.putNextEntry(jarEntry2);

                    byte[] modifiedClassBytes = null;
                    byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                    if (entryName.endsWith(".class")) {
                        className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "");
                        //todo 暂时先这样，以后优化
                        if (className.contains("com.carson.clickdouble") || className.contains("com.carson.mylibrary")) {
                            System.out.println("className2--->" + className);
                            modifiedClassBytes = modifyClass(sourceClassBytes);
                        }
                    }
                    if (modifiedClassBytes == null) {
                        modifiedClassBytes = sourceClassBytes;
                    }
                    jarOutputStream.write(modifiedClassBytes);
                    jarOutputStream.closeEntry();
                }
            }
            jarOutputStream.close();
            file.close();
            return outputJar;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
